package com.sudutcorp.sudutbulat.feature.report

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import com.sudutcorp.sudutbulat.R

@Composable
fun OwnerReportScreen(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    OwnerReportContent(
        modifier = modifier,
        onProfileClick = { /* contoh jika nanti ada aksi navigasi */ },
        onSeeDetailClick = { /* aksi navigasi lihat detail */ }
    )
}

@Composable
fun OwnerReportContent(
    modifier: Modifier = Modifier,
    onProfileClick: () -> Unit = {},
    onSeeDetailClick: () -> Unit = {}
) {
    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
            .padding(top = 18.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "Owner",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp
                )
                Text(
                    text = "Laporan",
                    fontWeight = FontWeight.Bold,
                    fontSize = 25.sp
                )
            }
            Box(contentAlignment = Alignment.BottomEnd) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .background(
                            color = Color(0xFFF8EBD8),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "AR",
                        color = Color(0xFFB62C08),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
                Box(
                    modifier = Modifier
                        .offset(x = 1.dp, y = 1.dp)
                        .size(12.dp)
                        .border(width = 2.dp, shape = CircleShape, color = Color.White)
                        .background(
                            color = Color(0xFF168257),
                            shape = CircleShape
                        )
                )
            }
        }

        // Filter waktu
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(color = Color(0xFFF1F0F0), shape = RoundedCornerShape(12.dp))
                .height(46.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .height(40.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    text = "7 Hari"
                )
            }
            Column(
                modifier = Modifier
                    .weight(1f)
                    .height(40.dp)
                    .background(
                        color = Color(0xFFFFFFFF),
                        shape = RoundedCornerShape(size = 9.dp)
                    ),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    text = "30 Hari"
                )
            }
            Column(
                modifier = Modifier
                    .weight(1f)
                    .height(40.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    text = "3 Bulan"
                )
            }
        }

        // Omzet
        Column {
            Text(
                color = Color(0xFF443D3D),
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                text = "Omzet bersih"
            )

            Text(
                fontSize = 25.sp,
                fontWeight = FontWeight.ExtraBold,
                modifier = Modifier.padding(top = 8.dp),
                text = "Rp184.650.000"
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.arrow_up),
                    contentDescription = "Panah Ke Atas",
                    tint = Color(0xFF14754F),
                    modifier = Modifier.size(12.dp)
                )
                Text(
                    color = Color(0xFF14754F),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 4.dp),
                    text = "8,7% dibanding periode lalu"
                )
            }
        }

        // Tren Penjualan
        OutlinedCard(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.outlinedCardColors(
                containerColor = Color.White
            ),
            border = CardDefaults.outlinedCardBorder().copy(
                brush = androidx.compose.ui.graphics.SolidColor(Color(0xFFEFEFEF))
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(
                            text = "Tren penjualan",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1C1B1F)
                        )
                        Text(
                            text = "24 Apr - 24 Mei",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Normal,
                            color = Color(0xFF685E5D)
                        )
                    }
                    Text(
                        text = "Rp",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Normal,
                        color = Color(0xFF685E5D)
                    )
                }

                // Grafik Batang
                val barData = listOf(
                    "S" to 0.45f,
                    "S" to 0.55f,
                    "R" to 0.50f,
                    "K" to 0.65f,
                    "J" to 0.60f,
                    "S" to 0.72f,
                    "M" to 0.85f
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    barData.forEach { (day, heightFraction) ->
                        val isHighlighted = day == "M"
                        val barColor = if (isHighlighted) Color(0xFFCD3009) else Color(0xFFFCDBD8)

                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Bottom,
                            modifier = Modifier.fillMaxHeight()
                        ) {
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .width(28.dp),
                                contentAlignment = Alignment.BottomCenter
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .fillMaxHeight(heightFraction)
                                        .clip(RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp))
                                        .background(barColor)
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = day,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color(0xFF685E5D)
                            )
                        }
                    }
                }
            }
        }

        // Ringkasan Bisnis
        Column(
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Ringkasan bisnis",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1C1B1F)
                )
                TextButton(onClick = onSeeDetailClick) {
                    Text(
                        text = "Lihat detail",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFCD3009)
                    )
                }
            }

            OutlinedCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.outlinedCardColors(
                    containerColor = Color.White
                ),
                border = CardDefaults.outlinedCardBorder().copy(
                    brush = androidx.compose.ui.graphics.SolidColor(Color(0xFFEFEFEF))
                )
            ) {
                Column {
                    // Item 1: Total penjualan
                    BusinessSummaryItem(
                        iconRes = R.drawable.ic_store,
                        title = "Total penjualan",
                        subtitle = "36.930 produk",
                        value = "Rp184,6jt",
                        valueColor = Color(0xFF1C1B1F)
                    )

                    HorizontalDivider(
                        color = Color(0xFFF1F0F0),
                        thickness = 1.dp,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )

                    // Item 2: Biaya operasional
                    BusinessSummaryItem(
                        iconRes = R.drawable.ic_wallet,
                        title = "Biaya operasional",
                        subtitle = "12,8% dari omzet",
                        value = "Rp23,7jt",
                        valueColor = Color(0xFF1C1B1F)
                    )

                    HorizontalDivider(
                        color = Color(0xFFF1F0F0),
                        thickness = 1.dp,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )

                    // Item 3: Estimasi laba
                    BusinessSummaryItem(
                        iconRes = R.drawable.ic_bar_chart,
                        title = "Estimasi laba",
                        subtitle = "Setelah biaya operasional",
                        value = "Rp62,4jt",
                        valueColor = Color(0xFF156E4B)
                    )
                }
            }
        }
    }
}

@Composable
private fun BusinessSummaryItem(
    iconRes: Int,
    title: String,
    subtitle: String,
    value: String,
    valueColor: Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(
                        color = Color(0xFFFFEDEC),
                        shape = RoundedCornerShape(12.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(id = iconRes),
                    contentDescription = title,
                    tint = Color(0xFFCD3009),
                    modifier = Modifier.size(20.dp)
                )
            }
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1C1B1F)
                )
                Text(
                    text = subtitle,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Normal,
                    color = Color(0xFF685E5D)
                )
            }
        }
        Text(
            text = value,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = valueColor
        )
    }
}

@Preview(showBackground = true)
@Composable
fun OwnerReportScreenPreview() {
    OwnerReportContent()
}