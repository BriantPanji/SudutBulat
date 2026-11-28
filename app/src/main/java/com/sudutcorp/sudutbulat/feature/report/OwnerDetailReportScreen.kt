package com.sudutcorp.sudutbulat.feature.report

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
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
import com.sudutcorp.sudutbulat.core.components.SBDottedDivider
import com.sudutcorp.sudutbulat.core.components.SBKeyValueRow
import com.sudutcorp.sudutbulat.core.components.SBSectionCard
import com.sudutcorp.sudutbulat.core.components.SBSegmentedTabs
import com.sudutcorp.sudutbulat.core.components.SBStatusBadge
import com.sudutcorp.sudutbulat.core.components.SBSubHeader
import com.sudutcorp.sudutbulat.core.ui.theme.SBColor
import com.sudutcorp.sudutbulat.core.util.formatDecimal
import com.sudutcorp.sudutbulat.core.util.formatRupiah
import com.sudutcorp.sudutbulat.data.model.ReportReviewStatus
import com.sudutcorp.sudutbulat.feature.owner.ReportPeriodSummaryUi
import com.sudutcorp.sudutbulat.feature.owner.SingleReportDetailUi
import com.sudutcorp.sudutbulat.navigation.popBackStackSafe

private val QUICK_REJECT_REASONS = listOf(
    "Rincian nota pengeluaran belum jelas",
    "Jumlah bahan rusak perlu penjelasan",
    "Selisih nominal Tunai / QRIS"
)

@Composable
fun OwnerDetailReportScreen(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    reportSummary: ReportPeriodSummaryUi = defaultReportPeriodSummary(),
    singleReport: SingleReportDetailUi? = null,
    selectedPeriod: String = "30 Hari",
    onPeriodSelected: ((String) -> Unit)? = null,
    isActionLoading: Boolean = false,
    onApproveReport: (reportId: String, note: String?) -> Unit = { _, _ -> },
    onRejectReport: (reportId: String, reason: String) -> Unit = { _, _ -> }
) {
    OutletDetailContent(
        modifier = modifier,
        reportSummary = reportSummary,
        singleReport = singleReport,
        selectedPeriod = selectedPeriod,
        onPeriodSelected = onPeriodSelected,
        isActionLoading = isActionLoading,
        onApproveReport = onApproveReport,
        onRejectReport = onRejectReport,
        onBackClick = { navController.popBackStackSafe() }
    )
}

