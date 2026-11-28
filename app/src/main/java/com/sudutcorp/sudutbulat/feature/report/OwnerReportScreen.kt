package com.sudutcorp.sudutbulat.feature.report

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.sudutcorp.sudutbulat.R
import com.sudutcorp.sudutbulat.core.components.SBDatePickerDialog
import com.sudutcorp.sudutbulat.core.components.SBFilterChipRow
import com.sudutcorp.sudutbulat.core.components.SBPageHeader
import com.sudutcorp.sudutbulat.core.components.SBSearchBar
import com.sudutcorp.sudutbulat.core.components.SBSegmentedTabs
import com.sudutcorp.sudutbulat.core.components.SBStatusBadge
import com.sudutcorp.sudutbulat.core.ui.theme.SBColor
import com.sudutcorp.sudutbulat.core.util.formatDecimal
import com.sudutcorp.sudutbulat.core.util.formatRupiah
import com.sudutcorp.sudutbulat.data.model.ReportReviewStatus
import com.sudutcorp.sudutbulat.feature.owner.DailyReportListItemUi
import com.sudutcorp.sudutbulat.feature.owner.OwnerUiEvent
import com.sudutcorp.sudutbulat.feature.owner.OwnerUiState
import com.sudutcorp.sudutbulat.feature.owner.ReportPeriodSummaryUi
import com.sudutcorp.sudutbulat.feature.owner.ReportReviewFilter
import com.sudutcorp.sudutbulat.feature.owner.ReportTabMode
import com.sudutcorp.sudutbulat.feature.owner.filterDailyReports
import com.sudutcorp.sudutbulat.feature.owner.formatUtcDateMillis

@Composable
fun OwnerReportScreen(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    uiState: OwnerUiState = OwnerUiState(),
    reportSummary: ReportPeriodSummaryUi = defaultReportPeriodSummary(),
    onEvent: (OwnerUiEvent) -> Unit = {},
    isDevMode: Boolean = false,
    onSwitchRoleClick: () -> Unit = {},
    onSeeDetailClick: (String) -> Unit = {},
    onReportItemClick: (String) -> Unit = {}
) {
    OwnerReportContent(
        modifier = modifier,
        uiState = uiState,
        initials = if (isDevMode) "DV" else uiState.ownerInitials,
        reportSummary = reportSummary,
        isDevMode = isDevMode,
        onEvent = onEvent,
        onProfileClick = onSwitchRoleClick,
        onSeeDetailClick = { onSeeDetailClick(uiState.selectedReportPeriod) },
        onReportItemClick = onReportItemClick
    )
}

