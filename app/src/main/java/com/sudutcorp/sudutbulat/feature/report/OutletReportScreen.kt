package com.sudutcorp.sudutbulat.feature.report

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.navigation.NavHostController
import com.sudutcorp.sudutbulat.R
import com.sudutcorp.sudutbulat.core.components.SBBottomCtaBar
import com.sudutcorp.sudutbulat.core.components.SBDottedDivider
import com.sudutcorp.sudutbulat.core.components.SBFilterChipRow
import com.sudutcorp.sudutbulat.core.components.SBInlineAlert
import com.sudutcorp.sudutbulat.core.components.SBKeyValueRow
import com.sudutcorp.sudutbulat.core.components.SBPageHeader
import com.sudutcorp.sudutbulat.core.components.SBSearchBar
import com.sudutcorp.sudutbulat.core.components.SBSegmentedTabs
import com.sudutcorp.sudutbulat.core.components.SBSectionCard
import com.sudutcorp.sudutbulat.core.components.SBStatusBadge
import com.sudutcorp.sudutbulat.core.components.SBSubHeader
import com.sudutcorp.sudutbulat.core.ui.theme.SBColor
import com.sudutcorp.sudutbulat.core.util.buildInitials
import com.sudutcorp.sudutbulat.core.util.formatDecimal
import com.sudutcorp.sudutbulat.core.util.formatRupiah
import com.sudutcorp.sudutbulat.core.util.formatShortRupiah
import com.sudutcorp.sudutbulat.data.model.ExpenseCategory
import com.sudutcorp.sudutbulat.data.model.Material
import com.sudutcorp.sudutbulat.data.model.Product
import com.sudutcorp.sudutbulat.data.model.ProductSaleType
import com.sudutcorp.sudutbulat.feature.outlet.DailyReportFormState
import com.sudutcorp.sudutbulat.feature.outlet.OutletReportHistoryFilter
import com.sudutcorp.sudutbulat.feature.outlet.OutletReportHistoryItemUi
import com.sudutcorp.sudutbulat.feature.outlet.OutletReportPeriodSummaryUi
import com.sudutcorp.sudutbulat.feature.outlet.OutletReportTabMode
import com.sudutcorp.sudutbulat.feature.outlet.OutletUiEffect
import com.sudutcorp.sudutbulat.feature.outlet.OutletUiEvent
import com.sudutcorp.sudutbulat.feature.outlet.OutletUiState
import com.sudutcorp.sudutbulat.feature.outlet.ReportDateCandidateUi
import com.sudutcorp.sudutbulat.feature.outlet.isSameCalendarDay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val QUICK_EXPENSE_AMOUNTS = listOf(15_000L, 20_000L, 22_000L, 50_000L)
private val QUICK_DAMAGED_REASONS = listOf(
    "Pecah saat digoreng",
    "Kempes kena hujan",
    "Asam / tidak layak jual"
)

