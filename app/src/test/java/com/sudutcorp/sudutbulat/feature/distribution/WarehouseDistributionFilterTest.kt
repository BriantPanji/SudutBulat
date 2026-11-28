package com.sudutcorp.sudutbulat.feature.distribution

import com.google.firebase.Timestamp
import com.sudutcorp.sudutbulat.data.model.DeliveryMethod
import com.sudutcorp.sudutbulat.data.model.DistributionStatus
import com.sudutcorp.sudutbulat.data.model.DistributionType
import com.sudutcorp.sudutbulat.data.model.MaterialDistribution
import com.sudutcorp.sudutbulat.data.model.Outlet
import com.sudutcorp.sudutbulat.data.model.RoutinePresetItem
import com.sudutcorp.sudutbulat.feature.owner.SortDirection
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZoneOffset

class WarehouseDistributionFilterTest {

    private val jakartaZone: ZoneId = ZoneId.of("Asia/Jakarta")

    private fun card(
        id: String,
        outletId: String = "o_$id",
        outletName: String = "Outlet Sudirman",
        createdByName: String = "Budi Gudang",
        status: DistributionStatus = DistributionStatus.SENT,
        type: DistributionType = DistributionType.ROUTINE,
        totalQuantity: Int = 100,
        createdAtMillis: Long,
        isToday: Boolean = true,
        items: List<DistributionItemDetailUi> = listOf(
            DistributionItemDetailUi("i_$id", "m1", "Tahu Original", "pcs", totalQuantity, null, null)
        ),
        note: String? = null,
        confirmationNote: String? = null
    ) = DistributionCardUi(
        id = id,
        outletId = outletId,
        outletName = outletName,
        createdByName = createdByName,
        confirmedByName = null,
        status = status,
        type = type,
        totalQuantity = totalQuantity,
        itemsSummary = items.joinToString(", ") { "${it.materialName} (${it.quantitySent} ${it.unitLabel})" },
        items = items,
        timeLabel = "27 Nov, 07.00",
        todayTimeLabel = "07.00",
        sentTimeLabel = null,
        confirmedTimeLabel = null,
        createdAtMillis = createdAtMillis,
        note = note,
        confirmationNote = confirmationNote,
        isToday = isToday
    )

    @Test
    fun `routine and additional subtabs strictly show today only while history shows only before today and excludes DRAFT`() {
        val todayMillis = LocalDate.of(2026, 11, 27).atTime(7, 0).atZone(jakartaZone).toInstant().toEpochMilli()
        val yesterdayMillis = LocalDate.of(2026, 11, 26).atTime(7, 0).atZone(jakartaZone).toInstant().toEpochMilli()

        val cards = listOf(
            card("r_today", type = DistributionType.ROUTINE, createdAtMillis = todayMillis, isToday = true),
            card("r_old", type = DistributionType.ROUTINE, createdAtMillis = yesterdayMillis, isToday = false),
            card("a_today_draft", type = DistributionType.ADDITIONAL, status = DistributionStatus.DRAFT, createdAtMillis = todayMillis + 1000, isToday = true),
            card("a_today_sent", type = DistributionType.ADDITIONAL, status = DistributionStatus.SENT, createdAtMillis = todayMillis + 1500, isToday = true),
            card("a_today_cancelled", type = DistributionType.ADDITIONAL, status = DistributionStatus.CANCELLED, createdAtMillis = todayMillis + 1800, isToday = true),
            card("a_old", type = DistributionType.ADDITIONAL, status = DistributionStatus.RECEIVED, createdAtMillis = yesterdayMillis + 1000, isToday = false),
            card("old_draft", type = DistributionType.ADDITIONAL, status = DistributionStatus.DRAFT, createdAtMillis = yesterdayMillis + 2000, isToday = false)
        )

        val routineIds = filterAndSortDistributions(cards, WarehouseDistributionTab.ROUTINE, DistributionTabFilterState(), jakartaZone).map { it.id }
        val additionalUnconfirmedIds = filterAndSortDistributions(
            cards,
            WarehouseDistributionTab.ADDITIONAL,
            DistributionTabFilterState(additionalSubTab = AdditionalDistributionSubTab.UNCONFIRMED),
            jakartaZone
        ).map { it.id }
        val additionalProcessedIds = filterAndSortDistributions(
            cards,
            WarehouseDistributionTab.ADDITIONAL,
            DistributionTabFilterState(additionalSubTab = AdditionalDistributionSubTab.PROCESSED),
            jakartaZone
        ).map { it.id }
        val historyIds = filterAndSortDistributions(cards, WarehouseDistributionTab.HISTORY, DistributionTabFilterState(), jakartaZone).map { it.id }

        assertEquals(listOf("r_today"), routineIds)
        assertEquals(listOf("a_today_draft"), additionalUnconfirmedIds)
        assertEquals(listOf("a_today_cancelled", "a_today_sent"), additionalProcessedIds)
        assertEquals(listOf("a_old", "r_old"), historyIds)
        assertEquals(
            emptyList<DistributionStatusFilter>(),
            DistributionStatusFilter.optionsForTab(WarehouseDistributionTab.ADDITIONAL, AdditionalDistributionSubTab.UNCONFIRMED)
        )
        assertEquals(
            false,
            DistributionStatusFilter.optionsForTab(WarehouseDistributionTab.ADDITIONAL, AdditionalDistributionSubTab.PROCESSED)
                .contains(DistributionStatusFilter.PENDING)
        )
        assertEquals(
            false,
            DistributionStatusFilter.optionsForTab(WarehouseDistributionTab.HISTORY).contains(DistributionStatusFilter.PENDING)
        )
    }

