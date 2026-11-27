package com.sudutcorp.sudutbulat.feature.stock

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.navigation.NavHostController
import com.sudutcorp.sudutbulat.R
import com.sudutcorp.sudutbulat.core.components.SBBottomCtaBar
import com.sudutcorp.sudutbulat.core.components.SBConfirmDialog
import com.sudutcorp.sudutbulat.core.components.SBFilterChipRow
import com.sudutcorp.sudutbulat.core.components.SBInlineAlert
import com.sudutcorp.sudutbulat.core.components.SBKeyValueRow
import com.sudutcorp.sudutbulat.core.components.SBPageHeader
import com.sudutcorp.sudutbulat.core.components.SBSearchBar
import com.sudutcorp.sudutbulat.core.ui.theme.SBColor
import com.sudutcorp.sudutbulat.core.util.buildInitials
import com.sudutcorp.sudutbulat.data.model.DistributionStatus
import com.sudutcorp.sudutbulat.data.model.DistributionType
import com.sudutcorp.sudutbulat.data.model.Material
import com.sudutcorp.sudutbulat.feature.distribution.DistributionStatusBadge
import com.sudutcorp.sudutbulat.feature.outlet.OutletDistributionUi
import com.sudutcorp.sudutbulat.feature.outlet.OutletStockFilter
import com.sudutcorp.sudutbulat.feature.outlet.OutletUiEffect
import com.sudutcorp.sudutbulat.feature.outlet.OutletUiEvent
import com.sudutcorp.sudutbulat.feature.outlet.OutletUiState
import com.sudutcorp.sudutbulat.feature.outlet.StockRequestFormState
import com.sudutcorp.sudutbulat.feature.outlet.filterOutletDistributions
import com.sudutcorp.sudutbulat.feature.outlet.resolveConfirmationStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import java.util.Locale

