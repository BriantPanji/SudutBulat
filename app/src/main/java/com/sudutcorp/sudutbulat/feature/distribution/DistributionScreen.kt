package com.sudutcorp.sudutbulat.feature.distribution

import androidx.activity.compose.BackHandler
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.sudutcorp.sudutbulat.R
import com.sudutcorp.sudutbulat.core.components.SBBottomCtaBar
import com.sudutcorp.sudutbulat.core.components.SBDatePickerDialog
import com.sudutcorp.sudutbulat.core.components.SBFilterChipRow
import com.sudutcorp.sudutbulat.core.components.SBKeyValueRow
import com.sudutcorp.sudutbulat.core.components.SBPageHeader
import com.sudutcorp.sudutbulat.core.components.SBSearchBar
import com.sudutcorp.sudutbulat.core.components.SBSegmentedTabs
import com.sudutcorp.sudutbulat.core.components.SBSelectField
import com.sudutcorp.sudutbulat.core.components.SBSortDropdownButton
import com.sudutcorp.sudutbulat.core.components.SBSubHeader
import com.sudutcorp.sudutbulat.core.ui.theme.SBColor
import com.sudutcorp.sudutbulat.core.ui.theme.SudutBulatTheme
import com.sudutcorp.sudutbulat.data.model.DistributionStatus
import com.sudutcorp.sudutbulat.data.model.DistributionType
import com.sudutcorp.sudutbulat.data.model.Outlet
import com.sudutcorp.sudutbulat.feature.owner.SortDirection
import com.sudutcorp.sudutbulat.navigation.OwnerSubRoutes
import com.sudutcorp.sudutbulat.navigation.navigateSingle
import com.sudutcorp.sudutbulat.navigation.popBackStackSafe
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DistributionScreen(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    uiState: WarehouseUiState = WarehouseUiState(),
    effects: Flow<WarehouseUiEffect> = emptyFlow(),
    onEvent: (WarehouseUiEvent) -> Unit = {},
    initials: String = "BS"
) {
    val focusManager = LocalFocusManager.current
    val gridState = rememberLazyGridState()

    var showDatePicker by rememberSaveable { mutableStateOf(false) }
    var showOutletFilterSheet by rememberSaveable { mutableStateOf(false) }
    var showOutletPresetListSheet by rememberSaveable { mutableStateOf(false) }
    var editingPresetOutletId by rememberSaveable { mutableStateOf<String?>(null) }
    var detailDistributionId by rememberSaveable { mutableStateOf<String?>(null) }
    var rejectingDistributionId by rememberSaveable { mutableStateOf<String?>(null) }

    LaunchedEffect(effects) {
        effects.collect { effect ->
            if (effect is WarehouseUiEffect.RoutinePresetSaved) {
                editingPresetOutletId = null
            }
        }
    }

    val activeTab = uiState.selectedTab
    val activeFilter = uiState.filterFor(activeTab)
    val isHistoryTab = activeTab == WarehouseDistributionTab.HISTORY
    val isRoutineTab = activeTab == WarehouseDistributionTab.ROUTINE
    val isAdditionalTab = activeTab == WarehouseDistributionTab.ADDITIONAL

    val statusChipOptions = remember(activeTab, activeFilter.additionalSubTab) {
        DistributionStatusFilter
            .optionsForTab(activeTab, activeFilter.additionalSubTab)
            .map { it.name to it.label }
    }

    val selectedOutletFilterName = remember(activeFilter.selectedOutletId, uiState.outlets) {
        activeFilter.selectedOutletId?.let { id ->
            uiState.outlets.firstOrNull { it.id == id }?.let { outlet ->
                if (outlet.name.startsWith("Outlet", ignoreCase = true)) outlet.name else "Outlet ${outlet.name}"
            }
        }
    }

    val filteredCards = remember(uiState.distributionCards, activeTab, activeFilter) {
        filterAndSortDistributions(
            cards = uiState.distributionCards,
            tab = activeTab,
            filter = activeFilter
        )
    }

    val filteredPendingOutlets = remember(uiState.outletsPendingMorning, activeTab, activeFilter) {
        if (isRoutineTab) {
            filterPendingMorningOutlets(uiState.outletsPendingMorning, activeFilter)
        } else {
            emptyList()
        }
    }

    val totalResultCount = if (isRoutineTab && activeFilter.statusFilter == DistributionStatusFilter.NOT_SENT) {
        filteredPendingOutlets.size
    } else {
        filteredCards.size
    }

    val defaultCreateType = if (activeTab == WarehouseDistributionTab.ADDITIONAL) {
        DistributionType.ADDITIONAL
    } else {
        DistributionType.ROUTINE
    }

    val openCreateSheet: (String?, DistributionType) -> Unit = remember(navController, onEvent) {
        { outletId, type ->
            navController.navigateSingle(
                route = OwnerSubRoutes.CreateDistribution,
                beforeNavigate = {
                    onEvent(WarehouseUiEvent.InitCreateForm(preselectedOutletId = outletId, defaultType = type))
                }
            )
        }
    }

    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 340.dp),
        state = gridState,
        modifier = modifier
            .fillMaxSize()
            .background(Color.White)
            .pointerInput(Unit) { detectTapGestures(onTap = { focusManager.clearFocus() }) },
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 96.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item(key = "dist_header", span = { GridItemSpan(maxLineSpan) }, contentType = "header") {
            SBPageHeader(
                eyebrow = "Admin Gudang",
                title = "Distribusi",
                initials = initials
            )
        }

        item(key = "dist_tabs", span = { GridItemSpan(maxLineSpan) }, contentType = "tabs") {
            SBSegmentedTabs(
                options = WarehouseDistributionTab.entries,
                selected = activeTab,
                labelOf = { tab ->
                    if (tab == WarehouseDistributionTab.ADDITIONAL && uiState.todayPendingAdditionalCount > 0) {
                        "${tab.label} (${uiState.todayPendingAdditionalCount})"
                    } else {
                        tab.label
                    }
                },
                onSelected = { onEvent(WarehouseUiEvent.SelectDistributionTab(it)) }
            )
        }

        if (isAdditionalTab) {
            item(key = "dist_additional_subtabs", span = { GridItemSpan(maxLineSpan) }, contentType = "subtabs") {
                SBSegmentedTabs(
                    options = AdditionalDistributionSubTab.entries,
                    selected = activeFilter.additionalSubTab,
                    labelOf = { subTab ->
                        if (subTab == AdditionalDistributionSubTab.UNCONFIRMED && uiState.todayPendingAdditionalCount > 0) {
                            "${subTab.label} (${uiState.todayPendingAdditionalCount})"
                        } else {
                            subTab.label
                        }
                    },
                    onSelected = { onEvent(WarehouseUiEvent.SelectAdditionalSubTab(it)) }
                )
            }
        }

        if (isRoutineTab) {
            item(key = "dist_routine_progress", span = { GridItemSpan(maxLineSpan) }, contentType = "progress") {
                RoutineProgressCard(
                    completedCount = uiState.outletsCompletedMorningCount,
                    totalActiveCount = uiState.activeOutlets.size,
                    pendingCount = uiState.outletsPendingMorning.size,
                    onManagePresets = { showOutletPresetListSheet = true },
                    onQuickCreate = { openCreateSheet(null, DistributionType.ROUTINE) }
                )
            }
        }

        item(key = "dist_search_row", span = { GridItemSpan(maxLineSpan) }, contentType = "search") {
            val hasSelectedDate = isHistoryTab && activeFilter.selectedDateMillis != null
            val hasSelectedOutlet = activeFilter.selectedOutletId != null
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                SBSearchBar(
                    query = activeFilter.searchQuery,
                    onQueryChange = { onEvent(WarehouseUiEvent.UpdateSearchQuery(activeTab, it)) },
                    placeholder = when (activeTab) {
                        WarehouseDistributionTab.ROUTINE -> "Cari outlet atau bahan hari ini..."
                        WarehouseDistributionTab.ADDITIONAL -> "Cari permintaan atau catatan..."
                        WarehouseDistributionTab.HISTORY -> "Cari outlet, bahan, atau pembuat..."
                    },
                    modifier = Modifier.weight(1f)
                )

                Surface(
                    onClick = { showOutletFilterSheet = true },
                    modifier = Modifier.size(56.dp),
                    shape = RoundedCornerShape(12.dp),
                    color = if (hasSelectedOutlet) SBColor.Primary_100 else Color.White,
                    border = BorderStroke(
                        width = 1.dp,
                        color = if (hasSelectedOutlet) SBColor.Primary_550 else SBColor.Grey_200
                    )
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_store),
                            contentDescription = "Filter outlet distribusi",
                            tint = if (hasSelectedOutlet) SBColor.Primary_550 else SBColor.Grey_600,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }

                if (isHistoryTab) {
                    Surface(
                        onClick = { showDatePicker = true },
                        modifier = Modifier.size(56.dp),
                        shape = RoundedCornerShape(12.dp),
                        color = if (hasSelectedDate) SBColor.Primary_100 else Color.White,
                        border = BorderStroke(
                            width = 1.dp,
                            color = if (hasSelectedDate) SBColor.Primary_550 else SBColor.Grey_200
                        )
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                painter = painterResource(id = R.drawable.calendar),
                                contentDescription = "Filter tanggal distribusi",
                                tint = if (hasSelectedDate) SBColor.Primary_550 else SBColor.Grey_600,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                }
            }
        }

        if (selectedOutletFilterName != null || (isHistoryTab && activeFilter.selectedDateMillis != null)) {
            item(key = "dist_active_filter_chips", span = { GridItemSpan(maxLineSpan) }, contentType = "active_chips") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (selectedOutletFilterName != null) {
                        FilterChip(
                            selected = true,
                            onClick = { onEvent(WarehouseUiEvent.SelectOutletFilter(activeTab, null)) },
                            label = {
                                Text(
                                    text = "$selectedOutletFilterName  ✕",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            },
                            shape = RoundedCornerShape(9.dp),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = SBColor.Primary_100,
                                selectedLabelColor = SBColor.Primary_550
                            ),
                            border = BorderStroke(1.dp, SBColor.Primary_200)
                        )
                    }
                    if (isHistoryTab && activeFilter.selectedDateMillis != null) {
                        val dateLabel = remember(activeFilter.selectedDateMillis) {
                            formatDistributionUtcDate(activeFilter.selectedDateMillis)
                        }
                        FilterChip(
                            selected = true,
                            onClick = { onEvent(WarehouseUiEvent.SelectHistoryDate(null)) },
                            label = {
                                Text(
                                    text = "Tanggal: $dateLabel  ✕",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            },
                            shape = RoundedCornerShape(9.dp),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = SBColor.Primary_100,
                                selectedLabelColor = SBColor.Primary_550
                            ),
                            border = BorderStroke(1.dp, SBColor.Primary_200)
                        )
                    }
                }
            }
        }

        if (statusChipOptions.isNotEmpty()) {
            item(key = "dist_status_chips", span = { GridItemSpan(maxLineSpan) }, contentType = "status_chips") {
                SBFilterChipRow(
                    options = statusChipOptions,
                    selectedKey = activeFilter.statusFilter.name,
                    onSelected = { key ->
                        val chosen = DistributionStatusFilter.entries.firstOrNull { it.name == key }
                            ?: DistributionStatusFilter.ALL
                        onEvent(WarehouseUiEvent.SelectStatusFilter(activeTab, chosen))
                    }
                )
            }
        }

        item(key = "dist_toolbar", span = { GridItemSpan(maxLineSpan) }, contentType = "toolbar") {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val unitLabel = when {
                        isRoutineTab && activeFilter.statusFilter == DistributionStatusFilter.NOT_SENT ->
                            "outlet belum dikirim"
                        isAdditionalTab && activeFilter.additionalSubTab == AdditionalDistributionSubTab.UNCONFIRMED ->
                            "permintaan menunggu"
                        else -> "distribusi"
                    }
                    Text(
                        text = "$totalResultCount $unitLabel",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = SBColor.Grey_700
                    )
                    if (activeFilter.hasActiveFilter) {
                        Text(
                            text = "Reset",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = SBColor.Primary_550,
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .clickable { onEvent(WarehouseUiEvent.ResetTabFilters(activeTab)) }
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    SBSortDropdownButton(
                        fields = DistributionSortField.entries,
                        selectedField = activeFilter.sortField,
                        isDescending = activeFilter.sortDirection == SortDirection.DESC,
                        labelOf = { it.label },
                        isAlphaField = { it == DistributionSortField.OUTLET },
                        defaultDescendingFor = {
                            it == DistributionSortField.TIME || it == DistributionSortField.QUANTITY
                        },
                        ascLabelOf = { field ->
                            when (field) {
                                DistributionSortField.TIME -> "Terlama ↑"
                                DistributionSortField.OUTLET -> "A – Z ↑"
                                DistributionSortField.QUANTITY -> "Tersedikit ↑"
                                DistributionSortField.STATUS -> "Prioritas ↑"
                            }
                        },
                        descLabelOf = { field ->
                            when (field) {
                                DistributionSortField.TIME -> "Terbaru ↓"
                                DistributionSortField.OUTLET -> "Z – A ↓"
                                DistributionSortField.QUANTITY -> "Terbanyak ↓"
                                DistributionSortField.STATUS -> "Selesai ↓"
                            }
                        },
                        onSelectSort = { field, isDesc ->
                            onEvent(
                                WarehouseUiEvent.UpdateSort(
                                    tab = activeTab,
                                    field = field,
                                    direction = if (isDesc) SortDirection.DESC else SortDirection.ASC
                                )
                            )
                        }
                    )

                    if (!isRoutineTab) {
                        TextButton(
                            onClick = { openCreateSheet(null, defaultCreateType) },
                            contentPadding = PaddingValues(horizontal = 6.dp, vertical = 0.dp)
                        ) {
                            Text(
                                text = "+ Kirim",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = SBColor.Primary_550
                            )
                        }
                    }
                }
            }
        }

        if (isRoutineTab && filteredPendingOutlets.isNotEmpty()) {
            item(key = "dist_pending_outlets_header", span = { GridItemSpan(maxLineSpan) }, contentType = "section_label") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Belum dikirim hari ini",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = SBColor.Grey_600
                    )
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = SBColor.Warning_100
                    ) {
                        Text(
                            text = "${filteredPendingOutlets.size} outlet",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = SBColor.Warning_600,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }
            }

            items(
                items = filteredPendingOutlets,
                key = { "pending_outlet_${it.id}" },
                contentType = { "pending_outlet" }
            ) { outlet ->
                PendingRoutineOutletCard(
                    outlet = outlet,
                    onSendClick = { openCreateSheet(outlet.id, DistributionType.ROUTINE) },
                    onEditPresetClick = { editingPresetOutletId = outlet.id }
                )
            }

            if (filteredCards.isNotEmpty()) {
                item(key = "dist_routine_sent_header", span = { GridItemSpan(maxLineSpan) }, contentType = "section_label") {
                    Text(
                        text = "Distribusi rutin hari ini",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = SBColor.Grey_600,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        }

        if (filteredCards.isEmpty() && filteredPendingOutlets.isEmpty()) {
            item(key = "dist_empty", span = { GridItemSpan(maxLineSpan) }, contentType = "empty") {
                EmptyDistributionCard(
                    tab = activeTab,
                    hasActiveFilter = activeFilter.hasActiveFilter,
                    onResetFilters = { onEvent(WarehouseUiEvent.ResetTabFilters(activeTab)) },
                    onCreateClick = { openCreateSheet(null, defaultCreateType) }
                )
            }
        } else {
            items(
                items = filteredCards,
                key = { "dist_${activeTab.name}_${it.id}" },
                contentType = { "distribution_card" }
            ) { card ->
                DistributionCardRow(
                    card = card,
                    isActionLoading = uiState.isActionLoading,
                    showTypeBadge = isHistoryTab,
                    showDateInTime = isHistoryTab,
                    onClick = { detailDistributionId = card.id },
                    onApprove = { onEvent(WarehouseUiEvent.ApproveRequest(card.id)) },
                    onReject = { rejectingDistributionId = card.id }
                )
            }
        }
    }

    if (showOutletFilterSheet) {
        OutletFilterBottomSheet(
            title = "Filter berdasarkan outlet",
            subtitle = "Cari nama atau alamat outlet untuk menyaring daftar distribusi.",
            outlets = if (isHistoryTab) uiState.outlets else uiState.activeOutlets,
            selectedOutletId = activeFilter.selectedOutletId,
            showAllOption = true,
            onDismiss = { showOutletFilterSheet = false },
            onSelectOutlet = { chosenId ->
                onEvent(WarehouseUiEvent.SelectOutletFilter(activeTab, chosenId))
                showOutletFilterSheet = false
            }
        )
    }

    if (showOutletPresetListSheet) {
        OutletPresetListBottomSheet(
            outlets = uiState.activeOutlets,
            onDismiss = { showOutletPresetListSheet = false },
            onSelectOutlet = { outletId ->
                showOutletPresetListSheet = false
                editingPresetOutletId = outletId
            }
        )
    }

    val editingPresetOutlet = editingPresetOutletId?.let { id ->
        uiState.activeOutlets.firstOrNull { it.id == id } ?: uiState.outlets.firstOrNull { it.id == id }
    }
    if (editingPresetOutlet != null) {
        val displayName = if (editingPresetOutlet.name.startsWith("Outlet", ignoreCase = true)) {
            editingPresetOutlet.name
        } else {
            "Outlet ${editingPresetOutlet.name}"
        }
        RoutinePresetEditorBottomSheet(
            outletName = displayName,
            activeMaterials = uiState.activeMaterials,
            initialPreset = editingPresetOutlet.routinePreset,
            isSaving = uiState.isActionLoading,
            onDismiss = { editingPresetOutletId = null },
            onSave = { items ->
                onEvent(WarehouseUiEvent.SaveOutletRoutinePreset(editingPresetOutlet.id, items))
            }
        )
    }

    if (showDatePicker) {
        SBDatePickerDialog(
            initialSelectedDateMillis = uiState.historyFilter.selectedDateMillis,
            onDismiss = { showDatePicker = false },
            onConfirm = { dateMillis ->
                onEvent(WarehouseUiEvent.SelectHistoryDate(dateMillis))
                showDatePicker = false
            },
            onReset = {
                onEvent(WarehouseUiEvent.SelectHistoryDate(null))
                showDatePicker = false
            }
        )
    }

    val detailCard = detailDistributionId?.let { id -> uiState.distributionCards.firstOrNull { it.id == id } }
    if (detailCard != null) {
        DistributionDetailBottomSheet(
            card = detailCard,
            isActionLoading = uiState.isActionLoading,
            onDismiss = { detailDistributionId = null },
            onApprove = {
                onEvent(WarehouseUiEvent.ApproveRequest(detailCard.id))
                detailDistributionId = null
            },
            onReject = {
                detailDistributionId = null
                rejectingDistributionId = detailCard.id
            }
        )
    }

    val rejectingCard = rejectingDistributionId?.let { id -> uiState.distributionCards.firstOrNull { it.id == id } }
    if (rejectingCard != null) {
        RejectDistributionDialog(
            outletName = rejectingCard.outletName,
            onDismiss = { rejectingDistributionId = null },
            onConfirm = { reason ->
                onEvent(WarehouseUiEvent.RejectRequest(rejectingCard.id, reason))
                rejectingDistributionId = null
            }
        )
    }
}

