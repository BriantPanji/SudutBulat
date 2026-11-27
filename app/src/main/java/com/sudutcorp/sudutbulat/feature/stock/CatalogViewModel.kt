package com.sudutcorp.sudutbulat.feature.stock

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sudutcorp.sudutbulat.data.model.Material
import com.sudutcorp.sudutbulat.data.model.MaterialCategory
import com.sudutcorp.sudutbulat.data.model.Product
import com.sudutcorp.sudutbulat.data.model.ProductSaleType
import com.sudutcorp.sudutbulat.data.model.UnitType
import com.sudutcorp.sudutbulat.data.repository.MaterialCategoryRepository
import com.sudutcorp.sudutbulat.data.repository.MaterialRepository
import com.sudutcorp.sudutbulat.data.repository.ProductRepository
import com.sudutcorp.sudutbulat.feature.owner.AddCatalogItemFormState
import com.sudutcorp.sudutbulat.feature.owner.CatalogItemUi
import com.sudutcorp.sudutbulat.feature.owner.CatalogTabMode
import com.sudutcorp.sudutbulat.feature.owner.MaterialSortField
import com.sudutcorp.sudutbulat.feature.owner.ProductSortField
import com.sudutcorp.sudutbulat.feature.owner.SortDirection
import com.sudutcorp.sudutbulat.feature.owner.buildCatalogItems
import com.sudutcorp.sudutbulat.feature.owner.defaultSeededMaterialCategories
import com.sudutcorp.sudutbulat.feature.owner.defaultSeededMaterials
import com.sudutcorp.sudutbulat.feature.owner.defaultSeededProducts
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Locale

data class CatalogUiState(
    val materials: List<Material> = emptyList(),
    val materialCategories: List<MaterialCategory> = defaultSeededMaterialCategories(),
    val products: List<Product> = emptyList(),
    val catalogItems: List<CatalogItemUi> = buildCatalogItems(
        defaultSeededMaterials(),
        defaultSeededProducts(),
        defaultSeededMaterialCategories()
    ),
    val catalogTabMode: CatalogTabMode = CatalogTabMode.MATERIAL,
    val catalogSearchQuery: String = "",
    val catalogCategoryFilter: String = "Semua",
    val productStatusFilter: String = "Semua",
    val materialSortField: MaterialSortField = MaterialSortField.NAME,
    val materialSortDirection: SortDirection = SortDirection.ASC,
    val productSortField: ProductSortField = ProductSortField.NAME,
    val productSortDirection: SortDirection = SortDirection.ASC,
    val addCatalogForm: AddCatalogItemFormState = AddCatalogItemFormState(),
    val backendError: String? = null
)

sealed interface CatalogUiEvent {
    data class TabSelected(val mode: CatalogTabMode) : CatalogUiEvent
    data class SearchChanged(val query: String) : CatalogUiEvent
    data class MaterialCategoryFilterSelected(val category: String) : CatalogUiEvent
    data class ProductStatusFilterSelected(val status: String) : CatalogUiEvent
    data class MaterialSortChanged(val field: MaterialSortField, val direction: SortDirection) : CatalogUiEvent
    data class ProductSortChanged(val field: ProductSortField, val direction: SortDirection) : CatalogUiEvent
    data class NameChanged(val name: String) : CatalogUiEvent
    data class CategoryInputChanged(val categoryInput: String) : CatalogUiEvent
    data class TypeChanged(val isProduct: Boolean) : CatalogUiEvent
    data class PriceChanged(val priceInput: String) : CatalogUiEvent
    data class UnitChanged(val unit: UnitType) : CatalogUiEvent
    data class SaleTypeChanged(val saleType: ProductSaleType) : CatalogUiEvent
    data class PcsPerPackChanged(val pcsInput: String) : CatalogUiEvent
    data class ResetForm(val defaultIsProduct: Boolean = false) : CatalogUiEvent
    data class StartEditItem(val item: CatalogItemUi) : CatalogUiEvent
    data class ToggleItemActive(val item: CatalogItemUi) : CatalogUiEvent
    data object SubmitItem : CatalogUiEvent
}

