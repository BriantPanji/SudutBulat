package com.sudutcorp.sudutbulat.feature.home

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.RadioButton
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
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.sudutcorp.sudutbulat.R
import com.sudutcorp.sudutbulat.core.components.SBMetricCard
import com.sudutcorp.sudutbulat.core.components.SBPageHeader
import com.sudutcorp.sudutbulat.core.components.SBStatusBadge
import com.sudutcorp.sudutbulat.core.ui.theme.SBColor
import com.sudutcorp.sudutbulat.core.util.formatDecimal
import com.sudutcorp.sudutbulat.data.model.DistributionType
import com.sudutcorp.sudutbulat.data.model.Outlet
import com.sudutcorp.sudutbulat.data.model.Role
import com.sudutcorp.sudutbulat.feature.distribution.AdditionalDistributionSubTab
import com.sudutcorp.sudutbulat.feature.distribution.ApproveDistributionMethodDialog
import com.sudutcorp.sudutbulat.feature.distribution.DistributionCardRow
import com.sudutcorp.sudutbulat.feature.distribution.DistributionCardUi
import com.sudutcorp.sudutbulat.feature.distribution.DistributionDetailBottomSheet
import com.sudutcorp.sudutbulat.feature.distribution.DistributionStatusBadge
import com.sudutcorp.sudutbulat.feature.distribution.DistributionStatusFilter
import com.sudutcorp.sudutbulat.feature.distribution.OutletPresetListBottomSheet
import com.sudutcorp.sudutbulat.feature.distribution.RejectDistributionDialog
import com.sudutcorp.sudutbulat.feature.distribution.RoutinePresetEditorBottomSheet
import com.sudutcorp.sudutbulat.feature.distribution.WarehouseDashboardMetrics
import com.sudutcorp.sudutbulat.feature.distribution.WarehouseDistributionTab
import com.sudutcorp.sudutbulat.feature.distribution.WarehouseUiEffect
import com.sudutcorp.sudutbulat.feature.distribution.WarehouseUiEvent
import com.sudutcorp.sudutbulat.feature.distribution.WarehouseUiState
import com.sudutcorp.sudutbulat.navigation.OwnerSubRoutes
import com.sudutcorp.sudutbulat.navigation.navigateSingle
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow

private const val MAX_DASHBOARD_REQUESTS = 3
private const val MAX_DASHBOARD_PENDING_OUTLETS = 4
private const val MAX_DASHBOARD_RECENT_ACTIVITY = 3