@Composable
fun OwnerReportContent(
    modifier: Modifier = Modifier,
    uiState: OwnerUiState = OwnerUiState(),
    initials: String = "AR",
    reportSummary: ReportPeriodSummaryUi = defaultReportPeriodSummary(),
    isDevMode: Boolean = false,
    onEvent: (OwnerUiEvent) -> Unit = {},
    onProfileClick: () -> Unit = {},
    onSeeDetailClick: () -> Unit = {},
    onReportItemClick: (String) -> Unit = {}
) {
    val focusManager = LocalFocusManager.current
    val periods = remember { listOf("7 Hari" to "7 hari", "30 Hari" to "30 hari", "3 Bulan" to "3 bulan") }
    var showOutletFilterMenu by rememberSaveable { mutableStateOf(false) }
    var showDatePicker by rememberSaveable { mutableStateOf(false) }

    val filteredReports = remember(
        uiState.reportListItems,
        uiState.reportSearchQuery,
        uiState.reportOutletFilterId,
        uiState.reportDateFilter,
        uiState.reportSelectedDateMillis,
        uiState.reportReviewFilter
    ) {
        filterDailyReports(
            items = uiState.reportListItems,
            searchQuery = uiState.reportSearchQuery,
            outletFilterId = uiState.reportOutletFilterId,
            dateFilter = uiState.reportDateFilter,
            selectedDateMillis = uiState.reportSelectedDateMillis,
            reviewFilter = uiState.reportReviewFilter
        )
    }

    val reviewFilterChips = remember(uiState.reportListItems) {
        val needsReview = uiState.reportListItems.count { it.hasAlert }
        val rejected = uiState.reportListItems.count { it.reviewStatus == ReportReviewStatus.REJECTED }
        val approved = uiState.reportListItems.count { it.reviewStatus == ReportReviewStatus.APPROVED }
        listOf(
            ReportReviewFilter.ALL.name to ReportReviewFilter.ALL.label,
            ReportReviewFilter.NEEDS_REVIEW.name to if (needsReview > 0) "Perlu Ditinjau ($needsReview)" else "Perlu Ditinjau",
            ReportReviewFilter.REJECTED.name to if (rejected > 0) "Ditolak ($rejected)" else "Ditolak",
            ReportReviewFilter.APPROVED.name to if (approved > 0) "Disetujui ($approved)" else "Disetujui"
        )
    }

    val selectedOutletFilterName = remember(uiState.reportOutletFilterId, uiState.outletItems) {
        uiState.outletItems.firstOrNull { it.id == uiState.reportOutletFilterId }?.name ?: "Semua outlet"
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White)
            .pointerInput(Unit) { detectTapGestures(onTap = { focusManager.clearFocus() }) }
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
            .padding(top = 16.dp, bottom = 80.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        SBPageHeader(
            eyebrow = "Owner",
            title = if (uiState.reportTabMode == ReportTabMode.SUMMARY) "Laporan" else "Daftar laporan",
            initials = initials,
            isAvatarClickable = isDevMode,
            onAvatarClick = onProfileClick
        )

        SBSegmentedTabs(
            options = ReportTabMode.entries,
            selected = uiState.reportTabMode,
            labelOf = { it.label },
            onSelected = { onEvent(OwnerUiEvent.ReportTabSelected(it)) }
        )

        if (uiState.reportTabMode == ReportTabMode.SUMMARY) {
            var selectedBarIndex by rememberSaveable(reportSummary.periodLabel, reportSummary.trendBars.size) {
                mutableStateOf<Int?>(null)
            }

            SBSegmentedTabs(
                options = periods.map { it.first },
                selected = periods.firstOrNull {
                    it.first.equals(uiState.selectedReportPeriod, ignoreCase = true)
                }?.first ?: "30 Hari",
                labelOf = { key -> periods.firstOrNull { it.first == key }?.second ?: key },
                onSelected = { onEvent(OwnerUiEvent.ReportPeriodSelected(it)) }
            )

            // Total Omzet Hero
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Total omzet (${reportSummary.periodLabel})",
                        color = SBColor.Grey_600,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                    SBStatusBadge(
                        text = "${reportSummary.reportCount} laporan",
                        containerColor = SBColor.Grey_100,
                        contentColor = SBColor.Grey_600
                    )
                }
                Text(
                    text = formatRupiah(reportSummary.totalRevenue),
                    fontSize = 28.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = SBColor.Grey_700
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    val deltaColor = when (reportSummary.deltaTrend) {
                        1 -> SBColor.Success_600
                        -1 -> SBColor.Primary_550
                        else -> SBColor.Grey_500
                    }
                    if (reportSummary.deltaTrend == 1 || reportSummary.deltaTrend == -1) {
                        Icon(
                            painter = painterResource(id = R.drawable.arrow_up),
                            contentDescription = if (reportSummary.deltaTrend == 1) "Naik" else "Turun",
                            tint = deltaColor,
                            modifier = Modifier
                                .size(12.dp)
                                .rotate(if (reportSummary.deltaTrend == -1) 180f else 0f)
                        )
                    }
                    Text(
                        text = reportSummary.deltaText,
                        color = deltaColor,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            // Tren Penjualan (Data Aktual)
            OutlinedCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.outlinedCardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, SBColor.Grey_100)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text("Tren penjualan", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = SBColor.Grey_700)
                            Text(reportSummary.dateRangeText, fontSize = 12.sp, color = SBColor.Grey_500)
                        }
                        Text(
                            text = reportSummary.maxBarRevenueText,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = SBColor.Grey_500
                        )
                    }

                    val hasAnyRevenue = reportSummary.trendBars.any { it.revenue > 0L }
                    if (!hasAnyRevenue) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(110.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Belum ada transaksi penjualan pada periode ini.",
                                fontSize = 13.sp,
                                color = SBColor.Grey_500
                            )
                        }
                    } else {
                        val activeIdx = selectedBarIndex?.takeIf { it in reportSummary.trendBars.indices }
                            ?: reportSummary.trendBars.indexOfLast { it.isHighlighted }.takeIf { it >= 0 }
                            ?: 0
                        val activeBar = reportSummary.trendBars[activeIdx]

                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp),
                            color = SBColor.Grey_100.copy(alpha = 0.7f)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = activeBar.detailLabel,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = SBColor.Grey_600
                                )
                                Text(
                                    text = formatRupiah(activeBar.revenue),
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = SBColor.Primary_550
                                )
                            }
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(132.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.Bottom
                        ) {
                            reportSummary.trendBars.forEachIndexed { idx, bar ->
                                val isSelected = idx == activeIdx
                                val barColor = if (isSelected) SBColor.Primary_550 else SBColor.Tertiary_100
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Bottom,
                                    modifier = Modifier
                                        .weight(1f)
                                        .fillMaxHeight()
                                        .clip(RoundedCornerShape(8.dp))
                                        .clickable { selectedBarIndex = idx }
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .fillMaxWidth(0.68f),
                                        contentAlignment = Alignment.BottomCenter
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .fillMaxHeight(bar.heightFraction)
                                                .clip(RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp))
                                                .background(barColor)
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = bar.label,
                                        fontSize = 11.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isSelected) SBColor.Primary_550 else SBColor.Grey_500,
                                        maxLines = 1
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Komposisi Pembayaran (Tunai vs QRIS)
            OutlinedCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.outlinedCardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, SBColor.Grey_100)
            ) {
                val cashPct = (reportSummary.cashShareFraction * 100f).toInt()
                val qrisPct = if (reportSummary.totalRevenue > 0L) (100 - cashPct) else 0
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "Komposisi pembayaran",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = SBColor.Grey_700
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(CircleShape)
                            .background(if (reportSummary.totalRevenue > 0L) SBColor.Secondary_200 else SBColor.Grey_100)
                    ) {
                        if (reportSummary.cashShareFraction > 0f) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(reportSummary.cashShareFraction)
                                    .fillMaxHeight()
                                    .clip(CircleShape)
                                    .background(SBColor.Primary_550)
                            )
                        }
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text("Tunai ($cashPct%)", fontSize = 12.sp, color = SBColor.Grey_500)
                            Text(reportSummary.cashSalesText, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = SBColor.Grey_700)
                        }
                        Column(
                            horizontalAlignment = Alignment.End,
                            verticalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            Text("QRIS ($qrisPct%)", fontSize = 12.sp, color = SBColor.Grey_500)
                            Text(reportSummary.qrisSalesText, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = SBColor.Grey_700)
                        }
                    }
                }
            }

            // Ringkasan Bisnis
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Ringkasan bisnis", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = SBColor.Grey_700)
                    TextButton(
                        onClick = onSeeDetailClick,
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)
                    ) {
                        Text("Lihat detail", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = SBColor.Primary_550)
                    }
                }

                OutlinedCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.outlinedCardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, SBColor.Grey_100)
                ) {
                    Column {
                        BusinessSummaryItem(
                            iconRes = R.drawable.ic_store,
                            title = "Total penjualan",
                            subtitle = "${reportSummary.totalProductsSoldText} • ${reportSummary.contributingOutletsCount} outlet",
                            value = reportSummary.totalSalesValueText,
                            valueColor = SBColor.Grey_700
                        )
                        HorizontalDivider(color = SBColor.Grey_100, thickness = 1.dp, modifier = Modifier.padding(horizontal = 16.dp))
                        BusinessSummaryItem(
                            iconRes = R.drawable.ic_wallet,
                            title = "Biaya operasional",
                            subtitle = reportSummary.operationalCostPercentText,
                            value = reportSummary.operationalCostValueText,
                            valueColor = SBColor.Grey_700
                        )
                        HorizontalDivider(color = SBColor.Grey_100, thickness = 1.dp, modifier = Modifier.padding(horizontal = 16.dp))
                        BusinessSummaryItem(
                            iconRes = R.drawable.ic_warning,
                            title = "Kerugian bahan rusak",
                            subtitle = if (reportSummary.damagedTotalUnits > 0) {
                                "${formatDecimal(reportSummary.damagedTotalUnits)} unit bahan rusak"
                            } else {
                                "Tidak ada bahan rusak"
                            },
                            value = reportSummary.damagedLossValueText,
                            valueColor = if (reportSummary.damagedEstimatedLoss > 0L) SBColor.Primary_550 else SBColor.Grey_600
                        )
                        HorizontalDivider(color = SBColor.Grey_100, thickness = 1.dp, modifier = Modifier.padding(horizontal = 16.dp))
                        BusinessSummaryItem(
                            iconRes = R.drawable.ic_bar_chart,
                            title = "Pendapatan bersih",
                            subtitle = "Omzet dikurangi biaya operasional",
                            value = reportSummary.estimatedProfitValueText,
                            valueColor = SBColor.Success_600
                        )
                    }
                }
            }
        } else {
            val hasSelectedDate = uiState.reportSelectedDateMillis != null
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                SBSearchBar(
                    query = uiState.reportSearchQuery,
                    onQueryChange = { onEvent(OwnerUiEvent.ReportSearchChanged(it)) },
                    placeholder = "Cari outlet...",
                    modifier = Modifier.weight(1f)
                )

                Surface(
                    onClick = { showDatePicker = true },
                    modifier = Modifier.size(56.dp),
                    shape = RoundedCornerShape(12.dp),
                    color = if (hasSelectedDate) SBColor.Primary_100 else Color.White,
                    border = BorderStroke(
                        width = 1.dp,
                        color = if (hasSelectedDate) SBColor.Primary_550 else SBColor.Grey_200
                    )
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            painter = painterResource(id = R.drawable.calendar),
                            contentDescription = "Pilih tanggal laporan",
                            tint = if (hasSelectedDate) SBColor.Primary_550 else SBColor.Grey_600,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
            }

            SBFilterChipRow(
                options = reviewFilterChips,
                selectedKey = uiState.reportReviewFilter.name,
                onSelected = { key ->
                    val filter = ReportReviewFilter.entries.firstOrNull { it.name == key } ?: ReportReviewFilter.ALL
                    onEvent(OwnerUiEvent.ReportReviewFilterSelected(filter))
                }
            )

            SBFilterChipRow(
                options = listOf("Semua", "Hari ini", "7 Hari", "30 Hari", "3 Bulan").map { it to it },
                selectedKey = if (hasSelectedDate) "" else uiState.reportDateFilter,
                onSelected = { onEvent(OwnerUiEvent.ReportDateFilterSelected(it)) }
            )

            uiState.reportSelectedDateMillis?.let { selectedMillis ->
                val selectedDateLabel = remember(selectedMillis) { formatUtcDateMillis(selectedMillis) }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    FilterChip(
                        selected = true,
                        onClick = { onEvent(OwnerUiEvent.ReportSpecificDateSelected(null)) },
                        label = {
                            Text(
                                text = "Tanggal: $selectedDateLabel  ✕",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        },
                        shape = RoundedCornerShape(9.dp),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = SBColor.Primary_100,
                            selectedLabelColor = SBColor.Primary_550
                        ),
                        border = BorderStroke(1.dp, SBColor.Primary_200)
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("${filteredReports.size} laporan ditemukan", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = SBColor.Grey_700)

                Box {
                    TextButton(
                        onClick = { showOutletFilterMenu = true },
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)
                    ) {
                        Text("$selectedOutletFilterName ▾", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = SBColor.Primary_550)
                    }

                    DropdownMenu(
                        expanded = showOutletFilterMenu,
                        onDismissRequest = { showOutletFilterMenu = false },
                        modifier = Modifier.width(210.dp).background(Color.White),
                        shape = RoundedCornerShape(14.dp),
                        containerColor = Color.White
                    ) {
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = "Semua outlet",
                                    fontSize = 14.sp,
                                    fontWeight = if (uiState.reportOutletFilterId == null) FontWeight.Bold else FontWeight.Medium,
                                    color = if (uiState.reportOutletFilterId == null) SBColor.Primary_550 else SBColor.Grey_700
                                )
                            },
                            onClick = {
                                onEvent(OwnerUiEvent.ReportOutletFilterSelected(null))
                                showOutletFilterMenu = false
                            }
                        )
                        uiState.outletItems.forEach { outlet ->
                            val isChosen = uiState.reportOutletFilterId == outlet.id
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = outlet.displayName,
                                        fontSize = 14.sp,
                                        fontWeight = if (isChosen) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isChosen) SBColor.Primary_550 else SBColor.Grey_700
                                    )
                                },
                                onClick = {
                                    onEvent(OwnerUiEvent.ReportOutletFilterSelected(outlet.id))
                                    showOutletFilterMenu = false
                                }
                            )
                        }
                    }
                }
            }

            if (filteredReports.isEmpty()) {
                OutlinedCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.outlinedCardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, SBColor.Grey_100)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Tidak ada laporan yang cocok dengan filter saat ini.", fontSize = 14.sp, color = SBColor.Grey_500)
                    }
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    filteredReports.forEach { item ->
                        DailyReportListCard(item = item, onClick = { onReportItemClick(item.id) })
                    }
                }
            }
        }
    }

    if (showDatePicker) {
        SBDatePickerDialog(
            initialSelectedDateMillis = uiState.reportSelectedDateMillis,
            onDismiss = { showDatePicker = false },
            onConfirm = { dateMillis ->
                onEvent(OwnerUiEvent.ReportSpecificDateSelected(dateMillis))
                showDatePicker = false
            },
            onReset = {
                onEvent(OwnerUiEvent.ReportSpecificDateSelected(null))
                showDatePicker = false
            }
        )
    }
}