@Composable
private fun RoutineProgressCard(
    completedCount: Int,
    totalActiveCount: Int,
    pendingCount: Int,
    onManagePresets: () -> Unit,
    onQuickCreate: () -> Unit,
    modifier: Modifier = Modifier
) {
    val progress = if (totalActiveCount > 0) {
        (completedCount.toFloat() / totalActiveCount.toFloat()).coerceIn(0f, 1f)
    } else {
        0f
    }
    val allDone = totalActiveCount > 0 && pendingCount == 0

    OutlinedCard(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.outlinedCardColors(
            containerColor = if (allDone) SBColor.Success_100.copy(alpha = 0.45f) else SBColor.Primary_100.copy(alpha = 0.55f)
        ),
        border = BorderStroke(
            width = 1.dp,
            color = if (allDone) SBColor.Success_300 else SBColor.Primary_200
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Text(
                        text = "Distribusi rutin hari ini",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (allDone) SBColor.Success_600 else SBColor.Primary_600
                    )
                    Text(
                        text = "$completedCount / $totalActiveCount outlet terkirim",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = SBColor.Grey_700
                    )
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = onManagePresets,
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, if (allDone) SBColor.Success_300 else SBColor.Primary_200),
                        colors = ButtonDefaults.outlinedButtonColors(containerColor = Color.White),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 8.dp),
                        modifier = Modifier.height(38.dp)
                    ) {
                        Text(
                            text = "Preset",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = SBColor.Grey_700
                        )
                    }
                    Button(
                        onClick = onQuickCreate,
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = SBColor.Primary_550,
                            contentColor = Color.White
                        ),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
                        modifier = Modifier.height(38.dp)
                    ) {
                        Text(
                            text = "+ Kirim",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(7.dp)
                    .clip(RoundedCornerShape(99.dp)),
                color = if (allDone) SBColor.Success_500 else SBColor.Primary_550,
                trackColor = Color.White,
                strokeCap = StrokeCap.Round
            )

            Text(
                text = if (allDone) {
                    "Seluruh outlet aktif sudah menerima pengiriman rutin hari ini."
                } else {
                    "$pendingCount outlet aktif masih menunggu distribusi rutin hari ini."
                },
                fontSize = 12.sp,
                color = SBColor.Grey_600
            )
        }
    }
}

