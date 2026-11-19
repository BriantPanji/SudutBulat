package com.sudutcorp.sudutbulat.feature.stock

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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
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
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import com.sudutcorp.sudutbulat.core.ui.theme.SBColor
import com.sudutcorp.sudutbulat.core.util.ThousandSeparatorVisualTransformation
import com.sudutcorp.sudutbulat.data.model.UnitType
import com.sudutcorp.sudutbulat.feature.outlet.FormLabeledTextField
import com.sudutcorp.sudutbulat.feature.owner.CatalogItemUi
import com.sudutcorp.sudutbulat.feature.owner.CatalogTabMode
import com.sudutcorp.sudutbulat.feature.owner.MaterialSortField
import com.sudutcorp.sudutbulat.feature.owner.OwnerUiEvent
import com.sudutcorp.sudutbulat.feature.owner.OwnerUiState
import com.sudutcorp.sudutbulat.feature.owner.ProductSortField
import com.sudutcorp.sudutbulat.feature.owner.SortDirection
import com.sudutcorp.sudutbulat.feature.owner.sortMaterialCatalogItems
import com.sudutcorp.sudutbulat.feature.owner.sortProductCatalogItems

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GudangStockScreen(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    uiState: OwnerUiState = OwnerUiState(),
    onEvent: (OwnerUiEvent) -> Unit = {},
    roleEyebrow: String = "Admin Gudang",
    initials: String = "AR",
    isStandaloneScreen: Boolean = false,
    onBackClick: () -> Unit = {}
) {
    val focusManager = LocalFocusManager.current
    var showAddSheet by rememberSaveable { mutableStateOf(false) }
    var showSortMenu by rememberSaveable { mutableStateOf(false) }
    val thousandTransformation = remember { ThousandSeparatorVisualTransformation() }

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
                if (isStandaloneScreen) {
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
        // Page Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (isStandaloneScreen) {
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
                        text = roleEyebrow,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = SBColor.Grey_600
                    )
                    Text(
                        text = "Daftar barang",
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
                    modifier = Modifier.size(44.dp),
                    shape = CircleShape,
                    color = SBColor.Secondary_100
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = initials,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = SBColor.Primary_500
                        )
                    }
                }
            }
        }

        // 2-Tab Segmented Control: Material | Produk Jual
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(color = SBColor.Grey_100, shape = RoundedCornerShape(12.dp))
                .padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            CatalogTabMode.entries.forEach { mode ->
                val isSelected = uiState.catalogTabMode == mode
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(36.dp)
                        .clip(RoundedCornerShape(9.dp))
                        .background(if (isSelected) Color.White else Color.Transparent)
                        .clickable { onEvent(OwnerUiEvent.CatalogTabSelected(mode)) },
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

        // Search Bar
        OutlinedTextField(
            value = uiState.catalogSearchQuery,
            onValueChange = { onEvent(OwnerUiEvent.CatalogSearchChanged(it)) },
            modifier = Modifier.fillMaxWidth(),
            placeholder = {
                Text(
                    if (isProductTab) "Cari produk jual..." else "Cari material atau kategori...",
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

        // Filter Chips + Sort + Tambah
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            val chipOptions = if (isProductTab) {
                listOf("Semua", "Dijual", "Tidak dijual")
            } else {
                listOf("Semua", "Tahu", "Saus", "Bumbu")
            }
            val selectedChip = if (isProductTab) uiState.productStatusFilter else uiState.catalogCategoryFilter

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                chipOptions.forEach { option ->
                    val isSelected = option == selectedChip
                    FilterChip(
                        selected = isSelected,
                        onClick = {
                            if (isProductTab) {
                                onEvent(OwnerUiEvent.ProductStatusFilterSelected(option))
                            } else {
                                onEvent(OwnerUiEvent.CatalogFilterSelected(option))
                            }
                        },
                        label = {
                            Text(
                                text = option,
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
                    text = if (isProductTab) "${displayedItems.size} produk" else "${displayedItems.size} material",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = SBColor.Grey_700
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Box {
                        val activeSortLabel = if (isProductTab) {
                            val arrow = if (uiState.productSortDirection == SortDirection.DESC) "↓" else "↑"
                            "${uiState.productSortField.label} $arrow"
                        } else {
                            val arrow = if (uiState.materialSortDirection == SortDirection.DESC) "↓" else "↑"
                            "${uiState.materialSortField.label} $arrow"
                        }

                        TextButton(
                            onClick = { showSortMenu = true },
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)
                        ) {
                            Text(
                                text = activeSortLabel,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = SBColor.Grey_600
                            )
                        }

                        CompactCatalogSortDropdown(
                            expanded = showSortMenu,
                            isProductTab = isProductTab,
                            materialField = uiState.materialSortField,
                            materialDirection = uiState.materialSortDirection,
                            productField = uiState.productSortField,
                            productDirection = uiState.productSortDirection,
                            onDismiss = { showSortMenu = false },
                            onSelectMaterialSort = { f, d -> onEvent(OwnerUiEvent.MaterialSortChanged(f, d)) },
                            onSelectProductSort = { f, d -> onEvent(OwnerUiEvent.ProductSortChanged(f, d)) }
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

        // Catalog List (without progress bar, ellipsis action menu instead of chevron)
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
                        CatalogInventoryRow(item = item)
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
        ModalBottomSheet(
            onDismissRequest = { showAddSheet = false },
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
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = "Tambah barang katalog",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = SBColor.Grey_700
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(color = SBColor.Grey_100, shape = RoundedCornerShape(12.dp))
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    listOf(false to "Material", true to "Produk Jual").forEach { (isProd, label) ->
                        val isSelected = form.isProduct == isProd
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(36.dp)
                                .clip(RoundedCornerShape(9.dp))
                                .background(if (isSelected) Color.White else Color.Transparent)
                                .clickable { onEvent(OwnerUiEvent.CatalogTypeChanged(isProd)) },
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

                FormLabeledTextField(
                    label = if (form.isProduct) "Nama produk" else "Nama material",
                    value = form.name,
                    placeholder = if (form.isProduct) "Tahu Bulat Keju (Bungkus 5 pcs)" else "Tahu Bulat Original",
                    leadingIconRes = R.drawable.ic_box,
                    imeAction = ImeAction.Next,
                    onImeAction = { focusManager.moveFocus(FocusDirection.Next) },
                    onValueChange = { onEvent(OwnerUiEvent.CatalogNameChanged(it)) }
                )

                FormLabeledTextField(
                    label = if (form.isProduct) "Harga jual" else "Harga modal per satuan",
                    value = form.priceOrCostInput,
                    placeholder = if (form.isProduct) "5.000" else "500",
                    leadingIconRes = R.drawable.ic_wallet,
                    prefixText = "Rp",
                    keyboardType = KeyboardType.Number,
                    imeAction = ImeAction.Done,
                    visualTransformation = thousandTransformation,
                    onImeAction = { focusManager.clearFocus() },
                    onValueChange = { onEvent(OwnerUiEvent.CatalogPriceChanged(it)) }
                )

                if (!form.isProduct) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "Satuan material",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = SBColor.Grey_700
                        )
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
                    Text(
                        text = err,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = SBColor.Primary_550
                    )
                }

                Button(
                    onClick = {
                        onEvent(OwnerUiEvent.SubmitCatalogItem)
                        if (form.name.isNotBlank()) {
                            showAddSheet = false
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = SBColor.Primary_550)
                ) {
                    Text(
                        text = "Simpan barang",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun CompactCatalogSortDropdown(
    expanded: Boolean,
    isProductTab: Boolean,
    materialField: MaterialSortField,
    materialDirection: SortDirection,
    productField: ProductSortField,
    productDirection: SortDirection,
    onDismiss: () -> Unit,
    onSelectMaterialSort: (MaterialSortField, SortDirection) -> Unit,
    onSelectProductSort: (ProductSortField, SortDirection) -> Unit
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

        if (isProductTab) {
            ProductSortField.entries.forEach { field ->
                val isSelected = field == productField
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
                                Text("✓", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = SBColor.Primary_550)
                            }
                        }
                    },
                    onClick = {
                        val nextDir = if (isSelected) {
                            if (productDirection == SortDirection.DESC) SortDirection.ASC else SortDirection.DESC
                        } else if (field == ProductSortField.PRICE) {
                            SortDirection.DESC
                        } else {
                            SortDirection.ASC
                        }
                        onSelectProductSort(field, nextDir)
                    }
                )
            }
        } else {
            MaterialSortField.entries.forEach { field ->
                val isSelected = field == materialField
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
                                Text("✓", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = SBColor.Primary_550)
                            }
                        }
                    },
                    onClick = {
                        val nextDir = if (isSelected) {
                            if (materialDirection == SortDirection.DESC) SortDirection.ASC else SortDirection.DESC
                        } else if (field == MaterialSortField.COST) {
                            SortDirection.DESC
                        } else {
                            SortDirection.ASC
                        }
                        onSelectMaterialSort(field, nextDir)
                    }
                )
            }
        }

        HorizontalDivider(
            thickness = 1.dp,
            color = SBColor.Grey_100,
            modifier = Modifier.padding(vertical = 4.dp)
        )

        val currentDir = if (isProductTab) productDirection else materialDirection
        val isAlphaField = if (isProductTab) {
            productField == ProductSortField.NAME
        } else {
            materialField == MaterialSortField.NAME || materialField == MaterialSortField.UNIT
        }
        val ascLabel = if (isAlphaField) "A – Z ↑" else "Terendah ↑"
        val descLabel = if (isAlphaField) "Z – A ↓" else "Tertinggi ↓"

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            listOf(SortDirection.ASC to ascLabel, SortDirection.DESC to descLabel).forEach { (dir, label) ->
                val isDirSelected = currentDir == dir
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(34.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isDirSelected) SBColor.Primary_100 else SBColor.Grey_100)
                        .clickable {
                            if (isProductTab) {
                                onSelectProductSort(productField, dir)
                            } else {
                                onSelectMaterialSort(materialField, dir)
                            }
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
private fun CatalogInventoryRow(
    item: CatalogItemUi
) {
    var menuExpanded by remember { mutableStateOf(false) }
    val tileBg = if (item.category == "Tahu") SBColor.Secondary_100 else SBColor.Primary_100
    val badgeBg = if (item.isWarning) SBColor.Grey_100 else SBColor.Success_100
    val badgeTextColor = if (item.isWarning) SBColor.Grey_600 else SBColor.Success_600

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
                    painter = painterResource(
                        if (item.isProduct) R.drawable.ic_store else R.drawable.ic_box
                    ),
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
                text = item.name,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = SBColor.Grey_700
            )
            Text(
                text = item.subtitleText,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = SBColor.Grey_600
            )
        }

        Surface(
            shape = RoundedCornerShape(6.dp),
            color = badgeBg
        ) {
            Text(
                text = item.statusBadge,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = badgeTextColor,
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
            )
        }

        // Ellipsis action menu (UI-only: Edit & Hentikan jual / Hentikan pakai)
        Box {
            IconButton(
                onClick = { menuExpanded = true },
                modifier = Modifier.size(34.dp)
            ) {
                Text(
                    text = "⋮",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = SBColor.Grey_500
                )
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
                    onClick = { menuExpanded = false }
                )
                DropdownMenuItem(
                    text = {
                        Text(
                            text = if (item.isProduct) "Hentikan jual" else "Hentikan pakai",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = SBColor.Primary_550
                        )
                    },
                    onClick = { menuExpanded = false }
                )
            }
        }
    }
}
