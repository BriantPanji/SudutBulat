package com.sudutcorp.sudutbulat.feature.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.sudutcorp.sudutbulat.R
import com.sudutcorp.sudutbulat.core.ui.theme.SBColor
import com.sudutcorp.sudutbulat.core.util.buildInitials
import com.sudutcorp.sudutbulat.core.util.formatRupiah
import com.sudutcorp.sudutbulat.data.model.DistributionStatus
import com.sudutcorp.sudutbulat.feature.outlet.OutletUiEvent
import com.sudutcorp.sudutbulat.feature.outlet.OutletUiState

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
    val firstName = uiState.staffName.trim().split(Regex("\\s+")).firstOrNull()?.takeIf { it.isNotBlank() } ?: "Staf"
    val initials = if (isDevMode) "DV" else buildInitials(uiState.staffName)
    val latestReport = uiState.recentReports.firstOrNull()
    val revenue = outlet?.totalRevenue?.takeIf { it > 0L } ?: latestReport?.totalRevenue ?: 0L
    val pieces = outlet?.totalPiecesSold?.takeIf { it > 0 } ?: latestReport?.totalSold ?: 0
    val dailyTarget = (outlet?.dailyTarget ?: 2_000_000L).coerceAtLeast(1L)
    val targetPct = ((revenue.toFloat() / dailyTarget.toFloat()) * 100f).toInt().coerceIn(0, 100)
    val pendingConfirmCount = uiState.distributions.count { it.status == DistributionStatus.SENT }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
            .padding(top = 32.dp, bottom = 80.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        OutletHeaderMain(
            staffFirstName = firstName,
            initials = initials,
            isDevMode = isDevMode,
            onProfileClick = onSwitchRoleClick
        )

        AssignedOutletCard(
            outletName = outlet?.name ?: "Belum ditugaskan",
            address = outlet?.address ?: "Hubungi Owner untuk penugasan outlet",
            isOpen = outlet?.isOpen ?: true,
            isToggleEnabled = outlet != null && !uiState.isActionLoading,
            onToggleOpenClick = { onEvent(OutletUiEvent.ToggleAssignedOutletOpen) }
        )

        SalesTargetCard(
            revenueText = formatRupiah(revenue),
            piecesText = "$pieces produk terjual",
            targetPercentText = "$targetPct%"
        )

        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(
                text = "Aksi hari ini",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = SBColor.Grey_700
            )

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutletActionItem(
                    iconRes = R.drawable.ic_wallet,
                    iconTint = SBColor.Primary_550,
                    iconBg = SBColor.Primary_100,
                    title = "Input laporan harian",
                    subtitle = "Catat penjualan, pengeluaran & bahan rusak",
                    onClick = onOpenReportTab
                )

                OutletActionItem(
                    iconRes = R.drawable.ic_box,
                    iconTint = SBColor.Warning_600,
                    iconBg = SBColor.Warning_100,
                    title = "Request tambahan stok",
                    subtitle = "Ajukan permintaan stok ke Gudang Pusat",
                    onClick = onOpenStockTab
                )

                OutletConfirmedStockItem(
                    pendingCount = pendingConfirmCount,
                    onClick = onOpenStockTab
                )
            }
        }
    }
}

@Composable
fun OutletHeaderMain(
    staffFirstName: String,
    initials: String,
    modifier: Modifier = Modifier,
    isDevMode: Boolean = false,
    onProfileClick: () -> Unit = {}
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = if (isDevMode) "Staf Outlet • Mode DEV" else "Staf Outlet",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = SBColor.Grey_500
            )
            Text(
                text = "Selamat bekerja, $staffFirstName",
                fontSize = 25.sp,
                fontWeight = FontWeight.ExtraBold,
                color = SBColor.Grey_700
            )
        }
        BadgedBox(
            badge = {
                Badge(
                    containerColor = SBColor.Success_500,
                    modifier = Modifier
                        .size(12.dp)
                        .border(2.dp, Color.White, CircleShape)
                )
            }
        ) {
            Surface(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .clickable(enabled = isDevMode, onClick = onProfileClick),
                shape = CircleShape,
                color = SBColor.Secondary_100
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = initials,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = SBColor.Primary_500
                    )
                }
            }
        }
    }
}