private fun ExpenseCategory.displayLabel(): String = when (this) {
    ExpenseCategory.GAS -> "Gas & bahan bakar"
    ExpenseCategory.PLASTIC_BAG -> "Plastik & tusuk"
    ExpenseCategory.OTHER -> "Lainnya"
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OutletReportScreen(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    uiState: OutletUiState = OutletUiState(),
    effects: Flow<OutletUiEffect> = emptyFlow(),
    onEvent: (OutletUiEvent) -> Unit = {},
    isDevMode: Boolean = false,
    onSwitchRoleClick: () -> Unit = {}
) {
    val focusManager = LocalFocusManager.current
    val listState = rememberLazyListState()

    var historySearchQuery by rememberSaveable { mutableStateOf("") }
    var historyFilterName by rememberSaveable { mutableStateOf(OutletReportHistoryFilter.ALL.name) }
    var detailReportId by rememberSaveable { mutableStateOf<String?>(null) }

    val selectedHistoryFilter = remember(historyFilterName) {
        OutletReportHistoryFilter.entries.firstOrNull { it.name == historyFilterName }
            ?: OutletReportHistoryFilter.ALL
    }

    LaunchedEffect(effects) {
        effects.collect { effect ->
            if (effect is OutletUiEffect.DailyReportSaved) {
                detailReportId = null
            }
        }
    }

    val eyebrow = uiState.assignedOutlet?.let { outlet ->
        val base = if (outlet.name.startsWith("Outlet", ignoreCase = true)) outlet.name else "Outlet ${outlet.name}"
        if (isDevMode) "$base • Mode DEV" else base
    } ?: if (isDevMode) "Staf Outlet • Mode DEV" else "Staf Outlet"
    val initials = if (isDevMode) "DV" else buildInitials(uiState.staffName)

    val missingCandidates = remember(uiState.reportDateCandidates) {
        uiState.reportDateCandidates.filter { !it.hasExistingReport }
    }
    val editableCount = remember(uiState.reportHistoryItems) {
        uiState.reportHistoryItems.count { it.isEditable }
    }

    val historyChipOptions = remember(uiState.reportHistoryItems.size, editableCount) {
        listOf(
            OutletReportHistoryFilter.ALL.name to "Semua (${uiState.reportHistoryItems.size})",
            OutletReportHistoryFilter.EDITABLE.name to "Bisa Diedit ($editableCount)",
            OutletReportHistoryFilter.DAYS_7.name to "7 Hari",
            OutletReportHistoryFilter.DAYS_30.name to "30 Hari"
        )
    }

    val filteredHistoryItems = remember(
        uiState.reportHistoryItems,
        selectedHistoryFilter,
        historySearchQuery
    ) {
        val q = historySearchQuery.trim().lowercase(Locale.ROOT)
        uiState.reportHistoryItems.filter { item ->
            val matchesFilter = when (selectedHistoryFilter) {
                OutletReportHistoryFilter.ALL -> true
                OutletReportHistoryFilter.EDITABLE -> item.isEditable
                OutletReportHistoryFilter.DAYS_7 -> item.daysAgo in 0..6
                OutletReportHistoryFilter.DAYS_30 -> item.daysAgo in 0..29
            }
            if (!matchesFilter) return@filter false
            if (q.isBlank()) return@filter true

            item.dateLabel.lowercase(Locale.ROOT).contains(q) ||
                item.fullDateLabel.lowercase(Locale.ROOT).contains(q) ||
                item.createdByName.lowercase(Locale.ROOT).contains(q) ||
                item.expenseLines.any { it.note?.lowercase(Locale.ROOT)?.contains(q) == true } ||
                item.damagedLines.any {
                    it.materialName.lowercase(Locale.ROOT).contains(q) ||
                        (it.note?.lowercase(Locale.ROOT)?.contains(q) == true)
                }
        }
    }

    LazyColumn(
        state = listState,
        modifier = modifier
            .fillMaxSize()
            .background(Color.White)
            .pointerInput(Unit) { detectTapGestures(onTap = { focusManager.clearFocus() }) },
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item(key = "report_header", contentType = "header") {
            SBPageHeader(
                eyebrow = eyebrow,
                title = "Laporan Harian",
                initials = initials,
                isAvatarClickable = isDevMode,
                onAvatarClick = onSwitchRoleClick
            )
        }

        item(key = "report_status_cta_card", contentType = "status_cta") {
            ReportTopActionCard(
                todayReport = uiState.todayReport,
                missingCandidates = missingCandidates,
                isLoadingEdit = uiState.reportForm.isLoadingExisting,
                onCreateTodayOrMissingClick = { preferredTs ->
                    onEvent(OutletUiEvent.OpenCreateDailyReport(preferredTs))
                },
                onEditTodayClick = { reportId ->
                    onEvent(OutletUiEvent.LoadExistingReportForEdit(reportId))
                }
            )
        }

        item(key = "report_main_tabs", contentType = "main_tabs") {
            SBSegmentedTabs(
                options = OutletReportTabMode.entries,
                selected = uiState.reportTabMode,
                labelOf = { tab ->
                    if (tab == OutletReportTabMode.HISTORY && uiState.reportHistoryItems.isNotEmpty()) {
                        "${tab.label} (${uiState.reportHistoryItems.size})"
                    } else {
                        tab.label
                    }
                },
                onSelected = { onEvent(OutletUiEvent.SelectReportTab(it)) }
            )
        }

        if (uiState.reportTabMode == OutletReportTabMode.SUMMARY) {
            item(key = "report_summary_content", contentType = "summary_content") {
                OutletReportSummaryTabContent(
                    selectedPeriod = uiState.selectedReportPeriod,
                    summary = uiState.reportPeriodSummary,
                    onSelectPeriod = { onEvent(OutletUiEvent.SelectReportSummaryPeriod(it)) },
                    onSeeHistoryClick = {
                        onEvent(OutletUiEvent.SelectReportTab(OutletReportTabMode.HISTORY))
                    }
                )
            }
        } else {
            item(key = "report_history_search", contentType = "history_search") {
                SBSearchBar(
                    query = historySearchQuery,
                    onQueryChange = { historySearchQuery = it },
                    placeholder = "Cari tanggal, staf pengirim, atau catatan..."
                )
            }

            item(key = "report_history_filters", contentType = "history_filters") {
                SBFilterChipRow(
                    options = historyChipOptions,
                    selectedKey = selectedHistoryFilter.name,
                    onSelected = { historyFilterName = it }
                )
            }

            item(key = "report_history_toolbar", contentType = "history_toolbar") {
                val hasActiveFilter = selectedHistoryFilter != OutletReportHistoryFilter.ALL ||
                    historySearchQuery.isNotBlank()
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${filteredHistoryItems.size} laporan ditemukan",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = SBColor.Grey_700
                    )
                    if (hasActiveFilter) {
                        Text(
                            text = "Reset",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = SBColor.Primary_550,
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .clickable {
                                    historyFilterName = OutletReportHistoryFilter.ALL.name
                                    historySearchQuery = ""
                                }
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    } else {
                        Text(
                            text = "Edit maks. 3 hari terakhir",
                            fontSize = 12.sp,
                            color = SBColor.Grey_500
                        )
                    }
                }
            }

            if (filteredHistoryItems.isEmpty()) {
                item(key = "report_history_empty", contentType = "history_empty") {
                    OutlinedCard(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.outlinedCardColors(containerColor = Color.White),
                        border = BorderStroke(1.dp, SBColor.Grey_100)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 24.dp, vertical = 28.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Surface(
                                modifier = Modifier.size(48.dp),
                                shape = RoundedCornerShape(14.dp),
                                color = SBColor.Grey_100
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        painter = painterResource(R.drawable.ic_bar_chart),
                                        contentDescription = null,
                                        tint = SBColor.Grey_500,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }
                            Text(
                                text = "Belum ada laporan untuk filter ini",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = SBColor.Grey_700
                            )
                            Text(
                                text = "Coba ubah kata kunci pencarian atau buat laporan harian baru.",
                                fontSize = 13.sp,
                                color = SBColor.Grey_500,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            } else {
                items(
                    items = filteredHistoryItems,
                    key = { it.id },
                    contentType = { "outlet_report_history_card" }
                ) { item ->
                    OutletReportHistoryCard(
                        item = item,
                        isLoadingEdit = uiState.reportForm.isLoadingExisting,
                        onClick = { detailReportId = item.id },
                        onEditClick = { onEvent(OutletUiEvent.LoadExistingReportForEdit(item.id)) }
                    )
                }
            }
        }
    }

    val detailItem = detailReportId?.let { id ->
        uiState.reportHistoryItems.firstOrNull { it.id == id }
    }
    if (detailItem != null) {
        OutletReportDetailBottomSheet(
            item = detailItem,
            outletName = eyebrow.substringBefore(" •"),
            isLoadingEdit = uiState.reportForm.isLoadingExisting,
            onDismiss = { detailReportId = null },
            onEditClick = {
                detailReportId = null
                onEvent(OutletUiEvent.LoadExistingReportForEdit(detailItem.id))
            }
        )
    }

    val isFormOpen = uiState.reportForm.isFormVisible || !uiState.reportForm.editingReportId.isNullOrBlank()
    if (isFormOpen) {
        OutletReportFormDialog(
            outletName = eyebrow.substringBefore(" •"),
            form = uiState.reportForm,
            dateCandidates = uiState.reportDateCandidates,
            activeProducts = uiState.activeProducts,
            activeMaterials = uiState.activeMaterials,
            onEvent = onEvent
        )
    }

    val duplicateReport = uiState.reportForm.pendingDuplicateReport
    if (duplicateReport != null) {
        val dayOnlyFmt = remember { SimpleDateFormat("dd MMM yyyy", Locale.forLanguageTag("id-ID")) }
        val dupDateLabel = dayOnlyFmt.format(Date(duplicateReport.date.toDate().time))
        AlertDialog(
            onDismissRequest = { onEvent(OutletUiEvent.DismissDuplicateReportAlert) },
            containerColor = Color.White,
            title = { Text("Laporan sudah ada", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Laporan harian untuk tanggal $dupDateLabel sudah tercatat (${formatRupiah(duplicateReport.totalRevenue)}, ${duplicateReport.totalSold} pcs).",
                        fontSize = 13.sp,
                        color = SBColor.Grey_700
                    )
                    Text(
                        text = "Sistem menolak pembuatan laporan ganda di tanggal yang sama. Apakah Anda ingin memperbarui laporan tersebut?",
                        fontSize = 13.sp,
                        color = SBColor.Grey_600
                    )
                    TextButton(
                        onClick = { onEvent(OutletUiEvent.LoadExistingReportForEdit(duplicateReport.id)) }
                    ) {
                        Text(
                            text = "Muat data lama ke form untuk diedit",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = SBColor.Primary_550
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = { onEvent(OutletUiEvent.ConfirmOverwriteDuplicateReport(duplicateReport.id)) }
                ) {
                    Text("Ya, Perbarui", fontWeight = FontWeight.Bold, color = SBColor.Primary_550)
                }
            },
            dismissButton = {
                TextButton(onClick = { onEvent(OutletUiEvent.DismissDuplicateReportAlert) }) {
                    Text("Batal", color = SBColor.Grey_600)
                }
            }
        )
    }
}

@Composable
private fun ReportTopActionCard(
    todayReport: com.sudutcorp.sudutbulat.data.model.DailyReport?,
    missingCandidates: List<ReportDateCandidateUi>,
    isLoadingEdit: Boolean,
    onCreateTodayOrMissingClick: (com.google.firebase.Timestamp?) -> Unit,
    onEditTodayClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val timeFmt = remember { SimpleDateFormat("HH.mm", Locale.forLanguageTag("id-ID")) }
    val missingPastDays = remember(missingCandidates) {
        missingCandidates.filter { it.daysAgo > 0 }
    }

    if (todayReport == null) {
        OutlinedCard(
            modifier = modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.outlinedCardColors(
                containerColor = SBColor.Primary_100.copy(alpha = 0.5f)
            ),
            border = BorderStroke(1.dp, SBColor.Primary_200)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        modifier = Modifier.size(42.dp),
                        shape = RoundedCornerShape(12.dp),
                        color = Color.White
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                painter = painterResource(R.drawable.ic_wallet),
                                contentDescription = null,
                                tint = SBColor.Primary_550,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Text(
                            text = "Laporan hari ini belum dikirim",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = SBColor.Grey_700
                        )
                        Text(
                            text = if (missingPastDays.isNotEmpty()) {
                                "Ada ${missingPastDays.size} hari sebelumnya (≤7 hari) yang juga belum dilaporkan."
                            } else {
                                "Catat penjualan produk, pengeluaran, dan bahan rusak saat tutup toko."
                            },
                            fontSize = 12.sp,
                            color = SBColor.Grey_600
                        )
                    }
                }

                Button(
                    onClick = { onCreateTodayOrMissingClick(null) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = SBColor.Primary_550,
                        contentColor = Color.White
                    )
                ) {
                    Text(
                        text = "Buat laporan hari ini",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    } else {
        OutlinedCard(
            modifier = modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.outlinedCardColors(
                containerColor = SBColor.Success_100.copy(alpha = 0.65f)
            ),
            border = BorderStroke(1.dp, SBColor.Success_300)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            modifier = Modifier.size(40.dp),
                            shape = RoundedCornerShape(10.dp),
                            color = Color.White
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    painter = painterResource(R.drawable.ic_check),
                                    contentDescription = null,
                                    tint = SBColor.Success_600,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text(
                                text = "Laporan hari ini sudah terkirim",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = SBColor.Success_600
                            )
                            Text(
                                text = "${formatRupiah(todayReport.totalRevenue)} • ${todayReport.totalSold} pcs (${timeFmt.format(todayReport.date.toDate())})",
                                fontSize = 12.sp,
                                color = SBColor.Grey_700
                            )
                        }
                    }

                    Surface(
                        onClick = { if (!isLoadingEdit) onEditTodayClick(todayReport.id) },
                        shape = RoundedCornerShape(10.dp),
                        color = Color.White,
                        border = BorderStroke(1.dp, SBColor.Success_300)
                    ) {
                        Text(
                            text = if (isLoadingEdit) "Memuat..." else "Edit",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = SBColor.Success_600,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp)
                        )
                    }
                }

                if (missingPastDays.isNotEmpty()) {
                    HorizontalDivider(thickness = 1.dp, color = SBColor.Success_300.copy(alpha = 0.5f))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${missingPastDays.size} hari sebelumnya belum memiliki laporan",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = SBColor.Grey_700,
                            modifier = Modifier.weight(1f).padding(end = 8.dp)
                        )
                        Text(
                            text = "Isi laporan susulan →",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = SBColor.Primary_550,
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .clickable { onCreateTodayOrMissingClick(missingPastDays.first().timestamp) }
                                .padding(horizontal = 6.dp, vertical = 4.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun OutletReportSummaryTabContent(
    selectedPeriod: String,
    summary: OutletReportPeriodSummaryUi,
    onSelectPeriod: (String) -> Unit,
    onSeeHistoryClick: () -> Unit
) {
    val periods = remember {
        listOf("7 Hari" to "7 hari", "30 Hari" to "30 hari", "3 Bulan" to "3 bulan")
    }
    val activePeriodKey = periods.firstOrNull {
        it.first.equals(selectedPeriod, ignoreCase = true)
    }?.first ?: "7 Hari"

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        SBSegmentedTabs(
            options = periods.map { it.first },
            selected = activePeriodKey,
            labelOf = { key -> periods.firstOrNull { it.first == key }?.second ?: key },
            onSelected = onSelectPeriod
        )

        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = "Estimasi setoran bersih (${summary.periodLabel})",
                color = SBColor.Grey_600,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = summary.netRevenueText,
                fontSize = 28.sp,
                fontWeight = FontWeight.ExtraBold,
                color = SBColor.Grey_700
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.arrow_up),
                    contentDescription = null,
                    tint = if (summary.isPositiveDelta) SBColor.Success_600 else SBColor.Warning_600,
                    modifier = Modifier.size(12.dp)
                )
                Text(
                    text = summary.deltaText,
                    color = if (summary.isPositiveDelta) SBColor.Success_600 else SBColor.Warning_600,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // Tren Penjualan 7 Hari Terakhir
        OutlinedCard(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.outlinedCardColors(containerColor = Color.White),
            border = BorderStroke(1.dp, SBColor.Grey_100)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(
                            text = "Tren penjualan 7 hari terakhir",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = SBColor.Grey_700
                        )
                        Text(
                            text = summary.dateRangeText,
                            fontSize = 12.sp,
                            color = SBColor.Grey_500
                        )
                    }
                    Text(
                        text = "${summary.reportCount} laporan",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = SBColor.Primary_550
                    )
                }

                val bars = summary.trendBars
                if (bars.isNotEmpty()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(132.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        bars.forEachIndexed { idx, (day, heightFraction) ->
                            val isTodayBar = idx == bars.lastIndex
                            val barColor = if (isTodayBar) SBColor.Primary_550 else SBColor.Tertiary_100
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Bottom,
                                modifier = Modifier.fillMaxHeight()
                            ) {
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .width(28.dp),
                                    contentAlignment = Alignment.BottomCenter
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .fillMaxHeight(heightFraction)
                                            .clip(RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp))
                                            .background(barColor)
                                    )
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = day,
                                    fontSize = 11.sp,
                                    fontWeight = if (isTodayBar) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isTodayBar) SBColor.Primary_550 else SBColor.Grey_500
                                )
                            }
                        }
                    }
                }
            }
        }

        // Ringkasan Operasional (Seperti Layar Owner)
        SBSectionCard(
            title = "Ringkasan operasional",
            actionText = "Riwayat",
            onActionClick = onSeeHistoryClick
        ) {
            SummaryMetricRow(
                iconRes = R.drawable.ic_store,
                title = "Total penjualan kotor",
                subtitle = summary.totalProductsSoldText,
                value = summary.totalSalesValueText,
                valueColor = SBColor.Grey_700
            )
            HorizontalDivider(thickness = 1.dp, color = SBColor.Grey_100)
            SummaryMetricRow(
                iconRes = R.drawable.ic_wallet,
                title = "Biaya operasional",
                subtitle = summary.operationalCostPercentText,
                value = summary.operationalCostValueText,
                valueColor = SBColor.Grey_700
            )
            HorizontalDivider(thickness = 1.dp, color = SBColor.Grey_100)
            SummaryMetricRow(
                iconRes = R.drawable.ic_bar_chart,
                title = "Estimasi setoran bersih",
                subtitle = "Omzet dikurangi biaya operasional",
                value = summary.estimatedProfitValueText,
                valueColor = SBColor.Success_600
            )
        }

        SBSectionCard(title = "Rincian pembayaran & biaya") {
            SBKeyValueRow(label = "Penjualan tunai", value = summary.cashSalesText)
            SBKeyValueRow(label = "Penjualan QRIS", value = summary.qrisSalesText)
            SBDottedDivider(modifier = Modifier.padding(vertical = 2.dp))
            SBKeyValueRow(label = "Gas & bahan bakar", value = summary.gasExpenseText)
            SBKeyValueRow(label = "Plastik & tusuk", value = summary.plasticExpenseText)
            SBKeyValueRow(label = "Biaya lainnya", value = summary.otherExpenseText)
        }

        if (summary.damagedItems.isNotEmpty()) {
            SBSectionCard(title = "Rekap bahan mentah rusak") {
                summary.damagedItems.forEachIndexed { idx, dmg ->
                    SBKeyValueRow(label = dmg.name, value = dmg.quantityText)
                    if (idx < summary.damagedItems.lastIndex) {
                        HorizontalDivider(thickness = 1.dp, color = SBColor.Grey_100)
                    }
                }
            }
        }
    }
}

