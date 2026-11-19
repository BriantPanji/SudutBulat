package com.sudutcorp.sudutbulat.feature.outlet

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.sudutcorp.sudutbulat.R
import com.sudutcorp.sudutbulat.core.ui.theme.SBColor
import com.sudutcorp.sudutbulat.feature.owner.OwnerUiEvent
import com.sudutcorp.sudutbulat.feature.owner.OwnerUiState
import com.sudutcorp.sudutbulat.feature.owner.formatRupiah

@Composable
fun OutletScreen(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    uiState: OwnerUiState = OwnerUiState(),
    onEvent: (OwnerUiEvent) -> Unit = {},
    isDevMode: Boolean = false,
    onSwitchRoleClick: () -> Unit = {},
    onOutletClick: (String) -> Unit = {},
    onAddOutletClick: () -> Unit = {},
    isStandaloneListScreen: Boolean = false,
    onBackClick: () -> Unit = {}
) {
    val focusManager = LocalFocusManager.current
    val isManagement = isStandaloneListScreen || uiState.isManagementMode
    val query = uiState.outletSearchQuery.trim().lowercase()

    val filteredOutlets = remember(uiState.outletItems, query, uiState.outletFilter) {
        uiState.outletItems.filter { item ->
            val matchesFilter = when (uiState.outletFilter) {
                "Buka" -> item.isOpen
                "Tutup" -> !item.isOpen
                else -> true
            }
            val matchesQuery = query.isBlank() ||
                item.name.lowercase().contains(query) ||
                item.displayName.lowercase().contains(query) ||
                item.address.lowercase().contains(query) ||
                item.staffNames.any { it.lowercase().contains(query) }
            matchesFilter && matchesQuery
        }
    }

    val totalCount = uiState.outletItems.size
    val openCount = uiState.openOutletsCount
    val closedCount = uiState.closedOutletsCount

    val filterOptions = listOf(
        "Semua" to if (isManagement) "Semua $totalCount" else "Semua",
        "Buka" to if (isManagement) "Buka $openCount" else "Buka",
        "Tutup" to if (isManagement) "Tutup $closedCount" else "Tutup"
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White)
            .then(
                if (isStandaloneListScreen) {
                    Modifier.statusBarsPadding().navigationBarsPadding()
                } else {
                    Modifier
                }
            )
            .pointerInput(Unit) {
                detectTapGestures(onTap = {
                    focusManager.clearFocus()
                })
            }
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
            .padding(top = 16.dp, bottom = 80.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (isStandaloneListScreen) {
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
                }
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        "Owner",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = SBColor.Grey_500
                    )
                    Text(
                        if (isManagement) "Daftar outlet" else "Outlet",
                        fontSize = 25.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = SBColor.Grey_700
                    )
                }
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
                        .clickable(enabled = isDevMode, onClick = onSwitchRoleClick),
                    shape = CircleShape,
                    color = SBColor.Secondary_100
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = if (isDevMode) "DV" else uiState.ownerInitials,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = SBColor.Primary_500
                        )
                    }
                }
            }
        }

        // Toggle Mode Performa (9:549) vs Daftar Outlet (204:5797) apabila di dalam Tab Outlet
        if (!isStandaloneListScreen) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(color = SBColor.Grey_100, shape = RoundedCornerShape(12.dp))
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                listOf(false to "Performa outlet", true to "Daftar outlet").forEach { (mode, label) ->
                    val isSelected = uiState.isManagementMode == mode
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(36.dp)
                            .clip(RoundedCornerShape(9.dp))
                            .background(if (isSelected) Color.White else Color.Transparent)
                            .clickable { onEvent(OwnerUiEvent.SetOutletManagementMode(mode)) },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = label,
                            fontSize = 13.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
                            color = if (isSelected) SBColor.Grey_700 else SBColor.Grey_500
                        )
                    }
                }
            }
        }

        // Search Bar
        OutlinedTextField(
            value = uiState.outletSearchQuery,
            onValueChange = { onEvent(OwnerUiEvent.OutletSearchChanged(it)) },
            modifier = Modifier.fillMaxWidth(),
            placeholder = {
                Text(
                    if (isManagement) "Cari outlet atau nama staf..." else "Cari outlet...",
                    fontSize = 14.sp,
                    color = SBColor.Grey_500
                )
            },
            leadingIcon = {
                Icon(
                    painter = painterResource(R.drawable.ic_search),
                    contentDescription = "Cari outlet",
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

        // Filter Chips & Action Header
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                filterOptions.forEach { (key, label) ->
                    val isSelected = key == uiState.outletFilter
                    FilterChip(
                        selected = isSelected,
                        onClick = { onEvent(OwnerUiEvent.OutletFilterSelected(key)) },
                        label = {
                            Text(
                                text = label,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        },
                        shape = RoundedCornerShape(9.dp),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = SBColor.Grey_700,
                            selectedLabelColor = Color.White,
                            containerColor = SBColor.Grey_100,
                            labelColor = SBColor.Grey_600
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
                Text(
                    text = if (isManagement) "${filteredOutlets.size} outlet terdaftar" else "${filteredOutlets.size} outlet aktif",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = SBColor.Grey_700
                )
                if (isManagement) {
                    TextButton(
                        onClick = onAddOutletClick,
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)
                    ) {
                        Text(
                            text = "+ Tambah outlet",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = SBColor.Primary_550
                        )
                    }
                } else {
                    TextButton(
                        onClick = { onEvent(OwnerUiEvent.CycleOutletSortOrder) },
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)
                    ) {
                        Text(
                            text = "Urutkan",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = SBColor.Primary_550
                        )
                    }
                }
            }
        }

        // Daftar Outlet (204:5797) atau Kartu Ringkasan Outlet (9:549)
        if (isManagement) {
            OutlinedCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.outlinedCardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, SBColor.Grey_100)
            ) {
                Column {
                    filteredOutlets.forEachIndexed { index, outlet ->
                        OutletManageRow(
                            name = outlet.displayName,
                            address = outlet.address,
                            staffText = "${outlet.activeStaffCount} staf aktif",
                            isOpen = outlet.isOpen,
                            onClick = { onOutletClick(outlet.id) }
                        )
                        if (index < filteredOutlets.lastIndex) {
                            HorizontalDivider(thickness = 1.dp, color = SBColor.Grey_100)
                        }
                    }
                }
            }
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                filteredOutlets.forEach { outlet ->
                    OutletSummaryCard(
                        name = outlet.displayName,
                        address = outlet.address,
                        revenue = formatRupiah(outlet.revenue),
                        sold = "${outlet.piecesSold} pcs",
                        progress = outlet.targetProgress,
                        targetText = outlet.targetPercentText,
                        isOpen = outlet.isOpen,
                        onClick = { onOutletClick(outlet.id) }
                    )
                }
            }
        }
    }
}

