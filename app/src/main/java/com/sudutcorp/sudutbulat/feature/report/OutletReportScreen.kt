package com.sudutcorp.sudutbulat.feature.report

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.sudutcorp.sudutbulat.core.ui.theme.SBColor
import com.sudutcorp.sudutbulat.core.util.buildInitials
import com.sudutcorp.sudutbulat.core.util.formatRupiah
import com.sudutcorp.sudutbulat.data.model.ExpenseCategory
import com.sudutcorp.sudutbulat.feature.outlet.OutletUiEvent
import com.sudutcorp.sudutbulat.feature.outlet.OutletUiState
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun OutletReportScreen(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    uiState: OutletUiState = OutletUiState(),
    onEvent: (OutletUiEvent) -> Unit = {}
) {
    val form = uiState.reportForm
    var showAddExpenseDialog by rememberSaveable { mutableStateOf(false) }
    var showAddDamagedDialog by rememberSaveable { mutableStateOf(false) }

    val totalSold = remember(form.productQuantities) {
        form.productQuantities.values.sumOf { it.toIntOrNull() ?: 0 }
    }
    val autoSubtotal = remember(form.productQuantities, uiState.activeProducts) {
        uiState.activeProducts.sumOf { prod ->
            (form.productQuantities[prod.id]?.toIntOrNull() ?: 0).toLong() * prod.price
        }
    }
    val cashVal = form.cashRevenueInput.toLongOrNull() ?: autoSubtotal
    val qrisVal = form.qrisRevenueInput.toLongOrNull() ?: 0L
    val totalExpense = form.expenses.sumOf { it.amount }
    val netDeposit = (cashVal + qrisVal - totalExpense).coerceAtLeast(0L)
    val dateFmt = remember { SimpleDateFormat("dd MMM yyyy, HH.mm", Locale.forLanguageTag("id-ID")) }
    val dayOnlyFmt = remember { SimpleDateFormat("dd MMM yyyy", Locale.forLanguageTag("id-ID")) }
    val isEditing = !form.editingReportId.isNullOrBlank()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
            .padding(top = 32.dp, bottom = 80.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = uiState.assignedOutlet?.let { "Outlet ${it.name}" } ?: "Staf Outlet",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = SBColor.Grey_500
                )
                Text("Laporan Harian", fontSize = 25.sp, fontWeight = FontWeight.ExtraBold, color = SBColor.Grey_700)
            }
            BadgedBox(
                badge = {
                    Badge(
                        containerColor = SBColor.Success_500,
                        modifier = Modifier.size(12.dp).border(2.dp, Color.White, CircleShape)
                    )
                }
            ) {
                Surface(modifier = Modifier.size(44.dp), shape = CircleShape, color = SBColor.Secondary_100) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = buildInitials(uiState.staffName),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = SBColor.Primary_500
                        )
                    }
                }
            }
        }

        // Indikator status laporan hari ini / mode edit
        if (isEditing) {
            OutlinedCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.outlinedCardColors(containerColor = SBColor.Primary_100),
                border = BorderStroke(1.dp, SBColor.Primary_200)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Mode edit laporan",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = SBColor.Primary_550
                        )
                        val editDateLabel = form.editingReportDate?.let { dayOnlyFmt.format(Date(it.toDate().time)) } ?: "hari ini"
                        Text(
                            text = "Perubahan akan memperbarui laporan tanggal $editDateLabel.",
                            fontSize = 12.sp,
                            color = SBColor.Grey_700
                        )
                    }
                    TextButton(onClick = { onEvent(OutletUiEvent.CancelEditDailyReport) }) {
                        Text("Batal", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = SBColor.Primary_550)
                    }
                }
            }
        } else if (uiState.todayReport != null) {
            val today = uiState.todayReport
            OutlinedCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.outlinedCardColors(containerColor = SBColor.Warning_100),
                border = BorderStroke(1.dp, SBColor.Warning_600.copy(alpha = 0.25f))
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Laporan hari ini sudah terkirim",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = SBColor.Warning_600
                        )
                        Text(
                            text = "${formatRupiah(today.totalRevenue)} • ${today.totalSold} pcs (${dateFmt.format(Date(today.date.toDate().time))})",
                            fontSize = 12.sp,
                            color = SBColor.Grey_700
                        )
                    }
                    TextButton(
                        enabled = !form.isLoadingExisting,
                        onClick = { onEvent(OutletUiEvent.LoadExistingReportForEdit(today.id)) }
                    ) {
                        Text(
                            text = if (form.isLoadingExisting) "Memuat..." else "Edit",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = SBColor.Primary_550
                        )
                    }
                }
            }
        }

        // 1. Input Penjualan Produk
        OutlinedCard(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.outlinedCardColors(containerColor = Color.White),
            border = BorderStroke(1.dp, SBColor.Grey_100)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("1. Produk terjual", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = SBColor.Grey_700)
                uiState.activeProducts.forEach { prod ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
                            Text(prod.name, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = SBColor.Grey_700)
                            Text(formatRupiah(prod.price), fontSize = 12.sp, color = SBColor.Grey_500)
                        }
                        OutlinedTextField(
                            value = form.productQuantities[prod.id].orEmpty(),
                            onValueChange = { onEvent(OutletUiEvent.UpdateProductQty(prod.id, it)) },
                            modifier = Modifier.width(88.dp),
                            placeholder = { Text("0") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            shape = RoundedCornerShape(10.dp)
                        )
                    }
                }
                HorizontalDivider(thickness = 1.dp, color = SBColor.Grey_100)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = form.cashRevenueInput,
                        onValueChange = { onEvent(OutletUiEvent.UpdateCashRevenue(it)) },
                        modifier = Modifier.weight(1f),
                        label = { Text("Tunai (Rp)", fontSize = 12.sp) },
                        placeholder = { Text(autoSubtotal.toString()) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        shape = RoundedCornerShape(10.dp)
                    )
                    OutlinedTextField(
                        value = form.qrisRevenueInput,
                        onValueChange = { onEvent(OutletUiEvent.UpdateQrisRevenue(it)) },
                        modifier = Modifier.weight(1f),
                        label = { Text("QRIS (Rp)", fontSize = 12.sp) },
                        placeholder = { Text("0") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        shape = RoundedCornerShape(10.dp)
                    )
                }
            }
        }

        // 2. Pengeluaran Operasional
        OutlinedCard(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.outlinedCardColors(containerColor = Color.White),
            border = BorderStroke(1.dp, SBColor.Grey_100)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("2. Pengeluaran operasional", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = SBColor.Grey_700)
                    TextButton(onClick = { showAddExpenseDialog = true }) {
                        Text("+ Tambah", fontWeight = FontWeight.Bold, color = SBColor.Primary_550)
                    }
                }
                if (form.expenses.isEmpty()) {
                    Text("Belum ada pengeluaran dicatat.", fontSize = 13.sp, color = SBColor.Grey_500)
                } else {
                    form.expenses.forEach { exp ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(exp.category.name, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = SBColor.Grey_700)
                                if (exp.note.isNotBlank()) Text(exp.note, fontSize = 12.sp, color = SBColor.Grey_500)
                            }
                            Text(formatRupiah(exp.amount), fontSize = 13.sp, fontWeight = FontWeight.Bold, color = SBColor.Grey_700)
                            TextButton(onClick = { onEvent(OutletUiEvent.RemoveDraftExpense(exp.id)) }) {
                                Text("Hapus", fontSize = 12.sp, color = SBColor.Primary_550)
                            }
                        }
                    }
                }
            }
        }

        // 3. Bahan Mentah Rusak
        OutlinedCard(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.outlinedCardColors(containerColor = Color.White),
            border = BorderStroke(1.dp, SBColor.Grey_100)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("3. Bahan mentah rusak", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = SBColor.Grey_700)
                    TextButton(onClick = { showAddDamagedDialog = true }) {
                        Text("+ Tambah", fontWeight = FontWeight.Bold, color = SBColor.Primary_550)
                    }
                }
                if (form.damagedMaterials.isEmpty()) {
                    Text("Tidak ada bahan rusak dicatat.", fontSize = 13.sp, color = SBColor.Grey_500)
                } else {
                    form.damagedMaterials.forEach { dmg ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("${dmg.materialName} (${dmg.quantity})", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = SBColor.Grey_700)
                                if (dmg.note.isNotBlank()) Text(dmg.note, fontSize = 12.sp, color = SBColor.Grey_500)
                            }
                            TextButton(onClick = { onEvent(OutletUiEvent.RemoveDraftDamaged(dmg.id)) }) {
                                Text("Hapus", fontSize = 12.sp, color = SBColor.Primary_550)
                            }
                        }
                    }
                }
            }
        }

        // Ringkasan Setoran
        OutlinedCard(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.outlinedCardColors(containerColor = Color.White),
            border = BorderStroke(1.dp, SBColor.Grey_100)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutletReportRow("Penjualan tunai", formatRupiah(cashVal))
                OutletReportRow("Penjualan QRIS", formatRupiah(qrisVal))
                OutletReportRow("Total produk terjual", "$totalSold pcs")
                OutletReportRow("Pengeluaran operasional", formatRupiah(totalExpense))
                HorizontalDivider(thickness = 1.dp, color = SBColor.Grey_100)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Estimasi setoran bersih", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = SBColor.Grey_700)
                    Text(formatRupiah(netDeposit), fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, color = SBColor.Success_600)
                }
            }
        }

        form.errorMessage?.let { err ->
            Text(err, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = SBColor.Primary_550)
        }

        Button(
            onClick = { onEvent(OutletUiEvent.SubmitDailyReport) },
            enabled = !form.isSaving && !form.isLoadingExisting,
            modifier = Modifier.fillMaxWidth().height(52.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = SBColor.Primary_550, contentColor = Color.White)
        ) {
            if (form.isSaving) {
                CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp, color = Color.White)
            } else {
                Text(
                    text = if (isEditing) "Perbarui laporan harian" else "Kirim laporan harian",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        if (uiState.recentReports.isNotEmpty()) {
            Text("Riwayat laporan terkirim", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = SBColor.Grey_700)
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                uiState.recentReports.take(5).forEach { rep ->
                    OutlinedCard(
                        onClick = { onEvent(OutletUiEvent.LoadExistingReportForEdit(rep.id)) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.outlinedCardColors(containerColor = Color.White),
                        border = BorderStroke(1.dp, SBColor.Grey_100)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(dateFmt.format(Date(rep.date.toDate().time)), fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = SBColor.Grey_700)
                                Text("${rep.totalSold} pcs terjual • Ketuk untuk edit", fontSize = 12.sp, color = SBColor.Grey_500)
                            }
                            Text(formatRupiah(rep.totalRevenue), fontSize = 14.sp, fontWeight = FontWeight.Bold, color = SBColor.Success_600)
                        }
                    }
                }
            }
        }
    }

    if (showAddExpenseDialog) {
        var category by remember { mutableStateOf(ExpenseCategory.GAS) }
        var amountInput by remember { mutableStateOf("") }
        var noteInput by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showAddExpenseDialog = false },
            containerColor = Color.White,
            title = { Text("Catat pengeluaran", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf(
                            ExpenseCategory.GAS to "Gas",
                            ExpenseCategory.PLASTIC_BAG to "Plastik",
                            ExpenseCategory.OTHER to "Lainnya"
                        ).forEach { (cat, label) ->
                            FilterChip(
                                selected = category == cat,
                                onClick = { category = cat },
                                label = { Text(label, fontSize = 12.sp) }
                            )
                        }
                    }
                    OutlinedTextField(
                        value = amountInput,
                        onValueChange = { amountInput = it.filter(Char::isDigit) },
                        label = { Text("Nominal (Rp)") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                    OutlinedTextField(
                        value = noteInput,
                        onValueChange = { noteInput = it },
                        label = { Text("Catatan") },
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val amt = amountInput.toLongOrNull() ?: 0L
                        if (amt > 0L) {
                            onEvent(OutletUiEvent.AddDraftExpense(category, amt, noteInput))
                            showAddExpenseDialog = false
                        }
                    }
                ) { Text("Tambah", fontWeight = FontWeight.Bold, color = SBColor.Primary_550) }
            },
            dismissButton = {
                TextButton(onClick = { showAddExpenseDialog = false }) { Text("Batal") }
            }
        )
    }

    if (showAddDamagedDialog) {
        var selectedMatId by remember { mutableStateOf(uiState.activeMaterials.firstOrNull()?.id.orEmpty()) }
        var qtyInput by remember { mutableStateOf("") }
        var noteInput by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showAddDamagedDialog = false },
            containerColor = Color.White,
            title = { Text("Laporkan bahan rusak", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        uiState.activeMaterials.take(6).forEach { mat ->
                            FilterChip(
                                selected = selectedMatId == mat.id,
                                onClick = { selectedMatId = mat.id },
                                label = { Text(mat.name, fontSize = 12.sp) }
                            )
                        }
                    }
                    OutlinedTextField(
                        value = qtyInput,
                        onValueChange = { qtyInput = it.filter(Char::isDigit) },
                        label = { Text("Jumlah rusak") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                    OutlinedTextField(
                        value = noteInput,
                        onValueChange = { noteInput = it },
                        label = { Text("Alasan / catatan") },
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val qty = qtyInput.toIntOrNull() ?: 0
                        if (selectedMatId.isNotBlank() && qty > 0) {
                            onEvent(OutletUiEvent.AddDraftDamaged(selectedMatId, qty, noteInput))
                            showAddDamagedDialog = false
                        }
                    }
                ) { Text("Tambah", fontWeight = FontWeight.Bold, color = SBColor.Primary_550) }
            },
            dismissButton = {
                TextButton(onClick = { showAddDamagedDialog = false }) { Text("Batal") }
            }
        )
    }

    val duplicateReport = form.pendingDuplicateReport
    if (duplicateReport != null) {
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
                        text = "Sistem menolak pembuatan laporan baru di tanggal yang sama. Apakah Anda ingin memperbarui laporan tersebut?",
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
private fun OutletReportRow(
    label: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, fontSize = 13.sp, color = SBColor.Grey_500)
        Text(text = value, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = SBColor.Grey_700)
    }
}
