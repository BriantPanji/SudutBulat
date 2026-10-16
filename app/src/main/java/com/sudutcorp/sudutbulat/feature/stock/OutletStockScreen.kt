package com.sudutcorp.sudutbulat.feature.stock

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
fun OutletStockScreen(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    val focusManager = LocalFocusManager.current
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf("Semua") }
    val filterOptions = listOf("Semua", "Aman", "Menipis")

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
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("Staf Outlet", fontSize = 14.sp, color = SBColor.Grey_500)
                    Text(
                        "Stok",
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

            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = {
                    Text(
                        "Cari produk atau bahan...",
                        fontSize = 14.sp,
                        color = SBColor.Grey_500
                    )
                },
                leadingIcon = {
                    Icon(
                        painter = painterResource(R.drawable.ic_search),
                        contentDescription = "Cari",
                        tint = SBColor.Grey_500,
                        modifier = Modifier.size(18.dp)
                    )
                },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = SBColor.Primary,
                    unfocusedBorderColor = SBColor.Grey_200,
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White
                )
            )

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
                    Text("Stok Outlet Marelan", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                    Text("Terakhir diperbarui 09.17", fontSize = 12.sp, color = SBColor.Grey_500)
                }
            }

            StockItemCard(
                title = "Tahu Original",
                subtitle = "878 pcs",
                status = StockStatus.SAFE,
                progress = 0.86f,
                onCardClick = {

                }
            )

            StockItemCard(
                title = "Tahu Pedas",
                subtitle = "312 pcs",
                status = StockStatus.LOW,
                progress = 0.70f,
                onCardClick = {

                }
            )

            StockItemCard(
                title = "Tahu Bakso",
                subtitle = "1000 pcs",
                status = StockStatus.SAFE,
                progress = 1f,
                onCardClick = {

                }
            )

            StockItemCard(
                title = "Bumbu Original",
                subtitle = "6 pak",
                status = StockStatus.OUT_OF_STOCK,
                progress = 0.10f,
                onCardClick = {

                }
            )
        }
    }
}

@Composable
fun StockItemCard(
    title: String,
    subtitle: String,
    status: StockStatus,
    progress: Float,
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
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Surface(
                    modifier = Modifier.size(40.dp),
                    shape = RoundedCornerShape(10.dp),
                    color = SBColor.Primary_100
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            painter = painterResource(R.drawable.ic_box),
                            contentDescription = null,
                            modifier = Modifier.size(20.dp),
                            tint = Color(0xFFCD3009)
                        )
                    }
                }

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Text(
                        text = title,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.Black
                    )
                    Text(
                        text = subtitle,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = SBColor.Grey_500
                    )
                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp)
                            .height(6.dp)
                            .clip(CircleShape),
                        color = SBColor.Primary,
                        trackColor = SBColor.Grey_100,
                        strokeCap = StrokeCap.Round,
                        gapSize = 0.dp,
                        drawStopIndicator = {}
                    )
                }

                StockStatusChip(status = status)

                IconButton(
                    onClick = onCardClick,
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_chevron_right),
                        contentDescription = "Detail Outlet",
                        tint = SBColor.Grey_500,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

enum class StockStatus {
    SAFE,
    LOW,
    OUT_OF_STOCK
}

@Composable
fun StockStatusChip(
    status: StockStatus,
    modifier: Modifier = Modifier
) {
    val (label, containerColor, contentColor) = when (status) {
        StockStatus.SAFE -> Triple(
            "Aman",
            SBColor.Success_100,
            SBColor.Success_600
        )

        StockStatus.LOW -> Triple(
            "Menipis",
            SBColor.Warning_100,
            SBColor.Warning_600
        )

        StockStatus.OUT_OF_STOCK -> Triple(
            "Habis",
            SBColor.Error_600,
            SBColor.Error_100
        )
    }

    Surface(
        modifier = modifier.width(72.dp),
        shape = RoundedCornerShape(6.dp),
        color = containerColor,
        contentColor = contentColor
    ) {
        Text(
            text = label,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 6.dp),
            textAlign = TextAlign.Center,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold
        )
    }
}
