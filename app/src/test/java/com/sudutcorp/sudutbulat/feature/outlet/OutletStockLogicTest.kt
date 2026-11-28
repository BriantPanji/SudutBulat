package com.sudutcorp.sudutbulat.feature.outlet

import com.google.firebase.Timestamp
import com.sudutcorp.sudutbulat.data.auth.AuthRepository
import com.sudutcorp.sudutbulat.data.model.DailyReport
import com.sudutcorp.sudutbulat.data.model.DeliveryMethod
import com.sudutcorp.sudutbulat.data.model.DistributionStatus
import com.sudutcorp.sudutbulat.data.model.DistributionType
import com.sudutcorp.sudutbulat.data.model.Material
import com.sudutcorp.sudutbulat.data.model.MaterialDistribution
import com.sudutcorp.sudutbulat.data.model.MaterialDistributionItem
import com.sudutcorp.sudutbulat.data.model.Outlet
import com.sudutcorp.sudutbulat.data.remote.CloudinaryMediaUploader
import com.sudutcorp.sudutbulat.data.repository.DailyReportRepository
import com.sudutcorp.sudutbulat.data.repository.FakeDailyReportRemoteDataSource
import com.sudutcorp.sudutbulat.data.repository.FakeMaterialDistributionRemoteDataSource
import com.sudutcorp.sudutbulat.data.repository.FakeMaterialRemoteDataSource
import com.sudutcorp.sudutbulat.data.repository.FakeOutletRemoteDataSource
import com.sudutcorp.sudutbulat.data.repository.FakeProductRemoteDataSource
import com.sudutcorp.sudutbulat.data.repository.FakeUserRemoteDataSource
import com.sudutcorp.sudutbulat.data.repository.MaterialDistributionRepository
import com.sudutcorp.sudutbulat.data.repository.MaterialRepository
import com.sudutcorp.sudutbulat.data.repository.OutletRepository
import com.sudutcorp.sudutbulat.data.repository.ProductRepository
import com.sudutcorp.sudutbulat.data.repository.UserRepository
import com.sudutcorp.sudutbulat.feature.distribution.WarehouseViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Date

private class FakeReceiptCloudinaryUploader : CloudinaryMediaUploader() {
    var shouldFail: Boolean = false
    var uploadCalls: Int = 0
    var lastDistributionId: String? = null
    var lastFolder: String? = null
    var urlToReturn: String = "https://res.cloudinary.com/fp7jzide/image/upload/v1/dr/dr_dist_1.jpg"

