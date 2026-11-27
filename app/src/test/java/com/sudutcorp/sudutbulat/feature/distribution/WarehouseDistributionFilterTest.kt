package com.sudutcorp.sudutbulat.feature.distribution

import com.sudutcorp.sudutbulat.data.model.DistributionStatus
import com.sudutcorp.sudutbulat.data.model.DistributionType
import com.sudutcorp.sudutbulat.data.model.Outlet
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
        outletId = "o_$id",
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
    fun `routine and additional tabs strictly show today only while history shows all dates`() {
        val todayMillis = LocalDate.of(2026, 11, 27).atTime(7, 0).atZone(jakartaZone).toInstant().toEpochMilli()
        val yesterdayMillis = LocalDate.of(2026, 11, 26).atTime(7, 0).atZone(jakartaZone).toInstant().toEpochMilli()

        val cards = listOf(
            card("r_today", type = DistributionType.ROUTINE, createdAtMillis = todayMillis, isToday = true),
            card("r_old", type = DistributionType.ROUTINE, createdAtMillis = yesterdayMillis, isToday = false),
            card("a_today", type = DistributionType.ADDITIONAL, status = DistributionStatus.DRAFT, createdAtMillis = todayMillis + 1000, isToday = true),
            card("a_old", type = DistributionType.ADDITIONAL, status = DistributionStatus.RECEIVED, createdAtMillis = yesterdayMillis + 1000, isToday = false)
        )

        val routineIds = filterAndSortDistributions(cards, WarehouseDistributionTab.ROUTINE, DistributionTabFilterState(), jakartaZone).map { it.id }
        val additionalIds = filterAndSortDistributions(cards, WarehouseDistributionTab.ADDITIONAL, DistributionTabFilterState(), jakartaZone).map { it.id }
        val historyIds = filterAndSortDistributions(cards, WarehouseDistributionTab.HISTORY, DistributionTabFilterState(), jakartaZone).map { it.id }

        assertEquals(listOf("r_today"), routineIds)
        assertEquals(listOf("a_today"), additionalIds)
        assertEquals(listOf("a_today", "r_today", "a_old", "r_old"), historyIds)
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
    fun `search, status, type, and sort order work together`() {
        val base = LocalDate.of(2026, 11, 27).atTime(6, 0).atZone(jakartaZone).toInstant().toEpochMilli()
        val cards = listOf(
            card("c1", outletName = "Outlet Tebet", status = DistributionStatus.RECEIVED, type = DistributionType.ROUTINE, totalQuantity = 200, createdAtMillis = base),
            card(
                "c2",
                outletName = "Outlet Cikini",
                status = DistributionStatus.CANCELLED,
                type = DistributionType.ADDITIONAL,
                totalQuantity = 500,
                createdAtMillis = base + 60_000,
                note = "Stok chilly oil habis"
            ),
            card("c3", outletName = "Outlet Gajah Mada", status = DistributionStatus.DRAFT, type = DistributionType.ADDITIONAL, totalQuantity = 150, createdAtMillis = base + 120_000)
        )

        val cancelledSearch = filterAndSortDistributions(
            cards = cards,
            tab = WarehouseDistributionTab.HISTORY,
            filter = DistributionTabFilterState(
                searchQuery = "chilly",
                statusFilter = DistributionStatusFilter.CANCELLED,
                typeFilter = DistributionTypeFilter.ADDITIONAL
            ),
            zoneId = jakartaZone
        )
        assertEquals(listOf("c2"), cancelledSearch.map { it.id })

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
    }
}
