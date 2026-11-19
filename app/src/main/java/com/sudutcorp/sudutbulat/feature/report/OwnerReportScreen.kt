package com.sudutcorp.sudutbulat.feature.report

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.sudutcorp.sudutbulat.R
import com.sudutcorp.sudutbulat.core.ui.theme.SBColor
import com.sudutcorp.sudutbulat.feature.owner.DailyReportListItemUi
import com.sudutcorp.sudutbulat.feature.owner.DamagedSummaryItemUi
import com.sudutcorp.sudutbulat.feature.owner.OwnerUiEvent
import com.sudutcorp.sudutbulat.feature.owner.OwnerUiState
import com.sudutcorp.sudutbulat.feature.owner.ReportPeriodSummaryUi
import com.sudutcorp.sudutbulat.feature.owner.ReportTabMode
import com.sudutcorp.sudutbulat.feature.owner.formatRupiah

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
    val periods = listOf("7 Hari" to "7 hari", "30 Hari" to "30 hari", "3 Bulan" to "3 bulan")
    var showOutletFilterMenu by rememberSaveable { mutableStateOf(false) }

    val filteredReports = remember(
        uiState.reportListItems,
        uiState.reportSearchQuery,
        uiState.reportOutletFilterId,
        uiState.reportDateFilter
    ) {
        val nowMillis = System.currentTimeMillis()
        val dayMillis = 86_400_000L
        val q = uiState.reportSearchQuery.trim().lowercase()
        uiState.reportListItems.filter { item ->
            val matchesOutlet = uiState.reportOutletFilterId.isNullOrBlank() ||
                item.outletId == uiState.reportOutletFilterId
            val ageMillis = (nowMillis - item.dateMillis).coerceAtLeast(0L)
            val matchesDate = when (uiState.reportDateFilter) {
                "Hari ini" -> ageMillis <= dayMillis
                "7 Hari" -> ageMillis <= 7L * dayMillis
                "30 Hari" -> ageMillis <= 30L * dayMillis
                "3 Bulan" -> ageMillis <= 90L * dayMillis
                else -> true
            }
            val matchesQuery = q.isBlank() ||
                item.outletName.lowercase().contains(q) ||
                item.dateLabel.lowercase().contains(q)
            matchesOutlet && matchesDate && matchesQuery
        }
    }

    val selectedOutletFilterName = remember(uiState.reportOutletFilterId, uiState.outletItems) {
        uiState.outletItems.firstOrNull { it.id == uiState.reportOutletFilterId }?.name ?: "Semua outlet"
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White)
            .pointerInput(Unit) {
                detectTapGestures(onTap = { focusManager.clearFocus() })
            }
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
            .padding(top = 16.dp, bottom = 80.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "Owner",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp,
                    color = SBColor.Grey_500
                )
                Text(
                    text = if (uiState.reportTabMode == ReportTabMode.SUMMARY) "Laporan" else "Daftar laporan",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 25.sp,
                    color = SBColor.Grey_700
                )
            }
            BadgedBox(
                badge = {
                    Badge(
                        containerColor = SBColor.Success_500,
                        modifier = Modifier
                            .size(12.dp)
                            .border(2.dp, Color.White, CircleShape)
                    )
                }
            ) {
                Surface(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .clickable(enabled = isDevMode, onClick = onProfileClick),
                    shape = CircleShape,
                    color = SBColor.Secondary_100
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = initials,
                            color = SBColor.Primary_500,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // 2-Tab Segmented Control: Ringkasan | Daftar Laporan
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(color = SBColor.Grey_100, shape = RoundedCornerShape(12.dp))
                .padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ReportTabMode.entries.forEach { mode ->
                val isSelected = uiState.reportTabMode == mode
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(36.dp)
                        .clip(RoundedCornerShape(9.dp))
                        .background(if (isSelected) Color.White else Color.Transparent)
                        .clickable { onEvent(OwnerUiEvent.ReportTabSelected(mode)) },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = mode.label,
                        fontSize = 13.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
                        color = if (isSelected) SBColor.Grey_700 else SBColor.Grey_500
                    )
                }
            }
        }

        if (uiState.reportTabMode == ReportTabMode.SUMMARY) {
            // === TAB 1: RINGKASAN LAPORAN ===
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(color = SBColor.Grey_100, shape = RoundedCornerShape(12.dp))
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                periods.forEach { (periodKey, periodLabel) ->
                    val isSelected = periodKey.equals(uiState.selectedReportPeriod, ignoreCase = true)
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(38.dp)
                            .clip(RoundedCornerShape(9.dp))
                            .background(if (isSelected) Color.White else Color.Transparent)
                            .clickable { onEvent(OwnerUiEvent.ReportPeriodSelected(periodKey)) },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            fontSize = 13.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
                            color = if (isSelected) SBColor.Grey_700 else SBColor.Grey_500,
                            text = periodLabel
                        )
                    }
                }
            }

            // Omzet
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    color = SBColor.Grey_600,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    text = "Omzet bersih"
                )
                Text(
                    fontSize = 28.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = SBColor.Grey_700,
                    text = reportSummary.netRevenueText
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.arrow_up),
                        contentDescription = "Panah Ke Atas",
                        tint = SBColor.Success_600,
                        modifier = Modifier.size(12.dp)
                    )
                    Text(
                        color = SBColor.Success_600,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        text = reportSummary.deltaText
                    )
                }
            }

            // Tren Penjualan
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
                                text = "Tren penjualan",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = SBColor.Grey_700
                            )
                            Text(
                                text = reportSummary.dateRangeText,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Normal,
                                color = SBColor.Grey_500
                            )
                        }
                        Text(
                            text = "Rp",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = SBColor.Grey_500
                        )
                    }

                    val barData = listOf(
                        "S" to 0.45f,
                        "S" to 0.55f,
                        "R" to 0.50f,
                        "K" to 0.65f,
                        "J" to 0.60f,
                        "S" to 0.72f,
                        "M" to 0.85f
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(140.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        barData.forEach { (day, heightFraction) ->
                            val isHighlighted = day == "M"
                            val barColor = if (isHighlighted) SBColor.Primary_550 else SBColor.Tertiary_100

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
                                    fontWeight = FontWeight.Medium,
                                    color = SBColor.Grey_500
                                )
                            }
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
                    Text(
                        text = "Ringkasan bisnis",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = SBColor.Grey_700
                    )
                    TextButton(
                        onClick = onSeeDetailClick,
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)
                    ) {
                        Text(
                            text = "Lihat detail",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = SBColor.Primary_550
                        )
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
                            subtitle = reportSummary.totalProductsSoldText,
                            value = reportSummary.totalSalesValueText,
                            valueColor = SBColor.Grey_700
                        )

                        HorizontalDivider(
                            color = SBColor.Grey_100,
                            thickness = 1.dp,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )

                        BusinessSummaryItem(
                            iconRes = R.drawable.ic_wallet,
                            title = "Biaya operasional",
                            subtitle = reportSummary.operationalCostPercentText,
                            value = reportSummary.operationalCostValueText,
                            valueColor = SBColor.Grey_700
                        )

                        HorizontalDivider(
                            color = SBColor.Grey_100,
                            thickness = 1.dp,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )

                        BusinessSummaryItem(
                            iconRes = R.drawable.ic_bar_chart,
                            title = "Estimasi laba",
                            subtitle = "Setelah biaya operasional",
                            value = reportSummary.estimatedProfitValueText,
                            valueColor = SBColor.Success_600
                        )
                    }
                }
            }
        } else {
            // === TAB 2: DAFTAR LAPORAN (INDEX) ===
            OutlinedTextField(
                value = uiState.reportSearchQuery,
                onValueChange = { onEvent(OwnerUiEvent.ReportSearchChanged(it)) },
                modifier = Modifier.fillMaxWidth(),
                placeholder = {
                    Text(
                        "Cari outlet atau tanggal laporan...",
                        fontSize = 14.sp,
                        color = SBColor.Grey_500
                    )
                },
                leadingIcon = {
                    Icon(
                        painter = painterResource(R.drawable.ic_search),
                        contentDescription = "Cari laporan",
                        tint = SBColor.Grey_500,
                        modifier = Modifier.size(18.dp)
                    )
                },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = SBColor.Primary,
                    unfocusedBorderColor = SBColor.Grey_200,
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White
                )
            )

            // Filter jarak waktu (chips)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("Semua", "Hari ini", "7 Hari", "30 Hari", "3 Bulan").forEach { rangeOpt ->
                    val isSelected = uiState.reportDateFilter == rangeOpt
                    FilterChip(
                        selected = isSelected,
                        onClick = { onEvent(OwnerUiEvent.ReportDateFilterSelected(rangeOpt)) },
                        label = {
                            Text(
                                text = rangeOpt,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        },
                        shape = RoundedCornerShape(9.dp),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = SBColor.Grey_700,
                            selectedLabelColor = Color.White,
                            containerColor = SBColor.Grey_100,
                            labelColor = SBColor.Grey_600
                        ),
                        border = null
                    )
                }
            }

            // Count + Compact Outlet Filter Dropdown
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${filteredReports.size} laporan ditemukan",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = SBColor.Grey_700
                )

                Box {
                    TextButton(
                        onClick = { showOutletFilterMenu = true },
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)
                    ) {
                        Text(
                            text = "$selectedOutletFilterName ▾",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = SBColor.Primary_550
                        )
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
                        Text(
                            text = "Tidak ada laporan yang cocok dengan filter saat ini.",
                            fontSize = 14.sp,
                            color = SBColor.Grey_500
                        )
                    }
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    filteredReports.forEach { item ->
                        DailyReportListCard(
                            item = item,
                            onClick = { onReportItemClick(item.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DailyReportListCard(
    item: DailyReportListItemUi,
    onClick: () -> Unit
) {
    OutlinedCard(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.outlinedCardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, if (item.hasAlert) SBColor.Secondary_200 else SBColor.Grey_100)
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
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        modifier = Modifier.size(40.dp),
                        shape = RoundedCornerShape(10.dp),
                        color = if (item.hasAlert) SBColor.Secondary_100 else SBColor.Primary_100
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                painter = painterResource(
                                    if (item.hasAlert) R.drawable.ic_warning else R.drawable.ic_bar_chart
                                ),
                                contentDescription = null,
                                tint = if (item.hasAlert) SBColor.Secondary_500 else SBColor.Primary_550,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(
                            text = item.outletName,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = SBColor.Grey_700
                        )
                        Text(
                            text = item.dateLabel,
                            fontSize = 12.sp,
                            color = SBColor.Grey_500
                        )
                    }
                }
                Icon(
                    painter = painterResource(id = R.drawable.ic_chevron_right),
                    contentDescription = "Lihat detail laporan",
                    tint = SBColor.Grey_400,
                    modifier = Modifier.size(16.dp)
                )
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
                }
                Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        text = "${item.totalSold} pcs terjual",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = SBColor.Grey_600
                    )
                    Text(
                        text = if (item.damagedCount > 0) {
                            "Rusak ${item.damagedCount} • Biaya ${formatRupiah(item.totalExpense)}"
                        } else {
                            "Tunai ${formatRupiah(item.cashRevenue)}"
                        },
                        fontSize = 11.sp,
                        color = if (item.hasAlert) SBColor.Primary_550 else SBColor.Grey_500
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
                Text(
                    text = title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = SBColor.Grey_700
                )
                Text(
                    text = subtitle,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Normal,
                    color = SBColor.Grey_500
                )
            }
        }
        Text(
            text = value,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = valueColor
        )
    }
}

