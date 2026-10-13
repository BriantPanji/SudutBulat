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
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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

@Composable
fun OutletScreen(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    var selectedFilter by remember { mutableStateOf("Semua") }
    val filterOptions = listOf("Semua", "Buka", "Tutup")

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
            .padding(top = 32.dp, bottom = 80.dp),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("Owner", fontSize = 14.sp, color = SBColor.Grey_500)
                    Text("Outlet", fontSize = 25.sp, fontWeight = FontWeight.ExtraBold, color = Color.Black)
                }
                Box {
                    Row(
                        modifier = Modifier
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
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, SBColor.Grey_200, RoundedCornerShape(12.dp))
                    .padding(12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    painterResource(R.drawable.ic_search),
                    contentDescription = "",
                    tint = SBColor.Grey_500,
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    "Cari outlet...",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Normal,
                    color = SBColor.Grey_500
                )
            }
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    filterOptions.forEach { option ->
                        val isSelected = option == selectedFilter
                        Text(
                            text = option,
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isSelected) SBColor.Grey_700 else SBColor.Grey_100)
                                .clickable { selectedFilter = option }
                                .padding(horizontal = 16.dp, vertical = 6.dp),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (isSelected) Color.White else SBColor.Grey_700
                        )
                    }
                }
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("5 outlet aktif", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                    Text("Urutkan", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFFCD3009))
                }
            }
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Box(
                            Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(SBColor.Primary_100),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(painterResource(R.drawable.ic_wallet), contentDescription = "", modifier = Modifier.size(20.dp), tint = Color(0xFFCD3009))
                        }
                        Column {
                            Text("Outlet Gajah Mada", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                            Text("Jl. Gajah Mada No. 7", fontSize = 12.sp)
                        }
                    }
                    Box(
                        Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(SBColor.Success_100)
                            .padding(8.dp, 4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Buka", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = SBColor.Success_600)
                    }
                }
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Rp2.120.000", fontSize = 20.sp, fontWeight = FontWeight.ExtraBold)
                    Text("424 pcs", fontSize = 12.sp, fontWeight = FontWeight.Medium, color = SBColor.Grey_500)
                }
                Row(Modifier.padding(vertical = 4.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth(0.7f)
                            .height(4.dp)
                            .clip(CircleShape)
                            .background(SBColor.Grey_100)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(0.7f)
                                .height(4.dp)
                                .background(SBColor.Primary)
                        )
                    }
                    Text("86% Target", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = SBColor.Grey_600)
                }
            }
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Box(
                            Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(SBColor.Primary_100),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(painterResource(R.drawable.ic_wallet), contentDescription = "", modifier = Modifier.size(20.dp), tint = Color(0xFFCD3009))
                        }
                        Column {
                            Text("Outlet Pattimura", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                            Text("Jl. Pattimura No. 10", fontSize = 12.sp)
                        }
                    }
                    Box(
                        Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(SBColor.Success_100)
                            .padding(8.dp, 4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Buka", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = SBColor.Success_600)
                    }
                }
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Rp1.120.000", fontSize = 20.sp, fontWeight = FontWeight.ExtraBold)
                    Text("321 pcs", fontSize = 12.sp, fontWeight = FontWeight.Medium, color = SBColor.Grey_500)
                }
                Row(Modifier.padding(vertical = 4.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth(0.7f)
                            .height(4.dp)
                            .clip(CircleShape)
                            .background(SBColor.Grey_100)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(0.46f)
                                .height(4.dp)
                                .background(SBColor.Primary)
                        )
                    }
                    Text("70% Target", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = SBColor.Grey_600)
                }
            }
        }
    }
}
