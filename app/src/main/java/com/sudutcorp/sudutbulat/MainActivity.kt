package com.sudutcorp.sudutbulat

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.lightColorScheme
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
import com.sudutcorp.sudutbulat.jetpackcompose.navigation_bar.NavbarNavigation
import com.sudutcorp.sudutbulat.ui.theme.SBColor
import com.sudutcorp.sudutbulat.ui.theme.Typography

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme(
                typography = Typography,
                colorScheme = lightColorScheme(
                    primary = SBColor.Primary,
                    background = Color.White
                )
            ) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    NavbarNavigation()
                }
            }
        }
    }
}

//@Preview(showBackground = true, showSystemUi = true, widthDp = 390)
//@Composable
//private fun HomePagePrev() {
//    MaterialTheme(
//        typography = Typography,
//        colorScheme = lightColorScheme(
//            primary = SBColor.Primary,
//            background = Color.White
//        )
//    ) {
//        Scaffold(
//            bottomBar = { BottomNavBar() },
//            floatingActionButton = { AIFab() }
//        ) { innerPadding ->
//            HomePage(modifier = Modifier.padding(innerPadding))
//        }
//    }
//}
//@Preview(showBackground = true, showSystemUi = true, widthDp = 390)
//@Composable
//private fun HomePagePrev() {
//    MaterialTheme(
//        typography = Typography,
//        colorScheme = lightColorScheme(
//            primary = SBColor.Primary,
//            background = Color.White
//        )
//    ) {
//        NavbarNavigation()
//    }
//}

@Composable
fun AIFab() {
    Row(
        modifier = Modifier
            .size(50.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFF1E1E1E))
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
//        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(painterResource(R.drawable.ic_spark), contentDescription = "AI", tint = Color.White, modifier = Modifier.size(20.dp))
//        Text("AI", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
    }
}

@Composable
fun BottomNavBar() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceAround
    ) {
        NavItem(icon = R.drawable.ic_home, label = "Beranda", isSelected = true)
        NavItem(icon = R.drawable.ic_store, label = "Outlet", isSelected = false)
        NavItem(icon = R.drawable.ic_bar_chart, label = "Laporan", isSelected = false)
        NavItem(icon = R.drawable.ic_user, label = "Profil", isSelected = false)
    }
}

@Composable
fun NavItem(icon: Int, label: String, isSelected: Boolean) {
    val color = if (isSelected) SBColor.Primary else SBColor.Grey_400
    val bgColor = if (isSelected) SBColor.Primary_100 else Color.Transparent
    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .background(bgColor)
                .padding(horizontal = 16.dp, vertical = 4.dp)
        ) {
            Icon(painterResource(icon), contentDescription = label, tint = color, modifier = Modifier.size(24.dp))
        }
        Text(label, fontSize = 10.sp, color = color, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal)
    }
}

