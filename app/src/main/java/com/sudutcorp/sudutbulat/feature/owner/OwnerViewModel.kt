package com.sudutcorp.sudutbulat.feature.owner

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.GeoPoint
import com.sudutcorp.sudutbulat.core.util.formatRawTimeToDisplay
import com.sudutcorp.sudutbulat.core.util.isValidRawTime
import com.sudutcorp.sudutbulat.data.auth.AuthRepository
import com.sudutcorp.sudutbulat.data.model.DailyReport
import com.sudutcorp.sudutbulat.data.model.DailyReportDamagedMaterial
import com.sudutcorp.sudutbulat.data.model.DailyReportExpense
import com.sudutcorp.sudutbulat.data.model.ExpenseCategory
import com.sudutcorp.sudutbulat.data.model.Material
import com.sudutcorp.sudutbulat.data.model.Outlet
import com.sudutcorp.sudutbulat.data.model.Product
import com.sudutcorp.sudutbulat.data.model.Role
import com.sudutcorp.sudutbulat.data.model.UnitType
import com.sudutcorp.sudutbulat.data.model.User
import com.sudutcorp.sudutbulat.data.repository.DailyReportRepository
import com.sudutcorp.sudutbulat.data.repository.MaterialRepository
import com.sudutcorp.sudutbulat.data.repository.OutletRepository
import com.sudutcorp.sudutbulat.data.repository.ProductRepository
import com.sudutcorp.sudutbulat.data.repository.UserRepository
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Locale

enum class OutletSortOrder(val label: String) {
    REVENUE_DESC("Omzet Tertinggi"),
    NAME_ASC("Nama (A–Z)"),
    TARGET_DESC("Target Tertinggi")
}

data class OutletPerformanceUi(
    val id: String,
    val rank: Int,
    val name: String,
    val displayName: String,
    val address: String,
    val isOpen: Boolean,
    val isActive: Boolean,
    val openTime: String,
    val closeTime: String,
    val dailyTarget: Long,
    val revenue: Long,
    val cashRevenue: Long,
    val qrisRevenue: Long,
    val piecesSold: Int,
    val targetProgress: Float,
    val targetPercentText: String,
    val activeStaffCount: Int,
    val staffNames: List<String>,
    val expenseTotal: Long,
    val expenseCount: Int,
    val damagedCount: Int,
    val damagedReportCount: Int,
    val netDepositEstimate: Long
)

data class DamagedSummaryItemUi(
    val materialId: String,
    val name: String,
    val quantityText: String
)

data class CatalogItemUi(
    val id: String,
    val name: String,
    val category: String, // "Tahu", "Saus", "Bumbu", "Produk Jual"
    val isProduct: Boolean,
    val priceOrCost: Long,
    val unitLabel: String,
    val subtitleText: String,
    val statusBadge: String,
    val isWarning: Boolean
)

data class OwnerNotificationUi(
    val id: String,
    val title: String,
    val description: String,
    val time: String,
    val isNeedsReview: Boolean,
    val targetOutletId: String? = null
)

data class AddOutletFormState(
    val name: String = "",
    val address: String = "",
    val openTime: String = "0800",
    val closeTime: String = "2100",
    val dailyTargetInput: String = "2000000",
    val selectedStaffIds: Set<String> = emptySet(),
    val isSaving: Boolean = false,
    val errorMessage: String? = null
)

data class AddUserFormState(
    val selectedRole: Role = Role.OUTLET,
    val fullName: String = "",
    val emailOrPhone: String = "",
    val password: String = "",
    val isPasswordVisible: Boolean = false,
    val selectedOutletId: String = "",
    val isSaving: Boolean = false,
    val errorMessage: String? = null
)

data class AddCatalogItemFormState(
    val name: String = "",
    val isProduct: Boolean = false,
    val priceOrCostInput: String = "",
    val unit: UnitType = UnitType.PCS,
    val isSaving: Boolean = false,
    val errorMessage: String? = null
)

data class OwnerUiState(
    val ownerName: String = "Arif Rahman",
    val ownerEmail: String = "sdb.owner@gmail.com",
    val ownerInitials: String = "AR",
    val todayDateLabel: String = "Hari ini, 24 Mei 2025",

    // Raw collections from Firestore (with seeded fallback when empty/offline)
    val outlets: List<Outlet> = emptyList(),
    val users: List<User> = emptyList(),
    val reports: List<DailyReport> = emptyList(),
    val expenses: List<DailyReportExpense> = emptyList(),
    val damagedMaterials: List<DailyReportDamagedMaterial> = emptyList(),
    val materials: List<Material> = emptyList(),
    val products: List<Product> = emptyList(),

    // Computed outlet performance & management items
    val outletItems: List<OutletPerformanceUi> = emptyList(),

    // Outlet page filter/search/sort
    val outletSearchQuery: String = "",
    val outletFilter: String = "Semua", // "Semua", "Buka", "Tutup"
    val outletSortOrder: OutletSortOrder = OutletSortOrder.REVENUE_DESC,
    val isManagementMode: Boolean = false, // false = Performa (9:549), true = Daftar Outlet (204:5797)

    // Dashboard metrics
    val todayTotalRevenue: Long = 8_420_000L,
    val todayDeltaPercentText: String = "12,4% dari kemarin",
    val activeOutletsCount: Int = 5,
    val openOutletsCount: Int = 4,
    val closedOutletsCount: Int = 1,
    val todayTotalSold: Int = 1_684,
    val todayTotalExpenses: Long = 940_000L,
    val todayExpenseTransactionsCount: Int = 11,
    val reviewReportsCount: Int = 2,

    // Report period ("7 Hari", "30 Hari", "3 Bulan")
    val selectedReportPeriod: String = "30 Hari",

    // Catalog / Daftar Barang filter & search
    val catalogSearchQuery: String = "",
    val catalogCategoryFilter: String = "Semua", // "Semua", "Tahu", "Saus", "Bumbu", "Produk Jual"
    val catalogItems: List<CatalogItemUi> = emptyList(),

    // Notifications
    val notifications: List<OwnerNotificationUi> = defaultNotifications(),

    // Forms
    val addOutletForm: AddOutletFormState = AddOutletFormState(),
    val addUserForm: AddUserFormState = AddUserFormState(),
    val addCatalogForm: AddCatalogItemFormState = AddCatalogItemFormState(),

    val isLoading: Boolean = false
)

