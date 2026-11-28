package com.sudutcorp.sudutbulat.feature.owner

import com.google.firebase.Timestamp
import com.sudutcorp.sudutbulat.data.model.DailyReport
import com.sudutcorp.sudutbulat.data.model.DailyReportDamagedMaterial
import com.sudutcorp.sudutbulat.data.model.DailyReportExpense
import com.sudutcorp.sudutbulat.data.model.DailyReportProduct
import com.sudutcorp.sudutbulat.data.model.ExpenseCategory
import com.sudutcorp.sudutbulat.data.model.Material
import com.sudutcorp.sudutbulat.data.model.Outlet
import com.sudutcorp.sudutbulat.data.model.Product
import com.sudutcorp.sudutbulat.data.model.Role
import com.sudutcorp.sudutbulat.data.model.UnitType
import com.sudutcorp.sudutbulat.data.model.User
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate
import java.time.ZoneId
import java.util.Date

class OwnerReportSummaryTest {

    private val jakartaZone: ZoneId = ZoneId.of("Asia/Jakarta")

    private fun ts(year: Int, month: Int, day: Int, hour: Int = 20): Timestamp {
        val instant = LocalDate.of(year, month, day)
            .atTime(hour, 0)
            .atZone(jakartaZone)
            .toInstant()
        return Timestamp(Date.from(instant))
    }

    @Test
    fun buildReportPeriodSummaryAggregatesRealFirestoreDataFor7Days() {
        val nowMillis = LocalDate.of(2026, 11, 28)
            .atTime(18, 0)
            .atZone(jakartaZone)
            .toInstant()
            .toEpochMilli()

        val outlets = listOf(
            Outlet(id = "o1", name = "Sudirman"),
            Outlet(id = "o2", name = "Gajah Mada")
        )
        val materials = listOf(
            Material(id = "m_tahu", name = "Tahu Original", cost = 500L, unit = UnitType.PCS),
            Material(id = "m_saus", name = "Saus Sambal", cost = 3_000L, unit = UnitType.UNIT)
        )
        val products = listOf(
            Product(id = "p_ori", name = "Tahu Ori (Bungkus 5)", price = 5_000L)
        )
        val reports = listOf(
            // Current 7-day window (22..28 Nov)
            DailyReport(id = "r1", outletId = "o1", date = ts(2026, 11, 28), cashRevenue = 1_200_000L, qrisRevenue = 800_000L, totalSold = 400, createdBy = "staff1"),
            DailyReport(id = "r2", outletId = "o2", date = ts(2026, 11, 26), cashRevenue = 600_000L, qrisRevenue = 400_000L, totalSold = 200, createdBy = "staff2"),
            // Previous 7-day window (15..21 Nov) -> Rp2.000.000 (+50% growth)
            DailyReport(id = "r_prev", outletId = "o1", date = ts(2026, 11, 18), cashRevenue = 1_000_000L, qrisRevenue = 1_000_000L, totalSold = 400)
        )
        val reportProducts = listOf(
            DailyReportProduct(id = "rp1", dailyReportId = "r1", productId = "p_ori", quantity = 80, price = 5_000L),
            DailyReportProduct(id = "rp2", dailyReportId = "r2", productId = "p_ori", quantity = 40, price = 5_000L),
            DailyReportProduct(id = "rp_prev", dailyReportId = "r_prev", productId = "p_ori", quantity = 50, price = 5_000L)
        )
        val expenses = listOf(
            DailyReportExpense(id = "e1", dailyReportId = "r1", category = ExpenseCategory.GAS, amount = 40_000L),
            DailyReportExpense(id = "e2", dailyReportId = "r1", category = ExpenseCategory.PLASTIC_BAG, amount = 20_000L),
            DailyReportExpense(id = "e3", dailyReportId = "r2", category = ExpenseCategory.OTHER, amount = 30_000L),
            DailyReportExpense(id = "e_prev", dailyReportId = "r_prev", category = ExpenseCategory.GAS, amount = 50_000L)
        )
        val damaged = listOf(
            DailyReportDamagedMaterial(id = "d1", dailyReportId = "r1", materialId = "m_tahu", quantity = 10),
            DailyReportDamagedMaterial(id = "d2", dailyReportId = "r2", materialId = "m_saus", quantity = 2)
        )

        val state = OwnerUiState(
            outlets = outlets,
            materials = materials,
            products = products,
            reports = reports,
            reportProducts = reportProducts,
            expenses = expenses,
            damagedMaterials = damaged,
            hasFirestoreSync = true
        )

        val summary = buildReportPeriodSummary(state, "7 Hari", nowMillis, jakartaZone)

        assertEquals("7 hari", summary.periodLabel)
        assertEquals(2, summary.reportCount)
        assertEquals(2, summary.contributingOutletsCount)
        assertEquals(3_000_000L, summary.totalRevenue)
        assertEquals(1_800_000L, summary.cashRevenue)
        assertEquals(1_200_000L, summary.qrisRevenue)
        assertEquals(0.6f, summary.cashShareFraction, 0.001f)
        assertEquals(600, summary.totalSold)
        assertEquals(90_000L, summary.totalExpense)
        assertEquals(2_910_000L, summary.netRevenue)
        assertEquals(12, summary.damagedTotalUnits)
        assertEquals(11_000L, summary.damagedEstimatedLoss) // 10*500 + 2*3000
        assertEquals(1, summary.deltaTrend)
        assertTrue(summary.deltaText.startsWith("+50"))
        assertEquals(7, summary.trendBars.size)
        assertEquals(2, summary.outletBreakdowns.size)
        assertEquals("o1", summary.outletBreakdowns.first().outletId)
        assertEquals(1, summary.productBreakdowns.size)
        assertEquals(120, summary.productBreakdowns.first().totalQuantity)
        assertEquals(600_000L, summary.productBreakdowns.first().totalSubtotal)
    }