@Composable
fun HomePage(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
            .padding(top = 32.dp, bottom = 80.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        PageHeaderMain()
        
        Column(
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(SBColor.Primary)
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text("Penjualan hari ini", fontSize = 13.sp, fontWeight = FontWeight.Medium, color = Color.White)
            Text("Rp8.420.000", fontSize = 36.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
            HorizontalDivider(thickness = 1.dp, color = Color.White.copy(alpha = 0.3f), modifier = Modifier.padding(vertical = 4.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Icon(painterResource(R.drawable.arrow_up), "", tint = Color.White, modifier = Modifier.size(10.dp))
                    Text("12,4% dari kemarin", fontSize = 11.sp, fontWeight = FontWeight.Medium, color = Color.White)
                }
                Text("5 outlet aktif", fontSize = 11.sp, fontWeight = FontWeight.Medium, color = Color.White)
            }
        }
        
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .border(1.dp, SBColor.Grey_100, RoundedCornerShape(16.dp))
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xFFF1F5F9)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(painterResource(R.drawable.ic_store), contentDescription = "", modifier = Modifier.size(20.dp), tint = Color(0xFF64748B))
                }
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text("Produk terjual", fontSize = 13.sp, color = SBColor.Grey_500)
                    Text("1.684", fontWeight = FontWeight.Bold, fontSize = 22.sp, color = SBColor.Grey_700)
                    Text("pcs hari ini", fontSize = 11.sp, color = SBColor.Grey_400)
                }
            }
            Column(
                modifier = Modifier
                    .weight(1f)
                    .border(1.dp, SBColor.Grey_100, RoundedCornerShape(16.dp))
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xFFFDF4E1)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(painterResource(R.drawable.ic_wallet), contentDescription = "", modifier = Modifier.size(20.dp), tint = Color(0xFFB45309))
                }
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text("Pengeluaran", fontSize = 13.sp, color = SBColor.Grey_500)
                    Text("Rp940rb", fontWeight = FontWeight.Bold, fontSize = 22.sp, color = SBColor.Grey_700)
                    Text("11 transaksi", fontSize = 11.sp, color = SBColor.Grey_400)
                }
            }
        }
        
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("Performa outlet", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = SBColor.Grey_700)
                Text("Lihat semua", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = SBColor.Primary)
            }
            
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, SBColor.Grey_100, RoundedCornerShape(16.dp))
            ) {
                OutletItem("1", "Sudirman", "496 pcs", "Rp2.480.000", 0.7f)
                HorizontalDivider(thickness = 1.dp, color = SBColor.Grey_100)
                OutletItem("2", "Gajah Mada", "424 pcs", "Rp2.120.000", 0.6f)
                HorizontalDivider(thickness = 1.dp, color = SBColor.Grey_100)
                OutletItem("3", "Cikini", "356 pcs", "Rp1.780.000", 0.5f)
            }
        }
        
        WarningLaporan()
    }
}

//@Preview(showBackground = true, showSystemUi = true, widthDp = 390)
@Composable
private fun WarningLaporanPrev() {
    Column(
        Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        WarningLaporan()
    }
}

@Composable
fun WarningLaporan(modifier: Modifier = Modifier) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(SBColor.Secondary_100)
            .border(1.dp, Color(0xFFD4AF37).copy(alpha = 0.3f), RoundedCornerShape(16.dp))
            .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(painterResource(R.drawable.ic_warning), contentDescription = "Warning", tint = SBColor.Secondary_400, modifier = Modifier.size(24.dp))
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text("2 laporan perlu ditinjau", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.Black)
            Text("Stok rusak dan pengeluaran tidak biasa", fontSize = 12.sp, color = SBColor.Grey_500)
        }
    }
}

@Composable
fun OutletItem(rank: String, name: String, pcs: String, amount: String, progress: Float) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(SBColor.Secondary_100),
            contentAlignment = Alignment.Center
        ) {
            Text(rank, fontWeight = FontWeight.Bold, color = SBColor.Primary_500, fontSize = 12.sp)
        }
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(name, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = SBColor.Grey_700)
                Text(pcs, fontSize = 11.sp, color = SBColor.Grey_400)
            }
            Row(modifier = Modifier
                .fillMaxWidth()
                .height(4.dp)
                .clip(CircleShape)
                .background(SBColor.Grey_100)) {
                Box(modifier = Modifier
                    .fillMaxWidth(progress)
                    .height(4.dp)
                    .background(SBColor.Primary))
            }
        }
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(amount, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = SBColor.Grey_700)
            Icon(painterResource(R.drawable.ic_chevron_right), contentDescription = "", modifier = Modifier.size(16.dp), tint = SBColor.Grey_300)
        }
    }
}

@Composable
fun PageHeaderMain(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("Ringkasan bisnis", fontSize = 14.sp, color = SBColor.Grey_500)
                Text("Selamat pagi, Arif", fontSize = 25.sp, fontWeight = FontWeight.ExtraBold, color = Color.Black)
            }
            Box {
                Row(Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(SBColor.Secondary_100),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text("AR", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = SBColor.Primary_500)
                }
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .align(Alignment.BottomEnd)
                        .clip(CircleShape)
                        .background(Color(0xFF10B981))
                        .border(2.dp, Color.White, CircleShape)
                )
            }
        }
        Row(
            Modifier
                .clip(RoundedCornerShape(9.dp))
                .background(SBColor.Grey_100)
                .padding(12.dp, 6.5.dp)
            ,
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(painterResource(R.drawable.calendar), "", modifier = Modifier.size(16.dp), tint = SBColor.Grey_500)
            Text("Hari ini, 24 Mei 2025", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = SBColor.Grey_500)
        }
    }
}