@Composable
private fun PendingRoutineOutletCard(
    outlet: Outlet,
    onSendClick: () -> Unit,
    onEditPresetClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val presetCount = outlet.routinePreset.count { it.quantity > 0 }
    val presetTotalQty = outlet.routinePreset.sumOf { it.quantity }

    OutlinedCard(
        onClick = onSendClick,
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.outlinedCardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, SBColor.Secondary_200)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Surface(
                modifier = Modifier.size(40.dp),
                shape = RoundedCornerShape(10.dp),
                color = SBColor.Warning_100
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        painter = painterResource(R.drawable.ic_store),
                        contentDescription = null,
                        tint = SBColor.Warning_600,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                val title = if (outlet.name.startsWith("Outlet", ignoreCase = true)) {
                    outlet.name
                } else {
                    "Outlet ${outlet.name}"
                }
                Text(
                    text = title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = SBColor.Grey_700,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = if (presetCount > 0) {
                        "Preset: $presetCount bahan • $presetTotalQty unit"
                    } else {
                        "Preset belum diatur"
                    },
                    fontSize = 12.sp,
                    color = if (presetCount > 0) SBColor.Grey_500 else SBColor.Warning_600,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    onClick = onEditPresetClick,
                    shape = RoundedCornerShape(8.dp),
                    color = SBColor.Grey_100
                ) {
                    Text(
                        text = "Preset",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = SBColor.Grey_700,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }
                Surface(
                    onClick = onSendClick,
                    shape = RoundedCornerShape(8.dp),
                    color = SBColor.Primary_100
                ) {
                    Text(
                        text = "Kirim",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = SBColor.Primary_550,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun OutletFilterBottomSheet(
    title: String,
    subtitle: String,
    outlets: List<Outlet>,
    selectedOutletId: String?,
    showAllOption: Boolean,
    onDismiss: () -> Unit,
    onSelectOutlet: (String?) -> Unit
) {
    var searchQuery by rememberSaveable { mutableStateOf("") }
    val filteredOutlets = remember(outlets, searchQuery) {
        val q = searchQuery.trim().lowercase()
        if (q.isBlank()) {
            outlets
        } else {
            outlets.filter { outlet ->
                outlet.name.lowercase().contains(q) ||
                    "outlet ${outlet.name}".lowercase().contains(q) ||
                    outlet.address.lowercase().contains(q)
            }
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = Color.White,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 24.dp)
                .padding(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = title,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = SBColor.Grey_700
                )
                Text(
                    text = subtitle,
                    fontSize = 12.sp,
                    color = SBColor.Grey_500
                )
            }

            SBSearchBar(
                query = searchQuery,
                onQueryChange = { searchQuery = it },
                placeholder = "Cari nama atau alamat outlet..."
            )

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 380.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (showAllOption && searchQuery.isBlank()) {
                    item(key = "outlet_filter_all", contentType = "outlet_filter_all") {
                        val isAllSelected = selectedOutletId == null
                        OutlinedCard(
                            onClick = { onSelectOutlet(null) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.outlinedCardColors(
                                containerColor = if (isAllSelected) SBColor.Primary_100.copy(alpha = 0.45f) else Color.White
                            ),
                            border = BorderStroke(
                                width = if (isAllSelected) 1.5.dp else 1.dp,
                                color = if (isAllSelected) SBColor.Primary_400 else SBColor.Grey_100
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 14.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(
                                    modifier = Modifier.weight(1f),
                                    verticalArrangement = Arrangement.spacedBy(2.dp)
                                ) {
                                    Text(
                                        text = "Semua outlet",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isAllSelected) SBColor.Primary_550 else SBColor.Grey_700
                                    )
                                    Text(
                                        text = "Tampilkan dari seluruh outlet (${outlets.size} outlet)",
                                        fontSize = 12.sp,
                                        color = SBColor.Grey_500
                                    )
                                }
                                RadioButton(
                                    selected = isAllSelected,
                                    onClick = { onSelectOutlet(null) },
                                    colors = RadioButtonDefaults.colors(selectedColor = SBColor.Primary_550)
                                )
                            }
                        }
                    }
                }

                if (filteredOutlets.isEmpty()) {
                    item(key = "outlet_filter_empty", contentType = "outlet_filter_empty") {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Tidak ada outlet yang cocok dengan pencarian.",
                                fontSize = 13.sp,
                                color = SBColor.Grey_500
                            )
                        }
                    }
                } else {
                    items(
                        items = filteredOutlets,
                        key = { it.id },
                        contentType = { "outlet_filter_item" }
                    ) { outlet ->
                        val isSelected = outlet.id == selectedOutletId
                        val displayName = if (outlet.name.startsWith("Outlet", ignoreCase = true)) {
                            outlet.name
                        } else {
                            "Outlet ${outlet.name}"
                        }
                        OutlinedCard(
                            onClick = { onSelectOutlet(outlet.id) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.outlinedCardColors(
                                containerColor = if (isSelected) SBColor.Primary_100.copy(alpha = 0.45f) else Color.White
                            ),
                            border = BorderStroke(
                                width = if (isSelected) 1.5.dp else 1.dp,
                                color = if (isSelected) SBColor.Primary_400 else SBColor.Grey_100
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 14.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(
                                    modifier = Modifier.weight(1f),
                                    verticalArrangement = Arrangement.spacedBy(2.dp)
                                ) {
                                    Text(
                                        text = displayName,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) SBColor.Primary_550 else SBColor.Grey_700
                                    )
                                    Text(
                                        text = outlet.address.ifBlank { if (outlet.isOpen) "Buka" else "Tutup" },
                                        fontSize = 12.sp,
                                        color = SBColor.Grey_500,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                                RadioButton(
                                    selected = isSelected,
                                    onClick = { onSelectOutlet(outlet.id) },
                                    colors = RadioButtonDefaults.colors(selectedColor = SBColor.Primary_550)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun OutletPresetListBottomSheet(
    outlets: List<Outlet>,
    onDismiss: () -> Unit,
    onSelectOutlet: (String) -> Unit
) {
    var searchQuery by rememberSaveable { mutableStateOf("") }
    val filteredOutlets = remember(outlets, searchQuery) {
        val q = searchQuery.trim().lowercase()
        if (q.isBlank()) {
            outlets
        } else {
            outlets.filter { outlet ->
                outlet.name.lowercase().contains(q) ||
                    "outlet ${outlet.name}".lowercase().contains(q) ||
                    outlet.address.lowercase().contains(q)
            }
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = Color.White,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 24.dp)
                .padding(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = "Preset distribusi rutin",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = SBColor.Grey_700
                )
                Text(
                    text = "Pilih outlet untuk mengatur jumlah bahan default pengiriman rutin.",
                    fontSize = 12.sp,
                    color = SBColor.Grey_500
                )
            }

            SBSearchBar(
                query = searchQuery,
                onQueryChange = { searchQuery = it },
                placeholder = "Cari nama atau alamat outlet..."
            )

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 360.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(
                    items = filteredOutlets,
                    key = { it.id },
                    contentType = { "preset_outlet_item" }
                ) { outlet ->
                    val count = outlet.routinePreset.count { it.quantity > 0 }
                    val total = outlet.routinePreset.sumOf { it.quantity }
                    val title = if (outlet.name.startsWith("Outlet", ignoreCase = true)) {
                        outlet.name
                    } else {
                        "Outlet ${outlet.name}"
                    }
                    OutlinedCard(
                        onClick = { onSelectOutlet(outlet.id) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.outlinedCardColors(containerColor = Color.White),
                        border = BorderStroke(1.dp, SBColor.Grey_100)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                Text(title, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = SBColor.Grey_700)
                                Text(
                                    text = if (count > 0) "$count bahan • $total unit" else "Belum diatur",
                                    fontSize = 12.sp,
                                    color = if (count > 0) SBColor.Grey_500 else SBColor.Warning_600
                                )
                            }
                            Text(
                                text = "Atur",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = SBColor.Primary_550
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyDistributionCard(
    tab: WarehouseDistributionTab,
    hasActiveFilter: Boolean,
    onResetFilters: () -> Unit,
    onCreateClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedCard(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.outlinedCardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, SBColor.Grey_100)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Surface(
                modifier = Modifier.size(48.dp),
                shape = RoundedCornerShape(14.dp),
                color = SBColor.Grey_100
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        painter = painterResource(R.drawable.ic_truck),
                        contentDescription = null,
                        tint = SBColor.Grey_500,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
            Text(
                text = when {
                    hasActiveFilter -> "Tidak ada distribusi yang cocok"
                    tab == WarehouseDistributionTab.ROUTINE -> "Belum ada distribusi rutin hari ini"
                    tab == WarehouseDistributionTab.ADDITIONAL -> "Belum ada distribusi tambahan hari ini"
                    else -> "Belum ada riwayat distribusi"
                },
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = SBColor.Grey_700
            )
            Text(
                text = if (hasActiveFilter) {
                    "Coba ubah kata kunci pencarian, status, atau tanggal filter."
                } else {
                    "Buat pengiriman baru ke outlet untuk mulai mencatat distribusi."
                },
                fontSize = 13.sp,
                color = SBColor.Grey_500
            )
            if (hasActiveFilter) {
                OutlinedButton(
                    onClick = onResetFilters,
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Reset filter", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = SBColor.Primary_550)
                }
            } else if (tab != WarehouseDistributionTab.HISTORY) {
                Button(
                    onClick = onCreateClick,
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = SBColor.Primary_550)
                ) {
                    Text("Buat distribusi baru", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun CreateDistributionScreen(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    uiState: WarehouseUiState = WarehouseUiState(),
    effects: Flow<WarehouseUiEffect> = emptyFlow(),
    onEvent: (WarehouseUiEvent) -> Unit = {}
) {
    val form = uiState.createForm
    val focusManager = LocalFocusManager.current
    val listState = rememberLazyListState()
    var showOutletPicker by rememberSaveable { mutableStateOf(false) }
    val selectedOutlet = uiState.activeOutlets.firstOrNull { it.id == form.selectedOutletId }
    val selectedItemCount = remember(form.itemQuantities) {
        form.itemQuantities.values.count { (it.toIntOrNull() ?: 0) > 0 }
    }
    val ctaLabel = if (selectedItemCount > 0) {
        "Kirim ${form.distributionType.label.lowercase()} ($selectedItemCount bahan)"
    } else {
        "Kirim distribusi sekarang"
    }

    BackHandler(enabled = form.isSaving) {
        // Prevent accidental back navigation while network save is in progress
    }

    LaunchedEffect(effects) {
        effects.collect { effect ->
            if (effect is WarehouseUiEffect.DistributionSaved) {
                navController.popBackStackSafe()
            }
        }
    }

    Scaffold(
        modifier = modifier,
        containerColor = Color.White,
        bottomBar = {
            SBBottomCtaBar(
                label = ctaLabel,
                isLoading = form.isSaving,
                onClick = { onEvent(WarehouseUiEvent.SubmitCreateDistribution) }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
                .padding(innerPadding)
                .imePadding()
                .pointerInput(Unit) { detectTapGestures(onTap = { focusManager.clearFocus() }) }
        ) {
            SBSubHeader(
                eyebrow = "Admin Gudang",
                title = "Buat distribusi ${form.distributionType.label.lowercase()}",
                onBackClick = { if (!form.isSaving) navController.popBackStackSafe() },
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 14.dp)
            )

            HorizontalDivider(thickness = 1.dp, color = SBColor.Grey_100)

            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                item(key = "create_outlet", contentType = "outlet") {
                    SBSelectField(
                        label = "Outlet tujuan",
                        valueText = selectedOutlet?.let {
                            if (it.name.startsWith("Outlet", ignoreCase = true)) it.name else "Outlet ${it.name}"
                        } ?: "Pilih outlet tujuan",
                        isPlaceholder = selectedOutlet == null,
                        onClick = { showOutletPicker = true }
                    )
                }

                if (form.distributionType == DistributionType.ROUTINE && selectedOutlet != null) {
                    item(key = "create_preset_banner", contentType = "preset_banner") {
                        val hasPreset = selectedOutlet.routinePreset.any { it.quantity > 0 }
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp),
                            color = if (hasPreset) SBColor.Success_100.copy(alpha = 0.7f) else SBColor.Warning_100
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = if (hasPreset) {
                                        "Terisi otomatis dari preset (dapat disesuaikan)"
                                    } else {
                                        "Preset rutin outlet ini belum diatur"
                                    },
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = if (hasPreset) SBColor.Success_600 else SBColor.Warning_600,
                                    modifier = Modifier.weight(1f)
                                )
                                if (hasPreset) {
                                    Text(
                                        text = "Reset",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = SBColor.Primary_550,
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .clickable { onEvent(WarehouseUiEvent.ApplyOutletPresetToForm) }
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                item(key = "create_materials_header", contentType = "section_header") {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Jumlah bahan mentah",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = SBColor.Grey_700
                        )
                        if (selectedItemCount > 0) {
                            Text(
                                text = "$selectedItemCount bahan dipilih",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = SBColor.Primary_550
                            )
                        }
                    }
                }

                items(
                    items = uiState.activeMaterials,
                    key = { it.id },
                    contentType = { "dist_material_input" }
                ) { mat ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
                            Text(mat.name, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = SBColor.Grey_700)
                            Text("Satuan: ${mat.unit.firestoreValue}", fontSize = 12.sp, color = SBColor.Grey_500)
                        }
                        OutlinedTextField(
                            value = form.itemQuantities[mat.id].orEmpty(),
                            onValueChange = { onEvent(WarehouseUiEvent.UpdateItemQuantity(mat.id, it)) },
                            modifier = Modifier.width(108.dp),
                            placeholder = { Text("0", fontSize = 13.sp) },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            shape = RoundedCornerShape(10.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = SBColor.Primary_550,
                                unfocusedBorderColor = SBColor.Grey_200
                            )
                        )
                    }
                }

                item(key = "create_note", contentType = "note") {
                    OutlinedTextField(
                        value = form.note,
                        onValueChange = { onEvent(WarehouseUiEvent.UpdateNote(it)) },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Catatan pengiriman (opsional)") },
                        placeholder = {
                            Text(
                                if (form.distributionType == DistributionType.ROUTINE) {
                                    "Distribusi rutin pagi hari"
                                } else {
                                    "Tambahan stok siang hari"
                                }
                            )
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = SBColor.Primary_550,
                            unfocusedBorderColor = SBColor.Grey_200
                        )
                    )
                }

                form.errorMessage?.let { err ->
                    item(key = "create_error", contentType = "error") {
                        Text(text = err, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = SBColor.Primary_550)
                    }
                }
            }
        }
    }

    if (showOutletPicker) {
        OutletFilterBottomSheet(
            title = "Pilih outlet tujuan",
            subtitle = "Cari nama atau alamat outlet tujuan pengiriman.",
            outlets = uiState.activeOutlets,
            selectedOutletId = form.selectedOutletId,
            showAllOption = false,
            onDismiss = { showOutletPicker = false },
            onSelectOutlet = { chosenId ->
                if (!chosenId.isNullOrBlank()) {
                    onEvent(WarehouseUiEvent.SelectOutlet(chosenId))
                }
                showOutletPicker = false
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun DistributionDetailBottomSheet(
    card: DistributionCardUi,
    isActionLoading: Boolean,
    onDismiss: () -> Unit,
    onApprove: () -> Unit,
    onReject: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = Color.White,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .navigationBarsPadding()
                .padding(horizontal = 24.dp)
                .padding(bottom = 28.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        text = card.outletName,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = SBColor.Grey_700
                    )
                    Text(
                        text = "Distribusi ${card.type.label} • ${card.timeLabel}",
                        fontSize = 12.sp,
                        color = SBColor.Grey_500
                    )
                }
                DistributionStatusBadge(status = card.status)
            }

            OutlinedCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.outlinedCardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, SBColor.Grey_100)
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    SBKeyValueRow(label = "Dibuat oleh", value = card.createdByName)
                    card.sentTimeLabel?.let { SBKeyValueRow(label = "Waktu dikirim", value = it) }
                    card.confirmedTimeLabel?.let { SBKeyValueRow(label = "Waktu dikonfirmasi", value = it) }
                    card.confirmedByName?.let { SBKeyValueRow(label = "Dikonfirmasi oleh", value = it) }
                }
            }

            Text(
                text = "Rincian bahan (${card.items.size} item • ${card.totalQuantity} unit)",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = SBColor.Grey_700
            )

            OutlinedCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.outlinedCardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, SBColor.Grey_100)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    if (card.items.isEmpty()) {
                        Text("Belum ada rincian item.", fontSize = 13.sp, color = SBColor.Grey_500)
                    } else {
                        card.items.forEachIndexed { idx, item ->
                            val diff = item.quantityReceived?.let { item.quantitySent - it }
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 6.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
                                    Text(
                                        text = item.materialName,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = SBColor.Grey_700
                                    )
                                    if (!item.note.isNullOrBlank()) {
                                        Text(item.note, fontSize = 11.sp, color = SBColor.Grey_500)
                                    }
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = if (item.quantityReceived != null) {
                                            "${item.quantityReceived}/${item.quantitySent} ${item.unitLabel}"
                                        } else {
                                            "${item.quantitySent} ${item.unitLabel}"
                                        },
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (diff != null && diff > 0) SBColor.Primary_550 else SBColor.Grey_700
                                    )
                                    if (diff != null && diff != 0) {
                                        Text(
                                            text = if (diff > 0) "Kurang $diff ${item.unitLabel}" else "Lebih ${-diff} ${item.unitLabel}",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = SBColor.Warning_600
                                        )
                                    }
                                }
                            }
                            if (idx < card.items.lastIndex) {
                                HorizontalDivider(thickness = 1.dp, color = SBColor.Grey_100)
                            }
                        }
                    }
                }
            }

            if (!card.note.isNullOrBlank()) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    color = SBColor.Grey_100
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Text("Catatan pengiriman", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = SBColor.Grey_500)
                        Text(card.note, fontSize = 13.sp, color = SBColor.Grey_700)
                    }
                }
            }

            if (!card.confirmationNote.isNullOrBlank()) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    color = if (card.status == DistributionStatus.CANCELLED || card.status == DistributionStatus.NOT_RECEIVED) {
                        SBColor.Error_100
                    } else {
                        SBColor.Secondary_100
                    }
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Text(
                            text = if (card.status == DistributionStatus.CANCELLED) "Alasan pembatalan" else "Catatan konfirmasi",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = SBColor.Grey_600
                        )
                        Text(card.confirmationNote, fontSize = 13.sp, color = SBColor.Grey_700)
                    }
                }
            }

            if (card.status == DistributionStatus.DRAFT) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onReject,
                        enabled = !isActionLoading,
                        modifier = Modifier
                            .weight(1f)
                            .height(46.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Tolak", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = SBColor.Primary_550)
                    }
                    Button(
                        onClick = onApprove,
                        enabled = !isActionLoading,
                        modifier = Modifier
                            .weight(1f)
                            .height(46.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = SBColor.Primary_550)
                    ) {
                        Text("Setujui & Kirim", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
internal fun RejectDistributionDialog(
    outletName: String,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var reason by rememberSaveable(outletName) { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            color = Color.White
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(22.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Batalkan permintaan stok?",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = SBColor.Grey_700
                )
                Text(
                    text = "Permintaan dari $outletName akan ditandai batal.",
                    fontSize = 13.sp,
                    color = SBColor.Grey_500
                )
                OutlinedTextField(
                    value = reason,
                    onValueChange = { reason = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Alasan (opsional)") },
                    placeholder = { Text("Stok bahan sedang habis di gudang") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = SBColor.Primary_550,
                        unfocusedBorderColor = SBColor.Grey_200
                    )
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = onDismiss,
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = SBColor.Grey_100,
                            contentColor = SBColor.Grey_700
                        )
                    ) {
                        Text("Kembali", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                    Button(
                        onClick = { onConfirm(reason.ifBlank { "Ditolak oleh Admin Gudang" }) },
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = SBColor.Primary_550,
                            contentColor = Color.White
                        )
                    ) {
                        Text("Tolak", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun DistributionCardRow(
    card: DistributionCardUi,
    isActionLoading: Boolean,
    onApprove: () -> Unit,
    onReject: () -> Unit,
    modifier: Modifier = Modifier,
    showTypeBadge: Boolean = false,
    showDateInTime: Boolean = false,
    onClick: () -> Unit = {}
) {
    val (iconBg, iconTint, borderColor) = when (card.status) {
        DistributionStatus.DRAFT -> Triple(SBColor.Warning_100, SBColor.Warning_600, SBColor.Secondary_200)
        DistributionStatus.SENT -> Triple(SBColor.Primary_100, SBColor.Primary_550, SBColor.Grey_100)
        DistributionStatus.RECEIVED -> Triple(SBColor.Success_100, SBColor.Success_600, SBColor.Grey_100)
        DistributionStatus.PARTIALLY_RECEIVED -> Triple(SBColor.Warning_100, SBColor.Warning_600, SBColor.Secondary_200)
        DistributionStatus.NOT_RECEIVED -> Triple(SBColor.Error_100, SBColor.Primary_550, SBColor.Primary_200)
        DistributionStatus.CANCELLED -> Triple(SBColor.Grey_100, SBColor.Grey_500, SBColor.Grey_100)
    }

    OutlinedCard(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.outlinedCardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, borderColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Surface(
                    modifier = Modifier.size(40.dp),
                    shape = RoundedCornerShape(10.dp),
                    color = iconBg
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            painter = painterResource(R.drawable.ic_truck),
                            contentDescription = null,
                            modifier = Modifier.size(20.dp),
                            tint = iconTint
                        )
                    }
                }
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = card.outletName,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = SBColor.Grey_700,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f).padding(end = 8.dp)
                        )
                        DistributionStatusBadge(status = card.status)
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        if (showTypeBadge) {
                            Surface(
                                shape = RoundedCornerShape(5.dp),
                                color = if (card.type == DistributionType.ROUTINE) SBColor.Secondary_100 else SBColor.Grey_100
                            ) {
                                Text(
                                    text = card.type.label,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (card.type == DistributionType.ROUTINE) SBColor.Secondary_500 else SBColor.Grey_600,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                        val timeText = if (showDateInTime) card.timeLabel else card.todayTimeLabel
                        Text(
                            text = "$timeText • ${card.createdByName}",
                            fontSize = 12.sp,
                            color = SBColor.Grey_500,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            HorizontalDivider(thickness = 1.dp, color = SBColor.Grey_100)

            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                val previewItems = card.items.take(3)
                if (previewItems.isEmpty()) {
                    Text(card.itemsSummary, fontSize = 12.sp, color = SBColor.Grey_600)
                } else {
                    previewItems.forEach { item ->
                        val hasDiscrepancy = item.quantityReceived != null && item.quantityReceived != item.quantitySent
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = item.materialName,
                                fontSize = 12.sp,
                                color = SBColor.Grey_600,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f).padding(end = 8.dp)
                            )
                            Text(
                                text = if (item.quantityReceived != null) {
                                    "${item.quantityReceived}/${item.quantitySent} ${item.unitLabel}"
                                } else {
                                    "${item.quantitySent} ${item.unitLabel}"
                                },
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (hasDiscrepancy) SBColor.Primary_550 else SBColor.Grey_700
                            )
                        }
                    }
                    if (card.items.size > 3) {
                        Text(
                            text = "+${card.items.size - 3} bahan lainnya • Total ${card.totalQuantity} unit",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = SBColor.Grey_500
                        )
                    } else {
                        Text(
                            text = "Total ${card.totalQuantity} unit",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = SBColor.Grey_500
                        )
                    }
                }
            }

            val displayNote = card.confirmationNote?.takeIf { it.isNotBlank() } ?: card.note?.takeIf { it.isNotBlank() }
            if (displayNote != null) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    color = SBColor.Grey_100.copy(alpha = 0.7f)
                ) {
                    Text(
                        text = displayNote,
                        fontSize = 12.sp,
                        color = SBColor.Grey_600,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp)
                    )
                }
            }

            if (card.status == DistributionStatus.DRAFT) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onReject,
                        enabled = !isActionLoading,
                        modifier = Modifier
                            .weight(1f)
                            .height(40.dp),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Text("Tolak", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = SBColor.Primary_550)
                    }
                    Button(
                        onClick = onApprove,
                        enabled = !isActionLoading,
                        modifier = Modifier
                            .weight(1f)
                            .height(40.dp),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = SBColor.Primary_550),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Text("Setujui & Kirim", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }
        }
    }
}

@Composable
fun DistributionStatusBadge(
    status: DistributionStatus,
    modifier: Modifier = Modifier
) {
    val (label, containerColor, contentColor) = when (status) {
        DistributionStatus.DRAFT -> Triple("Pending", SBColor.Warning_100, SBColor.Warning_600)
        DistributionStatus.SENT -> Triple("Dikirim", SBColor.Primary_100, SBColor.Primary_550)
        DistributionStatus.RECEIVED -> Triple("Diterima", SBColor.Success_100, SBColor.Success_600)
        DistributionStatus.PARTIALLY_RECEIVED -> Triple("Sebagian", SBColor.Warning_100, SBColor.Warning_600)
        DistributionStatus.NOT_RECEIVED -> Triple("Tidak diterima", SBColor.Error_100, SBColor.Primary_550)
        DistributionStatus.CANCELLED -> Triple("Batal", SBColor.Grey_100, SBColor.Grey_600)
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

private fun previewWarehouseUiState(): WarehouseUiState = WarehouseUiState(
    activeOutlets = listOf(
        Outlet(id = "o1", name = "Sudirman", address = "Jl. Jenderal Sudirman"),
        Outlet(id = "o2", name = "Gajah Mada", address = "Jl. Gajah Mada"),
        Outlet(id = "o3", name = "Dr. Mansyur", address = "Jl. Dr. Mansyur, Medan")
    ),
    outletsPendingMorning = listOf(
        Outlet(id = "o3", name = "Dr. Mansyur", address = "Jl. Dr. Mansyur, Medan")
    ),
    outletsCompletedMorningCount = 2,
    todayPendingAdditionalCount = 1,
    distributionCards = listOf(
        DistributionCardUi(
            id = "d1",
            outletId = "o1",
            outletName = "Outlet Sudirman",
            createdByName = "Budi Gudang",
            confirmedByName = "Rina Staf",
            status = DistributionStatus.RECEIVED,
            type = DistributionType.ROUTINE,
            totalQuantity = 870,
            itemsSummary = "Tahu Original (550 pcs), Tahu Pedas (320 pcs)",
            items = listOf(
                DistributionItemDetailUi("i1", "m1", "Tahu Original", "pcs", 550, 550, null),
                DistributionItemDetailUi("i2", "m2", "Tahu Pedas", "pcs", 320, 320, null)
            ),
            timeLabel = "27 Nov, 06.30",
            todayTimeLabel = "06.30",
            sentTimeLabel = "27 Nov, 06.45",
            confirmedTimeLabel = "27 Nov, 07.30",
            createdAtMillis = 1_764_200_000_000L,
            note = "Distribusi rutin pagi dari Gudang Pusat",
            confirmationNote = "Stok diterima lengkap",
            isToday = true
        )
    )
)

@Preview(name = "Phone", device = Devices.PHONE, showBackground = true)
@Preview(name = "Tablet", device = Devices.TABLET, showBackground = true)
@Composable
private fun DistributionScreenPreview() {
    SudutBulatTheme {
        DistributionScreen(
            navController = rememberNavController(),
            uiState = previewWarehouseUiState()
        )
    }
}
