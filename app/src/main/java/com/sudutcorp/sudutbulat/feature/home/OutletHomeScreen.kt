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
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.sudutcorp.sudutbulat.R
import com.sudutcorp.sudutbulat.core.ui.theme.SBColor

@Composable
fun OutletHomeScreen(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    isDevMode: Boolean = false,
    onSwitchRoleClick: () -> Unit = {}
) {
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
            isDevMode = isDevMode,
            onProfileClick = onSwitchRoleClick
        )

        AssignedOutletCard()

        SalesTargetCard()

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
                    title = "Input penjualan",
                    subtitle = "Catat penjualan hari ini"
                )

                OutletActionItem(
                    iconRes = R.drawable.ic_wallet,
                    iconTint = SBColor.Primary_550,
                    iconBg = SBColor.Primary_100,
                    title = "Catat pengeluaran",
                    subtitle = "Operasional dan kebutuhan outlet"
                )

                OutletActionItem(
                    iconRes = R.drawable.ic_warning,
                    iconTint = SBColor.Warning_600,
                    iconBg = SBColor.Warning_100,
                    title = "Laporkan stok rusak",
                    subtitle = "Belum ada laporan hari ini"
                )

                OutletConfirmedStockItem()
            }
        }
    }
}

@Composable
fun OutletHeaderMain(
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
                text = if (isDevMode) "Sabtu, 24 Mei • Mode DEV" else "Sabtu, 24 Mei",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = SBColor.Grey_500
            )
            Text(
                text = "Selamat bekerja, Dimas",
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
                        text = if (isDevMode) "DV" else "DP",
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
fun AssignedOutletCard(modifier: Modifier = Modifier) {
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
                Text(text = "Sudirman", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = SBColor.Grey_700)
                Text(text = "Jl. Jenderal Sudirman No. 18", fontSize = 12.sp, color = SBColor.Grey_500)
            }
            Surface(
                shape = RoundedCornerShape(6.dp),
                color = SBColor.Success_100
            ) {
                Text(
                    text = "Buka",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = SBColor.Success_600,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                )
            }
        }
    }
}

@Composable
fun SalesTargetCard(modifier: Modifier = Modifier) {
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
                Text(text = "Penjualan sementara", fontSize = 13.sp, fontWeight = FontWeight.Medium, color = Color.White)
                Text(text = "Rp1.860.000", fontSize = 30.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
                Text(text = "372 produk terjual", fontSize = 12.sp, color = Color.White.copy(alpha = 0.85f))
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
                        Text(text = "74%", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White)
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
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {}
) {
    OutlinedCard(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.outlinedCardColors(containerColor = SBColor.Success_100),
        border = BorderStroke(1.dp, SBColor.Success_300)
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
                        painter = painterResource(R.drawable.arrow_up),
                        contentDescription = null,
                        tint = SBColor.Success_600,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = "Stok awal sudah dikonfirmasi",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = SBColor.Success_600
                )
                Text(
                    text = "1.250 pcs • dikonfirmasi 07.12",
                    fontSize = 12.sp,
                    color = SBColor.Success_500
                )
            }
            Icon(
                painter = painterResource(R.drawable.ic_chevron_right),
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = SBColor.Success_600
            )
        }
    }
}

@Preview(showSystemUi = true, showBackground = true)
@Composable
private fun PreviewOutletHome() {
    val navController = rememberNavController()
    Scaffold(
        modifier = Modifier.fillMaxSize()
    ) { innerPadding ->
        OutletHomeScreen(
            navController = navController,
            modifier = Modifier.padding(innerPadding)
        )
    }
}
