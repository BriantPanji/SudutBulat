package com.sudutcorp.sudutbulat.feature.outlet

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.sudutcorp.sudutbulat.R

@Composable
fun OwnerDetailOutletScreen(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    OwnerDetailOutletContent(
        modifier = modifier,
        onBackClick = { navController.popBackStack() }
    )
}

@Composable
fun OwnerDetailOutletContent(
    modifier: Modifier = Modifier,
    onBackClick: () -> Unit = {}
) {
    // Color Palette
    val primaryColor = Color(0xFF231F1F)
    val secondaryColor = Color(0xFF443D3D)
    val successColor = Color(0xFF14754F)
    val mutedColor = Color(0xFF685E5D)
    val borderCardColor = Color(0xFFEFEFEF)

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
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            IconButton(
                onClick = onBackClick,
                modifier = Modifier
                    .size(44.dp)
                    .background(color = Color(0xFFF1F0F0), shape = CircleShape)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_chevron_left),
                    contentDescription = "Kembali",
                    tint = primaryColor,
                    modifier = Modifier.size(28.dp)
                )
            }
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = "Outlet",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = secondaryColor
                )
                Text(
                    text = "Sudirman",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = primaryColor
                )
            }
        }

        // Card Outlet Ditugaskan
        OutlinedCard(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.outlinedCardColors(containerColor = Color.White),
            border = CardDefaults.outlinedCardBorder().copy(
                brush = androidx.compose.ui.graphics.SolidColor(borderCardColor)
            )
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
                            .size(40.dp)
                            .background(
                                color = Color(0xFFFFEDEC),
                                shape = RoundedCornerShape(12.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_store),
                            contentDescription = "Outlet",
                            tint = Color(0xFFCD3009),
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(
                            text = "Outlet ditugaskan",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Normal,
                            color = secondaryColor
                        )
                        Text(
                            text = "Sudirman",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = primaryColor
                        )
                        Text(
                            text = "Jl. Jenderal Sudirman No. 18",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Normal,
                            color = secondaryColor
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .background(
                            color = Color(0xFFE6F5ED),
                            shape = RoundedCornerShape(6.dp)
                        )
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "Buka",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = successColor
                    )
                }
            }
        }

        // Grid 2x2 Metric Cards
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Card 1: Penjualan
                MetricCard(
                    modifier = Modifier.weight(1f),
                    iconRes = R.drawable.ic_store,
                    iconBgColor = Color(0xFFE6F5ED),
                    iconTint = successColor,
                    title = "Penjualan",
                    value = "Rp2.480.000",
                    subtitle = "496 pcs hari ini",
                    secondaryColor = secondaryColor,
                    primaryColor = primaryColor,
                    mutedColor = mutedColor,
                    borderColor = borderCardColor
                )

                // Card 2: Pengeluaran
                MetricCard(
                    modifier = Modifier.weight(1f),
                    iconRes = R.drawable.ic_wallet,
                    iconBgColor = Color(0xFFF8EBD8),
                    iconTint = Color(0xFF645434),
                    title = "Pengeluaran",
                    value = "Rp185.000",
                    subtitle = "3 transaksi",
                    secondaryColor = secondaryColor,
                    primaryColor = primaryColor,
                    mutedColor = mutedColor,
                    borderColor = borderCardColor
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Card 3: Stok saat ini
                MetricCard(
                    modifier = Modifier.weight(1f),
                    iconRes = R.drawable.ic_store, // Sementara ic_store sesuai instruksi
                    iconBgColor = Color(0xFFE6F5ED),
                    iconTint = successColor,
                    title = "Stok saat ini",
                    value = "878 pcs",
                    subtitle = "Tahu Original",
                    secondaryColor = secondaryColor,
                    primaryColor = primaryColor,
                    mutedColor = mutedColor,
                    borderColor = borderCardColor
                )

                // Card 4: Stok rusak
                MetricCard(
                    modifier = Modifier.weight(1f),
                    iconRes = R.drawable.ic_warning,
                    iconBgColor = Color(0xFFF8EBD8),
                    iconTint = Color(0xFF645434),
                    title = "Stok rusak",
                    value = "12 pcs",
                    subtitle = "1 laporan",
                    secondaryColor = secondaryColor,
                    primaryColor = primaryColor,
                    mutedColor = mutedColor,
                    borderColor = borderCardColor
                )
            }
        }

        // Section: Status Outlet
        Column(
            modifier = Modifier.padding(top = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Status outlet",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = primaryColor
            )

            // Jam operasional
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Jam operasional",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Normal,
                    color = secondaryColor
                )
                Text(
                    text = "09.00 – 21.00",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = primaryColor
                )
            }

            // Staf bertugas
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Staf bertugas",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Normal,
                    color = secondaryColor
                )
                Text(
                    text = "Dimas Pratama",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = primaryColor
                )
            }

            // Dotted Line atas Setoran Bersih
            DottedLine(
                color = Color(0xFFDCDCDC),
                modifier = Modifier.padding(vertical = 4.dp)
            )

            // Setoran bersih (est.)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Setoran bersih (est.)",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = primaryColor
                )
                Text(
                    text = "Rp1.675.000",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = primaryColor
                )
            }
        }
    }
}

@Composable
private fun MetricCard(
    modifier: Modifier = Modifier,
    iconRes: Int,
    iconBgColor: Color,
    iconTint: Color,
    title: String,
    value: String,
    subtitle: String,
    secondaryColor: Color,
    primaryColor: Color,
    mutedColor: Color,
    borderColor: Color
) {
    OutlinedCard(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.outlinedCardColors(containerColor = Color.White),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = androidx.compose.ui.graphics.SolidColor(borderColor)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(
                        color = iconBgColor,
                        shape = RoundedCornerShape(12.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(id = iconRes),
                    contentDescription = title,
                    tint = iconTint,
                    modifier = Modifier.size(20.dp)
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Normal,
                    color = secondaryColor
                )
                Text(
                    text = value,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = primaryColor
                )
                Text(
                    text = subtitle,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Normal,
                    color = mutedColor
                )
            }
        }
    }
}

@Composable
private fun DottedLine(
    modifier: Modifier = Modifier,
    color: Color = Color(0xFFCCCCCC)
) {
    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(1.dp)
    ) {
        val pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 12f), 0f)
        drawLine(
            color = color,
            start = Offset(0f, 0f),
            end = Offset(size.width, 0f),
            strokeWidth = 1.dp.toPx(),
            pathEffect = pathEffect
        )
    }
}

@Preview(showBackground = true)
@Composable
fun OwnerDetailOutletScreenPreview() {
    OwnerDetailOutletContent()
}