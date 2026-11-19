package com.sudutcorp.sudutbulat.data.remote

import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.sudutcorp.sudutbulat.data.model.DailyReport
import com.sudutcorp.sudutbulat.data.model.DailyReportDamagedMaterial
import com.sudutcorp.sudutbulat.data.model.DailyReportExpense
import com.sudutcorp.sudutbulat.data.model.DailyReportProduct
import com.sudutcorp.sudutbulat.data.remote.FirestoreMappers.toDailyReport
import com.sudutcorp.sudutbulat.data.remote.FirestoreMappers.toDailyReportDamagedMaterial
import com.sudutcorp.sudutbulat.data.remote.FirestoreMappers.toDailyReportExpense
import com.sudutcorp.sudutbulat.data.remote.FirestoreMappers.toDailyReportProduct
import com.sudutcorp.sudutbulat.data.remote.FirestoreMappers.toFirestoreMap
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

open class DailyReportRemoteDataSource(
    firestore: FirebaseFirestore? = null
) {
    private val db: FirebaseFirestore? by lazy {
        firestore ?: runCatching { FirebaseFirestore.getInstance() }.getOrNull()
    }

    private val dailyReportsCollection
        get() = db?.collection(FirestoreCollections.DAILY_REPORTS)

    private val reportProductsCollection
        get() = db?.collection(FirestoreCollections.DAILY_REPORT_PRODUCTS)

    private val reportExpensesCollection
        get() = db?.collection(FirestoreCollections.DAILY_REPORT_EXPENSES)

    private val reportDamagedMaterialsCollection
        get() = db?.collection(FirestoreCollections.DAILY_REPORT_DAMAGED_MATERIALS)

    open fun getDailyReportsFlow(outletId: String? = null): Flow<List<DailyReport>> = callbackFlow {
        val col = dailyReportsCollection
        if (col == null) {
            close(IllegalStateException("Layanan Firestore tidak tersedia."))
            return@callbackFlow
        }

        // Avoid requiring a composite index when filtering by outlet_id; sort client-side when filtered
        val query: Query = if (!outletId.isNullOrBlank()) {
            col.whereEqualTo("outlet_id", outletId)
        } else {
            col.orderBy("date", Query.Direction.DESCENDING)
        }

        val registration = query.addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }
            val reports = snapshot?.documents
                ?.map { it.toDailyReport() }
                ?.sortedByDescending { it.date.seconds }
                ?: emptyList()
            trySend(reports)
        }

        awaitClose { registration.remove() }
    }

    open suspend fun getDailyReports(outletId: String? = null): Result<List<DailyReport>> = runCatching {
        val col = dailyReportsCollection ?: throw IllegalStateException("Layanan Firestore tidak tersedia.")
        val query: Query = if (!outletId.isNullOrBlank()) {
            col.whereEqualTo("outlet_id", outletId)
        } else {
            col.orderBy("date", Query.Direction.DESCENDING)
        }
        val snapshot = query.get().await()
        snapshot.documents.map { it.toDailyReport() }.sortedByDescending { it.date.seconds }
    }

    open suspend fun getDailyReportById(id: String): Result<DailyReport?> = runCatching {
        val col = dailyReportsCollection ?: throw IllegalStateException("Layanan Firestore tidak tersedia.")
        val snapshot = col.document(id).get().await()
        if (snapshot.exists()) snapshot.toDailyReport() else null
    }

    open fun getDailyReportFlow(id: String): Flow<DailyReport?> = callbackFlow {
        val col = dailyReportsCollection
        if (col == null) {
            close(IllegalStateException("Layanan Firestore tidak tersedia."))
            return@callbackFlow
        }

        val registration = col.document(id).addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }
            trySend(snapshot?.takeIf { it.exists() }?.toDailyReport())
        }

        awaitClose { registration.remove() }
    }

    open suspend fun getDailyReportByOutletAndDate(
        outletId: String,
        startDate: Timestamp,
        endDate: Timestamp
    ): Result<DailyReport?> = runCatching {
        val col = dailyReportsCollection ?: throw IllegalStateException("Layanan Firestore tidak tersedia.")
        val snapshot = col.whereEqualTo("outlet_id", outletId)
            .whereGreaterThanOrEqualTo("date", startDate)
            .whereLessThanOrEqualTo("date", endDate)
            .limit(1)
            .get()
            .await()

        snapshot.documents.firstOrNull()?.toDailyReport()
    }

    /**
     * Atomically creates a DailyReport along with its products, expenses, and damaged materials in a single batch.
     */
    open suspend fun createDailyReport(
        report: DailyReport,
        products: List<DailyReportProduct> = emptyList(),
        expenses: List<DailyReportExpense> = emptyList(),
        damagedMaterials: List<DailyReportDamagedMaterial> = emptyList()
    ): Result<String> = runCatching {
        val firestore = db ?: throw IllegalStateException("Layanan Firestore tidak tersedia.")
        val reportCol = dailyReportsCollection ?: throw IllegalStateException("Layanan Firestore tidak tersedia.")
        val prodCol = reportProductsCollection ?: throw IllegalStateException("Layanan Firestore tidak tersedia.")
        val expCol = reportExpensesCollection ?: throw IllegalStateException("Layanan Firestore tidak tersedia.")
        val dmgCol = reportDamagedMaterialsCollection ?: throw IllegalStateException("Layanan Firestore tidak tersedia.")

        val batch = firestore.batch()

        val reportDoc = if (report.id.isNotBlank()) reportCol.document(report.id) else reportCol.document()
        val reportToSave = if (report.id.isBlank()) report.copy(id = reportDoc.id) else report
        batch.set(reportDoc, reportToSave.toFirestoreMap())

        products.forEach { prod ->
            val doc = if (prod.id.isNotBlank()) prodCol.document(prod.id) else prodCol.document()
            batch.set(doc, prod.copy(id = doc.id, dailyReportId = reportDoc.id).toFirestoreMap())
        }

        expenses.forEach { exp ->
            val doc = if (exp.id.isNotBlank()) expCol.document(exp.id) else expCol.document()
            batch.set(doc, exp.copy(id = doc.id, dailyReportId = reportDoc.id).toFirestoreMap())
        }

        damagedMaterials.forEach { dmg ->
            val doc = if (dmg.id.isNotBlank()) dmgCol.document(dmg.id) else dmgCol.document()
            batch.set(doc, dmg.copy(id = doc.id, dailyReportId = reportDoc.id).toFirestoreMap())
        }

        batch.commit().await()
        reportDoc.id
    }

    open suspend fun getDailyReportProducts(dailyReportId: String): Result<List<DailyReportProduct>> = runCatching {
        val col = reportProductsCollection ?: throw IllegalStateException("Layanan Firestore tidak tersedia.")
        val snapshot = col.whereEqualTo("daily_report_id", dailyReportId).get().await()
        snapshot.documents.map { it.toDailyReportProduct() }
    }

    open suspend fun getDailyReportExpenses(dailyReportId: String): Result<List<DailyReportExpense>> = runCatching {
        val col = reportExpensesCollection ?: throw IllegalStateException("Layanan Firestore tidak tersedia.")
        val snapshot = col.whereEqualTo("daily_report_id", dailyReportId).get().await()
        snapshot.documents.map { it.toDailyReportExpense() }
    }

    open suspend fun getDailyReportDamagedMaterials(dailyReportId: String): Result<List<DailyReportDamagedMaterial>> = runCatching {
        val col = reportDamagedMaterialsCollection ?: throw IllegalStateException("Layanan Firestore tidak tersedia.")
        val snapshot = col.whereEqualTo("daily_report_id", dailyReportId).get().await()
        snapshot.documents.map { it.toDailyReportDamagedMaterial() }
    }

    open suspend fun addExpense(expense: DailyReportExpense): Result<String> = runCatching {
        val col = reportExpensesCollection ?: throw IllegalStateException("Layanan Firestore tidak tersedia.")
        val doc = if (expense.id.isNotBlank()) col.document(expense.id) else col.document()
        val dataToSave = if (expense.id.isBlank()) expense.copy(id = doc.id) else expense
        doc.set(dataToSave.toFirestoreMap()).await()
        doc.id
    }

    open suspend fun addDamagedMaterial(damagedMaterial: DailyReportDamagedMaterial): Result<String> = runCatching {
        val col = reportDamagedMaterialsCollection ?: throw IllegalStateException("Layanan Firestore tidak tersedia.")
        val doc = if (damagedMaterial.id.isNotBlank()) col.document(damagedMaterial.id) else col.document()
        val dataToSave = if (damagedMaterial.id.isBlank()) damagedMaterial.copy(id = doc.id) else damagedMaterial
        doc.set(dataToSave.toFirestoreMap()).await()
        doc.id
    }

    open fun getAllReportProductsFlow(): Flow<List<DailyReportProduct>> = callbackFlow {
        val col = reportProductsCollection
        if (col == null) {
            close(IllegalStateException("Layanan Firestore tidak tersedia."))
            return@callbackFlow
        }
        val registration = col.addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }
            trySend(snapshot?.documents?.map { it.toDailyReportProduct() } ?: emptyList())
        }
        awaitClose { registration.remove() }
    }

    open fun getAllExpensesFlow(): Flow<List<DailyReportExpense>> = callbackFlow {
        val col = reportExpensesCollection
        if (col == null) {
            close(IllegalStateException("Layanan Firestore tidak tersedia."))
            return@callbackFlow
        }
        val registration = col.addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }
            trySend(snapshot?.documents?.map { it.toDailyReportExpense() } ?: emptyList())
        }
        awaitClose { registration.remove() }
    }

    open fun getAllDamagedMaterialsFlow(): Flow<List<DailyReportDamagedMaterial>> = callbackFlow {
        val col = reportDamagedMaterialsCollection
        if (col == null) {
            close(IllegalStateException("Layanan Firestore tidak tersedia."))
            return@callbackFlow
        }
        val registration = col.addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }
            trySend(snapshot?.documents?.map { it.toDailyReportDamagedMaterial() } ?: emptyList())
        }
        awaitClose { registration.remove() }
    }
}
