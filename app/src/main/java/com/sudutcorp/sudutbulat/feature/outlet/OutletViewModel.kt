package com.sudutcorp.sudutbulat.feature.outlet

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Timestamp
import com.sudutcorp.sudutbulat.data.auth.AuthRepository
import com.sudutcorp.sudutbulat.data.model.DailyReport
import com.sudutcorp.sudutbulat.data.model.DailyReportDamagedMaterial
import com.sudutcorp.sudutbulat.data.model.DailyReportExpense
import com.sudutcorp.sudutbulat.data.model.DailyReportProduct
import com.sudutcorp.sudutbulat.data.model.DistributionStatus
import com.sudutcorp.sudutbulat.data.model.DistributionType
import com.sudutcorp.sudutbulat.data.model.ExpenseCategory
import com.sudutcorp.sudutbulat.data.model.Material
import com.sudutcorp.sudutbulat.data.model.MaterialDistribution
import com.sudutcorp.sudutbulat.data.model.MaterialDistributionItem
import com.sudutcorp.sudutbulat.data.model.Outlet
import com.sudutcorp.sudutbulat.data.model.Product
import com.sudutcorp.sudutbulat.data.model.User
import com.sudutcorp.sudutbulat.data.repository.DailyReportRepository
import com.sudutcorp.sudutbulat.data.repository.MaterialDistributionRepository
import com.sudutcorp.sudutbulat.data.repository.MaterialRepository
import com.sudutcorp.sudutbulat.data.repository.OutletRepository
import com.sudutcorp.sudutbulat.data.repository.ProductRepository
import com.sudutcorp.sudutbulat.data.repository.UserRepository
import com.sudutcorp.sudutbulat.feature.distribution.findExpiredPendingDistributions
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

internal fun isSameCalendarDay(a: Timestamp, b: Timestamp): Boolean {
    val calA = Calendar.getInstance().apply { time = a.toDate() }
    val calB = Calendar.getInstance().apply { time = b.toDate() }
    return calA.get(Calendar.YEAR) == calB.get(Calendar.YEAR) &&
        calA.get(Calendar.DAY_OF_YEAR) == calB.get(Calendar.DAY_OF_YEAR)
}

internal fun calendarDayBounds(reference: Timestamp): Pair<Timestamp, Timestamp> {
    val start = Calendar.getInstance().apply {
        time = reference.toDate()
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }
    val end = Calendar.getInstance().apply {
        time = reference.toDate()
        set(Calendar.HOUR_OF_DAY, 23)
        set(Calendar.MINUTE, 59)
        set(Calendar.SECOND, 59)
        set(Calendar.MILLISECOND, 999)
    }
    return Timestamp(start.time) to Timestamp(end.time)
}

data class OutletDistributionItemUi(
    val id: String,
    val materialId: String,
    val materialName: String,
    val unitLabel: String,
    val quantitySent: Int,
    val quantityReceived: Int?
)

data class OutletDistributionUi(
    val id: String,
    val status: DistributionStatus,
    val timeLabel: String,
    val note: String?,
    val confirmationNote: String?,
    val items: List<OutletDistributionItemUi>,
    val totalSent: Int
)

data class DraftExpenseEntry(
    val id: String,
    val category: ExpenseCategory,
    val amount: Long,
    val note: String
)

data class DraftDamagedEntry(
    val id: String,
    val materialId: String,
    val materialName: String,
    val quantity: Int,
    val note: String
)

data class StockRequestFormState(
    val itemQuantities: Map<String, String> = emptyMap(),
    val note: String = "",
    val isSaving: Boolean = false,
    val errorMessage: String? = null
)

data class DailyReportFormState(
    val editingReportId: String? = null,
    val editingReportDate: Timestamp? = null,
    val pendingDuplicateReport: DailyReport? = null,
    val isLoadingExisting: Boolean = false,
    val productQuantities: Map<String, String> = emptyMap(),
    val cashRevenueInput: String = "",
    val qrisRevenueInput: String = "",
    val expenses: List<DraftExpenseEntry> = emptyList(),
    val damagedMaterials: List<DraftDamagedEntry> = emptyList(),
    val isSaving: Boolean = false,
    val errorMessage: String? = null
)

