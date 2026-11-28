package com.sudutcorp.sudutbulat.feature.owner

import com.google.firebase.Timestamp
import com.sudutcorp.sudutbulat.data.model.DailyReport
import com.sudutcorp.sudutbulat.data.model.DailyReportDamagedMaterial
import com.sudutcorp.sudutbulat.data.model.DailyReportExpense
import com.sudutcorp.sudutbulat.data.model.DistributionStatus
import com.sudutcorp.sudutbulat.data.model.DistributionType
import com.sudutcorp.sudutbulat.data.model.ExpenseCategory
import com.sudutcorp.sudutbulat.data.model.Material
import com.sudutcorp.sudutbulat.data.model.MaterialDistribution
import com.sudutcorp.sudutbulat.data.model.Outlet
import com.sudutcorp.sudutbulat.data.model.UnitType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate
import java.time.ZoneId
import java.util.Date

class OwnerDashboardMetricsTest {

    private val jakartaZone: ZoneId = ZoneId.of("Asia/Jakarta")

    private fun ts(year: Int, month: Int, day: Int, hour: Int, minute: Int = 0): Timestamp {
        val instant = LocalDate.of(year, month, day)
            .atTime(hour, minute)
            .atZone(jakartaZone)
            .toInstant()
        return Timestamp(Date.from(instant))
    }

