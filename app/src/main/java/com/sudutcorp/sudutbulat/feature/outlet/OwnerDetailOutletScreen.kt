package com.sudutcorp.sudutbulat.feature.outlet

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.navigation.NavHostController
import com.sudutcorp.sudutbulat.R
import com.sudutcorp.sudutbulat.core.ui.theme.SBColor
import com.sudutcorp.sudutbulat.data.model.Role
import com.sudutcorp.sudutbulat.feature.owner.OutletPerformanceUi
import com.sudutcorp.sudutbulat.feature.owner.OwnerUiEvent
import com.sudutcorp.sudutbulat.feature.owner.OwnerUiState
import com.sudutcorp.sudutbulat.feature.owner.StaffItemUi
import com.sudutcorp.sudutbulat.feature.owner.formatRupiah

@Composable
fun OwnerDetailOutletScreen(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    outletId: String = "outlet_sudirman",
    uiState: OwnerUiState = OwnerUiState(),
    onEvent: (OwnerUiEvent) -> Unit = {}
) {
    val outlet = uiState.outletItems.firstOrNull { it.id == outletId }
        ?: uiState.outletItems.firstOrNull()
    val allOutletStaff = remember(uiState.staffItems) {
        uiState.staffItems.filter { it.role == Role.OUTLET && it.isActive }
    }

    OwnerDetailOutletContent(
        modifier = modifier,
        outlet = outlet,
        allOutletStaff = allOutletStaff,
        onBackClick = { navController.popBackStack() },
        onToggleOpenStatus = {
            if (outlet != null) {
                onEvent(OwnerUiEvent.ToggleOutletOpenStatus(outlet.id))
            }
        },
        onSaveStaffAssignment = { selectedIds ->
            if (outlet != null) {
                onEvent(OwnerUiEvent.AssignStaffToOutlet(outlet.id, selectedIds))
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OwnerDetailOutletContent(
    modifier: Modifier = Modifier,
    outlet: OutletPerformanceUi? = null,
    allOutletStaff: List<StaffItemUi> = emptyList(),
    onBackClick: () -> Unit = {},
    onToggleOpenStatus: () -> Unit = {},
    onSaveStaffAssignment: (Set<String>) -> Unit = {}
) {
    val outletName = outlet?.name ?: "Sudirman"
    val outletAddress = outlet?.address ?: "Jl. Jenderal Sudirman No. 18"
    val isOpen = outlet?.isOpen ?: true
    val salesText = outlet?.let { formatRupiah(it.revenue) } ?: "Rp2.480.000"
    val piecesSoldText = "${outlet?.piecesSold ?: 496} pcs hari ini"
    val expenseText = outlet?.let { formatRupiah(it.expenseTotal) } ?: "Rp185.000"
    val expenseCountText = "${outlet?.expenseCount ?: 3} transaksi"
    val damagedText = "${outlet?.damagedCount ?: 12} pcs"
    val damagedReportText = "${outlet?.damagedReportCount ?: 1} laporan"
    val operatingHours = "${outlet?.openTime ?: "09.00"} – ${outlet?.closeTime ?: "21.00"}"
    val staffDisplay = outlet?.staffNames?.joinToString(", ")?.takeIf { it.isNotBlank() } ?: "Belum ada staf"
    val netDepositText = outlet?.let { formatRupiah(it.netDepositEstimate) } ?: "Rp1.675.000"

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
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            IconButton(
                onClick = onBackClick,
                modifier = Modifier
                    .size(44.dp)
                    .background(color = SBColor.Grey_100, shape = CircleShape)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_chevron_left),
                    contentDescription = "Kembali",
                    tint = SBColor.Grey_700,
                    modifier = Modifier.size(24.dp)
                )
            }
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = "Outlet",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = SBColor.Grey_500
                )
                Text(
                    text = outletName,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = SBColor.Grey_700
                )
            }
        }

        // Card Outlet Ditugaskan
        OutlinedCard(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.outlinedCardColors(containerColor = Color.White),
            border = BorderStroke(1.dp, SBColor.Grey_100)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
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
                        color = SBColor.Primary_100
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_store),
                                contentDescription = "Outlet",
                                tint = SBColor.Primary_550,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(
                            text = "Outlet ditugaskan",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Normal,
                            color = SBColor.Grey_500
                        )
                        Text(
                            text = outletName,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = SBColor.Grey_700
                        )
                        Text(
                            text = outletAddress,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Normal,
                            color = SBColor.Grey_500
                        )
                    }
                }

                Surface(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .clickable { showOverrideStatusDialog = true },
                    shape = RoundedCornerShape(6.dp),
                    color = if (isOpen) SBColor.Success_100 else SBColor.Grey_100
                ) {
                    Text(
                        text = if (isOpen) "Buka" else "Tutup",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isOpen) SBColor.Success_600 else SBColor.Grey_600,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }
        }

        // Grid 2x2 Metric Cards
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                MetricCard(
                    modifier = Modifier.weight(1f),
                    iconRes = R.drawable.ic_store,
                    iconBgColor = SBColor.Success_100,
                    iconTint = SBColor.Success_600,
                    title = "Penjualan",
                    value = salesText,
                    subtitle = piecesSoldText
                )

                MetricCard(
                    modifier = Modifier.weight(1f),
                    iconRes = R.drawable.ic_wallet,
                    iconBgColor = SBColor.Secondary_100,
                    iconTint = SBColor.Secondary_500,
                    title = "Pengeluaran",
                    value = expenseText,
                    subtitle = expenseCountText
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                MetricCard(
                    modifier = Modifier.weight(1f),
                    iconRes = R.drawable.ic_box,
                    iconBgColor = SBColor.Success_100,
                    iconTint = SBColor.Success_600,
                    title = "Stok saat ini",
                    value = "878 pcs",
                    subtitle = "Tahu Original"
                )

                MetricCard(
                    modifier = Modifier.weight(1f),
                    iconRes = R.drawable.ic_warning,
                    iconBgColor = SBColor.Secondary_100,
                    iconTint = SBColor.Secondary_500,
                    title = "Stok rusak",
                    value = damagedText,
                    subtitle = damagedReportText
                )
            }
        }

        // Section: Status Outlet
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Status outlet",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = SBColor.Grey_700
                )
                TextButton(
                    onClick = {
                        draftSelectedStaffIds = outlet?.staffIds?.toSet() ?: emptySet()
                        showStaffPickerSheet = true
                    },
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)
                ) {
                    Text(
                        text = "+ Pilih staf",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = SBColor.Primary_550
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Jam operasional",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Normal,
                    color = SBColor.Grey_600
                )
                Text(
                    text = operatingHours,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = SBColor.Grey_700
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Staf bertugas",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Normal,
                    color = SBColor.Grey_600
                )
                Text(
                    text = staffDisplay,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = SBColor.Grey_700,
                    textAlign = TextAlign.End,
                    modifier = Modifier.weight(1f).padding(start = 16.dp)
                )
            }

            DottedLine(
                color = SBColor.Grey_200,
                modifier = Modifier.padding(vertical = 4.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Setoran bersih (est.)",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = SBColor.Grey_700
                )
                Text(
                    text = netDepositText,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = SBColor.Grey_700
                )
            }
        }
    }

    if (showOverrideStatusDialog) {
        val targetStatusLabel = if (isOpen) "Tutup" else "Buka"
        OverrideStatusConfirmDialog(
            outletName = outletName,
            targetStatusLabel = targetStatusLabel,
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
                Text(
                    text = "Pilih staf untuk $outletName",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = SBColor.Grey_700
                )
                Text(
                    text = "Centang staf outlet yang bertugas di lokasi ini.",
                    fontSize = 13.sp,
                    color = SBColor.Grey_500
                )

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
                        val otherOutletHint = if (staff.outletId != null && staff.outletId != outlet?.id) {
                            "Saat ini di ${staff.outletName ?: staff.outletId}"
                        } else if (staff.outletId == outlet?.id) {
                            "Bertugas di outlet ini"
                        } else {
                            "Belum ditugaskan"
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
                                Text(
                                    text = staff.name,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = SBColor.Grey_700
                                )
                                Text(
                                    text = otherOutletHint,
                                    fontSize = 12.sp,
                                    color = SBColor.Grey_500
                                )
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

@Composable
private fun OverrideStatusConfirmDialog(
    outletName: String,
    targetStatusLabel: String,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            color = Color.White
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Surface(
                    modifier = Modifier.size(56.dp),
                    shape = CircleShape,
                    color = SBColor.Secondary_100
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_warning),
                            contentDescription = "Override Status",
                            tint = SBColor.Secondary_500,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }

                Text(
                    text = "Override status outlet?",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = SBColor.Grey_700,
                    textAlign = TextAlign.Center
                )

                Text(
                    text = "Status buka/tutup normalnya diubah oleh Staf Outlet. Yakin ingin mengubah status Outlet $outletName menjadi $targetStatusLabel?",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Normal,
                    color = SBColor.Grey_500,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = onDismiss,
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = SBColor.Grey_100,
                            contentColor = SBColor.Grey_700
                        )
                    ) {
                        Text(
                            text = "Batal",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Button(
                        onClick = onConfirm,
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = SBColor.Primary_550,
                            contentColor = Color.White
                        )
                    ) {
                        Text(
                            text = "Ubah",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MetricCard(
    modifier: Modifier = Modifier,
    iconRes: Int,
    iconBgColor: Color,
    iconTint: Color,
    title: String,
    value: String,
    subtitle: String
) {
    OutlinedCard(
        modifier = modifier,
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
                color = iconBgColor
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        painter = painterResource(id = iconRes),
                        contentDescription = title,
                        tint = iconTint,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = title,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Normal,
                    color = SBColor.Grey_500
                )
                Text(
                    text = value,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = SBColor.Grey_700
                )
                Text(
                    text = subtitle,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Normal,
                    color = SBColor.Grey_400
                )
            }
        }
    }
}

@Composable
private fun DottedLine(
    modifier: Modifier = Modifier,
    color: Color = SBColor.Grey_200
) {
    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(1.dp)
    ) {
        val pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 12f), 0f)
        drawLine(
            color = color,
            start = Offset(0f, 0f),
            end = Offset(size.width, 0f),
            strokeWidth = 1.dp.toPx(),
            pathEffect = pathEffect
        )
    }
}

@Preview(showBackground = true)
@Composable
fun OwnerDetailOutletScreenPreview() {
    OwnerDetailOutletContent()
}