internal fun defaultReportPeriodSummary(): ReportPeriodSummaryUi = ReportPeriodSummaryUi(
    periodLabel = "30 hari",
    dateRangeText = "24 Apr – 24 Mei",
    netRevenueText = "Rp184.650.000",
    deltaText = "8,7% dibanding periode lalu",
    totalSalesValueText = "Rp184,6jt",
    totalProductsSoldText = "36.930 produk",
    totalProductsPcsText = "36.930 pcs",
    operationalCostValueText = "Rp23,7jt",
    operationalCostPercentText = "12,8% dari omzet",
    estimatedProfitValueText = "Rp62,4jt",
    cashSalesText = "Rp102.340.000",
    qrisSalesText = "Rp82.310.000",
    gasExpenseText = "Rp9.480.000",
    plasticExpenseText = "Rp7.120.000",
    otherExpenseText = "Rp7.100.000",
    totalExpenseText = "Rp23.700.000",
    damagedItems = listOf(
        DamagedSummaryItemUi("mat_tahu_original", "Tahu Original", "86 pcs"),
        DamagedSummaryItemUi("mat_bumbu_balado", "Bumbu", "7 pak")
    )
)

@Preview(showBackground = true)
@Composable
fun OwnerReportScreenPreview() {
    OwnerReportContent()
}
