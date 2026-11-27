package com.sudutcorp.sudutbulat.feature.owner

import com.sudutcorp.sudutbulat.data.model.DailyReport
import com.sudutcorp.sudutbulat.data.model.DailyReportDamagedMaterial
import com.sudutcorp.sudutbulat.data.model.DailyReportExpense
import com.sudutcorp.sudutbulat.data.model.DailyReportProduct
import com.sudutcorp.sudutbulat.data.model.Material
import com.sudutcorp.sudutbulat.data.model.Outlet
import com.sudutcorp.sudutbulat.data.model.Product
import com.sudutcorp.sudutbulat.data.model.ProductSaleType
import com.sudutcorp.sudutbulat.data.model.Role
import com.sudutcorp.sudutbulat.data.model.RoutinePresetItem
import com.sudutcorp.sudutbulat.data.model.UnitType
import com.sudutcorp.sudutbulat.data.model.User

enum class OutletTabMode(val label: String) {
    PERFORMANCE("Performa"),
    OUTLET_LIST("Outlet"),
    STAFF_LIST("Staf")
}

enum class ReportTabMode(val label: String) {
    SUMMARY("Ringkasan"),
    LIST("Daftar Laporan")
}

enum class OutletSortField(val label: String) {
    PROFIT("Keuntungan"),
    NAME("Nama"),
    TARGET("Target")
}

enum class SortDirection {
    DESC,
    ASC
}

enum class CatalogTabMode(val label: String) {
    MATERIAL("Material"),
    PRODUCT("Produk Jual")
}

enum class MaterialSortField(val label: String) {
    NAME("Nama"),
    COST("Harga Modal"),
    UNIT("Satuan")
}

enum class ProductSortField(val label: String) {
    NAME("Nama"),
    PRICE("Harga Jual"),
    STATUS("Status")
}

data class OutletPerformanceUi(
    val id: String,
    val rank: Int,
    val name: String,
    val displayName: String,
    val address: String,
    val coordinateLabel: String,
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
    val staffIds: List<String>,
    val staffNames: List<String>,
    val expenseTotal: Long,
    val expenseCount: Int,
    val damagedCount: Int,
    val damagedReportCount: Int,
    val netDepositEstimate: Long,
    val routinePreset: List<RoutinePresetItem> = emptyList()
)

data class StaffItemUi(
    val id: String,
    val name: String,
    val initials: String,
    val email: String,
    val username: String,
    val role: Role,
    val outletId: String?,
    val outletName: String?,
    val isActive: Boolean
)

data class DailyReportListItemUi(
    val id: String,
    val outletId: String,
    val outletName: String,
    val dateMillis: Long,
    val dateLabel: String,
    val cashRevenue: Long,
    val qrisRevenue: Long,
    val totalRevenue: Long,
    val totalSold: Int,
    val totalExpense: Long,
    val damagedCount: Int,
    val hasAlert: Boolean
)

data class ReportProductLineUi(
    val id: String,
    val productName: String,
    val quantity: Int,
    val price: Long,
    val subtotal: Long
)

data class ReportExpenseLineUi(
    val id: String,
    val categoryLabel: String,
    val amount: Long,
    val note: String?
)

data class ReportDamagedLineUi(
    val id: String,
    val materialName: String,
    val quantityText: String,
    val note: String?
)

data class SingleReportDetailUi(
    val id: String,
    val outletName: String,
    val dateLabel: String,
    val fullDateLabel: String,
    val timeLabel: String,
    val cashRevenue: Long,
    val qrisRevenue: Long,
    val totalRevenue: Long,
    val totalSold: Int,
    val productLines: List<ReportProductLineUi>,
    val expenseLines: List<ReportExpenseLineUi>,
    val totalExpense: Long,
    val damagedLines: List<ReportDamagedLineUi>
)

data class DamagedSummaryItemUi(
    val materialId: String,
    val name: String,
    val quantityText: String
)

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

data class CatalogItemUi(
    val id: String,
    val name: String,
    val categoryId: String = "",
    val category: String,
    val isProduct: Boolean,
    val priceOrCost: Long,
    val unit: UnitType = UnitType.PCS,
    val saleType: ProductSaleType = ProductSaleType.SATUAN,
    val pcsPerPack: Int = 1,
    val unitLabel: String,
    val subtitleText: String,
    val statusBadge: String,
    val isWarning: Boolean,
    val isActive: Boolean = true
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
    val editingItemId: String? = null,
    val name: String = "",
    val isProduct: Boolean = false,
    val categoryInput: String = "",
    val priceOrCostInput: String = "",
    val unit: UnitType = UnitType.PCS,
    val saleType: ProductSaleType = ProductSaleType.SATUAN,
    val pcsPerPackInput: String = "",
    val isActive: Boolean = true,
    val isSaving: Boolean = false,
    val errorMessage: String? = null
)

