package com.sudutcorp.sudutbulat.feature.owner

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZoneOffset

class ReportDateFilterTest {

    private val jakartaZone: ZoneId = ZoneId.of("Asia/Jakarta")

    private fun report(
        id: String,
        outletId: String = "outlet_sudirman",
        outletName: String = "Outlet Sudirman",
        dateMillis: Long,
        dateLabel: String = "20 Nov 2026, 01.00"
    ) = DailyReportListItemUi(
        id = id,
        outletId = outletId,
        outletName = outletName,
        dateMillis = dateMillis,
        dateLabel = dateLabel,
        cashRevenue = 1_000_000L,
        qrisRevenue = 500_000L,
        totalRevenue = 1_500_000L,
        totalSold = 300,
        totalExpense = 100_000L,
        damagedCount = 0,
        hasAlert = false
    )

    @Test
    fun `specific date matches local calendar day across UTC midnight boundary`() {
        val selectedUtcMillis = LocalDate.of(2026, 11, 20)
            .atStartOfDay(ZoneOffset.UTC)
            .toInstant()
            .toEpochMilli()

        // 2026-11-20 01:00 WIB is 2026-11-19 18:00 UTC — must still match 20 Nov in Asia/Jakarta.
        val earlyMorningJakarta = LocalDate.of(2026, 11, 20)
            .atTime(1, 0)
            .atZone(jakartaZone)
            .toInstant()
            .toEpochMilli()

        val previousDayLateJakarta = LocalDate.of(2026, 11, 19)
            .atTime(23, 30)
            .atZone(jakartaZone)
            .toInstant()
            .toEpochMilli()

        val nextDayJakarta = LocalDate.of(2026, 11, 21)
            .atTime(9, 0)
            .atZone(jakartaZone)
            .toInstant()
            .toEpochMilli()

        val items = listOf(
            report("match_early", dateMillis = earlyMorningJakarta),
            report("prev_day", dateMillis = previousDayLateJakarta),
            report("next_day", dateMillis = nextDayJakarta)
        )

        val filtered = filterDailyReports(
            items = items,
            searchQuery = "",
            outletFilterId = null,
            dateFilter = "Semua",
            selectedDateMillis = selectedUtcMillis,
            zoneId = jakartaZone
        )

        assertEquals(listOf("match_early"), filtered.map { it.id })
    }

    @Test
    fun `search query filters by outlet name only and ignores dateLabel`() {
        val dateMillis = LocalDate.of(2026, 11, 20)
            .atTime(12, 0)
            .atZone(jakartaZone)
            .toInstant()
            .toEpochMilli()

        val items = listOf(
            report("r1", outletName = "Outlet Sudirman", dateMillis = dateMillis, dateLabel = "20 Nov 2026, 12.00"),
            report("r2", outletName = "Outlet Cikini", dateMillis = dateMillis, dateLabel = "20 Nov 2026, 14.00")
        )

        val byDateText = filterDailyReports(
            items = items,
            searchQuery = "Nov 2026",
            outletFilterId = null,
            dateFilter = "Semua",
            selectedDateMillis = null,
            nowMillis = dateMillis,
            zoneId = jakartaZone
        )
        assertEquals(emptyList<String>(), byDateText.map { it.id })

        val byOutletName = filterDailyReports(
            items = items,
            searchQuery = "cikini",
            outletFilterId = null,
            dateFilter = "Semua",
            selectedDateMillis = null,
            nowMillis = dateMillis,
            zoneId = jakartaZone
        )
        assertEquals(listOf("r2"), byOutletName.map { it.id })
    }

    @Test
    fun `preset date filters continue working when no specific date is selected`() {
        val nowMillis = LocalDate.of(2026, 11, 20)
            .atTime(12, 0)
            .atZone(jakartaZone)
            .toInstant()
            .toEpochMilli()
        val dayMillis = 86_400_000L

        val items = listOf(
            report("today", dateMillis = nowMillis - 3_600_000L),
            report("five_days", dateMillis = nowMillis - 5L * dayMillis),
            report("twenty_days", dateMillis = nowMillis - 20L * dayMillis)
        )

        val sevenDays = filterDailyReports(
            items = items,
            searchQuery = "",
            outletFilterId = null,
            dateFilter = "7 Hari",
            selectedDateMillis = null,
            nowMillis = nowMillis,
            zoneId = jakartaZone
        )

        assertEquals(listOf("today", "five_days"), sevenDays.map { it.id })
    }
}
