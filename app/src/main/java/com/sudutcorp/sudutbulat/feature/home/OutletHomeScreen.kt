package com.sudutcorp.sudutbulat.feature.outlet

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
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
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
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(top = 24.dp, bottom = 100.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Header: Tanggal, Ucapan & Profil AR
            OutletHeaderMain()

            // Kartu Informasi Outlet Ditugaskan
            AssignedOutletCard()

            // Kartu Utama: Penjualan Sementara & Target (Warna Oranye)
            SalesTargetCard()

            // Bagian Aksi Hari Ini
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "Aksi hari ini",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = SBColor.Grey_700
                )

                // PERBAIKAN: Pemisahan modifier dan verticalArrangement yang benar
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Item 1: Input Penjualan
                    OutletActionItem(
                        iconRes = R.drawable.ic_wallet,
                        iconTint = Color(0xFFD34317),
                        iconBg = Color(0xFFFEE2E2),
                        title = "Input penjualan",
                        subtitle = "Catat penjualan hari ini"
                    )

                    // Item 2: Catat Pengeluaran
                    OutletActionItem(
                        iconRes = R.drawable.ic_wallet,
                        iconTint = Color(0xFFD34317),
                        iconBg = Color(0xFFFEE2E2),
                        title = "Catat pengeluaran",
                        subtitle = "Operasional dan kebutuhan outlet"
                    )

                    // Item 3: Laporkan Stok Rusak
                    OutletActionItem(
                        iconRes = R.drawable.ic_warning,
                        iconTint = Color(0xFFB45309),
                        iconBg = Color(0xFFFEF3C7),
                        title = "Laporkan stok rusak",
                        subtitle = "Belum ada laporan hari ini"
                    )

                    // Item 4: Stok Awal Sudah Dikonfirmasi
                    OutletConfirmedStockItem()
                }
            }
        }
    }
}

@Composable
fun OutletHeaderMain(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(text = "Sabtu, 24 Mei", fontSize = 13.sp, color = SBColor.Grey_500)
            Text(text = "Selamat bekerja, Dimas", fontSize = 24.sp, fontWeight = FontWeight.ExtraBold, color = Color.Black)
        }
        Box {
            Row(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(SBColor.Secondary_100),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "AR", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = SBColor.Primary_500)
            }
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .align(Alignment.BottomEnd)
                    .clip(CircleShape)
                    .background(Color(0xFF10B981))
                    .border(2.dp, Color.White, CircleShape)
            )
        }
    }
}

@Composable
fun AssignedOutletCard(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, SBColor.Grey_100, RoundedCornerShape(16.dp))
            .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFFFEE2E2)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_store),
                contentDescription = "",
                tint = Color(0xFFD34317),
                modifier = Modifier.size(24.dp)
            )
        }
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(text = "Outlet ditugaskan", fontSize = 12.sp, color = SBColor.Grey_400)
            Text(text = "Sudirman", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = SBColor.Grey_700)
            Text(text = "Jl. Jenderal Sudirman No. 18", fontSize = 12.sp, color = SBColor.Grey_500)
        }
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0xFFD1FAE5))
                .padding(horizontal = 12.dp, vertical = 6.dp)
        ) {
            Text(
                text = "Buka",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF065F46)
            )
        }
    }
}

@Composable
fun SalesTargetCard(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFFD34317))
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
            Text(text = "372 produk terjual", fontSize = 12.sp, color = Color.White.copy(alpha = 0.8f))
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
                    .background(Color(0xFFB91C1C)),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "74%", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    Text(text = "target", fontSize = 9.sp, color = Color.White.copy(alpha = 0.8f))
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
    subtitle: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, SBColor.Grey_100, RoundedCornerShape(16.dp))
            .clickable { /* Handle click */ }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(iconBg),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(iconRes),
                contentDescription = "",
                tint = iconTint,
                modifier = Modifier.size(20.dp)
            )
        }
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(text = title, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = SBColor.Grey_700)
            Text(text = subtitle, fontSize = 12.sp, color = SBColor.Grey_400)
        }
        Icon(
            painter = painterResource(R.drawable.ic_chevron_right),
            contentDescription = "",
            modifier = Modifier.size(16.dp),
            tint = SBColor.Grey_300
        )
    }
}

@Composable
fun OutletConfirmedStockItem(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFFF0FDF4))
            .border(1.dp, Color(0xFFBBF7D0), RoundedCornerShape(16.dp))
            .clickable { /* Handle click */ }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(Color(0xFFDCFCE7)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(R.drawable.arrow_up),
                contentDescription = "",
                tint = Color(0xFF16A34A),
                modifier = Modifier.size(20.dp)
            )
        }
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(text = "Stok awal sudah dikonfirmasi", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFF166534))
            Text(text = "1.250 pcs • dikonfirmasi 07.12", fontSize = 12.sp, color = Color(0xFF15803D))
        }
        Icon(
            painter = painterResource(R.drawable.ic_chevron_right),
            contentDescription = "",
            modifier = Modifier.size(16.dp),
            tint = Color(0xFF16A34A)
        )
    }
}

@Preview(showSystemUi = true, showBackground = true)
@Composable
private fun PreviewOutletHome() {
    val navController = rememberNavController()
    Scaffold(
        bottomBar = {
            // SBNavbar(navController = navController)
        },
        modifier = Modifier.fillMaxSize()
    ) { innerPadding ->
        OutletHomeScreen(
            navController = navController,
            modifier = Modifier.padding(innerPadding)
        )
    }
}