    @Test
    fun `history date picker matches local calendar day across UTC midnight boundary`() {
        val selectedUtcMillis = LocalDate.of(2026, 11, 20)
            .atStartOfDay(ZoneOffset.UTC)
            .toInstant()
            .toEpochMilli()

        val earlyWib = LocalDate.of(2026, 11, 20).atTime(1, 0).atZone(jakartaZone).toInstant().toEpochMilli()
        val prevNightWib = LocalDate.of(2026, 11, 19).atTime(23, 30).atZone(jakartaZone).toInstant().toEpochMilli()
        val nextDayWib = LocalDate.of(2026, 11, 21).atTime(8, 0).atZone(jakartaZone).toInstant().toEpochMilli()

        val cards = listOf(
            card("match", createdAtMillis = earlyWib, isToday = false),
            card("prev", createdAtMillis = prevNightWib, isToday = false),
            card("next", createdAtMillis = nextDayWib, isToday = false)
        )

        val filtered = filterAndSortDistributions(
            cards = cards,
            tab = WarehouseDistributionTab.HISTORY,
            filter = DistributionTabFilterState(selectedDateMillis = selectedUtcMillis),
            zoneId = jakartaZone
        )

        assertEquals(listOf("match"), filtered.map { it.id })
    }

    @Test
    fun `search, status, outlet filter, and sort order work together`() {
        val base = LocalDate.of(2026, 11, 25).atTime(6, 0).atZone(jakartaZone).toInstant().toEpochMilli()
        val cards = listOf(
            card("c1", outletId = "o_tebet", outletName = "Outlet Tebet", status = DistributionStatus.RECEIVED, type = DistributionType.ROUTINE, totalQuantity = 200, createdAtMillis = base, isToday = false),
            card(
                "c2",
                outletId = "o_cikini",
                outletName = "Outlet Cikini",
                status = DistributionStatus.CANCELLED,
                type = DistributionType.ADDITIONAL,
                totalQuantity = 500,
                createdAtMillis = base + 60_000,
                isToday = false,
                note = "Stok chilly oil habis"
            ),
            card("c3", outletId = "o_gm", outletName = "Outlet Gajah Mada", status = DistributionStatus.SENT, type = DistributionType.ADDITIONAL, totalQuantity = 150, createdAtMillis = base + 120_000, isToday = false)
        )

        val cancelledSearch = filterAndSortDistributions(
            cards = cards,
            tab = WarehouseDistributionTab.HISTORY,
            filter = DistributionTabFilterState(
                searchQuery = "chilly",
                statusFilter = DistributionStatusFilter.CANCELLED,
                selectedOutletId = "o_cikini"
            ),
            zoneId = jakartaZone
        )
        assertEquals(listOf("c2"), cancelledSearch.map { it.id })

        val filteredByOutlet = filterAndSortDistributions(
            cards = cards,
            tab = WarehouseDistributionTab.HISTORY,
            filter = DistributionTabFilterState(selectedOutletId = "o_tebet"),
            zoneId = jakartaZone
        )
        assertEquals(listOf("c1"), filteredByOutlet.map { it.id })

        val byQtyDesc = filterAndSortDistributions(
            cards = cards,
            tab = WarehouseDistributionTab.HISTORY,
            filter = DistributionTabFilterState(
                sortField = DistributionSortField.QUANTITY,
                sortDirection = SortDirection.DESC
            ),
            zoneId = jakartaZone
        )
        assertEquals(listOf("c2", "c1", "c3"), byQtyDesc.map { it.id })

        val byStatusPriorityAsc = filterAndSortDistributions(
            cards = cards,
            tab = WarehouseDistributionTab.HISTORY,
            filter = DistributionTabFilterState(
                sortField = DistributionSortField.STATUS,
                sortDirection = SortDirection.ASC
            ),
            zoneId = jakartaZone
        )
        assertEquals(listOf("c3", "c1", "c2"), byStatusPriorityAsc.map { it.id })
    }