@Composable
private fun DailyReportListCard(
    item: DailyReportListItemUi,
    onClick: () -> Unit
) {
    val isRejected = item.reviewStatus == ReportReviewStatus.REJECTED
    val isApproved = item.reviewStatus == ReportReviewStatus.APPROVED
    val borderColor = when {
        isRejected -> SBColor.Primary_200
        item.hasAlert -> SBColor.Secondary_200
        else -> SBColor.Grey_100
    }
    val iconBg = when {
        isRejected -> SBColor.Error_100
        item.hasAlert -> SBColor.Secondary_100
        isApproved -> SBColor.Success_100
        else -> SBColor.Primary_100
    }
    val iconTint = when {
        isRejected -> SBColor.Primary_550
        item.hasAlert -> SBColor.Secondary_500
        isApproved -> SBColor.Success_600
        else -> SBColor.Primary_550
    }
    val iconRes = when {
        isRejected || item.hasAlert -> R.drawable.ic_warning
        isApproved -> R.drawable.ic_check
        else -> R.drawable.ic_bar_chart
    }

    OutlinedCard(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.outlinedCardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, borderColor)
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
                Row(
                    modifier = Modifier.weight(1f).padding(end = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        modifier = Modifier.size(40.dp),
                        shape = RoundedCornerShape(10.dp),
                        color = iconBg
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                painter = painterResource(iconRes),
                                contentDescription = null,
                                tint = iconTint,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(item.outletName, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = SBColor.Grey_700)
                        Text(item.dateLabel, fontSize = 12.sp, color = SBColor.Grey_500)
                    }
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    when {
                        isApproved -> SBStatusBadge(
                            text = "Disetujui",
                            containerColor = SBColor.Success_100,
                            contentColor = SBColor.Success_600
                        )
                        isRejected -> SBStatusBadge(
                            text = "Ditolak",
                            containerColor = SBColor.Error_100,
                            contentColor = SBColor.Primary_550
                        )
                        item.hasAlert -> SBStatusBadge(
                            text = "Perlu ditinjau",
                            containerColor = SBColor.Warning_100,
                            contentColor = SBColor.Warning_600
                        )
                    }
                    Icon(
                        painter = painterResource(id = R.drawable.ic_chevron_right),
                        contentDescription = "Lihat detail laporan",
                        tint = SBColor.Grey_400,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            if (isRejected && !item.reviewNote.isNullOrBlank()) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    color = SBColor.Error_100.copy(alpha = 0.65f)
                ) {
                    Text(
                        text = "Alasan tolak: ${item.reviewNote}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = SBColor.Primary_550,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }
            } else if (item.hasAlert && !item.anomalyReasonText.isNullOrBlank()) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    color = SBColor.Warning_100.copy(alpha = 0.7f)
                ) {
                    Text(
                        text = item.anomalyReasonText,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = SBColor.Warning_600,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
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
                    Text(formatRupiah(item.totalRevenue), fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, color = SBColor.Grey_700)
                }
                Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text("${item.totalSold} pcs terjual", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = SBColor.Grey_600)
                    Text(
                        text = if (item.damagedCount > 0) {
                            "Rusak ${item.damagedCount} • Biaya ${formatRupiah(item.totalExpense)}"
                        } else {
                            "Tunai ${formatRupiah(item.cashRevenue)}"
                        },
                        fontSize = 11.sp,
                        color = if (item.hasAlert || isRejected) SBColor.Primary_550 else SBColor.Grey_500
                    )
                }
            }
        }
    }
}

@Composable
private fun BusinessSummaryItem(
    iconRes: Int,
    title: String,
    subtitle: String,
    value: String,
    valueColor: Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
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

internal fun defaultReportPeriodSummary(): ReportPeriodSummaryUi = ReportPeriodSummaryUi(
    periodLabel = "30 hari",
    dateRangeText = "30 hari terakhir"
)