private val QUICK_DISCREPANCY_REASONS = listOf(
    "Jumlah kurang",
    "Barang rusak/pecah",
    "Outlet sudah tutup",
    "Barang tertukar"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OutletStockScreen(
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

    var selectedFilterName by rememberSaveable { mutableStateOf(OutletStockFilter.ALL.name) }
    var searchQuery by rememberSaveable { mutableStateOf("") }
    var showRequestSheet by rememberSaveable { mutableStateOf(false) }
    var confirmingDistId by rememberSaveable { mutableStateOf<String?>(null) }
    var detailDistId by rememberSaveable { mutableStateOf<String?>(null) }
    var cancellingDistId by rememberSaveable { mutableStateOf<String?>(null) }

    val selectedFilter = remember(selectedFilterName) {
        OutletStockFilter.entries.firstOrNull { it.name == selectedFilterName } ?: OutletStockFilter.ALL
    }

    LaunchedEffect(effects) {
        effects.collect { effect ->
            when (effect) {
                OutletUiEffect.StockRequestSaved -> showRequestSheet = false
                OutletUiEffect.DistributionConfirmed -> {
                    confirmingDistId = null
                    detailDistId = null
                }
                OutletUiEffect.StockRequestCancelled -> {
                    cancellingDistId = null
                    detailDistId = null
                }
                else -> Unit
            }
        }
    }

    val pendingConfirmDists = remember(uiState.distributions) {
        uiState.distributions.filter { it.status == DistributionStatus.SENT }
    }
    val pendingRequestCount = remember(uiState.distributions) {
        uiState.distributions.count { it.status == DistributionStatus.DRAFT }
    }
    val doneCount = remember(uiState.distributions) {
        uiState.distributions.count {
            it.status == DistributionStatus.RECEIVED ||
                it.status == DistributionStatus.PARTIALLY_RECEIVED ||
                it.status == DistributionStatus.NOT_RECEIVED ||
                it.status == DistributionStatus.CANCELLED
        }
    }

    val filterChipOptions = remember(
        uiState.distributions.size,
        pendingConfirmDists.size,
        pendingRequestCount,
        doneCount
    ) {
        listOf(
            OutletStockFilter.ALL.name to "Semua (${uiState.distributions.size})",
            OutletStockFilter.NEED_CONFIRM.name to "Perlu Konfirmasi (${pendingConfirmDists.size})",
            OutletStockFilter.REQUEST.name to "Permintaan ($pendingRequestCount)",
            OutletStockFilter.DONE.name to "Selesai ($doneCount)"
        )
    }

    val filteredDistributions = remember(uiState.distributions, selectedFilter, searchQuery) {
        filterOutletDistributions(uiState.distributions, selectedFilter, searchQuery)
    }

    val hasActiveFilter = selectedFilter != OutletStockFilter.ALL || searchQuery.isNotBlank()
    val eyebrow = uiState.assignedOutlet?.let { outlet ->
        val base = if (outlet.name.startsWith("Outlet", ignoreCase = true)) outlet.name else "Outlet ${outlet.name}"
        if (isDevMode) "$base • Mode DEV" else base
    } ?: if (isDevMode) "Staf Outlet • Mode DEV" else "Staf Outlet"
    val initials = if (isDevMode) "DV" else buildInitials(uiState.staffName)

    LazyColumn(
        state = listState,
        modifier = modifier
            .fillMaxSize()
            .background(Color.White)
            .pointerInput(Unit) { detectTapGestures(onTap = { focusManager.clearFocus() }) },
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item(key = "outlet_stock_header", contentType = "header") {
            SBPageHeader(
                eyebrow = eyebrow,
                title = "Stok & Distribusi",
                initials = initials,
                isAvatarClickable = isDevMode,
                onAvatarClick = onSwitchRoleClick
            )
        }

        if (pendingConfirmDists.isNotEmpty()) {
            item(key = "outlet_stock_pending_banner", contentType = "pending_banner") {
                val latest = pendingConfirmDists.first()
                PendingConfirmBanner(
                    pendingCount = pendingConfirmDists.size,
                    subtitle = if (pendingConfirmDists.size == 1) {
                        "${latest.type.label} • ${latest.timeLabel} (${latest.items.size} bahan)"
                    } else {
                        "Terbaru: ${latest.type.label} • ${latest.timeLabel}"
                    },
                    onActionClick = {
                        if (pendingConfirmDists.size == 1) {
                            confirmingDistId = latest.id
                        } else {
                            selectedFilterName = OutletStockFilter.NEED_CONFIRM.name
                        }
                    }
                )
            }
        }

        item(key = "outlet_stock_request_cta", contentType = "cta") {
            Button(
                onClick = {
                    onEvent(OutletUiEvent.ResetStockRequestForm)
                    showRequestSheet = true
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = SBColor.Primary_550,
                    contentColor = Color.White
                )
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_box),
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = "Ajukan request tambahan stok",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        item(key = "outlet_stock_search", contentType = "search") {
            SBSearchBar(
                query = searchQuery,
                onQueryChange = { searchQuery = it },
                placeholder = "Cari bahan, catatan, atau pengirim..."
            )
        }

        item(key = "outlet_stock_filters", contentType = "filters") {
            SBFilterChipRow(
                options = filterChipOptions,
                selectedKey = selectedFilter.name,
                onSelected = { selectedFilterName = it }
            )
        }

        item(key = "outlet_stock_toolbar", contentType = "toolbar") {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${filteredDistributions.size} distribusi",
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
                                selectedFilterName = OutletStockFilter.ALL.name
                                searchQuery = ""
                            }
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }
        }

        if (filteredDistributions.isEmpty()) {
            item(key = "outlet_stock_empty", contentType = "empty") {
                EmptyOutletStockCard(
                    hasActiveFilter = hasActiveFilter,
                    filter = selectedFilter,
                    onResetFilter = {
                        selectedFilterName = OutletStockFilter.ALL.name
                        searchQuery = ""
                    },
                    onRequestClick = {
                        onEvent(OutletUiEvent.ResetStockRequestForm)
                        showRequestSheet = true
                    }
                )
            }
        } else {
            items(
                items = filteredDistributions,
                key = { it.id },
                contentType = { "outlet_distribution_card" }
            ) { dist ->
                OutletDistributionCard(
                    dist = dist,
                    isActionLoading = uiState.isActionLoading,
                    onClick = { detailDistId = dist.id },
                    onConfirmClick = { confirmingDistId = dist.id },
                    onCancelRequestClick = { cancellingDistId = dist.id }
                )
            }
        }
    }

    if (showRequestSheet) {
        StockRequestBottomSheet(
            activeMaterials = uiState.activeMaterials,
            form = uiState.requestForm,
            onDismiss = { if (!uiState.requestForm.isSaving) showRequestSheet = false },
            onUpdateQty = { matId, qty -> onEvent(OutletUiEvent.UpdateRequestQty(matId, qty)) },
            onUpdateNote = { onEvent(OutletUiEvent.UpdateRequestNote(it)) },
            onResetForm = { onEvent(OutletUiEvent.ResetStockRequestForm) },
            onSubmit = { onEvent(OutletUiEvent.SubmitStockRequest) }
        )
    }

    val detailDist = detailDistId?.let { id -> uiState.distributions.firstOrNull { it.id == id } }
    if (detailDist != null) {
        OutletDistributionDetailSheet(
            dist = detailDist,
            isActionLoading = uiState.isActionLoading,
            onDismiss = { detailDistId = null },
            onConfirmClick = {
                detailDistId = null
                confirmingDistId = detailDist.id
            },
            onCancelRequestClick = {
                detailDistId = null
                cancellingDistId = detailDist.id
            }
        )
    }

    val confirmingDist = confirmingDistId?.let { id -> uiState.distributions.firstOrNull { it.id == id } }
    if (confirmingDist != null) {
        ConfirmReceiptBottomSheet(
            dist = confirmingDist,
            isActionLoading = uiState.isActionLoading,
            onDismiss = { if (!uiState.isActionLoading) confirmingDistId = null },
            onConfirm = { receivedMap, note ->
                onEvent(OutletUiEvent.ConfirmDistribution(confirmingDist.id, receivedMap, note))
            }
        )
    }

    val cancellingDist = cancellingDistId?.let { id -> uiState.distributions.firstOrNull { it.id == id } }
    if (cancellingDist != null) {
        SBConfirmDialog(
            title = "Batalkan permintaan stok?",
            description = "Permintaan ${cancellingDist.items.size} bahan (${cancellingDist.totalSent} unit) ini akan ditandai batal dan tidak akan diproses Gudang.",
            confirmText = "Ya, Batalkan",
            cancelText = "Kembali",
            iconRes = R.drawable.ic_warning,
            iconBgColor = SBColor.Primary_100,
            iconTint = SBColor.Primary_550,
            onDismiss = { cancellingDistId = null },
            onConfirm = { onEvent(OutletUiEvent.CancelStockRequest(cancellingDist.id)) }
        )
    }
}

