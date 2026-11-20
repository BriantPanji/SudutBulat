package com.sudutcorp.sudutbulat.feature.owner

import com.google.firebase.firestore.GeoPoint
import com.sudutcorp.sudutbulat.core.util.formatRawTimeToDisplay
import com.sudutcorp.sudutbulat.core.util.isValidRawTime
import com.sudutcorp.sudutbulat.data.auth.AuthRepository
import com.sudutcorp.sudutbulat.data.model.Material
import com.sudutcorp.sudutbulat.data.model.Outlet
import com.sudutcorp.sudutbulat.data.model.Product
import com.sudutcorp.sudutbulat.data.model.ProductSaleType
import com.sudutcorp.sudutbulat.data.model.Role
import com.sudutcorp.sudutbulat.data.model.User
import com.sudutcorp.sudutbulat.data.repository.MaterialRepository
import com.sudutcorp.sudutbulat.data.repository.OutletRepository
import com.sudutcorp.sudutbulat.data.repository.ProductRepository
import com.sudutcorp.sudutbulat.data.repository.UserRepository
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import java.util.Locale

private fun Throwable.readableMessage(): String =
    localizedMessage?.takeIf { it.isNotBlank() } ?: javaClass.simpleName

/**
 * Handles validation and verified Firestore/Auth write operations for Owner CRUD flows
 * (outlets, users/staff, and catalog items). Never fakes local success when backend writes fail.
 */