data class OutletUiState(
    val staffName: String = "Staf Outlet",
    val assignedOutlet: Outlet? = null,
    val activeMaterials: List<Material> = emptyList(),
    val activeProducts: List<Product> = emptyList(),
    val distributions: List<OutletDistributionUi> = emptyList(),
    val recentReports: List<DailyReport> = emptyList(),
    val todayReport: DailyReport? = null,
    val requestForm: StockRequestFormState = StockRequestFormState(),
    val reportForm: DailyReportFormState = DailyReportFormState(),
    val isActionLoading: Boolean = false,
    val backendError: String? = null,
    // raw caches
    val rawOutlets: List<Outlet> = emptyList(),
    val rawUsers: List<User> = emptyList(),
    val rawMaterials: List<Material> = emptyList(),
    val rawProducts: List<Product> = emptyList(),
    val rawDistributions: List<MaterialDistribution> = emptyList(),
    val rawReports: List<DailyReport> = emptyList()
)

sealed interface OutletUiEvent {
    data object ToggleAssignedOutletOpen : OutletUiEvent
    data class ConfirmDistribution(
        val distributionId: String,
        val receivedByItemId: Map<String, Int>,
        val note: String
    ) : OutletUiEvent

    data object ResetStockRequestForm : OutletUiEvent
    data class UpdateRequestQty(val materialId: String, val qtyInput: String) : OutletUiEvent
    data class UpdateRequestNote(val note: String) : OutletUiEvent
    data object SubmitStockRequest : OutletUiEvent

    data class UpdateProductQty(val productId: String, val qtyInput: String) : OutletUiEvent
    data class UpdateCashRevenue(val input: String) : OutletUiEvent
    data class UpdateQrisRevenue(val input: String) : OutletUiEvent
    data class AddDraftExpense(val category: ExpenseCategory, val amount: Long, val note: String) : OutletUiEvent
    data class RemoveDraftExpense(val id: String) : OutletUiEvent
    data class AddDraftDamaged(val materialId: String, val quantity: Int, val note: String) : OutletUiEvent
    data class RemoveDraftDamaged(val id: String) : OutletUiEvent
    data object SubmitDailyReport : OutletUiEvent
    data class ConfirmOverwriteDuplicateReport(val reportId: String) : OutletUiEvent
    data class LoadExistingReportForEdit(val reportId: String) : OutletUiEvent
    data object DismissDuplicateReportAlert : OutletUiEvent
    data object CancelEditDailyReport : OutletUiEvent
}

sealed interface OutletUiEffect {
    data class ShowSnackbar(val message: String) : OutletUiEffect
    data object StockRequestSaved : OutletUiEffect
    data object DistributionConfirmed : OutletUiEffect
    data object DailyReportSaved : OutletUiEffect
}