@Composable
fun WarehouseHomeScreen(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    uiState: WarehouseUiState = WarehouseUiState(),
    effects: Flow<WarehouseUiEffect> = emptyFlow(),
    onEvent: (WarehouseUiEvent) -> Unit = {},
    isDevMode: Boolean = false,
    onSwitchRoleClick: () -> Unit = {},
    onOpenCatalogTab: () -> Unit = {},
    onOpenDistributionTab: () -> Unit = {}
) {
    var showOutletPresetListSheet by rememberSaveable { mutableStateOf(false) }
    var editingPresetOutletId by rememberSaveable { mutableStateOf<String?>(null) }
    var detailDistributionId by rememberSaveable { mutableStateOf<String?>(null) }
    var approvingDistributionId by rememberSaveable { mutableStateOf<String?>(null) }
    var rejectingDistributionId by rememberSaveable { mutableStateOf<String?>(null) }

    LaunchedEffect(effects) {
        effects.collect { effect ->
            if (effect is WarehouseUiEffect.RoutinePresetSaved) {
                editingPresetOutletId = null
            }
        }
    }

    val firstName = remember(uiState.adminName) {
        uiState.adminName.trim().split(Regex("\\s+")).firstOrNull()?.takeIf { it.isNotBlank() } ?: "Gudang"
    }
    val activeMaterialIds = remember(uiState.activeMaterials) {
        uiState.activeMaterials.map { it.id }.toSet()
    }
    val openDistributionWithFilter: (WarehouseDistributionTab, DistributionStatusFilter) -> Unit = remember(
        onEvent,
        onOpenDistributionTab
    ) {
        { tab, statusFilter ->
            onEvent(WarehouseUiEvent.SelectDistributionTab(tab))
            onEvent(WarehouseUiEvent.SelectStatusFilter(tab, statusFilter))
            onOpenDistributionTab()
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
            .padding(top = 16.dp, bottom = 84.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            SBPageHeader(
                eyebrow = if (isDevMode) "Dashboard Gudang • Mode DEV" else "Dashboard Gudang",
                title = "Halo, $firstName",
                initials = if (isDevMode) "DV" else uiState.adminInitials,
                isAvatarClickable = isDevMode,
                onAvatarClick = onSwitchRoleClick
            )
            if (uiState.todayDateLabel.isNotBlank()) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = SBColor.Grey_100
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.calendar),
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = SBColor.Grey_500
                        )
                        Text(
                            text = uiState.todayDateLabel,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = SBColor.Grey_500
                        )
                    }
                }
            }
        }

        WarehouseHeroProgressCard(
            completedCount = uiState.outletsCompletedMorningCount,
            totalActiveOutlets = uiState.activeOutlets.size,
            pendingOutletsCount = uiState.outletsPendingMorning.size,
            metrics = uiState.dashboardMetrics
        )

        WarehouseKpiGrid(
            metrics = uiState.dashboardMetrics,
            pendingRequestsCount = uiState.pendingRequests.size,
            activeMaterialsCount = uiState.activeMaterials.size,
            totalActiveOutlets = uiState.activeOutlets.size,
            onInTransitClick = {
                openDistributionWithFilter(WarehouseDistributionTab.ROUTINE, DistributionStatusFilter.SENT)
            },
            onPendingRequestsClick = {
                onEvent(WarehouseUiEvent.SelectAdditionalSubTab(AdditionalDistributionSubTab.UNCONFIRMED))
                openDistributionWithFilter(WarehouseDistributionTab.ADDITIONAL, DistributionStatusFilter.ALL)
            },
            onConfirmedClick = {
                openDistributionWithFilter(WarehouseDistributionTab.ROUTINE, DistributionStatusFilter.RECEIVED)
            },
            onCatalogClick = onOpenCatalogTab
        )

        if (uiState.todayIssueCards.isNotEmpty()) {
            val firstIssue = uiState.todayIssueCards.first()
            WarehouseIssueBanner(
                issueCount = uiState.todayIssueCards.size,
                onClick = {
                    if (uiState.todayIssueCards.size == 1) {
                        detailDistributionId = firstIssue.id
                    } else {
                        val targetTab = if (firstIssue.type == DistributionType.ADDITIONAL) {
                            WarehouseDistributionTab.ADDITIONAL
                        } else {
                            WarehouseDistributionTab.ROUTINE
                        }
                        openDistributionWithFilter(targetTab, DistributionStatusFilter.ALL)
                    }
                }
            )
        }

        WarehouseQuickActionsRow(
            onCreateDistribution = {
                navController.navigateSingle(
                    route = OwnerSubRoutes.CreateDistribution,
                    beforeNavigate = { onEvent(WarehouseUiEvent.InitCreateForm(null)) }
                )
            },
            onManagePresets = { showOutletPresetListSheet = true },
            onOpenCatalog = onOpenCatalogTab
        )

        WarehousePendingRequestsSection(
            pendingRequests = uiState.pendingRequests,
            isActionLoading = uiState.isActionLoading,
            onSeeAllClick = {
                onEvent(WarehouseUiEvent.SelectAdditionalSubTab(AdditionalDistributionSubTab.UNCONFIRMED))
                openDistributionWithFilter(WarehouseDistributionTab.ADDITIONAL, DistributionStatusFilter.ALL)
            },
            onCardClick = { detailDistributionId = it },
            onApprove = { approvingDistributionId = it },
            onRejectClick = { rejectingDistributionId = it }
        )

        if (uiState.outletsPendingMorning.isNotEmpty()) {
            WarehousePendingOutletsSection(
                outlets = uiState.outletsPendingMorning,
                activeMaterialIds = activeMaterialIds,
                onSeeAllClick = {
                    openDistributionWithFilter(WarehouseDistributionTab.ROUTINE, DistributionStatusFilter.NOT_SENT)
                },
                onSendOutletClick = { outletId ->
                    navController.navigateSingle(
                        route = OwnerSubRoutes.CreateDistribution,
                        beforeNavigate = { onEvent(WarehouseUiEvent.InitCreateForm(outletId)) }
                    )
                }
            )
        }

        WarehouseRecentActivitySection(
            recentCards = uiState.todayRecentCards,
            onHistoryClick = {
                openDistributionWithFilter(WarehouseDistributionTab.HISTORY, DistributionStatusFilter.ALL)
            },
            onCardClick = { detailDistributionId = it }
        )
    }

    if (showOutletPresetListSheet) {
        OutletPresetListBottomSheet(
            outlets = uiState.activeOutlets,
            onDismiss = { showOutletPresetListSheet = false },
            onSelectOutlet = { outletId ->
                showOutletPresetListSheet = false
                editingPresetOutletId = outletId
            }
        )
    }

    val editingPresetOutlet = editingPresetOutletId?.let { id ->
        uiState.activeOutlets.firstOrNull { it.id == id } ?: uiState.outlets.firstOrNull { it.id == id }
    }
    if (editingPresetOutlet != null) {
        val displayName = if (editingPresetOutlet.name.startsWith("Outlet", ignoreCase = true)) {
            editingPresetOutlet.name
        } else {
            "Outlet ${editingPresetOutlet.name}"
        }
        RoutinePresetEditorBottomSheet(
            outletName = displayName,
            activeMaterials = uiState.activeMaterials,
            initialPreset = editingPresetOutlet.routinePreset,
            isSaving = uiState.isActionLoading,
            onDismiss = { editingPresetOutletId = null },
            onSave = { items ->
                onEvent(WarehouseUiEvent.SaveOutletRoutinePreset(editingPresetOutlet.id, items))
            }
        )
    }

    val detailCard = detailDistributionId?.let { id -> uiState.distributionCards.firstOrNull { it.id == id } }
    if (detailCard != null) {
        DistributionDetailBottomSheet(
            card = detailCard,
            isActionLoading = uiState.isActionLoading,
            onDismiss = { detailDistributionId = null },
            onApprove = {
                detailDistributionId = null
                approvingDistributionId = detailCard.id
            },
            onMarkArrived = {
                onEvent(WarehouseUiEvent.MarkDistributionArrived(detailCard.id))
                detailDistributionId = null
            },
            onReject = {
                detailDistributionId = null
                rejectingDistributionId = detailCard.id
            }
        )
    }

    val approvingCard = approvingDistributionId?.let { id -> uiState.distributionCards.firstOrNull { it.id == id } }
    if (approvingCard != null) {
        ApproveDistributionMethodDialog(
            outletName = approvingCard.outletName,
            initialMethod = approvingCard.deliveryMethod,
            onDismiss = { approvingDistributionId = null },
            onConfirm = { method ->
                onEvent(WarehouseUiEvent.ApproveRequest(approvingCard.id, method))
                approvingDistributionId = null
            }
        )
    }

    val rejectingCard = rejectingDistributionId?.let { id -> uiState.distributionCards.firstOrNull { it.id == id } }
    if (rejectingCard != null) {
        RejectDistributionDialog(
            outletName = rejectingCard.outletName,
            onDismiss = { rejectingDistributionId = null },
            onConfirm = { reason ->
                onEvent(WarehouseUiEvent.RejectRequest(rejectingCard.id, reason))
                rejectingDistributionId = null
            }
        )
    }
}

