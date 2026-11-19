package com.sudutcorp.sudutbulat.feature.distribution

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Timestamp
import com.sudutcorp.sudutbulat.data.auth.AuthRepository
import com.sudutcorp.sudutbulat.data.model.DistributionStatus
import com.sudutcorp.sudutbulat.data.model.Material
import com.sudutcorp.sudutbulat.data.model.MaterialDistribution
import com.sudutcorp.sudutbulat.data.model.MaterialDistributionItem
import com.sudutcorp.sudutbulat.data.model.Outlet
import com.sudutcorp.sudutbulat.data.model.User
import com.sudutcorp.sudutbulat.data.repository.MaterialDistributionRepository
import com.sudutcorp.sudutbulat.data.repository.MaterialRepository
import com.sudutcorp.sudutbulat.data.repository.OutletRepository
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
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

data class DistributionCardUi(
    val id: String,
    val outletId: String,
    val outletName: String,
    val createdByName: String,
    val status: DistributionStatus,
    val totalQuantity: Int,
    val itemsSummary: String,
    val timeLabel: String,
    val note: String?,
    val confirmationNote: String?,
    val isToday: Boolean
)

data class CreateDistributionFormState(
    val selectedOutletId: String = "",
    val itemQuantities: Map<String, String> = emptyMap(),
    val note: String = "",
    val isSaving: Boolean = false,
    val errorMessage: String? = null
)

data class WarehouseUiState(
    val outlets: List<Outlet> = emptyList(),
    val materials: List<Material> = emptyList(),
    val users: List<User> = emptyList(),
    val distributions: List<MaterialDistribution> = emptyList(),
    val distributionItems: List<MaterialDistributionItem> = emptyList(),
    val activeOutlets: List<Outlet> = emptyList(),
    val activeMaterials: List<Material> = emptyList(),
    val outletsPendingMorning: List<Outlet> = emptyList(),
    val outletsCompletedMorningCount: Int = 0,
    val pendingRequests: List<DistributionCardUi> = emptyList(),
    val distributionCards: List<DistributionCardUi> = emptyList(),
    val createForm: CreateDistributionFormState = CreateDistributionFormState(),
    val isActionLoading: Boolean = false,
    val backendError: String? = null
)

sealed interface WarehouseUiEvent {
    data class InitCreateForm(val preselectedOutletId: String? = null) : WarehouseUiEvent
    data class SelectOutlet(val outletId: String) : WarehouseUiEvent
    data class UpdateItemQuantity(val materialId: String, val qtyInput: String) : WarehouseUiEvent
    data class UpdateNote(val note: String) : WarehouseUiEvent
    data object SubmitCreateDistribution : WarehouseUiEvent
    data class ApproveRequest(val distributionId: String) : WarehouseUiEvent
    data class RejectRequest(val distributionId: String, val reason: String) : WarehouseUiEvent
}

