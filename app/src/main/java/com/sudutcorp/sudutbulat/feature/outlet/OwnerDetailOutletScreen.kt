package com.sudutcorp.sudutbulat.feature.outlet

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
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
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.sudutcorp.sudutbulat.R
import com.sudutcorp.sudutbulat.core.components.SBConfirmDialog
import com.sudutcorp.sudutbulat.core.components.SBDottedDivider
import com.sudutcorp.sudutbulat.core.components.SBKeyValueRow
import com.sudutcorp.sudutbulat.core.components.SBMetricCard
import com.sudutcorp.sudutbulat.core.components.SBSectionCard
import com.sudutcorp.sudutbulat.core.components.SBStatusBadge
import com.sudutcorp.sudutbulat.core.components.SBSubHeader
import com.sudutcorp.sudutbulat.core.ui.theme.SBColor
import com.sudutcorp.sudutbulat.core.util.formatRupiah
import com.sudutcorp.sudutbulat.data.model.Role
import com.sudutcorp.sudutbulat.feature.owner.DailyReportListItemUi
import com.sudutcorp.sudutbulat.feature.owner.OutletPerformanceUi
import com.sudutcorp.sudutbulat.feature.owner.OwnerUiEvent
import com.sudutcorp.sudutbulat.feature.owner.OwnerUiState
import com.sudutcorp.sudutbulat.feature.owner.StaffItemUi

