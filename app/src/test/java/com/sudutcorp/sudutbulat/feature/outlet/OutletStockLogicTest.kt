package com.sudutcorp.sudutbulat.feature.outlet

import com.google.firebase.Timestamp
import com.sudutcorp.sudutbulat.data.model.DailyReport
import com.sudutcorp.sudutbulat.data.model.DeliveryMethod
import com.sudutcorp.sudutbulat.data.model.DistributionStatus
import com.sudutcorp.sudutbulat.data.model.DistributionType
import com.sudutcorp.sudutbulat.data.model.Outlet
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Date

class OutletStockLogicTest {

    @Test
    fun resolveConfirmationStatus_coversFullPartialZeroAndExcess() {
        assertEquals(
            DistributionStatus.RECEIVED,
            resolveConfirmationStatus(listOf(100 to 100, 50 to 50))
        )
        assertEquals(
            DistributionStatus.RECEIVED,
            resolveConfirmationStatus(listOf(100 to 110, 50 to 50))
        )
        assertEquals(
            DistributionStatus.PARTIALLY_RECEIVED,
            resolveConfirmationStatus(listOf(100 to 90, 50 to 50))
        )
        assertEquals(
            DistributionStatus.PARTIALLY_RECEIVED,
            resolveConfirmationStatus(listOf(100 to 0, 50 to 50))
        )
        assertEquals(
            DistributionStatus.NOT_RECEIVED,
            resolveConfirmationStatus(listOf(100 to 0, 50 to 0))
        )
    }

    @Test
    fun filterOutletDistributions_filtersByStatusAndSearchQuery() {
        val sample = listOf(
            OutletDistributionUi(
                id = "d1",
                status = DistributionStatus.SENT,
                type = DistributionType.ROUTINE,
                timeLabel = "Hari ini, 06.45",
                createdByName = "Budi Santoso",
                note = "Pengiriman pagi",
                confirmationNote = null,
                items = listOf(
                    OutletDistributionItemUi("i1", "m1", "Tahu Original", "pcs", 200, null)
                ),
                totalSent = 200
            ),
            OutletDistributionUi(
                id = "d2",
                status = DistributionStatus.DRAFT,
                type = DistributionType.ADDITIONAL,
                timeLabel = "Hari ini, 11.20",
                createdByName = "Dimas Pratama",
                note = "Stok pedas menipis",
                confirmationNote = null,
                items = listOf(
                    OutletDistributionItemUi("i2", "m2", "Tahu Pedas", "pcs", 100, null)
                ),
                totalSent = 100
            ),
            OutletDistributionUi(
                id = "d3",
                status = DistributionStatus.RECEIVED,
                type = DistributionType.ROUTINE,
                timeLabel = "26 Nov, 06.30",
                createdByName = "Budi Santoso",
                note = null,
                confirmationNote = "Diterima lengkap",
                items = listOf(
                    OutletDistributionItemUi("i3", "m3", "Chilly Oil", "unit", 5, 5, note = "5 botol")
                ),
                totalSent = 5,
                totalReceived = 5
            )
        )

        assertEquals(listOf("d1"), filterOutletDistributions(sample, OutletStockFilter.NEED_CONFIRM, "").map { it.id })
        assertEquals(listOf("d2"), filterOutletDistributions(sample, OutletStockFilter.REQUEST, "").map { it.id })
        assertEquals(listOf("d3"), filterOutletDistributions(sample, OutletStockFilter.DONE, "").map { it.id })
        assertEquals(listOf("d2"), filterOutletDistributions(sample, OutletStockFilter.ALL, "pedas").map { it.id })
        assertEquals(listOf("d3"), filterOutletDistributions(sample, OutletStockFilter.ALL, "botol").map { it.id })
        assertEquals(listOf("d1", "d3"), filterOutletDistributions(sample, OutletStockFilter.ALL, "budi").map { it.id })
    }

