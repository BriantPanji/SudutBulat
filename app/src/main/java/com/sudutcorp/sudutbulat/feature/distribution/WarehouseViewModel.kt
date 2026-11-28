package com.sudutcorp.sudutbulat.feature.distribution

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Timestamp
import com.sudutcorp.sudutbulat.core.util.buildInitials
import com.sudutcorp.sudutbulat.data.auth.AuthRepository
import com.sudutcorp.sudutbulat.data.model.DeliveryMethod
import com.sudutcorp.sudutbulat.data.model.DistributionStatus
import com.sudutcorp.sudutbulat.data.model.DistributionType
import com.sudutcorp.sudutbulat.data.model.Material
import com.sudutcorp.sudutbulat.data.model.MaterialDistribution
import com.sudutcorp.sudutbulat.data.model.MaterialDistributionItem
import com.sudutcorp.sudutbulat.data.model.Outlet
import com.sudutcorp.sudutbulat.data.model.RoutinePresetItem
import com.sudutcorp.sudutbulat.data.model.User
import com.sudutcorp.sudutbulat.data.repository.MaterialDistributionRepository
import com.sudutcorp.sudutbulat.data.repository.MaterialRepository
import com.sudutcorp.sudutbulat.data.repository.OutletRepository
import com.sudutcorp.sudutbulat.data.repository.UserRepository
import com.sudutcorp.sudutbulat.feature.owner.SortDirection
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
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

data class DistributionItemDetailUi(
    val id: String,
    val materialId: String,
    val materialName: String,
    val unitLabel: String,
    val quantitySent: Int,
    val quantityReceived: Int?,
    val note: String?
)

data class DistributionCardUi(
    val id: String,
    val outletId: String,
    val outletName: String,
    val createdByName: String,
    val confirmedByName: String?,
    val status: DistributionStatus,
    val type: DistributionType,
    val deliveryMethod: DeliveryMethod = DeliveryMethod.WAREHOUSE_COURIER,
    val totalQuantity: Int,
    val itemsSummary: String,
    val items: List<DistributionItemDetailUi>,
    val timeLabel: String,
    val todayTimeLabel: String,
    val sentTimeLabel: String?,
    val arrivedTimeLabel: String? = null,
    val confirmedTimeLabel: String?,
    val createdAtMillis: Long,
    val note: String?,
    val confirmationNote: String?,
    val isToday: Boolean
)

data class CreateDistributionFormState(
    val selectedOutletId: String = "",
    val distributionType: DistributionType = DistributionType.ROUTINE,
    val deliveryMethod: DeliveryMethod = DeliveryMethod.WAREHOUSE_COURIER,
    val itemQuantities: Map<String, String> = emptyMap(),
    val note: String = "",
    val isSaving: Boolean = false,
    val errorMessage: String? = null
)

data class WarehouseUiState(
    val adminName: String = "Admin Gudang",
    val adminInitials: String = "GD",
    val todayDateLabel: String = "",
    val outlets: List<Outlet> = emptyList(),
    val materials: List<Material> = emptyList(),
    val users: List<User> = emptyList(),
    val distributions: List<MaterialDistribution> = emptyList(),
    val activeOutlets: List<Outlet> = emptyList(),
    val activeMaterials: List<Material> = emptyList(),
    val outletsPendingMorning: List<Outlet> = emptyList(),
    val outletsCompletedMorningCount: Int = 0,
    val pendingRequests: List<DistributionCardUi> = emptyList(),
    val todayPendingAdditionalCount: Int = 0,
    val todayIssueCards: List<DistributionCardUi> = emptyList(),
    val todayRecentCards: List<DistributionCardUi> = emptyList(),
    val dashboardMetrics: WarehouseDashboardMetrics = WarehouseDashboardMetrics(),
    val distributionCards: List<DistributionCardUi> = emptyList(),
    val selectedTab: WarehouseDistributionTab = WarehouseDistributionTab.ROUTINE,
    val routineFilter: DistributionTabFilterState = DistributionTabFilterState(),
    val additionalFilter: DistributionTabFilterState = DistributionTabFilterState(),
    val historyFilter: DistributionTabFilterState = DistributionTabFilterState(),
    val createForm: CreateDistributionFormState = CreateDistributionFormState(),
    val isActionLoading: Boolean = false,
    val backendError: String? = null
) {
    fun filterFor(tab: WarehouseDistributionTab): DistributionTabFilterState = when (tab) {
        WarehouseDistributionTab.ROUTINE -> routineFilter
        WarehouseDistributionTab.ADDITIONAL -> additionalFilter
        WarehouseDistributionTab.HISTORY -> historyFilter
    }
}