    @Test
    fun `pending morning outlets filter respects search and status filter`() {
        val outlets = listOf(
            Outlet(id = "o1", name = "Tebet", address = "Ringroad Setia Budi"),
            Outlet(id = "o2", name = "Cikini", address = "Jl. Dr. Mansyur")
        )

        val notSentFiltered = filterPendingMorningOutlets(
            outlets = outlets,
            filter = DistributionTabFilterState(
                searchQuery = "mansyur",
                statusFilter = DistributionStatusFilter.NOT_SENT
            )
        )
        assertEquals(listOf("o2"), notSentFiltered.map { it.id })

        val hiddenOnReceived = filterPendingMorningOutlets(
            outlets = outlets,
            filter = DistributionTabFilterState(statusFilter = DistributionStatusFilter.RECEIVED)
        )
        assertEquals(emptyList<String>(), hiddenOnReceived.map { it.id })

        val filteredBySelectedOutlet = filterPendingMorningOutlets(
            outlets = outlets,
            filter = DistributionTabFilterState(selectedOutletId = "o1")
        )
        assertEquals(listOf("o1"), filteredBySelectedOutlet.map { it.id })
    }

    @Test
    fun `partitionExpiredDraftDistributions deletes expired ROUTINE drafts and rejects expired ADDITIONAL drafts`() {
        val startOfTodaySec = 1_764_200_000L
        val list = listOf(
            MaterialDistribution(
                id = "expired_routine_draft",
                outletId = "o1",
                status = DistributionStatus.DRAFT,
                type = DistributionType.ROUTINE,
                createdAt = Timestamp(startOfTodaySec - 120, 0)
            ),
            MaterialDistribution(
                id = "expired_additional_draft",
                outletId = "o1",
                status = DistributionStatus.DRAFT,
                type = DistributionType.ADDITIONAL,
                createdAt = Timestamp(startOfTodaySec - 60, 0)
            ),
            MaterialDistribution(
                id = "today_draft",
                outletId = "o1",
                status = DistributionStatus.DRAFT,
                type = DistributionType.ADDITIONAL,
                createdAt = Timestamp(startOfTodaySec + 60, 0)
            ),
            MaterialDistribution(
                id = "yesterday_sent",
                outletId = "o1",
                status = DistributionStatus.SENT,
                type = DistributionType.ROUTINE,
                createdAt = Timestamp(startOfTodaySec - 3600, 0)
            )
        )

        val actions = partitionExpiredDraftDistributions(list, startOfTodaySec)
        assertEquals(listOf("expired_routine_draft"), actions.routineToDelete.map { it.id })
        assertEquals(listOf("expired_additional_draft"), actions.additionalToReject.map { it.id })
    }

    @Test
    fun `presetQuantitiesForOutlet filters inactive materials and zero quantities`() {
        val outlet = Outlet(
            id = "o1",
            name = "Sudirman",
            routinePreset = listOf(
                RoutinePresetItem("mat_1", 500),
                RoutinePresetItem("mat_2", 0),
                RoutinePresetItem("mat_inactive", 200)
            )
        )

        val map = presetQuantitiesForOutlet(outlet, setOf("mat_1", "mat_2"))
        assertEquals(mapOf("mat_1" to "500"), map)
    }

