package com.sudutcorp.sudutbulat.feature.owner

import com.google.firebase.firestore.GeoPoint
import com.sudutcorp.sudutbulat.core.util.formatRawTimeToDisplay
import com.sudutcorp.sudutbulat.core.util.isValidRawTime
import com.sudutcorp.sudutbulat.data.auth.AuthRepository
import com.sudutcorp.sudutbulat.data.model.Material
import com.sudutcorp.sudutbulat.data.model.Outlet
import com.sudutcorp.sudutbulat.data.model.Product
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

/**
 * Handles validation, optimistic state updates, and Firestore/Auth write operations
 * for Owner CRUD flows (outlets, users/staff, and catalog items).
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
        state.update { current ->
            val updated = current.outlets.ifEmpty { defaultSeededOutlets() }.map {
                if (it.id == outletId) it.copy(isOpen = newOpen) else it
            }
            computeOwnerDerivedState(current.copy(outlets = updated))
        }
        outletRepository.setOutletOpen(outletId, newOpen)
    }

    suspend fun assignStaffToOutlet(outletId: String, selectedStaffIds: Set<String>) {
        val baseUsers = state.value.users.ifEmpty { defaultSeededUsers() }
        val previouslyAssigned = baseUsers
            .filter { it.role == Role.OUTLET && it.outletId == outletId }
            .map { it.id }
            .toSet()

        val newlyAdded = selectedStaffIds - previouslyAssigned
        val removed = previouslyAssigned - selectedStaffIds

        state.update { current ->
            val updatedUsers = current.users.ifEmpty { defaultSeededUsers() }.map { user ->
                when (user.id) {
                    in newlyAdded -> user.copy(outletId = outletId)
                    in removed -> user.copy(outletId = null)
                    else -> user
                }
            }
            computeOwnerDerivedState(current.copy(users = updatedUsers))
        }

        if (newlyAdded.isNotEmpty()) {
            userRepository.assignUsersToOutlet(newlyAdded.toList(), outletId)
        }
        removed.forEach { uid -> userRepository.setUserOutlet(uid, null) }
    }

    suspend fun reassignSingleStaffOutlet(userId: String, outletId: String?) {
        state.update { current ->
            val updated = current.users.ifEmpty { defaultSeededUsers() }.map {
                if (it.id == userId) it.copy(outletId = outletId) else it
            }
            computeOwnerDerivedState(current.copy(users = updated))
        }
        userRepository.setUserOutlet(userId, outletId)
    }

    suspend fun toggleUserActive(userId: String) {
        val target = state.value.users.ifEmpty { defaultSeededUsers() }.firstOrNull { it.id == userId } ?: return
        val newActive = !target.isActive
        state.update { current ->
            val updated = current.users.ifEmpty { defaultSeededUsers() }.map {
                if (it.id == userId) it.copy(isActive = newActive) else it
            }
            computeOwnerDerivedState(current.copy(users = updated))
        }
        userRepository.setUserActive(userId, newActive)
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

        state.update { current ->
            val baseOutlets = current.outlets.ifEmpty { defaultSeededOutlets() }
            val updatedOutlets = baseOutlets.filterNot { it.id == newOutlet.id } + newOutlet
            val updatedUsers = current.users.ifEmpty { defaultSeededUsers() }.map { u ->
                if (u.id in form.selectedStaffIds) u.copy(outletId = newOutlet.id) else u
            }
            computeOwnerDerivedState(
                current.copy(outlets = updatedOutlets, users = updatedUsers, addOutletForm = AddOutletFormState())
            )
        }

        val savedId = outletRepository.saveOutlet(newOutlet).getOrNull() ?: newOutlet.id
        if (form.selectedStaffIds.isNotEmpty()) {
            userRepository.assignUsersToOutlet(form.selectedStaffIds.toList(), savedId)
        }
        effects.send(OwnerUiEffect.ShowSnackbar("Outlet ${newOutlet.name} berhasil disimpan."))
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

        val newUser = User(
            id = "${prefix}_$slug",
            name = cleanName,
            email = normalizedEmail,
            username = slug,
            role = form.selectedRole,
            outletId = if (form.selectedRole == Role.OUTLET) form.selectedOutletId else null,
            isActive = true
        )

        state.update { current ->
            val updated = current.users.ifEmpty { defaultSeededUsers() }.filterNot { it.id == newUser.id } + newUser
            computeOwnerDerivedState(
                current.copy(
                    users = updated,
                    addUserForm = AddUserFormState(selectedOutletId = current.outletItems.firstOrNull()?.id.orEmpty())
                )
            )
        }

        authRepository.registerUserAccount(normalizedEmail, form.password)
        userRepository.saveUser(newUser)
        effects.send(OwnerUiEffect.ShowSnackbar("Akun ${newUser.name} berhasil dibuat."))
        effects.send(OwnerUiEffect.NavigateBack)
    }

    suspend fun submitCatalogItem() {
        val form = state.value.addCatalogForm
        val cleanName = form.name.trim()
        if (cleanName.isBlank()) {
            state.update { it.copy(addCatalogForm = form.copy(errorMessage = "Nama barang wajib diisi.")) }
            return
        }

        state.update { it.copy(addCatalogForm = form.copy(isSaving = true, errorMessage = null)) }
        val priceOrCost = form.priceOrCostInput.toLongOrNull() ?: 0L
        val slug = cleanName.lowercase(Locale.ROOT).replace(Regex("[^a-z0-9]+"), "_").trim('_')

        if (form.isProduct) {
            val newProduct = Product("prod_$slug", cleanName, priceOrCost, true)
            state.update { current ->
                val updated = current.products.ifEmpty { defaultSeededProducts() }.filterNot { it.id == newProduct.id } + newProduct
                computeOwnerDerivedState(current.copy(products = updated, addCatalogForm = AddCatalogItemFormState(isProduct = true)))
            }
            productRepository.saveProduct(newProduct)
        } else {
            val newMaterial = Material("mat_$slug", cleanName, priceOrCost, form.unit)
            state.update { current ->
                val updated = current.materials.ifEmpty { defaultSeededMaterials() }.filterNot { it.id == newMaterial.id } + newMaterial
                computeOwnerDerivedState(current.copy(materials = updated, addCatalogForm = AddCatalogItemFormState(isProduct = false)))
            }
            materialRepository.saveMaterial(newMaterial)
        }
        effects.send(OwnerUiEffect.ShowSnackbar("Barang $cleanName berhasil ditambahkan ke katalog."))
    }
}