@Composable
fun OwnerDetailOutletScreen(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    outletId: String = "outlet_sudirman",
    uiState: OwnerUiState = OwnerUiState(),
    onEvent: (OwnerUiEvent) -> Unit = {},
    onReportClick: (String) -> Unit = {}
) {
    val outlet = uiState.outletItems.firstOrNull { it.id == outletId }
        ?: uiState.outletItems.firstOrNull()
    val allOutletStaff = remember(uiState.staffItems) {
        uiState.staffItems.filter { it.role == Role.OUTLET && it.isActive }
    }
    val recentOutletReports = remember(uiState.reportListItems, outlet?.id) {
        val targetId = outlet?.id ?: outletId
        uiState.reportListItems.filter { it.outletId == targetId }.take(3)
    }

    OwnerDetailOutletContent(
        modifier = modifier,
        outlet = outlet,
        allOutletStaff = allOutletStaff,
        recentReports = recentOutletReports,
        onBackClick = { navController.popBackStack() },
        onToggleOpenStatus = {
            if (outlet != null) onEvent(OwnerUiEvent.ToggleOutletOpenStatus(outlet.id))
        },
        onSaveStaffAssignment = { selectedIds ->
            if (outlet != null) onEvent(OwnerUiEvent.AssignStaffToOutlet(outlet.id, selectedIds))
        },
        onReportClick = onReportClick
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OwnerDetailOutletContent(
    modifier: Modifier = Modifier,
    outlet: OutletPerformanceUi? = null,
    allOutletStaff: List<StaffItemUi> = emptyList(),
    recentReports: List<DailyReportListItemUi> = emptyList(),
    onBackClick: () -> Unit = {},
    onToggleOpenStatus: () -> Unit = {},
    onSaveStaffAssignment: (Set<String>) -> Unit = {},
    onReportClick: (String) -> Unit = {}
) {
    val outletName = outlet?.name ?: "Sudirman"
    val outletAddress = outlet?.address ?: "Jl. Jenderal Sudirman No. 18"
    val coordinateLabel = outlet?.coordinateLabel ?: "-6.2146, 106.8222"
    val isOpen = outlet?.isOpen ?: true
    val targetProgress = outlet?.targetProgress ?: 0.85f
    val operatingHours = "${outlet?.openTime ?: "09.00"} – ${outlet?.closeTime ?: "21.00"} WIB"
    val staffDisplay = outlet?.staffNames?.joinToString(", ")?.takeIf { it.isNotBlank() } ?: "Belum ada staf"

    var showOverrideStatusDialog by rememberSaveable { mutableStateOf(false) }
    var showStaffPickerSheet by rememberSaveable { mutableStateOf(false) }
    var draftSelectedStaffIds by remember(outlet?.id, outlet?.staffIds) {
        mutableStateOf(outlet?.staffIds?.toSet() ?: emptySet())
    }
    val staffSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White)
            .statusBarsPadding()
            .navigationBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
            .padding(top = 16.dp, bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        SBSubHeader(
            eyebrow = "Detail outlet",
            title = "Outlet $outletName",
            onBackClick = onBackClick
        )

        // Card Identitas Outlet
        OutlinedCard(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.outlinedCardColors(containerColor = Color.White),
            border = BorderStroke(1.dp, SBColor.Grey_100)
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
                            modifier = Modifier.size(44.dp),
                            shape = RoundedCornerShape(12.dp),
                            color = SBColor.Primary_100
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_store),
                                    contentDescription = "Outlet",
                                    tint = SBColor.Primary_550,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }

                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text("Gerobak menetap", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = SBColor.Primary_550)
                            Text("Outlet $outletName", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = SBColor.Grey_700)
                            Text(outletAddress, fontSize = 12.sp, color = SBColor.Grey_500)
                        }
                    }

                    SBStatusBadge(
                        text = if (isOpen) "Buka ▾" else "Tutup ▾",
                        containerColor = if (isOpen) SBColor.Success_100 else SBColor.Grey_100,
                        contentColor = if (isOpen) SBColor.Success_600 else SBColor.Grey_600,
                        onClick = { showOverrideStatusDialog = true }
                    )
                }

                HorizontalDivider(thickness = 1.dp, color = SBColor.Grey_100)

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Koordinat lokasi", fontSize = 12.sp, color = SBColor.Grey_500)
                    Text(coordinateLabel, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = SBColor.Grey_600)
                }
            }
        }

        // Grid 2x2 Metric Cards
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                SBMetricCard(
                    modifier = Modifier.weight(1f),
                    iconRes = R.drawable.ic_store,
                    iconBgColor = SBColor.Success_100,
                    iconTint = SBColor.Success_600,
                    title = "Penjualan",
                    value = outlet?.let { formatRupiah(it.revenue) } ?: "Rp2.480.000",
                    subtitle = "${outlet?.piecesSold ?: 496} pcs terjual"
                )
                SBMetricCard(
                    modifier = Modifier.weight(1f),
                    iconRes = R.drawable.ic_wallet,
                    iconBgColor = SBColor.Secondary_100,
                    iconTint = SBColor.Secondary_500,
                    title = "Pengeluaran",
                    value = outlet?.let { formatRupiah(it.expenseTotal) } ?: "Rp185.000",
                    subtitle = "${outlet?.expenseCount ?: 3} transaksi"
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedCard(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.outlinedCardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, SBColor.Grey_100)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Surface(
                            modifier = Modifier.size(40.dp),
                            shape = RoundedCornerShape(10.dp),
                            color = SBColor.Primary_100
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_bar_chart),
                                    contentDescription = "Target harian",
                                    tint = SBColor.Primary_550,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }

                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("Target harian", fontSize = 13.sp, color = SBColor.Grey_500)
                            Text("${(targetProgress * 100).toInt()}%", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = SBColor.Grey_700)
                            LinearProgressIndicator(
                                progress = { targetProgress },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(5.dp)
                                    .clip(CircleShape),
                                color = SBColor.Primary,
                                trackColor = SBColor.Grey_100,
                                strokeCap = StrokeCap.Round,
                                gapSize = 0.dp,
                                drawStopIndicator = {}
                            )
                            Text(
                                text = "Target ${formatRupiah(outlet?.dailyTarget ?: 2_900_000L)}",
                                fontSize = 11.sp,
                                color = SBColor.Grey_400
                            )
                        }
                    }
                }

                SBMetricCard(
                    modifier = Modifier.weight(1f),
                    iconRes = R.drawable.ic_warning,
                    iconBgColor = SBColor.Secondary_100,
                    iconTint = SBColor.Secondary_500,
                    title = "Bahan rusak",
                    value = "${outlet?.damagedCount ?: 12} item",
                    subtitle = "${outlet?.damagedReportCount ?: 1} laporan"
                )
            }
        }

        // Operasional & Keuangan
        SBSectionCard(
            title = "Operasional & keuangan",
            actionText = "+ Pilih staf",
            onActionClick = {
                draftSelectedStaffIds = outlet?.staffIds?.toSet() ?: emptySet()
                showStaffPickerSheet = true
            }
        ) {
            SBKeyValueRow("Jam operasional", operatingHours)
            SBKeyValueRow("Staf bertugas", staffDisplay)
            HorizontalDivider(thickness = 1.dp, color = SBColor.Grey_100)
            SBKeyValueRow("Penjualan tunai", outlet?.let { formatRupiah(it.cashRevenue) } ?: "Rp1.480.000")
            SBKeyValueRow("Penjualan QRIS", outlet?.let { formatRupiah(it.qrisRevenue) } ?: "Rp1.000.000")
            SBDottedDivider(modifier = Modifier.padding(vertical = 2.dp))
            SBKeyValueRow(
                label = "Setoran bersih (est.)",
                value = outlet?.let { formatRupiah(it.netDepositEstimate) } ?: "Rp2.295.000",
                isBold = true,
                valueColor = SBColor.Success_600,
                valueFontSize = 16.sp
            )
        }

        // Laporan Harian Terbaru
        if (recentReports.isNotEmpty()) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Laporan harian terbaru", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = SBColor.Grey_700)
                OutlinedCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.outlinedCardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, SBColor.Grey_100)
                ) {
                    Column {
                        recentReports.forEachIndexed { index, report ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onReportClick(report.id) }
                                    .padding(horizontal = 16.dp, vertical = 14.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                    Text(report.dateLabel, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = SBColor.Grey_700)
                                    Text("${report.totalSold} pcs terjual", fontSize = 12.sp, color = SBColor.Grey_500)
                                }
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(formatRupiah(report.totalRevenue), fontSize = 14.sp, fontWeight = FontWeight.Bold, color = SBColor.Grey_700)
                                    Icon(
                                        painter = painterResource(id = R.drawable.ic_chevron_right),
                                        contentDescription = null,
                                        tint = SBColor.Grey_400,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                            if (index < recentReports.lastIndex) {
                                HorizontalDivider(thickness = 1.dp, color = SBColor.Grey_100)
                            }
                        }
                    }
                }
            }
        }
    }

    if (showOverrideStatusDialog) {
        val targetStatusLabel = if (isOpen) "Tutup" else "Buka"
        SBConfirmDialog(
            title = "Override status outlet?",
            description = "Status buka/tutup normalnya diubah oleh Staf Outlet. Yakin ingin mengubah status Outlet $outletName menjadi $targetStatusLabel?",
            confirmText = "Ubah",
            iconRes = R.drawable.ic_warning,
            onDismiss = { showOverrideStatusDialog = false },
            onConfirm = {
                showOverrideStatusDialog = false
                onToggleOpenStatus()
            }
        )
    }

    if (showStaffPickerSheet) {
        ModalBottomSheet(
            onDismissRequest = { showStaffPickerSheet = false },
            sheetState = staffSheetState,
            containerColor = Color.White,
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(horizontal = 24.dp)
                    .padding(bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text("Pilih staf untuk $outletName", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = SBColor.Grey_700)
                Text("Centang staf outlet yang bertugas di lokasi ini.", fontSize = 13.sp, color = SBColor.Grey_500)

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 360.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(
                        items = allOutletStaff,
                        key = { it.id },
                        contentType = { "outlet_staff_picker" }
                    ) { staff ->
                        val isChecked = staff.id in draftSelectedStaffIds
                        val otherOutletHint = when {
                            staff.outletId != null && staff.outletId != outlet?.id ->
                                "Saat ini di ${staff.outletName ?: staff.outletId}"
                            staff.outletId == outlet?.id -> "Bertugas di outlet ini"
                            else -> "Belum ditugaskan"
                        }
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    draftSelectedStaffIds = if (isChecked) {
                                        draftSelectedStaffIds - staff.id
                                    } else {
                                        draftSelectedStaffIds + staff.id
                                    }
                                }
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(2.dp)
                            ) {
                                Text(staff.name, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = SBColor.Grey_700)
                                Text(otherOutletHint, fontSize = 12.sp, color = SBColor.Grey_500)
                            }
                            Checkbox(
                                checked = isChecked,
                                onCheckedChange = { checked ->
                                    draftSelectedStaffIds = if (checked) {
                                        draftSelectedStaffIds + staff.id
                                    } else {
                                        draftSelectedStaffIds - staff.id
                                    }
                                },
                                colors = CheckboxDefaults.colors(checkedColor = SBColor.Primary_550)
                            )
                        }
                    }
                }

                Button(
                    onClick = {
                        onSaveStaffAssignment(draftSelectedStaffIds)
                        showStaffPickerSheet = false
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = SBColor.Primary_550)
                ) {
                    Text("Simpan penugasan", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