@Composable
private fun WarehouseHeroProgressCard(
    completedCount: Int,
    totalActiveOutlets: Int,
    pendingOutletsCount: Int,
    metrics: WarehouseDashboardMetrics,
    modifier: Modifier = Modifier
) {
    val progress = if (totalActiveOutlets > 0) {
        (completedCount.toFloat() / totalActiveOutlets.toFloat()).coerceIn(0f, 1f)
    } else {
        0f
    }
    val allDone = totalActiveOutlets > 0 && pendingOutletsCount == 0

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = SBColor.Primary
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "Distribusi rutin pagi hari ini",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.White.copy(alpha = 0.92f)
                    )
                    Row(
                        verticalAlignment = Alignment.Bottom,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "$completedCount / $totalActiveOutlets",
                            fontSize = 32.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White
                        )
                        Text(
                            text = "outlet",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White.copy(alpha = 0.85f),
                            modifier = Modifier.padding(bottom = 5.dp)
                        )
                    }
                }
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.16f)),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(SBColor.Primary_500),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "${metrics.completionPercent}%",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White
                        )
                    }
                }
            }

            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(CircleShape),
                color = Color.White,
                trackColor = Color.White.copy(alpha = 0.25f),
                strokeCap = StrokeCap.Round,
                gapSize = 0.dp,
                drawStopIndicator = {}
            )

            HorizontalDivider(
                thickness = 1.dp,
                color = Color.White.copy(alpha = 0.22f)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (allDone) {
                        "Semua outlet aktif sudah dikirim"
                    } else if (totalActiveOutlets == 0) {
                        "Belum ada outlet aktif"
                    } else {
                        "$pendingOutletsCount outlet belum dikirim"
                    },
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White
                )
                Text(
                    text = "${formatDecimal(metrics.todayTotalItemsSent)} item terkirim",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.White.copy(alpha = 0.9f)
                )
            }
        }
    }
}

