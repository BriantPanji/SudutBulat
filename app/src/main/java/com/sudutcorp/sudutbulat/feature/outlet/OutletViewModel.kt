package com.sudutcorp.sudutbulat.feature.outlet

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Timestamp
import com.sudutcorp.sudutbulat.data.auth.AuthRepository
import com.sudutcorp.sudutbulat.data.model.DailyReport
import com.sudutcorp.sudutbulat.data.model.DailyReportDamagedMaterial
import com.sudutcorp.sudutbulat.data.model.DailyReportExpense
import com.sudutcorp.sudutbulat.data.model.DailyReportProduct
import com.sudutcorp.sudutbulat.data.model.DistributionStatus
import com.sudutcorp.sudutbulat.data.model.DistributionType
import com.sudutcorp.sudutbulat.data.model.ExpenseCategory
import com.sudutcorp.sudutbulat.data.model.Material
import com.sudutcorp.sudutbulat.data.model.MaterialDistribution
import com.sudutcorp.sudutbulat.data.model.MaterialDistributionItem
import com.sudutcorp.sudutbulat.data.model.Outlet
import com.sudutcorp.sudutbulat.data.model.Product
import com.sudutcorp.sudutbulat.data.model.Role
import com.sudutcorp.sudutbulat.data.model.User
import com.sudutcorp.sudutbulat.data.remote.MAX_REPORT_CREATE_DAYS_BACK
import com.sudutcorp.sudutbulat.data.remote.MAX_REPORT_EDIT_DAYS_BACK
import com.sudutcorp.sudutbulat.data.remote.calendarDaysBetween
import com.sudutcorp.sudutbulat.core.util.buildInitials
import com.sudutcorp.sudutbulat.core.util.formatDecimal
import com.sudutcorp.sudutbulat.core.util.formatRupiah
import com.sudutcorp.sudutbulat.core.util.formatShortRupiah
import com.sudutcorp.sudutbulat.feature.owner.DamagedSummaryItemUi
import com.sudutcorp.sudutbulat.feature.owner.ReportDamagedLineUi
import com.sudutcorp.sudutbulat.feature.owner.ReportExpenseLineUi
import com.sudutcorp.sudutbulat.feature.owner.ReportProductLineUi
import com.sudutcorp.sudutbulat.data.repository.DailyReportRepository
import com.sudutcorp.sudutbulat.data.repository.MaterialDistributionRepository
import com.sudutcorp.sudutbulat.data.repository.MaterialRepository
import com.sudutcorp.sudutbulat.data.repository.OutletRepository
import com.sudutcorp.sudutbulat.data.repository.ProductRepository
import com.sudutcorp.sudutbulat.data.repository.UserRepository
import com.sudutcorp.sudutbulat.feature.distribution.partitionExpiredDraftDistributions
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

internal fun isSameCalendarDay(a: Timestamp, b: Timestamp): Boolean {
    val calA = Calendar.getInstance().apply { time = a.toDate() }
    val calB = Calendar.getInstance().apply { time = b.toDate() }
    return calA.get(Calendar.YEAR) == calB.get(Calendar.YEAR) &&
        calA.get(Calendar.DAY_OF_YEAR) == calB.get(Calendar.DAY_OF_YEAR)
}

internal fun calendarDayBounds(reference: Timestamp): Pair<Timestamp, Timestamp> {
    val start = Calendar.getInstance().apply {
        time = reference.toDate()
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }
    val end = Calendar.getInstance().apply {
        time = reference.toDate()
        set(Calendar.HOUR_OF_DAY, 23)
        set(Calendar.MINUTE, 59)
        set(Calendar.SECOND, 59)
        set(Calendar.MILLISECOND, 999)
    }
    return Timestamp(start.time) to Timestamp(end.time)
}

data class OutletDistributionItemUi(
    val id: String,
    val materialId: String,
    val materialName: String,
    val unitLabel: String,
    val quantitySent: Int,
    val quantityReceived: Int?,
    val note: String? = null
)

data class OutletDistributionUi(
    val id: String,
    val status: DistributionStatus,
    val type: DistributionType = DistributionType.ROUTINE,
    val timeLabel: String,
    val isToday: Boolean = false,
    val createdAtMillis: Long = 0L,
    val createdByName: String = "",
    val confirmedByName: String? = null,
    val sentTimeLabel: String? = null,
    val confirmedTimeLabel: String? = null,
    val note: String?,
    val confirmationNote: String?,
    val items: List<OutletDistributionItemUi>,
    val totalSent: Int,
    val totalReceived: Int? = null
)

enum class OutletStockFilter(val label: String) {
    ALL("Semua"),
    NEED_CONFIRM("Perlu Konfirmasi"),
    REQUEST("Permintaan"),
    DONE("Selesai")
}

internal fun resolveConfirmationStatus(sentAndReceived: List<Pair<Int, Int>>): DistributionStatus = when {
    sentAndReceived.isEmpty() -> DistributionStatus.RECEIVED
    sentAndReceived.all { (_, rec) -> rec <= 0 } -> DistributionStatus.NOT_RECEIVED
    sentAndReceived.all { (sent, rec) -> rec >= sent } -> DistributionStatus.RECEIVED
    else -> DistributionStatus.PARTIALLY_RECEIVED
}

internal fun filterOutletDistributions(
    distributions: List<OutletDistributionUi>,
    filter: OutletStockFilter,
    searchQuery: String
): List<OutletDistributionUi> {
    val q = searchQuery.trim().lowercase(Locale.ROOT)
    return distributions.filter { dist ->
        val matchesStatus = when (filter) {
            OutletStockFilter.ALL -> true
            OutletStockFilter.NEED_CONFIRM -> dist.status == DistributionStatus.SENT
            OutletStockFilter.REQUEST -> dist.status == DistributionStatus.DRAFT
            OutletStockFilter.DONE -> dist.status == DistributionStatus.RECEIVED ||
                dist.status == DistributionStatus.PARTIALLY_RECEIVED ||
                dist.status == DistributionStatus.NOT_RECEIVED ||
                dist.status == DistributionStatus.CANCELLED
        }
        if (!matchesStatus) return@filter false
        if (q.isBlank()) return@filter true

        dist.createdByName.lowercase(Locale.ROOT).contains(q) ||
            dist.type.label.lowercase(Locale.ROOT).contains(q) ||
            (dist.note?.lowercase(Locale.ROOT)?.contains(q) == true) ||
            (dist.confirmationNote?.lowercase(Locale.ROOT)?.contains(q) == true) ||
            dist.items.any { item ->
                item.materialName.lowercase(Locale.ROOT).contains(q) ||
                    (item.note?.lowercase(Locale.ROOT)?.contains(q) == true)
            }
    }
}

data class DraftExpenseEntry(
    val id: String,
    val category: ExpenseCategory,
    val amount: Long,
    val note: String
)

data class DraftDamagedEntry(
    val id: String,
    val materialId: String,
    val materialName: String,
    val quantity: Int,
    val note: String
)

data class StockRequestFormState(
    val itemQuantities: Map<String, String> = emptyMap(),
    val note: String = "",
    val isSaving: Boolean = false,
    val errorMessage: String? = null
)

enum class OutletReportTabMode(val label: String) {
    SUMMARY("Ringkasan"),
    HISTORY("Riwayat Laporan")
}

enum class OutletReportHistoryFilter(val label: String) {
    ALL("Semua"),
    EDITABLE("Bisa Diedit (≤3 Hari)"),
    DAYS_7("7 Hari"),
    DAYS_30("30 Hari")
}

data class ReportDateCandidateUi(
    val timestamp: Timestamp,
    val dayKey: String,
    val label: String,
    val shortLabel: String,
    val daysAgo: Int,
    val hasExistingReport: Boolean
)