sealed interface WarehouseUiEffect {
    data class ShowSnackbar(val message: String) : WarehouseUiEffect
    data object DistributionSaved : WarehouseUiEffect
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
                .collect { list -> _state.update { recompute(it.copy(distributions = list)) } }
        }
        viewModelScope.launch {
            distributionRepository.getAllDistributionItemsFlow()
                .catch { e -> reportError("memuat item distribusi", e) }
                .collect { list -> _state.update { recompute(it.copy(distributionItems = list)) } }
        }
    }

    fun onEvent(event: WarehouseUiEvent) {
        when (event) {
            is WarehouseUiEvent.InitCreateForm -> _state.update { current ->
                val outletId = event.preselectedOutletId?.takeIf { it.isNotBlank() }
                    ?: current.outletsPendingMorning.firstOrNull()?.id
                    ?: current.activeOutlets.firstOrNull()?.id.orEmpty()
                current.copy(createForm = CreateDistributionFormState(selectedOutletId = outletId))
            }
            is WarehouseUiEvent.SelectOutlet -> _state.update {
                it.copy(createForm = it.createForm.copy(selectedOutletId = event.outletId, errorMessage = null))
            }
            is WarehouseUiEvent.UpdateItemQuantity -> _state.update { current ->
                val digits = event.qtyInput.filter(Char::isDigit)
                val updated = current.createForm.itemQuantities.toMutableMap()
                if (digits.isBlank()) updated.remove(event.materialId) else updated[event.materialId] = digits
                current.copy(createForm = current.createForm.copy(itemQuantities = updated, errorMessage = null))
            }
            is WarehouseUiEvent.UpdateNote -> _state.update {
                it.copy(createForm = it.createForm.copy(note = event.note, errorMessage = null))
            }
            WarehouseUiEvent.SubmitCreateDistribution -> submitCreateDistribution()
            is WarehouseUiEvent.ApproveRequest -> approveRequest(event.distributionId)
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
            val distribution = MaterialDistribution(
                outletId = form.selectedOutletId,
                createdBy = resolveCurrentUserId(),
                status = DistributionStatus.SENT,
                note = form.note.trim().ifBlank { "Distribusi stok dari gudang" },
                createdAt = now,
                sentAt = now
            )

            distributionRepository.createDistribution(distribution, items)
                .onSuccess {
                    _state.update { it.copy(createForm = CreateDistributionFormState()) }
                    _effects.send(WarehouseUiEffect.ShowSnackbar("Distribusi berhasil dikirim ke outlet."))
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

    private fun approveRequest(distributionId: String) {
        viewModelScope.launch {
            _state.update { it.copy(isActionLoading = true) }
            distributionRepository.updateDistributionStatus(
                id = distributionId,
                status = DistributionStatus.SENT,
                sentAt = Timestamp.now()
            ).onSuccess {
                _state.update { it.copy(isActionLoading = false) }
                _effects.send(WarehouseUiEffect.ShowSnackbar("Permintaan stok disetujui dan dikirim."))
            }.onFailure { e ->
                reportError("menyetujui permintaan stok", e)
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
                _effects.send(WarehouseUiEffect.ShowSnackbar("Permintaan stok ditolak."))
            }.onFailure { e ->
                reportError("menolak permintaan stok", e)
            }
        }
    }

    private fun recompute(raw: WarehouseUiState): WarehouseUiState {
        val activeOutlets = raw.outlets.filter { it.isActive }
        val activeMaterials = raw.materials.filter { it.isActive }
        val outletMap = raw.outlets.associateBy { it.id }
        val materialMap = raw.materials.associateBy { it.id }
        val userMap = raw.users.associateBy { it.id }
        val itemsByDist = raw.distributionItems.groupBy { it.distributionId }

        val startOfTodaySec = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis / 1000L

        val timeFmt = SimpleDateFormat("dd MMM, HH.mm", Locale.forLanguageTag("id-ID"))

        val cards = raw.distributions.map { dist ->
            val items = itemsByDist[dist.id].orEmpty()
            val totalQty = items.sumOf { it.quantitySent }
            val summary = items.take(2).joinToString(", ") { item ->
                val matName = materialMap[item.materialId]?.name ?: item.materialId
                "$matName (${item.quantitySent})"
            }.let { base ->
                if (items.size > 2) "$base +${items.size - 2} lainnya" else base
            }.ifBlank { "Belum ada rincian item" }

            val outlet = outletMap[dist.outletId]
            val outletName = outlet?.let {
                if (it.name.startsWith("Outlet", ignoreCase = true)) it.name else "Outlet ${it.name}"
            } ?: dist.outletId

            DistributionCardUi(
                id = dist.id,
                outletId = dist.outletId,
                outletName = outletName,
                createdByName = userMap[dist.createdBy]?.name ?: dist.createdBy,
                status = dist.status,
                totalQuantity = totalQty,
                itemsSummary = summary,
                timeLabel = timeFmt.format(Date(dist.createdAt.toDate().time)),
                note = dist.note,
                confirmationNote = dist.confirmationNote,
                isToday = dist.createdAt.seconds >= startOfTodaySec
            )
        }

        val outletsWithTodayDelivery = raw.distributions
            .filter {
                it.createdAt.seconds >= startOfTodaySec &&
                    it.status != DistributionStatus.DRAFT &&
                    it.status != DistributionStatus.CANCELLED
            }
            .map { it.outletId }
            .toSet()

        val pendingMorning = activeOutlets.filter { it.id !in outletsWithTodayDelivery }
        val pendingRequests = cards.filter { it.status == DistributionStatus.DRAFT }

        return raw.copy(
            activeOutlets = activeOutlets,
            activeMaterials = activeMaterials,
            outletsPendingMorning = pendingMorning,
            outletsCompletedMorningCount = activeOutlets.size - pendingMorning.size,
            pendingRequests = pendingRequests,
            distributionCards = cards
        )
    }
}
