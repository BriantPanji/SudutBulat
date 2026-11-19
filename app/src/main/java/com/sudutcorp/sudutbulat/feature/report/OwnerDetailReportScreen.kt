package com.sudutcorp.sudutbulat.feature.report

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Surface
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
import com.sudutcorp.sudutbulat.feature.owner.ReportPeriodSummaryUi
import com.sudutcorp.sudutbulat.feature.owner.SingleReportDetailUi
import com.sudutcorp.sudutbulat.feature.owner.formatRupiah

@Composable
fun OwnerDetailReportScreen(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    reportSummary: ReportPeriodSummaryUi = defaultReportPeriodSummary(),
    singleReport: SingleReportDetailUi? = null
) {
    OutletDetailContent(
        modifier = modifier,
        reportSummary = reportSummary,
        singleReport = singleReport,
        onBackClick = { navController.popBackStack() }
    )
}

@Composable
fun OutletDetailScreen(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    reportSummary: ReportPeriodSummaryUi = defaultReportPeriodSummary(),
    singleReport: SingleReportDetailUi? = null
) {
    OutletDetailContent(
        modifier = modifier,
        reportSummary = reportSummary,
        singleReport = singleReport,
        onBackClick = { navController.popBackStack() }
    )
}

@Composable
fun OutletDetailContent(
    modifier: Modifier = Modifier,
    reportSummary: ReportPeriodSummaryUi = defaultReportPeriodSummary(),
    singleReport: SingleReportDetailUi? = null,
    onBackClick: () -> Unit = {}
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White)
            .statusBarsPadding()
            .navigationBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
            .padding(top = 16.dp, bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        // Sub Header bersih
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
                    modifier = Modifier.size(22.dp)
                )
            }
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = if (singleReport != null) "Laporan harian" else "Ringkasan laporan",
                    fontSize = 13.sp,
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

        // Identitas Outlet & Date Chip yang lega dan mudah dibaca
        OutlinedCard(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.outlinedCardColors(containerColor = Color.White),
            border = BorderStroke(1.dp, SBColor.Grey_100)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Surface(
                        modifier = Modifier.size(40.dp),
                        shape = RoundedCornerShape(10.dp),
                        color = SBColor.Primary_100
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                painter = painterResource(
                                    id = if (singleReport != null) R.drawable.ic_store else R.drawable.ic_bar_chart
                                ),
                                contentDescription = null,
                                tint = SBColor.Primary_550,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(
                            text = if (singleReport != null) singleReport.outletName else "Seluruh outlet",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = SBColor.Grey_700
                        )
                        Text(
                            text = if (singleReport != null) {
                                "Laporan tutup kasir harian"
                            } else {
                                "Rekap periode ${reportSummary.periodLabel}"
                            },
                            fontSize = 12.sp,
                            color = SBColor.Grey_500
                        )
                    }
                }

                // Spacious Date & Time Pill
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    color = SBColor.Grey_100
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.calendar),
                                contentDescription = "Tanggal laporan",
                                tint = SBColor.Grey_600,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = if (singleReport != null) {
                                    singleReport.fullDateLabel
                                } else {
                                    reportSummary.dateRangeText
                                },
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = SBColor.Grey_700
                            )
                        }
                        if (singleReport != null) {
                            Text(
                                text = singleReport.timeLabel,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = SBColor.Grey_500
                            )
                        }
                    }
                }
            }
        }

        if (singleReport != null) {
            // === MODE 1: DETAIL LAPORAN HARIAN SPESIFIK ===
            ReportHeroCard(
                label = "Total omzet",
                revenueText = formatRupiah(singleReport.totalRevenue),
                badgeText = "${singleReport.totalSold} pcs terjual",
                cashText = formatRupiah(singleReport.cashRevenue),
                qrisText = formatRupiah(singleReport.qrisRevenue)
            )

            // Rincian produk terjual
            if (singleReport.productLines.isNotEmpty()) {
                SectionCard(title = "Rincian produk terjual") {
                    singleReport.productLines.forEachIndexed { idx, prod ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(end = 12.dp),
                                verticalArrangement = Arrangement.spacedBy(2.dp)
                            ) {
                                Text(
                                    text = prod.productName,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = SBColor.Grey_700
                                )
                                Text(
                                    text = "${prod.quantity} × ${formatRupiah(prod.price)}",
                                    fontSize = 12.sp,
                                    color = SBColor.Grey_500
                                )
                            }
                            Text(
                                text = formatRupiah(prod.subtotal),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = SBColor.Grey_700
                            )
                        }
                        if (idx < singleReport.productLines.lastIndex) {
                            HorizontalDivider(thickness = 1.dp, color = SBColor.Grey_100)
                        }
                    }
                }
            }

            // Pengeluaran operasional
            SectionCard(title = "Pengeluaran operasional") {
                if (singleReport.expenseLines.isEmpty()) {
                    Text(
                        text = "Tidak ada pengeluaran operasional yang dicatat.",
                        fontSize = 13.sp,
                        color = SBColor.Grey_500
                    )
                } else {
                    singleReport.expenseLines.forEachIndexed { idx, exp ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Top
                        ) {
                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(end = 12.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = SBColor.Secondary_100
                                ) {
                                    Text(
                                        text = exp.categoryLabel,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = SBColor.Secondary_500,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                    )
                                }
                                if (!exp.note.isNullOrBlank()) {
                                    Text(
                                        text = exp.note,
                                        fontSize = 13.sp,
                                        color = SBColor.Grey_600
                                    )
                                }
                            }
                            Text(
                                text = formatRupiah(exp.amount),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = SBColor.Grey_700
                            )
                        }
                        if (idx < singleReport.expenseLines.lastIndex) {
                            HorizontalDivider(thickness = 1.dp, color = SBColor.Grey_100)
                        }
                    }
                    DottedDivider(color = SBColor.Grey_200, modifier = Modifier.padding(vertical = 2.dp))
                    DetailRowItem(
                        label = "Total pengeluaran",
                        value = formatRupiah(singleReport.totalExpense),
                        isBold = true,
                        valueFontSize = 15.sp,
                        valueFontWeight = FontWeight.ExtraBold
                    )
                }
            }

            // Bahan mentah rusak
            SectionCard(title = "Bahan mentah rusak") {
                if (singleReport.damagedLines.isEmpty()) {
                    Text(
                        text = "Tidak ada bahan mentah rusak yang dilaporkan.",
                        fontSize = 13.sp,
                        color = SBColor.Grey_500
                    )
                } else {
                    singleReport.damagedLines.forEachIndexed { idx, dmg ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Top
                        ) {
                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(end = 12.dp),
                                verticalArrangement = Arrangement.spacedBy(2.dp)
                            ) {
                                Text(
                                    text = dmg.materialName,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = SBColor.Grey_700
                                )
                                if (!dmg.note.isNullOrBlank()) {
                                    Text(
                                        text = dmg.note,
                                        fontSize = 12.sp,
                                        color = SBColor.Grey_500
                                    )
                                }
                            }
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = SBColor.Primary_100
                            ) {
                                Text(
                                    text = dmg.quantityText,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = SBColor.Primary_550,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                        if (idx < singleReport.damagedLines.lastIndex) {
                            HorizontalDivider(thickness = 1.dp, color = SBColor.Grey_100)
                        }
                    }
                }
            }

            // Ringkasan Setoran Bersih
            val netEstimate = (singleReport.totalRevenue - singleReport.totalExpense).coerceAtLeast(0L)
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = SBColor.Success_100
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(
                            text = "Estimasi setoran bersih",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = SBColor.Success_600
                        )
                        Text(
                            text = "Omzet dikurangi pengeluaran",
                            fontSize = 11.sp,
                            color = SBColor.Success_500
                        )
                    }
                    Text(
                        text = formatRupiah(netEstimate),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = SBColor.Success_600
                    )
                }
            }
        } else {
            // === MODE 2: DETAIL RINGKASAN PERIODE ===
            ReportHeroCard(
                label = "Omzet bersih (${reportSummary.periodLabel})",
                revenueText = reportSummary.netRevenueText,
                badgeText = "↑ ${reportSummary.deltaText}",
                cashText = reportSummary.cashSalesText,
                qrisText = reportSummary.qrisSalesText
            )

            SectionCard(title = "Ringkasan penjualan") {
                DetailRowItem(
                    label = "Penjualan tunai",
                    value = reportSummary.cashSalesText,
                    labelColor = SBColor.Grey_600
                )
                DetailRowItem(
                    label = "Penjualan QRIS",
                    value = reportSummary.qrisSalesText,
                    labelColor = SBColor.Grey_600
                )
                DottedDivider(color = SBColor.Grey_200, modifier = Modifier.padding(vertical = 2.dp))
                DetailRowItem(
                    label = "Total produk terjual",
                    value = reportSummary.totalProductsPcsText,
                    isBold = true,
                    valueFontSize = 15.sp,
                    valueFontWeight = FontWeight.ExtraBold
                )
            }

            SectionCard(title = "Pengeluaran operasional") {
                DetailRowItem(
                    label = "Gas & bahan bakar",
                    value = reportSummary.gasExpenseText,
                    labelColor = SBColor.Grey_600
                )
                DetailRowItem(
                    label = "Plastik & tusuk",
                    value = reportSummary.plasticExpenseText,
                    labelColor = SBColor.Grey_600
                )
                DetailRowItem(
                    label = "Lainnya",
                    value = reportSummary.otherExpenseText,
                    labelColor = SBColor.Grey_600
                )
                DottedDivider(color = SBColor.Grey_200, modifier = Modifier.padding(vertical = 2.dp))
                DetailRowItem(
                    label = "Total pengeluaran",
                    value = reportSummary.totalExpenseText,
                    isBold = true,
                    valueFontSize = 15.sp,
                    valueFontWeight = FontWeight.ExtraBold
                )
            }

            SectionCard(title = "Bahan mentah rusak") {
                reportSummary.damagedItems.forEachIndexed { idx, dmg ->
                    DetailRowItem(
                        label = dmg.name,
                        value = dmg.quantityText,
                        labelColor = SBColor.Grey_600
                    )
                    if (idx < reportSummary.damagedItems.lastIndex) {
                        HorizontalDivider(thickness = 1.dp, color = SBColor.Grey_100)
                    }
                }
            }
        }
    }
}

