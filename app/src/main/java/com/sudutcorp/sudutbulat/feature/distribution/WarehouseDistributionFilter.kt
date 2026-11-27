package com.sudutcorp.sudutbulat.feature.distribution

import com.sudutcorp.sudutbulat.data.model.DistributionStatus
import com.sudutcorp.sudutbulat.data.model.DistributionType
import com.sudutcorp.sudutbulat.data.model.MaterialDistribution
import com.sudutcorp.sudutbulat.data.model.Outlet
import com.sudutcorp.sudutbulat.feature.owner.SortDirection
import java.time.Instant
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.Locale

private val ID_LOCALE: Locale = Locale.forLanguageTag("id-ID")
private val DIST_DATE_CHIP_FORMATTER: DateTimeFormatter =
    DateTimeFormatter.ofPattern("dd MMM yyyy", ID_LOCALE)

enum class WarehouseDistributionTab(val label: String) {
    ROUTINE("Rutin"),
    ADDITIONAL("Tambahan"),
    HISTORY("Riwayat")
}

enum class AdditionalDistributionSubTab(val label: String) {
    UNCONFIRMED("Menunggu"),
    PROCESSED("Diproses")
}

enum class DistributionStatusFilter(
    val label: String,
    val status: DistributionStatus? = null
) {
    ALL("Semua"),
    NOT_SENT("Belum dikirim"),
    PENDING("Pending", DistributionStatus.DRAFT),
    SENT("Dikirim", DistributionStatus.SENT),
    RECEIVED("Diterima", DistributionStatus.RECEIVED),
    PARTIALLY_RECEIVED("Sebagian", DistributionStatus.PARTIALLY_RECEIVED),
    NOT_RECEIVED("Tidak diterima", DistributionStatus.NOT_RECEIVED),
    CANCELLED("Batal", DistributionStatus.CANCELLED);

    companion object {
        fun optionsForTab(
            tab: WarehouseDistributionTab,
            additionalSubTab: AdditionalDistributionSubTab = AdditionalDistributionSubTab.PROCESSED
        ): List<DistributionStatusFilter> = when (tab) {
            WarehouseDistributionTab.ROUTINE -> entries
            WarehouseDistributionTab.ADDITIONAL -> when (additionalSubTab) {
                AdditionalDistributionSubTab.UNCONFIRMED -> emptyList()
                AdditionalDistributionSubTab.PROCESSED -> entries.filterNot { it == NOT_SENT || it == PENDING }
            }
            WarehouseDistributionTab.HISTORY -> entries.filterNot { it == NOT_SENT || it == PENDING }
        }
    }
}

enum class DistributionSortField(val label: String) {
    TIME("Waktu"),
    OUTLET("Outlet"),
    QUANTITY("Jumlah"),
    STATUS("Status")
}

data class DistributionTabFilterState(
    val searchQuery: String = "",
    val statusFilter: DistributionStatusFilter = DistributionStatusFilter.ALL,
    val sortField: DistributionSortField = DistributionSortField.TIME,
    val sortDirection: SortDirection = SortDirection.DESC,
    val selectedDateMillis: Long? = null,
    val selectedOutletId: String? = null,
    val additionalSubTab: AdditionalDistributionSubTab = AdditionalDistributionSubTab.UNCONFIRMED
) {
    val hasActiveFilter: Boolean
        get() = searchQuery.isNotBlank() ||
            statusFilter != DistributionStatusFilter.ALL ||
            selectedDateMillis != null ||
            selectedOutletId != null
}

private fun DistributionStatus.sortPriority(): Int = when (this) {
    DistributionStatus.DRAFT -> 0
    DistributionStatus.SENT -> 1
    DistributionStatus.PARTIALLY_RECEIVED -> 2
    DistributionStatus.NOT_RECEIVED -> 3
    DistributionStatus.RECEIVED -> 4
    DistributionStatus.CANCELLED -> 5
}

