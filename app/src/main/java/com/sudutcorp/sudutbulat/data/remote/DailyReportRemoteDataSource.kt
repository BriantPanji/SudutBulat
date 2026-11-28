package com.sudutcorp.sudutbulat.data.remote

import com.google.firebase.Timestamp
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import com.sudutcorp.sudutbulat.data.model.DailyReport
import com.sudutcorp.sudutbulat.data.model.DailyReportDamagedMaterial
import com.sudutcorp.sudutbulat.data.model.DailyReportExpense
import com.sudutcorp.sudutbulat.data.model.DailyReportProduct
import com.sudutcorp.sudutbulat.data.model.ReportReviewStatus
import com.sudutcorp.sudutbulat.data.remote.FirestoreMappers.toDailyReport
import com.sudutcorp.sudutbulat.data.remote.FirestoreMappers.toDailyReportDamagedMaterials
import com.sudutcorp.sudutbulat.data.remote.FirestoreMappers.toDailyReportExpenses
import com.sudutcorp.sudutbulat.data.remote.FirestoreMappers.toDailyReportProducts
import com.sudutcorp.sudutbulat.data.remote.FirestoreMappers.toFirestoreItemMap
import com.sudutcorp.sudutbulat.data.remote.FirestoreMappers.toFirestoreMap
import com.sudutcorp.sudutbulat.data.remote.FirestoreMappers.toReportDamagedDocMap
import com.sudutcorp.sudutbulat.data.remote.FirestoreMappers.toReportExpensesDocMap
import com.sudutcorp.sudutbulat.data.remote.FirestoreMappers.toReportProductsDocMap
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.util.Calendar

const val MAX_REPORT_EDIT_DAYS_BACK = 3
const val MAX_REPORT_CREATE_DAYS_BACK = 7