class OwnerActionsHandler(
    private val outletRepository: OutletRepository,
    private val userRepository: UserRepository,
    private val materialRepository: MaterialRepository,
    private val productRepository: ProductRepository,
    private val authRepository: AuthRepository,
    private val state: MutableStateFlow<OwnerUiState>,
    private val effects: Channel<OwnerUiEffect>
) {
    suspend fun toggleOutletOpenStatus(outletId: String) {
        val currentOutlet = state.value.outletItems.firstOrNull { it.id == outletId } ?: return
        val newOpen = !currentOutlet.isOpen

        outletRepository.setOutletOpen(outletId, newOpen)
            .onSuccess {
                state.update { current ->
                    val updated = current.outlets.map {
                        if (it.id == outletId) it.copy(isOpen = newOpen) else it
                    }
                    computeOwnerDerivedState(current.copy(outlets = updated))
                }
                val label = if (newOpen) "Buka" else "Tutup"
                effects.send(OwnerUiEffect.ShowSnackbar("Status ${currentOutlet.displayName} diubah ke $label."))
            }
            .onFailure { e ->
                effects.send(OwnerUiEffect.ShowSnackbar("Gagal mengubah status outlet: ${e.readableMessage()}"))
            }
    }

    suspend fun assignStaffToOutlet(outletId: String, selectedStaffIds: Set<String>) {
        val baseUsers = state.value.users
        val previouslyAssigned = baseUsers
            .filter { it.role == Role.OUTLET && it.outletId == outletId }
            .map { it.id }
            .toSet()

        val newlyAdded = selectedStaffIds - previouslyAssigned
        val removed = previouslyAssigned - selectedStaffIds

        userRepository.updateOutletStaffAssignments(
            outletId = outletId,
            addUserIds = newlyAdded.toList(),
            removeUserIds = removed.toList()
        ).onSuccess {
            state.update { current ->
                val updatedUsers = current.users.map { user ->
                    when (user.id) {
                        in newlyAdded -> user.copy(outletId = outletId)
                        in removed -> user.copy(outletId = null)
                        else -> user
                    }
                }
                computeOwnerDerivedState(current.copy(users = updatedUsers))
            }
            effects.send(OwnerUiEffect.ShowSnackbar("Penugasan staf berhasil disimpan."))
        }.onFailure { e ->
            effects.send(OwnerUiEffect.ShowSnackbar("Gagal menyimpan penugasan staf: ${e.readableMessage()}"))
        }
    }

    suspend fun reassignSingleStaffOutlet(userId: String, outletId: String?) {
        userRepository.setUserOutlet(userId, outletId)
            .onSuccess {
                state.update { current ->
                    val updated = current.users.map {
                        if (it.id == userId) it.copy(outletId = outletId) else it
                    }
                    computeOwnerDerivedState(current.copy(users = updated))
                }
                effects.send(OwnerUiEffect.ShowSnackbar("Outlet penugasan staf berhasil diperbarui."))
            }
            .onFailure { e ->
                effects.send(OwnerUiEffect.ShowSnackbar("Gagal memperbarui outlet staf: ${e.readableMessage()}"))
            }
    }

    suspend fun toggleUserActive(userId: String) {
        val target = state.value.users.firstOrNull { it.id == userId } ?: return
        val newActive = !target.isActive

        userRepository.setUserActive(userId, newActive)
            .onSuccess {
                state.update { current ->
                    val updated = current.users.map {
                        if (it.id == userId) it.copy(isActive = newActive) else it
                    }
                    computeOwnerDerivedState(current.copy(users = updated))
                }
                val statusText = if (newActive) "diaktifkan" else "dinonaktifkan"
                effects.send(OwnerUiEffect.ShowSnackbar("Akun ${target.name} berhasil $statusText."))
            }
            .onFailure { e ->
                effects.send(OwnerUiEffect.ShowSnackbar("Gagal mengubah status akun: ${e.readableMessage()}"))
            }
    }

    suspend fun submitNewOutlet() {
        val form = state.value.addOutletForm
        val cleanName = form.name.trim()
        val cleanAddress = form.address.trim()
        val error = when {
            cleanName.isBlank() -> "Nama outlet wajib diisi."
            cleanAddress.isBlank() -> "Alamat lengkap wajib diisi."
            !isValidRawTime(form.openTime) -> "Jam buka harus 4 digit valid (00.00–23.59)."
            !isValidRawTime(form.closeTime) -> "Jam tutup harus 4 digit valid (00.00–23.59)."
            else -> null
        }
        if (error != null) {
            state.update { it.copy(addOutletForm = form.copy(errorMessage = error)) }
            return
        }

        state.update { it.copy(addOutletForm = form.copy(isSaving = true, errorMessage = null)) }

        val slug = cleanName.removePrefix("Outlet ").removePrefix("outlet ")
            .lowercase(Locale.ROOT).replace(Regex("[^a-z0-9]+"), "_").trim('_')
            .ifBlank { "baru_${System.currentTimeMillis()}" }

        val newOutlet = Outlet(
            id = "outlet_$slug",
            name = cleanName.removePrefix("Outlet ").trim().ifBlank { cleanName },
            address = cleanAddress,
            coordinate = GeoPoint(-6.21462, 106.82215),
            isActive = true,
            isOpen = true,
            openTime = formatRawTimeToDisplay(form.openTime, "08.00"),
            closeTime = formatRawTimeToDisplay(form.closeTime, "21.00"),
            dailyTarget = form.dailyTargetInput.toLongOrNull() ?: 2_000_000L
        )

        val saveResult = outletRepository.saveOutlet(newOutlet)
        if (saveResult.isFailure) {
            val msg = "Gagal menyimpan outlet ke Firestore: ${saveResult.exceptionOrNull()?.readableMessage()}"
            state.update { it.copy(addOutletForm = form.copy(isSaving = false, errorMessage = msg)) }
            effects.send(OwnerUiEffect.ShowSnackbar(msg))
            return
        }

        val savedId = saveResult.getOrNull().orEmpty().ifBlank { newOutlet.id }
        val persistedOutlet = newOutlet.copy(id = savedId)

        if (form.selectedStaffIds.isNotEmpty()) {
            val assignResult = userRepository.assignUsersToOutlet(form.selectedStaffIds.toList(), savedId)
            if (assignResult.isFailure) {
                val msg = "Outlet tersimpan, tetapi penugasan staf gagal: ${assignResult.exceptionOrNull()?.readableMessage()}"
                state.update { it.copy(addOutletForm = form.copy(isSaving = false, errorMessage = msg)) }
                effects.send(OwnerUiEffect.ShowSnackbar(msg))
                return
            }
        }

        state.update { current ->
            val updatedOutlets = current.outlets.filterNot { it.id == persistedOutlet.id } + persistedOutlet
            val updatedUsers = current.users.map { u ->
                if (u.id in form.selectedStaffIds) u.copy(outletId = persistedOutlet.id) else u
            }
            computeOwnerDerivedState(
                current.copy(outlets = updatedOutlets, users = updatedUsers, addOutletForm = AddOutletFormState())
            )
        }

        effects.send(OwnerUiEffect.ShowSnackbar("Outlet ${persistedOutlet.name} berhasil disimpan."))
        effects.send(OwnerUiEffect.NavigateBack)
    }

    suspend fun submitNewUser() {
        val form = state.value.addUserForm
        val cleanName = form.fullName.trim()
        val cleanEmailInput = form.emailOrPhone.trim()
        val error = when {
            cleanName.isBlank() -> "Nama lengkap wajib diisi."
            cleanEmailInput.isBlank() -> "Email atau nomor HP wajib diisi."
            form.password.length < 6 -> "Kata sandi sementara minimal 6 karakter."
            form.selectedRole == Role.OUTLET && form.selectedOutletId.isBlank() -> "Pilih outlet tempat staf ditugaskan."
            else -> null
        }
        if (error != null) {
            state.update { it.copy(addUserForm = form.copy(errorMessage = error)) }
            return
        }

        state.update { it.copy(addUserForm = form.copy(isSaving = true, errorMessage = null)) }

        val normalizedEmail = if (cleanEmailInput.contains("@")) {
            cleanEmailInput.lowercase(Locale.ROOT)
        } else {
            "${cleanEmailInput.lowercase(Locale.ROOT).replace(Regex("[^a-z0-9]"), "")}@sudutbulat.id"
        }
        val slug = cleanName.lowercase(Locale.ROOT).replace(Regex("[^a-z0-9]+"), "_").trim('_')
            .ifBlank { "user_${System.currentTimeMillis()}" }
        val prefix = if (form.selectedRole == Role.WAREHOUSE) "gudang" else "staff"

        val authResult = authRepository.registerUserAccount(normalizedEmail, form.password)
        if (authResult.isFailure) {
            val msg = "Gagal membuat akun Auth: ${authResult.exceptionOrNull()?.readableMessage()}"
            state.update { it.copy(addUserForm = form.copy(isSaving = false, errorMessage = msg)) }
            effects.send(OwnerUiEffect.ShowSnackbar(msg))
            return
        }

        val authUid = authResult.getOrNull()
        val newUser = User(
            id = "${prefix}_$slug",
            name = cleanName,
            email = normalizedEmail,
            username = slug,
            role = form.selectedRole,
            outletId = if (form.selectedRole == Role.OUTLET) form.selectedOutletId else null,
            isActive = true,
            authUid = authUid
        )

        val saveResult = userRepository.saveUser(newUser)
        if (saveResult.isFailure) {
            val msg = "Akun Auth dibuat, tetapi gagal simpan profil ke Firestore: ${saveResult.exceptionOrNull()?.readableMessage()}"
            state.update { it.copy(addUserForm = form.copy(isSaving = false, errorMessage = msg)) }
            effects.send(OwnerUiEffect.ShowSnackbar(msg))
            return
        }

        state.update { current ->
            val updated = current.users.filterNot { it.id == newUser.id } + newUser
            computeOwnerDerivedState(
                current.copy(
                    users = updated,
                    addUserForm = AddUserFormState(selectedOutletId = current.outletItems.firstOrNull()?.id.orEmpty())
                )
            )
        }

        effects.send(OwnerUiEffect.ShowSnackbar("Akun ${newUser.name} berhasil dibuat (kode: ${newUser.id})."))
        effects.send(OwnerUiEffect.NavigateBack)
    }

    suspend fun submitCatalogItem() {
        val form = state.value.addCatalogForm
        val cleanName = form.name.trim()
        if (cleanName.isBlank()) {
            state.update { it.copy(addCatalogForm = form.copy(errorMessage = "Nama barang wajib diisi.")) }
            return
        }

        val resolvedPcsPerPack = if (form.isProduct && form.saleType == ProductSaleType.BUNGKUS) {
            val parsed = form.pcsPerPackInput.toIntOrNull()
            if (parsed == null || parsed < 2) {
                state.update {
                    it.copy(addCatalogForm = form.copy(errorMessage = "Jumlah pcs per bungkus minimal 2."))
                }
                return
            }
            parsed
        } else {
            1
        }

        state.update { it.copy(addCatalogForm = form.copy(isSaving = true, errorMessage = null)) }
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
                state.update { it.copy(addCatalogForm = form.copy(isSaving = false, errorMessage = msg)) }
                effects.send(OwnerUiEffect.ShowSnackbar(msg))
                return
            }
            state.update { current ->
                val updated = current.products.filterNot { it.id == product.id } + product
                computeOwnerDerivedState(
                    current.copy(products = updated, addCatalogForm = AddCatalogItemFormState(isProduct = true))
                )
            }
        } else {
            val material = Material(
                id = form.editingItemId ?: "mat_$slug",
                name = cleanName,
                cost = priceOrCost,
                unit = form.unit,
                isActive = form.isActive
            )
            val result: Result<Any> = if (isEdit) {
                materialRepository.updateMaterial(material)
            } else {
                materialRepository.saveMaterial(material)
            }
            if (result.isFailure) {
                val msg = "Gagal menyimpan material: ${result.exceptionOrNull()?.readableMessage()}"
                state.update { it.copy(addCatalogForm = form.copy(isSaving = false, errorMessage = msg)) }
                effects.send(OwnerUiEffect.ShowSnackbar(msg))
                return
            }
            state.update { current ->
                val updated = current.materials.filterNot { it.id == material.id } + material
                computeOwnerDerivedState(
                    current.copy(materials = updated, addCatalogForm = AddCatalogItemFormState(isProduct = false))
                )
            }
        }

        val actionWord = if (isEdit) "diperbarui" else "ditambahkan ke katalog"
        effects.send(OwnerUiEffect.ShowSnackbar("Barang $cleanName berhasil $actionWord."))
        effects.send(OwnerUiEffect.CatalogSaved)
    }

    suspend fun toggleCatalogItemActive(item: CatalogItemUi) {
        val nextActive = !item.isActive
        val result = if (item.isProduct) {
            productRepository.setProductActive(item.id, nextActive)
        } else {
            materialRepository.setMaterialActive(item.id, nextActive)
        }

        result.onSuccess {
            state.update { current ->
                if (item.isProduct) {
                    val updated = current.products.map {
                        if (it.id == item.id) it.copy(isActive = nextActive) else it
                    }
                    computeOwnerDerivedState(current.copy(products = updated))
                } else {
                    val updated = current.materials.map {
                        if (it.id == item.id) it.copy(isActive = nextActive) else it
                    }
                    computeOwnerDerivedState(current.copy(materials = updated))
                }
            }
            val label = when {
                item.isProduct && nextActive -> "kembali dijual"
                item.isProduct -> "dihentikan penjualannya"
                nextActive -> "kembali dipakai"
                else -> "dihentikan pemakaiannya"
            }
            effects.send(OwnerUiEffect.ShowSnackbar("${item.name} $label."))
        }.onFailure { e ->
            effects.send(OwnerUiEffect.ShowSnackbar("Gagal mengubah status ${item.name}: ${e.readableMessage()}"))
        }
    }
}