@Composable
fun OutletDetailContent(
    modifier: Modifier = Modifier,
    reportSummary: ReportPeriodSummaryUi = defaultReportPeriodSummary(),
    singleReport: SingleReportDetailUi? = null,
    selectedPeriod: String = "30 Hari",
    onPeriodSelected: ((String) -> Unit)? = null,
    isActionLoading: Boolean = false,
    onApproveReport: (reportId: String, note: String?) -> Unit = { _, _ -> },
    onRejectReport: (reportId: String, reason: String) -> Unit = { _, _ -> },
    onBackClick: () -> Unit = {}
) {
    var showRejectDialog by rememberSaveable { mutableStateOf(false) }
    val periods = remember { listOf("7 Hari" to "7 hari", "30 Hari" to "30 hari", "3 Bulan" to "3 bulan") }

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
            eyebrow = if (singleReport != null) "Laporan harian" else "Rekap seluruh outlet",
            title = if (singleReport != null) "Detail laporan" else "Detail seluruh laporan",
            onBackClick = onBackClick
        )

        if (singleReport == null && onPeriodSelected != null) {
            SBSegmentedTabs(
                options = periods.map { it.first },
                selected = periods.firstOrNull {
                    it.first.equals(selectedPeriod, ignoreCase = true)
                }?.first ?: "30 Hari",
                labelOf = { key -> periods.firstOrNull { it.first == key }?.second ?: key },
                onSelected = onPeriodSelected
            )
        }

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
                                singleReport.createdByName?.let { "Dilaporkan oleh $it" } ?: "Laporan tutup kasir harian"
                            } else {
                                "${reportSummary.reportCount} laporan • ${reportSummary.contributingOutletsCount} outlet (${reportSummary.periodLabel})"
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

            OwnerReportReviewActionCard(
                report = singleReport,
                isActionLoading = isActionLoading,
                onApproveClick = {
                    onApproveReport(singleReport.id, "Disetujui oleh Owner.")
                },
                onRejectClick = {
                    showRejectDialog = true
                }
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
                            Column(
                                horizontalAlignment = Alignment.End,
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                SBStatusBadge(
                                    text = dmg.quantityText,
                                    containerColor = SBColor.Primary_100,
                                    contentColor = SBColor.Primary_550
                                )
                                if (dmg.estimatedLoss > 0L) {
                                    Text(
                                        text = formatRupiah(dmg.estimatedLoss),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = SBColor.Grey_600
                                    )
                                }
                            }
                        }
                        if (idx < singleReport.damagedLines.lastIndex) {
                            HorizontalDivider(thickness = 1.dp, color = SBColor.Grey_100)
                        }
                    }
                    if (singleReport.damagedEstimatedLoss > 0L) {
                        SBDottedDivider(modifier = Modifier.padding(vertical = 2.dp))
                        SBKeyValueRow(
                            label = "Estimasi kerugian bahan rusak",
                            value = formatRupiah(singleReport.damagedEstimatedLoss),
                            isBold = true,
                            valueColor = SBColor.Primary_550,
                            valueFontSize = 14.sp
                        )
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
            val heroBadgeText = when (reportSummary.deltaTrend) {
                1 -> "↑ ${reportSummary.deltaText.substringBefore(" ")}"
                -1 -> "↓ ${reportSummary.deltaText.substringBefore(" ")}"
                else -> reportSummary.totalProductsPcsText
            }
            val cashPct = (reportSummary.cashShareFraction * 100f).toInt()
            val qrisPct = if (reportSummary.totalRevenue > 0L) (100 - cashPct) else 0

            ReportHeroCard(
                label = "Total omzet (${reportSummary.periodLabel})",
                revenueText = formatRupiah(reportSummary.totalRevenue),
                badgeText = heroBadgeText,
                cashText = "${reportSummary.cashSalesText} ($cashPct%)",
                qrisText = "${reportSummary.qrisSalesText} ($qrisPct%)"
            )

            SBSectionCard(title = "Kontribusi per outlet") {
                if (reportSummary.outletBreakdowns.isEmpty()) {
                    Text(
                        text = "Belum ada laporan harian outlet pada periode ini.",
                        fontSize = 13.sp,
                        color = SBColor.Grey_500
                    )
                } else {
                    reportSummary.outletBreakdowns.forEachIndexed { idx, outlet ->
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
                                Text(outlet.outletName, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = SBColor.Grey_700)
                                Text(
                                    text = "${outlet.reportCount} laporan • ${formatDecimal(outlet.totalSold)} pcs",
                                    fontSize = 12.sp,
                                    color = SBColor.Grey_500
                                )
                            }
                            Column(
                                horizontalAlignment = Alignment.End,
                                verticalArrangement = Arrangement.spacedBy(2.dp)
                            ) {
                                Text(formatRupiah(outlet.totalRevenue), fontSize = 14.sp, fontWeight = FontWeight.Bold, color = SBColor.Grey_700)
                                Text(
                                    text = "Biaya ${formatRupiah(outlet.totalExpense)}",
                                    fontSize = 11.sp,
                                    color = SBColor.Grey_500
                                )
                            }
                        }
                        if (idx < reportSummary.outletBreakdowns.lastIndex) {
                            HorizontalDivider(thickness = 1.dp, color = SBColor.Grey_100)
                        }
                    }
                }
            }

            SBSectionCard(title = "Rincian produk terjual") {
                if (reportSummary.productBreakdowns.isEmpty()) {
                    Text(
                        text = "Belum ada rincian produk terjual pada periode ini.",
                        fontSize = 13.sp,
                        color = SBColor.Grey_500
                    )
                } else {
                    reportSummary.productBreakdowns.forEachIndexed { idx, prod ->
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
                                Text("${formatDecimal(prod.totalQuantity)} terjual", fontSize = 12.sp, color = SBColor.Grey_500)
                            }
                            Text(formatRupiah(prod.totalSubtotal), fontSize = 14.sp, fontWeight = FontWeight.Bold, color = SBColor.Grey_700)
                        }
                        if (idx < reportSummary.productBreakdowns.lastIndex) {
                            HorizontalDivider(thickness = 1.dp, color = SBColor.Grey_100)
                        }
                    }
                    SBDottedDivider(modifier = Modifier.padding(vertical = 2.dp))
                    SBKeyValueRow(
                        label = "Total produk terjual",
                        value = reportSummary.totalProductsPcsText,
                        isBold = true,
                        valueFontSize = 15.sp
                    )
                }
            }

            SBSectionCard(title = "Pengeluaran operasional") {
                SBKeyValueRow("Gas & bahan bakar", reportSummary.gasExpenseText)
                SBKeyValueRow("Plastik & tusuk", reportSummary.plasticExpenseText)
                SBKeyValueRow("Lainnya", reportSummary.otherExpenseText)
                SBDottedDivider(modifier = Modifier.padding(vertical = 2.dp))
                SBKeyValueRow(
                    label = "Total pengeluaran (${reportSummary.operationalCostPercentText})",
                    value = reportSummary.totalExpenseText,
                    isBold = true,
                    valueFontSize = 15.sp
                )
            }

            SBSectionCard(title = "Bahan mentah rusak") {
                if (reportSummary.damagedItems.isEmpty()) {
                    Text(
                        text = "Tidak ada bahan mentah rusak pada periode ini.",
                        fontSize = 13.sp,
                        color = SBColor.Grey_500
                    )
                } else {
                    reportSummary.damagedItems.forEachIndexed { idx, dmg ->
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
                                Text(dmg.name, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = SBColor.Grey_700)
                                if (dmg.estimatedLoss > 0L) {
                                    Text(
                                        text = "Est. rugi ${formatRupiah(dmg.estimatedLoss)}",
                                        fontSize = 12.sp,
                                        color = SBColor.Grey_500
                                    )
                                }
                            }
                            SBStatusBadge(
                                text = dmg.quantityText,
                                containerColor = SBColor.Primary_100,
                                contentColor = SBColor.Primary_550
                            )
                        }
                        if (idx < reportSummary.damagedItems.lastIndex) {
                            HorizontalDivider(thickness = 1.dp, color = SBColor.Grey_100)
                        }
                    }
                    if (reportSummary.damagedEstimatedLoss > 0L) {
                        SBDottedDivider(modifier = Modifier.padding(vertical = 2.dp))
                        SBKeyValueRow(
                            label = "Estimasi total kerugian",
                            value = formatRupiah(reportSummary.damagedEstimatedLoss),
                            isBold = true,
                            valueColor = SBColor.Primary_550,
                            valueFontSize = 15.sp
                        )
                    }
                }
            }

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
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .padding(end = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Text(
                            text = "Estimasi pendapatan bersih",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = SBColor.Success_600
                        )
                        Text(
                            text = "Total omzet dikurangi biaya operasional",
                            fontSize = 11.sp,
                            color = SBColor.Success_500
                        )
                    }
                    Text(
                        text = formatRupiah(reportSummary.netRevenue),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = SBColor.Success_600
                    )
                }
            }
        }
    }

    if (showRejectDialog && singleReport != null) {
        RejectDailyReportDialog(
            outletName = singleReport.outletName,
            dateLabel = singleReport.fullDateLabel,
            initialReason = singleReport.reviewNote.orEmpty(),
            isSaving = isActionLoading,
            onDismiss = { showRejectDialog = false },
            onConfirm = { reason ->
                showRejectDialog = false
                onRejectReport(singleReport.id, reason)
            }
        )
    }
}