    override suspend fun uploadDistributionReceiptPhoto(
        distributionId: String,
        jpegBytes: ByteArray,
        folder: String
    ): Result<String> = runCatching {
        uploadCalls += 1
        lastDistributionId = distributionId
        lastFolder = folder
        if (shouldFail) throw IllegalStateException("Simulasi gagal upload Cloudinary")
        urlToReturn
    }
}

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

    @OptIn(ExperimentalCoroutinesApi::class)
    @After
    fun tearDownMain() {
        Dispatchers.resetMain()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun confirmDistribution_requiresPhotoForReceived_skipsUploadForNotReceived_andExposesInWarehouseHistory() = runTest {
        Dispatchers.setMain(UnconfinedTestDispatcher(testScheduler))
        assertEquals("dr", CloudinaryMediaUploader.DEFAULT_DISTRIBUTION_RECEIPT_FOLDER)

        val outletDs = FakeOutletRemoteDataSource().apply {
            outlets["o1"] = Outlet(id = "o1", name = "Sudirman", isActive = true)
        }
        val matDs = FakeMaterialRemoteDataSource().apply {
            materials["m1"] = Material(id = "m1", name = "Tahu Original")
        }
        val distDs = FakeMaterialDistributionRemoteDataSource().apply {
            distributions["dist_1"] = MaterialDistribution(
                id = "dist_1",
                outletId = "o1",
                createdBy = "gudang",
                status = DistributionStatus.ARRIVED,
                items = listOf(
                    MaterialDistributionItem(
                        id = "item_1",
                        distributionId = "dist_1",
                        materialId = "m1",
                        quantitySent = 100
                    )
                )
            )
            distributions["dist_2"] = MaterialDistribution(
                id = "dist_2",
                outletId = "o1",
                createdBy = "gudang",
                status = DistributionStatus.ARRIVED,
                items = listOf(
                    MaterialDistributionItem(
                        id = "item_2",
                        distributionId = "dist_2",
                        materialId = "m1",
                        quantitySent = 50
                    )
                )
            )
        }
        val fakeUploader = FakeReceiptCloudinaryUploader()
        val outletVm = OutletViewModel(
            outletRepository = OutletRepository(outletDs),
            userRepository = UserRepository(FakeUserRemoteDataSource()),
            materialRepository = MaterialRepository(matDs),
            productRepository = ProductRepository(FakeProductRemoteDataSource()),
            distributionRepository = MaterialDistributionRepository(distDs),
            dailyReportRepository = DailyReportRepository(FakeDailyReportRemoteDataSource()),
            authRepository = object : AuthRepository() {},
            mediaUploader = fakeUploader
        )

        // 1. RECEIVED without photo bytes must be rejected before Cloudinary/Firestore
        outletVm.onEvent(
            OutletUiEvent.ConfirmDistribution(
                distributionId = "dist_1",
                receivedByItemId = mapOf("item_1" to 100),
                note = "Lengkap",
                receiptPhotoBytes = null
            )
        )
        assertEquals(0, fakeUploader.uploadCalls)
        assertEquals(DistributionStatus.ARRIVED, distDs.distributions["dist_1"]?.status)

        // 2. Upload failure must NOT update Firestore
        fakeUploader.shouldFail = true
        outletVm.onEvent(
            OutletUiEvent.ConfirmDistribution(
                distributionId = "dist_1",
                receivedByItemId = mapOf("item_1" to 100),
                note = "Lengkap",
                receiptPhotoBytes = byteArrayOf(1, 2, 3)
            )
        )
        assertEquals(1, fakeUploader.uploadCalls)
        assertEquals(DistributionStatus.ARRIVED, distDs.distributions["dist_1"]?.status)
        assertNull(distDs.distributions["dist_1"]?.receiptPhotoUrl)

        // 3. RECEIVED with photo uploads to folder "dr" and persists secure_url in Firestore
        fakeUploader.shouldFail = false
        outletVm.onEvent(
            OutletUiEvent.ConfirmDistribution(
                distributionId = "dist_1",
                receivedByItemId = mapOf("item_1" to 100),
                note = "Lengkap",
                receiptPhotoBytes = byteArrayOf(4, 5, 6)
            )
        )
        assertEquals(2, fakeUploader.uploadCalls)
        assertEquals("dist_1", fakeUploader.lastDistributionId)
        assertEquals("dr", fakeUploader.lastFolder)
        assertEquals(DistributionStatus.RECEIVED, distDs.distributions["dist_1"]?.status)
        assertEquals(fakeUploader.urlToReturn, distDs.distributions["dist_1"]?.receiptPhotoUrl)

        // 4. NOT_RECEIVED (0 qty) must succeed WITHOUT uploading to Cloudinary
        outletVm.onEvent(
            OutletUiEvent.ConfirmDistribution(
                distributionId = "dist_2",
                receivedByItemId = mapOf("item_2" to 0),
                note = "Outlet sudah tutup",
                receiptPhotoBytes = null
            )
        )
        assertEquals(2, fakeUploader.uploadCalls)
        assertEquals(DistributionStatus.NOT_RECEIVED, distDs.distributions["dist_2"]?.status)
        assertNull(distDs.distributions["dist_2"]?.receiptPhotoUrl)

        // 5. WarehouseViewModel exposes receiptPhotoUrl in DistributionCardUi for history screen
        val warehouseVm = WarehouseViewModel(
            outletRepository = OutletRepository(outletDs),
            materialRepository = MaterialRepository(matDs),
            distributionRepository = MaterialDistributionRepository(distDs),
            userRepository = UserRepository(FakeUserRemoteDataSource()),
            authRepository = object : AuthRepository() {}
        )
        val dist1Card = warehouseVm.state.value.distributionCards.first { it.id == "dist_1" }
        val dist2Card = warehouseVm.state.value.distributionCards.first { it.id == "dist_2" }
        assertEquals(fakeUploader.urlToReturn, dist1Card.receiptPhotoUrl)
        assertNull(dist2Card.receiptPhotoUrl)
    }
}