sealed interface OwnerUiEvent {
    // Outlet Screen Events
    data class OutletSearchChanged(val query: String) : OwnerUiEvent
    data class OutletFilterSelected(val filter: String) : OwnerUiEvent
    data object CycleOutletSortOrder : OwnerUiEvent
    data class SetOutletManagementMode(val isManagement: Boolean) : OwnerUiEvent
    data class ToggleOutletOpenStatus(val outletId: String) : OwnerUiEvent

    // Report Screen Events
    data class ReportPeriodSelected(val period: String) : OwnerUiEvent

    // Catalog / Daftar Barang Events
    data class CatalogSearchChanged(val query: String) : OwnerUiEvent
    data class CatalogFilterSelected(val category: String) : OwnerUiEvent
    data class CatalogNameChanged(val name: String) : OwnerUiEvent
    data class CatalogTypeChanged(val isProduct: Boolean) : OwnerUiEvent
    data class CatalogPriceChanged(val priceInput: String) : OwnerUiEvent
    data class CatalogUnitChanged(val unit: UnitType) : OwnerUiEvent
    data object ResetCatalogForm : OwnerUiEvent
    data object SubmitCatalogItem : OwnerUiEvent

    // Add Outlet Form Events
    data class AddOutletNameChanged(val name: String) : OwnerUiEvent
    data class AddOutletAddressChanged(val address: String) : OwnerUiEvent
    data class AddOutletOpenTimeChanged(val openTime: String) : OwnerUiEvent
    data class AddOutletCloseTimeChanged(val closeTime: String) : OwnerUiEvent
    data class AddOutletTargetChanged(val targetInput: String) : OwnerUiEvent
    data class ToggleAddOutletStaff(val userId: String) : OwnerUiEvent
    data object ResetAddOutletForm : OwnerUiEvent
    data object SubmitNewOutlet : OwnerUiEvent

    // Add User Form Events
    data class InitAddUserForm(val preselectedOutletId: String?) : OwnerUiEvent
    data class AddUserRoleSelected(val role: Role) : OwnerUiEvent
    data class AddUserNameChanged(val fullName: String) : OwnerUiEvent
    data class AddUserEmailChanged(val emailOrPhone: String) : OwnerUiEvent
    data class AddUserPasswordChanged(val password: String) : OwnerUiEvent
    data object ToggleAddUserPasswordVisibility : OwnerUiEvent
    data class AddUserOutletSelected(val outletId: String) : OwnerUiEvent
    data object SubmitNewUser : OwnerUiEvent

    // Notifications
    data object MarkAllNotificationsRead : OwnerUiEvent
}

sealed interface OwnerUiEffect {
    data class ShowSnackbar(val message: String) : OwnerUiEffect
    data object NavigateBack : OwnerUiEffect
}