data class OutletReportHistoryItemUi(
    val id: String,
    val date: Timestamp,
    val dateMillis: Long,
    val dateLabel: String,
    val fullDateLabel: String,
    val timeLabel: String,
    val daysAgo: Int,
    val createdById: String,
    val createdByName: String,
    val createdByInitials: String,
    val cashRevenue: Long,
    val qrisRevenue: Long,
    val totalRevenue: Long,
    val totalSold: Int,
    val totalExpense: Long,
    val damagedCount: Int,
    val netDeposit: Long,
    val isEditable: Boolean,
    val productLines: List<ReportProductLineUi> = emptyList(),
    val expenseLines: List<ReportExpenseLineUi> = emptyList(),
    val damagedLines: List<ReportDamagedLineUi> = emptyList()
)

data class OutletReportPeriodSummaryUi(
    val periodLabel: String = "7 hari",
    val dateRangeText: String = "",
    val reportCount: Int = 0,
    val netRevenueText: String = "Rp0",
    val deltaText: String = "Periode berjalan",
    val isPositiveDelta: Boolean = true,
    val totalSalesValueText: String = "Rp0",
    val totalProductsSoldText: String = "0 pcs terjual",
    val cashSalesText: String = "Rp0",
    val qrisSalesText: String = "Rp0",
    val gasExpenseText: String = "Rp0",
    val plasticExpenseText: String = "Rp0",
    val otherExpenseText: String = "Rp0",
    val operationalCostValueText: String = "Rp0",
    val operationalCostPercentText: String = "0% dari omzet",
    val estimatedProfitValueText: String = "Rp0",
    val trendBars: List<Pair<String, Float>> = emptyList(),
    val damagedItems: List<DamagedSummaryItemUi> = emptyList()
)

internal fun isReportEditable(reportDate: Timestamp, reference: Timestamp = Timestamp.now()): Boolean {
    val daysAgo = calendarDaysBetween(reportDate, reference)
    return daysAgo in 0..MAX_REPORT_EDIT_DAYS_BACK
}

internal fun isReportDateCreatable(
    targetDate: Timestamp,
    existingReports: List<DailyReport>,
    reference: Timestamp = Timestamp.now()
): Boolean {
    val daysAgo = calendarDaysBetween(targetDate, reference)
    if (daysAgo !in 0..MAX_REPORT_CREATE_DAYS_BACK) return false
    return existingReports.none { isSameCalendarDay(it.date, targetDate) }
}

internal fun buildReportDateCandidates(
    existingReports: List<DailyReport>,
    reference: Timestamp = Timestamp.now()
): List<ReportDateCandidateUi> {
    val idLocale = Locale.forLanguageTag("id-ID")
    val keyFmt = SimpleDateFormat("yyyy-MM-dd", Locale.US)
    val shortFmt = SimpleDateFormat("dd MMM", idLocale)
    val dayNameFmt = SimpleDateFormat("EEE, dd MMM", idLocale)

    return (0..MAX_REPORT_CREATE_DAYS_BACK).map { daysAgo ->
        val cal = Calendar.getInstance().apply {
            time = reference.toDate()
            add(Calendar.DAY_OF_YEAR, -daysAgo)
        }
        val ts = Timestamp(cal.time)
        val shortDate = shortFmt.format(cal.time)
        val label = when (daysAgo) {
            0 -> "Hari ini ($shortDate)"
            1 -> "Kemarin ($shortDate)"
            else -> dayNameFmt.format(cal.time)
        }
        val shortLabel = when (daysAgo) {
            0 -> "Hari ini"
            1 -> "Kemarin"
            else -> dayNameFmt.format(cal.time)
        }
        val hasReport = existingReports.any { isSameCalendarDay(it.date, ts) }
        ReportDateCandidateUi(
            timestamp = ts,
            dayKey = keyFmt.format(cal.time),
            label = label,
            shortLabel = shortLabel,
            daysAgo = daysAgo,
            hasExistingReport = hasReport
        )
    }
}

internal fun computeOutletReportPeriodSummary(
    period: String,
    reports: List<DailyReport>,
    expensesByReportId: Map<String, List<DailyReportExpense>>,
    damagedByReportId: Map<String, List<DailyReportDamagedMaterial>>,
    materialMap: Map<String, Material>,
    reference: Timestamp = Timestamp.now()
): OutletReportPeriodSummaryUi {
    val idLocale = Locale.forLanguageTag("id-ID")
    val rangeFmt = SimpleDateFormat("dd MMM", idLocale)
    val dayShortFmt = SimpleDateFormat("EEE", idLocale)

    val daysWindow = when {
        period.contains("7", ignoreCase = true) -> 7
        period.contains("3", ignoreCase = true) && period.contains("Bulan", ignoreCase = true) -> 90
        else -> 30
    }
    val periodLabel = when (daysWindow) {
        7 -> "7 hari"
        90 -> "3 bulan"
        else -> "30 hari"
    }

    val currentReports = reports.filter { calendarDaysBetween(it.date, reference) in 0 until daysWindow }
    val prevReports = reports.filter { calendarDaysBetween(it.date, reference) in daysWindow until (daysWindow * 2) }

    val totalSales = currentReports.sumOf { it.totalRevenue }
    val cashSales = currentReports.sumOf { it.cashRevenue }
    val qrisSales = currentReports.sumOf { it.qrisRevenue }
    val totalSold = currentReports.sumOf { it.totalSold }

    val currentExpenses = currentReports.flatMap { expensesByReportId[it.id].orEmpty() }
    val gasExp = currentExpenses.filter { it.category == ExpenseCategory.GAS }.sumOf { it.amount }
    val plasticExp = currentExpenses.filter { it.category == ExpenseCategory.PLASTIC_BAG }.sumOf { it.amount }
    val otherExp = currentExpenses.filter { it.category == ExpenseCategory.OTHER }.sumOf { it.amount }
    val totalExp = gasExp + plasticExp + otherExp
    val netRevenue = (totalSales - totalExp).coerceAtLeast(0L)

    val prevNet = prevReports.sumOf { rep ->
        val exp = expensesByReportId[rep.id].orEmpty().sumOf { it.amount }
        (rep.totalRevenue - exp).coerceAtLeast(0L)
    }
    val deltaPct = if (prevNet > 0L) {
        ((netRevenue - prevNet).toDouble() / prevNet.toDouble()) * 100.0
    } else {
        0.0
    }
    val isPositiveDelta = deltaPct >= 0.0
    val deltaText = if (prevNet > 0L) {
        val sign = if (deltaPct >= 0.0) "+" else ""
        "${String.format(idLocale, "%s%.1f%%", sign, deltaPct)} dibanding periode lalu"
    } else {
        "${currentReports.size} laporan tercatat pada periode ini"
    }

    val costPct = if (totalSales > 0L) {
        ((totalExp.toDouble() / totalSales.toDouble()) * 100.0)
    } else {
        0.0
    }

    val startCal = Calendar.getInstance().apply {
        time = reference.toDate()
        add(Calendar.DAY_OF_YEAR, -(daysWindow - 1))
    }
    val dateRangeText = "${rangeFmt.format(startCal.time)} – ${rangeFmt.format(reference.toDate())}"

    // 7-day trend bar chart (H-6 .. H)
    val rawDayRevenues = (6 downTo 0).map { daysAgo ->
        val cal = Calendar.getInstance().apply {
            time = reference.toDate()
            add(Calendar.DAY_OF_YEAR, -daysAgo)
        }
        val dayTs = Timestamp(cal.time)
        val dayLabel = dayShortFmt.format(cal.time).take(3)
        val rev = reports.filter { isSameCalendarDay(it.date, dayTs) }.sumOf { it.totalRevenue }
        dayLabel to rev
    }
    val maxDayRev = rawDayRevenues.maxOfOrNull { it.second }?.coerceAtLeast(1L) ?: 1L
    val trendBars = rawDayRevenues.map { (dayLabel, rev) ->
        val fraction = if (rev <= 0L) 0.08f else (rev.toFloat() / maxDayRev.toFloat()).coerceIn(0.15f, 1f)
        dayLabel to fraction
    }

    val currentDamaged = currentReports.flatMap { damagedByReportId[it.id].orEmpty() }
    val damagedSummary = currentDamaged
        .groupBy { it.materialId }
        .map { (matId, list) ->
            val mat = materialMap[matId]
            val unit = mat?.unit?.firestoreValue ?: "pcs"
            DamagedSummaryItemUi(
                materialId = matId,
                name = mat?.name ?: matId,
                quantityText = "${list.sumOf { it.quantity }} $unit"
            )
        }

    return OutletReportPeriodSummaryUi(
        periodLabel = periodLabel,
        dateRangeText = dateRangeText,
        reportCount = currentReports.size,
        netRevenueText = formatRupiah(netRevenue),
        deltaText = deltaText,
        isPositiveDelta = isPositiveDelta,
        totalSalesValueText = formatShortRupiah(totalSales),
        totalProductsSoldText = "${formatDecimal(totalSold)} pcs terjual",
        cashSalesText = formatRupiah(cashSales),
        qrisSalesText = formatRupiah(qrisSales),
        gasExpenseText = formatRupiah(gasExp),
        plasticExpenseText = formatRupiah(plasticExp),
        otherExpenseText = formatRupiah(otherExp),
        operationalCostValueText = formatShortRupiah(totalExp),
        operationalCostPercentText = "${String.format(idLocale, "%.1f%%", costPct)} dari omzet",
        estimatedProfitValueText = formatShortRupiah(netRevenue),
        trendBars = trendBars,
        damagedItems = damagedSummary
    )
}