    @Test
    fun computesTodayRevenueSplitExpensesDamagedLossAndDeltaFromFirestoreData() {
        val nowMillis = LocalDate.of(2026, 11, 28)
            .atTime(15, 30)
            .atZone(jakartaZone)
            .toInstant()
            .toEpochMilli()

        val outlets = listOf(
            Outlet(id = "o1", name = "Sudirman", isActive = true, isOpen = true, dailyTarget = 2_000_000L),
            Outlet(id = "o2", name = "Cikini", isActive = true, isOpen = false, dailyTarget = 1_000_000L),
            Outlet(id = "o_inactive", name = "Lama", isActive = false, isOpen = false, dailyTarget = 1_000_000L)
        )
        val materials = listOf(
            Material(id = "m_tahu", name = "Tahu Original", cost = 500L, unit = UnitType.PCS),
            Material(id = "m_saus", name = "Saus Sambal", cost = 3_000L, unit = UnitType.UNIT)
        )
        val reports = listOf(
            DailyReport(id = "r_today_o1", outletId = "o1", date = ts(2026, 11, 28, 14, 0), cashRevenue = 1_200_000L, qrisRevenue = 600_000L, totalSold = 360),
            DailyReport(id = "r_yest_o1", outletId = "o1", date = ts(2026, 11, 27, 20, 0), cashRevenue = 1_000_000L, qrisRevenue = 500_000L, totalSold = 300)
        )
        val expenses = listOf(
            DailyReportExpense(id = "e1", dailyReportId = "r_today_o1", category = ExpenseCategory.GAS, amount = 22_000L),
            DailyReportExpense(id = "e2", dailyReportId = "r_today_o1", category = ExpenseCategory.OTHER, amount = 220_000L),
            // Yesterday's expense must NOT be counted in today's total
            DailyReportExpense(id = "e_yest", dailyReportId = "r_yest_o1", category = ExpenseCategory.GAS, amount = 50_000L)
        )
        val damaged = listOf(
            DailyReportDamagedMaterial(id = "d1", dailyReportId = "r_today_o1", materialId = "m_tahu", quantity = 10),
            DailyReportDamagedMaterial(id = "d2", dailyReportId = "r_today_o1", materialId = "m_saus", quantity = 2),
            // Yesterday's damaged must NOT be counted in today's total
            DailyReportDamagedMaterial(id = "d_yest", dailyReportId = "r_yest_o1", materialId = "m_tahu", quantity = 12)
        )
        val distributions = listOf(
            MaterialDistribution(
                id = "dist_routine_sent",
                outletId = "o1",
                createdBy = "gudang",
                status = DistributionStatus.SENT,
                type = DistributionType.ROUTINE,
                createdAt = ts(2026, 11, 28, 7, 0),
                sentAt = ts(2026, 11, 28, 7, 15)
            ),
            MaterialDistribution(
                id = "dist_issue_today",
                outletId = "o2",
                createdBy = "gudang",
                status = DistributionStatus.PARTIALLY_RECEIVED,
                type = DistributionType.ROUTINE,
                createdAt = ts(2026, 11, 28, 7, 30),
                sentAt = ts(2026, 11, 28, 7, 45)
            ),
            MaterialDistribution(
                id = "dist_req_pending",
                outletId = "o1",
                createdBy = "staff",
                status = DistributionStatus.DRAFT,
                type = DistributionType.ADDITIONAL,
                createdAt = ts(2026, 11, 28, 13, 0)
            )
        )

        val derived = computeOwnerDerivedState(
            raw = OwnerUiState(
                outlets = outlets,
                materials = materials,
                reports = reports,
                expenses = expenses,
                damagedMaterials = damaged,
                distributions = distributions,
                hasFirestoreSync = true
            ),
            nowMillis = nowMillis,
            zoneId = jakartaZone
        )

        val m = derived.dashboardMetrics
        assertEquals("Selamat sore", m.greetingPrefix)
        assertEquals(1_800_000L, m.todayTotalRevenue)
        assertEquals(1_200_000L, m.todayCashRevenue)
        assertEquals(600_000L, m.todayQrisRevenue)
        assertEquals(3_000_000L, m.totalDailyTarget) // only active outlets
        assertEquals(60, m.targetPercent)
        assertEquals(1, m.todayDeltaTrend) // +20,0% vs 1.5M yesterday
        assertTrue(m.todayDeltaPercentText.startsWith("+20"))
        assertEquals(2, m.activeOutletsCount)
        assertEquals(1, m.openOutletsCount)
        assertEquals(1, m.closedOutletsCount)
        assertEquals(1, m.todayReportedOutletsCount)
        assertEquals(360, m.todayTotalSold)
        assertEquals(242_000L, m.todayTotalExpenses)
        assertEquals(2, m.todayExpenseTransactionsCount)
        assertEquals(12, m.todayDamagedUnits)
        assertEquals(11_000L, m.todayDamagedEstimatedLoss) // 10*500 + 2*3000
        assertEquals(1_558_000L, m.todayNetRevenue)
        assertEquals(2, m.reviewReportsCount) // today (dmg=12 & other=220k) + yesterday (dmg=12)
        assertEquals(2, m.todayRoutineSentOutletsCount)
        assertEquals(1, m.todayInTransitDistributionsCount)
        assertEquals(1, m.pendingAdditionalRequestsCount)
        assertEquals(1, m.todayDistributionIssueCount)

        val o1Ui = derived.outletItems.first { it.id == "o1" }
        val o2Ui = derived.outletItems.first { it.id == "o2" }
        assertTrue(o1Ui.hasTodayReport)
        assertFalse(o2Ui.hasTodayReport)
        assertEquals("Belum closing", o2Ui.targetPercentText)
    }

    @Test
    fun syncedEmptyFirestoreProducesCleanZeroMetricsWithoutFakeFallbacks() {
        val nowMillis = LocalDate.of(2026, 11, 28)
            .atTime(9, 0)
            .atZone(jakartaZone)
            .toInstant()
            .toEpochMilli()

        val derived = computeOwnerDerivedState(
            raw = OwnerUiState(hasFirestoreSync = true),
            nowMillis = nowMillis,
            zoneId = jakartaZone
        )

        val m = derived.dashboardMetrics
        assertEquals("Selamat pagi", m.greetingPrefix)
        assertEquals(0L, m.todayTotalRevenue)
        assertEquals(0L, m.todayTotalExpenses)
        assertEquals(0, m.todayTotalSold)
        assertEquals(0, m.todayDamagedUnits)
        assertEquals(0L, m.todayDamagedEstimatedLoss)
        assertEquals(0, m.reviewReportsCount)
        assertEquals(0, m.activeOutletsCount)
        assertNull(m.todayDeltaTrend)
        assertEquals("Belum ada pembanding kemarin", m.todayDeltaPercentText)
        assertTrue(derived.outletItems.isEmpty())
    }
}