    @Test
    fun `computeWarehouseDashboardMetrics aggregates today statuses, items sent, presets, and completion`() {
        val todayMillis = LocalDate.of(2026, 11, 27).atTime(8, 0).atZone(jakartaZone).toInstant().toEpochMilli()
        val yesterdayMillis = LocalDate.of(2026, 11, 26).atTime(8, 0).atZone(jakartaZone).toInstant().toEpochMilli()

        val activeOutlets = listOf(
            Outlet(id = "o1", name = "Sudirman", routinePreset = listOf(RoutinePresetItem("m1", 200))),
            Outlet(id = "o2", name = "Tebet", routinePreset = listOf(RoutinePresetItem("m_inactive", 100))),
            Outlet(id = "o3", name = "Cikini", routinePreset = listOf(RoutinePresetItem("m1", 0))),
            Outlet(id = "o4", name = "Kemang", routinePreset = listOf(RoutinePresetItem("m2", 50)))
        )

        val cards = listOf(
            card("sent_today", status = DistributionStatus.SENT, totalQuantity = 200, createdAtMillis = todayMillis, isToday = true),
            card("recv_today", status = DistributionStatus.RECEIVED, totalQuantity = 150, createdAtMillis = todayMillis, isToday = true),
            card("partial_today", status = DistributionStatus.PARTIALLY_RECEIVED, totalQuantity = 100, createdAtMillis = todayMillis, isToday = true),
            card("not_recv_today", status = DistributionStatus.NOT_RECEIVED, totalQuantity = 50, createdAtMillis = todayMillis, isToday = true),
            card("draft_today", status = DistributionStatus.DRAFT, totalQuantity = 999, createdAtMillis = todayMillis, isToday = true),
            card("cancel_today", status = DistributionStatus.CANCELLED, totalQuantity = 888, createdAtMillis = todayMillis, isToday = true),
            card("sent_yesterday", status = DistributionStatus.SENT, totalQuantity = 777, createdAtMillis = yesterdayMillis, isToday = false)
        )

        val metrics = computeWarehouseDashboardMetrics(
            activeOutlets = activeOutlets,
            activeMaterialIds = setOf("m1", "m2"),
            cards = cards,
            completedMorningCount = 3
        )

        assertEquals(1, metrics.todayInTransitCount)
        assertEquals(1, metrics.todayConfirmedCount)
        assertEquals(2, metrics.todayIssueCount)
        assertEquals(500, metrics.todayTotalItemsSent)
        assertEquals(2, metrics.outletsWithPresetCount)
        assertEquals(75, metrics.completionPercent)
    }

    @Test
    fun `computeWarehouseDashboardMetrics handles empty active outlets without division by zero`() {
        val metrics = computeWarehouseDashboardMetrics(
            activeOutlets = emptyList(),
            activeMaterialIds = emptySet(),
            cards = emptyList(),
            completedMorningCount = 0
        )

        assertEquals(0, metrics.completionPercent)
        assertEquals(0, metrics.outletsWithPresetCount)
        assertEquals(0, metrics.todayTotalItemsSent)
    }

    @Test
    fun `ARRIVED status is searchable by deliveryMethod, filterable, prioritized above SENT, and counted in transit`() {
        val todayMillis = LocalDate.of(2026, 11, 27).atTime(9, 0).atZone(jakartaZone).toInstant().toEpochMilli()
        val cards = listOf(
            card("sent_c", status = DistributionStatus.SENT, createdAtMillis = todayMillis, isToday = true)
                .copy(deliveryMethod = DeliveryMethod.WAREHOUSE_COURIER),
            card("arrived_c", status = DistributionStatus.ARRIVED, createdAtMillis = todayMillis + 1000, isToday = true)
                .copy(deliveryMethod = DeliveryMethod.GOJEK, arrivedTimeLabel = "09.15")
        )

        val arrivedOnly = filterAndSortDistributions(
            cards = cards,
            tab = WarehouseDistributionTab.ROUTINE,
            filter = DistributionTabFilterState(statusFilter = DistributionStatusFilter.ARRIVED),
            zoneId = jakartaZone
        )
        assertEquals(listOf("arrived_c"), arrivedOnly.map { it.id })

        val byGojekSearch = filterAndSortDistributions(
            cards = cards,
            tab = WarehouseDistributionTab.ROUTINE,
            filter = DistributionTabFilterState(searchQuery = "gojek"),
            zoneId = jakartaZone
        )
        assertEquals(listOf("arrived_c"), byGojekSearch.map { it.id })

        val byStatusAsc = filterAndSortDistributions(
            cards = cards,
            tab = WarehouseDistributionTab.ROUTINE,
            filter = DistributionTabFilterState(sortField = DistributionSortField.STATUS, sortDirection = SortDirection.ASC),
            zoneId = jakartaZone
        )
        assertEquals(listOf("arrived_c", "sent_c"), byStatusAsc.map { it.id })

        val metrics = computeWarehouseDashboardMetrics(
            activeOutlets = listOf(Outlet(id = "o1", name = "Sudirman")),
            activeMaterialIds = setOf("m1"),
            cards = cards,
            completedMorningCount = 1
        )
        assertEquals(2, metrics.todayInTransitCount)
    }
}
