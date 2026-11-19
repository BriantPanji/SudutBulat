package com.sudutcorp.sudutbulat.feature.distribution

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.sudutcorp.sudutbulat.R
import com.sudutcorp.sudutbulat.core.components.SBSelectField
import com.sudutcorp.sudutbulat.core.ui.theme.SBColor
import com.sudutcorp.sudutbulat.data.model.DistributionStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DistributionScreen(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    uiState: WarehouseUiState = WarehouseUiState(),
    effects: Flow<WarehouseUiEffect> = emptyFlow(),
    onEvent: (WarehouseUiEvent) -> Unit = {},
    initials: String = "BS"
) {
    var selectedFilter by rememberSaveable { mutableStateOf("Semua") }
    var showCreateSheet by rememberSaveable { mutableStateOf(false) }
    val filterOptions = listOf("Semua", "Permintaan", "Dikirim", "Selesai")

    LaunchedEffect(effects) {
        effects.collect { effect ->
            if (effect is WarehouseUiEffect.DistributionSaved) {
                showCreateSheet = false
            }
        }
    }

    val filteredCards = remember(uiState.distributionCards, selectedFilter) {
        uiState.distributionCards.filter { card ->
            when (selectedFilter) {
                "Permintaan" -> card.status == DistributionStatus.DRAFT
                "Dikirim" -> card.status == DistributionStatus.SENT
                "Selesai" -> card.status == DistributionStatus.RECEIVED ||
                    card.status == DistributionStatus.PARTIALLY_RECEIVED ||
                    card.status == DistributionStatus.NOT_RECEIVED ||
                    card.status == DistributionStatus.CANCELLED
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
                Text("Admin Gudang", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = SBColor.Grey_500)
                Text("Distribusi", fontSize = 25.sp, fontWeight = FontWeight.ExtraBold, color = SBColor.Grey_700)
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
                        Text(initials, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = SBColor.Primary_500)
                    }
                }
            }
        }

        Button(
            onClick = {
                onEvent(WarehouseUiEvent.InitCreateForm(null))
                showCreateSheet = true
            },
            modifier = Modifier.fillMaxWidth().height(52.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = SBColor.Primary_550, contentColor = Color.White)
        ) {
            Text(text = "Buat distribusi baru", fontSize = 15.sp, fontWeight = FontWeight.Bold)
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
                    label = { Text(text = option, fontSize = 13.sp, fontWeight = FontWeight.SemiBold) },
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

        Text(
            text = "${filteredCards.size} distribusi",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = SBColor.Grey_700
        )

        if (filteredCards.isEmpty()) {
            OutlinedCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.outlinedCardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, SBColor.Grey_100)
            ) {
                Box(modifier = Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                    Text("Belum ada data distribusi untuk filter ini.", fontSize = 14.sp, color = SBColor.Grey_500)
                }
            }
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                filteredCards.forEach { card ->
                    DistributionCardRow(
                        card = card,
                        isActionLoading = uiState.isActionLoading,
                        onApprove = { onEvent(WarehouseUiEvent.ApproveRequest(card.id)) },
                        onReject = { onEvent(WarehouseUiEvent.RejectRequest(card.id, "Ditolak oleh Admin Gudang")) }
                    )
                }
            }
        }
    }

    if (showCreateSheet) {
        CreateDistributionBottomSheet(
            uiState = uiState,
            onEvent = onEvent,
            onDismiss = { if (!uiState.createForm.isSaving) showCreateSheet = false }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateDistributionBottomSheet(
    uiState: WarehouseUiState,
    onEvent: (WarehouseUiEvent) -> Unit,
    onDismiss: () -> Unit
) {
    val form = uiState.createForm
    var showOutletPicker by rememberSaveable { mutableStateOf(false) }
    val selectedOutlet = uiState.activeOutlets.firstOrNull { it.id == form.selectedOutletId }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
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
            Text("Buat distribusi ke outlet", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = SBColor.Grey_700)

            SBSelectField(
                label = "Outlet tujuan",
                valueText = selectedOutlet?.let { "Outlet ${it.name}" } ?: "Pilih outlet tujuan",
                isPlaceholder = selectedOutlet == null,
                onClick = { showOutletPicker = !showOutletPicker }
            )

            if (showOutletPicker) {
                OutlinedCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.outlinedCardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, SBColor.Grey_100)
                ) {
                    Column {
                        uiState.activeOutlets.forEachIndexed { idx, outlet ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        onEvent(WarehouseUiEvent.SelectOutlet(outlet.id))
                                        showOutletPicker = false
                                    }
                                    .padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Outlet ${outlet.name}", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = SBColor.Grey_700)
                                RadioButton(
                                    selected = outlet.id == form.selectedOutletId,
                                    onClick = {
                                        onEvent(WarehouseUiEvent.SelectOutlet(outlet.id))
                                        showOutletPicker = false
                                    },
                                    colors = RadioButtonDefaults.colors(selectedColor = SBColor.Primary_550)
                                )
                            }
                            if (idx < uiState.activeOutlets.lastIndex) {
                                HorizontalDivider(thickness = 1.dp, color = SBColor.Grey_100)
                            }
                        }
                    }
                }
            }

            Text("Jumlah bahan mentah", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = SBColor.Grey_700)

            LazyColumn(
                modifier = Modifier.fillMaxWidth().heightIn(max = 260.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(
                    items = uiState.activeMaterials,
                    key = { it.id },
                    contentType = { "dist_material_input" }
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
                            onValueChange = { onEvent(WarehouseUiEvent.UpdateItemQuantity(mat.id, it)) },
                            modifier = Modifier.width(100.dp),
                            placeholder = { Text("0", fontSize = 13.sp) },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            shape = RoundedCornerShape(10.dp)
                        )
                    }
                }
            }

            OutlinedTextField(
                value = form.note,
                onValueChange = { onEvent(WarehouseUiEvent.UpdateNote(it)) },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Catatan pengiriman (opsional)") },
                placeholder = { Text("Distribusi rutin pagi hari") },
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )

            form.errorMessage?.let { err ->
                Text(text = err, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = SBColor.Primary_550)
            }

            Button(
                onClick = { onEvent(WarehouseUiEvent.SubmitCreateDistribution) },
                enabled = !form.isSaving,
                modifier = Modifier.fillMaxWidth().height(50.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = SBColor.Primary_550)
            ) {
                if (form.isSaving) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp, color = Color.White)
                } else {
                    Text("Kirim distribusi sekarang", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun DistributionCardRow(
    card: DistributionCardUi,
    isActionLoading: Boolean,
    onApprove: () -> Unit,
    onReject: () -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedCard(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.outlinedCardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, if (card.status == DistributionStatus.DRAFT) SBColor.Warning_100 else SBColor.Grey_100)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Surface(
                    modifier = Modifier.size(40.dp),
                    shape = RoundedCornerShape(10.dp),
                    color = SBColor.Primary_100
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            painter = painterResource(R.drawable.ic_truck),
                            contentDescription = null,
                            modifier = Modifier.size(20.dp),
                            tint = SBColor.Primary_550
                        )
                    }
                }
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(card.outletName, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = SBColor.Grey_700)
                        DistributionStatusBadge(status = card.status)
                    }
                    Text(card.itemsSummary, fontSize = 12.sp, color = SBColor.Grey_600)
                    Text("${card.totalQuantity} total unit • ${card.timeLabel}", fontSize = 11.sp, color = SBColor.Grey_500)
                    if (!card.note.isNullOrBlank()) {
                        Text(card.note, fontSize = 12.sp, color = SBColor.Grey_500)
                    }
                }
            }

            if (card.status == DistributionStatus.DRAFT) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onReject,
                        enabled = !isActionLoading,
                        modifier = Modifier.weight(1f).height(40.dp),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Text("Tolak", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = SBColor.Primary_550)
                    }
                    Button(
                        onClick = onApprove,
                        enabled = !isActionLoading,
                        modifier = Modifier.weight(1f).height(40.dp),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = SBColor.Primary_550),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Text("Setujui & Kirim", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }
        }
    }
}

@Composable
fun DistributionStatusBadge(
    status: DistributionStatus,
    modifier: Modifier = Modifier
) {
    val (label, containerColor, contentColor) = when (status) {
        DistributionStatus.DRAFT -> Triple("Permintaan", SBColor.Warning_100, SBColor.Warning_600)
        DistributionStatus.SENT -> Triple("Dikirim", SBColor.Primary_100, SBColor.Primary_550)
        DistributionStatus.RECEIVED -> Triple("Diterima", SBColor.Success_100, SBColor.Success_600)
        DistributionStatus.PARTIALLY_RECEIVED -> Triple("Diterima sebagian", SBColor.Warning_100, SBColor.Warning_600)
        DistributionStatus.NOT_RECEIVED -> Triple("Tidak diterima", SBColor.Error_100, SBColor.Primary_550)
        DistributionStatus.CANCELLED -> Triple("Ditolak", SBColor.Grey_100, SBColor.Grey_600)
    }

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(6.dp),
        color = containerColor,
        contentColor = contentColor
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold
        )
    }
}
