package com.sudutcorp.sudutbulat.feature.report

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DisplayMode
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
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
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import androidx.navigation.NavHostController
import com.sudutcorp.sudutbulat.R
import com.sudutcorp.sudutbulat.core.components.SBFilterChipRow
import com.sudutcorp.sudutbulat.core.components.SBPageHeader
import com.sudutcorp.sudutbulat.core.components.SBSearchBar
import com.sudutcorp.sudutbulat.core.components.SBSegmentedTabs
import com.sudutcorp.sudutbulat.core.ui.theme.SBColor
import com.sudutcorp.sudutbulat.core.util.formatRupiah
import com.sudutcorp.sudutbulat.feature.owner.DailyReportListItemUi
import com.sudutcorp.sudutbulat.feature.owner.DamagedSummaryItemUi
import com.sudutcorp.sudutbulat.feature.owner.OwnerUiEvent
import com.sudutcorp.sudutbulat.feature.owner.OwnerUiState
import com.sudutcorp.sudutbulat.feature.owner.ReportPeriodSummaryUi
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
        uiState.reportSelectedDateMillis
    ) {
        filterDailyReports(
            items = uiState.reportListItems,
            searchQuery = uiState.reportSearchQuery,
            outletFilterId = uiState.reportOutletFilterId,
            dateFilter = uiState.reportDateFilter,
            selectedDateMillis = uiState.reportSelectedDateMillis
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
            SBSegmentedTabs(
                options = periods.map { it.first },
                selected = periods.firstOrNull {
                    it.first.equals(uiState.selectedReportPeriod, ignoreCase = true)
                }?.first ?: "30 Hari",
                labelOf = { key -> periods.firstOrNull { it.first == key }?.second ?: key },
                onSelected = { onEvent(OwnerUiEvent.ReportPeriodSelected(it)) }
            )

            // Omzet
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("Omzet bersih", color = SBColor.Grey_600, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                Text(reportSummary.netRevenueText, fontSize = 28.sp, fontWeight = FontWeight.ExtraBold, color = SBColor.Grey_700)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.arrow_up),
                        contentDescription = "Naik",
                        tint = SBColor.Success_600,
                        modifier = Modifier.size(12.dp)
                    )
                    Text(reportSummary.deltaText, color = SBColor.Success_600, fontSize = 12.sp, fontWeight = FontWeight.Bold)
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
                            Text("Tren penjualan", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = SBColor.Grey_700)
                            Text(reportSummary.dateRangeText, fontSize = 12.sp, color = SBColor.Grey_500)
                        }
                        Text("Rp", fontSize = 12.sp, fontWeight = FontWeight.Medium, color = SBColor.Grey_500)
                    }

                    val barData = listOf(
                        "S" to 0.45f, "S" to 0.55f, "R" to 0.50f,
                        "K" to 0.65f, "J" to 0.60f, "S" to 0.72f, "M" to 0.85f
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(140.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        barData.forEach { (day, heightFraction) ->
                            val barColor = if (day == "M") SBColor.Primary_550 else SBColor.Tertiary_100
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
                                Text(day, fontSize = 11.sp, fontWeight = FontWeight.Medium, color = SBColor.Grey_500)
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
                            subtitle = reportSummary.totalProductsSoldText,
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
        ReportDatePickerDialog(
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ReportDatePickerDialog(
    initialSelectedDateMillis: Long?,
    onDismiss: () -> Unit,
    onConfirm: (Long?) -> Unit,
    onReset: () -> Unit
) {
    val datePickerState = rememberDatePickerState(initialSelectedDateMillis = initialSelectedDateMillis)
    val dateFormatter = remember { DatePickerDefaults.dateFormatter() }
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val pickerColors = DatePickerDefaults.colors(
        containerColor = Color.White,
        selectedDayContainerColor = SBColor.Primary_550,
        selectedDayContentColor = Color.White,
        todayContentColor = SBColor.Primary_550,
        todayDateBorderColor = SBColor.Primary_550
    )

    LaunchedEffect(datePickerState.displayMode) {
        if (datePickerState.displayMode == DisplayMode.Picker) {
            focusManager.clearFocus(force = true)
            keyboardController?.hide()
        }
    }

    DatePickerDialog(
        onDismissRequest = onDismiss,
        // Prevent Android's Dialog window from resizing frame-by-frame while the IME animates closed.
        modifier = if (datePickerState.displayMode == DisplayMode.Input) {
            Modifier.padding(bottom = 96.dp)
        } else {
            Modifier
        },
        colors = pickerColors,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false
        ),
        confirmButton = {
            TextButton(onClick = { onConfirm(datePickerState.selectedDateMillis) }) {
                Text("Pilih", fontWeight = FontWeight.Bold, color = SBColor.Primary_550)
            }
        },
        dismissButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                if (initialSelectedDateMillis != null) {
                    TextButton(onClick = onReset) {
                        Text("Reset", fontWeight = FontWeight.SemiBold, color = SBColor.Grey_600)
                    }
                }
                TextButton(onClick = onDismiss) {
                    Text("Batal", fontWeight = FontWeight.SemiBold, color = SBColor.Grey_600)
                }
            }
        }
    ) {
        // Bypass M3's private AnimatedContent + SizeTransform to prevent choppy dialog resizing on mode switch.
        key(datePickerState.displayMode) {
            DatePicker(
                state = datePickerState,
                dateFormatter = dateFormatter,
                colors = pickerColors
            )
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
                                painter = painterResource(if (item.hasAlert) R.drawable.ic_warning else R.drawable.ic_bar_chart),
                                contentDescription = null,
                                tint = if (item.hasAlert) SBColor.Secondary_500 else SBColor.Primary_550,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(item.outletName, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = SBColor.Grey_700)
                        Text(item.dateLabel, fontSize = 12.sp, color = SBColor.Grey_500)
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
                Text(title, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = SBColor.Grey_700)
                Text(subtitle, fontSize = 12.sp, color = SBColor.Grey_500)
            }
        }
        Text(value, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = valueColor)
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