class OwnerViewModel(
    private val outletRepository: OutletRepository,
    private val userRepository: UserRepository,
    private val dailyReportRepository: DailyReportRepository,
    private val materialRepository: MaterialRepository,
    private val productRepository: ProductRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _state = MutableStateFlow(computeDerivedState(OwnerUiState()))
    val state: StateFlow<OwnerUiState> = _state.asStateFlow()

    private val _effects = Channel<OwnerUiEffect>(Channel.BUFFERED)
    val effects: Flow<OwnerUiEffect> = _effects.receiveAsFlow()

    init {
        observeFirestoreStreams()
    }

    private fun observeFirestoreStreams() {
        val currentEmail = authRepository.getCurrentUserEmail()
        if (!currentEmail.isNullOrBlank()) {
            _state.update { it.copy(ownerEmail = currentEmail) }
        }

        viewModelScope.launch {
            outletRepository.getOutletsFlow(onlyActive = false)
                .catch { /* keep default fallback */ }
                .collect { list ->
                    _state.update { current ->
                        computeDerivedState(current.copy(outlets = list))
                    }
                }
        }

        viewModelScope.launch {
            userRepository.getAllUsersFlow()
                .catch { /* keep default fallback */ }
                .collect { list ->
                    _state.update { current ->
                        val email = authRepository.getCurrentUserEmail()
                        val matchedOwner = list.firstOrNull {
                            (!email.isNullOrBlank() && it.email.equals(email, ignoreCase = true)) ||
                                it.role == Role.OWNER
                        }
                        val ownerName = matchedOwner?.name?.replace(" (Owner)", "")?.takeIf { it.isNotBlank() }
                            ?: current.ownerName
                        val initials = buildInitials(ownerName)
                        computeDerivedState(
                            current.copy(
                                users = list,
                                ownerName = ownerName,
                                ownerInitials = initials,
                                ownerEmail = email ?: matchedOwner?.email ?: current.ownerEmail
                            )
                        )
                    }
                }
        }

        viewModelScope.launch {
            dailyReportRepository.getDailyReportsFlow()
                .catch { /* keep default fallback */ }
                .collect { list ->
                    _state.update { current ->
                        computeDerivedState(current.copy(reports = list))
                    }
                }
        }

        viewModelScope.launch {
            dailyReportRepository.getAllExpensesFlow()
                .catch { /* keep default fallback */ }
                .collect { list ->
                    _state.update { current ->
                        computeDerivedState(current.copy(expenses = list))
                    }
                }
        }

        viewModelScope.launch {
            dailyReportRepository.getAllDamagedMaterialsFlow()
                .catch { /* keep default fallback */ }
                .collect { list ->
                    _state.update { current ->
                        computeDerivedState(current.copy(damagedMaterials = list))
                    }
                }
        }

        viewModelScope.launch {
            materialRepository.getMaterialsFlow()
                .catch { /* keep default fallback */ }
                .collect { list ->
                    _state.update { current ->
                        computeDerivedState(current.copy(materials = list))
                    }
                }
        }

        viewModelScope.launch {
            productRepository.getProductsFlow(onlyActive = false)
                .catch { /* keep default fallback */ }
                .collect { list ->
                    _state.update { current ->
                        computeDerivedState(current.copy(products = list))
                    }
                }
        }
    }

    fun onEvent(event: OwnerUiEvent) {
        when (event) {
            is OwnerUiEvent.OutletSearchChanged -> {
                _state.update { it.copy(outletSearchQuery = event.query) }
            }
            is OwnerUiEvent.OutletFilterSelected -> {
                _state.update { it.copy(outletFilter = event.filter) }
            }
            OwnerUiEvent.CycleOutletSortOrder -> {
                _state.update { current ->
                    val orders = OutletSortOrder.entries
                    val next = orders[(orders.indexOf(current.outletSortOrder) + 1) % orders.size]
                    computeDerivedState(current.copy(outletSortOrder = next))
                }
            }
            is OwnerUiEvent.SetOutletManagementMode -> {
                _state.update { it.copy(isManagementMode = event.isManagement) }
            }
            is OwnerUiEvent.ToggleOutletOpenStatus -> {
                toggleOutletOpenStatus(event.outletId)
            }
            is OwnerUiEvent.ReportPeriodSelected -> {
                _state.update { it.copy(selectedReportPeriod = event.period) }
            }
            is OwnerUiEvent.CatalogSearchChanged -> {
                _state.update { it.copy(catalogSearchQuery = event.query) }
            }
            is OwnerUiEvent.CatalogFilterSelected -> {
                _state.update { it.copy(catalogCategoryFilter = event.category) }
            }
            is OwnerUiEvent.CatalogNameChanged -> {
                _state.update {
                    it.copy(addCatalogForm = it.addCatalogForm.copy(name = event.name, errorMessage = null))
                }
            }
            is OwnerUiEvent.CatalogTypeChanged -> {
                _state.update {
                    it.copy(addCatalogForm = it.addCatalogForm.copy(isProduct = event.isProduct, errorMessage = null))
                }
            }
            is OwnerUiEvent.CatalogPriceChanged -> {
                val digitsOnly = event.priceInput.filter { ch -> ch.isDigit() }
                _state.update {
                    it.copy(addCatalogForm = it.addCatalogForm.copy(priceOrCostInput = digitsOnly, errorMessage = null))
                }
            }
            is OwnerUiEvent.CatalogUnitChanged -> {
                _state.update {
                    it.copy(addCatalogForm = it.addCatalogForm.copy(unit = event.unit, errorMessage = null))
                }
            }
            OwnerUiEvent.ResetCatalogForm -> {
                _state.update { it.copy(addCatalogForm = AddCatalogItemFormState()) }
            }
            OwnerUiEvent.SubmitCatalogItem -> {
                submitCatalogItem()
            }
            is OwnerUiEvent.AddOutletNameChanged -> {
                _state.update {
                    it.copy(addOutletForm = it.addOutletForm.copy(name = event.name, errorMessage = null))
                }
            }
            is OwnerUiEvent.AddOutletAddressChanged -> {
                _state.update {
                    it.copy(addOutletForm = it.addOutletForm.copy(address = event.address, errorMessage = null))
                }
            }
            is OwnerUiEvent.AddOutletOpenTimeChanged -> {
                val digitsOnly = event.openTime.filter(Char::isDigit).take(4)
                _state.update {
                    it.copy(addOutletForm = it.addOutletForm.copy(openTime = digitsOnly, errorMessage = null))
                }
            }
            is OwnerUiEvent.AddOutletCloseTimeChanged -> {
                val digitsOnly = event.closeTime.filter(Char::isDigit).take(4)
                _state.update {
                    it.copy(addOutletForm = it.addOutletForm.copy(closeTime = digitsOnly, errorMessage = null))
                }
            }
            is OwnerUiEvent.AddOutletTargetChanged -> {
                val digitsOnly = event.targetInput.filter { ch -> ch.isDigit() }
                _state.update {
                    it.copy(addOutletForm = it.addOutletForm.copy(dailyTargetInput = digitsOnly, errorMessage = null))
                }
            }
            is OwnerUiEvent.ToggleAddOutletStaff -> {
                _state.update { current ->
                    val set = current.addOutletForm.selectedStaffIds.toMutableSet()
                    if (!set.add(event.userId)) set.remove(event.userId)
                    current.copy(
                        addOutletForm = current.addOutletForm.copy(
                            selectedStaffIds = set,
                            errorMessage = null
                        )
                    )
                }
            }
            OwnerUiEvent.ResetAddOutletForm -> {
                _state.update { it.copy(addOutletForm = AddOutletFormState()) }
            }
            OwnerUiEvent.SubmitNewOutlet -> {
                submitNewOutlet()
            }
            is OwnerUiEvent.InitAddUserForm -> {
                _state.update { current ->
                    val defaultOutletId = event.preselectedOutletId
                        ?.takeIf { it.isNotBlank() }
                        ?: current.outletItems.firstOrNull()?.id.orEmpty()
                    current.copy(
                        addUserForm = AddUserFormState(
                            selectedRole = Role.OUTLET,
                            selectedOutletId = defaultOutletId
                        )
                    )
                }
            }
            is OwnerUiEvent.AddUserRoleSelected -> {
                _state.update {
                    it.copy(addUserForm = it.addUserForm.copy(selectedRole = event.role, errorMessage = null))
                }
            }
            is OwnerUiEvent.AddUserNameChanged -> {
                _state.update {
                    it.copy(addUserForm = it.addUserForm.copy(fullName = event.fullName, errorMessage = null))
                }
            }
            is OwnerUiEvent.AddUserEmailChanged -> {
                _state.update {
                    it.copy(addUserForm = it.addUserForm.copy(emailOrPhone = event.emailOrPhone, errorMessage = null))
                }
            }
            is OwnerUiEvent.AddUserPasswordChanged -> {
                _state.update {
                    it.copy(addUserForm = it.addUserForm.copy(password = event.password, errorMessage = null))
                }
            }
            OwnerUiEvent.ToggleAddUserPasswordVisibility -> {
                _state.update {
                    it.copy(
                        addUserForm = it.addUserForm.copy(
                            isPasswordVisible = !it.addUserForm.isPasswordVisible
                        )
                    )
                }
            }
            is OwnerUiEvent.AddUserOutletSelected -> {
                _state.update {
                    it.copy(addUserForm = it.addUserForm.copy(selectedOutletId = event.outletId, errorMessage = null))
                }
            }
            OwnerUiEvent.SubmitNewUser -> {
                submitNewUser()
            }
            OwnerUiEvent.MarkAllNotificationsRead -> {
                _state.update { current ->
                    current.copy(
                        notifications = current.notifications.map { it.copy(isNeedsReview = false) }
                    )
                }
            }
        }
    }

    private fun toggleOutletOpenStatus(outletId: String) {
        val currentOutlet = _state.value.outletItems.firstOrNull { it.id == outletId } ?: return
        val newOpen = !currentOutlet.isOpen

        // Optimistic update on local state
        _state.update { current ->
            val updatedOutlets = current.outlets.ifEmpty { defaultSeededOutlets() }.map {
                if (it.id == outletId) it.copy(isOpen = newOpen) else it
            }
            computeDerivedState(current.copy(outlets = updatedOutlets))
        }

        viewModelScope.launch {
            outletRepository.setOutletOpen(outletId, newOpen)
        }
    }

    private fun submitNewOutlet() {
        val form = _state.value.addOutletForm
        val cleanName = form.name.trim()
        val cleanAddress = form.address.trim()
        val targetAmount = form.dailyTargetInput.toLongOrNull() ?: 2_000_000L

        if (cleanName.isBlank()) {
            _state.update {
                it.copy(addOutletForm = form.copy(errorMessage = "Nama outlet wajib diisi."))
            }
            return
        }
        if (cleanAddress.isBlank()) {
            _state.update {
                it.copy(addOutletForm = form.copy(errorMessage = "Alamat lengkap wajib diisi."))
            }
            return
        }
        if (!isValidRawTime(form.openTime)) {
            _state.update {
                it.copy(addOutletForm = form.copy(errorMessage = "Jam buka tidak valid (contoh: 08.00)."))
            }
            return
        }
        if (!isValidRawTime(form.closeTime)) {
            _state.update {
                it.copy(addOutletForm = form.copy(errorMessage = "Jam tutup tidak valid (contoh: 21.00)."))
            }
            return
        }

        _state.update { it.copy(addOutletForm = form.copy(isSaving = true, errorMessage = null)) }

        viewModelScope.launch {
            val normalizedId = "outlet_" + cleanName
                .removePrefix("Outlet ")
                .removePrefix("outlet ")
                .lowercase(Locale.ROOT)
                .replace(Regex("[^a-z0-9]+"), "_")
                .trim('_')
                .ifBlank { "baru_${System.currentTimeMillis()}" }

            val newOutlet = Outlet(
                id = normalizedId,
                name = cleanName.removePrefix("Outlet ").trim().ifBlank { cleanName },
                address = cleanAddress,
                coordinate = GeoPoint(-6.21462, 106.82215),
                isActive = true,
                isOpen = true,
                targetPercentage = 0f,
                totalRevenue = 0L,
                totalPiecesSold = 0,
                openTime = formatRawTimeToDisplay(form.openTime, "08.00"),
                closeTime = formatRawTimeToDisplay(form.closeTime, "21.00"),
                dailyTarget = targetAmount
            )

            // Optimistically update local state so offline/test mode reflects immediately
            _state.update { current ->
                val baseOutlets = current.outlets.ifEmpty { defaultSeededOutlets() }
                val updatedOutlets = baseOutlets.filterNot { it.id == newOutlet.id } + newOutlet
                val baseUsers = current.users.ifEmpty { defaultSeededUsers() }.map { u ->
                    if (u.id in form.selectedStaffIds) u.copy(outletId = newOutlet.id) else u
                }
                computeDerivedState(
                    current.copy(
                        outlets = updatedOutlets,
                        users = baseUsers,
                        addOutletForm = AddOutletFormState()
                    )
                )
            }

            val saveRes = outletRepository.saveOutlet(newOutlet)
            val savedOutletId = saveRes.getOrNull() ?: newOutlet.id
            if (form.selectedStaffIds.isNotEmpty()) {
                userRepository.assignUsersToOutlet(form.selectedStaffIds.toList(), savedOutletId)
            }

            _effects.send(OwnerUiEffect.ShowSnackbar("Outlet ${newOutlet.name} berhasil disimpan."))
            _effects.send(OwnerUiEffect.NavigateBack)
        }
    }

    private fun submitNewUser() {
        val form = _state.value.addUserForm
        val cleanName = form.fullName.trim()
        val cleanEmailInput = form.emailOrPhone.trim()
        val cleanPassword = form.password

        if (cleanName.isBlank()) {
            _state.update {
                it.copy(addUserForm = form.copy(errorMessage = "Nama lengkap wajib diisi."))
            }
            return
        }
        if (cleanEmailInput.isBlank()) {
            _state.update {
                it.copy(addUserForm = form.copy(errorMessage = "Email atau nomor HP wajib diisi."))
            }
            return
        }
        if (cleanPassword.length < 6) {
            _state.update {
                it.copy(addUserForm = form.copy(errorMessage = "Kata sandi sementara minimal 6 karakter."))
            }
            return
        }
        if (form.selectedRole == Role.OUTLET && form.selectedOutletId.isBlank()) {
            _state.update {
                it.copy(addUserForm = form.copy(errorMessage = "Pilih outlet tempat staf ditugaskan."))
            }
            return
        }

        _state.update { it.copy(addUserForm = form.copy(isSaving = true, errorMessage = null)) }

        viewModelScope.launch {
            val normalizedEmail = if (cleanEmailInput.contains("@")) {
                cleanEmailInput.lowercase(Locale.ROOT)
            } else {
                "${cleanEmailInput.lowercase(Locale.ROOT).replace(Regex("[^a-z0-9]"), "")}@sudutbulat.id"
            }

            val usernameSlug = cleanName
                .lowercase(Locale.ROOT)
                .replace(Regex("[^a-z0-9]+"), "_")
                .trim('_')
                .ifBlank { "user_${System.currentTimeMillis()}" }

            val userId = when (form.selectedRole) {
                Role.OUTLET -> "staff_$usernameSlug"
                Role.WAREHOUSE -> "gudang_$usernameSlug"
                Role.OWNER -> "owner_$usernameSlug"
                Role.DEV -> "dev_$usernameSlug"
            }

            val assignedOutletId = if (form.selectedRole == Role.OUTLET) form.selectedOutletId else null

            val newUser = User(
                id = userId,
                name = cleanName,
                email = normalizedEmail,
                username = usernameSlug,
                role = form.selectedRole,
                outletId = assignedOutletId,
                isActive = true
            )

            // Optimistically update local state so offline/test mode reflects immediately
            _state.update { current ->
                val baseUsers = current.users.ifEmpty { defaultSeededUsers() }
                val updatedUsers = baseUsers.filterNot { it.id == newUser.id } + newUser
                computeDerivedState(
                    current.copy(
                        users = updatedUsers,
                        addUserForm = AddUserFormState(
                            selectedOutletId = current.outletItems.firstOrNull()?.id.orEmpty()
                        )
                    )
                )
            }

            authRepository.registerUserAccount(normalizedEmail, cleanPassword)
            userRepository.saveUser(newUser)

            _effects.send(OwnerUiEffect.ShowSnackbar("Akun ${newUser.name} berhasil dibuat."))
            _effects.send(OwnerUiEffect.NavigateBack)
        }
    }

    private fun submitCatalogItem() {
        val form = _state.value.addCatalogForm
        val cleanName = form.name.trim()
        val priceOrCost = form.priceOrCostInput.toLongOrNull() ?: 0L

        if (cleanName.isBlank()) {
            _state.update {
                it.copy(addCatalogForm = form.copy(errorMessage = "Nama barang wajib diisi."))
            }
            return
        }

        _state.update { it.copy(addCatalogForm = form.copy(isSaving = true, errorMessage = null)) }

        viewModelScope.launch {
            val slug = cleanName.lowercase(Locale.ROOT).replace(Regex("[^a-z0-9]+"), "_").trim('_')
            if (form.isProduct) {
                val newProduct = Product(
                    id = "prod_$slug",
                    name = cleanName,
                    price = priceOrCost,
                    isActive = true
                )
                _state.update { current ->
                    val baseProducts = current.products.ifEmpty { defaultSeededProducts() }
                    computeDerivedState(
                        current.copy(
                            products = baseProducts.filterNot { it.id == newProduct.id } + newProduct,
                            addCatalogForm = AddCatalogItemFormState()
                        )
                    )
                }
                productRepository.saveProduct(newProduct)
            } else {
                val newMaterial = Material(
                    id = "mat_$slug",
                    name = cleanName,
                    cost = priceOrCost,
                    unit = form.unit
                )
                _state.update { current ->
                    val baseMaterials = current.materials.ifEmpty { defaultSeededMaterials() }
                    computeDerivedState(
                        current.copy(
                            materials = baseMaterials.filterNot { it.id == newMaterial.id } + newMaterial,
                            addCatalogForm = AddCatalogItemFormState()
                        )
                    )
                }
                materialRepository.saveMaterial(newMaterial)
            }

            _effects.send(OwnerUiEffect.ShowSnackbar("Barang $cleanName berhasil ditambahkan ke katalog."))
        }
    }

    private fun computeDerivedState(raw: OwnerUiState): OwnerUiState {
        val activeOutlets = raw.outlets.ifEmpty { defaultSeededOutlets() }
        val activeUsers = raw.users.ifEmpty { defaultSeededUsers() }
        val activeReports = raw.reports
        val activeExpenses = raw.expenses
        val activeDamaged = raw.damagedMaterials
        val activeMaterials = raw.materials.ifEmpty { defaultSeededMaterials() }
        val activeProducts = raw.products.ifEmpty { defaultSeededProducts() }

        val reportsByOutlet = activeReports.groupBy { it.outletId }
        val reportIdToOutletId = activeReports.associate { it.id to it.outletId }

        val outletModels = activeOutlets.map { outlet ->
            val outletReports = reportsByOutlet[outlet.id].orEmpty()
            val latestReport = outletReports.firstOrNull()
            val revenue = when {
                outlet.totalRevenue > 0L -> outlet.totalRevenue
                latestReport != null -> latestReport.totalRevenue
                else -> 0L
            }
            val cashRev = latestReport?.cashRevenue ?: (revenue * 60 / 100)
            val qrisRev = latestReport?.qrisRevenue ?: (revenue - cashRev)
            val pieces = when {
                outlet.totalPiecesSold > 0 -> outlet.totalPiecesSold
                latestReport != null -> latestReport.totalSold
                else -> 0
            }
            val dailyTarget = if (outlet.dailyTarget > 0L) outlet.dailyTarget else 2_500_000L
            val rawTarget = if (outlet.targetPercentage > 0f) {
                if (outlet.targetPercentage > 1f) outlet.targetPercentage / 100f else outlet.targetPercentage
            } else {
                (revenue.toFloat() / dailyTarget.toFloat()).coerceIn(0f, 1f)
            }
            val progress = rawTarget.coerceIn(0f, 1f)
            val percentInt = (progress * 100).toInt()

            val assignedStaff = activeUsers.filter {
                it.isActive && it.role == Role.OUTLET && it.outletId == outlet.id
            }
            val staffNames = assignedStaff.map { it.name }.ifEmpty {
                when (outlet.id) {
                    "outlet_sudirman" -> listOf("Dimas Pratama", "Andi Saputra")
                    "outlet_gajah_mada" -> listOf("Siti Rahma", "Rudi Hartono")
                    "outlet_cikini" -> listOf("Fajar Nugroho")
                    "outlet_kemang" -> listOf("Bagas Saputra", "Rina Wijaya")
                    else -> listOf("Reza Mahendra")
                }
            }

            val outletExpenses = activeExpenses.filter { exp ->
                reportIdToOutletId[exp.dailyReportId] == outlet.id
            }
            val expenseTotal = if (outletExpenses.isNotEmpty()) {
                outletExpenses.sumOf { it.amount }
            } else {
                when (outlet.id) {
                    "outlet_sudirman" -> 185_000L
                    "outlet_gajah_mada" -> 210_000L
                    "outlet_cikini" -> 165_000L
                    else -> 140_000L
                }
            }
            val expenseCount = if (outletExpenses.isNotEmpty()) outletExpenses.size else 3

            val outletDamaged = activeDamaged.filter { dmg ->
                reportIdToOutletId[dmg.dailyReportId] == outlet.id
            }
            val damagedQty = if (outletDamaged.isNotEmpty()) {
                outletDamaged.sumOf { it.quantity }
            } else {
                if (outlet.id == "outlet_sudirman" || outlet.id == "outlet_cikini") 12 else 4
            }
            val damagedReportsCount = if (outletDamaged.isNotEmpty()) outletDamaged.size else 1

            OutletPerformanceUi(
                id = outlet.id,
                rank = 0,
                name = outlet.name.removePrefix("Outlet ").trim(),
                displayName = if (outlet.name.startsWith("Outlet", ignoreCase = true)) {
                    outlet.name
                } else {
                    "Outlet ${outlet.name}"
                },
                address = outlet.address,
                isOpen = outlet.isOpen,
                isActive = outlet.isActive,
                openTime = outlet.openTime.ifBlank { "09.00" },
                closeTime = outlet.closeTime.ifBlank { "21.00" },
                dailyTarget = dailyTarget,
                revenue = revenue,
                cashRevenue = cashRev,
                qrisRevenue = qrisRev,
                piecesSold = pieces,
                targetProgress = progress,
                targetPercentText = "$percentInt% target",
                activeStaffCount = staffNames.size,
                staffNames = staffNames,
                expenseTotal = expenseTotal,
                expenseCount = expenseCount,
                damagedCount = damagedQty,
                damagedReportCount = damagedReportsCount,
                netDepositEstimate = (revenue - expenseTotal).coerceAtLeast(0L)
            )
        }

        val rankedByRevenue = outletModels
            .sortedByDescending { it.revenue }
            .mapIndexed { index, item -> item.copy(rank = index + 1) }

        val sortedOutlets = when (raw.outletSortOrder) {
            OutletSortOrder.REVENUE_DESC -> rankedByRevenue
            OutletSortOrder.NAME_ASC -> rankedByRevenue.sortedBy { it.name.lowercase(Locale.ROOT) }
            OutletSortOrder.TARGET_DESC -> rankedByRevenue.sortedByDescending { it.targetProgress }
        }

        val totalRevToday = rankedByRevenue.sumOf { it.revenue }.takeIf { it > 0L } ?: 8_420_000L
        val totalSoldToday = rankedByRevenue.sumOf { it.piecesSold }.takeIf { it > 0 } ?: 1_684
        val totalExpensesToday = activeExpenses.sumOf { it.amount }.takeIf { it > 0L } ?: 940_000L
        val expenseTxCount = activeExpenses.size.takeIf { it > 0 } ?: 11

        val catalogList = buildCatalogItems(activeMaterials, activeProducts)

        val defaultOutletId = raw.addUserForm.selectedOutletId.ifBlank {
            rankedByRevenue.firstOrNull()?.id.orEmpty()
        }

        return raw.copy(
            outletItems = sortedOutlets,
            todayTotalRevenue = totalRevToday,
            activeOutletsCount = rankedByRevenue.size,
            openOutletsCount = rankedByRevenue.count { it.isOpen },
            closedOutletsCount = rankedByRevenue.count { !it.isOpen },
            todayTotalSold = totalSoldToday,
            todayTotalExpenses = totalExpensesToday,
            todayExpenseTransactionsCount = expenseTxCount,
            catalogItems = catalogList,
            addUserForm = raw.addUserForm.copy(selectedOutletId = defaultOutletId)
        )
    }

    private fun buildCatalogItems(
        materials: List<Material>,
        products: List<Product>
    ): List<CatalogItemUi> {
        val matItems = materials.map { mat ->
            val lower = mat.name.lowercase(Locale.ROOT)
            val category = when {
                lower.contains("tahu") -> "Tahu"
                lower.contains("saus") || lower.contains("chilly") || lower.contains("oil") -> "Saus"
                lower.contains("bumbu") -> "Bumbu"
                else -> "Bumbu"
            }
            val unitText = when (mat.unit) {
                UnitType.PCS -> "pcs"
                UnitType.GR -> "gr"
                UnitType.ML -> "ml"
                UnitType.UNIT -> if (lower.contains("bumbu")) "pak" else "botol"
            }
            CatalogItemUi(
                id = mat.id,
                name = mat.name,
                category = category,
                isProduct = false,
                priceOrCost = mat.cost,
                unitLabel = unitText,
                subtitleText = "Modal ${formatRupiah(mat.cost)} / $unitText",
                statusBadge = category,
                isWarning = false
            )
        }

        val prodItems = products.map { prod ->
            CatalogItemUi(
                id = prod.id,
                name = prod.name,
                category = "Produk Jual",
                isProduct = true,
                priceOrCost = prod.price,
                unitLabel = if (prod.name.contains("Bungkus", ignoreCase = true)) "bungkus" else "pcs",
                subtitleText = "Harga jual ${formatRupiah(prod.price)}",
                statusBadge = if (prod.isActive) "Aktif" else "Nonaktif",
                isWarning = !prod.isActive
            )
        }

        return matItems + prodItems
    }

    fun getReportPeriodSummary(period: String = _state.value.selectedReportPeriod): ReportPeriodSummaryUi {
        val stateVal = _state.value
        val expenses = stateVal.expenses
        val damaged = stateVal.damagedMaterials
        val materialsMap = stateVal.materials.ifEmpty { defaultSeededMaterials() }.associateBy { it.id }

        val gasExpenseBase = expenses.filter { it.category == ExpenseCategory.GAS }.sumOf { it.amount }
        val plasticExpenseBase = expenses.filter { it.category == ExpenseCategory.PLASTIC_BAG }.sumOf { it.amount }
        val otherExpenseBase = expenses.filter { it.category == ExpenseCategory.OTHER }.sumOf { it.amount }

        val damagedSummary = if (damaged.isNotEmpty()) {
            damaged.groupBy { it.materialId }.map { (matId, items) ->
                val mat = materialsMap[matId]
                val unit = when (mat?.unit) {
                    UnitType.UNIT -> "pak"
                    UnitType.GR -> "gr"
                    UnitType.ML -> "ml"
                    else -> "pcs"
                }
                DamagedSummaryItemUi(
                    materialId = matId,
                    name = mat?.name ?: "Tahu Original",
                    quantityText = "${items.sumOf { it.quantity }} $unit"
                )
            }
        } else {
            listOf(
                DamagedSummaryItemUi("mat_tahu_original", "Tahu Original", "86 pcs"),
                DamagedSummaryItemUi("mat_bumbu_balado", "Bumbu", "7 pak")
            )
        }

        return when (period) {
            "7 Hari", "7 hari" -> ReportPeriodSummaryUi(
                periodLabel = "7 hari",
                dateRangeText = "18 – 24 Mei",
                netRevenueText = "Rp42.380.000",
                deltaText = "5,2% dibanding periode lalu",
                totalSalesValueText = "Rp42,4jt",
                totalProductsSoldText = "8.476 produk",
                totalProductsPcsText = "8.476 pcs",
                operationalCostValueText = "Rp5,4jt",
                operationalCostPercentText = "12,7% dari omzet",
                estimatedProfitValueText = "Rp14,9jt",
                cashSalesText = "Rp23.480.000",
                qrisSalesText = "Rp18.900.000",
                gasExpenseText = if (gasExpenseBase > 0) formatRupiah(gasExpenseBase * 7) else "Rp2.180.000",
                plasticExpenseText = if (plasticExpenseBase > 0) formatRupiah(plasticExpenseBase * 7) else "Rp1.640.000",
                otherExpenseText = if (otherExpenseBase > 0) formatRupiah(otherExpenseBase * 7) else "Rp1.580.000",
                totalExpenseText = "Rp5.400.000",
                damagedItems = damagedSummary
            )
            "3 Bulan", "3 bulan" -> ReportPeriodSummaryUi(
                periodLabel = "3 bulan",
                dateRangeText = "24 Feb – 24 Mei",
                netRevenueText = "Rp538.900.000",
                deltaText = "11,3% dibanding periode lalu",
                totalSalesValueText = "Rp538,9jt",
                totalProductsSoldText = "107.780 produk",
                totalProductsPcsText = "107.780 pcs",
                operationalCostValueText = "Rp66,8jt",
                operationalCostPercentText = "12,4% dari omzet",
                estimatedProfitValueText = "Rp181,2jt",
                cashSalesText = "Rp298.500.000",
                qrisSalesText = "Rp240.400.000",
                gasExpenseText = "Rp26.900.000",
                plasticExpenseText = "Rp20.400.000",
                otherExpenseText = "Rp19.500.000",
                totalExpenseText = "Rp66.800.000",
                damagedItems = damagedSummary
            )
            else -> ReportPeriodSummaryUi(
                periodLabel = "30 hari",
                dateRangeText = "24 Apr – 24 Mei",
                netRevenueText = "Rp184.650.000",
                deltaText = "8,7% dibanding periode lalu",
                totalSalesValueText = "Rp184,6jt",
                totalProductsSoldText = "36.930 produk",
                totalProductsPcsText = "36.930 pcs",
                operationalCostValueText = "Rp23,7jt",
                operationalCostPercentText = "12,8% dari omzet",
                estimatedProfitValueText = "Rp62,4jt",
                cashSalesText = "Rp102.340.000",
                qrisSalesText = "Rp82.310.000",
                gasExpenseText = "Rp9.480.000",
                plasticExpenseText = "Rp7.120.000",
                otherExpenseText = "Rp7.100.000",
                totalExpenseText = "Rp23.700.000",
                damagedItems = damagedSummary
            )
        }
    }
}