@Composable
fun AssignedOutletCard(
    outletName: String,
    address: String,
    isOpen: Boolean,
    isToggleEnabled: Boolean,
    onToggleOpenClick: () -> Unit,
    modifier: Modifier = Modifier
) {
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
                Text(text = "Outlet ditugaskan", fontSize = 12.sp, color = SBColor.Grey_500)
                Text(text = outletName, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = SBColor.Grey_700)
                Text(text = address, fontSize = 12.sp, color = SBColor.Grey_500)
            }
            Surface(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .clickable(enabled = isToggleEnabled, onClick = onToggleOpenClick),
                shape = RoundedCornerShape(8.dp),
                color = if (isOpen) SBColor.Success_100 else SBColor.Grey_100
            ) {
                Text(
                    text = if (isOpen) "Buka ▾" else "Tutup ▾",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isOpen) SBColor.Success_600 else SBColor.Grey_600,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                )
            }
        }
    }
}

@Composable
fun SalesTargetCard(
    revenueText: String,
    piecesText: String,
    targetPercentText: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = SBColor.Primary
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(text = "Penjualan terakhir", fontSize = 13.sp, fontWeight = FontWeight.Medium, color = Color.White)
                Text(text = revenueText, fontSize = 28.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
                Text(text = piecesText, fontSize = 12.sp, color = Color.White.copy(alpha = 0.85f))
            }

            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .clip(CircleShape)
                        .background(SBColor.Primary_500),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = targetPercentText, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        Text(text = "target", fontSize = 9.sp, color = Color.White.copy(alpha = 0.85f))
                    }
                }
            }
        }
    }
}

@Composable
fun OutletActionItem(
    iconRes: Int,
    iconTint: Color,
    iconBg: Color,
    title: String,
    subtitle: String,
    onClick: () -> Unit = {}
) {
    OutlinedCard(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.outlinedCardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, SBColor.Grey_100)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
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
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(text = title, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = SBColor.Grey_700)
                Text(text = subtitle, fontSize = 12.sp, color = SBColor.Grey_500)
            }
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
fun OutletConfirmedStockItem(
    pendingCount: Int,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {}
) {
    val hasPending = pendingCount > 0
    OutlinedCard(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.outlinedCardColors(
            containerColor = if (hasPending) SBColor.Warning_100 else SBColor.Success_100
        ),
        border = BorderStroke(1.dp, if (hasPending) SBColor.Warning_600.copy(alpha = 0.3f) else SBColor.Success_300)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Surface(
                modifier = Modifier.size(40.dp),
                shape = RoundedCornerShape(10.dp),
                color = Color.White.copy(alpha = 0.7f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        painter = painterResource(if (hasPending) R.drawable.ic_truck else R.drawable.arrow_up),
                        contentDescription = null,
                        tint = if (hasPending) SBColor.Warning_600 else SBColor.Success_600,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = if (hasPending) "$pendingCount pengiriman menunggu konfirmasi" else "Semua pengiriman sudah dikonfirmasi",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = if (hasPending) SBColor.Warning_600 else SBColor.Success_600
                )
                Text(
                    text = if (hasPending) "Ketuk untuk konfirmasi penerimaan stok" else "Lihat riwayat distribusi & request stok",
                    fontSize = 12.sp,
                    color = if (hasPending) SBColor.Warning_600.copy(alpha = 0.85f) else SBColor.Success_500
                )
            }
            Icon(
                painter = painterResource(R.drawable.ic_chevron_right),
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = if (hasPending) SBColor.Warning_600 else SBColor.Success_600
            )
        }
    }
}
