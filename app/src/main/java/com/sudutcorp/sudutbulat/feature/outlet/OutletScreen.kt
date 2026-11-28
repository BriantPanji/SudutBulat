package com.sudutcorp.sudutbulat.feature.outlet

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedCard
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
import com.sudutcorp.sudutbulat.core.components.SBAvatar
import com.sudutcorp.sudutbulat.core.components.SBFilterChipRow
import com.sudutcorp.sudutbulat.core.components.SBPageHeader
import com.sudutcorp.sudutbulat.core.components.SBSearchBar
import com.sudutcorp.sudutbulat.core.components.SBSegmentedTabs
import com.sudutcorp.sudutbulat.core.components.SBSortDropdownButton
import com.sudutcorp.sudutbulat.core.components.SBStatusBadge
import com.sudutcorp.sudutbulat.core.ui.theme.SBColor
import com.sudutcorp.sudutbulat.core.util.formatRupiah
import com.sudutcorp.sudutbulat.data.model.Role
import com.sudutcorp.sudutbulat.feature.owner.OutletSortField
import com.sudutcorp.sudutbulat.feature.owner.OutletTabMode
import com.sudutcorp.sudutbulat.feature.owner.OwnerUiEvent
import com.sudutcorp.sudutbulat.feature.owner.OwnerUiState
import com.sudutcorp.sudutbulat.feature.owner.SortDirection
import com.sudutcorp.sudutbulat.feature.owner.StaffItemUi

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
                if (isStandaloneListScreen) Modifier.statusBarsPadding().navigationBarsPadding() else Modifier
            )
            .pointerInput(Unit) { detectTapGestures(onTap = { focusManager.clearFocus() }) }
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
            .padding(top = 16.dp, bottom = 80.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        SBPageHeader(
            eyebrow = "Owner",
            title = headerTitle,
            initials = if (isDevMode) "DV" else uiState.ownerInitials,
            isAvatarClickable = isDevMode,
            onAvatarClick = onSwitchRoleClick,
            onBackClick = if (isStandaloneListScreen) onBackClick else null
        )

        if (!isStandaloneListScreen) {
            SBSegmentedTabs(
                options = OutletTabMode.entries,
                selected = activeTab,
                labelOf = { it.label },
                onSelected = { onEvent(OwnerUiEvent.OutletTabSelected(it)) }
            )
        }

        if (activeTab == OutletTabMode.STAFF_LIST) {
            SBSearchBar(
                query = uiState.staffSearchQuery,
                onQueryChange = { onEvent(OwnerUiEvent.StaffSearchChanged(it)) },
                placeholder = "Cari nama staf, email, atau outlet..."
            )

            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                SBFilterChipRow(
                    options = listOf("Semua" to "Semua", "Outlet" to "Outlet", "Gudang" to "Gudang"),
                    selectedKey = uiState.staffRoleFilter,
                    onSelected = { onEvent(OwnerUiEvent.StaffRoleFilterSelected(it)) }
                )

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
            val isManagement = activeTab == OutletTabMode.OUTLET_LIST
            val filterOptions = listOf(
                "Semua" to if (isManagement) "Semua ${uiState.outletItems.size}" else "Semua",
                "Buka" to if (isManagement) "Buka ${uiState.openOutletsCount}" else "Buka",
                "Tutup" to if (isManagement) "Tutup ${uiState.closedOutletsCount}" else "Tutup"
            )

            SBSearchBar(
                query = uiState.outletSearchQuery,
                onQueryChange = { onEvent(OwnerUiEvent.OutletSearchChanged(it)) },
                placeholder = if (isManagement) "Cari outlet atau nama staf..." else "Cari outlet..."
            )

            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                SBFilterChipRow(
                    options = filterOptions,
                    selectedKey = uiState.outletFilter,
                    onSelected = { onEvent(OwnerUiEvent.OutletFilterSelected(it)) }
                )

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
                        SBSortDropdownButton(
                            fields = OutletSortField.entries,
                            selectedField = uiState.outletSortField,
                            isDescending = uiState.outletSortDirection == SortDirection.DESC,
                            labelOf = { it.label },
                            isAlphaField = { it == OutletSortField.NAME },
                            defaultDescendingFor = { it != OutletSortField.NAME },
                            onSelectSort = { field, isDesc ->
                                val dir = if (isDesc) SortDirection.DESC else SortDirection.ASC
                                onEvent(OwnerUiEvent.OutletSortChanged(field, dir))
                            }
                        )

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
        SBAvatar(
            initials = staff.initials,
            photoUrl = staff.photoUrl,
            size = 40.dp,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold
        )

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(text = staff.name, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = SBColor.Grey_700)
            Text(text = "${staff.role.displayName} • $subtitle", fontSize = 12.sp, color = SBColor.Grey_500)
        }

        SBStatusBadge(
            text = if (staff.isActive) "Aktif" else "Nonaktif",
            containerColor = if (staff.isActive) SBColor.Success_100 else SBColor.Grey_100,
            contentColor = if (staff.isActive) SBColor.Success_600 else SBColor.Grey_600
        )

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
            Text(text = name, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = SBColor.Grey_700)
            Text(text = address, fontSize = 12.sp, color = SBColor.Grey_600)
            Text(text = staffText, fontSize = 12.sp, fontWeight = FontWeight.Medium, color = SBColor.Grey_500)
        }

        SBStatusBadge(
            text = if (isOpen) "Buka" else "Tutup",
            containerColor = if (isOpen) SBColor.Success_100 else SBColor.Grey_100,
            contentColor = if (isOpen) SBColor.Success_600 else SBColor.Grey_600
        )

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
                SBStatusBadge(
                    text = if (isOpen) "Buka" else "Tutup",
                    containerColor = if (isOpen) SBColor.Success_100 else SBColor.Grey_100,
                    contentColor = if (isOpen) SBColor.Success_600 else SBColor.Grey_600
                )
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