sealed interface WarehouseUiEvent {
    data class SelectDistributionTab(val tab: WarehouseDistributionTab) : WarehouseUiEvent
    data class SelectAdditionalSubTab(val subTab: AdditionalDistributionSubTab) : WarehouseUiEvent
    data class UpdateSearchQuery(val tab: WarehouseDistributionTab, val query: String) : WarehouseUiEvent
    data class SelectStatusFilter(val tab: WarehouseDistributionTab, val statusFilter: DistributionStatusFilter) : WarehouseUiEvent
    data class SelectOutletFilter(val tab: WarehouseDistributionTab, val outletId: String?) : WarehouseUiEvent
    data class UpdateSort(
        val tab: WarehouseDistributionTab,
        val field: DistributionSortField,
        val direction: SortDirection
    ) : WarehouseUiEvent
    data class SelectHistoryDate(val dateMillis: Long?) : WarehouseUiEvent
    data class ResetTabFilters(val tab: WarehouseDistributionTab) : WarehouseUiEvent

    data class InitCreateForm(
        val preselectedOutletId: String? = null,
        val defaultType: DistributionType = DistributionType.ROUTINE
    ) : WarehouseUiEvent
    data class SelectOutlet(val outletId: String) : WarehouseUiEvent
    data object ApplyOutletPresetToForm : WarehouseUiEvent
    data class SaveOutletRoutinePreset(
        val outletId: String,
        val items: List<RoutinePresetItem>
    ) : WarehouseUiEvent
    data class UpdateItemQuantity(val materialId: String, val qtyInput: String) : WarehouseUiEvent
    data class SelectDeliveryMethod(val method: DeliveryMethod) : WarehouseUiEvent
    data class UpdateNote(val note: String) : WarehouseUiEvent
    data object SubmitCreateDistribution : WarehouseUiEvent
    data class ApproveRequest(
        val distributionId: String,
        val deliveryMethod: DeliveryMethod = DeliveryMethod.WAREHOUSE_COURIER
    ) : WarehouseUiEvent
    data class MarkDistributionArrived(val distributionId: String) : WarehouseUiEvent
    data class RejectRequest(val distributionId: String, val reason: String) : WarehouseUiEvent
}

sealed interface WarehouseUiEffect {
    data class ShowSnackbar(val message: String) : WarehouseUiEffect
    data object DistributionSaved : WarehouseUiEffect
    data object RoutinePresetSaved : WarehouseUiEffect
}