fun calendarDaysBetween(target: Timestamp, reference: Timestamp = Timestamp.now()): Int {
    val startOfDayMillis: (Timestamp) -> Long = { ts ->
        Calendar.getInstance().apply {
            time = ts.toDate()
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
    }
    val diffMillis = startOfDayMillis(reference) - startOfDayMillis(target)
    return (diffMillis / 86_400_000L).toInt()
}

fun validateDailyReportDateWindow(
    reportDate: Timestamp,
    isUpdate: Boolean,
    reference: Timestamp = Timestamp.now(),
    allowRejectedOverride: Boolean = false
) {
    val daysAgo = calendarDaysBetween(reportDate, reference)
    require(daysAgo >= 0) {
        "Tanggal laporan tidak boleh di masa depan."
    }
    if (isUpdate) {
        if (!allowRejectedOverride) {
            require(daysAgo <= MAX_REPORT_EDIT_DAYS_BACK) {
                "Laporan yang sudah melewati $MAX_REPORT_EDIT_DAYS_BACK hari terakhir tidak dapat diubah."
            }
        }
    } else {
        require(daysAgo <= MAX_REPORT_CREATE_DAYS_BACK) {
            "Laporan susulan hanya dapat dibuat maksimal $MAX_REPORT_CREATE_DAYS_BACK hari ke belakang."
        }
    }
}

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
     * Atomically creates a DailyReport along with 1 consolidated document per sub-collection
     * (`daily_report_products/{reportId}`, `daily_report_expenses/{reportId}`, `daily_report_damaged_materials/{reportId}`).
     */
    open suspend fun createDailyReport(
        report: DailyReport,
        products: List<DailyReportProduct> = emptyList(),
        expenses: List<DailyReportExpense> = emptyList(),
        damagedMaterials: List<DailyReportDamagedMaterial> = emptyList(),
        allowRejectedOverride: Boolean = false
    ): Result<String> = runCatching {
        val firestore = db ?: throw IllegalStateException("Layanan Firestore tidak tersedia.")
        val reportCol = dailyReportsCollection ?: throw IllegalStateException("Layanan Firestore tidak tersedia.")
        val prodCol = reportProductsCollection ?: throw IllegalStateException("Layanan Firestore tidak tersedia.")
        val expCol = reportExpensesCollection ?: throw IllegalStateException("Layanan Firestore tidak tersedia.")
        val dmgCol = reportDamagedMaterialsCollection ?: throw IllegalStateException("Layanan Firestore tidak tersedia.")

        val batch = firestore.batch()

        val isUpdate = report.id.isNotBlank()
        validateDailyReportDateWindow(
            reportDate = report.date,
            isUpdate = isUpdate,
            allowRejectedOverride = allowRejectedOverride
        )
        val reportDoc = if (isUpdate) reportCol.document(report.id) else reportCol.document()
        val reportId = reportDoc.id
        val reportToSave = if (report.id.isBlank()) report.copy(id = reportId) else report
        batch.set(reportDoc, reportToSave.toFirestoreMap())

        if (products.isNotEmpty()) {
            batch.set(prodCol.document(reportId), products.toReportProductsDocMap(reportId))
        } else if (isUpdate) {
            batch.delete(prodCol.document(reportId))
        }

        if (expenses.isNotEmpty()) {
            batch.set(expCol.document(reportId), expenses.toReportExpensesDocMap(reportId))
        } else if (isUpdate) {
            batch.delete(expCol.document(reportId))
        }

        if (damagedMaterials.isNotEmpty()) {
            batch.set(dmgCol.document(reportId), damagedMaterials.toReportDamagedDocMap(reportId))
        } else if (isUpdate) {
            batch.delete(dmgCol.document(reportId))
        }

        batch.commit().await()
        reportId
    }

    open suspend fun updateDailyReportReview(
        reportId: String,
        status: ReportReviewStatus,
        reviewedBy: String,
        reviewNote: String? = null,
        reviewedAt: Timestamp = Timestamp.now()
    ): Result<Unit> = runCatching {
        require(reportId.isNotBlank()) { "ID laporan harian tidak boleh kosong." }
        val col = dailyReportsCollection ?: throw IllegalStateException("Layanan Firestore tidak tersedia.")
        col.document(reportId).update(
            mapOf(
                "review_status" to status.name,
                "review_note" to reviewNote?.trim()?.takeIf { it.isNotBlank() },
                "reviewed_by" to reviewedBy.trim().ifBlank { "owner" },
                "reviewed_at" to reviewedAt
            )
        ).await()
    }

    open suspend fun getDailyReportProducts(dailyReportId: String): Result<List<DailyReportProduct>> = runCatching {
        val col = reportProductsCollection ?: throw IllegalStateException("Layanan Firestore tidak tersedia.")
        val snapshot = col.document(dailyReportId).get().await()
        if (snapshot.exists()) snapshot.toDailyReportProducts() else emptyList()
    }

    open suspend fun getDailyReportExpenses(dailyReportId: String): Result<List<DailyReportExpense>> = runCatching {
        val col = reportExpensesCollection ?: throw IllegalStateException("Layanan Firestore tidak tersedia.")
        val snapshot = col.document(dailyReportId).get().await()
        if (snapshot.exists()) snapshot.toDailyReportExpenses() else emptyList()
    }

    open suspend fun getDailyReportDamagedMaterials(dailyReportId: String): Result<List<DailyReportDamagedMaterial>> = runCatching {
        val col = reportDamagedMaterialsCollection ?: throw IllegalStateException("Layanan Firestore tidak tersedia.")
        val snapshot = col.document(dailyReportId).get().await()
        if (snapshot.exists()) snapshot.toDailyReportDamagedMaterials() else emptyList()
    }

    open suspend fun addExpense(expense: DailyReportExpense): Result<String> = runCatching {
        val col = reportExpensesCollection ?: throw IllegalStateException("Layanan Firestore tidak tersedia.")
        val docId = expense.dailyReportId?.takeIf { it.isNotBlank() }
            ?: expense.id.takeIf { it.isNotBlank() }
            ?: col.document().id
        val doc = col.document(docId)
        doc.set(
            mapOf(
                "daily_report_id" to docId,
                "items" to FieldValue.arrayUnion(expense.toFirestoreItemMap())
            ),
            SetOptions.merge()
        ).await()
        docId
    }

    open suspend fun addDamagedMaterial(damagedMaterial: DailyReportDamagedMaterial): Result<String> = runCatching {
        val col = reportDamagedMaterialsCollection ?: throw IllegalStateException("Layanan Firestore tidak tersedia.")
        val docId = damagedMaterial.dailyReportId.takeIf { it.isNotBlank() }
            ?: damagedMaterial.id.takeIf { it.isNotBlank() }
            ?: col.document().id
        val doc = col.document(docId)
        doc.set(
            mapOf(
                "daily_report_id" to docId,
                "items" to FieldValue.arrayUnion(damagedMaterial.toFirestoreItemMap())
            ),
            SetOptions.merge()
        ).await()
        docId
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
            trySend(snapshot?.documents?.flatMap { it.toDailyReportProducts() } ?: emptyList())
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
            trySend(snapshot?.documents?.flatMap { it.toDailyReportExpenses() } ?: emptyList())
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
            trySend(snapshot?.documents?.flatMap { it.toDailyReportDamagedMaterials() } ?: emptyList())
        }
        awaitClose { registration.remove() }
    }
}