@Composable
private fun PendingConfirmBanner(
    pendingCount: Int,
    subtitle: String,
    onActionClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedCard(
        onClick = onActionClick,
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.outlinedCardColors(containerColor = SBColor.Warning_100),
        border = BorderStroke(1.dp, SBColor.Warning_600.copy(alpha = 0.25f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Surface(
                modifier = Modifier.size(40.dp),
                shape = RoundedCornerShape(10.dp),
                color = Color.White.copy(alpha = 0.8f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        painter = painterResource(R.drawable.ic_truck),
                        contentDescription = null,
                        tint = SBColor.Warning_600,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = "$pendingCount pengiriman menunggu konfirmasi",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = SBColor.Warning_600
                )
                Text(
                    text = subtitle,
                    fontSize = 12.sp,
                    color = SBColor.Warning_600.copy(alpha = 0.85f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = SBColor.Primary_550
            ) {
                Text(
                    text = "Cek",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                )
            }
        }
    }
}

@Composable
private fun EmptyOutletStockCard(
    hasActiveFilter: Boolean,
    filter: OutletStockFilter,
    onResetFilter: () -> Unit,
    onRequestClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedCard(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.outlinedCardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, SBColor.Grey_100)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Surface(
                modifier = Modifier.size(48.dp),
                shape = RoundedCornerShape(14.dp),
                color = SBColor.Grey_100
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        painter = painterResource(R.drawable.ic_box),
                        contentDescription = null,
                        tint = SBColor.Grey_500,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
            Text(
                text = when {
                    hasActiveFilter -> "Tidak ada distribusi yang cocok"
                    filter == OutletStockFilter.NEED_CONFIRM -> "Semua pengiriman sudah dikonfirmasi"
                    else -> "Belum ada riwayat stok & distribusi"
                },
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = SBColor.Grey_700,
                textAlign = TextAlign.Center
            )
            Text(
                text = if (hasActiveFilter) {
                    "Coba ubah kata kunci pencarian atau pilih filter status lainnya."
                } else {
                    "Pengiriman rutin dari Gudang atau request tambahan stok akan tampil di sini."
                },
                fontSize = 13.sp,
                color = SBColor.Grey_500,
                textAlign = TextAlign.Center
            )
            if (hasActiveFilter) {
                OutlinedButton(
                    onClick = onResetFilter,
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text(
                        text = "Reset filter",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = SBColor.Primary_550
                    )
                }
            } else {
                Button(
                    onClick = onRequestClick,
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = SBColor.Primary_550)
                ) {
                    Text(
                        text = "Ajukan request stok",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun OutletDistributionCard(
    dist: OutletDistributionUi,
    isActionLoading: Boolean,
    onClick: () -> Unit,
    onConfirmClick: () -> Unit,
    onCancelRequestClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val (iconBg, iconTint, borderColor) = when (dist.status) {
        DistributionStatus.DRAFT -> Triple(SBColor.Warning_100, SBColor.Warning_600, SBColor.Secondary_200)
        DistributionStatus.SENT -> Triple(SBColor.Primary_100, SBColor.Primary_550, SBColor.Primary_200)
        DistributionStatus.RECEIVED -> Triple(SBColor.Success_100, SBColor.Success_600, SBColor.Grey_100)
        DistributionStatus.PARTIALLY_RECEIVED -> Triple(SBColor.Warning_100, SBColor.Warning_600, SBColor.Secondary_200)
        DistributionStatus.NOT_RECEIVED -> Triple(SBColor.Error_100, SBColor.Primary_550, SBColor.Primary_200)
        DistributionStatus.CANCELLED -> Triple(SBColor.Grey_100, SBColor.Grey_500, SBColor.Grey_100)
    }
    val iconRes = if (dist.type == DistributionType.ROUTINE) R.drawable.ic_truck else R.drawable.ic_box

    OutlinedCard(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.outlinedCardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, borderColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
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
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.weight(1f).padding(end = 8.dp)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(5.dp),
                                color = if (dist.type == DistributionType.ROUTINE) SBColor.Secondary_100 else SBColor.Grey_100
                            ) {
                                Text(
                                    text = dist.type.label,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (dist.type == DistributionType.ROUTINE) SBColor.Secondary_500 else SBColor.Grey_600,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                            Text(
                                text = dist.timeLabel,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = SBColor.Grey_700,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        DistributionStatusBadge(status = dist.status)
                    }
                    Text(
                        text = if (dist.createdByName.isNotBlank()) {
                            "Oleh ${dist.createdByName} • ${dist.items.size} bahan"
                        } else {
                            "${dist.items.size} bahan"
                        },
                        fontSize = 12.sp,
                        color = SBColor.Grey_500,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            HorizontalDivider(thickness = 1.dp, color = SBColor.Grey_100)

            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                val previewItems = dist.items.take(3)
                previewItems.forEach { item ->
                    val hasDiscrepancy = item.quantityReceived != null && item.quantityReceived != item.quantitySent
                    val qtyText = if (item.quantityReceived != null) {
                        "${item.quantityReceived}/${item.quantitySent} ${item.unitLabel}"
                    } else {
                        "${item.quantitySent} ${item.unitLabel}"
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = item.materialName,
                            fontSize = 13.sp,
                            color = SBColor.Grey_600,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f).padding(end = 8.dp)
                        )
                        Text(
                            text = qtyText,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (hasDiscrepancy) SBColor.Primary_550 else SBColor.Grey_700
                        )
                    }
                }
                val summaryFooter = if (dist.items.size > 3) {
                    "+${dist.items.size - 3} bahan lainnya • Total ${dist.totalSent} unit"
                } else {
                    "Total ${dist.totalSent} unit"
                }
                Text(
                    text = summaryFooter,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    color = SBColor.Grey_500
                )
            }

            val displayNote = dist.confirmationNote?.takeIf { it.isNotBlank() }
                ?: dist.note?.takeIf { it.isNotBlank() }
            if (displayNote != null) {
                val isIssueNote = !dist.confirmationNote.isNullOrBlank() &&
                    (dist.status == DistributionStatus.CANCELLED || dist.status == DistributionStatus.NOT_RECEIVED)
                val noteBg = when {
                    isIssueNote -> SBColor.Error_100
                    !dist.confirmationNote.isNullOrBlank() && dist.status == DistributionStatus.PARTIALLY_RECEIVED -> SBColor.Warning_100
                    else -> SBColor.Grey_100.copy(alpha = 0.7f)
                }
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    color = noteBg
                ) {
                    Text(
                        text = displayNote,
                        fontSize = 12.sp,
                        color = SBColor.Grey_700,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp)
                    )
                }
            }

            when (dist.status) {
                DistributionStatus.SENT -> {
                    Button(
                        onClick = onConfirmClick,
                        enabled = !isActionLoading,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(42.dp),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = SBColor.Primary_550)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.ic_check),
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Text("Konfirmasi penerimaan", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
                DistributionStatus.DRAFT -> {
                    OutlinedButton(
                        onClick = onCancelRequestClick,
                        enabled = !isActionLoading,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(40.dp),
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, SBColor.Primary_200)
                    ) {
                        Text(
                            text = "Batalkan permintaan",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = SBColor.Primary_550
                        )
                    }
                }
                else -> Unit
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun OutletDistributionDetailSheet(
    dist: OutletDistributionUi,
    isActionLoading: Boolean,
    onDismiss: () -> Unit,
    onConfirmClick: () -> Unit,
    onCancelRequestClick: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = Color.White,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .navigationBarsPadding()
                .padding(horizontal = 24.dp)
                .padding(bottom = 28.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        text = "Distribusi ${dist.type.label}",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = SBColor.Grey_700
                    )
                    Text(
                        text = dist.timeLabel,
                        fontSize = 12.sp,
                        color = SBColor.Grey_500
                    )
                }
                DistributionStatusBadge(status = dist.status)
            }

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
                    SBKeyValueRow(label = "Dibuat oleh", value = dist.createdByName.ifBlank { "Gudang" })
                    dist.sentTimeLabel?.let { SBKeyValueRow(label = "Waktu dikirim", value = it) }
                    dist.confirmedTimeLabel?.let { SBKeyValueRow(label = "Waktu dikonfirmasi", value = it) }
                    dist.confirmedByName?.let { SBKeyValueRow(label = "Dikonfirmasi oleh", value = it) }
                }
            }

            Text(
                text = "Rincian bahan (${dist.items.size} item • ${dist.totalSent} unit)",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = SBColor.Grey_700
            )

            OutlinedCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.outlinedCardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, SBColor.Grey_100)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    dist.items.forEachIndexed { idx, item ->
                        val diff = item.quantityReceived?.let { item.quantitySent - it }
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
                                Text(
                                    text = item.materialName,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = SBColor.Grey_700
                                )
                                if (!item.note.isNullOrBlank()) {
                                    Text(item.note, fontSize = 11.sp, color = SBColor.Grey_500)
                                }
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = if (item.quantityReceived != null) {
                                        "${item.quantityReceived}/${item.quantitySent} ${item.unitLabel}"
                                    } else {
                                        "${item.quantitySent} ${item.unitLabel}"
                                    },
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (diff != null && diff > 0) SBColor.Primary_550 else SBColor.Grey_700
                                )
                                if (diff != null && diff != 0) {
                                    Text(
                                        text = if (diff > 0) "Kurang $diff ${item.unitLabel}" else "Lebih ${-diff} ${item.unitLabel}",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = SBColor.Warning_600
                                    )
                                }
                            }
                        }
                        if (idx < dist.items.lastIndex) {
                            HorizontalDivider(thickness = 1.dp, color = SBColor.Grey_100)
                        }
                    }
                }
            }

            if (!dist.note.isNullOrBlank()) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    color = SBColor.Grey_100
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Text(
                            text = if (dist.type == DistributionType.ADDITIONAL) "Catatan permintaan" else "Catatan pengiriman",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = SBColor.Grey_500
                        )
                        Text(dist.note, fontSize = 13.sp, color = SBColor.Grey_700)
                    }
                }
            }

            if (!dist.confirmationNote.isNullOrBlank()) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    color = if (dist.status == DistributionStatus.CANCELLED || dist.status == DistributionStatus.NOT_RECEIVED) {
                        SBColor.Error_100
                    } else {
                        SBColor.Secondary_100
                    }
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Text(
                            text = if (dist.status == DistributionStatus.CANCELLED) "Alasan pembatalan" else "Catatan konfirmasi",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = SBColor.Grey_600
                        )
                        Text(dist.confirmationNote, fontSize = 13.sp, color = SBColor.Grey_700)
                    }
                }
            }

            when (dist.status) {
                DistributionStatus.SENT -> {
                    Button(
                        onClick = onConfirmClick,
                        enabled = !isActionLoading,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = SBColor.Primary_550)
                    ) {
                        Text("Konfirmasi penerimaan", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    }
                }
                DistributionStatus.DRAFT -> {
                    OutlinedButton(
                        onClick = onCancelRequestClick,
                        enabled = !isActionLoading,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(46.dp),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, SBColor.Primary_200)
                    ) {
                        Text(
                            text = "Batalkan permintaan",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = SBColor.Primary_550
                        )
                    }
                }
                else -> Unit
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ConfirmReceiptBottomSheet(
    dist: OutletDistributionUi,
    isActionLoading: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (Map<String, Int>, String) -> Unit
) {
    val focusManager = LocalFocusManager.current
    val listState = rememberLazyListState()
    val qtyMap = remember(dist.id) {
        mutableStateMapOf<String, String>().apply {
            dist.items.forEach { put(it.id, it.quantitySent.toString()) }
        }
    }
    var confirmNote by rememberSaveable(dist.id) { mutableStateOf("") }
    var noteError by rememberSaveable(dist.id) { mutableStateOf<String?>(null) }

    val parsedPairs = dist.items.map { item ->
        item.quantitySent to (qtyMap[item.id]?.toIntOrNull() ?: 0)
    }
    val previewStatus = resolveConfirmationStatus(parsedPairs)
    val totalReceivedPreview = parsedPairs.sumOf { it.second }
    val discrepancyCount = parsedPairs.count { (sent, rec) -> sent != rec }
    val needsDiscrepancyNote = previewStatus == DistributionStatus.PARTIALLY_RECEIVED ||
        previewStatus == DistributionStatus.NOT_RECEIVED

    val (summaryBg, summaryBorder, summaryTint) = when (previewStatus) {
        DistributionStatus.RECEIVED -> Triple(
            SBColor.Success_100.copy(alpha = 0.65f),
            SBColor.Success_300,
            SBColor.Success_600
        )
        DistributionStatus.PARTIALLY_RECEIVED -> Triple(
            SBColor.Warning_100.copy(alpha = 0.75f),
            SBColor.Secondary_200,
            SBColor.Warning_600
        )
        else -> Triple(
            SBColor.Error_100.copy(alpha = 0.75f),
            SBColor.Primary_200,
            SBColor.Primary_550
        )
    }

    val ctaText = when (previewStatus) {
        DistributionStatus.RECEIVED -> "Konfirmasi terima lengkap (${dist.items.size} bahan)"
        DistributionStatus.PARTIALLY_RECEIVED -> "Konfirmasi sebagian ($discrepancyCount selisih)"
        else -> "Tandai tidak diterima"
    }

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
                .imePadding()
                .pointerInput(Unit) { detectTapGestures(onTap = { focusManager.clearFocus() }) }
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .padding(bottom = 14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier.weight(1f).padding(end = 10.dp),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Text(
                        text = "Konfirmasi penerimaan",
                        fontSize = 19.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = SBColor.Grey_700
                    )
                    Text(
                        text = "${dist.type.label} • ${dist.timeLabel} • ${dist.createdByName.ifBlank { "Gudang" }}",
                        fontSize = 12.sp,
                        color = SBColor.Grey_500,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                DistributionStatusBadge(status = previewStatus)
            }

            HorizontalDivider(thickness = 1.dp, color = SBColor.Grey_100)

            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item(key = "confirm_summary_card", contentType = "summary_card") {
                    OutlinedCard(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.outlinedCardColors(containerColor = summaryBg),
                        border = BorderStroke(1.dp, summaryBorder)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                    Text(
                                        text = "Total diterima",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = summaryTint
                                    )
                                    Text(
                                        text = "$totalReceivedPreview / ${dist.totalSent} unit",
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = SBColor.Grey_700
                                    )
                                }
                                Text(
                                    text = if (discrepancyCount == 0) "Semua sesuai" else "$discrepancyCount bahan selisih",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = summaryTint
                                )
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedButton(
                                    onClick = {
                                        dist.items.forEach { qtyMap[it.id] = it.quantitySent.toString() }
                                        noteError = null
                                    },
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(40.dp),
                                    shape = RoundedCornerShape(10.dp),
                                    border = BorderStroke(
                                        width = if (previewStatus == DistributionStatus.RECEIVED) 1.5.dp else 1.dp,
                                        color = SBColor.Success_400
                                    ),
                                    colors = ButtonDefaults.outlinedButtonColors(containerColor = Color.White),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 0.dp)
                                ) {
                                    Text(
                                        text = "Terima lengkap",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = SBColor.Success_600
                                    )
                                }
                                OutlinedButton(
                                    onClick = {
                                        dist.items.forEach { qtyMap[it.id] = "0" }
                                        noteError = null
                                    },
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(40.dp),
                                    shape = RoundedCornerShape(10.dp),
                                    border = BorderStroke(
                                        width = if (previewStatus == DistributionStatus.NOT_RECEIVED) 1.5.dp else 1.dp,
                                        color = SBColor.Primary_300
                                    ),
                                    colors = ButtonDefaults.outlinedButtonColors(containerColor = Color.White),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 0.dp)
                                ) {
                                    Text(
                                        text = "Tidak diterima (0)",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = SBColor.Primary_550
                                    )
                                }
                            }
                        }
                    }
                }

                item(key = "confirm_items_header", contentType = "section_header") {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Cek rincian bahan (${dist.items.size})",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = SBColor.Grey_700
                        )
                        Text(
                            text = "Ubah jika ada selisih",
                            fontSize = 12.sp,
                            color = SBColor.Grey_500
                        )
                    }
                }

                items(
                    items = dist.items,
                    key = { it.id },
                    contentType = { "confirm_receipt_item" }
                ) { item ->
                    val currentStr = qtyMap[item.id].orEmpty()
                    val currentQty = currentStr.toIntOrNull() ?: 0
                    val diff = item.quantitySent - currentQty
                    val (badgeBg, badgeColor, badgeText) = when {
                        diff == 0 -> Triple(SBColor.Success_100, SBColor.Success_600, "Sesuai")
                        currentQty == 0 -> Triple(SBColor.Error_100, SBColor.Primary_550, "Kosong")
                        diff > 0 -> Triple(SBColor.Warning_100, SBColor.Warning_600, "Kurang $diff")
                        else -> Triple(SBColor.Secondary_100, SBColor.Secondary_500, "Lebih ${-diff}")
                    }

                    OutlinedCard(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.outlinedCardColors(
                            containerColor = if (diff != 0) SBColor.Warning_100.copy(alpha = 0.25f) else Color.White
                        ),
                        border = BorderStroke(
                            width = 1.dp,
                            color = if (diff != 0) SBColor.Secondary_200 else SBColor.Grey_100
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.Top
                            ) {
                                Column(
                                    modifier = Modifier.weight(1f).padding(end = 8.dp),
                                    verticalArrangement = Arrangement.spacedBy(2.dp)
                                ) {
                                    Text(
                                        text = item.materialName,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = SBColor.Grey_700,
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = "Dikirim: ${item.quantitySent} ${item.unitLabel}",
                                        fontSize = 12.sp,
                                        color = SBColor.Grey_500
                                    )
                                    if (!item.note.isNullOrBlank()) {
                                        Text(
                                            text = "Info: ${item.note}",
                                            fontSize = 11.sp,
                                            color = SBColor.Grey_500
                                        )
                                    }
                                }
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = badgeBg
                                ) {
                                    Text(
                                        text = badgeText,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = badgeColor,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }

                            HorizontalDivider(thickness = 1.dp, color = SBColor.Grey_100)

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                if (diff != 0) {
                                    Surface(
                                        onClick = {
                                            qtyMap[item.id] = item.quantitySent.toString()
                                            noteError = null
                                        },
                                        shape = RoundedCornerShape(8.dp),
                                        color = SBColor.Success_100
                                    ) {
                                        Text(
                                            text = "Isi penuh (${item.quantitySent})",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = SBColor.Success_600,
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp)
                                        )
                                    }
                                } else {
                                    Surface(
                                        onClick = {
                                            qtyMap[item.id] = "0"
                                            noteError = null
                                        },
                                        shape = RoundedCornerShape(8.dp),
                                        color = SBColor.Grey_100
                                    ) {
                                        Text(
                                            text = "Set 0",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = SBColor.Grey_600,
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp)
                                        )
                                    }
                                }

                                QuantityStepperField(
                                    value = currentStr,
                                    onValueChange = {
                                        qtyMap[item.id] = it
                                        noteError = null
                                    },
                                    heightDp = 42,
                                    fieldWidthDp = 64
                                )
                            }
                        }
                    }
                }

                item(key = "confirm_note_section", contentType = "note_section") {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = if (needsDiscrepancyNote) "Catatan selisih (wajib diisi)" else "Catatan penerimaan (opsional)",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = SBColor.Grey_700
                        )

                        if (needsDiscrepancyNote) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                QUICK_DISCREPANCY_REASONS.forEach { reason ->
                                    val isChosen = confirmNote.equals(reason, ignoreCase = true)
                                    FilterChip(
                                        selected = isChosen,
                                        onClick = {
                                            confirmNote = reason
                                            noteError = null
                                        },
                                        label = {
                                            Text(reason, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                                        },
                                        shape = RoundedCornerShape(9.dp),
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = SBColor.Primary_100,
                                            selectedLabelColor = SBColor.Primary_550,
                                            containerColor = SBColor.Grey_100,
                                            labelColor = SBColor.Grey_600
                                        ),
                                        border = null
                                    )
                                }
                            }
                        }

                        OutlinedTextField(
                            value = confirmNote,
                            onValueChange = {
                                confirmNote = it
                                noteError = null
                            },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = {
                                Text(
                                    text = if (needsDiscrepancyNote) {
                                        "Jelaskan alasan selisih penerimaan..."
                                    } else {
                                        "Contoh: Stok diterima lengkap dan kondisi baik"
                                    },
                                    fontSize = 14.sp,
                                    color = SBColor.Grey_500
                                )
                            },
                            minLines = 2,
                            maxLines = 3,
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = SBColor.Primary_550,
                                unfocusedBorderColor = SBColor.Grey_200,
                                focusedContainerColor = Color.White,
                                unfocusedContainerColor = Color.White
                            )
                        )

                        noteError?.let { err ->
                            SBInlineAlert(message = err)
                        }
                    }
                }
            }

            SBBottomCtaBar(
                label = ctaText,
                isLoading = isActionLoading,
                onClick = {
                    if (needsDiscrepancyNote && confirmNote.isBlank()) {
                        noteError = "Isi catatan selisih atau pilih alasan cepat di atas."
                        return@SBBottomCtaBar
                    }
                    val parsed = dist.items.associate { item ->
                        item.id to (qtyMap[item.id]?.toIntOrNull() ?: 0).coerceAtLeast(0)
                    }
                    onConfirm(parsed, confirmNote)
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun StockRequestBottomSheet(
    activeMaterials: List<Material>,
    form: StockRequestFormState,
    onDismiss: () -> Unit,
    onUpdateQty: (String, String) -> Unit,
    onUpdateNote: (String) -> Unit,
    onResetForm: () -> Unit,
    onSubmit: () -> Unit
) {
    var materialQuery by rememberSaveable { mutableStateOf("") }

    val filteredMaterials = remember(activeMaterials, materialQuery) {
        val q = materialQuery.trim().lowercase(Locale.ROOT)
        if (q.isBlank()) {
            activeMaterials
        } else {
            activeMaterials.filter { mat ->
                mat.name.lowercase(Locale.ROOT).contains(q) ||
                    mat.unit.firestoreValue.lowercase(Locale.ROOT).contains(q)
            }
        }
    }
    val selectedItemCount = remember(form.itemQuantities) {
        form.itemQuantities.values.count { (it.toIntOrNull() ?: 0) > 0 }
    }
    val totalRequestedUnits = remember(form.itemQuantities) {
        form.itemQuantities.values.sumOf { it.toIntOrNull() ?: 0 }
    }

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
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        text = "Request tambahan stok",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = SBColor.Grey_700
                    )
                    Text(
                        text = if (selectedItemCount > 0) {
                            "$selectedItemCount bahan dipilih • Total $totalRequestedUnits unit"
                        } else {
                            "Pilih bahan mentah dan jumlah yang dibutuhkan"
                        },
                        fontSize = 12.sp,
                        fontWeight = if (selectedItemCount > 0) FontWeight.SemiBold else FontWeight.Normal,
                        color = if (selectedItemCount > 0) SBColor.Primary_550 else SBColor.Grey_500
                    )
                }
                if (selectedItemCount > 0) {
                    Text(
                        text = "Reset",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = SBColor.Primary_550,
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .clickable(enabled = !form.isSaving, onClick = onResetForm)
                            .padding(horizontal = 6.dp, vertical = 4.dp)
                    )
                }
            }

            SBSearchBar(
                query = materialQuery,
                onQueryChange = { materialQuery = it },
                placeholder = "Cari nama bahan mentah..."
            )

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 260.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (filteredMaterials.isEmpty()) {
                    item(key = "req_empty_materials", contentType = "empty_materials") {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 20.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Bahan tidak ditemukan.",
                                fontSize = 13.sp,
                                color = SBColor.Grey_500
                            )
                        }
                    }
                } else {
                    items(
                        items = filteredMaterials,
                        key = { it.id },
                        contentType = { "req_material_item" }
                    ) { mat ->
                        val qtyStr = form.itemQuantities[mat.id].orEmpty()
                        val isSelected = (qtyStr.toIntOrNull() ?: 0) > 0
                        OutlinedCard(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.outlinedCardColors(
                                containerColor = if (isSelected) SBColor.Primary_100.copy(alpha = 0.35f) else Color.White
                            ),
                            border = BorderStroke(
                                width = 1.dp,
                                color = if (isSelected) SBColor.Primary_200 else SBColor.Grey_100
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Column(
                                    modifier = Modifier.weight(1f),
                                    verticalArrangement = Arrangement.spacedBy(2.dp)
                                ) {
                                    Text(
                                        text = mat.name,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = SBColor.Grey_700,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = "Satuan: ${mat.unit.firestoreValue}",
                                        fontSize = 12.sp,
                                        color = SBColor.Grey_500
                                    )
                                }
                                QuantityStepperField(
                                    value = qtyStr,
                                    onValueChange = { onUpdateQty(mat.id, it) }
                                )
                            }
                        }
                    }
                }
            }

            OutlinedTextField(
                value = form.note,
                onValueChange = onUpdateNote,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Alasan permintaan (opsional)") },
                placeholder = { Text("Stok menipis karena pesanan ramai") },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = SBColor.Primary_550,
                    unfocusedBorderColor = SBColor.Grey_200
                )
            )

            form.errorMessage?.let { err ->
                SBInlineAlert(message = err)
            }

            val ctaLabel = if (selectedItemCount > 0) {
                "Kirim permintaan ($selectedItemCount bahan)"
            } else {
                "Kirim permintaan ke Gudang"
            }

            Button(
                onClick = onSubmit,
                enabled = !form.isSaving,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = SBColor.Primary_550)
            ) {
                if (form.isSaving) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp,
                        color = Color.White
                    )
                } else {
                    Text(ctaLabel, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun QuantityStepperField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    heightDp: Int = 38,
    fieldWidthDp: Int = 52
) {
    val current = value.toIntOrNull() ?: 0
    val buttonWidth = (heightDp - 2).coerceAtLeast(36).dp
    val height = heightDp.dp
    Surface(
        modifier = modifier.height(height),
        shape = RoundedCornerShape(10.dp),
        color = Color.White,
        border = BorderStroke(1.dp, SBColor.Grey_200)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(buttonWidth, height)
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
                    .width(fieldWidthDp.dp)
                    .height(height)
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
                    .size(buttonWidth, height)
                    .clickable {
                        onValueChange((current + 1).toString())
                    },
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