class OutletViewModel(
    private val outletRepository: OutletRepository,
    private val userRepository: UserRepository,
    private val materialRepository: MaterialRepository,
    private val productRepository: ProductRepository,
    private val distributionRepository: MaterialDistributionRepository,
    private val dailyReportRepository: DailyReportRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _state = MutableStateFlow(OutletUiState())
    val state: StateFlow<OutletUiState> = _state.asStateFlow()

    private val _effects = Channel<OutletUiEffect>(Channel.BUFFERED)
    val effects: Flow<OutletUiEffect> = _effects.receiveAsFlow().shareIn(viewModelScope, SharingStarted.Eagerly)

    private val autoRejectingIds = mutableSetOf<String>()

    init {
        observeStreams()
    }

    private fun reportError(source: String, error: Throwable) {
        Log.e("OutletViewModel", "Backend error ($source)", error)
        val detail = error.localizedMessage?.takeIf { it.isNotBlank() } ?: error.javaClass.simpleName
        val message = "Gagal $source: $detail"
        _state.update { it.copy(backendError = message, isActionLoading = false) }
        viewModelScope.launch { _effects.send(OutletUiEffect.ShowSnackbar(message)) }
    }

    private fun observeStreams() {
        viewModelScope.launch {
            outletRepository.getOutletsFlow(onlyActive = false)
                .catch { e -> reportError("memuat outlets", e) }
                .collect { list -> _state.update { recompute(it.copy(rawOutlets = list)) } }
        }
        viewModelScope.launch {
            userRepository.getAllUsersFlow()
                .catch { e -> reportError("memuat users", e) }
                .collect { list -> _state.update { recompute(it.copy(rawUsers = list)) } }
        }
        viewModelScope.launch {
            materialRepository.getMaterialsFlow()
                .catch { e -> reportError("memuat materials", e) }
                .collect { list -> _state.update { recompute(it.copy(rawMaterials = list)) } }
        }
        viewModelScope.launch {
            productRepository.getProductsFlow(onlyActive = false)
                .catch { e -> reportError("memuat products", e) }
                .collect { list -> _state.update { recompute(it.copy(rawProducts = list)) } }
        }
        viewModelScope.launch {
            distributionRepository.getDistributionsFlow(outletId = null)
                .catch { e -> reportError("memuat distribusi", e) }
                .collect { list ->
                    _state.update { recompute(it.copy(rawDistributions = list)) }
                    autoRejectExpiredRequests(list)
                }
        }
        viewModelScope.launch {
            dailyReportRepository.getDailyReportsFlow(outletId = null)
                .catch { e -> reportError("memuat laporan harian", e) }
                .collect { list -> _state.update { recompute(it.copy(rawReports = list)) } }
        }
    }

    private fun autoRejectExpiredRequests(distributions: List<MaterialDistribution>) {
        val startOfTodaySec = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis / 1000L

        findExpiredPendingDistributions(distributions, startOfTodaySec).forEach { dist ->
            if (dist.id.isBlank() || !autoRejectingIds.add(dist.id)) return@forEach
            viewModelScope.launch {
                distributionRepository.updateDistributionStatus(
                    id = dist.id,
                    status = DistributionStatus.CANCELLED,
                    confirmedBy = "system",
                    confirmationNote = "Ditolak otomatis karena melewati batas tanggal permintaan",
                    confirmedAt = Timestamp.now()
                ).onFailure { e ->
                    autoRejectingIds.remove(dist.id)
                    Log.e("OutletViewModel", "Gagal auto-reject permintaan kadaluarsa (${dist.id})", e)
                }
            }
        }
    }

    fun onEvent(event: OutletUiEvent) {
        when (event) {
            OutletUiEvent.ToggleAssignedOutletOpen -> toggleAssignedOutletOpen()
            is OutletUiEvent.ConfirmDistribution -> confirmDistribution(
                event.distributionId,
                event.receivedByItemId,
                event.note
            )
            OutletUiEvent.ResetStockRequestForm -> _state.update {
                it.copy(requestForm = StockRequestFormState())
            }
            is OutletUiEvent.UpdateRequestQty -> _state.update { current ->
                val digits = event.qtyInput.filter(Char::isDigit)
                val updated = current.requestForm.itemQuantities.toMutableMap()
                if (digits.isBlank()) updated.remove(event.materialId) else updated[event.materialId] = digits
                current.copy(requestForm = current.requestForm.copy(itemQuantities = updated, errorMessage = null))
            }
            is OutletUiEvent.UpdateRequestNote -> _state.update {
                it.copy(requestForm = it.requestForm.copy(note = event.note, errorMessage = null))
            }
            OutletUiEvent.SubmitStockRequest -> submitStockRequest()

            is OutletUiEvent.UpdateProductQty -> _state.update { current ->
                val digits = event.qtyInput.filter(Char::isDigit)
                val updated = current.reportForm.productQuantities.toMutableMap()
                if (digits.isBlank()) updated.remove(event.productId) else updated[event.productId] = digits
                current.copy(reportForm = current.reportForm.copy(productQuantities = updated, errorMessage = null))
            }
            is OutletUiEvent.UpdateCashRevenue -> _state.update {
                it.copy(reportForm = it.reportForm.copy(cashRevenueInput = event.input.filter(Char::isDigit), errorMessage = null))
            }
            is OutletUiEvent.UpdateQrisRevenue -> _state.update {
                it.copy(reportForm = it.reportForm.copy(qrisRevenueInput = event.input.filter(Char::isDigit), errorMessage = null))
            }
            is OutletUiEvent.AddDraftExpense -> _state.update { current ->
                val entry = DraftExpenseEntry(
                    id = "exp_${System.currentTimeMillis()}",
                    category = event.category,
                    amount = event.amount,
                    note = event.note.trim()
                )
                current.copy(reportForm = current.reportForm.copy(expenses = current.reportForm.expenses + entry))
            }
            is OutletUiEvent.RemoveDraftExpense -> _state.update { current ->
                current.copy(
                    reportForm = current.reportForm.copy(
                        expenses = current.reportForm.expenses.filterNot { it.id == event.id }
                    )
                )
            }
            is OutletUiEvent.AddDraftDamaged -> _state.update { current ->
                val matName = current.rawMaterials.firstOrNull { it.id == event.materialId }?.name ?: event.materialId
                val entry = DraftDamagedEntry(
                    id = "dmg_${System.currentTimeMillis()}",
                    materialId = event.materialId,
                    materialName = matName,
                    quantity = event.quantity,
                    note = event.note.trim()
                )
                current.copy(reportForm = current.reportForm.copy(damagedMaterials = current.reportForm.damagedMaterials + entry))
            }
            is OutletUiEvent.RemoveDraftDamaged -> _state.update { current ->
                current.copy(
                    reportForm = current.reportForm.copy(
                        damagedMaterials = current.reportForm.damagedMaterials.filterNot { it.id == event.id }
                    )
                )
            }
            OutletUiEvent.SubmitDailyReport -> submitDailyReport(overwriteReportId = null)
            is OutletUiEvent.ConfirmOverwriteDuplicateReport -> submitDailyReport(overwriteReportId = event.reportId)
            is OutletUiEvent.LoadExistingReportForEdit -> loadExistingReportForEdit(event.reportId)
            OutletUiEvent.DismissDuplicateReportAlert -> _state.update {
                it.copy(reportForm = it.reportForm.copy(pendingDuplicateReport = null))
            }
            OutletUiEvent.CancelEditDailyReport -> _state.update {
                it.copy(reportForm = DailyReportFormState())
            }
        }
    }

    private fun resolveCurrentUser(): User? {
        val email = authRepository.getCurrentUserEmail()
        return _state.value.rawUsers.firstOrNull {
            !email.isNullOrBlank() && it.email.equals(email, ignoreCase = true)
        }
    }

    private fun toggleAssignedOutletOpen() {
        val outlet = _state.value.assignedOutlet ?: return
        val nextOpen = !outlet.isOpen
        viewModelScope.launch {
            _state.update { it.copy(isActionLoading = true) }
            outletRepository.setOutletOpen(outlet.id, nextOpen)
                .onSuccess {
                    _state.update { it.copy(isActionLoading = false) }
                    val label = if (nextOpen) "Buka" else "Tutup"
                    _effects.send(OutletUiEffect.ShowSnackbar("Status Outlet ${outlet.name} diubah ke $label."))
                }
                .onFailure { e -> reportError("mengubah status outlet", e) }
        }
    }

    private fun confirmDistribution(
        distributionId: String,
        receivedByItemId: Map<String, Int>,
        note: String
    ) {
        val dist = _state.value.distributions.firstOrNull { it.id == distributionId } ?: return
        val updatedItems = dist.items.map { item ->
            val rec = (receivedByItemId[item.id] ?: item.quantitySent).coerceAtLeast(0)
            MaterialDistributionItem(
                id = item.id,
                distributionId = distributionId,
                materialId = item.materialId,
                quantitySent = item.quantitySent,
                quantityReceived = rec
            )
        }
        val status = when {
            updatedItems.all { (it.quantityReceived ?: 0) == 0 } -> DistributionStatus.NOT_RECEIVED
            updatedItems.all { (it.quantityReceived ?: 0) >= it.quantitySent } -> DistributionStatus.RECEIVED
            else -> DistributionStatus.PARTIALLY_RECEIVED
        }

        viewModelScope.launch {
            _state.update { it.copy(isActionLoading = true) }
            val confirmedBy = resolveCurrentUser()?.id ?: authRepository.getCurrentUserUid() ?: "staff"
            distributionRepository.confirmDistribution(
                distributionId = distributionId,
                confirmedBy = confirmedBy,
                confirmationNote = note.trim().ifBlank { "Stok diterima dan dikonfirmasi staf outlet" },
                updatedItems = updatedItems,
                status = status
            ).onSuccess {
                _state.update { it.copy(isActionLoading = false) }
                _effects.send(OutletUiEffect.ShowSnackbar("Penerimaan stok berhasil dikonfirmasi."))
                _effects.send(OutletUiEffect.DistributionConfirmed)
            }.onFailure { e ->
                reportError("mengonfirmasi penerimaan stok", e)
            }
        }
    }

    private fun submitStockRequest() {
        val outlet = _state.value.assignedOutlet
        val form = _state.value.requestForm
        val items = form.itemQuantities.mapNotNull { (matId, qtyStr) ->
            val qty = qtyStr.toIntOrNull() ?: 0
            if (qty > 0) MaterialDistributionItem(materialId = matId, quantitySent = qty) else null
        }

        val error = when {
            outlet == null -> "Outlet penugasan belum ditemukan."
            items.isEmpty() -> "Masukkan jumlah minimal 1 bahan mentah (> 0)."
            else -> null
        }
        if (error != null) {
            _state.update { it.copy(requestForm = form.copy(errorMessage = error)) }
            return
        }

        viewModelScope.launch {
            _state.update { it.copy(requestForm = form.copy(isSaving = true, errorMessage = null)) }
            val createdBy = resolveCurrentUser()?.id ?: authRepository.getCurrentUserUid() ?: "staff"
            val distribution = MaterialDistribution(
                outletId = outlet!!.id,
                createdBy = createdBy,
                status = DistributionStatus.DRAFT,
                type = DistributionType.ADDITIONAL,
                note = form.note.trim().ifBlank { "Permintaan tambahan stok dari Outlet ${outlet.name}" },
                createdAt = Timestamp.now()
            )
            distributionRepository.createDistribution(distribution, items)
                .onSuccess {
                    _state.update { it.copy(requestForm = StockRequestFormState()) }
                    _effects.send(OutletUiEffect.ShowSnackbar("Permintaan tambahan stok berhasil dikirim ke Gudang."))
                    _effects.send(OutletUiEffect.StockRequestSaved)
                }
                .onFailure { e ->
                    val detail = e.localizedMessage?.takeIf { it.isNotBlank() } ?: e.javaClass.simpleName
                    val msg = "Gagal mengirim permintaan stok: $detail"
                    _state.update { it.copy(requestForm = form.copy(isSaving = false, errorMessage = msg)) }
                    _effects.send(OutletUiEffect.ShowSnackbar(msg))
                }
        }
    }

    private fun loadExistingReportForEdit(reportId: String) {
        viewModelScope.launch {
            _state.update {
                it.copy(
                    reportForm = it.reportForm.copy(
                        isLoadingExisting = true,
                        pendingDuplicateReport = null,
                        errorMessage = null
                    )
                )
            }
            dailyReportRepository.getFullDailyReport(reportId)
                .onSuccess { full ->
                    if (full == null) {
                        _state.update {
                            it.copy(
                                reportForm = it.reportForm.copy(
                                    isLoadingExisting = false,
                                    errorMessage = "Data laporan tidak ditemukan."
                                )
                            )
                        }
                        return@onSuccess
                    }
                    val matMap = _state.value.rawMaterials.associateBy { it.id }
                    _state.update {
                        it.copy(
                            reportForm = DailyReportFormState(
                                editingReportId = full.report.id,
                                editingReportDate = full.report.date,
                                productQuantities = full.products
                                    .filter { p -> p.quantity > 0 }
                                    .associate { p -> p.productId to p.quantity.toString() },
                                cashRevenueInput = full.report.cashRevenue.toString(),
                                qrisRevenueInput = full.report.qrisRevenue.toString(),
                                expenses = full.expenses.mapIndexed { idx, exp ->
                                    DraftExpenseEntry(
                                        id = exp.id.ifBlank { "exp_edit_$idx" },
                                        category = exp.category,
                                        amount = exp.amount,
                                        note = exp.note.orEmpty()
                                    )
                                },
                                damagedMaterials = full.damagedMaterials.mapIndexed { idx, dmg ->
                                    DraftDamagedEntry(
                                        id = dmg.id.ifBlank { "dmg_edit_$idx" },
                                        materialId = dmg.materialId,
                                        materialName = matMap[dmg.materialId]?.name ?: dmg.materialId,
                                        quantity = dmg.quantity,
                                        note = dmg.note.orEmpty()
                                    )
                                }
                            )
                        )
                    }
                    _effects.send(OutletUiEffect.ShowSnackbar("Data laporan dimuat. Silakan ubah lalu simpan pembaruan."))
                }
                .onFailure { e ->
                    val detail = e.localizedMessage?.takeIf { it.isNotBlank() } ?: e.javaClass.simpleName
                    _state.update {
                        it.copy(
                            reportForm = it.reportForm.copy(
                                isLoadingExisting = false,
                                errorMessage = "Gagal memuat laporan: $detail"
                            )
                        )
                    }
                    _effects.send(OutletUiEffect.ShowSnackbar("Gagal memuat laporan: $detail"))
                }
        }
    }

    private fun submitDailyReport(overwriteReportId: String?) {
        val outlet = _state.value.assignedOutlet
        val form = _state.value.reportForm
        val productMap = _state.value.rawProducts.associateBy { it.id }

        val soldProducts = form.productQuantities.mapNotNull { (prodId, qtyStr) ->
            val qty = qtyStr.toIntOrNull() ?: 0
            val prod = productMap[prodId]
            if (qty > 0 && prod != null) {
                DailyReportProduct(productId = prodId, quantity = qty, price = prod.price)
            } else null
        }
        val totalSold = soldProducts.sumOf { it.quantity }
        val autoRevenue = soldProducts.sumOf { it.subtotal }
        val cashRev = form.cashRevenueInput.toLongOrNull() ?: autoRevenue
        val qrisRev = form.qrisRevenueInput.toLongOrNull() ?: 0L
        val totalRev = cashRev + qrisRev

        val error = when {
            outlet == null -> "Outlet penugasan belum ditemukan."
            totalRev <= 0L && totalSold <= 0 -> "Isi minimal jumlah produk terjual atau pendapatan tunai/QRIS."
            else -> null
        }
        if (error != null) {
            _state.update { it.copy(reportForm = form.copy(errorMessage = error, pendingDuplicateReport = null)) }
            return
        }

        viewModelScope.launch {
            _state.update {
                it.copy(reportForm = form.copy(isSaving = true, errorMessage = null, pendingDuplicateReport = null))
            }

            val targetDate = form.editingReportDate ?: Timestamp.now()
            val resolvedUpdateId = overwriteReportId ?: form.editingReportId

            // Reject duplicate creation automatically unless the user explicitly confirmed update/edit
            if (resolvedUpdateId.isNullOrBlank()) {
                val (dayStart, dayEnd) = calendarDayBounds(targetDate)
                val existingInMemory = _state.value.recentReports.firstOrNull {
                    it.outletId == outlet!!.id && isSameCalendarDay(it.date, targetDate)
                }
                val existingReport = existingInMemory
                    ?: dailyReportRepository.getDailyReportByOutletAndDate(outlet!!.id, dayStart, dayEnd).getOrNull()

                if (existingReport != null) {
                    _state.update {
                        it.copy(
                            reportForm = it.reportForm.copy(
                                isSaving = false,
                                pendingDuplicateReport = existingReport
                            )
                        )
                    }
                    return@launch
                }
            }

            val existingForDate = resolvedUpdateId?.let { id ->
                _state.value.recentReports.firstOrNull { it.id == id }
                    ?: form.pendingDuplicateReport?.takeIf { it.id == id }
            }
            val report = DailyReport(
                id = resolvedUpdateId.orEmpty(),
                outletId = outlet!!.id,
                date = existingForDate?.date ?: targetDate,
                cashRevenue = cashRev,
                qrisRevenue = qrisRev,
                totalSold = totalSold
            )
            val expenses = form.expenses.map {
                DailyReportExpense(category = it.category, amount = it.amount, note = it.note.ifBlank { null })
            }
            val damaged = form.damagedMaterials.map {
                DailyReportDamagedMaterial(materialId = it.materialId, quantity = it.quantity, note = it.note.ifBlank { null })
            }

            dailyReportRepository.createDailyReport(report, soldProducts, expenses, damaged)
                .onSuccess {
                    if (isSameCalendarDay(report.date, Timestamp.now())) {
                        val dailyTarget = outlet.dailyTarget.coerceAtLeast(1L)
                        val targetPct = (totalRev.toFloat() / dailyTarget.toFloat()).coerceIn(0f, 1f)
                        outletRepository.updateOutletDailySummary(outlet.id, totalRev, totalSold, targetPct)
                    }
                    val successMsg = if (resolvedUpdateId.isNullOrBlank()) {
                        "Laporan harian berhasil disimpan ke Firestore."
                    } else {
                        "Laporan harian berhasil diperbarui."
                    }
                    _state.update { it.copy(reportForm = DailyReportFormState()) }
                    _effects.send(OutletUiEffect.ShowSnackbar(successMsg))
                    _effects.send(OutletUiEffect.DailyReportSaved)
                }
                .onFailure { e ->
                    val detail = e.localizedMessage?.takeIf { it.isNotBlank() } ?: e.javaClass.simpleName
                    val msg = "Gagal menyimpan laporan harian: $detail"
                    _state.update { it.copy(reportForm = form.copy(isSaving = false, errorMessage = msg)) }
                    _effects.send(OutletUiEffect.ShowSnackbar(msg))
                }
        }
    }

    private fun recompute(raw: OutletUiState): OutletUiState {
        val email = authRepository.getCurrentUserEmail()
        val matchedUser = raw.rawUsers.firstOrNull {
            !email.isNullOrBlank() && it.email.equals(email, ignoreCase = true)
        }
        val assignedOutlet = matchedUser?.outletId?.let { oid ->
            raw.rawOutlets.firstOrNull { it.id == oid }
        } ?: raw.rawOutlets.firstOrNull { it.isActive } ?: raw.rawOutlets.firstOrNull()

        val matMap = raw.rawMaterials.associateBy { it.id }
        val timeFmt = SimpleDateFormat("dd MMM, HH.mm", Locale.forLanguageTag("id-ID"))

        val outletDists = raw.rawDistributions
            .filter { assignedOutlet == null || it.outletId == assignedOutlet.id }
            .map { dist ->
                val items = dist.items.map { item ->
                    val mat = matMap[item.materialId]
                    OutletDistributionItemUi(
                        id = item.id,
                        materialId = item.materialId,
                        materialName = mat?.name ?: item.materialId,
                        unitLabel = mat?.unit?.firestoreValue ?: "pcs",
                        quantitySent = item.quantitySent,
                        quantityReceived = item.quantityReceived
                    )
                }
                OutletDistributionUi(
                    id = dist.id,
                    status = dist.status,
                    timeLabel = timeFmt.format(Date(dist.createdAt.toDate().time)),
                    note = dist.note,
                    confirmationNote = dist.confirmationNote,
                    items = items,
                    totalSent = items.sumOf { it.quantitySent }
                )
            }

        val outletReports = raw.rawReports
            .filter { assignedOutlet == null || it.outletId == assignedOutlet.id }
            .sortedByDescending { it.date.seconds }
        val nowTs = Timestamp.now()
        val todayReport = outletReports.firstOrNull { isSameCalendarDay(it.date, nowTs) }

        return raw.copy(
            staffName = matchedUser?.name?.takeIf { it.isNotBlank() } ?: "Dimas Pratama",
            assignedOutlet = assignedOutlet,
            activeMaterials = raw.rawMaterials.filter { it.isActive },
            activeProducts = raw.rawProducts.filter { it.isActive },
            distributions = outletDists,
            recentReports = outletReports,
            todayReport = todayReport
        )
    }
}
