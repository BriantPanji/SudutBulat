package com.sudutcorp.sudutbulat.feature.report

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.sudutcorp.sudutbulat.R
import com.sudutcorp.sudutbulat.core.ui.theme.SBColor

@Composable
fun OutletDetailScreen(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    OutletDetailContent(
        modifier = modifier,
        onBackClick = { navController.popBackStack() }
    )
}

@Composable
fun OutletDetailContent(
    modifier: Modifier = Modifier,
    onBackClick: () -> Unit = {}
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
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            IconButton(
                onClick = onBackClick,
                modifier = Modifier
                    .size(44.dp)
                    .background(color = SBColor.Grey_100, shape = CircleShape)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_chevron_left),
                    contentDescription = "Kembali",
                    tint = SBColor.Grey_700,
                    modifier = Modifier.size(24.dp)
                )
            }
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = "Laporan • 30 hari",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = SBColor.Grey_500
                )
                Text(
                    text = "Detail laporan",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = SBColor.Grey_700
                )
            }
        }

        // Section Omzet Bersih
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = "Omzet bersih",
                color = SBColor.Grey_600,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium
            )

            Text(
                text = "Rp184.650.000",
                fontSize = 28.sp,
                fontWeight = FontWeight.ExtraBold,
                color = SBColor.Grey_700
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.arrow_up),
                    contentDescription = "Panah Ke Atas",
                    tint = SBColor.Success_600,
                    modifier = Modifier.size(12.dp)
                )
                Text(
                    text = "8,7% dibanding periode lalu",
                    color = SBColor.Success_600,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // Section Rincian
        Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "Ringkasan penjualan",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = SBColor.Grey_700
                )

                DetailRowItem(
                    label = "Penjualan tunai",
                    value = "Rp102.340.000",
                    labelColor = SBColor.Grey_600
                )

                DetailRowItem(
                    label = "Penjualan QRIS",
                    value = "Rp82.310.000",
                    labelColor = SBColor.Grey_600
                )

                DottedDivider(
                    color = SBColor.Grey_200,
                    modifier = Modifier.padding(vertical = 4.dp)
                )

                DetailRowItem(
                    label = "Total produk terjual",
                    value = "36.930 pcs",
                    isBold = true,
                    valueFontSize = 16.sp,
                    valueFontWeight = FontWeight.ExtraBold
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "Pengeluaran",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = SBColor.Grey_700
                )

                DetailRowItem(
                    label = "Gas & bahan bakar",
                    value = "Rp9.480.000",
                    labelColor = SBColor.Grey_600
                )

                DetailRowItem(
                    label = "Plastik & tusuk",
                    value = "Rp7.120.000",
                    labelColor = SBColor.Grey_600
                )

                DottedDivider(
                    color = SBColor.Grey_200,
                    modifier = Modifier.padding(vertical = 4.dp)
                )

                DetailRowItem(
                    label = "Total pengeluaran",
                    value = "Rp23.700.000",
                    isBold = true,
                    valueFontSize = 16.sp,
                    valueFontWeight = FontWeight.ExtraBold
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "Stok rusak",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = SBColor.Grey_700
                )

                DetailRowItem(
                    label = "Tahu Original",
                    value = "86 pcs",
                    labelColor = SBColor.Grey_600
                )

                DetailRowItem(
                    label = "Bumbu",
                    value = "7 pak",
                    labelColor = SBColor.Grey_600
                )
            }
        }
    }
}

@Composable
private fun DottedDivider(
    modifier: Modifier = Modifier,
    color: Color = SBColor.Grey_200
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

@Composable
private fun DetailRowItem(
    label: String,
    value: String,
    isBold: Boolean = false,
    labelColor: Color = SBColor.Grey_700,
    valueFontSize: TextUnit = 14.sp,
    valueFontWeight: FontWeight = if (isBold) FontWeight.Bold else FontWeight.SemiBold
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = 14.sp,
            fontWeight = if (isBold) FontWeight.Bold else FontWeight.Normal,
            color = if (isBold) SBColor.Grey_700 else labelColor
        )
        Text(
            text = value,
            fontSize = valueFontSize,
            fontWeight = valueFontWeight,
            color = SBColor.Grey_700
        )
    }
}

@Preview(showBackground = true)
@Composable
fun OutletDetailScreenPreview() {
    OutletDetailContent()
}
