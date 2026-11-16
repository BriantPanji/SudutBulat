package com.sudutcorp.sudutbulat.data.model

/**
 * Composite data holder for a daily report header and all its sub-collections
 * (sold products, operational expenses, and damaged materials).
 */
data class FullDailyReport(
    val report: DailyReport,
    val products: List<DailyReportProduct>,
    val expenses: List<DailyReportExpense>,
    val damagedMaterials: List<DailyReportDamagedMaterial>
)