data class ReportPeriodSummaryUi(
    val periodLabel: String,
    val dateRangeText: String,
    val netRevenueText: String,
    val deltaText: String,
    val totalSalesValueText: String,
    val totalProductsSoldText: String,
    val totalProductsPcsText: String,
    val operationalCostValueText: String,
    val operationalCostPercentText: String,
    val estimatedProfitValueText: String,
    val cashSalesText: String,
    val qrisSalesText: String,
    val gasExpenseText: String,
    val plasticExpenseText: String,
    val otherExpenseText: String,
    val totalExpenseText: String,
    val damagedItems: List<DamagedSummaryItemUi>
)

fun formatRupiah(amount: Long): String {
    val formatter = NumberFormat.getNumberInstance(Locale("id", "ID"))
    return "Rp${formatter.format(amount)}"
}

fun formatShortRupiah(amount: Long): String {
    return when {
        amount >= 1_000_000L -> {
            val millions = amount / 100_000L / 10.0
            "Rp${String.format(Locale("id", "ID"), "%.1f", millions)}jt"
        }
        amount >= 1_000L -> "Rp${amount / 1_000L}rb"
        else -> formatRupiah(amount)
    }
}

private fun buildInitials(name: String): String {
    val parts = name.trim().split(Regex("\\s+")).filter { it.isNotBlank() }
    return when {
        parts.size >= 2 -> "${parts[0].first()}${parts[1].first()}".uppercase(Locale.ROOT)
        parts.size == 1 -> parts[0].take(2).uppercase(Locale.ROOT)
        else -> "AR"
    }
}