@Composable
private fun OutletManageRow(
    name: String,
    address: String,
    staffText: String,
    isOpen: Boolean,
    onClick: () -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Surface(
            modifier = Modifier.size(40.dp),
            shape = RoundedCornerShape(12.dp),
            color = SBColor.Primary_100
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    painter = painterResource(R.drawable.ic_store),
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = SBColor.Tertiary_400
                )
            }
        }

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = name,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = SBColor.Grey_700
            )
            Text(
                text = address,
                fontSize = 12.sp,
                fontWeight = FontWeight.Normal,
                color = SBColor.Grey_600
            )
            Text(
                text = staffText,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = SBColor.Grey_500
            )
        }

        Surface(
            shape = RoundedCornerShape(6.dp),
            color = if (isOpen) SBColor.Success_100 else SBColor.Grey_100
        ) {
            Text(
                text = if (isOpen) "Buka" else "Tutup",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = if (isOpen) SBColor.Success_600 else SBColor.Grey_600,
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
            )
        }

        Icon(
            painter = painterResource(R.drawable.ic_chevron_right),
            contentDescription = null,
            modifier = Modifier.size(16.dp),
            tint = SBColor.Grey_400
        )
    }
}

@Composable
private fun OutletSummaryCard(
    name: String,
    address: String,
    revenue: String,
    sold: String,
    progress: Float,
    targetText: String,
    isOpen: Boolean,
    onClick: () -> Unit = {}
) {
    OutlinedCard(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.outlinedCardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, SBColor.Grey_100)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
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
                                painter = painterResource(R.drawable.ic_store),
                                contentDescription = null,
                                modifier = Modifier.size(20.dp),
                                tint = SBColor.Primary_550
                            )
                        }
                    }
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(name, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = SBColor.Grey_700)
                        Text(address, fontSize = 12.sp, color = SBColor.Grey_500)
                    }
                }
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = if (isOpen) SBColor.Success_100 else SBColor.Grey_100
                ) {
                    Text(
                        text = if (isOpen) "Buka" else "Tutup",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isOpen) SBColor.Success_600 else SBColor.Grey_600,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(revenue, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = SBColor.Grey_700)
                Text(sold, fontSize = 12.sp, fontWeight = FontWeight.Medium, color = SBColor.Grey_500)
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .weight(1f)
                        .height(6.dp)
                        .clip(CircleShape),
                    color = SBColor.Primary,
                    trackColor = SBColor.Grey_100,
                    strokeCap = StrokeCap.Round,
                    gapSize = 0.dp,
                    drawStopIndicator = {}
                )
                Text(targetText, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = SBColor.Grey_600)
            }
        }
    }
}
