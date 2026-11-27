package com.sudutcorp.sudutbulat.feature.owner

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sudutcorp.sudutbulat.core.util.buildInitials
import com.sudutcorp.sudutbulat.core.util.sanitizeTimeDigits
import com.sudutcorp.sudutbulat.data.auth.AuthRepository
import com.sudutcorp.sudutbulat.data.model.Role
import com.sudutcorp.sudutbulat.data.repository.DailyReportRepository
import com.sudutcorp.sudutbulat.data.repository.MaterialRepository
import com.sudutcorp.sudutbulat.data.repository.OutletRepository
import com.sudutcorp.sudutbulat.data.repository.ProductRepository
import com.sudutcorp.sudutbulat.data.repository.UserRepository
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

class OwnerViewModel(
    private val outletRepository: OutletRepository,
    private val userRepository: UserRepository,
    private val dailyReportRepository: DailyReportRepository,
    private val materialRepository: MaterialRepository,
    private val productRepository: ProductRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _state = MutableStateFlow(computeOwnerDerivedState(OwnerUiState()))
    val state: StateFlow<OwnerUiState> = _state.asStateFlow()

    private val _effects = Channel<OwnerUiEffect>(Channel.BUFFERED)
    val effects: Flow<OwnerUiEffect> = _effects.receiveAsFlow().shareIn(viewModelScope, SharingStarted.Eagerly)

    private val actions = OwnerActionsHandler(
        outletRepository = outletRepository,
        userRepository = userRepository,
        authRepository = authRepository,
        state = _state,
        effects = _effects
    )

    init {
        observeFirestoreStreams()
    }

    private fun reportStreamError(source: String, error: Throwable) {
        Log.e("OwnerViewModel", "Firestore stream error ($source)", error)
        val detail = error.localizedMessage?.takeIf { it.isNotBlank() } ?: error.javaClass.simpleName
        val message = "Gagal memuat $source: $detail"
        _state.update { it.copy(backendError = message) }
        viewModelScope.launch { _effects.send(OwnerUiEffect.ShowSnackbar(message)) }
    }

    private fun observeFirestoreStreams() {
        authRepository.getCurrentUserEmail()?.takeIf { it.isNotBlank() }?.let { email ->
            _state.update { it.copy(ownerEmail = email) }
        }

        viewModelScope.launch {
            outletRepository.getOutletsFlow(onlyActive = false)
                .catch { e -> reportStreamError("outlets", e) }
                .collect { list ->
                    _state.update { computeOwnerDerivedState(it.copy(outlets = list)) }
                }
        }
        viewModelScope.launch {
            userRepository.getAllUsersFlow()
                .catch { e -> reportStreamError("users", e) }
                .collect { list ->
                    _state.update { current ->
                        val email = authRepository.getCurrentUserEmail()
                        val matchedOwner = list.firstOrNull {
                            (!email.isNullOrBlank() && it.email.equals(email, ignoreCase = true)) || it.role == Role.OWNER
                        }
                        val ownerName = matchedOwner?.name?.replace(" (Owner)", "")?.takeIf { it.isNotBlank() }
                            ?: current.ownerName
                        computeOwnerDerivedState(
                            current.copy(
                                users = list,
                                ownerName = ownerName,
                                ownerInitials = buildInitials(ownerName),
                                ownerEmail = email ?: matchedOwner?.email ?: current.ownerEmail
                            )
                        )
                    }
                }
        }
        viewModelScope.launch {
            dailyReportRepository.getDailyReportsFlow()
                .catch { e -> reportStreamError("daily_reports", e) }
                .collect { list ->
                    _state.update { computeOwnerDerivedState(it.copy(reports = list)) }
                }
        }
        viewModelScope.launch {
            dailyReportRepository.getAllReportProductsFlow()
                .catch { e -> reportStreamError("daily_report_products", e) }
                .collect { list ->
                    _state.update { computeOwnerDerivedState(it.copy(reportProducts = list)) }
                }
        }
        viewModelScope.launch {
            dailyReportRepository.getAllExpensesFlow()
                .catch { e -> reportStreamError("daily_report_expenses", e) }
                .collect { list ->
                    _state.update { computeOwnerDerivedState(it.copy(expenses = list)) }
                }
        }
        viewModelScope.launch {
            dailyReportRepository.getAllDamagedMaterialsFlow()
                .catch { e -> reportStreamError("daily_report_damaged_materials", e) }
                .collect { list ->
                    _state.update { computeOwnerDerivedState(it.copy(damagedMaterials = list)) }
                }
        }
        viewModelScope.launch {
            materialRepository.getMaterialsFlow()
                .catch { e -> reportStreamError("materials", e) }
                .collect { list ->
                    _state.update { computeOwnerDerivedState(it.copy(materials = list)) }
                }
        }
        viewModelScope.launch {
            productRepository.getProductsFlow(onlyActive = false)
                .catch { e -> reportStreamError("products", e) }
                .collect { list ->
                    _state.update { computeOwnerDerivedState(it.copy(products = list)) }
                }
        }
    }

    fun getSingleReportDetail(reportId: String): SingleReportDetailUi? =
        buildSingleReportDetail(_state.value, reportId)

    fun getReportPeriodSummary(period: String = _state.value.selectedReportPeriod): ReportPeriodSummaryUi =
        buildReportPeriodSummary(_state.value, period)

    fun onEvent(event: OwnerUiEvent) {
        when (event) {
            is OwnerUiEvent.OutletSearchChanged -> _state.update { it.copy(outletSearchQuery = event.query) }
            is OwnerUiEvent.OutletFilterSelected -> _state.update { it.copy(outletFilter = event.filter) }
            is OwnerUiEvent.OutletSortChanged -> _state.update {
                computeOwnerDerivedState(it.copy(outletSortField = event.field, outletSortDirection = event.direction))
            }
            is OwnerUiEvent.OutletTabSelected -> _state.update { it.copy(outletTabMode = event.mode) }
            is OwnerUiEvent.ToggleOutletOpenStatus -> viewModelScope.launch { actions.toggleOutletOpenStatus(event.outletId) }
            is OwnerUiEvent.SaveOutletRoutinePreset -> viewModelScope.launch { actions.saveOutletRoutinePreset(event.outletId, event.items) }

            is OwnerUiEvent.StaffSearchChanged -> _state.update { it.copy(staffSearchQuery = event.query) }
            is OwnerUiEvent.StaffRoleFilterSelected -> _state.update { it.copy(staffRoleFilter = event.filter) }
            is OwnerUiEvent.AssignStaffToOutlet -> viewModelScope.launch { actions.assignStaffToOutlet(event.outletId, event.staffIds) }
            is OwnerUiEvent.ReassignSingleStaffOutlet -> viewModelScope.launch { actions.reassignSingleStaffOutlet(event.userId, event.outletId) }
            is OwnerUiEvent.ToggleUserActive -> viewModelScope.launch { actions.toggleUserActive(event.userId) }

            is OwnerUiEvent.ReportTabSelected -> _state.update { it.copy(reportTabMode = event.mode) }
            is OwnerUiEvent.ReportPeriodSelected -> _state.update { it.copy(selectedReportPeriod = event.period) }
            is OwnerUiEvent.ReportSearchChanged -> _state.update { it.copy(reportSearchQuery = event.query) }
            is OwnerUiEvent.ReportOutletFilterSelected -> _state.update { it.copy(reportOutletFilterId = event.outletId) }
            is OwnerUiEvent.ReportDateFilterSelected -> _state.update {
                it.copy(reportDateFilter = event.filter, reportSelectedDateMillis = null)
            }
            is OwnerUiEvent.ReportSpecificDateSelected -> _state.update {
                it.copy(reportSelectedDateMillis = event.dateMillis)
            }

            is OwnerUiEvent.AddOutletNameChanged -> _state.update {
                it.copy(addOutletForm = it.addOutletForm.copy(name = event.name, errorMessage = null))
            }
            is OwnerUiEvent.AddOutletAddressChanged -> _state.update {
                it.copy(addOutletForm = it.addOutletForm.copy(address = event.address, errorMessage = null))
            }
            is OwnerUiEvent.AddOutletOpenTimeChanged -> _state.update {
                it.copy(addOutletForm = it.addOutletForm.copy(openTime = sanitizeTimeDigits(event.openTime), errorMessage = null))
            }
            is OwnerUiEvent.AddOutletCloseTimeChanged -> _state.update {
                it.copy(addOutletForm = it.addOutletForm.copy(closeTime = sanitizeTimeDigits(event.closeTime), errorMessage = null))
            }
            is OwnerUiEvent.AddOutletTargetChanged -> _state.update {
                it.copy(addOutletForm = it.addOutletForm.copy(dailyTargetInput = event.targetInput.filter(Char::isDigit), errorMessage = null))
            }
            is OwnerUiEvent.ToggleAddOutletStaff -> _state.update { current ->
                val set = current.addOutletForm.selectedStaffIds.toMutableSet()
                if (!set.add(event.userId)) set.remove(event.userId)
                current.copy(addOutletForm = current.addOutletForm.copy(selectedStaffIds = set, errorMessage = null))
            }
            OwnerUiEvent.ResetAddOutletForm -> _state.update { it.copy(addOutletForm = AddOutletFormState()) }
            OwnerUiEvent.SubmitNewOutlet -> viewModelScope.launch { actions.submitNewOutlet() }

            is OwnerUiEvent.InitAddUserForm -> _state.update { current ->
                val outletId = event.preselectedOutletId?.takeIf { it.isNotBlank() }
                    ?: current.outletItems.firstOrNull()?.id.orEmpty()
                current.copy(addUserForm = AddUserFormState(selectedRole = Role.OUTLET, selectedOutletId = outletId))
            }
            is OwnerUiEvent.AddUserRoleSelected -> _state.update {
                it.copy(addUserForm = it.addUserForm.copy(selectedRole = event.role, errorMessage = null))
            }
            is OwnerUiEvent.AddUserNameChanged -> _state.update {
                it.copy(addUserForm = it.addUserForm.copy(fullName = event.fullName, errorMessage = null))
            }
            is OwnerUiEvent.AddUserEmailChanged -> _state.update {
                it.copy(addUserForm = it.addUserForm.copy(emailOrPhone = event.emailOrPhone, errorMessage = null))
            }
            is OwnerUiEvent.AddUserPasswordChanged -> _state.update {
                it.copy(addUserForm = it.addUserForm.copy(password = event.password, errorMessage = null))
            }
            OwnerUiEvent.ToggleAddUserPasswordVisibility -> _state.update {
                it.copy(addUserForm = it.addUserForm.copy(isPasswordVisible = !it.addUserForm.isPasswordVisible))
            }
            is OwnerUiEvent.AddUserOutletSelected -> _state.update {
                it.copy(addUserForm = it.addUserForm.copy(selectedOutletId = event.outletId, errorMessage = null))
            }
            OwnerUiEvent.SubmitNewUser -> viewModelScope.launch { actions.submitNewUser() }

            OwnerUiEvent.MarkAllNotificationsRead -> _state.update { current ->
                current.copy(notifications = current.notifications.map { it.copy(isNeedsReview = false) })
            }
            OwnerUiEvent.DismissBackendError -> _state.update { it.copy(backendError = null) }
        }
    }
}