@Composable
private fun OwnerReportReviewActionCard(
    report: SingleReportDetailUi,
    isActionLoading: Boolean,
    onApproveClick: () -> Unit,
    onRejectClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val status = report.reviewStatus
    val isPendingAnomaly = status == ReportReviewStatus.PENDING && report.hasAnomaly
    val isApproved = status == ReportReviewStatus.APPROVED
    val isRejected = status == ReportReviewStatus.REJECTED

    val containerColor = when {
        isRejected -> SBColor.Error_100.copy(alpha = 0.6f)
        isApproved -> SBColor.Success_100.copy(alpha = 0.6f)
        isPendingAnomaly -> SBColor.Warning_100.copy(alpha = 0.8f)
        else -> Color.White
    }
    val borderColor = when {
        isRejected -> SBColor.Primary_200
        isApproved -> SBColor.Success_300
        isPendingAnomaly -> SBColor.Secondary_200
        else -> SBColor.Grey_100
    }

    OutlinedCard(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.outlinedCardColors(containerColor = containerColor),
        border = BorderStroke(1.dp, borderColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f).padding(end = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    val iconRes = when {
                        isApproved -> R.drawable.ic_check
                        isRejected || isPendingAnomaly -> R.drawable.ic_warning
                        else -> R.drawable.ic_info
                    }
                    val iconTint = when {
                        isApproved -> SBColor.Success_600
                        isRejected -> SBColor.Primary_550
                        isPendingAnomaly -> SBColor.Warning_600
                        else -> SBColor.Grey_600
                    }
                    Icon(
                        painter = painterResource(iconRes),
                        contentDescription = null,
                        tint = iconTint,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = when {
                            isApproved -> "Laporan disetujui"
                            isRejected -> "Laporan ditolak Owner"
                            isPendingAnomaly -> "Wajib ditinjau Owner"
                            else -> "Tinjauan laporan (opsional)"
                        },
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = SBColor.Grey_700
                    )
                }
                when {
                    isApproved -> SBStatusBadge(
                        text = "Disetujui",
                        containerColor = Color.White,
                        contentColor = SBColor.Success_600
                    )
                    isRejected -> SBStatusBadge(
                        text = "Ditolak",
                        containerColor = Color.White,
                        contentColor = SBColor.Primary_550
                    )
                    isPendingAnomaly -> SBStatusBadge(
                        text = "Perlu ditinjau",
                        containerColor = Color.White,
                        contentColor = SBColor.Warning_600
                    )
                    else -> SBStatusBadge(
                        text = "Normal",
                        containerColor = SBColor.Success_100,
                        contentColor = SBColor.Success_600
                    )
                }
            }

            when {
                isPendingAnomaly -> {
                    Text(
                        text = report.anomalyReasonText
                            ?: "Laporan ini memiliki stok rusak tinggi atau pengeluaran ekstra yang perlu diverifikasi.",
                        fontSize = 12.sp,
                        color = SBColor.Grey_700
                    )
                }
                isRejected -> {
                    Text(
                        text = "Alasan penolakan: ${report.reviewNote ?: "Perlu diperbaiki oleh staf outlet."}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = SBColor.Primary_550
                    )
                    if (!report.reviewedAtLabel.isNullOrBlank()) {
                        Text(
                            text = "Ditinjau oleh ${report.reviewedByName ?: "Owner"} • ${report.reviewedAtLabel}",
                            fontSize = 11.sp,
                            color = SBColor.Grey_500
                        )
                    }
                }
                isApproved -> {
                    Text(
                        text = report.reviewNote ?: "Laporan telah diverifikasi dan disetujui oleh Owner.",
                        fontSize = 12.sp,
                        color = SBColor.Grey_700
                    )
                    if (!report.reviewedAtLabel.isNullOrBlank()) {
                        Text(
                            text = "Disetujui oleh ${report.reviewedByName ?: "Owner"} • ${report.reviewedAtLabel}",
                            fontSize = 11.sp,
                            color = SBColor.Grey_500
                        )
                    }
                }
                else -> {
                    Text(
                        text = "Laporan ini tidak memiliki anomali sehingga tidak wajib di-ACC. Namun, Anda tetap dapat menyetujui (ACC) atau menolak laporan ini.",
                        fontSize = 12.sp,
                        color = SBColor.Grey_600
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (status != ReportReviewStatus.REJECTED) {
                    OutlinedButton(
                        onClick = onRejectClick,
                        enabled = !isActionLoading,
                        modifier = Modifier
                            .weight(1f)
                            .height(42.dp),
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, SBColor.Primary_300),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = Color.White,
                            contentColor = SBColor.Primary_550
                        ),
                        contentPadding = PaddingValues(horizontal = 12.dp)
                    ) {
                        Text(
                            text = "Tolak",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                } else {
                    OutlinedButton(
                        onClick = onRejectClick,
                        enabled = !isActionLoading,
                        modifier = Modifier
                            .weight(1f)
                            .height(42.dp),
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, SBColor.Primary_300),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = Color.White,
                            contentColor = SBColor.Primary_550
                        ),
                        contentPadding = PaddingValues(horizontal = 12.dp)
                    ) {
                        Text(
                            text = "Ubah alasan",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                if (status != ReportReviewStatus.APPROVED) {
                    Button(
                        onClick = onApproveClick,
                        enabled = !isActionLoading,
                        modifier = Modifier
                            .weight(1f)
                            .height(42.dp),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = SBColor.Success_600,
                            contentColor = Color.White
                        ),
                        contentPadding = PaddingValues(horizontal = 12.dp)
                    ) {
                        Text(
                            text = if (isActionLoading) "Menyimpan..." else "Setujui",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun RejectDailyReportDialog(
    outletName: String,
    dateLabel: String,
    initialReason: String,
    isSaving: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var reason by rememberSaveable(initialReason) { mutableStateOf(initialReason) }
    var errorText by rememberSaveable { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = { if (!isSaving) onDismiss() },
        containerColor = Color.White,
        title = {
            Text(
                text = "Tolak laporan harian?",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = SBColor.Grey_700
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "$outletName • $dateLabel\nStaf outlet akan menerima kartu notifikasi di dashboard mereka untuk memperbaiki laporan ini.",
                    fontSize = 13.sp,
                    color = SBColor.Grey_600
                )

                Text(
                    text = "Pilih alasan cepat:",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = SBColor.Grey_700
                )
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    QUICK_REJECT_REASONS.forEach { preset ->
                        val isSelected = reason.trim().equals(preset, ignoreCase = true)
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isSelected) SBColor.Primary_100 else SBColor.Grey_100,
                            border = BorderStroke(
                                width = 1.dp,
                                color = if (isSelected) SBColor.Primary_300 else Color.Transparent
                            ),
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .clickable {
                                    reason = preset
                                    errorText = null
                                }
                        ) {
                            Text(
                                text = preset,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (isSelected) SBColor.Primary_550 else SBColor.Grey_700,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = reason,
                    onValueChange = {
                        reason = it
                        errorText = null
                    },
                    label = { Text("Alasan penolakan (wajib)") },
                    placeholder = { Text("Contoh: Lampirkan nota pembelian minyak & gas...") },
                    minLines = 2,
                    maxLines = 4,
                    isError = errorText != null,
                    supportingText = errorText?.let { err ->
                        { Text(err, color = SBColor.Primary_550, fontSize = 11.sp) }
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = SBColor.Primary_550,
                        unfocusedBorderColor = SBColor.Grey_200,
                        cursorColor = SBColor.Primary_550
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val clean = reason.trim()
                    if (clean.isBlank()) {
                        errorText = "Tuliskan atau pilih alasan penolakan terlebih dahulu."
                    } else {
                        onConfirm(clean)
                    }
                },
                enabled = !isSaving,
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = SBColor.Primary_550,
                    contentColor = Color.White
                )
            ) {
                Text(
                    text = if (isSaving) "Mengirim..." else "Tolak & beri tahu staf",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                enabled = !isSaving
            ) {
                Text("Batal", color = SBColor.Grey_600, fontWeight = FontWeight.SemiBold)
            }
        }
    )
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