// Material 3 DatePicker returns UTC midnight millis; convert via ZoneOffset.UTC before matching local calendar day.
fun filterAndSortDistributions(
    cards: List<DistributionCardUi>,
    tab: WarehouseDistributionTab,
    filter: DistributionTabFilterState,
    zoneId: ZoneId = ZoneId.systemDefault()
): List<DistributionCardUi> {
    if (tab == WarehouseDistributionTab.ROUTINE && filter.statusFilter == DistributionStatusFilter.NOT_SENT) {
        return emptyList()
    }

    val query = filter.searchQuery.trim().lowercase(Locale.ROOT)
    val selectedLocalDate = if (tab == WarehouseDistributionTab.HISTORY) {
        filter.selectedDateMillis?.let {
            Instant.ofEpochMilli(it).atZone(ZoneOffset.UTC).toLocalDate()
        }
    } else {
        null
    }

    val filtered = cards.filter { card ->
        val matchesTabScope = when (tab) {
            WarehouseDistributionTab.ROUTINE -> card.isToday && card.type == DistributionType.ROUTINE
            WarehouseDistributionTab.ADDITIONAL -> card.isToday &&
                card.type == DistributionType.ADDITIONAL &&
                when (filter.additionalSubTab) {
                    AdditionalDistributionSubTab.UNCONFIRMED -> card.status == DistributionStatus.DRAFT
                    AdditionalDistributionSubTab.PROCESSED -> card.status != DistributionStatus.DRAFT
                }
            WarehouseDistributionTab.HISTORY -> !card.isToday &&
                card.status != DistributionStatus.DRAFT
        }
        if (!matchesTabScope) return@filter false

        if (filter.selectedOutletId != null && card.outletId != filter.selectedOutletId) {
            return@filter false
        }

        val matchesStatus = if (
            tab == WarehouseDistributionTab.ADDITIONAL &&
            filter.additionalSubTab == AdditionalDistributionSubTab.UNCONFIRMED
        ) {
            true
        } else {
            when (val target = filter.statusFilter.status) {
                null -> true
                else -> card.status == target
            }
        }
        if (!matchesStatus) return@filter false

        if (selectedLocalDate != null) {
            val cardDate = Instant.ofEpochMilli(card.createdAtMillis).atZone(zoneId).toLocalDate()
            if (cardDate != selectedLocalDate) return@filter false
        }

        if (query.isBlank()) return@filter true
        card.outletName.lowercase(Locale.ROOT).contains(query) ||
            card.itemsSummary.lowercase(Locale.ROOT).contains(query) ||
            card.createdByName.lowercase(Locale.ROOT).contains(query) ||
            (card.note?.lowercase(Locale.ROOT)?.contains(query) == true) ||
            (card.confirmationNote?.lowercase(Locale.ROOT)?.contains(query) == true) ||
            card.items.any { it.materialName.lowercase(Locale.ROOT).contains(query) }
    }

    val comparator: Comparator<DistributionCardUi> = when (filter.sortField) {
        DistributionSortField.TIME -> compareBy<DistributionCardUi> { it.createdAtMillis }
            .thenBy { it.outletName.lowercase(Locale.ROOT) }
        DistributionSortField.OUTLET -> compareBy<DistributionCardUi> { it.outletName.lowercase(Locale.ROOT) }
            .thenByDescending { it.createdAtMillis }
        DistributionSortField.QUANTITY -> compareBy<DistributionCardUi> { it.totalQuantity }
            .thenByDescending { it.createdAtMillis }
        DistributionSortField.STATUS -> compareBy<DistributionCardUi> { it.status.sortPriority() }
            .thenByDescending { it.createdAtMillis }
    }

    return if (filter.sortDirection == SortDirection.ASC) {
        filtered.sortedWith(comparator)
    } else {
        filtered.sortedWith(comparator.reversed())
    }
}