data class OwnerUiState(
    val ownerName: String = "Arif Rahman",
    val ownerEmail: String = "sdb.owner@gmail.com",
    val ownerInitials: String = "AR",
    val todayDateLabel: String = "Hari ini, 24 Mei 2025",

    val outlets: List<Outlet> = emptyList(),
    val users: List<User> = emptyList(),
    val reports: List<DailyReport> = emptyList(),
    val reportProducts: List<DailyReportProduct> = emptyList(),
    val expenses: List<DailyReportExpense> = emptyList(),
    val damagedMaterials: List<DailyReportDamagedMaterial> = emptyList(),
    val materials: List<Material> = emptyList(),
    val products: List<Product> = emptyList(),

    val outletItems: List<OutletPerformanceUi> = emptyList(),
    val staffItems: List<StaffItemUi> = emptyList(),
    val reportListItems: List<DailyReportListItemUi> = emptyList(),

    val outletSearchQuery: String = "",
    val outletFilter: String = "Semua",
    val outletSortField: OutletSortField = OutletSortField.PROFIT,
    val outletSortDirection: SortDirection = SortDirection.DESC,
    val outletTabMode: OutletTabMode = OutletTabMode.PERFORMANCE,

    val staffSearchQuery: String = "",
    val staffRoleFilter: String = "Semua",

    val todayTotalRevenue: Long = 8_420_000L,
    val todayDeltaPercentText: String = "12,4% dari kemarin",
    val activeOutletsCount: Int = 5,
    val openOutletsCount: Int = 4,
    val closedOutletsCount: Int = 1,
    val todayTotalSold: Int = 1_684,
    val todayTotalExpenses: Long = 940_000L,
    val todayExpenseTransactionsCount: Int = 11,
    val reviewReportsCount: Int = 2,

    val reportTabMode: ReportTabMode = ReportTabMode.SUMMARY,
    val selectedReportPeriod: String = "30 Hari",
    val reportSearchQuery: String = "",
    val reportOutletFilterId: String? = null,
    val reportDateFilter: String = "Semua",
    val reportSelectedDateMillis: Long? = null,

    val catalogItems: List<CatalogItemUi> = emptyList(),

    val notifications: List<OwnerNotificationUi> = defaultNotifications(),

    val addOutletForm: AddOutletFormState = AddOutletFormState(),
    val addUserForm: AddUserFormState = AddUserFormState(),

    val isLoading: Boolean = false,
    val backendError: String? = null
)

sealed interface OwnerUiEvent {
    data class OutletSearchChanged(val query: String) : OwnerUiEvent
    data class OutletFilterSelected(val filter: String) : OwnerUiEvent
    data class OutletSortChanged(val field: OutletSortField, val direction: SortDirection) : OwnerUiEvent
    data class OutletTabSelected(val mode: OutletTabMode) : OwnerUiEvent
    data class ToggleOutletOpenStatus(val outletId: String) : OwnerUiEvent
    data class SaveOutletRoutinePreset(val outletId: String, val items: List<RoutinePresetItem>) : OwnerUiEvent

    data class StaffSearchChanged(val query: String) : OwnerUiEvent
    data class StaffRoleFilterSelected(val filter: String) : OwnerUiEvent
    data class AssignStaffToOutlet(val outletId: String, val staffIds: Set<String>) : OwnerUiEvent
    data class ReassignSingleStaffOutlet(val userId: String, val outletId: String?) : OwnerUiEvent
    data class ToggleUserActive(val userId: String) : OwnerUiEvent

    data class ReportTabSelected(val mode: ReportTabMode) : OwnerUiEvent
    data class ReportPeriodSelected(val period: String) : OwnerUiEvent
    data class ReportSearchChanged(val query: String) : OwnerUiEvent
    data class ReportOutletFilterSelected(val outletId: String?) : OwnerUiEvent
    data class ReportDateFilterSelected(val filter: String) : OwnerUiEvent
    data class ReportSpecificDateSelected(val dateMillis: Long?) : OwnerUiEvent

    data class AddOutletNameChanged(val name: String) : OwnerUiEvent
    data class AddOutletAddressChanged(val address: String) : OwnerUiEvent
    data class AddOutletOpenTimeChanged(val openTime: String) : OwnerUiEvent
    data class AddOutletCloseTimeChanged(val closeTime: String) : OwnerUiEvent
    data class AddOutletTargetChanged(val targetInput: String) : OwnerUiEvent
    data class ToggleAddOutletStaff(val userId: String) : OwnerUiEvent
    data object ResetAddOutletForm : OwnerUiEvent
    data object SubmitNewOutlet : OwnerUiEvent

    data class InitAddUserForm(val preselectedOutletId: String?) : OwnerUiEvent
    data class AddUserRoleSelected(val role: Role) : OwnerUiEvent
    data class AddUserNameChanged(val fullName: String) : OwnerUiEvent
    data class AddUserEmailChanged(val emailOrPhone: String) : OwnerUiEvent
    data class AddUserPasswordChanged(val password: String) : OwnerUiEvent
    data object ToggleAddUserPasswordVisibility : OwnerUiEvent
    data class AddUserOutletSelected(val outletId: String) : OwnerUiEvent
    data object SubmitNewUser : OwnerUiEvent

    data object MarkAllNotificationsRead : OwnerUiEvent
    data object DismissBackendError : OwnerUiEvent
}

sealed interface OwnerUiEffect {
    data class ShowSnackbar(val message: String) : OwnerUiEffect
    data object NavigateBack : OwnerUiEffect
}