@Composable
private fun WarehouseKpiGrid(
    metrics: WarehouseDashboardMetrics,
    pendingRequestsCount: Int,
    activeMaterialsCount: Int,
    totalActiveOutlets: Int,
    onInTransitClick: () -> Unit,
    onPendingRequestsClick: () -> Unit,
    onConfirmedClick: () -> Unit,
    onCatalogClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            SBMetricCard(
                modifier = Modifier.weight(1f),
                iconRes = R.drawable.ic_truck,
                iconBgColor = SBColor.Primary_100,
                iconTint = SBColor.Primary_550,
                title = "Dalam pengiriman",
                value = metrics.todayInTransitCount.toString(),
                subtitle = "menunggu konfirmasi",
                onClick = onInTransitClick
            )
            SBMetricCard(
                modifier = Modifier.weight(1f),
                iconRes = R.drawable.ic_bell,
                iconBgColor = SBColor.Warning_100,
                iconTint = SBColor.Warning_600,
                title = "Permintaan stok",
                value = pendingRequestsCount.toString(),
                subtitle = "menunggu persetujuan",
                onClick = onPendingRequestsClick
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            SBMetricCard(
                modifier = Modifier.weight(1f),
                iconRes = R.drawable.ic_check,
                iconBgColor = SBColor.Success_100,
                iconTint = SBColor.Success_600,
                title = "Terkonfirmasi",
                value = metrics.todayConfirmedCount.toString(),
                subtitle = "diterima hari ini",
                onClick = onConfirmedClick
            )
            SBMetricCard(
                modifier = Modifier.weight(1f),
                iconRes = R.drawable.ic_box,
                iconBgColor = SBColor.Secondary_100,
                iconTint = SBColor.Secondary_500,
                title = "Katalog & preset",
                value = "$activeMaterialsCount bahan",
                subtitle = "${metrics.outletsWithPresetCount}/$totalActiveOutlets preset siap",
                onClick = onCatalogClick
            )
        }
    }
}

@Composable
private fun WarehouseIssueBanner(
    issueCount: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedCard(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.outlinedCardColors(containerColor = SBColor.Warning_100),
        border = BorderStroke(1.dp, SBColor.Secondary_200)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_warning),
                contentDescription = "Perhatian pengiriman",
                tint = SBColor.Warning_600,
                modifier = Modifier.size(24.dp)
            )
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = "$issueCount pengiriman perlu ditinjau",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = SBColor.Grey_700
                )
                Text(
                    text = "Ada stok diterima sebagian atau tidak diterima outlet",
                    fontSize = 12.sp,
                    color = SBColor.Grey_500
                )
            }
            Icon(
                painter = painterResource(R.drawable.ic_chevron_right),
                contentDescription = null,
                tint = SBColor.Warning_600,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

@Composable
private fun WarehouseQuickActionsRow(
    onCreateDistribution: () -> Unit,
    onManagePresets: () -> Unit,
    onOpenCatalog: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(
            text = "Aksi cepat",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = SBColor.Grey_700
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            QuickActionTile(
                iconRes = R.drawable.ic_truck,
                iconBg = SBColor.Primary_100,
                iconTint = SBColor.Primary_550,
                label = "Kirim stok",
                onClick = onCreateDistribution,
                modifier = Modifier.weight(1f)
            )
            QuickActionTile(
                iconRes = R.drawable.ic_settings,
                iconBg = SBColor.Secondary_100,
                iconTint = SBColor.Secondary_500,
                label = "Atur preset",
                onClick = onManagePresets,
                modifier = Modifier.weight(1f)
            )
            QuickActionTile(
                iconRes = R.drawable.ic_box,
                iconBg = SBColor.Grey_100,
                iconTint = SBColor.Grey_600,
                label = "Katalog",
                onClick = onOpenCatalog,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun QuickActionTile(
    iconRes: Int,
    iconBg: Color,
    iconTint: Color,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedCard(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.outlinedCardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, SBColor.Grey_100)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 14.dp, horizontal = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Surface(
                modifier = Modifier.size(38.dp),
                shape = RoundedCornerShape(10.dp),
                color = iconBg
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        painter = painterResource(iconRes),
                        contentDescription = label,
                        tint = iconTint,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
            Text(
                text = label,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = SBColor.Grey_700,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun WarehousePendingRequestsSection(
    pendingRequests: List<DistributionCardUi>,
    isActionLoading: Boolean,
    onSeeAllClick: () -> Unit,
    onCardClick: (String) -> Unit,
    onApprove: (String) -> Unit,
    onRejectClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val visibleRequests = remember(pendingRequests) { pendingRequests.take(MAX_DASHBOARD_REQUESTS) }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Permintaan tambahan stok",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = SBColor.Grey_700
                )
                if (pendingRequests.isNotEmpty()) {
                    SBStatusBadge(
                        text = pendingRequests.size.toString(),
                        containerColor = SBColor.Warning_100,
                        contentColor = SBColor.Warning_600
                    )
                }
            }
            TextButton(
                onClick = onSeeAllClick,
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)
            ) {
                Text(
                    text = if (pendingRequests.size > MAX_DASHBOARD_REQUESTS) {
                        "Lihat semua (${pendingRequests.size})"
                    } else {
                        "Semua"
                    },
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = SBColor.Primary_550
                )
            }
        }

        if (visibleRequests.isEmpty()) {
            OutlinedCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.outlinedCardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, SBColor.Grey_100)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Tidak ada permintaan tambahan stok yang menunggu.",
                        fontSize = 13.sp,
                        color = SBColor.Grey_500
                    )
                }
            }
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                visibleRequests.forEach { req ->
                    DistributionCardRow(
                        card = req,
                        isActionLoading = isActionLoading,
                        onClick = { onCardClick(req.id) },
                        onApprove = { onApprove(req.id) },
                        onReject = { onRejectClick(req.id) }
                    )
                }
            }
        }
    }
}

