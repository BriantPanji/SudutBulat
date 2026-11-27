package com.sudutcorp.sudutbulat.feature.home

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.sudutcorp.sudutbulat.R
import com.sudutcorp.sudutbulat.core.components.SBConfirmDialog
import com.sudutcorp.sudutbulat.core.components.SBMetricCard
import com.sudutcorp.sudutbulat.core.components.SBPageHeader
import com.sudutcorp.sudutbulat.core.components.SBSectionCard
import com.sudutcorp.sudutbulat.core.components.SBStatusBadge
import com.sudutcorp.sudutbulat.core.ui.theme.SBColor
import com.sudutcorp.sudutbulat.core.util.buildInitials
import com.sudutcorp.sudutbulat.core.util.formatDecimal
import com.sudutcorp.sudutbulat.core.util.formatRupiah
import com.sudutcorp.sudutbulat.core.util.formatShortRupiah
import com.sudutcorp.sudutbulat.data.model.DailyReport
import com.sudutcorp.sudutbulat.data.model.DistributionType
import com.sudutcorp.sudutbulat.data.model.Outlet
import com.sudutcorp.sudutbulat.feature.distribution.DistributionStatusBadge
import com.sudutcorp.sudutbulat.feature.outlet.OutletDashboardMetrics
import com.sudutcorp.sudutbulat.feature.outlet.OutletDistributionUi
import com.sudutcorp.sudutbulat.feature.outlet.OutletStockFilter
import com.sudutcorp.sudutbulat.feature.outlet.OutletUiEvent
import com.sudutcorp.sudutbulat.feature.outlet.OutletUiState
import java.text.SimpleDateFormat
import java.util.Locale

private const val MAX_DASHBOARD_ITEMS = 3

