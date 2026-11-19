package com.sudutcorp.sudutbulat.feature.outlet

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.sudutcorp.sudutbulat.R
import com.sudutcorp.sudutbulat.core.ui.theme.SBColor
import com.sudutcorp.sudutbulat.data.model.Role
import com.sudutcorp.sudutbulat.feature.owner.OutletSortField
import com.sudutcorp.sudutbulat.feature.owner.OutletTabMode
import com.sudutcorp.sudutbulat.feature.owner.OwnerUiEvent
import com.sudutcorp.sudutbulat.feature.owner.OwnerUiState
import com.sudutcorp.sudutbulat.feature.owner.SortDirection
import com.sudutcorp.sudutbulat.feature.owner.StaffItemUi
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
    onStaffClick: (String) -> Unit = {},
    onAddStaffAccountClick: () -> Unit = {},
    isStandaloneListScreen: Boolean = false,
    onBackClick: () -> Unit = {}
) {
    val focusManager = LocalFocusManager.current
    val activeTab = if (isStandaloneListScreen) OutletTabMode.OUTLET_LIST else uiState.outletTabMode
    var showSortMenu by rememberSaveable { mutableStateOf(false) }

    val outletQuery = uiState.outletSearchQuery.trim().lowercase()
    val filteredOutlets = remember(uiState.outletItems, outletQuery, uiState.outletFilter) {
        uiState.outletItems.filter { item ->
            val matchesFilter = when (uiState.outletFilter) {
                "Buka" -> item.isOpen
                "Tutup" -> !item.isOpen
                else -> true
            }
            val matchesQuery = outletQuery.isBlank() ||
                item.name.lowercase().contains(outletQuery) ||
                item.displayName.lowercase().contains(outletQuery) ||
                item.address.lowercase().contains(outletQuery) ||
                item.staffNames.any { it.lowercase().contains(outletQuery) }
            matchesFilter && matchesQuery
        }
    }

    val staffQuery = uiState.staffSearchQuery.trim().lowercase()
    val filteredStaff = remember(uiState.staffItems, staffQuery, uiState.staffRoleFilter) {
        uiState.staffItems.filter { staff ->
            val matchesRole = when (uiState.staffRoleFilter) {
                "Outlet" -> staff.role == Role.OUTLET
                "Gudang" -> staff.role == Role.WAREHOUSE
                "Owner" -> staff.role == Role.OWNER
                else -> true
            }
            val matchesQuery = staffQuery.isBlank() ||
                staff.name.lowercase().contains(staffQuery) ||
                staff.email.lowercase().contains(staffQuery) ||
                (staff.outletName?.lowercase()?.contains(staffQuery) == true)
            matchesRole && matchesQuery
        }
    }

    val headerTitle = when (activeTab) {
        OutletTabMode.PERFORMANCE -> "Outlet"
        OutletTabMode.OUTLET_LIST -> "Daftar outlet"
        OutletTabMode.STAFF_LIST -> "Daftar staf"
    }

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
                detectTapGestures(onTap = { focusManager.clearFocus() })
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
                        headerTitle,
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

        // 3-Tab Segmented Control: Performa | Outlet | Staf
        if (!isStandaloneListScreen) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(color = SBColor.Grey_100, shape = RoundedCornerShape(12.dp))
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutletTabMode.entries.forEach { mode ->
                    val isSelected = activeTab == mode
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(36.dp)
                            .clip(RoundedCornerShape(9.dp))
                            .background(if (isSelected) Color.White else Color.Transparent)
                            .clickable { onEvent(OwnerUiEvent.OutletTabSelected(mode)) },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = mode.label,
                            fontSize = 13.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
                            color = if (isSelected) SBColor.Grey_700 else SBColor.Grey_500
                        )
                    }
                }
            }
        }

        if (activeTab == OutletTabMode.STAFF_LIST) {
            // === TAB DAFTAR STAF ===
            OutlinedTextField(
                value = uiState.staffSearchQuery,
                onValueChange = { onEvent(OwnerUiEvent.StaffSearchChanged(it)) },
                modifier = Modifier.fillMaxWidth(),
                placeholder = {
                    Text(
                        "Cari nama staf, email, atau outlet...",
                        fontSize = 14.sp,
                        color = SBColor.Grey_500
                    )
                },
                leadingIcon = {
                    Icon(
                        painter = painterResource(R.drawable.ic_search),
                        contentDescription = "Cari staf",
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
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("Semua", "Outlet", "Gudang").forEach { roleOpt ->
                        val isSelected = uiState.staffRoleFilter == roleOpt
                        FilterChip(
                            selected = isSelected,
                            onClick = { onEvent(OwnerUiEvent.StaffRoleFilterSelected(roleOpt)) },
                            label = {
                                Text(
                                    text = roleOpt,
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
                        text = "${filteredStaff.size} pengguna terdaftar",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = SBColor.Grey_700
                    )
                    TextButton(
                        onClick = onAddStaffAccountClick,
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)
                    ) {
                        Text(
                            text = "+ Tambah akun",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = SBColor.Primary_550
                        )
                    }
                }
            }

            OutlinedCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.outlinedCardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, SBColor.Grey_100)
            ) {
                Column {
                    filteredStaff.forEachIndexed { index, staff ->
                        StaffDirectoryRow(
                            staff = staff,
                            onClick = { onStaffClick(staff.id) }
                        )
                        if (index < filteredStaff.lastIndex) {
                            HorizontalDivider(thickness = 1.dp, color = SBColor.Grey_100)
                        }
                    }
                }
            }
        } else {
            // === TAB PERFORMA OUTLET & TAB DAFTAR OUTLET ===
            val isManagement = activeTab == OutletTabMode.OUTLET_LIST
            val totalCount = uiState.outletItems.size
            val openCount = uiState.openOutletsCount
            val closedCount = uiState.closedOutletsCount
            val filterOptions = listOf(
                "Semua" to if (isManagement) "Semua $totalCount" else "Semua",
                "Buka" to if (isManagement) "Buka $openCount" else "Buka",
                "Tutup" to if (isManagement) "Tutup $closedCount" else "Tutup"
            )

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
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        // Compact anchored DropdownMenu for sorting (never fills the screen)
                        Box {
                            val dirArrow = if (uiState.outletSortDirection == SortDirection.DESC) "↓" else "↑"
                            TextButton(
                                onClick = { showSortMenu = true },
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)
                            ) {
                                Text(
                                    text = "${uiState.outletSortField.label} $dirArrow",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = SBColor.Grey_600
                                )
                            }

                            CompactOutletSortDropdown(
                                expanded = showSortMenu,
                                selectedField = uiState.outletSortField,
                                selectedDirection = uiState.outletSortDirection,
                                onDismiss = { showSortMenu = false },
                                onSelectSort = { field, direction ->
                                    onEvent(OwnerUiEvent.OutletSortChanged(field, direction))
                                }
                            )
                        }

                        if (isManagement) {
                            TextButton(
                                onClick = onAddOutletClick,
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)
                            ) {
                                Text(
                                    text = "+ Tambah",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = SBColor.Primary_550
                                )
                            }
                        }
                    }
                }
            }

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
}