@Composable
private fun ReportHeroCard(
    label: String,
    revenueText: String,
    badgeText: String,
    cashText: String,
    qrisText: String
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = SBColor.Primary
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = label,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.White.copy(alpha = 0.9f)
                )
                Surface(
                    shape = RoundedCornerShape(99.dp),
                    color = Color.White.copy(alpha = 0.2f)
                ) {
                    Text(
                        text = badgeText,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }

            Text(
                text = revenueText,
                fontSize = 30.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White
            )

            HorizontalDivider(
                thickness = 1.dp,
                color = Color.White.copy(alpha = 0.22f)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        text = "Tunai",
                        fontSize = 11.sp,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                    Text(
                        text = cashText,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
                Column(
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Text(
                        text = "QRIS",
                        fontSize = 11.sp,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                    Text(
                        text = qrisText,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }
    }
}

@Composable
private fun SectionCard(
    title: String,
    content: @Composable () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(
            text = title,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = SBColor.Grey_700
        )
        OutlinedCard(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.outlinedCardColors(containerColor = Color.White),
            border = BorderStroke(1.dp, SBColor.Grey_100)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                content()
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
            color = if (isBold) SBColor.Grey_700 else labelColor,
            modifier = Modifier
                .weight(1f)
                .padding(end = 12.dp)
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