@Composable
private fun SummaryMetricRow(
    iconRes: Int,
    title: String,
    subtitle: String,
    value: String,
    valueColor: Color
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f).padding(end = 10.dp)
        ) {
            Surface(
                modifier = Modifier.size(40.dp),
                shape = RoundedCornerShape(10.dp),
                color = SBColor.Primary_100
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        painter = painterResource(id = iconRes),
                        contentDescription = title,
                        tint = SBColor.Primary_550,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(title, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = SBColor.Grey_700)
                Text(subtitle, fontSize = 12.sp, color = SBColor.Grey_500)
            }
        }
        Text(value, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = valueColor)
    }
}

@Composable
private fun OutletReportHistoryCard(
    item: OutletReportHistoryItemUi,
    isLoadingEdit: Boolean,
    onClick: () -> Unit,
    onEditClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedCard(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.outlinedCardColors(containerColor = Color.White),
        border = BorderStroke(
            width = 1.dp,
            color = if (item.daysAgo == 0) SBColor.Primary_200 else SBColor.Grey_100
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier.weight(1f).padding(end = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Text(
                        text = item.dateLabel,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = SBColor.Grey_700
                    )
                    Text(
                        text = "Dikirim pkl ${item.timeLabel}",
                        fontSize = 12.sp,
                        color = SBColor.Grey_500
                    )
                }
                SBStatusBadge(
                    text = if (item.isEditable) "Bisa diedit" else "Terkunci (>3h)",
                    containerColor = if (item.isEditable) SBColor.Primary_100 else SBColor.Grey_100,
                    contentColor = if (item.isEditable) SBColor.Primary_550 else SBColor.Grey_500
                )
            }

            // Informasi Staf Pengirim
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                color = SBColor.Grey_100.copy(alpha = 0.65f)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        modifier = Modifier.size(26.dp),
                        shape = CircleShape,
                        color = SBColor.Secondary_100
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = item.createdByInitials,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = SBColor.Primary_550
                            )
                        }
                    }
                    Text(
                        text = "Pengirim: ${item.createdByName}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = SBColor.Grey_700,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            HorizontalDivider(thickness = 1.dp, color = SBColor.Grey_100)

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text("Total omzet", fontSize = 11.sp, color = SBColor.Grey_500)
                    Text(
                        text = formatRupiah(item.totalRevenue),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = SBColor.Grey_700
                    )
                    Text(
                        text = "Tunai ${formatShortRupiah(item.cashRevenue)} • QRIS ${formatShortRupiah(item.qrisRevenue)}",
                        fontSize = 11.sp,
                        color = SBColor.Grey_500
                    )
                }
                Column(
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Text(
                        text = "${formatDecimal(item.totalSold)} pcs terjual",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = SBColor.Grey_700
                    )
                    Text(
                        text = if (item.damagedCount > 0) {
                            "Biaya ${formatShortRupiah(item.totalExpense)} • Rusak ${item.damagedCount}"
                        } else {
                            "Biaya ${formatShortRupiah(item.totalExpense)}"
                        },
                        fontSize = 11.sp,
                        color = if (item.damagedCount >= 10) SBColor.Primary_550 else SBColor.Grey_500
                    )
                    Text(
                        text = "Setor ${formatShortRupiah(item.netDeposit)}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = SBColor.Success_600
                    )
                }
            }

            if (item.isEditable) {
                OutlinedButton(
                    onClick = onEditClick,
                    enabled = !isLoadingEdit,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(38.dp),
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, SBColor.Primary_200),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Text(
                        text = if (isLoadingEdit) "Memuat data..." else "Edit laporan",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = SBColor.Primary_550
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun OutletReportDetailBottomSheet(
    item: OutletReportHistoryItemUi,
    outletName: String,
    isLoadingEdit: Boolean,
    onDismiss: () -> Unit,
    onEditClick: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = Color.White,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        contentWindowInsets = { WindowInsets(0, 0, 0, 0) }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.88f)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .padding(bottom = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier.weight(1f).padding(end = 10.dp),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Text(
                        text = "Detail laporan harian",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = SBColor.Grey_700
                    )
                    Text(
                        text = "${item.fullDateLabel} • ${item.timeLabel}",
                        fontSize = 12.sp,
                        color = SBColor.Grey_500
                    )
                }
                SBStatusBadge(
                    text = if (item.isEditable) "Bisa diedit" else "Terkunci",
                    containerColor = if (item.isEditable) SBColor.Primary_100 else SBColor.Grey_100,
                    contentColor = if (item.isEditable) SBColor.Primary_550 else SBColor.Grey_500
                )
            }

            HorizontalDivider(thickness = 1.dp, color = SBColor.Grey_100)

            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                OutlinedCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.outlinedCardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, SBColor.Grey_100)
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        SBKeyValueRow(label = "Outlet", value = outletName)
                        SBKeyValueRow(label = "Staf pengirim", value = item.createdByName)
                        SBKeyValueRow(label = "Produk terjual", value = "${formatDecimal(item.totalSold)} pcs")
                        SBKeyValueRow(label = "Penjualan tunai", value = formatRupiah(item.cashRevenue))
                        SBKeyValueRow(label = "Penjualan QRIS", value = formatRupiah(item.qrisRevenue))
                        SBDottedDivider(modifier = Modifier.padding(vertical = 2.dp))
                        SBKeyValueRow(
                            label = "Total omzet kotor",
                            value = formatRupiah(item.totalRevenue),
                            isBold = true
                        )
                    }
                }

                if (item.productLines.isNotEmpty()) {
                    SBSectionCard(title = "Rincian produk terjual") {
                        item.productLines.forEachIndexed { idx, prod ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(
                                    modifier = Modifier.weight(1f).padding(end = 10.dp),
                                    verticalArrangement = Arrangement.spacedBy(2.dp)
                                ) {
                                    Text(
                                        text = prod.productName,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = SBColor.Grey_700
                                    )
                                    Text(
                                        text = "${prod.quantity} × ${formatRupiah(prod.price)}",
                                        fontSize = 12.sp,
                                        color = SBColor.Grey_500
                                    )
                                }
                                Text(
                                    text = formatRupiah(prod.subtotal),
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = SBColor.Grey_700
                                )
                            }
                            if (idx < item.productLines.lastIndex) {
                                HorizontalDivider(thickness = 1.dp, color = SBColor.Grey_100)
                            }
                        }
                    }
                }

                SBSectionCard(title = "Pengeluaran operasional") {
                    if (item.expenseLines.isEmpty()) {
                        Text(
                            text = "Tidak ada pengeluaran operasional yang dicatat.",
                            fontSize = 13.sp,
                            color = SBColor.Grey_500
                        )
                    } else {
                        item.expenseLines.forEachIndexed { idx, exp ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.Top
                            ) {
                                Column(
                                    modifier = Modifier.weight(1f).padding(end = 10.dp),
                                    verticalArrangement = Arrangement.spacedBy(2.dp)
                                ) {
                                    Text(
                                        text = exp.categoryLabel,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = SBColor.Grey_700
                                    )
                                    if (!exp.note.isNullOrBlank()) {
                                        Text(exp.note, fontSize = 12.sp, color = SBColor.Grey_500)
                                    }
                                }
                                Text(
                                    text = formatRupiah(exp.amount),
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = SBColor.Grey_700
                                )
                            }
                            if (idx < item.expenseLines.lastIndex) {
                                HorizontalDivider(thickness = 1.dp, color = SBColor.Grey_100)
                            }
                        }
                        SBDottedDivider(modifier = Modifier.padding(vertical = 2.dp))
                        SBKeyValueRow(
                            label = "Total pengeluaran",
                            value = formatRupiah(item.totalExpense),
                            isBold = true
                        )
                    }
                }

                SBSectionCard(title = "Bahan mentah rusak") {
                    if (item.damagedLines.isEmpty()) {
                        Text(
                            text = "Tidak ada bahan rusak yang dilaporkan.",
                            fontSize = 13.sp,
                            color = SBColor.Grey_500
                        )
                    } else {
                        item.damagedLines.forEachIndexed { idx, dmg ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.Top
                            ) {
                                Column(
                                    modifier = Modifier.weight(1f).padding(end = 10.dp),
                                    verticalArrangement = Arrangement.spacedBy(2.dp)
                                ) {
                                    Text(
                                        text = dmg.materialName,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = SBColor.Grey_700
                                    )
                                    if (!dmg.note.isNullOrBlank()) {
                                        Text(dmg.note, fontSize = 12.sp, color = SBColor.Grey_500)
                                    }
                                }
                                SBStatusBadge(
                                    text = dmg.quantityText,
                                    containerColor = SBColor.Primary_100,
                                    contentColor = SBColor.Primary_550
                                )
                            }
                            if (idx < item.damagedLines.lastIndex) {
                                HorizontalDivider(thickness = 1.dp, color = SBColor.Grey_100)
                            }
                        }
                    }
                }

                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    color = SBColor.Success_100
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text(
                                text = "Estimasi setoran bersih",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = SBColor.Success_600
                            )
                            Text(
                                text = "Omzet dikurangi pengeluaran operasional",
                                fontSize = 11.sp,
                                color = SBColor.Success_500
                            )
                        }
                        Text(
                            text = formatRupiah(item.netDeposit),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = SBColor.Success_600
                        )
                    }
                }
            }

            if (item.isEditable) {
                SBBottomCtaBar(
                    label = "Edit laporan ini",
                    isLoading = isLoadingEdit,
                    onClick = onEditClick
                )
            } else {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding(),
                    color = SBColor.Grey_100
                ) {
                    Text(
                        text = "Laporan berusia lebih dari 3 hari terakhir tidak dapat diubah lagi.",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = SBColor.Grey_600,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 14.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun OutletReportFormDialog(
    outletName: String,
    form: DailyReportFormState,
    dateCandidates: List<ReportDateCandidateUi>,
    activeProducts: List<Product>,
    activeMaterials: List<Material>,
    onEvent: (OutletUiEvent) -> Unit
) {
    val focusManager = LocalFocusManager.current
    val listState = rememberLazyListState()
    var showAddExpenseDialog by rememberSaveable { mutableStateOf(false) }
    var showAddDamagedDialog by rememberSaveable { mutableStateOf(false) }

    val isEditing = !form.editingReportId.isNullOrBlank()
    val fullDateFmt = remember { SimpleDateFormat("EEEE, dd MMM yyyy", Locale.forLanguageTag("id-ID")) }

    val totalSold = remember(form.productQuantities) {
        form.productQuantities.values.sumOf { it.toIntOrNull() ?: 0 }
    }
    val selectedProductCount = remember(form.productQuantities) {
        form.productQuantities.values.count { (it.toIntOrNull() ?: 0) > 0 }
    }
    val autoSubtotal = remember(form.productQuantities, activeProducts) {
        activeProducts.sumOf { prod ->
            (form.productQuantities[prod.id]?.toIntOrNull() ?: 0).toLong() * prod.price
        }
    }
    val cashVal = form.cashRevenueInput.toLongOrNull() ?: autoSubtotal
    val qrisVal = form.qrisRevenueInput.toLongOrNull() ?: 0L
    val grossRevenue = cashVal + qrisVal
    val totalExpense = form.expenses.sumOf { it.amount }
    val netDeposit = (grossRevenue - totalExpense).coerceAtLeast(0L)

    BackHandler(enabled = form.isSaving) {
        // Prevent dismissing while save is in flight
    }

    Dialog(
        onDismissRequest = {
            if (!form.isSaving) {
                onEvent(OutletUiEvent.CloseDailyReportForm)
            }
        },
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false
        )
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = Color.White
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .imePadding()
                    .pointerInput(Unit) { detectTapGestures(onTap = { focusManager.clearFocus() }) }
            ) {
                SBSubHeader(
                    eyebrow = outletName,
                    title = if (isEditing) "Edit laporan harian" else "Buat laporan harian",
                    onBackClick = {
                        if (!form.isSaving) {
                            onEvent(OutletUiEvent.CloseDailyReportForm)
                        }
                    },
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 14.dp)
                )

                HorizontalDivider(thickness = 1.dp, color = SBColor.Grey_100)

                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // 0. Pilih Tanggal (7 Hari ke Belakang & Hanya Hari Kosong) atau Banner Edit
                    item(key = "form_date_section", contentType = "date_section") {
                        if (isEditing) {
                            val editDateLabel = form.editingReportDate?.let {
                                fullDateFmt.format(it.toDate())
                            } ?: "Hari ini"
                            OutlinedCard(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(14.dp),
                                colors = CardDefaults.outlinedCardColors(containerColor = SBColor.Primary_100),
                                border = BorderStroke(1.dp, SBColor.Primary_200)
                            ) {
                                Column(
                                    modifier = Modifier.padding(14.dp),
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Text(
                                        text = "Mengedit laporan: $editDateLabel",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = SBColor.Primary_550
                                    )
                                    Text(
                                        text = "Laporan hanya dapat diubah dalam batas 3 hari terakhir.",
                                        fontSize = 12.sp,
                                        color = SBColor.Grey_700
                                    )
                                }
                            }
                        } else {
                            ReportDatePickerCard(
                                candidates = dateCandidates,
                                selectedDate = form.selectedCreateDate,
                                onSelectDate = { onEvent(OutletUiEvent.SelectCreateReportDate(it)) }
                            )
                        }
                    }

                    // 1. Produk Terjual
                    item(key = "form_products_header", contentType = "section_header") {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                Text(
                                    text = "1. Produk terjual",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = SBColor.Grey_700
                                )
                                Text(
                                    text = "$selectedProductCount jenis • ${formatDecimal(totalSold)} pcs • ${formatRupiah(autoSubtotal)}",
                                    fontSize = 12.sp,
                                    fontWeight = if (totalSold > 0) FontWeight.SemiBold else FontWeight.Normal,
                                    color = if (totalSold > 0) SBColor.Primary_550 else SBColor.Grey_500
                                )
                            }
                        }
                    }

                    items(
                        items = activeProducts,
                        key = { it.id },
                        contentType = { "form_product_item" }
                    ) { prod ->
                        val qtyStr = form.productQuantities[prod.id].orEmpty()
                        val qty = qtyStr.toIntOrNull() ?: 0
                        val isSelected = qty > 0
                        val subtotal = qty.toLong() * prod.price
                        val saleLabel = if (prod.saleType == ProductSaleType.BUNGKUS) {
                            "Bungkus (${prod.pcsPerPack} pcs)"
                        } else {
                            "Satuan"
                        }

                        OutlinedCard(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.outlinedCardColors(
                                containerColor = if (isSelected) SBColor.Primary_100.copy(alpha = 0.28f) else Color.White
                            ),
                            border = BorderStroke(
                                width = 1.dp,
                                color = if (isSelected) SBColor.Primary_200 else SBColor.Grey_100
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 14.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Column(
                                    modifier = Modifier.weight(1f),
                                    verticalArrangement = Arrangement.spacedBy(3.dp)
                                ) {
                                    Text(
                                        text = prod.name,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = SBColor.Grey_700
                                    )
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Surface(
                                            shape = RoundedCornerShape(5.dp),
                                            color = SBColor.Grey_100
                                        ) {
                                            Text(
                                                text = saleLabel,
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = SBColor.Grey_600,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                        Text(
                                            text = formatRupiah(prod.price),
                                            fontSize = 12.sp,
                                            color = SBColor.Grey_500
                                        )
                                    }
                                    if (isSelected) {
                                        Text(
                                            text = "Subtotal: ${formatRupiah(subtotal)}",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = SBColor.Primary_550
                                        )
                                    }
                                }
                                ReportQuantityStepper(
                                    value = qtyStr,
                                    onValueChange = { onEvent(OutletUiEvent.UpdateProductQty(prod.id, it)) }
                                )
                            }
                        }
                    }

                    // 2. Rincian Pembayaran (Tunai & QRIS)
                    item(key = "form_payment_section", contentType = "payment_section") {
                        OutlinedCard(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.outlinedCardColors(containerColor = Color.White),
                            border = BorderStroke(1.dp, SBColor.Grey_100)
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "2. Rincian Tunai & QRIS",
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = SBColor.Grey_700,
                                        modifier = Modifier.weight(1f).padding(end = 8.dp)
                                    )
                                    Text(
                                        text = "Omzet: ${formatRupiah(autoSubtotal)}",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = SBColor.Primary_550
                                    )
                                }

                                if (autoSubtotal > 0L) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .horizontalScroll(rememberScrollState()),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Surface(
                                            onClick = {
                                                onEvent(OutletUiEvent.UpdateCashRevenue(autoSubtotal.toString()))
                                                onEvent(OutletUiEvent.UpdateQrisRevenue("0"))
                                            },
                                            shape = RoundedCornerShape(8.dp),
                                            color = SBColor.Grey_100
                                        ) {
                                            Text(
                                                text = "100% Tunai",
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = SBColor.Grey_700,
                                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                            )
                                        }
                                        Surface(
                                            onClick = {
                                                val rem = (autoSubtotal - qrisVal).coerceAtLeast(0L)
                                                onEvent(OutletUiEvent.UpdateCashRevenue(rem.toString()))
                                            },
                                            shape = RoundedCornerShape(8.dp),
                                            color = SBColor.Primary_100
                                        ) {
                                            Text(
                                                text = "Hitung Tunai (Omzet − QRIS)",
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = SBColor.Primary_550,
                                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                            )
                                        }
                                        Surface(
                                            onClick = {
                                                val rem = (autoSubtotal - cashVal).coerceAtLeast(0L)
                                                onEvent(OutletUiEvent.UpdateQrisRevenue(rem.toString()))
                                            },
                                            shape = RoundedCornerShape(8.dp),
                                            color = SBColor.Secondary_100
                                        ) {
                                            Text(
                                                text = "Hitung QRIS (Omzet − Tunai)",
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = SBColor.Secondary_500,
                                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                            )
                                        }
                                    }
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    OutlinedTextField(
                                        value = form.cashRevenueInput,
                                        onValueChange = { onEvent(OutletUiEvent.UpdateCashRevenue(it)) },
                                        modifier = Modifier.weight(1f),
                                        label = { Text("Tunai (Rp)", fontSize = 12.sp) },
                                        placeholder = { Text(autoSubtotal.toString()) },
                                        singleLine = true,
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        shape = RoundedCornerShape(12.dp),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = SBColor.Primary_550,
                                            unfocusedBorderColor = SBColor.Grey_200
                                        )
                                    )
                                    OutlinedTextField(
                                        value = form.qrisRevenueInput,
                                        onValueChange = { onEvent(OutletUiEvent.UpdateQrisRevenue(it)) },
                                        modifier = Modifier.weight(1f),
                                        label = { Text("QRIS (Rp)", fontSize = 12.sp) },
                                        placeholder = { Text("0") },
                                        singleLine = true,
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        shape = RoundedCornerShape(12.dp),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = SBColor.Primary_550,
                                            unfocusedBorderColor = SBColor.Grey_200
                                        )
                                    )
                                }
                            }
                        }
                    }

                    // 3. Pengeluaran Operasional
                    item(key = "form_expenses_section", contentType = "expenses_section") {
                        OutlinedCard(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.outlinedCardColors(containerColor = Color.White),
                            border = BorderStroke(1.dp, SBColor.Grey_100)
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(
                                        modifier = Modifier.weight(1f).padding(end = 10.dp),
                                        verticalArrangement = Arrangement.spacedBy(2.dp)
                                    ) {
                                        Text(
                                            text = "3. Pengeluaran operasional",
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = SBColor.Grey_700
                                        )
                                        Text(
                                            text = if (form.expenses.isEmpty()) {
                                                "Gas LPG, plastik, minyak goreng, dll."
                                            } else {
                                                "${form.expenses.size} item • Total ${formatRupiah(totalExpense)}"
                                            },
                                            fontSize = 12.sp,
                                            color = SBColor.Grey_500
                                        )
                                    }
                                    Surface(
                                        onClick = { showAddExpenseDialog = true },
                                        shape = RoundedCornerShape(8.dp),
                                        color = SBColor.Primary_100
                                    ) {
                                        Text(
                                            text = "+ Tambah",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = SBColor.Primary_550,
                                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp)
                                        )
                                    }
                                }

                                if (form.expenses.isNotEmpty()) {
                                    HorizontalDivider(thickness = 1.dp, color = SBColor.Grey_100)
                                    form.expenses.forEach { exp ->
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column(
                                                modifier = Modifier.weight(1f).padding(end = 8.dp),
                                                verticalArrangement = Arrangement.spacedBy(2.dp)
                                            ) {
                                                Text(
                                                    text = exp.category.displayLabel(),
                                                    fontSize = 13.sp,
                                                    fontWeight = FontWeight.SemiBold,
                                                    color = SBColor.Grey_700
                                                )
                                                if (exp.note.isNotBlank()) {
                                                    Text(exp.note, fontSize = 12.sp, color = SBColor.Grey_500)
                                                }
                                            }
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                                            ) {
                                                Text(
                                                    text = formatRupiah(exp.amount),
                                                    fontSize = 13.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = SBColor.Grey_700
                                                )
                                                Text(
                                                    text = "Hapus",
                                                    fontSize = 12.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = SBColor.Primary_550,
                                                    modifier = Modifier
                                                        .clip(RoundedCornerShape(6.dp))
                                                        .clickable { onEvent(OutletUiEvent.RemoveDraftExpense(exp.id)) }
                                                        .padding(horizontal = 6.dp, vertical = 4.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // 4. Bahan Mentah Rusak
                    item(key = "form_damaged_section", contentType = "damaged_section") {
                        OutlinedCard(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.outlinedCardColors(containerColor = Color.White),
                            border = BorderStroke(1.dp, SBColor.Grey_100)
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(
                                        modifier = Modifier.weight(1f).padding(end = 10.dp),
                                        verticalArrangement = Arrangement.spacedBy(2.dp)
                                    ) {
                                        Text(
                                            text = "4. Bahan mentah rusak",
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = SBColor.Grey_700
                                        )
                                        Text(
                                            text = if (form.damagedMaterials.isEmpty()) {
                                                "Opsional jika ada bahan rusak / gagal goreng"
                                            } else {
                                                "${form.damagedMaterials.size} jenis • ${form.damagedMaterials.sumOf { it.quantity }} unit"
                                            },
                                            fontSize = 12.sp,
                                            color = SBColor.Grey_500
                                        )
                                    }
                                    Surface(
                                        onClick = { showAddDamagedDialog = true },
                                        shape = RoundedCornerShape(8.dp),
                                        color = SBColor.Secondary_100
                                    ) {
                                        Text(
                                            text = "+ Tambah",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = SBColor.Secondary_500,
                                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp)
                                        )
                                    }
                                }

                                if (form.damagedMaterials.isNotEmpty()) {
                                    HorizontalDivider(thickness = 1.dp, color = SBColor.Grey_100)
                                    form.damagedMaterials.forEach { dmg ->
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column(
                                                modifier = Modifier.weight(1f).padding(end = 8.dp),
                                                verticalArrangement = Arrangement.spacedBy(2.dp)
                                            ) {
                                                Text(
                                                    text = "${dmg.materialName} (${dmg.quantity} unit)",
                                                    fontSize = 13.sp,
                                                    fontWeight = FontWeight.SemiBold,
                                                    color = SBColor.Grey_700
                                                )
                                                if (dmg.note.isNotBlank()) {
                                                    Text(dmg.note, fontSize = 12.sp, color = SBColor.Grey_500)
                                                }
                                            }
                                            Text(
                                                text = "Hapus",
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = SBColor.Primary_550,
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(6.dp))
                                                    .clickable { onEvent(OutletUiEvent.RemoveDraftDamaged(dmg.id)) }
                                                    .padding(horizontal = 6.dp, vertical = 4.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // 5. Ringkasan Setoran
                    item(key = "form_summary_card", contentType = "summary_card") {
                        OutlinedCard(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.outlinedCardColors(containerColor = Color.White),
                            border = BorderStroke(1.dp, SBColor.Grey_100)
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = "Ringkasan setoran",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = SBColor.Grey_700
                                )
                                SBKeyValueRow(label = "Penjualan tunai", value = formatRupiah(cashVal))
                                SBKeyValueRow(label = "Penjualan QRIS", value = formatRupiah(qrisVal))
                                SBKeyValueRow(label = "Total produk terjual", value = "${formatDecimal(totalSold)} pcs")
                                SBKeyValueRow(label = "Pengeluaran operasional", value = "- ${formatRupiah(totalExpense)}")
                                SBDottedDivider(modifier = Modifier.padding(vertical = 2.dp))
                                SBKeyValueRow(
                                    label = "Estimasi setoran bersih",
                                    value = formatRupiah(netDeposit),
                                    isBold = true,
                                    valueColor = SBColor.Success_600,
                                    valueFontSize = 16.sp
                                )
                            }
                        }
                    }

                    form.errorMessage?.let { err ->
                        item(key = "form_error_alert", contentType = "error_alert") {
                            SBInlineAlert(message = err)
                        }
                    }
                }

                val ctaLabel = if (isEditing) {
                    "Simpan pembaruan • ${formatShortRupiah(netDeposit)}"
                } else {
                    "Kirim laporan • ${formatShortRupiah(netDeposit)}"
                }
                SBBottomCtaBar(
                    label = ctaLabel,
                    isLoading = form.isSaving || form.isLoadingExisting,
                    onClick = { onEvent(OutletUiEvent.SubmitDailyReport) }
                )
            }
        }
    }

    if (showAddExpenseDialog) {
        AddExpenseDialog(
            onDismiss = { showAddExpenseDialog = false },
            onAdd = { cat, amt, note ->
                onEvent(OutletUiEvent.AddDraftExpense(cat, amt, note))
                showAddExpenseDialog = false
            }
        )
    }

    if (showAddDamagedDialog) {
        AddDamagedMaterialDialog(
            activeMaterials = activeMaterials,
            onDismiss = { showAddDamagedDialog = false },
            onAdd = { matId, qty, note ->
                onEvent(OutletUiEvent.AddDraftDamaged(matId, qty, note))
                showAddDamagedDialog = false
            }
        )
    }
}