    @Test
    fun computeOutletDashboardMetrics_separatesTodayReportFromLastReportAndCountsStock() {
        val outlet = Outlet(
            id = "outlet_sudirman",
            name = "Sudirman",
            dailyTarget = 2_000_000L,
            totalRevenue = 1_800_000L,
            totalPiecesSold = 350
        )
        val now = Date(1_760_000_000_000L)
        val yesterdayReport = DailyReport(
            id = "rep_yday",
            outletId = "outlet_sudirman",
            date = Timestamp(Date(now.time - 86_400_000L)),
            cashRevenue = 1_000_000L,
            qrisRevenue = 700_000L,
            totalSold = 320
        )
        val todayReport = DailyReport(
            id = "rep_today",
            outletId = "outlet_sudirman",
            date = Timestamp(now),
            cashRevenue = 900_000L,
            qrisRevenue = 600_000L,
            totalSold = 290
        )
        val dists = listOf(
            OutletDistributionUi(
                id = "dist_routine",
                status = DistributionStatus.RECEIVED,
                type = DistributionType.ROUTINE,
                timeLabel = "Hari ini, 07.00",
                isToday = true,
                note = null,
                confirmationNote = null,
                items = emptyList(),
                totalSent = 500,
                totalReceived = 490
            ),
            OutletDistributionUi(
                id = "dist_extra_sent",
                status = DistributionStatus.SENT,
                type = DistributionType.ADDITIONAL,
                timeLabel = "Hari ini, 13.00",
                isToday = true,
                note = null,
                confirmationNote = null,
                items = emptyList(),
                totalSent = 150
            ),
            OutletDistributionUi(
                id = "dist_req_draft",
                status = DistributionStatus.DRAFT,
                type = DistributionType.ADDITIONAL,
                timeLabel = "Hari ini, 14.00",
                isToday = true,
                note = null,
                confirmationNote = null,
                items = emptyList(),
                totalSent = 80
            )
        )

        // Case 1: Today report not yet submitted -> todayRevenue is 0, lastReport falls back to yesterdayReport
        val withoutToday = computeOutletDashboardMetrics(
            assignedOutlet = outlet,
            todayReport = null,
            recentReports = listOf(yesterdayReport),
            distributions = dists,
            nowDate = now
        )
        assertFalse(withoutToday.hasTodayReport)
        assertEquals(0L, withoutToday.todayRevenue)
        assertEquals(0, withoutToday.targetPercent)
        assertEquals(1_700_000L, withoutToday.lastReportRevenue)
        assertEquals(320, withoutToday.lastReportSold)
        assertEquals(1, withoutToday.pendingConfirmCount)
        assertEquals(1, withoutToday.pendingRequestCount)
        assertEquals(640, withoutToday.todayReceivedUnits)
        assertEquals("Rutin pagi diterima", withoutToday.morningRoutineStatusLabel)

        // Case 2: Today report submitted -> todayRevenue reflects cash + QRIS and target %
        val withToday = computeOutletDashboardMetrics(
            assignedOutlet = outlet,
            todayReport = todayReport,
            recentReports = listOf(todayReport, yesterdayReport),
            distributions = dists,
            nowDate = now
        )
        assertTrue(withToday.hasTodayReport)
        assertEquals(1_500_000L, withToday.todayRevenue)
        assertEquals(900_000L, withToday.todayCashRevenue)
        assertEquals(600_000L, withToday.todayQrisRevenue)
        assertEquals(75, withToday.targetPercent)
    }

    @Test
    fun arrivedDistributions_appearFirstInNeedConfirm_searchableByMethod_andCountedInMetrics() {
        val sample = listOf(
            OutletDistributionUi(
                id = "d_sent",
                status = DistributionStatus.SENT,
                type = DistributionType.ROUTINE,
                deliveryMethod = DeliveryMethod.WAREHOUSE_COURIER,
                timeLabel = "Hari ini, 07.00",
                isToday = true,
                note = null,
                confirmationNote = null,
                items = emptyList(),
                totalSent = 100
            ),
            OutletDistributionUi(
                id = "d_arrived",
                status = DistributionStatus.ARRIVED,
                type = DistributionType.ADDITIONAL,
                deliveryMethod = DeliveryMethod.GRAB,
                arrivedTimeLabel = "11.05",
                timeLabel = "Hari ini, 10.45",
                isToday = true,
                note = null,
                confirmationNote = null,
                items = emptyList(),
                totalSent = 50
            )
        )

        val needConfirm = filterOutletDistributions(sample, OutletStockFilter.NEED_CONFIRM, "")
        assertEquals(listOf("d_arrived", "d_sent"), needConfirm.map { it.id })

        val byMethod = filterOutletDistributions(sample, OutletStockFilter.ALL, "grab")
        assertEquals(listOf("d_arrived"), byMethod.map { it.id })

        val metrics = computeOutletDashboardMetrics(
            assignedOutlet = Outlet(id = "o1", name = "Sudirman"),
            todayReport = null,
            recentReports = emptyList(),
            distributions = sample,
            nowDate = Date(1_760_000_000_000L)
        )
        assertEquals(2, metrics.pendingConfirmCount)
        assertEquals(1, metrics.arrivedPendingCount)
    }
}