data class DailyReportFormState(
    val isFormVisible: Boolean = false,
    val selectedCreateDate: Timestamp? = null,
    val editingReportId: String? = null,
    val editingReportDate: Timestamp? = null,
    val pendingDuplicateReport: DailyReport? = null,
    val isLoadingExisting: Boolean = false,
    val productQuantities: Map<String, String> = emptyMap(),
    val cashRevenueInput: String = "",
    val qrisRevenueInput: String = "",
    val expenses: List<DraftExpenseEntry> = emptyList(),
    val damagedMaterials: List<DraftDamagedEntry> = emptyList(),
    val isSaving: Boolean = false,
    val errorMessage: String? = null
)

data class OutletDashboardMetrics(
    val todayDateLabel: String = "",
    val hasTodayReport: Boolean = false,
    val todayReportTimeLabel: String? = null,
    val todayRevenue: Long = 0L,
    val todayCashRevenue: Long = 0L,
    val todayQrisRevenue: Long = 0L,
    val todaySoldPieces: Int = 0,
    val dailyTarget: Long = 2_000_000L,
    val targetProgress: Float = 0f,
    val targetPercent: Int = 0,
    val lastReportDateLabel: String? = null,
    val lastReportRevenue: Long = 0L,
    val lastReportSold: Int = 0,
    val pendingConfirmCount: Int = 0,
    val pendingRequestCount: Int = 0,
    val todayReceivedUnits: Int = 0,
    val morningRoutineStatusLabel: String = "Belum dikirim Gudang"
)

internal fun computeOutletDashboardMetrics(
    assignedOutlet: Outlet?,
    todayReport: DailyReport?,
    recentReports: List<DailyReport>,
    distributions: List<OutletDistributionUi>,
    nowDate: Date = Date()
): OutletDashboardMetrics {
    val idLocale = Locale.forLanguageTag("id-ID")
    val headerDateFmt = SimpleDateFormat("EEEE, dd MMM yyyy", idLocale)
    val shortDateFmt = SimpleDateFormat("dd MMM", idLocale)
    val hourFmt = SimpleDateFormat("HH.mm", idLocale)

    val dailyTarget = (assignedOutlet?.dailyTarget ?: 2_000_000L).coerceAtLeast(1L)
    val todayRev = todayReport?.totalRevenue ?: 0L
    val targetProgress = (todayRev.toFloat() / dailyTarget.toFloat()).coerceIn(0f, 1f)
    val targetPercent = (targetProgress * 100f).toInt().coerceIn(0, 100)

    val previousReport = if (todayReport != null) {
        recentReports.firstOrNull { it.id != todayReport.id }
    } else {
        recentReports.firstOrNull()
    }

    val pendingConfirmCount = distributions.count { it.status == DistributionStatus.SENT }
    val pendingRequestCount = distributions.count { it.status == DistributionStatus.DRAFT }

    val todayDists = distributions.filter { it.isToday }
    val todayReceivedUnits = todayDists.sumOf { dist ->
        when (dist.status) {
            DistributionStatus.RECEIVED,
            DistributionStatus.PARTIALLY_RECEIVED -> dist.totalReceived ?: dist.totalSent
            DistributionStatus.SENT -> dist.totalSent
            else -> 0
        }
    }

    val todayRoutine = todayDists.firstOrNull { it.type == DistributionType.ROUTINE }
    val morningRoutineStatusLabel = when (todayRoutine?.status) {
        DistributionStatus.RECEIVED -> "Rutin pagi diterima"
        DistributionStatus.PARTIALLY_RECEIVED -> "Rutin diterima sebagian"
        DistributionStatus.SENT -> "Rutin menunggu konfirmasi"
        DistributionStatus.NOT_RECEIVED -> "Rutin tidak diterima"
        else -> "Rutin belum dikirim"
    }

    return OutletDashboardMetrics(
        todayDateLabel = headerDateFmt.format(nowDate),
        hasTodayReport = todayReport != null,
        todayReportTimeLabel = todayReport?.let { hourFmt.format(it.date.toDate()) },
        todayRevenue = todayRev,
        todayCashRevenue = todayReport?.cashRevenue ?: 0L,
        todayQrisRevenue = todayReport?.qrisRevenue ?: 0L,
        todaySoldPieces = todayReport?.totalSold ?: 0,
        dailyTarget = dailyTarget,
        targetProgress = targetProgress,
        targetPercent = targetPercent,
        lastReportDateLabel = previousReport?.let { shortDateFmt.format(it.date.toDate()) },
        lastReportRevenue = previousReport?.totalRevenue ?: (assignedOutlet?.totalRevenue ?: 0L),
        lastReportSold = previousReport?.totalSold ?: (assignedOutlet?.totalPiecesSold ?: 0),
        pendingConfirmCount = pendingConfirmCount,
        pendingRequestCount = pendingRequestCount,
        todayReceivedUnits = todayReceivedUnits,
        morningRoutineStatusLabel = morningRoutineStatusLabel
    )
}

data class OutletUiState(
    val staffName: String = "Staf Outlet",
    val assignedOutlet: Outlet? = null,
    val activeMaterials: List<Material> = emptyList(),
    val activeProducts: List<Product> = emptyList(),
    val distributions: List<OutletDistributionUi> = emptyList(),
    val recentReports: List<DailyReport> = emptyList(),
    val reportHistoryItems: List<OutletReportHistoryItemUi> = emptyList(),
    val reportDateCandidates: List<ReportDateCandidateUi> = emptyList(),
    val reportTabMode: OutletReportTabMode = OutletReportTabMode.SUMMARY,
    val selectedReportPeriod: String = "7 Hari",
    val reportPeriodSummary: OutletReportPeriodSummaryUi = OutletReportPeriodSummaryUi(),
    val todayReport: DailyReport? = null,
    val dashboardMetrics: OutletDashboardMetrics = OutletDashboardMetrics(),
    val selectedStockFilter: OutletStockFilter = OutletStockFilter.ALL,
    val showStockRequestSheet: Boolean = false,
    val requestForm: StockRequestFormState = StockRequestFormState(),
    val reportForm: DailyReportFormState = DailyReportFormState(),
    val isActionLoading: Boolean = false,
    val backendError: String? = null,
    // raw caches
    val rawOutlets: List<Outlet> = emptyList(),
    val rawUsers: List<User> = emptyList(),
    val rawMaterials: List<Material> = emptyList(),
    val rawProducts: List<Product> = emptyList(),
    val rawDistributions: List<MaterialDistribution> = emptyList(),
    val rawReports: List<DailyReport> = emptyList(),
    val rawReportProducts: List<DailyReportProduct> = emptyList(),
    val rawReportExpenses: List<DailyReportExpense> = emptyList(),
    val rawReportDamaged: List<DailyReportDamagedMaterial> = emptyList()
)

