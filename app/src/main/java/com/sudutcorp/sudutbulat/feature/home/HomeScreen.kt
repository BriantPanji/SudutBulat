package com.sudutcorp.sudutbulat.feature.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
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
import com.sudutcorp.sudutbulat.core.components.SBSectionCard
import com.sudutcorp.sudutbulat.core.components.SBStatusBadge
import com.sudutcorp.sudutbulat.core.ui.theme.SBColor
import com.sudutcorp.sudutbulat.core.util.formatDecimal
import com.sudutcorp.sudutbulat.core.util.formatRupiah
import com.sudutcorp.sudutbulat.core.util.formatShortRupiah
import com.sudutcorp.sudutbulat.feature.owner.DailyReportListItemUi
import com.sudutcorp.sudutbulat.feature.owner.OutletPerformanceUi
import com.sudutcorp.sudutbulat.feature.owner.OwnerDashboardMetrics
import com.sudutcorp.sudutbulat.feature.owner.OwnerUiState

private const val MAX_DASHBOARD_OUTLETS = 4
private const val MAX_DASHBOARD_REPORTS = 3

@Composable
fun HomeScreen(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    uiState: OwnerUiState = OwnerUiState(),
    isDevMode: Boolean = false,
    onSwitchRoleClick: () -> Unit = {},
    onSeeAllOutletsClick: () -> Unit = {},
    onOutletClick: (String) -> Unit = {},
    onReviewReportsClick: () -> Unit = {},
    onOpenReportsSummaryClick: () -> Unit = {},
    onOpenReportsListClick: () -> Unit = {},
    onReportClick: (String) -> Unit = {},
    onAddOutletClick: () -> Unit = {},
    onAddUserClick: () -> Unit = {},
    onOpenCatalogClick: () -> Unit = {}
) {
    val metrics = uiState.dashboardMetrics
    val firstName = remember(uiState.ownerName) {
        uiState.ownerName.trim().split(Regex("\\s+")).firstOrNull()?.takeIf { it.isNotBlank() } ?: "Owner"
    }
    val topOutlets = remember(uiState.outletItems) {
        uiState.outletItems.filter { it.isActive }.sortedBy { it.rank }.take(MAX_DASHBOARD_OUTLETS)
    }
    val recentReports = remember(uiState.reportListItems) {
        uiState.reportListItems.take(MAX_DASHBOARD_REPORTS)
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
        // 1. Header + Live Date & Outlet Status Chips
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            SBPageHeader(
                eyebrow = if (isDevMode) "Ringkasan bisnis • Mode DEV" else "Ringkasan bisnis",
                title = "${metrics.greetingPrefix}, $firstName",
                initials = if (isDevMode) "DV" else uiState.ownerInitials,
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
                val hasOpenOutlet = metrics.openOutletsCount > 0
                Surface(
                    onClick = onSeeAllOutletsClick,
                    shape = RoundedCornerShape(10.dp),
                    color = if (hasOpenOutlet) SBColor.Success_100 else SBColor.Grey_100
                ) {
                    Text(
                        text = if (metrics.activeOutletsCount == 0) {
                            "0 outlet aktif"
                        } else {
                            "${metrics.openOutletsCount}/${metrics.activeOutletsCount} buka"
                        },
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (hasOpenOutlet) SBColor.Success_600 else SBColor.Grey_600,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp)
                    )
                }
            }
        }

        // 2. Hero Revenue Card (Omzet, Target Progress, Delta vs Kemarin, Cash/QRIS, Closing)
        OwnerHeroRevenueCard(
            metrics = metrics,
            onClick = onOpenReportsSummaryClick
        )

        // 3. Contextual Alert Banner (only shown when reports need review)
        if (metrics.reviewReportsCount > 0) {
            WarningLaporan(
                count = metrics.reviewReportsCount,
                onClick = onReviewReportsClick
            )
        }

        // 4. 2x2 KPI Grid (PRD 3.1: Produk Terjual, Pengeluaran, Bahan Rusak & Est. Rugi, Est. Bersih)
        OwnerKpiGrid(
            metrics = metrics,
            onSoldClick = onOpenReportsSummaryClick,
            onExpenseClick = onOpenReportsSummaryClick,
            onDamagedClick = onOpenReportsListClick,
            onNetClick = onOpenReportsSummaryClick
        )

        // 5. Quick Actions Row (PRD 3.1: Tambah Outlet, Tambah Akun, Katalog)
        OwnerQuickActionsRow(
            onAddOutletClick = onAddOutletClick,
            onAddUserClick = onAddUserClick,
            onOpenCatalogClick = onOpenCatalogClick
        )

        // 6. Warehouse Distribution Overview Today (live from material_distributions)
        OwnerDistributionSummaryCard(metrics = metrics)

        // 7. Outlet Performance Ranking
        OwnerOutletPerformanceSection(
            outlets = topOutlets,
            onSeeAllClick = onSeeAllOutletsClick,
            onOutletClick = onOutletClick
        )

        // 8. Recent Daily Reports
        OwnerRecentReportsSection(
            reports = recentReports,
            onSeeAllClick = onOpenReportsListClick,
            onReportClick = onReportClick
        )
    }
}