@Composable
private fun CompactOutletSortDropdown(
    expanded: Boolean,
    selectedField: OutletSortField,
    selectedDirection: SortDirection,
    onDismiss: () -> Unit,
    onSelectSort: (OutletSortField, SortDirection) -> Unit
) {
    DropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismiss,
        modifier = Modifier
            .width(228.dp)
            .background(Color.White),
        shape = RoundedCornerShape(14.dp),
        containerColor = Color.White
    ) {
        Text(
            text = "Urutkan berdasarkan",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = SBColor.Grey_500,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
        )

        OutletSortField.entries.forEach { field ->
            val isSelected = field == selectedField
            DropdownMenuItem(
                text = {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = field.label,
                            fontSize = 14.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) SBColor.Primary_550 else SBColor.Grey_700
                        )
                        if (isSelected) {
                            Text(
                                text = "✓",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = SBColor.Primary_550
                            )
                        }
                    }
                },
                onClick = {
                    val nextDir = if (isSelected) {
                        if (selectedDirection == SortDirection.DESC) SortDirection.ASC else SortDirection.DESC
                    } else if (field == OutletSortField.NAME) {
                        SortDirection.ASC
                    } else {
                        SortDirection.DESC
                    }
                    onSelectSort(field, nextDir)
                }
            )
        }

        HorizontalDivider(
            thickness = 1.dp,
            color = SBColor.Grey_100,
            modifier = Modifier.padding(vertical = 4.dp)
        )

        val ascLabel = if (selectedField == OutletSortField.NAME) "A – Z ↑" else "Terendah ↑"
        val descLabel = if (selectedField == OutletSortField.NAME) "Z – A ↓" else "Tertinggi ↓"

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            listOf(SortDirection.DESC to descLabel, SortDirection.ASC to ascLabel).forEach { (dir, label) ->
                val isDirSelected = selectedDirection == dir
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(34.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isDirSelected) SBColor.Primary_100 else SBColor.Grey_100)
                        .clickable {
                            onSelectSort(selectedField, dir)
                            onDismiss()
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = label,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isDirSelected) SBColor.Primary_550 else SBColor.Grey_600
                    )
                }
            }
        }
    }
}

@Composable
private fun StaffDirectoryRow(
    staff: StaffItemUi,
    onClick: () -> Unit
) {
    val subtitle = when (staff.role) {
        Role.OUTLET -> staff.outletName ?: "Belum ditugaskan ke outlet"
        Role.WAREHOUSE -> "Gudang Pusat"
        Role.OWNER, Role.DEV -> "Pemilik Bisnis"
    }

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
            shape = CircleShape,
            color = SBColor.Secondary_100
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    text = staff.initials,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = SBColor.Primary_500
                )
            }
        }

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = staff.name,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = SBColor.Grey_700
            )
            Text(
                text = "${staff.role.displayName} • $subtitle",
                fontSize = 12.sp,
                color = SBColor.Grey_500
            )
        }

        Surface(
            shape = RoundedCornerShape(6.dp),
            color = if (staff.isActive) SBColor.Success_100 else SBColor.Grey_100
        ) {
            Text(
                text = if (staff.isActive) "Aktif" else "Nonaktif",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = if (staff.isActive) SBColor.Success_600 else SBColor.Grey_600,
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