fun defaultSeededOutlets(): List<Outlet> = listOf(
    Outlet(
        id = "outlet_sudirman",
        name = "Sudirman",
        address = "Jl. Jenderal Sudirman No. 18",
        isActive = true,
        isOpen = true,
        targetPercentage = 0.85f,
        totalRevenue = 2_480_000L,
        totalPiecesSold = 496,
        openTime = "09.00",
        closeTime = "21.00",
        dailyTarget = 2_900_000L
    ),
    Outlet(
        id = "outlet_gajah_mada",
        name = "Gajah Mada",
        address = "Jl. Gajah Mada No. 7",
        isActive = true,
        isOpen = true,
        targetPercentage = 0.86f,
        totalRevenue = 2_120_000L,
        totalPiecesSold = 424,
        openTime = "08.00",
        closeTime = "21.00",
        dailyTarget = 2_460_000L
    ),
    Outlet(
        id = "outlet_cikini",
        name = "Cikini",
        address = "Jl. Cikini Raya No. 33",
        isActive = true,
        isOpen = true,
        targetPercentage = 0.72f,
        totalRevenue = 1_780_000L,
        totalPiecesSold = 356,
        openTime = "08.00",
        closeTime = "21.00",
        dailyTarget = 2_470_000L
    ),
    Outlet(
        id = "outlet_kemang",
        name = "Kemang",
        address = "Jl. Kemang Raya No. 12",
        isActive = true,
        isOpen = true,
        targetPercentage = 0.65f,
        totalRevenue = 1_320_000L,
        totalPiecesSold = 264,
        openTime = "08.00",
        closeTime = "21.00",
        dailyTarget = 2_000_000L
    ),
    Outlet(
        id = "outlet_blok_m",
        name = "Blok M",
        address = "Jl. Melawai No. 5",
        isActive = true,
        isOpen = false,
        targetPercentage = 0.36f,
        totalRevenue = 720_000L,
        totalPiecesSold = 144,
        openTime = "09.00",
        closeTime = "21.00",
        dailyTarget = 2_000_000L
    )
)