@Composable
private fun OwnerHeroRevenueCard(
    metrics: OwnerDashboardMetrics,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isTargetReached = metrics.totalDailyTarget > 0L && metrics.todayTotalRevenue >= metrics.totalDailyTarget
    val pillText = when {
        metrics.totalDailyTarget <= 0L -> "${metrics.activeOutletsCount} outlet"
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
                        text = "Omzet hari ini",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.White.copy(alpha = 0.92f)
                    )
                    Surface(
                        shape = CircleShape,
                        color = if (isTargetReached) Color.White else Color.White.copy(alpha = 0.2f)
                    ) {
                        Text(
                            text = pillText,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isTargetReached) SBColor.Primary_550 else Color.White,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }
                Text(
                    text = formatRupiah(metrics.todayTotalRevenue),
                    fontSize = 32.sp,
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
                        text = "Target ${formatShortRupiah(metrics.totalDailyTarget)}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.White.copy(alpha = 0.88f)
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        if (metrics.todayDeltaTrend == 1 || metrics.todayDeltaTrend == -1) {
                            val rotation = if (metrics.todayDeltaTrend == -1) 180f else 0f
                            Icon(
                                painter = painterResource(R.drawable.arrow_up),
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier
                                    .size(11.dp)
                                    .graphicsLayer { rotationZ = rotation }
                            )
                        }
                        Text(
                            text = metrics.todayDeltaPercentText,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White
                        )
                    }
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
                Text(
                    text = "Tunai ${formatShortRupiah(metrics.todayCashRevenue)} • QRIS ${formatShortRupiah(metrics.todayQrisRevenue)}",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White
                )
                Text(
                    text = "${metrics.todayReportedOutletsCount}/${metrics.activeOutletsCount} closing",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.White.copy(alpha = 0.9f)
                )
            }
        }
    }
}

@Composable
private fun OwnerKpiGrid(
    metrics: OwnerDashboardMetrics,
    onSoldClick: () -> Unit,
    onExpenseClick: () -> Unit,
    onDamagedClick: () -> Unit,
    onNetClick: () -> Unit,
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
                iconRes = R.drawable.ic_store,
                iconBgColor = SBColor.Grey_100,
                iconTint = SBColor.Grey_600,
                title = "Produk terjual",
                value = formatDecimal(metrics.todayTotalSold),
                subtitle = "pcs hari ini",
                onClick = onSoldClick
            )
            SBMetricCard(
                modifier = Modifier.weight(1f),
                iconRes = R.drawable.ic_wallet,
                iconBgColor = SBColor.Secondary_100,
                iconTint = SBColor.Secondary_500,
                title = "Pengeluaran",
                value = formatShortRupiah(metrics.todayTotalExpenses),
                subtitle = "${metrics.todayExpenseTransactionsCount} transaksi",
                onClick = onExpenseClick
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            val hasDamage = metrics.todayDamagedUnits > 0
            SBMetricCard(
                modifier = Modifier.weight(1f),
                iconRes = R.drawable.ic_warning,
                iconBgColor = if (hasDamage) SBColor.Primary_100 else SBColor.Grey_100,
                iconTint = if (hasDamage) SBColor.Primary_550 else SBColor.Grey_600,
                title = "Bahan rusak",
                value = "${formatDecimal(metrics.todayDamagedUnits)} unit",
                subtitle = if (metrics.todayDamagedEstimatedLoss > 0L) {
                    "Rugi ${formatShortRupiah(metrics.todayDamagedEstimatedLoss)}"
                } else {
                    "Tidak ada kerugian"
                },
                onClick = onDamagedClick
            )
            SBMetricCard(
                modifier = Modifier.weight(1f),
                iconRes = R.drawable.ic_bar_chart,
                iconBgColor = SBColor.Success_100,
                iconTint = SBColor.Success_600,
                title = "Estimasi bersih",
                value = formatShortRupiah(metrics.todayNetRevenue),
                subtitle = "Omzet dikurangi biaya",
                onClick = onNetClick
            )
        }
    }
}

