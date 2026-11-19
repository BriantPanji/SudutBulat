package com.sudutcorp.sudutbulat.feature.stock

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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
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
import com.sudutcorp.sudutbulat.data.model.DistributionStatus
import com.sudutcorp.sudutbulat.feature.distribution.DistributionStatusBadge
import com.sudutcorp.sudutbulat.feature.outlet.OutletDistributionUi
import com.sudutcorp.sudutbulat.feature.outlet.OutletUiEffect
import com.sudutcorp.sudutbulat.feature.outlet.OutletUiEvent
import com.sudutcorp.sudutbulat.feature.outlet.OutletUiState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OutletStockScreen(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    uiState: OutletUiState = OutletUiState(),
    effects: Flow<OutletUiEffect> = emptyFlow(),
    onEvent: (OutletUiEvent) -> Unit = {}
) {
    var selectedFilter by rememberSaveable { mutableStateOf("Semua") }
    var showRequestSheet by rememberSaveable { mutableStateOf(false) }
    var confirmingDistId by rememberSaveable { mutableStateOf<String?>(null) }
    val filterOptions = listOf("Semua", "Perlu Konfirmasi", "Permintaan", "Selesai")

    LaunchedEffect(effects) {
        effects.collect { effect ->
            when (effect) {
                OutletUiEffect.StockRequestSaved -> showRequestSheet = false
                OutletUiEffect.DistributionConfirmed -> confirmingDistId = null
                else -> Unit
            }
        }
    }

    val filteredDistributions = remember(uiState.distributions, selectedFilter) {
        uiState.distributions.filter { dist ->
            when (selectedFilter) {
                "Perlu Konfirmasi" -> dist.status == DistributionStatus.SENT
                "Permintaan" -> dist.status == DistributionStatus.DRAFT
                "Selesai" -> dist.status == DistributionStatus.RECEIVED ||
                    dist.status == DistributionStatus.PARTIALLY_RECEIVED ||
                    dist.status == DistributionStatus.NOT_RECEIVED ||
                    dist.status == DistributionStatus.CANCELLED
                else -> true
            }
        }
    }

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
                Text("Stok & Distribusi", fontSize = 25.sp, fontWeight = FontWeight.ExtraBold, color = SBColor.Grey_700)
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

        Button(
            onClick = {
                onEvent(OutletUiEvent.ResetStockRequestForm)
                showRequestSheet = true
            },
            modifier = Modifier.fillMaxWidth().height(50.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = SBColor.Primary_550, contentColor = Color.White)
        ) {
            Text("Ajukan request tambahan stok", fontSize = 14.sp, fontWeight = FontWeight.Bold)
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            filterOptions.forEach { option ->
                val isSelected = option == selectedFilter
                FilterChip(
                    selected = isSelected,
                    onClick = { selectedFilter = option },
                    label = { Text(text = option, fontSize = 12.sp, fontWeight = FontWeight.SemiBold) },
                    shape = RoundedCornerShape(10.dp),
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = SBColor.Grey_700,
                        selectedLabelColor = Color.White,
                        containerColor = SBColor.Grey_100,
                        labelColor = SBColor.Grey_700
                    ),
                    border = null
                )
            }
        }

        if (filteredDistributions.isEmpty()) {
            OutlinedCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.outlinedCardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, SBColor.Grey_100)
            ) {
                Box(modifier = Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                    Text("Belum ada pengiriman atau request stok untuk filter ini.", fontSize = 14.sp, color = SBColor.Grey_500)
                }
            }
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                filteredDistributions.forEach { dist ->
                    OutletDistributionCard(
                        dist = dist,
                        onConfirmClick = { confirmingDistId = dist.id }
                    )
                }
            }
        }
    }

    if (showRequestSheet) {
        val form = uiState.requestForm
        ModalBottomSheet(
            onDismissRequest = { if (!form.isSaving) showRequestSheet = false },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            containerColor = Color.White,
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(horizontal = 24.dp)
                    .padding(bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text("Request tambahan stok", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = SBColor.Grey_700)

                LazyColumn(
                    modifier = Modifier.fillMaxWidth().heightIn(max = 280.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(
                        items = uiState.activeMaterials,
                        key = { it.id },
                        contentType = { "req_material_item" }
                    ) { mat ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
                                Text(mat.name, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = SBColor.Grey_700)
                                Text("Satuan: ${mat.unit.firestoreValue}", fontSize = 12.sp, color = SBColor.Grey_500)
                            }
                            OutlinedTextField(
                                value = form.itemQuantities[mat.id].orEmpty(),
                                onValueChange = { onEvent(OutletUiEvent.UpdateRequestQty(mat.id, it)) },
                                modifier = Modifier.width(100.dp),
                                placeholder = { Text("0") },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                shape = RoundedCornerShape(10.dp)
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = form.note,
                    onValueChange = { onEvent(OutletUiEvent.UpdateRequestNote(it)) },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Alasan permintaan (opsional)") },
                    placeholder = { Text("Stok menipis karena pesanan ramai") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )

                form.errorMessage?.let { err ->
                    Text(err, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = SBColor.Primary_550)
                }

                Button(
                    onClick = { onEvent(OutletUiEvent.SubmitStockRequest) },
                    enabled = !form.isSaving,
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = SBColor.Primary_550)
                ) {
                    if (form.isSaving) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp, color = Color.White)
                    } else {
                        Text("Kirim permintaan ke Gudang", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }

    val confirmingDist = confirmingDistId?.let { id -> uiState.distributions.firstOrNull { it.id == id } }
    if (confirmingDist != null) {
        val qtyMap = remember(confirmingDist.id) {
            mutableStateMapOf<String, String>().apply {
                confirmingDist.items.forEach { put(it.id, it.quantitySent.toString()) }
            }
        }
        var confirmNote by rememberSaveable(confirmingDist.id) { mutableStateOf("") }

        ModalBottomSheet(
            onDismissRequest = { if (!uiState.isActionLoading) confirmingDistId = null },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            containerColor = Color.White,
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(horizontal = 24.dp)
                    .padding(bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text("Konfirmasi penerimaan stok", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = SBColor.Grey_700)
                Text("Sesuaikan jumlah yang benar-benar diterima jika ada selisih.", fontSize = 13.sp, color = SBColor.Grey_500)

                confirmingDist.items.forEach { item ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
                            Text(item.materialName, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = SBColor.Grey_700)
                            Text("Dikirim: ${item.quantitySent} ${item.unitLabel}", fontSize = 12.sp, color = SBColor.Grey_500)
                        }
                        OutlinedTextField(
                            value = qtyMap[item.id].orEmpty(),
                            onValueChange = { qtyMap[item.id] = it.filter(Char::isDigit) },
                            modifier = Modifier.width(100.dp),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            shape = RoundedCornerShape(10.dp)
                        )
                    }
                }

                OutlinedTextField(
                    value = confirmNote,
                    onValueChange = { confirmNote = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Catatan penerimaan (opsional)") },
                    placeholder = { Text("Stok diterima lengkap dan sesuai") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )

                Button(
                    onClick = {
                        val parsed = confirmingDist.items.associate { item ->
                            item.id to (qtyMap[item.id]?.toIntOrNull() ?: item.quantitySent)
                        }
                        onEvent(OutletUiEvent.ConfirmDistribution(confirmingDist.id, parsed, confirmNote))
                    },
                    enabled = !uiState.isActionLoading,
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = SBColor.Primary_550)
                ) {
                    if (uiState.isActionLoading) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp, color = Color.White)
                    } else {
                        Text("Konfirmasi terima", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun OutletDistributionCard(
    dist: OutletDistributionUi,
    onConfirmClick: () -> Unit
) {
    OutlinedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.outlinedCardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, if (dist.status == DistributionStatus.SENT) SBColor.Primary_200 else SBColor.Grey_100)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(dist.timeLabel, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = SBColor.Grey_700)
                DistributionStatusBadge(status = dist.status)
            }

            dist.items.forEach { item ->
                val qtyText = if (item.quantityReceived != null) {
                    "Diterima ${item.quantityReceived}/${item.quantitySent} ${item.unitLabel}"
                } else {
                    "${item.quantitySent} ${item.unitLabel}"
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(item.materialName, fontSize = 13.sp, color = SBColor.Grey_600)
                    Text(qtyText, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = SBColor.Grey_700)
                }
            }

            if (!dist.note.isNullOrBlank()) {
                HorizontalDivider(thickness = 1.dp, color = SBColor.Grey_100)
                Text(dist.note, fontSize = 12.sp, color = SBColor.Grey_500)
            }
            if (!dist.confirmationNote.isNullOrBlank()) {
                Text("Catatan: ${dist.confirmationNote}", fontSize = 12.sp, color = SBColor.Grey_600)
            }

            if (dist.status == DistributionStatus.SENT) {
                Button(
                    onClick = onConfirmClick,
                    modifier = Modifier.fillMaxWidth().height(42.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = SBColor.Primary_550)
                ) {
                    Text("Konfirmasi penerimaan", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