    @Test
    fun syncedEmptyFirestoreProducesCleanZeroSummaryWithoutHardcodedMocks() {
        val nowMillis = LocalDate.of(2026, 11, 28)
            .atTime(12, 0)
            .atZone(jakartaZone)
            .toInstant()
            .toEpochMilli()

        val summary = buildReportPeriodSummary(
            state = OwnerUiState(hasFirestoreSync = true),
            period = "30 Hari",
            nowMillis = nowMillis,
            zoneId = jakartaZone
        )

        assertEquals(0, summary.reportCount)
        assertEquals(0L, summary.totalRevenue)
        assertEquals(0L, summary.totalExpense)
        assertEquals(0L, summary.netRevenue)
        assertEquals(0L, summary.damagedEstimatedLoss)
        assertNull(summary.deltaTrend)
        assertTrue(summary.outletBreakdowns.isEmpty())
        assertTrue(summary.productBreakdowns.isEmpty())
        assertTrue(summary.damagedItems.isEmpty())
        assertEquals(6, summary.trendBars.size)
    }

    @Test
    fun buildSingleReportDetailIncludesCreatorNameAndDamagedLoss() {
        val raw = computeOwnerDerivedState(
            OwnerUiState(
                outlets = listOf(Outlet(id = "o1", name = "Sudirman")),
                users = listOf(User(id = "staff1", name = "Dimas Pratama", email = "dimas@sb.id", username = "staff1", role = Role.OUTLET, outletId = "o1")),
                materials = listOf(Material(id = "m_tahu", name = "Tahu Original", cost = 500L, unit = UnitType.PCS)),
                reports = listOf(
                    DailyReport(id = "r1", outletId = "o1", date = ts(2026, 11, 28), cashRevenue = 500_000L, qrisRevenue = 250_000L, totalSold = 150, createdBy = "staff1")
                ),
                damagedMaterials = listOf(
                    DailyReportDamagedMaterial(id = "d1", dailyReportId = "r1", materialId = "m_tahu", quantity = 8, note = "Sobek")
                ),
                hasFirestoreSync = true
            )
        )

        val detail = buildSingleReportDetail(raw, "r1")!!
        assertEquals("Dimas Pratama", detail.createdByName)
        assertEquals(4_000L, detail.damagedEstimatedLoss)
        assertEquals(4_000L, detail.damagedLines.first().estimatedLoss)
    }
}