@Composable
private fun ReportDatePickerCard(
    candidates: List<ReportDateCandidateUi>,
    selectedDate: com.google.firebase.Timestamp?,
    onSelectDate: (com.google.firebase.Timestamp) -> Unit
) {
    val activeSelected = selectedDate ?: candidates.firstOrNull { !it.hasExistingReport }?.timestamp

    OutlinedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.outlinedCardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, SBColor.Grey_100)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = "Tanggal laporan",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = SBColor.Grey_700
                )
                Text(
                    text = "Maks. 7 hari ke belakang (hanya tanggal yang belum melapor)",
                    fontSize = 12.sp,
                    color = SBColor.Grey_500
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                candidates.forEach { candidate ->
                    val isSelected = activeSelected != null &&
                        isSameCalendarDay(candidate.timestamp, activeSelected)
                    val isAvailable = !candidate.hasExistingReport

                    val containerColor = when {
                        isSelected -> SBColor.Primary_550
                        !isAvailable -> SBColor.Grey_100
                        else -> Color.White
                    }
                    val borderColor = when {
                        isSelected -> SBColor.Primary_550
                        !isAvailable -> SBColor.Grey_100
                        else -> SBColor.Grey_200
                    }
                    val titleColor = when {
                        isSelected -> Color.White
                        !isAvailable -> SBColor.Grey_400
                        else -> SBColor.Grey_700
                    }
                    val subColor = when {
                        isSelected -> Color.White.copy(alpha = 0.9f)
                        !isAvailable -> SBColor.Grey_400
                        else -> SBColor.Success_600
                    }

                    Surface(
                        onClick = { if (isAvailable) onSelectDate(candidate.timestamp) },
                        enabled = isAvailable,
                        shape = RoundedCornerShape(12.dp),
                        color = containerColor,
                        border = BorderStroke(1.dp, borderColor)
                    ) {
                        Column(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 9.dp),
                            verticalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            Text(
                                text = candidate.label,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = titleColor
                            )
                            Text(
                                text = if (candidate.hasExistingReport) "✓ Sudah ada" else "Belum lapor",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = subColor
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AddExpenseDialog(
    onDismiss: () -> Unit,
    onAdd: (ExpenseCategory, Long, String) -> Unit
) {
    var category by rememberSaveable { mutableStateOf(ExpenseCategory.GAS) }
    var amountInput by rememberSaveable { mutableStateOf("") }
    var noteInput by rememberSaveable { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color.White,
        title = { Text("Catat pengeluaran operasional", fontSize = 17.sp, fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    ExpenseCategory.entries.forEach { cat ->
                        val shortLabel = when (cat) {
                            ExpenseCategory.GAS -> "Gas"
                            ExpenseCategory.PLASTIC_BAG -> "Plastik"
                            ExpenseCategory.OTHER -> "Lainnya"
                        }
                        FilterChip(
                            selected = category == cat,
                            onClick = { category = cat },
                            label = { Text(shortLabel, fontSize = 12.sp, fontWeight = FontWeight.SemiBold) },
                            shape = RoundedCornerShape(9.dp),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = SBColor.Primary_100,
                                selectedLabelColor = SBColor.Primary_550
                            )
                        )
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    QUICK_EXPENSE_AMOUNTS.forEach { amt ->
                        Surface(
                            onClick = { amountInput = amt.toString() },
                            shape = RoundedCornerShape(8.dp),
                            color = SBColor.Grey_100
                        ) {
                            Text(
                                text = formatShortRupiah(amt),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = SBColor.Grey_700,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = amountInput,
                    onValueChange = { amountInput = it.filter(Char::isDigit) },
                    label = { Text("Nominal (Rp)") },
                    placeholder = { Text("22000") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    shape = RoundedCornerShape(10.dp)
                )
                OutlinedTextField(
                    value = noteInput,
                    onValueChange = { noteInput = it },
                    label = { Text("Catatan") },
                    placeholder = {
                        Text(
                            when (category) {
                                ExpenseCategory.GAS -> "Beli Gas LPG 3kg"
                                ExpenseCategory.PLASTIC_BAG -> "Plastik kresek & tusuk lidi"
                                ExpenseCategory.OTHER -> "Minyak goreng 2 liter"
                            }
                        )
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp)
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val amt = amountInput.toLongOrNull() ?: 0L
                    if (amt > 0L) {
                        onAdd(category, amt, noteInput)
                    }
                }
            ) {
                Text("Tambah", fontWeight = FontWeight.Bold, color = SBColor.Primary_550)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Batal", color = SBColor.Grey_600)
            }
        }
    )
}

@Composable
private fun AddDamagedMaterialDialog(
    activeMaterials: List<Material>,
    onDismiss: () -> Unit,
    onAdd: (String, Int, String) -> Unit
) {
    var selectedMatId by rememberSaveable { mutableStateOf(activeMaterials.firstOrNull()?.id.orEmpty()) }
    var qtyInput by rememberSaveable { mutableStateOf("") }
    var noteInput by rememberSaveable { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color.White,
        title = { Text("Laporkan bahan mentah rusak", fontSize = 17.sp, fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Pilih bahan mentah:", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = SBColor.Grey_600)
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 160.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(
                        items = activeMaterials,
                        key = { it.id },
                        contentType = { "damaged_material_choice" }
                    ) { mat ->
                        val isChosen = selectedMatId == mat.id
                        OutlinedCard(
                            onClick = { selectedMatId = mat.id },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp),
                            colors = CardDefaults.outlinedCardColors(
                                containerColor = if (isChosen) SBColor.Primary_100.copy(alpha = 0.4f) else Color.White
                            ),
                            border = BorderStroke(
                                width = 1.dp,
                                color = if (isChosen) SBColor.Primary_400 else SBColor.Grey_100
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = mat.name,
                                    fontSize = 13.sp,
                                    fontWeight = if (isChosen) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isChosen) SBColor.Primary_550 else SBColor.Grey_700
                                )
                                Text(
                                    text = mat.unit.firestoreValue,
                                    fontSize = 11.sp,
                                    color = SBColor.Grey_500
                                )
                            }
                        }
                    }
                }

                OutlinedTextField(
                    value = qtyInput,
                    onValueChange = { qtyInput = it.filter(Char::isDigit) },
                    label = { Text("Jumlah rusak") },
                    placeholder = { Text("5") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    shape = RoundedCornerShape(10.dp)
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    QUICK_DAMAGED_REASONS.forEach { reason ->
                        Surface(
                            onClick = { noteInput = reason },
                            shape = RoundedCornerShape(8.dp),
                            color = SBColor.Grey_100
                        ) {
                            Text(
                                text = reason,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = SBColor.Grey_700,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = noteInput,
                    onValueChange = { noteInput = it },
                    label = { Text("Alasan / catatan") },
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp)
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val qty = qtyInput.toIntOrNull() ?: 0
                    if (selectedMatId.isNotBlank() && qty > 0) {
                        onAdd(selectedMatId, qty, noteInput)
                    }
                }
            ) {
                Text("Tambah", fontWeight = FontWeight.Bold, color = SBColor.Primary_550)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Batal", color = SBColor.Grey_600)
            }
        }
    )
}

@Composable
private fun ReportQuantityStepper(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val current = value.toIntOrNull() ?: 0
    Surface(
        modifier = modifier.height(40.dp),
        shape = RoundedCornerShape(10.dp),
        color = Color.White,
        border = BorderStroke(1.dp, SBColor.Grey_200)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(36.dp, 40.dp)
                    .clickable(enabled = current > 0) {
                        val next = (current - 1).coerceAtLeast(0)
                        onValueChange(if (next == 0) "" else next.toString())
                    },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "−",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (current > 0) SBColor.Grey_700 else SBColor.Grey_300
                )
            }
            Box(
                modifier = Modifier
                    .width(56.dp)
                    .height(40.dp)
                    .background(SBColor.Grey_100.copy(alpha = 0.5f)),
                contentAlignment = Alignment.Center
            ) {
                BasicTextField(
                    value = value,
                    onValueChange = { onValueChange(it.filter(Char::isDigit).take(5)) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    textStyle = TextStyle(
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = SBColor.Grey_700,
                        textAlign = TextAlign.Center
                    ),
                    cursorBrush = SolidColor(SBColor.Primary_550),
                    decorationBox = { innerTextField ->
                        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                            if (value.isEmpty()) {
                                Text(
                                    text = "0",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = SBColor.Grey_400
                                )
                            }
                            innerTextField()
                        }
                    }
                )
            }
            Box(
                modifier = Modifier
                    .size(36.dp, 40.dp)
                    .clickable { onValueChange((current + 1).toString()) },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "+",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = SBColor.Primary_550
                )
            }
        }
    }
}