sealed interface CatalogUiEffect {
    data class ShowSnackbar(val message: String) : CatalogUiEffect
    data object CatalogSaved : CatalogUiEffect
}

class CatalogViewModel(
    private val materialRepository: MaterialRepository,
    private val materialCategoryRepository: MaterialCategoryRepository,
    private val productRepository: ProductRepository
) : ViewModel() {

    private val _state = MutableStateFlow(CatalogUiState())
    val state: StateFlow<CatalogUiState> = _state.asStateFlow()

    private val _effects = Channel<CatalogUiEffect>(Channel.BUFFERED)
    val effects: Flow<CatalogUiEffect> = _effects.receiveAsFlow().shareIn(viewModelScope, SharingStarted.Eagerly)

    init {
        observeCatalogStreams()
    }

    private fun reportError(source: String, error: Throwable) {
        Log.e("CatalogViewModel", "Firestore stream error ($source)", error)
        val detail = error.localizedMessage?.takeIf { it.isNotBlank() } ?: error.javaClass.simpleName
        val message = "Gagal memuat $source: $detail"
        _state.update { it.copy(backendError = message) }
        viewModelScope.launch { _effects.send(CatalogUiEffect.ShowSnackbar(message)) }
    }

    private fun observeCatalogStreams() {
        viewModelScope.launch {
            materialRepository.getMaterialsFlow()
                .catch { e -> reportError("materials", e) }
                .collect { list ->
                    _state.update { recompute(it.copy(materials = list)) }
                }
        }
        viewModelScope.launch {
            materialCategoryRepository.getCategoriesFlow()
                .catch { e -> reportError("material_categories", e) }
                .collect { list ->
                    _state.update { current ->
                        val activeCategories = list.ifEmpty { defaultSeededMaterialCategories() }
                        val validFilter = if (
                            current.catalogCategoryFilter == "Semua" ||
                            activeCategories.any { it.name.equals(current.catalogCategoryFilter, ignoreCase = true) }
                        ) {
                            current.catalogCategoryFilter
                        } else {
                            "Semua"
                        }
                        recompute(current.copy(materialCategories = activeCategories, catalogCategoryFilter = validFilter))
                    }
                }
        }
        viewModelScope.launch {
            productRepository.getProductsFlow(onlyActive = false)
                .catch { e -> reportError("products", e) }
                .collect { list ->
                    _state.update { recompute(it.copy(products = list)) }
                }
        }
    }

    private fun recompute(raw: CatalogUiState): CatalogUiState {
        val activeMaterials = raw.materials.ifEmpty { defaultSeededMaterials() }
        val activeProducts = raw.products.ifEmpty { defaultSeededProducts() }
        val activeCategories = raw.materialCategories.ifEmpty { defaultSeededMaterialCategories() }
        return raw.copy(
            materialCategories = activeCategories,
            catalogItems = buildCatalogItems(activeMaterials, activeProducts, activeCategories)
        )
    }

    fun onEvent(event: CatalogUiEvent) {
        when (event) {
            is CatalogUiEvent.TabSelected -> _state.update { it.copy(catalogTabMode = event.mode) }
            is CatalogUiEvent.SearchChanged -> _state.update { it.copy(catalogSearchQuery = event.query) }
            is CatalogUiEvent.MaterialCategoryFilterSelected -> _state.update { it.copy(catalogCategoryFilter = event.category) }
            is CatalogUiEvent.ProductStatusFilterSelected -> _state.update { it.copy(productStatusFilter = event.status) }
            is CatalogUiEvent.MaterialSortChanged -> _state.update {
                it.copy(materialSortField = event.field, materialSortDirection = event.direction)
            }
            is CatalogUiEvent.ProductSortChanged -> _state.update {
                it.copy(productSortField = event.field, productSortDirection = event.direction)
            }
            is CatalogUiEvent.NameChanged -> _state.update {
                it.copy(addCatalogForm = it.addCatalogForm.copy(name = event.name, errorMessage = null))
            }
            is CatalogUiEvent.CategoryInputChanged -> _state.update {
                it.copy(addCatalogForm = it.addCatalogForm.copy(categoryInput = event.categoryInput, errorMessage = null))
            }
            is CatalogUiEvent.TypeChanged -> _state.update {
                it.copy(addCatalogForm = it.addCatalogForm.copy(isProduct = event.isProduct, errorMessage = null))
            }
            is CatalogUiEvent.PriceChanged -> _state.update {
                it.copy(addCatalogForm = it.addCatalogForm.copy(priceOrCostInput = event.priceInput.filter(Char::isDigit), errorMessage = null))
            }
            is CatalogUiEvent.UnitChanged -> _state.update {
                it.copy(addCatalogForm = it.addCatalogForm.copy(unit = event.unit, errorMessage = null))
            }
            is CatalogUiEvent.SaleTypeChanged -> _state.update {
                it.copy(addCatalogForm = it.addCatalogForm.copy(saleType = event.saleType, errorMessage = null))
            }
            is CatalogUiEvent.PcsPerPackChanged -> _state.update {
                it.copy(
                    addCatalogForm = it.addCatalogForm.copy(
                        pcsPerPackInput = event.pcsInput.filter(Char::isDigit),
                        errorMessage = null
                    )
                )
            }
            is CatalogUiEvent.ResetForm -> _state.update {
                it.copy(addCatalogForm = AddCatalogItemFormState(isProduct = event.defaultIsProduct))
            }
            is CatalogUiEvent.StartEditItem -> _state.update {
                it.copy(
                    addCatalogForm = AddCatalogItemFormState(
                        editingItemId = event.item.id,
                        name = event.item.name,
                        isProduct = event.item.isProduct,
                        categoryInput = if (event.item.isProduct) "" else event.item.category,
                        priceOrCostInput = event.item.priceOrCost.toString(),
                        unit = event.item.unit,
                        saleType = event.item.saleType,
                        pcsPerPackInput = if (event.item.saleType == ProductSaleType.BUNGKUS) {
                            event.item.pcsPerPack.toString()
                        } else {
                            ""
                        },
                        isActive = event.item.isActive
                    )
                )
            }
            is CatalogUiEvent.ToggleItemActive -> viewModelScope.launch { toggleCatalogItemActive(event.item) }
            CatalogUiEvent.SubmitItem -> viewModelScope.launch { submitCatalogItem() }
        }
    }

    internal suspend fun submitCatalogItem() {
        val form = _state.value.addCatalogForm
        val cleanName = form.name.trim()
        if (cleanName.isBlank()) {
            _state.update { it.copy(addCatalogForm = form.copy(errorMessage = "Nama barang wajib diisi.")) }
            return
        }

        val resolvedPcsPerPack = if (form.isProduct && form.saleType == ProductSaleType.BUNGKUS) {
            val parsed = form.pcsPerPackInput.toIntOrNull()
            if (parsed == null || parsed < 2) {
                _state.update {
                    it.copy(addCatalogForm = form.copy(errorMessage = "Jumlah pcs per bungkus minimal 2."))
                }
                return
            }
            parsed
        } else {
            1
        }

        val cleanCategoryInput = form.categoryInput.trim().replace(Regex("\\s+"), " ")
        if (!form.isProduct && cleanCategoryInput.isBlank()) {
            _state.update { it.copy(addCatalogForm = form.copy(errorMessage = "Kategori material wajib diisi.")) }
            return
        }

        _state.update { it.copy(addCatalogForm = form.copy(isSaving = true, errorMessage = null)) }
        val priceOrCost = form.priceOrCostInput.toLongOrNull() ?: 0L
        val slug = cleanName.lowercase(Locale.ROOT).replace(Regex("[^a-z0-9]+"), "_").trim('_')
            .ifBlank { "${System.currentTimeMillis()}" }
        val isEdit = !form.editingItemId.isNullOrBlank()

        if (form.isProduct) {
            val product = Product(
                id = form.editingItemId ?: "prod_$slug",
                name = cleanName,
                price = priceOrCost,
                saleType = form.saleType,
                pcsPerPack = resolvedPcsPerPack,
                isActive = form.isActive
            )
            val result: Result<Any> = if (isEdit) {
                productRepository.updateProduct(product)
            } else {
                productRepository.saveProduct(product)
            }
            if (result.isFailure) {
                val msg = "Gagal menyimpan produk: ${result.exceptionOrNull()?.readableMessage()}"
                _state.update { it.copy(addCatalogForm = form.copy(isSaving = false, errorMessage = msg)) }
                _effects.send(CatalogUiEffect.ShowSnackbar(msg))
                return
            }
            _state.update { current ->
                val updated = current.products.filterNot { it.id == product.id } + product
                recompute(current.copy(products = updated, addCatalogForm = AddCatalogItemFormState(isProduct = true)))
            }
        } else {
            val categoryResult = materialCategoryRepository.findOrCreateByName(cleanCategoryInput)
            if (categoryResult.isFailure) {
                val msg = "Gagal menyimpan kategori: ${categoryResult.exceptionOrNull()?.readableMessage()}"
                _state.update { it.copy(addCatalogForm = form.copy(isSaving = false, errorMessage = msg)) }
                _effects.send(CatalogUiEffect.ShowSnackbar(msg))
                return
            }
            val resolvedCategory = categoryResult.getOrThrow()

            val material = Material(
                id = form.editingItemId ?: "mat_$slug",
                name = cleanName,
                cost = priceOrCost,
                unit = form.unit,
                categoryId = resolvedCategory.id,
                isActive = form.isActive
            )
            val result: Result<Any> = if (isEdit) {
                materialRepository.updateMaterial(material)
            } else {
                materialRepository.saveMaterial(material)
            }
            if (result.isFailure) {
                val msg = "Gagal menyimpan material: ${result.exceptionOrNull()?.readableMessage()}"
                _state.update { it.copy(addCatalogForm = form.copy(isSaving = false, errorMessage = msg)) }
                _effects.send(CatalogUiEffect.ShowSnackbar(msg))
                return
            }
            _state.update { current ->
                val updatedCategories = (current.materialCategories.filterNot { it.id == resolvedCategory.id } + resolvedCategory)
                    .sortedBy { it.nameLower }
                val updated = current.materials.filterNot { it.id == material.id } + material
                recompute(
                    current.copy(
                        materials = updated,
                        materialCategories = updatedCategories,
                        addCatalogForm = AddCatalogItemFormState(isProduct = false)
                    )
                )
            }
        }

        val actionWord = if (isEdit) "diperbarui" else "ditambahkan ke katalog"
        _effects.send(CatalogUiEffect.ShowSnackbar("Barang $cleanName berhasil $actionWord."))
        _effects.send(CatalogUiEffect.CatalogSaved)
    }

    internal suspend fun toggleCatalogItemActive(item: CatalogItemUi) {
        val nextActive = !item.isActive
        val result = if (item.isProduct) {
            productRepository.setProductActive(item.id, nextActive)
        } else {
            materialRepository.setMaterialActive(item.id, nextActive)
        }

        result.onSuccess {
            _state.update { current ->
                if (item.isProduct) {
                    val updated = current.products.map {
                        if (it.id == item.id) it.copy(isActive = nextActive) else it
                    }
                    recompute(current.copy(products = updated))
                } else {
                    val updated = current.materials.map {
                        if (it.id == item.id) it.copy(isActive = nextActive) else it
                    }
                    recompute(current.copy(materials = updated))
                }
            }
            val label = when {
                item.isProduct && nextActive -> "kembali dijual"
                item.isProduct -> "dihentikan penjualannya"
                nextActive -> "kembali dipakai"
                else -> "dihentikan pemakaiannya"
            }
            _effects.send(CatalogUiEffect.ShowSnackbar("${item.name} $label."))
        }.onFailure { e ->
            _effects.send(CatalogUiEffect.ShowSnackbar("Gagal mengubah status ${item.name}: ${e.readableMessage()}"))
        }
    }

    private fun Throwable.readableMessage(): String =
        localizedMessage?.takeIf { it.isNotBlank() } ?: javaClass.simpleName
}
