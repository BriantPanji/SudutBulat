package com.sudutcorp.sudutbulat.feature.stock

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedCard
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
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.sudutcorp.sudutbulat.R
import com.sudutcorp.sudutbulat.core.components.SBFilterChipRow
import com.sudutcorp.sudutbulat.core.components.SBLabeledTextField
import com.sudutcorp.sudutbulat.core.components.SBPageHeader
import com.sudutcorp.sudutbulat.core.components.SBSearchBar
import com.sudutcorp.sudutbulat.core.components.SBSegmentedTabs
import com.sudutcorp.sudutbulat.core.components.SBSortDropdownButton
import com.sudutcorp.sudutbulat.core.components.SBStatusBadge
import com.sudutcorp.sudutbulat.core.ui.theme.SBColor
import com.sudutcorp.sudutbulat.core.util.ThousandSeparatorVisualTransformation
import com.sudutcorp.sudutbulat.data.model.ProductSaleType
import com.sudutcorp.sudutbulat.data.model.UnitType
import com.sudutcorp.sudutbulat.feature.owner.CatalogItemUi
import com.sudutcorp.sudutbulat.feature.owner.CatalogTabMode
import com.sudutcorp.sudutbulat.feature.owner.MaterialSortField
import com.sudutcorp.sudutbulat.feature.owner.OwnerUiEffect
import com.sudutcorp.sudutbulat.feature.owner.OwnerUiEvent
import com.sudutcorp.sudutbulat.feature.owner.OwnerUiState
import com.sudutcorp.sudutbulat.feature.owner.ProductSortField
import com.sudutcorp.sudutbulat.feature.owner.SortDirection
import com.sudutcorp.sudutbulat.feature.owner.sortMaterialCatalogItems
import com.sudutcorp.sudutbulat.feature.owner.sortProductCatalogItems
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GudangStockScreen(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    uiState: OwnerUiState = OwnerUiState(),
    effects: Flow<OwnerUiEffect> = emptyFlow(),
    onEvent: (OwnerUiEvent) -> Unit = {},
    roleEyebrow: String = "Admin Gudang",
    initials: String = "AR",
    isStandaloneScreen: Boolean = false,
    onBackClick: () -> Unit = {}
) {
    val focusManager = LocalFocusManager.current
    var showAddSheet by rememberSaveable { mutableStateOf(false) }
    val thousandTransformation = remember { ThousandSeparatorVisualTransformation() }

    LaunchedEffect(effects) {
        effects.collect { effect ->
            if (effect is OwnerUiEffect.CatalogSaved) {
                showAddSheet = false
            }
        }
    }

    val isProductTab = uiState.catalogTabMode == CatalogTabMode.PRODUCT
    val query = uiState.catalogSearchQuery.trim().lowercase()

    val displayedItems = remember(
        uiState.catalogItems,
        uiState.catalogTabMode,
        query,
        uiState.catalogCategoryFilter,
        uiState.productStatusFilter,
        uiState.materialSortField,
        uiState.materialSortDirection,
        uiState.productSortField,
        uiState.productSortDirection
    ) {
        if (isProductTab) {
            val filtered = uiState.catalogItems.filter { item ->
                item.isProduct &&
                    (uiState.productStatusFilter == "Semua" ||
                        item.statusBadge.equals(uiState.productStatusFilter, ignoreCase = true)) &&
                    (query.isBlank() || item.name.lowercase().contains(query))
            }
            sortProductCatalogItems(filtered, uiState.productSortField, uiState.productSortDirection)
        } else {
            val filtered = uiState.catalogItems.filter { item ->
                !item.isProduct &&
                    (uiState.catalogCategoryFilter == "Semua" ||
                        item.category.equals(uiState.catalogCategoryFilter, ignoreCase = true)) &&
                    (query.isBlank() ||
                        item.name.lowercase().contains(query) ||
                        item.category.lowercase().contains(query))
            }
            sortMaterialCatalogItems(filtered, uiState.materialSortField, uiState.materialSortDirection)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White)
            .then(
                if (isStandaloneScreen) Modifier.statusBarsPadding().navigationBarsPadding() else Modifier
            )
            .pointerInput(Unit) { detectTapGestures(onTap = { focusManager.clearFocus() }) }
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
            .padding(top = 16.dp, bottom = 80.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        SBPageHeader(
            eyebrow = roleEyebrow,
            title = "Daftar barang",
            initials = initials,
            onBackClick = if (isStandaloneScreen) onBackClick else null
        )

        SBSegmentedTabs(
            options = CatalogTabMode.entries,
            selected = uiState.catalogTabMode,
            labelOf = { it.label },
            onSelected = { onEvent(OwnerUiEvent.CatalogTabSelected(it)) }
        )

        SBSearchBar(
            query = uiState.catalogSearchQuery,
            onQueryChange = { onEvent(OwnerUiEvent.CatalogSearchChanged(it)) },
            placeholder = if (isProductTab) "Cari produk jual..." else "Cari material atau kategori..."
        )

        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            val chipOptions = if (isProductTab) {
                listOf("Semua" to "Semua", "Dijual" to "Dijual", "Tidak dijual" to "Tidak dijual")
            } else {
                listOf("Semua" to "Semua", "Tahu" to "Tahu", "Saus" to "Saus", "Bumbu" to "Bumbu")
            }
            val selectedChip = if (isProductTab) uiState.productStatusFilter else uiState.catalogCategoryFilter

            SBFilterChipRow(
                options = chipOptions,
                selectedKey = selectedChip,
                onSelected = { option ->
                    if (isProductTab) {
                        onEvent(OwnerUiEvent.ProductStatusFilterSelected(option))
                    } else {
                        onEvent(OwnerUiEvent.CatalogFilterSelected(option))
                    }
                }
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isProductTab) "${displayedItems.size} produk" else "${displayedItems.size} material",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = SBColor.Grey_700
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    if (isProductTab) {
                        SBSortDropdownButton(
                            fields = ProductSortField.entries,
                            selectedField = uiState.productSortField,
                            isDescending = uiState.productSortDirection == SortDirection.DESC,
                            labelOf = { it.label },
                            isAlphaField = { it == ProductSortField.NAME },
                            defaultDescendingFor = { it == ProductSortField.PRICE },
                            onSelectSort = { field, isDesc ->
                                onEvent(
                                    OwnerUiEvent.ProductSortChanged(
                                        field,
                                        if (isDesc) SortDirection.DESC else SortDirection.ASC
                                    )
                                )
                            }
                        )
                    } else {
                        SBSortDropdownButton(
                            fields = MaterialSortField.entries,
                            selectedField = uiState.materialSortField,
                            isDescending = uiState.materialSortDirection == SortDirection.DESC,
                            labelOf = { it.label },
                            isAlphaField = { it == MaterialSortField.NAME || it == MaterialSortField.UNIT },
                            defaultDescendingFor = { it == MaterialSortField.COST },
                            onSelectSort = { field, isDesc ->
                                onEvent(
                                    OwnerUiEvent.MaterialSortChanged(
                                        field,
                                        if (isDesc) SortDirection.DESC else SortDirection.ASC
                                    )
                                )
                            }
                        )
                    }

                    TextButton(
                        onClick = {
                            onEvent(OwnerUiEvent.ResetCatalogForm(defaultIsProduct = isProductTab))
                            showAddSheet = true
                        },
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

        // Catalog List (tanpa progress bar, menu elipsis untuk Edit / Hentikan jual / Hentikan pakai)
        OutlinedCard(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.outlinedCardColors(containerColor = Color.White),
            border = BorderStroke(1.dp, SBColor.Grey_100)
        ) {
            if (displayedItems.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (isProductTab) "Belum ada produk jual yang cocok." else "Belum ada material yang cocok.",
                        fontSize = 14.sp,
                        color = SBColor.Grey_500
                    )
                }
            } else {
                Column {
                    displayedItems.forEachIndexed { index, item ->
                        CatalogInventoryRow(
                            item = item,
                            onEditClick = {
                                onEvent(OwnerUiEvent.StartEditCatalogItem(item))
                                showAddSheet = true
                            },
                            onToggleActiveClick = {
                                onEvent(OwnerUiEvent.ToggleCatalogItemActive(item))
                            }
                        )
                        if (index < displayedItems.lastIndex) {
                            HorizontalDivider(thickness = 1.dp, color = SBColor.Grey_100)
                        }
                    }
                }
            }
        }
    }

    if (showAddSheet) {
        val form = uiState.addCatalogForm
        val isEditing = !form.editingItemId.isNullOrBlank()
        ModalBottomSheet(
            onDismissRequest = { if (!form.isSaving) showAddSheet = false },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            containerColor = Color.White,
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .imePadding()
                    .verticalScroll(rememberScrollState())
                    .navigationBarsPadding()
                    .padding(horizontal = 24.dp)
                    .padding(bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = if (isEditing) "Edit barang katalog" else "Tambah barang katalog",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = SBColor.Grey_700
                )

                if (!isEditing) {
                    SBSegmentedTabs(
                        options = listOf(false, true),
                        selected = form.isProduct,
                        labelOf = { if (it) "Produk Jual" else "Material" },
                        onSelected = { onEvent(OwnerUiEvent.CatalogTypeChanged(it)) }
                    )
                }

                SBLabeledTextField(
                    label = if (form.isProduct) "Nama produk" else "Nama material",
                    value = form.name,
                    placeholder = if (form.isProduct) "Tahu Bulat Keju (Bungkus 5 pcs)" else "Tahu Bulat Original",
                    leadingIconRes = R.drawable.ic_box,
                    imeAction = ImeAction.Next,
                    onImeAction = { focusManager.moveFocus(FocusDirection.Next) },
                    onValueChange = { onEvent(OwnerUiEvent.CatalogNameChanged(it)) }
                )

                val isPackProduct = form.isProduct && form.saleType == ProductSaleType.BUNGKUS
                SBLabeledTextField(
                    label = when {
                        isPackProduct -> "Harga jual per bungkus"
                        form.isProduct -> "Harga jual per satuan"
                        else -> "Harga modal per satuan"
                    },
                    value = form.priceOrCostInput,
                    placeholder = if (form.isProduct) "5.000" else "500",
                    leadingIconRes = R.drawable.ic_wallet,
                    prefixText = "Rp",
                    keyboardType = KeyboardType.Number,
                    imeAction = if (isPackProduct) ImeAction.Next else ImeAction.Done,
                    visualTransformation = thousandTransformation,
                    onImeAction = {
                        if (isPackProduct) focusManager.moveFocus(FocusDirection.Next) else focusManager.clearFocus()
                    },
                    onValueChange = { onEvent(OwnerUiEvent.CatalogPriceChanged(it)) }
                )

                if (form.isProduct) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Tipe penjualan", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = SBColor.Grey_700)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            ProductSaleType.entries.forEach { saleType ->
                                val isSelected = form.saleType == saleType
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { onEvent(OwnerUiEvent.CatalogSaleTypeChanged(saleType)) },
                                    label = { Text(saleType.label, fontSize = 13.sp, fontWeight = FontWeight.SemiBold) },
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
                    }

                    if (isPackProduct) {
                        SBLabeledTextField(
                            label = "Isi per bungkus (pcs)",
                            value = form.pcsPerPackInput,
                            placeholder = "5",
                            leadingIconRes = R.drawable.ic_box,
                            keyboardType = KeyboardType.Number,
                            imeAction = ImeAction.Done,
                            onImeAction = { focusManager.clearFocus() },
                            onValueChange = { onEvent(OwnerUiEvent.CatalogPcsPerPackChanged(it)) }
                        )
                    }
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Satuan material", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = SBColor.Grey_700)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf(
                                UnitType.PCS to "pcs",
                                UnitType.UNIT to "unit",
                                UnitType.GR to "gr",
                                UnitType.ML to "ml"
                            ).forEach { (unitType, unitLabel) ->
                                val isSelected = form.unit == unitType
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { onEvent(OwnerUiEvent.CatalogUnitChanged(unitType)) },
                                    label = { Text(unitLabel, fontSize = 13.sp, fontWeight = FontWeight.SemiBold) },
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
                    }
                }

                form.errorMessage?.let { err ->
                    Text(text = err, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = SBColor.Primary_550)
                }

                Button(
                    onClick = { onEvent(OwnerUiEvent.SubmitCatalogItem) },
                    enabled = !form.isSaving,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = SBColor.Primary_550)
                ) {
                    if (form.isSaving) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp,
                            color = Color.White
                        )
                    } else {
                        Text(
                            text = if (isEditing) "Simpan perubahan" else "Simpan barang",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CatalogInventoryRow(
    item: CatalogItemUi,
    onEditClick: () -> Unit,
    onToggleActiveClick: () -> Unit
) {
    var menuExpanded by remember { mutableStateOf(false) }
    val tileBg = if (item.category == "Tahu") SBColor.Secondary_100 else SBColor.Primary_100

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 6.dp, top = 12.dp, bottom = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Surface(
            modifier = Modifier.size(40.dp),
            shape = RoundedCornerShape(12.dp),
            color = tileBg
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    painter = painterResource(if (item.isProduct) R.drawable.ic_store else R.drawable.ic_box),
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
            Text(text = item.name, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = SBColor.Grey_700)
            Text(text = item.subtitleText, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = SBColor.Grey_600)
        }

        SBStatusBadge(
            text = item.statusBadge,
            containerColor = if (item.isWarning) SBColor.Grey_100 else SBColor.Success_100,
            contentColor = if (item.isWarning) SBColor.Grey_600 else SBColor.Success_600
        )

        Box {
            IconButton(
                onClick = { menuExpanded = true },
                modifier = Modifier.size(34.dp)
            ) {
                Text(text = "⋮", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = SBColor.Grey_500)
            }

            DropdownMenu(
                expanded = menuExpanded,
                onDismissRequest = { menuExpanded = false },
                modifier = Modifier.background(Color.White),
                shape = RoundedCornerShape(12.dp),
                containerColor = Color.White
            ) {
                DropdownMenuItem(
                    text = {
                        Text(
                            text = if (item.isProduct) "Edit produk" else "Edit material",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = SBColor.Grey_700
                        )
                    },
                    onClick = {
                        menuExpanded = false
                        onEditClick()
                    }
                )
                DropdownMenuItem(
                    text = {
                        val toggleLabel = when {
                            item.isProduct && item.isActive -> "Hentikan jual"
                            item.isProduct -> "Aktifkan jual"
                            item.isActive -> "Hentikan pakai"
                            else -> "Aktifkan pakai"
                        }
                        Text(
                            text = toggleLabel,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (item.isActive) SBColor.Primary_550 else SBColor.Success_600
                        )
                    },
                    onClick = {
                        menuExpanded = false
                        onToggleActiveClick()
                    }
                )
            }
        }
    }
}
