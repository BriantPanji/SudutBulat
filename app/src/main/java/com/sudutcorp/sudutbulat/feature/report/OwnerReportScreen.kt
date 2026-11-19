package com.sudutcorp.sudutbulat.feature.report

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Surface
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
import com.sudutcorp.sudutbulat.core.ui.theme.SBColor
import com.sudutcorp.sudutbulat.feature.owner.DamagedSummaryItemUi
import com.sudutcorp.sudutbulat.feature.owner.OwnerUiEvent
import com.sudutcorp.sudutbulat.feature.owner.OwnerUiState
import com.sudutcorp.sudutbulat.feature.owner.ReportPeriodSummaryUi

@Composable
fun OwnerReportScreen(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    uiState: OwnerUiState = OwnerUiState(),
    reportSummary: ReportPeriodSummaryUi = defaultReportPeriodSummary(),
    onEvent: (OwnerUiEvent) -> Unit = {},
    isDevMode: Boolean = false,
    onSwitchRoleClick: () -> Unit = {},
    onSeeDetailClick: (String) -> Unit = {}
) {
    OwnerReportContent(
        modifier = modifier,
        initials = if (isDevMode) "DV" else uiState.ownerInitials,
        selectedPeriod = uiState.selectedReportPeriod,
        reportSummary = reportSummary,
        isDevMode = isDevMode,
        onPeriodSelected = { onEvent(OwnerUiEvent.ReportPeriodSelected(it)) },
        onProfileClick = onSwitchRoleClick,
        onSeeDetailClick = { onSeeDetailClick(uiState.selectedReportPeriod) }
    )
}

@Composable
fun OwnerReportContent(
    modifier: Modifier = Modifier,
    initials: String = "AR",
    selectedPeriod: String = "30 Hari",
    reportSummary: ReportPeriodSummaryUi = defaultReportPeriodSummary(),
    isDevMode: Boolean = false,
    onPeriodSelected: (String) -> Unit = {},
    onProfileClick: () -> Unit = {},
    onSeeDetailClick: () -> Unit = {}
) {
    val periods = listOf("7 Hari" to "7 hari", "30 Hari" to "30 hari", "3 Bulan" to "3 bulan")

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
            .padding(top = 16.dp, bottom = 80.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "Owner",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp,
                    color = SBColor.Grey_500
                )
                Text(
                    text = "Laporan",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 25.sp,
                    color = SBColor.Grey_700
                )
            }
            BadgedBox(
                badge = {
                    Badge(
                        containerColor = SBColor.Success_500,
                        modifier = Modifier
                            .size(12.dp)
                            .border(2.dp, Color.White, CircleShape)
                    )
                }
            ) {
                Surface(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .clickable(enabled = isDevMode, onClick = onProfileClick),
                    shape = CircleShape,
                    color = SBColor.Secondary_100
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = initials,
                            color = SBColor.Primary_500,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // Filter waktu (Segmented Control)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(color = SBColor.Grey_100, shape = RoundedCornerShape(12.dp))
                .padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            periods.forEach { (periodKey, periodLabel) ->
                val isSelected = periodKey.equals(selectedPeriod, ignoreCase = true)
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(38.dp)
                        .clip(RoundedCornerShape(9.dp))
                        .background(if (isSelected) Color.White else Color.Transparent)
                        .clickable { onPeriodSelected(periodKey) },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        fontSize = 13.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
                        color = if (isSelected) SBColor.Grey_700 else SBColor.Grey_500,
                        text = periodLabel
                    )
                }
            }
        }

        // Omzet
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                color = SBColor.Grey_600,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                text = "Omzet bersih"
            )

            Text(
                fontSize = 28.sp,
                fontWeight = FontWeight.ExtraBold,
                color = SBColor.Grey_700,
                text = reportSummary.netRevenueText
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
                    color = SBColor.Success_600,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    text = reportSummary.deltaText
                )
            }
        }

        // Tren Penjualan
        OutlinedCard(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.outlinedCardColors(containerColor = Color.White),
            border = BorderStroke(1.dp, SBColor.Grey_100)
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
                            color = SBColor.Grey_700
                        )
                        Text(
                            text = reportSummary.dateRangeText,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Normal,
                            color = SBColor.Grey_500
                        )
                    }
                    Text(
                        text = "Rp",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = SBColor.Grey_500
                    )
                }

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
                        val barColor = if (isHighlighted) SBColor.Primary_550 else SBColor.Tertiary_100

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
                                color = SBColor.Grey_500
                            )
                        }
                    }
                }
            }
        }

        // Ringkasan Bisnis
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Ringkasan bisnis",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = SBColor.Grey_700
                )
                TextButton(
                    onClick = onSeeDetailClick,
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)
                ) {
                    Text(
                        text = "Lihat detail",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = SBColor.Primary_550
                    )
                }
            }

            OutlinedCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.outlinedCardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, SBColor.Grey_100)
            ) {
                Column {
                    BusinessSummaryItem(
                        iconRes = R.drawable.ic_store,
                        title = "Total penjualan",
                        subtitle = reportSummary.totalProductsSoldText,
                        value = reportSummary.totalSalesValueText,
                        valueColor = SBColor.Grey_700
                    )

                    HorizontalDivider(
                        color = SBColor.Grey_100,
                        thickness = 1.dp,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )

                    BusinessSummaryItem(
                        iconRes = R.drawable.ic_wallet,
                        title = "Biaya operasional",
                        subtitle = reportSummary.operationalCostPercentText,
                        value = reportSummary.operationalCostValueText,
                        valueColor = SBColor.Grey_700
                    )

                    HorizontalDivider(
                        color = SBColor.Grey_100,
                        thickness = 1.dp,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )

                    BusinessSummaryItem(
                        iconRes = R.drawable.ic_bar_chart,
                        title = "Estimasi laba",
                        subtitle = "Setelah biaya operasional",
                        value = reportSummary.estimatedProfitValueText,
                        valueColor = SBColor.Success_600
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
            Surface(
                modifier = Modifier.size(40.dp),
                shape = RoundedCornerShape(10.dp),
                color = SBColor.Primary_100
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        painter = painterResource(id = iconRes),
                        contentDescription = title,
                        tint = SBColor.Primary_550,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = SBColor.Grey_700
                )
                Text(
                    text = subtitle,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Normal,
                    color = SBColor.Grey_500
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

internal fun defaultReportPeriodSummary(): ReportPeriodSummaryUi = ReportPeriodSummaryUi(
    periodLabel = "30 hari",
    dateRangeText = "24 Apr – 24 Mei",
    netRevenueText = "Rp184.650.000",
    deltaText = "8,7% dibanding periode lalu",
    totalSalesValueText = "Rp184,6jt",
    totalProductsSoldText = "36.930 produk",
    totalProductsPcsText = "36.930 pcs",
    operationalCostValueText = "Rp23,7jt",
    operationalCostPercentText = "12,8% dari omzet",
    estimatedProfitValueText = "Rp62,4jt",
    cashSalesText = "Rp102.340.000",
    qrisSalesText = "Rp82.310.000",
    gasExpenseText = "Rp9.480.000",
    plasticExpenseText = "Rp7.120.000",
    otherExpenseText = "Rp7.100.000",
    totalExpenseText = "Rp23.700.000",
    damagedItems = listOf(
        DamagedSummaryItemUi("mat_tahu_original", "Tahu Original", "86 pcs"),
        DamagedSummaryItemUi("mat_bumbu_balado", "Bumbu", "7 pak")
    )
)

@Preview(showBackground = true)
@Composable
fun OwnerReportScreenPreview() {
    OwnerReportContent()
}
