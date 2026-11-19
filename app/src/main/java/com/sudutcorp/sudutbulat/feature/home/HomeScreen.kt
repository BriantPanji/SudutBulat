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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.sudutcorp.sudutbulat.R
import com.sudutcorp.sudutbulat.core.components.SBMetricCard
import com.sudutcorp.sudutbulat.core.components.SBPageHeader
import com.sudutcorp.sudutbulat.core.ui.theme.SBColor
import com.sudutcorp.sudutbulat.core.util.formatDecimal
import com.sudutcorp.sudutbulat.core.util.formatRupiah
import com.sudutcorp.sudutbulat.core.util.formatShortRupiah
import com.sudutcorp.sudutbulat.feature.owner.OwnerUiState

@Composable
fun HomeScreen(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    uiState: OwnerUiState = OwnerUiState(),
    isDevMode: Boolean = false,
    onSwitchRoleClick: () -> Unit = {},
    onSeeAllOutletsClick: () -> Unit = {},
    onOutletClick: (String) -> Unit = {},
    onReviewReportsClick: () -> Unit = {}
) {
    val firstName = uiState.ownerName.trim().split(Regex("\\s+")).firstOrNull()?.takeIf { it.isNotBlank() } ?: "Arif"
    val topOutlets = remember(uiState.outletItems) { uiState.outletItems.sortedBy { it.rank }.take(3) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
            .padding(top = 16.dp, bottom = 80.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            SBPageHeader(
                eyebrow = if (isDevMode) "Ringkasan bisnis • Mode DEV" else "Ringkasan bisnis",
                title = "Selamat pagi, $firstName",
                initials = if (isDevMode) "DV" else uiState.ownerInitials,
                isAvatarClickable = isDevMode,
                onAvatarClick = onSwitchRoleClick
            )
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
                        painterResource(R.drawable.calendar),
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

        // Hero Revenue Card
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            color = SBColor.Primary
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text("Penjualan hari ini", fontSize = 13.sp, fontWeight = FontWeight.Medium, color = Color.White)
                Text(
                    text = formatRupiah(uiState.todayTotalRevenue),
                    fontSize = 34.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White
                )
                HorizontalDivider(
                    thickness = 1.dp,
                    color = Color.White.copy(alpha = 0.25f),
                    modifier = Modifier.padding(vertical = 4.dp)
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            painterResource(R.drawable.arrow_up),
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(12.dp)
                        )
                        Text(
                            text = uiState.todayDeltaPercentText,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.White
                        )
                    }
                    Text(
                        text = "${uiState.activeOutletsCount} outlet aktif",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.White
                    )
                }
            }
        }

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
                value = formatDecimal(uiState.todayTotalSold),
                subtitle = "pcs hari ini"
            )
            SBMetricCard(
                modifier = Modifier.weight(1f),
                iconRes = R.drawable.ic_wallet,
                iconBgColor = SBColor.Secondary_100,
                iconTint = SBColor.Secondary_500,
                title = "Pengeluaran",
                value = formatShortRupiah(uiState.todayTotalExpenses),
                subtitle = "${uiState.todayExpenseTransactionsCount} transaksi"
            )
        }

        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Performa outlet", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = SBColor.Grey_700)
                TextButton(
                    onClick = onSeeAllOutletsClick,
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)
                ) {
                    Text("Lihat semua", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = SBColor.Primary_550)
                }
            }

            OutlinedCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.outlinedCardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, SBColor.Grey_100)
            ) {
                Column {
                    topOutlets.forEachIndexed { index, outlet ->
                        OutletItem(
                            rank = outlet.rank.toString(),
                            name = outlet.name,
                            pcs = "${outlet.piecesSold} pcs",
                            amount = formatRupiah(outlet.revenue),
                            progress = outlet.targetProgress,
                            onClick = { onOutletClick(outlet.id) }
                        )
                        if (index < topOutlets.lastIndex) {
                            HorizontalDivider(thickness = 1.dp, color = SBColor.Grey_100)
                        }
                    }
                }
            }
        }

        WarningLaporan(
            count = uiState.reviewReportsCount,
            onClick = onReviewReportsClick
        )
    }
}

@Composable
fun WarningLaporan(
    modifier: Modifier = Modifier,
    count: Int = 2,
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
                painterResource(R.drawable.ic_warning),
                contentDescription = "Warning",
                tint = SBColor.Secondary_500,
                modifier = Modifier.size(24.dp)
            )
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text("$count laporan perlu ditinjau", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = SBColor.Grey_700)
                Text("Stok rusak dan pengeluaran tidak biasa", fontSize = 12.sp, color = SBColor.Grey_500)
            }
            Icon(
                painterResource(R.drawable.ic_chevron_right),
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
    pcs: String,
    amount: String,
    progress: Float,
    onClick: () -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Surface(
            modifier = Modifier.size(32.dp),
            shape = RoundedCornerShape(8.dp),
            color = SBColor.Secondary_100
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(rank, fontWeight = FontWeight.Bold, color = SBColor.Primary_500, fontSize = 12.sp)
            }
        }
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(name, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = SBColor.Grey_700)
                Text(pcs, fontSize = 12.sp, color = SBColor.Grey_500)
            }
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
            Text(amount, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = SBColor.Grey_700)
            Icon(
                painterResource(R.drawable.ic_chevron_right),
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = SBColor.Grey_400
            )
        }
    }
}