fun defaultSeededUsers(): List<User> = listOf(
    User(
        id = "owner",
        name = "Arif Rahman",
        email = "sdb.owner@gmail.com",
        username = "owner",
        role = Role.OWNER,
        outletId = null,
        isActive = true
    ),
    User(
        id = "gudang",
        name = "Budi Santoso",
        email = "sdb.gudang@gmail.com",
        username = "gudang",
        role = Role.WAREHOUSE,
        outletId = null,
        isActive = true
    ),
    User(
        id = "gudang2",
        name = "Riko Pratama",
        email = "sdb.gudang2@gmail.com",
        username = "gudang2",
        role = Role.WAREHOUSE,
        outletId = null,
        isActive = true
    ),
    User(
        id = "staff",
        name = "Dimas Pratama",
        email = "sdb.staff@gmail.com",
        username = "staff",
        role = Role.OUTLET,
        outletId = "outlet_sudirman",
        isActive = true
    ),
    User(
        id = "staff2",
        name = "Andi Saputra",
        email = "sdb.staff2@gmail.com",
        username = "staff2",
        role = Role.OUTLET,
        outletId = "outlet_sudirman",
        isActive = true
    ),
    User(
        id = "staff_gm",
        name = "Siti Rahma",
        email = "sdb.staff.gm@gmail.com",
        username = "staff_gm",
        role = Role.OUTLET,
        outletId = "outlet_gajah_mada",
        isActive = true
    ),
    User(
        id = "staff_gm2",
        name = "Rudi Hartono",
        email = "sdb.staff.gm2@gmail.com",
        username = "staff_gm2",
        role = Role.OUTLET,
        outletId = "outlet_gajah_mada",
        isActive = true
    ),
    User(
        id = "staff_cikini",
        name = "Fajar Nugroho",
        email = "sdb.staff.cikini@gmail.com",
        username = "staff_cikini",
        role = Role.OUTLET,
        outletId = "outlet_cikini",
        isActive = true
    ),
    User(
        id = "staff_kemang1",
        name = "Bagas Saputra",
        email = "sdb.staff.kemang1@gmail.com",
        username = "staff_kemang1",
        role = Role.OUTLET,
        outletId = "outlet_kemang",
        isActive = true
    ),
    User(
        id = "staff_kemang2",
        name = "Rina Wijaya",
        email = "sdb.staff.kemang2@gmail.com",
        username = "staff_kemang2",
        role = Role.OUTLET,
        outletId = "outlet_kemang",
        isActive = true
    ),
    User(
        id = "staff_blok_m",
        name = "Reza Mahendra",
        email = "sdb.staff.blokm@gmail.com",
        username = "staff_blok_m",
        role = Role.OUTLET,
        outletId = "outlet_blok_m",
        isActive = true
    )
)