@Composable
private fun OwnerQuickActionsRow(
    onAddOutletClick: () -> Unit,
    onAddUserClick: () -> Unit,
    onOpenCatalogClick: () -> Unit,
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
            OwnerQuickActionTile(
                iconRes = R.drawable.ic_store,
                iconBg = SBColor.Primary_100,
                iconTint = SBColor.Primary_550,
                label = "Tambah outlet",
                onClick = onAddOutletClick,
                modifier = Modifier.weight(1f)
            )
            OwnerQuickActionTile(
                iconRes = R.drawable.ic_user,
                iconBg = SBColor.Secondary_100,
                iconTint = SBColor.Secondary_500,
                label = "Tambah akun",
                onClick = onAddUserClick,
                modifier = Modifier.weight(1f)
            )
            OwnerQuickActionTile(
                iconRes = R.drawable.ic_box,
                iconBg = SBColor.Grey_100,
                iconTint = SBColor.Grey_600,
                label = "Katalog",
                onClick = onOpenCatalogClick,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun OwnerQuickActionTile(
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
private fun OwnerDistributionSummaryCard(
    metrics: OwnerDashboardMetrics,
    modifier: Modifier = Modifier
) {
    val routineProgress = if (metrics.activeOutletsCount > 0) {
        (metrics.todayRoutineSentOutletsCount.toFloat() / metrics.activeOutletsCount.toFloat()).coerceIn(0f, 1f)
    } else {
        0f
    }

    SBSectionCard(
        title = "Distribusi gudang hari ini",
        modifier = modifier
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_truck),
                        contentDescription = null,
                        tint = SBColor.Primary_550,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "Distribusi rutin pagi",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = SBColor.Grey_700
                    )
                }
                Text(
                    text = "${metrics.todayRoutineSentOutletsCount}/${metrics.activeOutletsCount} outlet",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = SBColor.Grey_700
                )
            }
            LinearProgressIndicator(
                progress = { routineProgress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(CircleShape),
                color = if (routineProgress >= 1f) SBColor.Success_500 else SBColor.Primary,
                trackColor = SBColor.Grey_100,
                strokeCap = StrokeCap.Round,
                gapSize = 0.dp,
                drawStopIndicator = {}
            )
        }

        HorizontalDivider(thickness = 1.dp, color = SBColor.Grey_100)

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            DistributionStatPill(
                label = "Dalam kirim",
                value = metrics.todayInTransitDistributionsCount.toString(),
                bgColor = SBColor.Grey_100,
                valueColor = SBColor.Grey_700,
                modifier = Modifier.weight(1f)
            )
            DistributionStatPill(
                label = "Request stok",
                value = metrics.pendingAdditionalRequestsCount.toString(),
                bgColor = if (metrics.pendingAdditionalRequestsCount > 0) SBColor.Warning_100 else SBColor.Grey_100,
                valueColor = if (metrics.pendingAdditionalRequestsCount > 0) SBColor.Warning_600 else SBColor.Grey_700,
                modifier = Modifier.weight(1f)
            )
            DistributionStatPill(
                label = "Kendala",
                value = metrics.todayDistributionIssueCount.toString(),
                bgColor = if (metrics.todayDistributionIssueCount > 0) SBColor.Error_100 else SBColor.Grey_100,
                valueColor = if (metrics.todayDistributionIssueCount > 0) SBColor.Primary_550 else SBColor.Grey_700,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun DistributionStatPill(
    label: String,
    value: String,
    bgColor: Color,
    valueColor: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(10.dp),
        color = bgColor
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = value,
                fontSize = 18.sp,
                fontWeight = FontWeight.ExtraBold,
                color = valueColor
            )
            Text(
                text = label,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = SBColor.Grey_600,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun OwnerOutletPerformanceSection(
    outlets: List<OutletPerformanceUi>,
    onSeeAllClick: () -> Unit,
    onOutletClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
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
                text = "Performa outlet",
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
            if (outlets.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Belum ada outlet aktif terdaftar.",
                        fontSize = 13.sp,
                        color = SBColor.Grey_500
                    )
                }
            } else {
                Column {
                    outlets.forEachIndexed { index, outlet ->
                        OutletItem(
                            rank = outlet.rank.toString(),
                            name = outlet.name,
                            subtitle = if (outlet.hasTodayReport) {
                                "${formatDecimal(outlet.piecesSold)} pcs • ${outlet.targetPercentText}"
                            } else {
                                "Belum closing"
                            },
                            isOpen = outlet.isOpen,
                            amount = formatRupiah(outlet.revenue),
                            progress = outlet.targetProgress,
                            onClick = { onOutletClick(outlet.id) }
                        )
                        if (index < outlets.lastIndex) {
                            HorizontalDivider(thickness = 1.dp, color = SBColor.Grey_100)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun OwnerRecentReportsSection(
    reports: List<DailyReportListItemUi>,
    onSeeAllClick: () -> Unit,
    onReportClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    SBSectionCard(
        title = "Laporan terbaru",
        actionText = "Lihat semua",
        onActionClick = onSeeAllClick,
        modifier = modifier
    ) {
        if (reports.isEmpty()) {
            Text(
                text = "Belum ada laporan harian yang masuk.",
                fontSize = 13.sp,
                color = SBColor.Grey_500,
                modifier = Modifier.padding(vertical = 4.dp)
            )
        } else {
            reports.forEachIndexed { index, report ->
                RecentOwnerReportRow(
                    report = report,
                    onClick = { onReportClick(report.id) }
                )
                if (index < reports.lastIndex) {
                    HorizontalDivider(thickness = 1.dp, color = SBColor.Grey_100)
                }
            }
        }
    }
}

@Composable
private fun RecentOwnerReportRow(
    report: DailyReportListItemUi,
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
            modifier = Modifier
                .weight(1f)
                .padding(end = 10.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = report.outletName,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = SBColor.Grey_700,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (report.hasAlert) {
                    SBStatusBadge(
                        text = "Tinjau",
                        containerColor = SBColor.Warning_100,
                        contentColor = SBColor.Warning_600
                    )
                }
            }
            Text(
                text = "${report.dateLabel} • ${formatDecimal(report.totalSold)} pcs",
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
                fontWeight = FontWeight.Bold,
                color = SBColor.Grey_700
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
fun WarningLaporan(
    modifier: Modifier = Modifier,
    count: Int = 0,
    onClick: () -> Unit = {}
) {
    OutlinedCard(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.outlinedCardColors(containerColor = SBColor.Secondary_100),
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
                contentDescription = "Peringatan laporan",
                tint = SBColor.Secondary_500,
                modifier = Modifier.size(24.dp)
            )
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = "$count laporan perlu ditinjau",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = SBColor.Grey_700
                )
                Text(
                    text = "Stok rusak tinggi atau pengeluaran tidak biasa",
                    fontSize = 12.sp,
                    color = SBColor.Grey_500
                )
            }
            Icon(
                painter = painterResource(R.drawable.ic_chevron_right),
                contentDescription = null,
                tint = SBColor.Secondary_500,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

@Composable
fun OutletItem(
    rank: String,
    name: String,
    subtitle: String,
    isOpen: Boolean,
    amount: String,
    progress: Float,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {}
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Surface(
            modifier = Modifier.size(32.dp),
            shape = RoundedCornerShape(8.dp),
            color = SBColor.Secondary_100
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    text = rank,
                    fontWeight = FontWeight.Bold,
                    color = SBColor.Primary_500,
                    fontSize = 12.sp
                )
            }
        }
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = name,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = SBColor.Grey_700,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false)
                )
                Surface(
                    shape = CircleShape,
                    color = if (isOpen) SBColor.Success_100 else SBColor.Grey_100
                ) {
                    Text(
                        text = if (isOpen) "Buka" else "Tutup",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isOpen) SBColor.Success_600 else SBColor.Grey_500,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }
            Text(
                text = subtitle,
                fontSize = 12.sp,
                color = SBColor.Grey_500,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(CircleShape),
                color = SBColor.Primary,
                trackColor = SBColor.Grey_100,
                strokeCap = StrokeCap.Round,
                gapSize = 0.dp,
                drawStopIndicator = {}
            )
        }
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = amount,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                color = SBColor.Grey_700
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
