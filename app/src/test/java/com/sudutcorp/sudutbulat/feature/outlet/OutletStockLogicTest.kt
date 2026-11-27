package com.sudutcorp.sudutbulat.feature.outlet

import com.sudutcorp.sudutbulat.data.model.DistributionStatus
import com.sudutcorp.sudutbulat.data.model.DistributionType
import org.junit.Assert.assertEquals
import org.junit.Test

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
}