class WarehouseViewModel(
    private val outletRepository: OutletRepository,
    private val materialRepository: MaterialRepository,
    private val distributionRepository: MaterialDistributionRepository,
    private val userRepository: UserRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _state = MutableStateFlow(WarehouseUiState())
    val state: StateFlow<WarehouseUiState> = _state.asStateFlow()

    private val _effects = Channel<WarehouseUiEffect>(Channel.BUFFERED)
    val effects: Flow<WarehouseUiEffect> = _effects.receiveAsFlow().shareIn(viewModelScope, SharingStarted.Eagerly)

    private val reconcilingDraftIds = mutableSetOf<String>()

    init {
        observeStreams()
    }

    private fun reportError(source: String, error: Throwable) {
        Log.e("WarehouseViewModel", "Backend error ($source)", error)
        val detail = error.localizedMessage?.takeIf { it.isNotBlank() } ?: error.javaClass.simpleName
        val message = "Gagal $source: $detail"
        _state.update { it.copy(backendError = message, isActionLoading = false) }
        viewModelScope.launch { _effects.send(WarehouseUiEffect.ShowSnackbar(message)) }
    }

    private fun observeStreams() {
        viewModelScope.launch {
            outletRepository.getOutletsFlow(onlyActive = false)
                .catch { e -> reportError("memuat outlets", e) }
                .collect { list -> _state.update { recompute(it.copy(outlets = list)) } }
        }
        viewModelScope.launch {
            materialRepository.getMaterialsFlow()
                .catch { e -> reportError("memuat materials", e) }
                .collect { list -> _state.update { recompute(it.copy(materials = list)) } }
        }
        viewModelScope.launch {
            userRepository.getAllUsersFlow()
                .catch { e -> reportError("memuat users", e) }
                .collect { list -> _state.update { recompute(it.copy(users = list)) } }
        }
        viewModelScope.launch {
            distributionRepository.getDistributionsFlow(outletId = null)
                .catch { e -> reportError("memuat distribusi", e) }
                .collect { list ->
                    _state.update { recompute(it.copy(distributions = list)) }
                    reconcileExpiredDrafts(list)
                }
        }
    }

    private fun startOfTodaySeconds(): Long = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }.timeInMillis / 1000L

    private fun reconcileExpiredDrafts(distributions: List<MaterialDistribution>) {
        val actions = partitionExpiredDraftDistributions(distributions, startOfTodaySeconds())
        actions.routineToDelete.forEach { dist ->
            if (dist.id.isBlank() || !reconcilingDraftIds.add(dist.id)) return@forEach
            viewModelScope.launch {
                distributionRepository.deleteDistribution(dist.id)
                    .onFailure { e ->
                        reconcilingDraftIds.remove(dist.id)
                        Log.e("WarehouseViewModel", "Gagal menghapus draft rutin kadaluarsa (${dist.id})", e)
                    }
            }
        }
        actions.additionalToReject.forEach { dist ->
            if (dist.id.isBlank() || !reconcilingDraftIds.add(dist.id)) return@forEach
            viewModelScope.launch {
                distributionRepository.updateDistributionStatus(
                    id = dist.id,
                    status = DistributionStatus.CANCELLED,
                    confirmedBy = "system",
                    confirmationNote = "Ditolak otomatis karena melewati batas tanggal permintaan",
                    confirmedAt = Timestamp.now()
                ).onFailure { e ->
                    reconcilingDraftIds.remove(dist.id)
                    Log.e("WarehouseViewModel", "Gagal auto-reject permintaan kadaluarsa (${dist.id})", e)
                }
            }
        }
    }

    private fun presetMapFor(state: WarehouseUiState, outletId: String): Map<String, String> {
        val outlet = state.outlets.firstOrNull { it.id == outletId }
        val activeMatIds = state.activeMaterials.map { it.id }.toSet()
        return presetQuantitiesForOutlet(outlet, activeMatIds)
    }

    private fun updateTabFilter(
        tab: WarehouseDistributionTab,
        transform: (DistributionTabFilterState) -> DistributionTabFilterState
    ) {
        _state.update { current ->
            when (tab) {
                WarehouseDistributionTab.ROUTINE -> current.copy(routineFilter = transform(current.routineFilter))
                WarehouseDistributionTab.ADDITIONAL -> current.copy(additionalFilter = transform(current.additionalFilter))
                WarehouseDistributionTab.HISTORY -> current.copy(historyFilter = transform(current.historyFilter))
            }
        }
    }

    fun onEvent(event: WarehouseUiEvent) {
        when (event) {
            is WarehouseUiEvent.SelectDistributionTab -> _state.update {
                it.copy(selectedTab = event.tab)
            }
            is WarehouseUiEvent.SelectAdditionalSubTab -> updateTabFilter(WarehouseDistributionTab.ADDITIONAL) {
                it.copy(
                    additionalSubTab = event.subTab,
                    statusFilter = DistributionStatusFilter.ALL
                )
            }
            is WarehouseUiEvent.UpdateSearchQuery -> updateTabFilter(event.tab) {
                it.copy(searchQuery = event.query)
            }
            is WarehouseUiEvent.SelectStatusFilter -> updateTabFilter(event.tab) {
                it.copy(statusFilter = event.statusFilter)
            }
            is WarehouseUiEvent.SelectOutletFilter -> updateTabFilter(event.tab) {
                it.copy(selectedOutletId = event.outletId)
            }
            is WarehouseUiEvent.UpdateSort -> updateTabFilter(event.tab) {
                it.copy(sortField = event.field, sortDirection = event.direction)
            }
            is WarehouseUiEvent.SelectHistoryDate -> updateTabFilter(WarehouseDistributionTab.HISTORY) {
                it.copy(selectedDateMillis = event.dateMillis)
            }
            is WarehouseUiEvent.ResetTabFilters -> updateTabFilter(event.tab) {
                DistributionTabFilterState(
                    sortField = it.sortField,
                    sortDirection = it.sortDirection,
                    additionalSubTab = it.additionalSubTab
                )
            }

            is WarehouseUiEvent.InitCreateForm -> _state.update { current ->
                val outletId = event.preselectedOutletId?.takeIf { it.isNotBlank() }
                    ?: if (event.defaultType == DistributionType.ROUTINE) {
                        current.outletsPendingMorning.firstOrNull()?.id
                    } else {
                        null
                    }
                    ?: current.activeOutlets.firstOrNull()?.id.orEmpty()
                val initialQuantities = if (event.defaultType == DistributionType.ROUTINE) {
                    presetMapFor(current, outletId)
                } else {
                    emptyMap()
                }
                current.copy(
                    createForm = CreateDistributionFormState(
                        selectedOutletId = outletId,
                        distributionType = event.defaultType,
                        itemQuantities = initialQuantities
                    )
                )
            }
            is WarehouseUiEvent.SelectOutlet -> _state.update { current ->
                val nextQuantities = if (current.createForm.distributionType == DistributionType.ROUTINE) {
                    presetMapFor(current, event.outletId)
                } else {
                    current.createForm.itemQuantities
                }
                current.copy(
                    createForm = current.createForm.copy(
                        selectedOutletId = event.outletId,
                        itemQuantities = nextQuantities,
                        errorMessage = null
                    )
                )
            }
            WarehouseUiEvent.ApplyOutletPresetToForm -> _state.update { current ->
                current.copy(
                    createForm = current.createForm.copy(
                        itemQuantities = presetMapFor(current, current.createForm.selectedOutletId),
                        errorMessage = null
                    )
                )
            }
            is WarehouseUiEvent.SaveOutletRoutinePreset -> saveOutletRoutinePreset(event.outletId, event.items)
            is WarehouseUiEvent.UpdateItemQuantity -> _state.update { current ->
                val digits = event.qtyInput.filter(Char::isDigit)
                val updated = current.createForm.itemQuantities.toMutableMap()
                if (digits.isBlank()) updated.remove(event.materialId) else updated[event.materialId] = digits
                current.copy(createForm = current.createForm.copy(itemQuantities = updated, errorMessage = null))
            }
            is WarehouseUiEvent.SelectDeliveryMethod -> _state.update {
                it.copy(createForm = it.createForm.copy(deliveryMethod = event.method, errorMessage = null))
            }
            is WarehouseUiEvent.UpdateNote -> _state.update {
                it.copy(createForm = it.createForm.copy(note = event.note, errorMessage = null))
            }
            WarehouseUiEvent.SubmitCreateDistribution -> submitCreateDistribution()
            is WarehouseUiEvent.ApproveRequest -> approveRequest(event.distributionId, event.deliveryMethod)
            is WarehouseUiEvent.MarkDistributionArrived -> markDistributionArrived(event.distributionId)
            is WarehouseUiEvent.RejectRequest -> rejectRequest(event.distributionId, event.reason)
        }
    }

    private fun resolveCurrentUserId(): String {
        val email = authRepository.getCurrentUserEmail()
        val matched = _state.value.users.firstOrNull {
            !email.isNullOrBlank() && it.email.equals(email, ignoreCase = true)
        }
        return matched?.id ?: authRepository.getCurrentUserUid() ?: "gudang"
    }

    private fun submitCreateDistribution() {
        val form = _state.value.createForm
        val items = form.itemQuantities.mapNotNull { (matId, qtyStr) ->
            val qty = qtyStr.toIntOrNull() ?: 0
            if (qty > 0) MaterialDistributionItem(materialId = matId, quantitySent = qty) else null
        }

        val error = when {
            form.selectedOutletId.isBlank() -> "Pilih outlet tujuan distribusi."
            items.isEmpty() -> "Masukkan jumlah minimal 1 bahan mentah (> 0)."
            else -> null
        }
        if (error != null) {
            _state.update { it.copy(createForm = form.copy(errorMessage = error)) }
            return
        }

        viewModelScope.launch {
            _state.update { it.copy(createForm = form.copy(isSaving = true, errorMessage = null)) }
            val now = Timestamp.now()
            val defaultNote = if (form.distributionType == DistributionType.ROUTINE) {
                "Distribusi rutin dari gudang"
            } else {
                "Distribusi tambahan stok dari gudang"
            }
            val distribution = MaterialDistribution(
                outletId = form.selectedOutletId,
                createdBy = resolveCurrentUserId(),
                status = DistributionStatus.SENT,
                type = form.distributionType,
                deliveryMethod = form.deliveryMethod,
                note = form.note.trim().ifBlank { defaultNote },
                createdAt = now,
                sentAt = now
            )

            distributionRepository.createDistribution(distribution, items)
                .onSuccess {
                    _state.update { it.copy(createForm = CreateDistributionFormState()) }
                    _effects.send(WarehouseUiEffect.ShowSnackbar("Distribusi ${form.distributionType.label.lowercase()} berhasil dikirim ke outlet."))
                    _effects.send(WarehouseUiEffect.DistributionSaved)
                }
                .onFailure { e ->
                    val detail = e.localizedMessage?.takeIf { it.isNotBlank() } ?: e.javaClass.simpleName
                    val msg = "Gagal menyimpan distribusi: $detail"
                    _state.update { it.copy(createForm = form.copy(isSaving = false, errorMessage = msg)) }
                    _effects.send(WarehouseUiEffect.ShowSnackbar(msg))
                }
        }
    }

    private fun saveOutletRoutinePreset(outletId: String, items: List<RoutinePresetItem>) {
        viewModelScope.launch {
            _state.update { it.copy(isActionLoading = true) }
            outletRepository.updateRoutinePreset(outletId, items)
                .onSuccess {
                    _state.update { it.copy(isActionLoading = false) }
                    _effects.send(WarehouseUiEffect.ShowSnackbar("Preset distribusi rutin berhasil disimpan."))
                    _effects.send(WarehouseUiEffect.RoutinePresetSaved)
                }
                .onFailure { e ->
                    reportError("menyimpan preset distribusi rutin", e)
                }
        }
    }

    private fun approveRequest(
        distributionId: String,
        deliveryMethod: DeliveryMethod = DeliveryMethod.WAREHOUSE_COURIER
    ) {
        viewModelScope.launch {
            _state.update { it.copy(isActionLoading = true) }
            distributionRepository.updateDistributionStatus(
                id = distributionId,
                status = DistributionStatus.SENT,
                sentAt = Timestamp.now(),
                deliveryMethod = deliveryMethod
            ).onSuccess {
                _state.update { it.copy(isActionLoading = false) }
                _effects.send(WarehouseUiEffect.ShowSnackbar("Permintaan stok disetujui & dikirim (${deliveryMethod.label})."))
            }.onFailure { e ->
                reportError("menyetujui permintaan stok", e)
            }
        }
    }

    private fun markDistributionArrived(distributionId: String) {
        val card = _state.value.distributionCards.firstOrNull { it.id == distributionId }
        if (card != null && card.status != DistributionStatus.SENT) return

        viewModelScope.launch {
            _state.update { it.copy(isActionLoading = true) }
            distributionRepository.updateDistributionStatus(
                id = distributionId,
                status = DistributionStatus.ARRIVED,
                arrivedAt = Timestamp.now()
            ).onSuccess {
                _state.update { it.copy(isActionLoading = false) }
                _effects.send(
                    WarehouseUiEffect.ShowSnackbar("Status diubah ke Tiba di Outlet. Pengingat dikirim ke staf outlet.")
                )
            }.onFailure { e ->
                reportError("memperbarui status tiba di outlet", e)
            }
        }
    }

    private fun rejectRequest(distributionId: String, reason: String) {
        viewModelScope.launch {
            _state.update { it.copy(isActionLoading = true) }
            distributionRepository.updateDistributionStatus(
                id = distributionId,
                status = DistributionStatus.CANCELLED,
                confirmedBy = resolveCurrentUserId(),
                confirmationNote = reason.trim().ifBlank { "Ditolak oleh Admin Gudang" },
                confirmedAt = Timestamp.now()
            ).onSuccess {
                _state.update { it.copy(isActionLoading = false) }
                _effects.send(WarehouseUiEffect.ShowSnackbar("Permintaan stok dibatalkan."))
            }.onFailure { e ->
                reportError("membatalkan permintaan stok", e)
            }
        }
    }

    private fun recompute(raw: WarehouseUiState): WarehouseUiState {
        val activeOutlets = raw.outlets.filter { it.isActive }
        val activeMaterials = raw.materials.filter { it.isActive }
        val outletMap = raw.outlets.associateBy { it.id }
        val materialMap = raw.materials.associateBy { it.id }
        val userMap = raw.users.associateBy { it.id }

        val startOfTodaySec = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis / 1000L

        val idLocale = Locale.forLanguageTag("id-ID")
        val dateTimeFmt = SimpleDateFormat("dd MMM, HH.mm", idLocale)
        val clockFmt = SimpleDateFormat("HH.mm", idLocale)
        val todayDateLabel = SimpleDateFormat("EEEE, dd MMM yyyy", idLocale).format(Date())

        val currentEmail = authRepository.getCurrentUserEmail()
        val matchedUser = raw.users.firstOrNull {
            !currentEmail.isNullOrBlank() && it.email.equals(currentEmail, ignoreCase = true)
        }
        val adminName = matchedUser?.name?.takeIf { it.isNotBlank() } ?: "Admin Gudang"
        val adminInitials = matchedUser?.name?.takeIf { it.isNotBlank() }?.let(::buildInitials) ?: "GD"

        val cards = raw.distributions.map { dist ->
            val detailItems = dist.items.map { item ->
                val mat = materialMap[item.materialId]
                DistributionItemDetailUi(
                    id = item.id,
                    materialId = item.materialId,
                    materialName = mat?.name ?: item.materialId,
                    unitLabel = mat?.unit?.firestoreValue ?: "pcs",
                    quantitySent = item.quantitySent,
                    quantityReceived = item.quantityReceived,
                    note = item.note
                )
            }
            val totalQty = detailItems.sumOf { it.quantitySent }
            val summary = detailItems.take(2).joinToString(", ") { item ->
                "${item.materialName} (${item.quantitySent} ${item.unitLabel})"
            }.let { base ->
                if (detailItems.size > 2) "$base +${detailItems.size - 2} lainnya" else base
            }.ifBlank { "Belum ada rincian item" }

            val outlet = outletMap[dist.outletId]
            val outletName = outlet?.let {
                if (it.name.startsWith("Outlet", ignoreCase = true)) it.name else "Outlet ${it.name}"
            } ?: dist.outletId

            val createdDate = dist.createdAt.toDate()

            DistributionCardUi(
                id = dist.id,
                outletId = dist.outletId,
                outletName = outletName,
                createdByName = userMap[dist.createdBy]?.name ?: dist.createdBy,
                confirmedByName = dist.confirmedBy?.let { userMap[it]?.name ?: it },
                status = dist.status,
                type = dist.type,
                deliveryMethod = dist.deliveryMethod,
                totalQuantity = totalQty,
                itemsSummary = summary,
                items = detailItems,
                timeLabel = dateTimeFmt.format(createdDate),
                todayTimeLabel = clockFmt.format(createdDate),
                sentTimeLabel = dist.sentAt?.toDate()?.let { dateTimeFmt.format(it) },
                arrivedTimeLabel = dist.arrivedAt?.toDate()?.let { dateTimeFmt.format(it) },
                confirmedTimeLabel = dist.confirmedAt?.toDate()?.let { dateTimeFmt.format(it) },
                createdAtMillis = createdDate.time,
                note = dist.note,
                confirmationNote = dist.confirmationNote,
                isToday = dist.createdAt.seconds >= startOfTodaySec
            )
        }

        // Only today's active ROUTINE distributions count toward daily routine completion.
        val outletsWithTodayRoutine = raw.distributions
            .filter {
                it.type == DistributionType.ROUTINE &&
                    it.createdAt.seconds >= startOfTodaySec &&
                    it.status != DistributionStatus.DRAFT &&
                    it.status != DistributionStatus.CANCELLED
            }
            .map { it.outletId }
            .toSet()

        val pendingMorning = activeOutlets.filter { it.id !in outletsWithTodayRoutine }
        val completedMorningCount = activeOutlets.size - pendingMorning.size
        val pendingRequests = cards.filter { it.isToday && it.status == DistributionStatus.DRAFT }
        val todayPendingAdditionalCount = cards.count {
            it.isToday && it.type == DistributionType.ADDITIONAL && it.status == DistributionStatus.DRAFT
        }
        val todayIssueCards = cards
            .filter {
                it.isToday && (
                    it.status == DistributionStatus.PARTIALLY_RECEIVED ||
                        it.status == DistributionStatus.NOT_RECEIVED
                    )
            }
            .sortedByDescending { it.createdAtMillis }
        val todayRecentCards = cards
            .filter { it.isToday && it.status != DistributionStatus.DRAFT }
            .sortedByDescending { it.createdAtMillis }
            .take(4)
        val activeMaterialIds = activeMaterials.map { it.id }.toSet()
        val dashboardMetrics = computeWarehouseDashboardMetrics(
            activeOutlets = activeOutlets,
            activeMaterialIds = activeMaterialIds,
            cards = cards,
            completedMorningCount = completedMorningCount
        )

        return raw.copy(
            adminName = adminName,
            adminInitials = adminInitials,
            todayDateLabel = todayDateLabel,
            activeOutlets = activeOutlets,
            activeMaterials = activeMaterials,
            outletsPendingMorning = pendingMorning,
            outletsCompletedMorningCount = completedMorningCount,
            pendingRequests = pendingRequests,
            todayPendingAdditionalCount = todayPendingAdditionalCount,
            todayIssueCards = todayIssueCards,
            todayRecentCards = todayRecentCards,
            dashboardMetrics = dashboardMetrics,
            distributionCards = cards
        )
    }
}
