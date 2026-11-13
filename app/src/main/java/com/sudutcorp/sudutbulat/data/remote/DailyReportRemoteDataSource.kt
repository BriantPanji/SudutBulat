package com.sudutcorp.sudutbulat.data.remote

import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.sudutcorp.sudutbulat.data.model.DailyReport
import com.sudutcorp.sudutbulat.data.model.DailyReportDamagedMaterial
import com.sudutcorp.sudutbulat.data.model.DailyReportExpense
import com.sudutcorp.sudutbulat.data.model.DailyReportProduct
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class DailyReportRemoteDataSource(
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

    fun getDailyReportsFlow(outletId: String? = null): Flow<List<DailyReport>> = callbackFlow {
        val col = dailyReportsCollection
        if (col == null) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }

        var query: Query = col
        if (!outletId.isNullOrBlank()) {
            query = query.whereEqualTo("outlet_id", outletId)
        }
        query = query.orderBy("date", Query.Direction.DESCENDING)

        val registration = query.addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }
            val reports = snapshot?.toObjects(DailyReport::class.java) ?: emptyList()
            trySend(reports)
        }

        awaitClose { registration.remove() }
    }

    suspend fun getDailyReports(outletId: String? = null): Result<List<DailyReport>> = runCatching {
        val col = dailyReportsCollection ?: throw IllegalStateException("Layanan Firestore tidak tersedia.")
        var query: Query = col
        if (!outletId.isNullOrBlank()) {
            query = query.whereEqualTo("outlet_id", outletId)
        }
        query = query.orderBy("date", Query.Direction.DESCENDING)

        val snapshot = query.get().await()
        snapshot.toObjects(DailyReport::class.java)
    }

    suspend fun getDailyReportById(id: String): Result<DailyReport?> = runCatching {
        val col = dailyReportsCollection ?: throw IllegalStateException("Layanan Firestore tidak tersedia.")
        val snapshot = col.document(id).get().await()
        if (snapshot.exists()) snapshot.toObject(DailyReport::class.java) else null
    }

    fun getDailyReportFlow(id: String): Flow<DailyReport?> = callbackFlow {
        val col = dailyReportsCollection
        if (col == null) {
            trySend(null)
            close()
            return@callbackFlow
        }

        val registration = col.document(id).addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }
            trySend(snapshot?.toObject(DailyReport::class.java))
        }

        awaitClose { registration.remove() }
    }

    suspend fun getDailyReportByOutletAndDate(
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

        val documents = snapshot.toObjects(DailyReport::class.java)
        documents.firstOrNull()
    }

    /**
     * Atomically creates a DailyReport along with its products, expenses, and damaged materials in a single batch.
     */
    suspend fun createDailyReport(
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

        val reportDoc = if (report.id.isNotBlank()) {
            reportCol.document(report.id)
        } else {
            reportCol.document()
        }
        val reportToSave = if (report.id.isBlank()) report.copy(id = reportDoc.id) else report
        batch.set(reportDoc, reportToSave)

        products.forEach { prod ->
            val doc = if (prod.id.isNotBlank()) prodCol.document(prod.id) else prodCol.document()
            batch.set(doc, prod.copy(id = doc.id, dailyReportId = reportDoc.id))
        }

        expenses.forEach { exp ->
            val doc = if (exp.id.isNotBlank()) expCol.document(exp.id) else expCol.document()
            batch.set(doc, exp.copy(id = doc.id, dailyReportId = reportDoc.id))
        }

        damagedMaterials.forEach { dmg ->
            val doc = if (dmg.id.isNotBlank()) dmgCol.document(dmg.id) else dmgCol.document()
            batch.set(doc, dmg.copy(id = doc.id, dailyReportId = reportDoc.id))
        }

        batch.commit().await()
        reportDoc.id
    }

    suspend fun getDailyReportProducts(dailyReportId: String): Result<List<DailyReportProduct>> = runCatching {
        val col = reportProductsCollection ?: throw IllegalStateException("Layanan Firestore tidak tersedia.")
        val snapshot = col.whereEqualTo("daily_report_id", dailyReportId).get().await()
        snapshot.toObjects(DailyReportProduct::class.java)
    }

    suspend fun getDailyReportExpenses(dailyReportId: String): Result<List<DailyReportExpense>> = runCatching {
        val col = reportExpensesCollection ?: throw IllegalStateException("Layanan Firestore tidak tersedia.")
        val snapshot = col.whereEqualTo("daily_report_id", dailyReportId).get().await()
        snapshot.toObjects(DailyReportExpense::class.java)
    }

    suspend fun getDailyReportDamagedMaterials(dailyReportId: String): Result<List<DailyReportDamagedMaterial>> = runCatching {
        val col = reportDamagedMaterialsCollection ?: throw IllegalStateException("Layanan Firestore tidak tersedia.")
        val snapshot = col.whereEqualTo("daily_report_id", dailyReportId).get().await()
        snapshot.toObjects(DailyReportDamagedMaterial::class.java)
    }

    suspend fun addExpense(expense: DailyReportExpense): Result<String> = runCatching {
        val col = reportExpensesCollection ?: throw IllegalStateException("Layanan Firestore tidak tersedia.")
        val doc = if (expense.id.isNotBlank()) col.document(expense.id) else col.document()
        val dataToSave = if (expense.id.isBlank()) expense.copy(id = doc.id) else expense
        doc.set(dataToSave).await()
        doc.id
    }

    suspend fun addDamagedMaterial(damagedMaterial: DailyReportDamagedMaterial): Result<String> = runCatching {
        val col = reportDamagedMaterialsCollection ?: throw IllegalStateException("Layanan Firestore tidak tersedia.")
        val doc = if (damagedMaterial.id.isNotBlank()) col.document(damagedMaterial.id) else col.document()
        val dataToSave = if (damagedMaterial.id.isBlank()) damagedMaterial.copy(id = doc.id) else damagedMaterial
        doc.set(dataToSave).await()
        doc.id
    }
}
