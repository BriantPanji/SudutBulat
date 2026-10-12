package com.sudutcorp.sudutbulat.jetpackcompose.navigation_bar

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
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
import com.sudutcorp.sudutbulat.OutletItem
import com.sudutcorp.sudutbulat.PageHeaderMain
import com.sudutcorp.sudutbulat.R
import com.sudutcorp.sudutbulat.WarningLaporan
import com.sudutcorp.sudutbulat.ui.theme.SBColor

@Composable
fun NavbarHomeScreen(navController: NavHostController) {
    Column(
        modifier = Modifier
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