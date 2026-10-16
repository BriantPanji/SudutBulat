package com.sudutcorp.sudutbulat.feature.distribution

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.foundation.layout.width
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
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
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
    val focusManager = LocalFocusManager.current
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf("Semua") }
    val filterOptions = listOf("Semua", "Dijadwalkan", "Dikirim", "Selesai")

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White)
            .pointerInput(Unit) {
                detectTapGestures(onTap = {
                    focusManager.clearFocus()
                })
            }
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
            .padding(top = 32.dp, bottom = 80.dp),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header Profile Avatar dengan BadgedBox & Surface M3
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("Admin Gudang", fontSize = 14.sp, color = SBColor.Grey_500)
                    Text(
                        "Distribusi",
                        fontSize = 25.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.Black
                    )
                }
                BadgedBox(
                    badge = {
                        Badge(
                            containerColor = Color(0xFF10B981),
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
                                "AR",
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
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
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
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    filterOptions.forEach { option ->
                        val isSelected = option == selectedFilter
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedFilter = option },
                            label = {
                                Text(
                                    text = option,
                                    fontSize = 14.sp,
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
                    Text("Hari ini", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                    TextButton(
                        onClick = onSeeAllClick,
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Text(
                            text = "Lihat semua",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = SBColor.Primary
                        )
                    }
                }
            }

            // Cards List
            DistributionItemCard(
                title = "Tahu Original",
                subtitle = "9.191",
                status = StockStatus.Terkirim,
                time = "10.30",
                time2 = "11.30",
                onCardClick = {
                    // Navigate to detail page
                }
            )

            DistributionItemCard(
                title = "Tahu Pedas",
                subtitle = "3.980",
                status = StockStatus.DiPerjalanan,
                time = "10.30",
                time2 = "Est. 11.30",
                onCardClick = {
                    // Navigate to detail page
                }
            )

            DistributionItemCard(
                title = "Tahu Bakso",
                subtitle = "5.000",
                status = StockStatus.Dijadwalkan,
                time = "10.30",
                time2 = "10.30",
                onCardClick = {
                    // Navigate to detail page
                }
            )

            DistributionItemCard(
                title = "Bumbu Original",
                subtitle = "48 pak",
                status = StockStatus.Dijadwalkan,
                time = "10.30",
                time2 = "10.30",
                onCardClick = {
                    // Navigate to detail page
                }
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
    onCardClick: () -> Unit
) {
    OutlinedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.outlinedCardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, SBColor.Grey_100)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(4.dp, 11.dp),
            verticalArrangement = Arrangement.Center
        ) {
            // Header Row: Icon + Title/Subtitle (weight 1f) + Status Badge + Chevron Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
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
                            tint = Color(0xFFCD3009)
                        )
                    }
                }

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(2.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween){
                        Text(
                            text = title,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )
                        StockStatusChip(status = status)
                    }
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Row() {
                            Text(
                                text = subtitle + "•" ,
                                fontSize = 12.sp,
                                color = SBColor.Grey_500
                            )
                            Text(
                                text = time ,
                                fontSize = 12.sp,
                                color = SBColor.Grey_500
                            )
                        }
                        Text(
                            text = time2 ,
                            fontSize = 12.sp,
                            color = SBColor.Grey_500
                        )
                    }
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
    val (label,containerColor, contentColor) = when (status) {
        StockStatus.Terkirim -> Triple(
            "Terkirim",
            Color.White,
            SBColor.Success_600
        )

        StockStatus.DiPerjalanan -> Triple(
            "Perjalanan",
            Color.White,
            SBColor.Warning_600
        )

        StockStatus.Dijadwalkan -> Triple(
            "Dijadwalkan",
            Color.White,
            Color.Red
        )
    }

    Surface(
        modifier = modifier.width(80.dp),
        shape = RoundedCornerShape(6.dp),
        color = containerColor,
        contentColor = contentColor
    ) {
        Text(
            text = label,
            textAlign = TextAlign.End,
            modifier = Modifier
                .fillMaxWidth(),
            fontSize = 12.sp,
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