@Composable
fun OutletHomeScreen(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    uiState: OutletUiState = OutletUiState(),
    onEvent: (OutletUiEvent) -> Unit = {},
    isDevMode: Boolean = false,
    onSwitchRoleClick: () -> Unit = {},
    onOpenStockTab: () -> Unit = {},
    onOpenReportTab: () -> Unit = {}
) {
    val outlet = uiState.assignedOutlet
    val metrics = uiState.dashboardMetrics
    var showToggleOpenDialog by rememberSaveable { mutableStateOf(false) }

    val firstName = remember(uiState.staffName) {
        uiState.staffName.trim().split(Regex("\\s+")).firstOrNull()?.takeIf { it.isNotBlank() } ?: "Staf"
    }
    val initials = if (isDevMode) "DV" else buildInitials(uiState.staffName)
    val todayDistributions = remember(uiState.distributions) {
        uiState.distributions.filter { it.isToday }.take(MAX_DASHBOARD_ITEMS)
    }
    val recentReportsPreview = remember(uiState.recentReports) {
        uiState.recentReports.take(MAX_DASHBOARD_ITEMS)
    }
    val reportDateFmt = remember {
        SimpleDateFormat("EEE, dd MMM yyyy", Locale.forLanguageTag("id-ID"))
    }

    val openStockWithFilter: (OutletStockFilter) -> Unit = { filter ->
        onEvent(OutletUiEvent.SelectStockFilter(filter))
        onOpenStockTab()
    }
    val openStockRequest: () -> Unit = {
        onEvent(OutletUiEvent.SetStockRequestSheetVisible(true))
        onOpenStockTab()
    }
    val openDailyReportContextual: () -> Unit = {
        val today = uiState.todayReport
        if (today != null) {
            onEvent(OutletUiEvent.LoadExistingReportForEdit(today.id))
        } else {
            onEvent(OutletUiEvent.CancelEditDailyReport)
        }
        onOpenReportTab()
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
                eyebrow = if (isDevMode) "Dashboard Outlet • Mode DEV" else "Dashboard Outlet",
                title = "Halo, $firstName",
                initials = initials,
                isAvatarClickable = isDevMode,
                onAvatarClick = onSwitchRoleClick
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (metrics.todayDateLabel.isNotBlank()) {
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
                                text = metrics.todayDateLabel,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = SBColor.Grey_500
                            )
                        }
                    }
                }
                if (outlet != null) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = SBColor.Secondary_100
                    ) {
                        Text(
                            text = "${outlet.openTime} – ${outlet.closeTime}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = SBColor.Secondary_500,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp)
                        )
                    }
                }
            }
        }

        AssignedOutletCard(
            outlet = outlet,
            isToggleEnabled = outlet != null && outlet.isActive && !uiState.isActionLoading,
            onToggleOpenClick = { showToggleOpenDialog = true }
        )

        OutletHeroSalesCard(
            metrics = metrics,
            onClick = openDailyReportContextual
        )

        if (metrics.pendingConfirmCount > 0) {
            OutletPendingConfirmBanner(
                pendingCount = metrics.pendingConfirmCount,
                onClick = { openStockWithFilter(OutletStockFilter.NEED_CONFIRM) }
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            val stockSub = if (metrics.pendingRequestCount > 0) {
                "${metrics.pendingRequestCount} request menunggu"
            } else {
                metrics.morningRoutineStatusLabel
            }
            SBMetricCard(
                modifier = Modifier.weight(1f),
                iconRes = R.drawable.ic_truck,
                iconBgColor = SBColor.Primary_100,
                iconTint = SBColor.Primary_550,
                title = "Stok hari ini",
                value = "${formatDecimal(metrics.todayReceivedUnits)} unit",
                subtitle = stockSub,
                onClick = { openStockWithFilter(OutletStockFilter.ALL) }
            )
            SBMetricCard(
                modifier = Modifier.weight(1f),
                iconRes = R.drawable.ic_wallet,
                iconBgColor = if (metrics.hasTodayReport) SBColor.Success_100 else SBColor.Secondary_100,
                iconTint = if (metrics.hasTodayReport) SBColor.Success_600 else SBColor.Secondary_500,
                title = "Laporan closing",
                value = if (metrics.hasTodayReport) "Terkirim" else "Belum",
                subtitle = if (metrics.hasTodayReport) {
                    "Pukul ${metrics.todayReportTimeLabel ?: "-"}"
                } else {
                    "Tutup pkl ${outlet?.closeTime ?: "21.00"}"
                },
                onClick = openDailyReportContextual
            )
        }

        OutletQuickActionsRow(
            pendingConfirmCount = metrics.pendingConfirmCount,
            hasTodayReport = metrics.hasTodayReport,
            onCheckStockClick = {
                val target = if (metrics.pendingConfirmCount > 0) {
                    OutletStockFilter.NEED_CONFIRM
                } else {
                    OutletStockFilter.ALL
                }
                openStockWithFilter(target)
            },
            onRequestStockClick = openStockRequest,
            onReportClick = openDailyReportContextual
        )

        SBSectionCard(
            title = "Distribusi & request hari ini",
            actionText = "Lihat semua",
            onActionClick = { openStockWithFilter(OutletStockFilter.ALL) }
        ) {
            if (todayDistributions.isEmpty()) {
                Text(
                    text = "Belum ada pengiriman atau request stok hari ini.",
                    fontSize = 13.sp,
                    color = SBColor.Grey_500,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            } else {
                todayDistributions.forEachIndexed { idx, dist ->
                    TodayDistributionCompactRow(
                        dist = dist,
                        onClick = {
                            val filter = when {
                                dist.status == com.sudutcorp.sudutbulat.data.model.DistributionStatus.SENT ->
                                    OutletStockFilter.NEED_CONFIRM
                                dist.status == com.sudutcorp.sudutbulat.data.model.DistributionStatus.DRAFT ->
                                    OutletStockFilter.REQUEST
                                else -> OutletStockFilter.ALL
                            }
                            openStockWithFilter(filter)
                        }
                    )
                    if (idx < todayDistributions.lastIndex) {
                        HorizontalDivider(thickness = 1.dp, color = SBColor.Grey_100)
                    }
                }
            }
        }

        SBSectionCard(
            title = "Riwayat laporan harian",
            actionText = if (metrics.hasTodayReport) "Edit hari ini" else "Input baru",
            onActionClick = openDailyReportContextual
        ) {
            if (recentReportsPreview.isEmpty()) {
                Text(
                    text = "Belum ada riwayat laporan harian di outlet ini.",
                    fontSize = 13.sp,
                    color = SBColor.Grey_500,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            } else {
                recentReportsPreview.forEachIndexed { idx, report ->
                    val isTodayItem = uiState.todayReport?.id == report.id
                    RecentDailyReportRow(
                        report = report,
                        dateLabel = if (isTodayItem) "Hari ini" else reportDateFmt.format(report.date.toDate()),
                        onClick = {
                            onEvent(OutletUiEvent.LoadExistingReportForEdit(report.id))
                            onOpenReportTab()
                        }
                    )
                    if (idx < recentReportsPreview.lastIndex) {
                        HorizontalDivider(thickness = 1.dp, color = SBColor.Grey_100)
                    }
                }
            }
        }
    }

    if (showToggleOpenDialog && outlet != null) {
        val nextOpen = !outlet.isOpen
        val displayName = if (outlet.name.startsWith("Outlet", ignoreCase = true)) {
            outlet.name
        } else {
            "Outlet ${outlet.name}"
        }
        SBConfirmDialog(
            title = if (nextOpen) "Buka operasional outlet?" else "Tutup operasional outlet?",
            description = if (nextOpen) {
                "$displayName akan ditandai sedang Buka untuk operasional hari ini."
            } else {
                "$displayName akan ditandai Tutup. Pastikan laporan harian sudah dikirim sebelum selesai bertugas."
            },
            confirmText = if (nextOpen) "Ya, Buka" else "Ya, Tutup",
            cancelText = "Batal",
            iconRes = R.drawable.ic_store,
            iconBgColor = if (nextOpen) SBColor.Success_100 else SBColor.Secondary_100,
            iconTint = if (nextOpen) SBColor.Success_600 else SBColor.Secondary_500,
            onDismiss = { showToggleOpenDialog = false },
            onConfirm = {
                showToggleOpenDialog = false
                onEvent(OutletUiEvent.ToggleAssignedOutletOpen)
            }
        )
    }
}

@Composable
fun AssignedOutletCard(
    outlet: Outlet?,
    isToggleEnabled: Boolean,
    onToggleOpenClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val outletTitle = outlet?.let {
        if (it.name.startsWith("Outlet", ignoreCase = true)) it.name else "Outlet ${it.name}"
    } ?: "Belum ditugaskan"
    val addressText = outlet?.address?.ifBlank { "Alamat belum diatur" }
        ?: "Hubungi Owner untuk penugasan outlet"
    val isActive = outlet?.isActive ?: false
    val isOpen = outlet?.isOpen ?: false

    OutlinedCard(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.outlinedCardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, SBColor.Grey_100)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(44.dp),
                shape = RoundedCornerShape(12.dp),
                color = SBColor.Primary_100
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        painter = painterResource(R.drawable.ic_store),
                        contentDescription = null,
                        tint = SBColor.Primary_550,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = "Outlet ditugaskan",
                    fontSize = 12.sp,
                    color = SBColor.Grey_500
                )
                Text(
                    text = outletTitle,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = SBColor.Grey_700,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = addressText,
                    fontSize = 12.sp,
                    color = SBColor.Grey_500,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
            if (outlet != null) {
                if (!isActive) {
                    SBStatusBadge(
                        text = "Nonaktif",
                        containerColor = SBColor.Error_100,
                        contentColor = SBColor.Primary_550
                    )
                } else {
                    SBStatusBadge(
                        text = if (isOpen) "Buka ▾" else "Tutup ▾",
                        containerColor = if (isOpen) SBColor.Success_100 else SBColor.Grey_100,
                        contentColor = if (isOpen) SBColor.Success_600 else SBColor.Grey_600,
                        onClick = if (isToggleEnabled) onToggleOpenClick else null
                    )
                }
            }
        }
    }
}