@Composable
private fun WarehousePendingOutletsSection(
    outlets: List<Outlet>,
    activeMaterialIds: Set<String>,
    onSeeAllClick: () -> Unit,
    onSendOutletClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val visibleOutlets = remember(outlets) { outlets.take(MAX_DASHBOARD_PENDING_OUTLETS) }
    val hiddenCount = (outlets.size - visibleOutlets.size).coerceAtLeast(0)

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Belum dikirim hari ini",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = SBColor.Grey_700
            )
            TextButton(
                onClick = onSeeAllClick,
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)
            ) {
                Text(
                    text = "Lihat semua",
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
                visibleOutlets.forEachIndexed { idx, outlet ->
                    val hasPreset = outlet.routinePreset.any {
                        it.quantity > 0 && it.materialId in activeMaterialIds
                    }
                    PendingOutletDashboardRow(
                        outlet = outlet,
                        hasPreset = hasPreset,
                        onClick = { onSendOutletClick(outlet.id) }
                    )
                    if (idx < visibleOutlets.lastIndex || hiddenCount > 0) {
                        HorizontalDivider(thickness = 1.dp, color = SBColor.Grey_100)
                    }
                }
                if (hiddenCount > 0) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(onClick = onSeeAllClick)
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "+$hiddenCount outlet lainnya belum dikirim",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = SBColor.Primary_550
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PendingOutletDashboardRow(
    outlet: Outlet,
    hasPreset: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val displayName = if (outlet.name.startsWith("Outlet", ignoreCase = true)) {
        outlet.name
    } else {
        "Outlet ${outlet.name}"
    }
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(16.dp),
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
                    painter = painterResource(R.drawable.ic_store),
                    contentDescription = null,
                    tint = SBColor.Primary_550,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            Text(
                text = displayName,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = SBColor.Grey_700,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(7.dp)
                        .clip(CircleShape)
                        .background(if (outlet.isOpen) SBColor.Success_500 else SBColor.Grey_300)
                )
                Text(
                    text = buildString {
                        append(if (outlet.isOpen) "Buka" else "Tutup")
                        append(" • ")
                        append(if (hasPreset) "Preset siap" else "Manual")
                    },
                    fontSize = 12.sp,
                    color = SBColor.Grey_500,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            SBStatusBadge(
                text = "Kirim",
                containerColor = SBColor.Primary_100,
                contentColor = SBColor.Primary_550
            )
            Icon(
                painter = painterResource(R.drawable.ic_chevron_right),
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = SBColor.Grey_400
            )
        }
    }
}

@Composable
private fun WarehouseRecentActivitySection(
    recentCards: List<DistributionCardUi>,
    onHistoryClick: () -> Unit,
    onCardClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val visibleCards = remember(recentCards) { recentCards.take(MAX_DASHBOARD_RECENT_ACTIVITY) }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Aktivitas hari ini",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = SBColor.Grey_700
            )
            TextButton(
                onClick = onHistoryClick,
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)
            ) {
                Text(
                    text = "Riwayat",
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
            if (visibleCards.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Belum ada distribusi yang dikirim hari ini.",
                        fontSize = 13.sp,
                        color = SBColor.Grey_500
                    )
                }
            } else {
                Column {
                    visibleCards.forEachIndexed { idx, card ->
                        RecentActivityRow(
                            card = card,
                            onClick = { onCardClick(card.id) }
                        )
                        if (idx < visibleCards.lastIndex) {
                            HorizontalDivider(thickness = 1.dp, color = SBColor.Grey_100)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun RecentActivityRow(
    card: DistributionCardUi,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = card.outletName,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = SBColor.Grey_700,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false)
                )
                Text(
                    text = "• ${card.type.label}",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = SBColor.Grey_500
                )
            }
            Text(
                text = card.itemsSummary,
                fontSize = 12.sp,
                color = SBColor.Grey_500,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        Column(
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            DistributionStatusBadge(status = card.status)
            Text(
                text = card.todayTimeLabel,
                fontSize = 11.sp,
                color = SBColor.Grey_400
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WarehouseRoleBottomSheet(
    selectedRole: Role,
    onDismiss: () -> Unit,
    onRoleSelected: (Role) -> Unit
) {
    val sheetState = rememberModalBottomSheetState()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color.White,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 36.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("Lihat sebagai", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = SBColor.Grey_700)
                Text("Berpindah peran untuk meninjau sistem (Mode DEV)", fontSize = 13.sp, color = SBColor.Grey_500)
            }

            RoleOptionItem(
                title = "Owner",
                subtitle = "Pantau bisnis & laporan",
                isSelected = selectedRole == Role.OWNER,
                iconRes = R.drawable.ic_wallet,
                onClick = { onRoleSelected(Role.OWNER) }
            )

            RoleOptionItem(
                title = "Admin Gudang",
                subtitle = "Kelola stok & distribusi",
                isSelected = selectedRole == Role.WAREHOUSE,
                iconRes = R.drawable.ic_truck,
                onClick = { onRoleSelected(Role.WAREHOUSE) }
            )

            RoleOptionItem(
                title = "Staf Outlet",
                subtitle = "Operasional outlet harian",
                isSelected = selectedRole == Role.OUTLET,
                iconRes = R.drawable.ic_store,
                onClick = { onRoleSelected(Role.OUTLET) }
            )
        }
    }
}

@Composable
fun RoleOptionItem(
    title: String,
    subtitle: String,
    isSelected: Boolean,
    iconRes: Int,
    onClick: () -> Unit
) {
    val scale by animateFloatAsState(
        targetValue = if (isSelected) 1.02f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "RoleOptionScale"
    )
    val borderColor by animateColorAsState(
        targetValue = if (isSelected) SBColor.Primary_400 else SBColor.Grey_100,
        label = "RoleOptionBorder"
    )
    val bgColor by animateColorAsState(
        targetValue = if (isSelected) SBColor.Primary_100.copy(alpha = 0.5f) else Color.White,
        label = "RoleOptionBg"
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clip(RoundedCornerShape(16.dp))
            .border(
                width = if (isSelected) 1.5.dp else 1.dp,
                color = borderColor,
                shape = RoundedCornerShape(16.dp)
            )
            .background(bgColor)
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Surface(
            modifier = Modifier.size(44.dp),
            shape = RoundedCornerShape(10.dp),
            color = if (isSelected) SBColor.Primary_100 else SBColor.Secondary_100
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    painterResource(iconRes),
                    contentDescription = null,
                    tint = if (isSelected) SBColor.Primary_550 else SBColor.Secondary_500,
                    modifier = Modifier.size(22.dp)
                )
            }
        }
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(title, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = SBColor.Grey_700)
            Text(subtitle, fontSize = 12.sp, color = SBColor.Grey_500)
        }
        RadioButton(
            selected = isSelected,
            onClick = onClick
        )
    }
}
