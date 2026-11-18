package com.sudutcorp.sudutbulat.feature.distribution

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.sudutcorp.sudutbulat.R
import com.sudutcorp.sudutbulat.core.ui.theme.SBColor
import com.sudutcorp.sudutbulat.core.ui.theme.SudutBulatTheme

@Composable
fun DistributionScreen(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    onCreateDistributionClick: () -> Unit = {},
    onSeeAllClick: () -> Unit = {}
) {
    var selectedFilter by rememberSaveable { mutableStateOf("Semua") }
    val filterOptions = listOf("Semua", "Dijadwalkan", "Dikirim", "Selesai")

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
            .padding(top = 32.dp, bottom = 80.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header Profile Avatar dengan BadgedBox & Surface M3
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    "Admin Gudang",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = SBColor.Grey_500
                )
                Text(
                    "Distribusi",
                    fontSize = 25.sp,
                    fontWeight = FontWeight.ExtraBold,
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
                    modifier = Modifier.size(44.dp),
                    shape = CircleShape,
                    color = SBColor.Secondary_100
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            "BS",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = SBColor.Primary_500
                        )
                    }
                }
            }
        }

        // Button Buat Distribusi
        Button(
            onClick = onCreateDistributionClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = SBColor.Primary_550,
                contentColor = Color.White
            )
        ) {
            Text(
                text = "Buat distribusi baru",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold
            )
        }

        // Filter Chips & Info Header
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                filterOptions.forEach { option ->
                    val isSelected = option == selectedFilter
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedFilter = option },
                        label = {
                            Text(
                                text = option,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        },
                        shape = RoundedCornerShape(10.dp),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = SBColor.Grey_700,
                            selectedLabelColor = Color.White,
                            containerColor = SBColor.Grey_100,
                            labelColor = SBColor.Grey_700
                        ),
                        border = null
                    )
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Hari ini", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = SBColor.Grey_700)
                TextButton(
                    onClick = onSeeAllClick,
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)
                ) {
                    Text(
                        text = "Lihat semua",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = SBColor.Primary_550
                    )
                }
            }
        }

        // Cards List
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            DistributionItemCard(
                title = "Outlet Sudirman",
                subtitle = "1.200 pcs",
                status = StockStatus.Terkirim,
                time = "08.15",
                time2 = "Tiba 09.10",
                onCardClick = {}
            )

            DistributionItemCard(
                title = "Outlet Cikini",
                subtitle = "900 pcs",
                status = StockStatus.DiPerjalanan,
                time = "10.30",
                time2 = "Est. 11.30",
                onCardClick = {}
            )

            DistributionItemCard(
                title = "Outlet Tebet",
                subtitle = "1.050 pcs",
                status = StockStatus.Dijadwalkan,
                time = "13.00",
                time2 = "Est. 14.00",
                onCardClick = {}
            )

            DistributionItemCard(
                title = "Outlet Gajah Mada",
                subtitle = "840 pcs",
                status = StockStatus.Dijadwalkan,
                time = "15.00",
                time2 = "Est. 16.00",
                onCardClick = {}
            )
        }
    }
}

@Composable
fun DistributionItemCard(
    title: String,
    subtitle: String,
    status: StockStatus,
    time: String,
    time2: String,
    onCardClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedCard(
        onClick = onCardClick,
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.outlinedCardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, SBColor.Grey_100)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
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
                        painter = painterResource(R.drawable.ic_truck),
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                        tint = SBColor.Primary_550
                    )
                }
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = title,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = SBColor.Grey_700
                    )
                    StockStatusChip(status = status)
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "$subtitle • $time",
                        fontSize = 12.sp,
                        color = SBColor.Grey_500
                    )
                    Text(
                        text = time2,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = SBColor.Grey_500
                    )
                }
            }
        }
    }
}

enum class StockStatus {
    Terkirim,
    DiPerjalanan,
    Dijadwalkan
}

@Composable
fun StockStatusChip(
    status: StockStatus,
    modifier: Modifier = Modifier
) {
    val (label, containerColor, contentColor) = when (status) {
        StockStatus.Terkirim -> Triple(
            "Terkirim",
            SBColor.Success_100,
            SBColor.Success_600
        )

        StockStatus.DiPerjalanan -> Triple(
            "Di perjalanan",
            SBColor.Warning_100,
            SBColor.Warning_600
        )

        StockStatus.Dijadwalkan -> Triple(
            "Dijadwalkan",
            SBColor.Grey_100,
            SBColor.Grey_600
        )
    }

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(6.dp),
        color = containerColor,
        contentColor = contentColor
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Preview(showSystemUi = true, showBackground = true)
@Composable
private fun DistributionScreenPreview() {
    SudutBulatTheme {
        DistributionScreen(navController = rememberNavController())
    }
}