sealed interface OutletUiEvent {
    data object ToggleAssignedOutletOpen : OutletUiEvent
    data class ConfirmDistribution(
        val distributionId: String,
        val receivedByItemId: Map<String, Int>,
        val note: String
    ) : OutletUiEvent

    data class SelectStockFilter(val filter: OutletStockFilter) : OutletUiEvent
    data class SetStockRequestSheetVisible(val visible: Boolean) : OutletUiEvent
    data object ResetStockRequestForm : OutletUiEvent
    data class UpdateRequestQty(val materialId: String, val qtyInput: String) : OutletUiEvent
    data class UpdateRequestNote(val note: String) : OutletUiEvent
    data object SubmitStockRequest : OutletUiEvent
    data class CancelStockRequest(val distributionId: String) : OutletUiEvent

    data class SelectReportTab(val tab: OutletReportTabMode) : OutletUiEvent
    data class SelectReportSummaryPeriod(val period: String) : OutletUiEvent
    data class OpenCreateDailyReport(val preferredDate: Timestamp? = null) : OutletUiEvent
    data class SelectCreateReportDate(val date: Timestamp) : OutletUiEvent
    data object CloseDailyReportForm : OutletUiEvent
    data class UpdateProductQty(val productId: String, val qtyInput: String) : OutletUiEvent
    data class UpdateCashRevenue(val input: String) : OutletUiEvent
    data class UpdateQrisRevenue(val input: String) : OutletUiEvent
    data class AddDraftExpense(val category: ExpenseCategory, val amount: Long, val note: String) : OutletUiEvent
    data class RemoveDraftExpense(val id: String) : OutletUiEvent
    data class AddDraftDamaged(val materialId: String, val quantity: Int, val note: String) : OutletUiEvent
    data class RemoveDraftDamaged(val id: String) : OutletUiEvent
    data object SubmitDailyReport : OutletUiEvent
    data class ConfirmOverwriteDuplicateReport(val reportId: String) : OutletUiEvent
    data class LoadExistingReportForEdit(val reportId: String) : OutletUiEvent
    data object DismissDuplicateReportAlert : OutletUiEvent
    data object CancelEditDailyReport : OutletUiEvent
}

sealed interface OutletUiEffect {
    data class ShowSnackbar(val message: String) : OutletUiEffect
    data object StockRequestSaved : OutletUiEffect
    data object StockRequestCancelled : OutletUiEffect
    data object DistributionConfirmed : OutletUiEffect
    data object DailyReportSaved : OutletUiEffect
}

