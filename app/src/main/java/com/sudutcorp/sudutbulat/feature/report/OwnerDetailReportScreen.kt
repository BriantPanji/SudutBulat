package com.sudutcorp.sudutbulat.feature.report

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.sudutcorp.sudutbulat.R
import com.sudutcorp.sudutbulat.navigation.popBackStackSafe
import com.sudutcorp.sudutbulat.core.components.SBDottedDivider
import com.sudutcorp.sudutbulat.core.components.SBKeyValueRow
import com.sudutcorp.sudutbulat.core.components.SBSectionCard
import com.sudutcorp.sudutbulat.core.components.SBStatusBadge
import com.sudutcorp.sudutbulat.core.components.SBSubHeader
import com.sudutcorp.sudutbulat.core.ui.theme.SBColor
import com.sudutcorp.sudutbulat.core.util.formatRupiah
import com.sudutcorp.sudutbulat.feature.owner.ReportPeriodSummaryUi
import com.sudutcorp.sudutbulat.feature.owner.SingleReportDetailUi

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
        onBackClick = { navController.popBackStackSafe() }
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
        SBSubHeader(
            eyebrow = if (singleReport != null) "Laporan harian" else "Ringkasan laporan",
            title = "Detail laporan",
            onBackClick = onBackClick
        )

        // Identitas Outlet & Date Pill
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
                            text = singleReport?.outletName ?: "Seluruh outlet",
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
                                text = singleReport?.fullDateLabel ?: reportSummary.dateRangeText,
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
            ReportHeroCard(
                label = "Total omzet",
                revenueText = formatRupiah(singleReport.totalRevenue),
                badgeText = "${singleReport.totalSold} pcs terjual",
                cashText = formatRupiah(singleReport.cashRevenue),
                qrisText = formatRupiah(singleReport.qrisRevenue)
            )

            if (singleReport.productLines.isNotEmpty()) {
                SBSectionCard(title = "Rincian produk terjual") {
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
                                Text(prod.productName, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = SBColor.Grey_700)
                                Text("${prod.quantity} × ${formatRupiah(prod.price)}", fontSize = 12.sp, color = SBColor.Grey_500)
                            }
                            Text(formatRupiah(prod.subtotal), fontSize = 14.sp, fontWeight = FontWeight.Bold, color = SBColor.Grey_700)
                        }
                        if (idx < singleReport.productLines.lastIndex) {
                            HorizontalDivider(thickness = 1.dp, color = SBColor.Grey_100)
                        }
                    }
                }
            }

            SBSectionCard(title = "Pengeluaran operasional") {
                if (singleReport.expenseLines.isEmpty()) {
                    Text("Tidak ada pengeluaran operasional yang dicatat.", fontSize = 13.sp, color = SBColor.Grey_500)
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
                                SBStatusBadge(
                                    text = exp.categoryLabel,
                                    containerColor = SBColor.Secondary_100,
                                    contentColor = SBColor.Secondary_500
                                )
                                if (!exp.note.isNullOrBlank()) {
                                    Text(exp.note, fontSize = 13.sp, color = SBColor.Grey_600)
                                }
                            }
                            Text(formatRupiah(exp.amount), fontSize = 14.sp, fontWeight = FontWeight.Bold, color = SBColor.Grey_700)
                        }
                        if (idx < singleReport.expenseLines.lastIndex) {
                            HorizontalDivider(thickness = 1.dp, color = SBColor.Grey_100)
                        }
                    }
                    SBDottedDivider(modifier = Modifier.padding(vertical = 2.dp))
                    SBKeyValueRow(
                        label = "Total pengeluaran",
                        value = formatRupiah(singleReport.totalExpense),
                        isBold = true,
                        valueFontSize = 15.sp
                    )
                }
            }

            SBSectionCard(title = "Bahan mentah rusak") {
                if (singleReport.damagedLines.isEmpty()) {
                    Text("Tidak ada bahan mentah rusak yang dilaporkan.", fontSize = 13.sp, color = SBColor.Grey_500)
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
                                Text(dmg.materialName, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = SBColor.Grey_700)
                                if (!dmg.note.isNullOrBlank()) {
                                    Text(dmg.note, fontSize = 12.sp, color = SBColor.Grey_500)
                                }
                            }
                            SBStatusBadge(
                                text = dmg.quantityText,
                                containerColor = SBColor.Primary_100,
                                contentColor = SBColor.Primary_550
                            )
                        }
                        if (idx < singleReport.damagedLines.lastIndex) {
                            HorizontalDivider(thickness = 1.dp, color = SBColor.Grey_100)
                        }
                    }
                }
            }

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
                        Text("Estimasi setoran bersih", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = SBColor.Success_600)
                        Text("Omzet dikurangi pengeluaran", fontSize = 11.sp, color = SBColor.Success_500)
                    }
                    Text(formatRupiah(netEstimate), fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = SBColor.Success_600)
                }
            }
        } else {
            ReportHeroCard(
                label = "Omzet bersih (${reportSummary.periodLabel})",
                revenueText = reportSummary.netRevenueText,
                badgeText = "↑ ${reportSummary.deltaText}",
                cashText = reportSummary.cashSalesText,
                qrisText = reportSummary.qrisSalesText
            )

            SBSectionCard(title = "Ringkasan penjualan") {
                SBKeyValueRow("Penjualan tunai", reportSummary.cashSalesText)
                SBKeyValueRow("Penjualan QRIS", reportSummary.qrisSalesText)
                SBDottedDivider(modifier = Modifier.padding(vertical = 2.dp))
                SBKeyValueRow("Total produk terjual", reportSummary.totalProductsPcsText, isBold = true, valueFontSize = 15.sp)
            }

            SBSectionCard(title = "Pengeluaran operasional") {
                SBKeyValueRow("Gas & bahan bakar", reportSummary.gasExpenseText)
                SBKeyValueRow("Plastik & tusuk", reportSummary.plasticExpenseText)
                SBKeyValueRow("Lainnya", reportSummary.otherExpenseText)
                SBDottedDivider(modifier = Modifier.padding(vertical = 2.dp))
                SBKeyValueRow("Total pengeluaran", reportSummary.totalExpenseText, isBold = true, valueFontSize = 15.sp)
            }

            SBSectionCard(title = "Bahan mentah rusak") {
                reportSummary.damagedItems.forEachIndexed { idx, dmg ->
                    SBKeyValueRow(dmg.name, dmg.quantityText)
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
                Text(label, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = Color.White.copy(alpha = 0.9f))
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

            Text(revenueText, fontSize = 30.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
            HorizontalDivider(thickness = 1.dp, color = Color.White.copy(alpha = 0.22f))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text("Tunai", fontSize = 11.sp, color = Color.White.copy(alpha = 0.8f))
                    Text(cashText, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
                Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text("QRIS", fontSize = 11.sp, color = Color.White.copy(alpha = 0.8f))
                    Text(qrisText, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
        }
    }
}