fun filterPendingMorningOutlets(
    outlets: List<Outlet>,
    filter: DistributionTabFilterState
): List<Outlet> {
    if (filter.statusFilter != DistributionStatusFilter.ALL &&
        filter.statusFilter != DistributionStatusFilter.NOT_SENT
    ) {
        return emptyList()
    }

    val outletScoped = if (filter.selectedOutletId != null) {
        outlets.filter { it.id == filter.selectedOutletId }
    } else {
        outlets
    }

    val query = filter.searchQuery.trim().lowercase(Locale.ROOT)
    val filtered = if (query.isBlank()) {
        outletScoped
    } else {
        outletScoped.filter { outlet ->
            outlet.name.lowercase(Locale.ROOT).contains(query) ||
                "outlet ${outlet.name}".lowercase(Locale.ROOT).contains(query) ||
                outlet.address.lowercase(Locale.ROOT).contains(query)
        }
    }

    return if (filter.sortField == DistributionSortField.OUTLET && filter.sortDirection == SortDirection.DESC) {
        filtered.sortedByDescending { it.name.lowercase(Locale.ROOT) }
    } else {
        filtered.sortedBy { it.name.lowercase(Locale.ROOT) }
    }
}

fun formatDistributionUtcDate(dateMillis: Long): String =
    Instant.ofEpochMilli(dateMillis)
        .atZone(ZoneOffset.UTC)
        .toLocalDate()
        .format(DIST_DATE_CHIP_FORMATTER)

data class ExpiredDraftActions(
    val routineToDelete: List<MaterialDistribution>,
    val additionalToReject: List<MaterialDistribution>
)

fun partitionExpiredDraftDistributions(
    distributions: List<MaterialDistribution>,
    startOfTodaySec: Long
): ExpiredDraftActions {
    val expired = distributions.filter {
        it.status == DistributionStatus.DRAFT && it.createdAt.seconds < startOfTodaySec
    }
    val (routine, additional) = expired.partition { it.type == DistributionType.ROUTINE }
    return ExpiredDraftActions(
        routineToDelete = routine,
        additionalToReject = additional
    )
}

fun presetQuantitiesForOutlet(
    outlet: Outlet?,
    activeMaterialIds: Set<String>
): Map<String, String> = outlet?.routinePreset
    ?.filter { it.quantity > 0 && it.materialId in activeMaterialIds }
    ?.associate { it.materialId to it.quantity.toString() }
    ?: emptyMap()

data class WarehouseDashboardMetrics(
    val todayInTransitCount: Int = 0,
    val todayConfirmedCount: Int = 0,
    val todayIssueCount: Int = 0,
    val todayTotalItemsSent: Int = 0,
    val outletsWithPresetCount: Int = 0,
    val completionPercent: Int = 0
)

fun computeWarehouseDashboardMetrics(
    activeOutlets: List<Outlet>,
    activeMaterialIds: Set<String>,
    cards: List<DistributionCardUi>,
    completedMorningCount: Int
): WarehouseDashboardMetrics {
    val todayCards = cards.filter { it.isToday }
    val todayInTransitCount = todayCards.count { it.status == DistributionStatus.SENT }
    val todayConfirmedCount = todayCards.count { it.status == DistributionStatus.RECEIVED }
    val todayIssueCount = todayCards.count {
        it.status == DistributionStatus.PARTIALLY_RECEIVED || it.status == DistributionStatus.NOT_RECEIVED
    }
    val todayTotalItemsSent = todayCards
        .filter { it.status != DistributionStatus.DRAFT && it.status != DistributionStatus.CANCELLED }
        .sumOf { it.totalQuantity }
    val outletsWithPresetCount = activeOutlets.count { outlet ->
        outlet.routinePreset.any { it.quantity > 0 && it.materialId in activeMaterialIds }
    }
    val completionPercent = if (activeOutlets.isNotEmpty()) {
        ((completedMorningCount.toFloat() / activeOutlets.size.toFloat()) * 100f).toInt().coerceIn(0, 100)
    } else {
        0
    }
    return WarehouseDashboardMetrics(
        todayInTransitCount = todayInTransitCount,
        todayConfirmedCount = todayConfirmedCount,
        todayIssueCount = todayIssueCount,
        todayTotalItemsSent = todayTotalItemsSent,
        outletsWithPresetCount = outletsWithPresetCount,
        completionPercent = completionPercent
    )
}
