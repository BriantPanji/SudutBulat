package com.sudutcorp.sudutbulat.data.repository

import com.google.firebase.Timestamp
import com.sudutcorp.sudutbulat.data.model.DailyReport
import com.sudutcorp.sudutbulat.data.model.DailyReportDamagedMaterial
import com.sudutcorp.sudutbulat.data.model.DailyReportExpense
import com.sudutcorp.sudutbulat.data.model.DailyReportProduct
import com.sudutcorp.sudutbulat.data.model.FullDailyReport
import com.sudutcorp.sudutbulat.data.remote.DailyReportRemoteDataSource
import kotlinx.coroutines.flow.Flow

/**
 * Repository for managing daily reports and outlet sales data.
 * Wraps [DailyReportRemoteDataSource].
 */
class DailyReportRepository(
    private val remoteDataSource: DailyReportRemoteDataSource
) {
    fun getDailyReportsFlow(outletId: String? = null): Flow<List<DailyReport>> {
        return remoteDataSource.getDailyReportsFlow(outletId)
    }

    suspend fun getDailyReports(outletId: String? = null): Result<List<DailyReport>> {
        return remoteDataSource.getDailyReports(outletId)
    }

    suspend fun getDailyReportById(id: String): Result<DailyReport?> {
        return remoteDataSource.getDailyReportById(id)
    }

    fun getDailyReportFlow(id: String): Flow<DailyReport?> {
        return remoteDataSource.getDailyReportFlow(id)
    }

    suspend fun getDailyReportByOutletAndDate(
        outletId: String,
        startDate: Timestamp,
        endDate: Timestamp
    ): Result<DailyReport?> {
        return remoteDataSource.getDailyReportByOutletAndDate(outletId, startDate, endDate)
    }

    /**
     * Retrieves the complete daily report aggregate with all items, expenses, and damaged materials.
     * Returns null if report with [reportId] does not exist.
     */
    suspend fun getFullDailyReport(reportId: String): Result<FullDailyReport?> = runCatching {
        val report = remoteDataSource.getDailyReportById(reportId).getOrThrow() ?: return@runCatching null
        val products = remoteDataSource.getDailyReportProducts(reportId).getOrThrow()
        val expenses = remoteDataSource.getDailyReportExpenses(reportId).getOrThrow()
        val damagedMaterials = remoteDataSource.getDailyReportDamagedMaterials(reportId).getOrThrow()
        FullDailyReport(
            report = report,
            products = products,
            expenses = expenses,
            damagedMaterials = damagedMaterials
        )
    }

    /**
     * Atomically creates a DailyReport along with its products, expenses, and damaged materials.
     */
    suspend fun createDailyReport(
        report: DailyReport,
        products: List<DailyReportProduct> = emptyList(),
        expenses: List<DailyReportExpense> = emptyList(),
        damagedMaterials: List<DailyReportDamagedMaterial> = emptyList()
    ): Result<String> {
        return remoteDataSource.createDailyReport(
            report = report,
            products = products,
            expenses = expenses,
            damagedMaterials = damagedMaterials
        )
    }

    suspend fun getDailyReportProducts(dailyReportId: String): Result<List<DailyReportProduct>> {
        return remoteDataSource.getDailyReportProducts(dailyReportId)
    }

    suspend fun getDailyReportExpenses(dailyReportId: String): Result<List<DailyReportExpense>> {
        return remoteDataSource.getDailyReportExpenses(dailyReportId)
    }

    suspend fun getDailyReportDamagedMaterials(dailyReportId: String): Result<List<DailyReportDamagedMaterial>> {
        return remoteDataSource.getDailyReportDamagedMaterials(dailyReportId)
    }

    suspend fun addExpense(expense: DailyReportExpense): Result<String> {
        return remoteDataSource.addExpense(expense)
    }

    suspend fun addDamagedMaterial(damagedMaterial: DailyReportDamagedMaterial): Result<String> {
        return remoteDataSource.addDamagedMaterial(damagedMaterial)
    }
}