fun defaultSeededMaterials(): List<Material> = listOf(
    Material(id = "mat_tahu_original", name = "Tahu Original", cost = 500L, unit = UnitType.PCS),
    Material(id = "mat_tahu_pedas", name = "Tahu Pedas", cost = 600L, unit = UnitType.PCS),
    Material(id = "mat_tahu_keju", name = "Tahu Keju", cost = 700L, unit = UnitType.PCS),
    Material(id = "mat_saus_sambal", name = "Saus Sambal", cost = 3_000L, unit = UnitType.UNIT),
    Material(id = "mat_chilly_oil", name = "Chilly Oil", cost = 15_000L, unit = UnitType.UNIT),
    Material(id = "mat_bumbu_original", name = "Bumbu Original", cost = 12_000L, unit = UnitType.UNIT),
    Material(id = "mat_bumbu_pedas", name = "Bumbu Pedas", cost = 14_000L, unit = UnitType.UNIT),
    Material(id = "mat_bumbu_bbq", name = "Bumbu BBQ", cost = 14_000L, unit = UnitType.UNIT)
)

fun defaultSeededProducts(): List<Product> = listOf(
    Product(id = "prod_tahu_ori_satuan", name = "Tahu Bulat Original (Satuan)", price = 1_000L, isActive = true),
    Product(id = "prod_tahu_ori_bungkus", name = "Tahu Bulat Original (Bungkus 5 pcs)", price = 5_000L, isActive = true),
    Product(id = "prod_tahu_pedas_satuan", name = "Tahu Bulat Pedas (Satuan)", price = 1_200L, isActive = true),
    Product(id = "prod_tahu_pedas_bungkus", name = "Tahu Bulat Pedas (Bungkus 5 pcs)", price = 6_000L, isActive = true),
    Product(id = "prod_tahu_keju_satuan", name = "Tahu Bulat Keju (Satuan)", price = 1_500L, isActive = true),
    Product(id = "prod_tahu_keju_bungkus", name = "Tahu Bulat Keju (Bungkus 5 pcs)", price = 7_000L, isActive = true)
)