@Composable
private fun OutletHeroSalesCard(
    metrics: OutletDashboardMetrics,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val remainingTarget = (metrics.dailyTarget - metrics.todayRevenue).coerceAtLeast(0L)
    val isTargetReached = metrics.hasTodayReport && metrics.todayRevenue >= metrics.dailyTarget

    val statusPillText = when {
        !metrics.hasTodayReport -> "Belum closing"
        isTargetReached -> "Target tercapai"
        else -> "${metrics.targetPercent}% target"
    }

    Surface(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = SBColor.Primary
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Penjualan hari ini",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.White.copy(alpha = 0.92f)
                    )
                    Surface(
                        shape = CircleShape,
                        color = if (isTargetReached) Color.White else Color.White.copy(alpha = 0.2f)
                    ) {
                        Text(
                            text = statusPillText,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isTargetReached) SBColor.Primary_550 else Color.White,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }

                Text(
                    text = formatRupiah(metrics.todayRevenue),
                    fontSize = 34.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Target ${formatRupiah(metrics.dailyTarget)}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.White.copy(alpha = 0.88f)
                    )
                    Text(
                        text = when {
                            !metrics.hasTodayReport -> "0%"
                            isTargetReached -> "Melampaui target"
                            else -> "Kurang ${formatShortRupiah(remainingTarget)}"
                        },
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                LinearProgressIndicator(
                    progress = { metrics.targetProgress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(7.dp)
                        .clip(CircleShape),
                    color = Color.White,
                    trackColor = Color.White.copy(alpha = 0.24f),
                    strokeCap = StrokeCap.Round,
                    gapSize = 0.dp,
                    drawStopIndicator = {}
                )
            }

            HorizontalDivider(
                thickness = 1.dp,
                color = Color.White.copy(alpha = 0.22f)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (metrics.hasTodayReport) {
                    Text(
                        text = "Tunai ${formatShortRupiah(metrics.todayCashRevenue)} • QRIS ${formatShortRupiah(metrics.todayQrisRevenue)}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White
                    )
                    Text(
                        text = "${formatDecimal(metrics.todaySoldPieces)} pcs terjual",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.White.copy(alpha = 0.9f)
                    )
                } else {
                    Text(
                        text = "Ketuk untuk input closing",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White
                    )
                    if (metrics.lastReportRevenue > 0L) {
                        val datePrefix = metrics.lastReportDateLabel?.let { "Terakhir ($it)" } ?: "Terakhir"
                        Text(
                            text = "$datePrefix: ${formatShortRupiah(metrics.lastReportRevenue)}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.White.copy(alpha = 0.9f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun OutletPendingConfirmBanner(
    pendingCount: Int,
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
                    text = "Ketuk untuk cek jumlah bahan & konfirmasi penerimaan",
                    fontSize = 12.sp,
                    color = SBColor.Warning_600.copy(alpha = 0.85f)
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
private fun OutletQuickActionsRow(
    pendingConfirmCount: Int,
    hasTodayReport: Boolean,
    onCheckStockClick: () -> Unit,
    onRequestStockClick: () -> Unit,
    onReportClick: () -> Unit,
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
            OutletQuickActionTile(
                iconRes = R.drawable.ic_truck,
                iconBg = if (pendingConfirmCount > 0) SBColor.Warning_100 else SBColor.Primary_100,
                iconTint = if (pendingConfirmCount > 0) SBColor.Warning_600 else SBColor.Primary_550,
                label = if (pendingConfirmCount > 0) "Konfirmasi ($pendingConfirmCount)" else "Cek stok",
                onClick = onCheckStockClick,
                modifier = Modifier.weight(1f)
            )
            OutletQuickActionTile(
                iconRes = R.drawable.ic_box,
                iconBg = SBColor.Secondary_100,
                iconTint = SBColor.Secondary_500,
                label = "Minta stok",
                onClick = onRequestStockClick,
                modifier = Modifier.weight(1f)
            )
            OutletQuickActionTile(
                iconRes = R.drawable.ic_bar_chart,
                iconBg = if (hasTodayReport) SBColor.Success_100 else SBColor.Grey_100,
                iconTint = if (hasTodayReport) SBColor.Success_600 else SBColor.Grey_600,
                label = if (hasTodayReport) "Edit laporan" else "Input closing",
                onClick = onReportClick,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun OutletQuickActionTile(
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
                .padding(vertical = 14.dp, horizontal = 8.dp),
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
private fun TodayDistributionCompactRow(
    dist: OutletDistributionUi,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier.weight(1f).padding(end = 10.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
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
                    color = SBColor.Grey_700
                )
            }
            val itemPreview = dist.items.take(2).joinToString(", ") { it.materialName }
            val extra = if (dist.items.size > 2) " +${dist.items.size - 2}" else ""
            Text(
                text = "$itemPreview$extra • ${dist.totalSent} unit",
                fontSize = 12.sp,
                color = SBColor.Grey_500,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        DistributionStatusBadge(status = dist.status)
    }
}

@Composable
private fun RecentDailyReportRow(
    report: DailyReport,
    dateLabel: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier.weight(1f).padding(end = 10.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = dateLabel,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = SBColor.Grey_700
            )
            Text(
                text = "${formatDecimal(report.totalSold)} pcs • Tunai ${formatShortRupiah(report.cashRevenue)} • QRIS ${formatShortRupiah(report.qrisRevenue)}",
                fontSize = 12.sp,
                color = SBColor.Grey_500,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = formatRupiah(report.totalRevenue),
                fontSize = 13.sp,
                fontWeight = FontWeight.ExtraBold,
                color = SBColor.Grey_700
            )
            Icon(
                painter = painterResource(R.drawable.ic_chevron_right),
                contentDescription = null,
                tint = SBColor.Grey_400,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}