class OutletViewModel(
    private val outletRepository: OutletRepository,
    private val userRepository: UserRepository,
    private val materialRepository: MaterialRepository,
    private val productRepository: ProductRepository,
    private val distributionRepository: MaterialDistributionRepository,
    private val dailyReportRepository: DailyReportRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _state = MutableStateFlow(OutletUiState())
    val state: StateFlow<OutletUiState> = _state.asStateFlow()

    private val _effects = Channel<OutletUiEffect>(Channel.BUFFERED)
    val effects: Flow<OutletUiEffect> = _effects.receiveAsFlow().shareIn(viewModelScope, SharingStarted.Eagerly)

    private val reconcilingDraftIds = mutableSetOf<String>()

    init {
        observeStreams()
    }

    private fun reportError(source: String, error: Throwable) {
        Log.e("OutletViewModel", "Backend error ($source)", error)
        val detail = error.localizedMessage?.takeIf { it.isNotBlank() } ?: error.javaClass.simpleName
        val message = "Gagal $source: $detail"
        _state.update { it.copy(backendError = message, isActionLoading = false) }
        viewModelScope.launch { _effects.send(OutletUiEffect.ShowSnackbar(message)) }
    }

    private fun observeStreams() {
        viewModelScope.launch {
            outletRepository.getOutletsFlow(onlyActive = false)
                .catch { e -> reportError("memuat outlets", e) }
                .collect { list -> _state.update { recompute(it.copy(rawOutlets = list)) } }
        }
        viewModelScope.launch {
            userRepository.getAllUsersFlow()
                .catch { e -> reportError("memuat users", e) }
                .collect { list -> _state.update { recompute(it.copy(rawUsers = list)) } }
        }
        viewModelScope.launch {
            materialRepository.getMaterialsFlow()
                .catch { e -> reportError("memuat materials", e) }
                .collect { list -> _state.update { recompute(it.copy(rawMaterials = list)) } }
        }
        viewModelScope.launch {
            productRepository.getProductsFlow(onlyActive = false)
                .catch { e -> reportError("memuat products", e) }
                .collect { list -> _state.update { recompute(it.copy(rawProducts = list)) } }
        }
        viewModelScope.launch {
            distributionRepository.getDistributionsFlow(outletId = null)
                .catch { e -> reportError("memuat distribusi", e) }
                .collect { list ->
                    _state.update { recompute(it.copy(rawDistributions = list)) }
                    reconcileExpiredDrafts(list)
                }
        }
        viewModelScope.launch {
            dailyReportRepository.getDailyReportsFlow(outletId = null)
                .catch { e -> reportError("memuat laporan harian", e) }
                .collect { list -> _state.update { recompute(it.copy(rawReports = list)) } }
        }
        viewModelScope.launch {
            dailyReportRepository.getAllReportProductsFlow()
                .catch { e -> reportError("memuat rincian produk laporan", e) }
                .collect { list -> _state.update { recompute(it.copy(rawReportProducts = list)) } }
        }
        viewModelScope.launch {
            dailyReportRepository.getAllExpensesFlow()
                .catch { e -> reportError("memuat pengeluaran laporan", e) }
                .collect { list -> _state.update { recompute(it.copy(rawReportExpenses = list)) } }
        }
        viewModelScope.launch {
            dailyReportRepository.getAllDamagedMaterialsFlow()
                .catch { e -> reportError("memuat bahan rusak laporan", e) }
                .collect { list -> _state.update { recompute(it.copy(rawReportDamaged = list)) } }
        }
    }

    private fun reconcileExpiredDrafts(distributions: List<MaterialDistribution>) {
        val startOfTodaySec = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis / 1000L

        val actions = partitionExpiredDraftDistributions(distributions, startOfTodaySec)
        actions.routineToDelete.forEach { dist ->
            if (dist.id.isBlank() || !reconcilingDraftIds.add(dist.id)) return@forEach
            viewModelScope.launch {
                distributionRepository.deleteDistribution(dist.id)
                    .onFailure { e ->
                        reconcilingDraftIds.remove(dist.id)
                        Log.e("OutletViewModel", "Gagal menghapus draft rutin kadaluarsa (${dist.id})", e)
                    }
            }
        }
        actions.additionalToReject.forEach { dist ->
            if (dist.id.isBlank() || !reconcilingDraftIds.add(dist.id)) return@forEach
            viewModelScope.launch {
                distributionRepository.updateDistributionStatus(
                    id = dist.id,
                    status = DistributionStatus.CANCELLED,
                    confirmedBy = "system",
                    confirmationNote = "Ditolak otomatis karena melewati batas tanggal permintaan",
                    confirmedAt = Timestamp.now()
                ).onFailure { e ->
                    reconcilingDraftIds.remove(dist.id)
                    Log.e("OutletViewModel", "Gagal auto-reject permintaan kadaluarsa (${dist.id})", e)
                }
            }
        }
    }

    fun onEvent(event: OutletUiEvent) {
        when (event) {
            OutletUiEvent.ToggleAssignedOutletOpen -> toggleAssignedOutletOpen()
            is OutletUiEvent.ConfirmDistribution -> confirmDistribution(
                event.distributionId,
                event.receivedByItemId,
                event.note
            )
            is OutletUiEvent.SelectStockFilter -> _state.update {
                it.copy(selectedStockFilter = event.filter)
            }
            is OutletUiEvent.SetStockRequestSheetVisible -> _state.update {
                it.copy(
                    showStockRequestSheet = event.visible,
                    requestForm = if (event.visible) StockRequestFormState() else it.requestForm
                )
            }
            OutletUiEvent.ResetStockRequestForm -> _state.update {
                it.copy(requestForm = StockRequestFormState())
            }
            is OutletUiEvent.UpdateRequestQty -> _state.update { current ->
                val digits = event.qtyInput.filter(Char::isDigit)
                val updated = current.requestForm.itemQuantities.toMutableMap()
                if (digits.isBlank()) updated.remove(event.materialId) else updated[event.materialId] = digits
                current.copy(requestForm = current.requestForm.copy(itemQuantities = updated, errorMessage = null))
            }
            is OutletUiEvent.UpdateRequestNote -> _state.update {
                it.copy(requestForm = it.requestForm.copy(note = event.note, errorMessage = null))
            }
            OutletUiEvent.SubmitStockRequest -> submitStockRequest()
            is OutletUiEvent.CancelStockRequest -> cancelStockRequest(event.distributionId)

            is OutletUiEvent.SelectReportTab -> _state.update {
                it.copy(reportTabMode = event.tab)
            }
            is OutletUiEvent.SelectReportSummaryPeriod -> _state.update {
                recompute(it.copy(selectedReportPeriod = event.period))
            }
            is OutletUiEvent.OpenCreateDailyReport -> openCreateDailyReport(event.preferredDate)
            is OutletUiEvent.SelectCreateReportDate -> selectCreateReportDate(event.date)
            OutletUiEvent.CloseDailyReportForm -> _state.update {
                it.copy(reportForm = DailyReportFormState())
            }
            is OutletUiEvent.UpdateProductQty -> _state.update { current ->
                val digits = event.qtyInput.filter(Char::isDigit)
                val updated = current.reportForm.productQuantities.toMutableMap()
                if (digits.isBlank()) updated.remove(event.productId) else updated[event.productId] = digits
                current.copy(reportForm = current.reportForm.copy(productQuantities = updated, errorMessage = null))
            }
            is OutletUiEvent.UpdateCashRevenue -> _state.update {
                it.copy(reportForm = it.reportForm.copy(cashRevenueInput = event.input.filter(Char::isDigit), errorMessage = null))
            }
            is OutletUiEvent.UpdateQrisRevenue -> _state.update {
                it.copy(reportForm = it.reportForm.copy(qrisRevenueInput = event.input.filter(Char::isDigit), errorMessage = null))
            }
            is OutletUiEvent.AddDraftExpense -> _state.update { current ->
                val entry = DraftExpenseEntry(
                    id = "exp_${System.currentTimeMillis()}",
                    category = event.category,
                    amount = event.amount,
                    note = event.note.trim()
                )
                current.copy(reportForm = current.reportForm.copy(expenses = current.reportForm.expenses + entry))
            }
            is OutletUiEvent.RemoveDraftExpense -> _state.update { current ->
                current.copy(
                    reportForm = current.reportForm.copy(
                        expenses = current.reportForm.expenses.filterNot { it.id == event.id }
                    )
                )
            }
            is OutletUiEvent.AddDraftDamaged -> _state.update { current ->
                val matName = current.rawMaterials.firstOrNull { it.id == event.materialId }?.name ?: event.materialId
                val entry = DraftDamagedEntry(
                    id = "dmg_${System.currentTimeMillis()}",
                    materialId = event.materialId,
                    materialName = matName,
                    quantity = event.quantity,
                    note = event.note.trim()
                )
                current.copy(reportForm = current.reportForm.copy(damagedMaterials = current.reportForm.damagedMaterials + entry))
            }
            is OutletUiEvent.RemoveDraftDamaged -> _state.update { current ->
                current.copy(
                    reportForm = current.reportForm.copy(
                        damagedMaterials = current.reportForm.damagedMaterials.filterNot { it.id == event.id }
                    )
                )
            }
            OutletUiEvent.SubmitDailyReport -> submitDailyReport(overwriteReportId = null)
            is OutletUiEvent.ConfirmOverwriteDuplicateReport -> submitDailyReport(overwriteReportId = event.reportId)
            is OutletUiEvent.LoadExistingReportForEdit -> loadExistingReportForEdit(event.reportId)
            OutletUiEvent.DismissDuplicateReportAlert -> _state.update {
                it.copy(reportForm = it.reportForm.copy(pendingDuplicateReport = null))
            }
            OutletUiEvent.CancelEditDailyReport -> _state.update {
                it.copy(reportForm = DailyReportFormState())
            }
        }
    }

    private fun resolveCurrentUser(): User? {
        val email = authRepository.getCurrentUserEmail()
        return _state.value.rawUsers.firstOrNull {
            !email.isNullOrBlank() && it.email.equals(email, ignoreCase = true)
        }
    }

    private fun toggleAssignedOutletOpen() {
        val outlet = _state.value.assignedOutlet ?: return
        val nextOpen = !outlet.isOpen
        viewModelScope.launch {
            _state.update { it.copy(isActionLoading = true) }
            outletRepository.setOutletOpen(outlet.id, nextOpen)
                .onSuccess {
                    _state.update { it.copy(isActionLoading = false) }
                    val label = if (nextOpen) "Buka" else "Tutup"
                    _effects.send(OutletUiEffect.ShowSnackbar("Status Outlet ${outlet.name} diubah ke $label."))
                }
                .onFailure { e -> reportError("mengubah status outlet", e) }
        }
    }

    private fun confirmDistribution(
        distributionId: String,
        receivedByItemId: Map<String, Int>,
        note: String
    ) {
        val dist = _state.value.distributions.firstOrNull { it.id == distributionId } ?: return
        val updatedItems = dist.items.map { item ->
            val rec = (receivedByItemId[item.id] ?: item.quantitySent).coerceAtLeast(0)
            MaterialDistributionItem(
                id = item.id,
                distributionId = distributionId,
                materialId = item.materialId,
                quantitySent = item.quantitySent,
                quantityReceived = rec,
                note = item.note
            )
        }
        val status = resolveConfirmationStatus(
            updatedItems.map { it.quantitySent to (it.quantityReceived ?: 0) }
        )

        viewModelScope.launch {
            _state.update { it.copy(isActionLoading = true) }
            val confirmedBy = resolveCurrentUser()?.id ?: authRepository.getCurrentUserUid() ?: "staff"
            distributionRepository.confirmDistribution(
                distributionId = distributionId,
                confirmedBy = confirmedBy,
                confirmationNote = note.trim().ifBlank { "Stok diterima dan dikonfirmasi staf outlet" },
                updatedItems = updatedItems,
                status = status
            ).onSuccess {
                _state.update { it.copy(isActionLoading = false) }
                _effects.send(OutletUiEffect.ShowSnackbar("Penerimaan stok berhasil dikonfirmasi."))
                _effects.send(OutletUiEffect.DistributionConfirmed)
            }.onFailure { e ->
                reportError("mengonfirmasi penerimaan stok", e)
            }
        }
    }

    private fun cancelStockRequest(distributionId: String) {
        val dist = _state.value.distributions.firstOrNull { it.id == distributionId } ?: return
        if (dist.status != DistributionStatus.DRAFT) return

        viewModelScope.launch {
            _state.update { it.copy(isActionLoading = true) }
            val cancelledBy = resolveCurrentUser()?.id ?: authRepository.getCurrentUserUid() ?: "staff"
            distributionRepository.updateDistributionStatus(
                id = distributionId,
                status = DistributionStatus.CANCELLED,
                confirmedBy = cancelledBy,
                confirmationNote = "Dibatalkan oleh staf outlet",
                confirmedAt = Timestamp.now()
            ).onSuccess {
                _state.update { it.copy(isActionLoading = false) }
                _effects.send(OutletUiEffect.ShowSnackbar("Permintaan tambahan stok dibatalkan."))
                _effects.send(OutletUiEffect.StockRequestCancelled)
            }.onFailure { e ->
                reportError("membatalkan permintaan stok", e)
            }
        }
    }

    private fun submitStockRequest() {
        val outlet = _state.value.assignedOutlet
        val form = _state.value.requestForm
        val items = form.itemQuantities.mapNotNull { (matId, qtyStr) ->
            val qty = qtyStr.toIntOrNull() ?: 0
            if (qty > 0) MaterialDistributionItem(materialId = matId, quantitySent = qty) else null
        }

        val error = when {
            outlet == null -> "Outlet penugasan belum ditemukan."
            items.isEmpty() -> "Masukkan jumlah minimal 1 bahan mentah (> 0)."
            else -> null
        }
        if (error != null) {
            _state.update { it.copy(requestForm = form.copy(errorMessage = error)) }
            return
        }

        viewModelScope.launch {
            _state.update { it.copy(requestForm = form.copy(isSaving = true, errorMessage = null)) }
            val createdBy = resolveCurrentUser()?.id ?: authRepository.getCurrentUserUid() ?: "staff"
            val distribution = MaterialDistribution(
                outletId = outlet!!.id,
                createdBy = createdBy,
                status = DistributionStatus.DRAFT,
                type = DistributionType.ADDITIONAL,
                note = form.note.trim().ifBlank { "Permintaan tambahan stok dari Outlet ${outlet.name}" },
                createdAt = Timestamp.now()
            )
            distributionRepository.createDistribution(distribution, items)
                .onSuccess {
                    _state.update {
                        it.copy(
                            showStockRequestSheet = false,
                            requestForm = StockRequestFormState()
                        )
                    }
                    _effects.send(OutletUiEffect.ShowSnackbar("Permintaan tambahan stok berhasil dikirim ke Gudang."))
                    _effects.send(OutletUiEffect.StockRequestSaved)
                }
                .onFailure { e ->
                    val detail = e.localizedMessage?.takeIf { it.isNotBlank() } ?: e.javaClass.simpleName
                    val msg = "Gagal mengirim permintaan stok: $detail"
                    _state.update { it.copy(requestForm = form.copy(isSaving = false, errorMessage = msg)) }
                    _effects.send(OutletUiEffect.ShowSnackbar(msg))
                }
        }
    }

    private fun openCreateDailyReport(preferredDate: Timestamp?) {
        val current = _state.value
        val candidates = current.reportDateCandidates.ifEmpty {
            buildReportDateCandidates(current.recentReports, Timestamp.now())
        }
        val availableDate = preferredDate?.takeIf { pref ->
            candidates.any { !it.hasExistingReport && isSameCalendarDay(it.timestamp, pref) }
        } ?: candidates.firstOrNull { !it.hasExistingReport }?.timestamp

        if (availableDate == null) {
            viewModelScope.launch {
                _effects.send(
                    OutletUiEffect.ShowSnackbar("Seluruh tanggal dalam 7 hari terakhir sudah memiliki laporan harian.")
                )
            }
            return
        }

        _state.update {
            it.copy(
                reportForm = DailyReportFormState(
                    isFormVisible = true,
                    selectedCreateDate = availableDate
                )
            )
        }
    }

    private fun selectCreateReportDate(targetDate: Timestamp) {
        val current = _state.value
        val now = Timestamp.now()
        val daysAgo = calendarDaysBetween(targetDate, now)
        val error = when {
            daysAgo < 0 -> "Tidak dapat memilih tanggal di masa depan."
            daysAgo > MAX_REPORT_CREATE_DAYS_BACK -> "Laporan susulan dibatasi maksimal $MAX_REPORT_CREATE_DAYS_BACK hari ke belakang."
            current.recentReports.any { isSameCalendarDay(it.date, targetDate) } ->
                "Tanggal tersebut sudah memiliki laporan harian. Pilih tanggal lain yang masih kosong."
            else -> null
        }
        if (error != null) {
            _state.update { it.copy(reportForm = it.reportForm.copy(errorMessage = error)) }
            viewModelScope.launch { _effects.send(OutletUiEffect.ShowSnackbar(error)) }
            return
        }
        _state.update {
            it.copy(
                reportForm = it.reportForm.copy(
                    selectedCreateDate = targetDate,
                    errorMessage = null
                )
            )
        }
    }

    private fun loadExistingReportForEdit(reportId: String) {
        val existingInMemory = _state.value.recentReports.firstOrNull { it.id == reportId }
        if (existingInMemory != null && !isReportEditable(existingInMemory.date, Timestamp.now())) {
            val msg = "Laporan lebih dari $MAX_REPORT_EDIT_DAYS_BACK hari terakhir terkunci dan tidak dapat diedit."
            _state.update { it.copy(reportForm = it.reportForm.copy(errorMessage = msg)) }
            viewModelScope.launch { _effects.send(OutletUiEffect.ShowSnackbar(msg)) }
            return
        }

        viewModelScope.launch {
            _state.update {
                it.copy(
                    reportForm = it.reportForm.copy(
                        isLoadingExisting = true,
                        pendingDuplicateReport = null,
                        errorMessage = null
                    )
                )
            }
            dailyReportRepository.getFullDailyReport(reportId)
                .onSuccess { full ->
                    if (full == null) {
                        _state.update {
                            it.copy(
                                reportForm = it.reportForm.copy(
                                    isLoadingExisting = false,
                                    errorMessage = "Data laporan tidak ditemukan."
                                )
                            )
                        }
                        return@onSuccess
                    }
                    if (!isReportEditable(full.report.date, Timestamp.now())) {
                        val msg = "Laporan lebih dari $MAX_REPORT_EDIT_DAYS_BACK hari terakhir terkunci dan tidak dapat diedit."
                        _state.update {
                            it.copy(
                                reportForm = it.reportForm.copy(
                                    isLoadingExisting = false,
                                    errorMessage = msg
                                )
                            )
                        }
                        _effects.send(OutletUiEffect.ShowSnackbar(msg))
                        return@onSuccess
                    }
                    val matMap = _state.value.rawMaterials.associateBy { it.id }
                    _state.update {
                        it.copy(
                            reportForm = DailyReportFormState(
                                isFormVisible = true,
                                editingReportId = full.report.id,
                                editingReportDate = full.report.date,
                                productQuantities = full.products
                                    .filter { p -> p.quantity > 0 }
                                    .associate { p -> p.productId to p.quantity.toString() },
                                cashRevenueInput = full.report.cashRevenue.toString(),
                                qrisRevenueInput = full.report.qrisRevenue.toString(),
                                expenses = full.expenses.mapIndexed { idx, exp ->
                                    DraftExpenseEntry(
                                        id = exp.id.ifBlank { "exp_edit_$idx" },
                                        category = exp.category,
                                        amount = exp.amount,
                                        note = exp.note.orEmpty()
                                    )
                                },
                                damagedMaterials = full.damagedMaterials.mapIndexed { idx, dmg ->
                                    DraftDamagedEntry(
                                        id = dmg.id.ifBlank { "dmg_edit_$idx" },
                                        materialId = dmg.materialId,
                                        materialName = matMap[dmg.materialId]?.name ?: dmg.materialId,
                                        quantity = dmg.quantity,
                                        note = dmg.note.orEmpty()
                                    )
                                }
                            )
                        )
                    }
                    _effects.send(OutletUiEffect.ShowSnackbar("Data laporan dimuat. Silakan ubah lalu simpan pembaruan."))
                }
                .onFailure { e ->
                    val detail = e.localizedMessage?.takeIf { it.isNotBlank() } ?: e.javaClass.simpleName
                    _state.update {
                        it.copy(
                            reportForm = it.reportForm.copy(
                                isLoadingExisting = false,
                                errorMessage = "Gagal memuat laporan: $detail"
                            )
                        )
                    }
                    _effects.send(OutletUiEffect.ShowSnackbar("Gagal memuat laporan: $detail"))
                }
        }
    }

    private fun submitDailyReport(overwriteReportId: String?) {
        val outlet = _state.value.assignedOutlet
        val form = _state.value.reportForm
        val productMap = _state.value.rawProducts.associateBy { it.id }

        val soldProducts = form.productQuantities.mapNotNull { (prodId, qtyStr) ->
            val qty = qtyStr.toIntOrNull() ?: 0
            val prod = productMap[prodId]
            if (qty > 0 && prod != null) {
                DailyReportProduct(productId = prodId, quantity = qty, price = prod.price)
            } else null
        }
        val totalSold = soldProducts.sumOf { it.quantity }
        val autoRevenue = soldProducts.sumOf { it.subtotal }
        val cashRev = form.cashRevenueInput.toLongOrNull() ?: autoRevenue
        val qrisRev = form.qrisRevenueInput.toLongOrNull() ?: 0L
        val totalRev = cashRev + qrisRev

        val nowTs = Timestamp.now()
        val resolvedUpdateId = overwriteReportId ?: form.editingReportId
        val existingForDate = resolvedUpdateId?.let { id ->
            _state.value.recentReports.firstOrNull { it.id == id }
                ?: form.pendingDuplicateReport?.takeIf { it.id == id }
        }
        val targetDate = existingForDate?.date ?: form.editingReportDate ?: form.selectedCreateDate ?: nowTs
        val daysAgo = calendarDaysBetween(targetDate, nowTs)

        val error = when {
            outlet == null -> "Outlet penugasan belum ditemukan."
            totalRev <= 0L && totalSold <= 0 -> "Isi minimal jumlah produk terjual atau pendapatan tunai/QRIS."
            daysAgo < 0 -> "Tanggal laporan tidak boleh di masa depan."
            !resolvedUpdateId.isNullOrBlank() && daysAgo > MAX_REPORT_EDIT_DAYS_BACK ->
                "Laporan lebih dari $MAX_REPORT_EDIT_DAYS_BACK hari terakhir tidak dapat diubah lagi."
            resolvedUpdateId.isNullOrBlank() && daysAgo > MAX_REPORT_CREATE_DAYS_BACK ->
                "Laporan susulan hanya dapat dibuat maksimal $MAX_REPORT_CREATE_DAYS_BACK hari ke belakang."
            else -> null
        }
        if (error != null) {
            _state.update { it.copy(reportForm = form.copy(errorMessage = error, pendingDuplicateReport = null)) }
            viewModelScope.launch { _effects.send(OutletUiEffect.ShowSnackbar(error)) }
            return
        }

        viewModelScope.launch {
            _state.update {
                it.copy(reportForm = form.copy(isSaving = true, errorMessage = null, pendingDuplicateReport = null))
            }

            // Reject duplicate creation automatically unless the user explicitly confirmed update/edit
            if (resolvedUpdateId.isNullOrBlank()) {
                val (dayStart, dayEnd) = calendarDayBounds(targetDate)
                val existingInMemory = _state.value.recentReports.firstOrNull {
                    it.outletId == outlet!!.id && isSameCalendarDay(it.date, targetDate)
                }
                val existingReport = existingInMemory
                    ?: dailyReportRepository.getDailyReportByOutletAndDate(outlet!!.id, dayStart, dayEnd).getOrNull()

                if (existingReport != null) {
                    _state.update {
                        it.copy(
                            reportForm = it.reportForm.copy(
                                isSaving = false,
                                pendingDuplicateReport = existingReport
                            )
                        )
                    }
                    return@launch
                }
            }

            val currentSenderId = resolveCurrentUser()?.id ?: authRepository.getCurrentUserUid() ?: "staff"
            val report = DailyReport(
                id = resolvedUpdateId.orEmpty(),
                outletId = outlet!!.id,
                date = targetDate,
                cashRevenue = cashRev,
                qrisRevenue = qrisRev,
                totalSold = totalSold,
                createdBy = existingForDate?.createdBy?.takeIf { it.isNotBlank() } ?: currentSenderId
            )
            val expenses = form.expenses.map {
                DailyReportExpense(category = it.category, amount = it.amount, note = it.note.ifBlank { null })
            }
            val damaged = form.damagedMaterials.map {
                DailyReportDamagedMaterial(materialId = it.materialId, quantity = it.quantity, note = it.note.ifBlank { null })
            }

            dailyReportRepository.createDailyReport(report, soldProducts, expenses, damaged)
                .onSuccess {
                    if (isSameCalendarDay(report.date, Timestamp.now())) {
                        val dailyTarget = outlet.dailyTarget.coerceAtLeast(1L)
                        val targetPct = (totalRev.toFloat() / dailyTarget.toFloat()).coerceIn(0f, 1f)
                        outletRepository.updateOutletDailySummary(outlet.id, totalRev, totalSold, targetPct)
                    }
                    val successMsg = if (resolvedUpdateId.isNullOrBlank()) {
                        "Laporan harian berhasil disimpan ke Firestore."
                    } else {
                        "Laporan harian berhasil diperbarui."
                    }
                    _state.update { it.copy(reportForm = DailyReportFormState()) }
                    _effects.send(OutletUiEffect.ShowSnackbar(successMsg))
                    _effects.send(OutletUiEffect.DailyReportSaved)
                }
                .onFailure { e ->
                    val detail = e.localizedMessage?.takeIf { it.isNotBlank() } ?: e.javaClass.simpleName
                    val msg = "Gagal menyimpan laporan harian: $detail"
                    _state.update { it.copy(reportForm = form.copy(isSaving = false, errorMessage = msg)) }
                    _effects.send(OutletUiEffect.ShowSnackbar(msg))
                }
        }
    }

    private fun recompute(raw: OutletUiState): OutletUiState {
        val email = authRepository.getCurrentUserEmail()
        val matchedUser = raw.rawUsers.firstOrNull {
            !email.isNullOrBlank() && it.email.equals(email, ignoreCase = true)
        }
        val assignedOutlet = matchedUser?.outletId?.let { oid ->
            raw.rawOutlets.firstOrNull { it.id == oid }
        } ?: raw.rawOutlets.firstOrNull { it.isActive } ?: raw.rawOutlets.firstOrNull()

        val matMap = raw.rawMaterials.associateBy { it.id }
        val userMap = raw.rawUsers.associateBy { it.id }
        val idLocale = Locale.forLanguageTag("id-ID")
        val timeFmt = SimpleDateFormat("dd MMM, HH.mm", idLocale)
        val hourFmt = SimpleDateFormat("HH.mm", idLocale)
        val nowTs = Timestamp.now()

        val outletDists = raw.rawDistributions
            .filter { assignedOutlet == null || it.outletId == assignedOutlet.id }
            .sortedByDescending { it.createdAt.seconds }
            .map { dist ->
                val items = dist.items.map { item ->
                    val mat = matMap[item.materialId]
                    OutletDistributionItemUi(
                        id = item.id,
                        materialId = item.materialId,
                        materialName = mat?.name ?: item.materialId,
                        unitLabel = mat?.unit?.firestoreValue ?: "pcs",
                        quantitySent = item.quantitySent,
                        quantityReceived = item.quantityReceived,
                        note = item.note
                    )
                }
                val isToday = isSameCalendarDay(dist.createdAt, nowTs)
                val createdDate = Date(dist.createdAt.toDate().time)
                val createdByName = userMap[dist.createdBy]?.name?.takeIf { it.isNotBlank() }
                    ?: dist.createdBy.ifBlank { "Gudang" }
                val confirmedByName = dist.confirmedBy?.let { cid ->
                    if (cid.equals("system", ignoreCase = true)) {
                        "Sistem"
                    } else {
                        userMap[cid]?.name?.takeIf { it.isNotBlank() } ?: cid
                    }
                }
                val totalReceived = if (items.any { it.quantityReceived != null }) {
                    items.sumOf { it.quantityReceived ?: 0 }
                } else {
                    null
                }
                OutletDistributionUi(
                    id = dist.id,
                    status = dist.status,
                    type = dist.type,
                    timeLabel = if (isToday) "Hari ini, ${hourFmt.format(createdDate)}" else timeFmt.format(createdDate),
                    isToday = isToday,
                    createdAtMillis = dist.createdAt.toDate().time,
                    createdByName = createdByName,
                    confirmedByName = confirmedByName,
                    sentTimeLabel = dist.sentAt?.let { timeFmt.format(it.toDate()) },
                    confirmedTimeLabel = dist.confirmedAt?.let { timeFmt.format(it.toDate()) },
                    note = dist.note,
                    confirmationNote = dist.confirmationNote,
                    items = items,
                    totalSent = items.sumOf { it.quantitySent },
                    totalReceived = totalReceived
                )
            }

        val outletReports = raw.rawReports
            .filter { assignedOutlet == null || it.outletId == assignedOutlet.id }
            .sortedByDescending { it.date.seconds }
        val todayReport = outletReports.firstOrNull { isSameCalendarDay(it.date, nowTs) }

        val prodMap = raw.rawProducts.associateBy { it.id }
        val productsByReportId = raw.rawReportProducts.groupBy { it.dailyReportId }
        val expensesByReportId = raw.rawReportExpenses.groupBy { it.dailyReportId.orEmpty() }
        val damagedByReportId = raw.rawReportDamaged.groupBy { it.dailyReportId }
        val fallbackOutletStaffName = raw.rawUsers
            .firstOrNull { it.role == Role.OUTLET && assignedOutlet != null && it.outletId == assignedOutlet.id }
            ?.name
            ?.replace(Regex("\\s*\\(.*\\)"), "")
            ?.takeIf { it.isNotBlank() }
            ?: matchedUser?.name?.replace(Regex("\\s*\\(.*\\)"), "")?.takeIf { it.isNotBlank() }
            ?: "Dimas Pratama"

        val reportDateFmt = SimpleDateFormat("EEE, dd MMM yyyy", idLocale)
        val fullDateFmt = SimpleDateFormat("EEEE, dd MMMM yyyy", idLocale)

        val historyItems = outletReports.map { rep ->
            val dateObj = rep.date.toDate()
            val daysAgo = calendarDaysBetween(rep.date, nowTs)
            val senderName = if (rep.createdBy.isNotBlank()) {
                userMap[rep.createdBy]?.name
                    ?.replace(Regex("\\s*\\(.*\\)"), "")
                    ?.takeIf { it.isNotBlank() }
                    ?: rep.createdBy
            } else {
                fallbackOutletStaffName
            }
            val repProducts = productsByReportId[rep.id].orEmpty().map { rp ->
                ReportProductLineUi(
                    id = rp.id,
                    productName = prodMap[rp.productId]?.name ?: rp.productId,
                    quantity = rp.quantity,
                    price = rp.price,
                    subtotal = rp.subtotal
                )
            }
            val repExpenses = expensesByReportId[rep.id].orEmpty().map { exp ->
                val catLabel = when (exp.category) {
                    ExpenseCategory.GAS -> "Gas & bahan bakar"
                    ExpenseCategory.PLASTIC_BAG -> "Plastik & tusuk"
                    ExpenseCategory.OTHER -> "Lainnya"
                }
                ReportExpenseLineUi(
                    id = exp.id,
                    categoryLabel = catLabel,
                    amount = exp.amount,
                    note = exp.note
                )
            }
            val repDamaged = damagedByReportId[rep.id].orEmpty().map { dmg ->
                val mat = matMap[dmg.materialId]
                val unit = mat?.unit?.firestoreValue ?: "pcs"
                ReportDamagedLineUi(
                    id = dmg.id,
                    materialName = mat?.name ?: dmg.materialId,
                    quantityText = "${dmg.quantity} $unit",
                    note = dmg.note
                )
            }
            val totalExp = repExpenses.sumOf { it.amount }
            val dmgCount = damagedByReportId[rep.id].orEmpty().sumOf { it.quantity }
            val dateTitle = when (daysAgo) {
                0 -> "Hari ini, ${SimpleDateFormat("dd MMM yyyy", idLocale).format(dateObj)}"
                1 -> "Kemarin, ${SimpleDateFormat("dd MMM yyyy", idLocale).format(dateObj)}"
                else -> reportDateFmt.format(dateObj)
            }

            OutletReportHistoryItemUi(
                id = rep.id,
                date = rep.date,
                dateMillis = dateObj.time,
                dateLabel = dateTitle,
                fullDateLabel = fullDateFmt.format(dateObj),
                timeLabel = "${hourFmt.format(dateObj)} WIB",
                daysAgo = daysAgo,
                createdById = rep.createdBy,
                createdByName = senderName,
                createdByInitials = buildInitials(senderName),
                cashRevenue = rep.cashRevenue,
                qrisRevenue = rep.qrisRevenue,
                totalRevenue = rep.totalRevenue,
                totalSold = rep.totalSold,
                totalExpense = totalExp,
                damagedCount = dmgCount,
                netDeposit = (rep.totalRevenue - totalExp).coerceAtLeast(0L),
                isEditable = isReportEditable(rep.date, nowTs),
                productLines = repProducts,
                expenseLines = repExpenses,
                damagedLines = repDamaged
            )
        }

        val dateCandidates = buildReportDateCandidates(outletReports, nowTs)
        val periodSummary = computeOutletReportPeriodSummary(
            period = raw.selectedReportPeriod,
            reports = outletReports,
            expensesByReportId = expensesByReportId,
            damagedByReportId = damagedByReportId,
            materialMap = matMap,
            reference = nowTs
        )
        val dashboardMetrics = computeOutletDashboardMetrics(
            assignedOutlet = assignedOutlet,
            todayReport = todayReport,
            recentReports = outletReports,
            distributions = outletDists,
            nowDate = nowTs.toDate()
        )

        return raw.copy(
            staffName = matchedUser?.name?.takeIf { it.isNotBlank() } ?: "Dimas Pratama",
            assignedOutlet = assignedOutlet,
            activeMaterials = raw.rawMaterials.filter { it.isActive },
            activeProducts = raw.rawProducts.filter { it.isActive },
            distributions = outletDists,
            recentReports = outletReports,
            reportHistoryItems = historyItems,
            reportDateCandidates = dateCandidates,
            reportPeriodSummary = periodSummary,
            todayReport = todayReport,
            dashboardMetrics = dashboardMetrics
        )
    }
}