private fun defaultNotifications(): List<OwnerNotificationUi> = listOf(
    OwnerNotificationUi(
        id = "notif_1",
        title = "Stok rusak — Outlet Cikini",
        description = "12 pcs Tahu Pedas dilaporkan rusak (kemasan sobek).",
        time = "5 menit lalu",
        isNeedsReview = true,
        targetOutletId = "outlet_cikini"
    ),
    OwnerNotificationUi(
        id = "notif_2",
        title = "Pengeluaran tidak biasa",
        description = "Outlet Gajah Mada mencatat Rp420.000 untuk kategori Lainnya.",
        time = "32 menit lalu",
        isNeedsReview = true,
        targetOutletId = "outlet_gajah_mada"
    ),
    OwnerNotificationUi(
        id = "notif_3",
        title = "Laporan harian diterima",
        description = "Outlet Sudirman mengirim laporan tutup kasir kemarin.",
        time = "Kemarin, 21.40",
        isNeedsReview = false,
        targetOutletId = "outlet_sudirman"
    ),
    OwnerNotificationUi(
        id = "notif_4",
        title = "Distribusi pagi selesai",
        description = "Gudang pusat telah mengirim stok pagi ke Outlet Sudirman.",
        time = "Kemarin, 15.10",
        isNeedsReview = false,
        targetOutletId = "outlet_sudirman"
    )
)
