package com.sudutcorp.sudutbulat.feature.outlet

import androidx.lifecycle.viewmodel.CreationExtras
import com.google.firebase.Timestamp
import com.sudutcorp.sudutbulat.data.DefaultAppContainer
import com.sudutcorp.sudutbulat.data.auth.AuthRepository
import com.sudutcorp.sudutbulat.data.model.DailyReport
import com.sudutcorp.sudutbulat.data.model.Outlet
import com.sudutcorp.sudutbulat.data.model.Product
import com.sudutcorp.sudutbulat.data.model.Role
import com.sudutcorp.sudutbulat.data.model.User
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
import com.sudutcorp.sudutbulat.feature.owner.OwnerViewModel
import com.sudutcorp.sudutbulat.feature.stock.CatalogViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Calendar

@OptIn(ExperimentalCoroutinesApi::class)
class OutletDailyReportTest {

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun daysAgoTimestamp(daysAgo: Int): Timestamp {
        val cal = Calendar.getInstance().apply {
            add(Calendar.DAY_OF_YEAR, -daysAgo)
        }
        return Timestamp(cal.time)
    }

    @Test
    fun isSameCalendarDayDistinguishesTodayAndYesterday() {
        val now = Calendar.getInstance()
        val sameDayDifferentHour = (now.clone() as Calendar).apply {
            set(Calendar.HOUR_OF_DAY, if (get(Calendar.HOUR_OF_DAY) == 0) 23 else 0)
        }
        val yesterday = (now.clone() as Calendar).apply {
            add(Calendar.DAY_OF_YEAR, -1)
        }

        assertTrue(isSameCalendarDay(Timestamp(now.time), Timestamp(sameDayDifferentHour.time)))
        assertFalse(isSameCalendarDay(Timestamp(now.time), Timestamp(yesterday.time)))
    }

    @Test
    fun editWindowAllowsOnlyLastThreeDaysAndCreateWindowAllowsMissingDatesWithinSevenDays() {
        val nowTs = Timestamp.now()

        assertTrue(isReportEditable(daysAgoTimestamp(0), nowTs))
        assertTrue(isReportEditable(daysAgoTimestamp(1), nowTs))
        assertTrue(isReportEditable(daysAgoTimestamp(3), nowTs))
        assertFalse(isReportEditable(daysAgoTimestamp(4), nowTs))
        assertFalse(isReportEditable(daysAgoTimestamp(10), nowTs))

        val existingReports = listOf(
            DailyReport(id = "r_today", outletId = "o1", date = daysAgoTimestamp(0)),
            DailyReport(id = "r_2d", outletId = "o1", date = daysAgoTimestamp(2))
        )

        val candidates = buildReportDateCandidates(existingReports, nowTs)
        assertEquals(8, candidates.size) // 0..7 days ago
        assertTrue(candidates[0].hasExistingReport)
        assertFalse(candidates[1].hasExistingReport)
        assertTrue(candidates[2].hasExistingReport)

        assertFalse(isReportDateCreatable(daysAgoTimestamp(0), existingReports, nowTs)) // duplicate
        assertTrue(isReportDateCreatable(daysAgoTimestamp(1), existingReports, nowTs))  // missing & <= 7d
        assertTrue(isReportDateCreatable(daysAgoTimestamp(7), existingReports, nowTs))  // missing & == 7d
        assertFalse(isReportDateCreatable(daysAgoTimestamp(8), existingReports, nowTs)) // > 7d
    }

    @Test
    fun editingReportOlderThanThreeDaysIsRejectedInViewModelAndRepository() = runTest {
        Dispatchers.setMain(UnconfinedTestDispatcher(testScheduler))

        val outletDs = FakeOutletRemoteDataSource().apply {
            outlets["outlet_1"] = Outlet(id = "outlet_1", name = "Sudirman", address = "Jl. Sudirman", isActive = true)
        }
        val userDs = FakeUserRemoteDataSource().apply {
            users["staff_1"] = User(
                id = "staff_1",
                name = "Dimas Pratama",
                email = "staff@sudutbulat.id",
                role = Role.OUTLET,
                outletId = "outlet_1"
            )
        }
        val reportDs = FakeDailyReportRemoteDataSource().apply {
            reports["rep_old"] = DailyReport(
                id = "rep_old",
                outletId = "outlet_1",
                date = daysAgoTimestamp(5),
                cashRevenue = 100_000L,
                qrisRevenue = 50_000L,
                totalSold = 30,
                createdBy = "staff_1"
            )
        }
        val reportRepo = DailyReportRepository(reportDs)

        val vm = OutletViewModel(
            outletRepository = OutletRepository(outletDs),
            userRepository = UserRepository(userDs),
            materialRepository = MaterialRepository(FakeMaterialRemoteDataSource()),
            productRepository = ProductRepository(FakeProductRemoteDataSource()),
            distributionRepository = MaterialDistributionRepository(FakeMaterialDistributionRemoteDataSource()),
            dailyReportRepository = reportRepo,
            authRepository = object : AuthRepository() {}
        )

        // Verify history item resolves sender name and marks report > 3 days as not editable
        val historyItem = vm.state.value.reportHistoryItems.firstOrNull { it.id == "rep_old" }
        assertNotNull(historyItem)
        assertEquals("Dimas Pratama", historyItem?.createdByName)
        assertFalse(historyItem!!.isEditable)

        // Frontend/ViewModel guard rejects loading report > 3 days for edit
        vm.onEvent(OutletUiEvent.LoadExistingReportForEdit("rep_old"))
        assertNull(vm.state.value.reportForm.editingReportId)
        assertNotNull(vm.state.value.reportForm.errorMessage)

        // Backend repository guard also rejects updating report > 3 days
        val backendUpdateRes = reportRepo.createDailyReport(
            report = reportDs.reports["rep_old"]!!.copy(cashRevenue = 999_000L)
        )
        assertTrue(backendUpdateRes.isFailure)
    }

    @Test
    fun duplicateReportOnSameDateIsRejectedAndTriggersConfirmationThenOverwritesSameId() = runTest {
        Dispatchers.setMain(UnconfinedTestDispatcher(testScheduler))

        val outletDs = FakeOutletRemoteDataSource().apply {
            outlets["outlet_1"] = Outlet(id = "outlet_1", name = "Sudirman", address = "Jl. Sudirman", isActive = true)
        }
        val userDs = FakeUserRemoteDataSource().apply {
            users["staff_1"] = User(id = "staff_1", name = "Dimas", email = "staff@sudutbulat.id", role = Role.OUTLET, outletId = "outlet_1")
        }
        val productDs = FakeProductRemoteDataSource().apply {
            products["prod_1"] = Product(id = "prod_1", name = "Tahu Original", price = 5000L, isActive = true)
        }
        val reportDs = FakeDailyReportRemoteDataSource().apply {
            reports["rep_today"] = DailyReport(
                id = "rep_today",
                outletId = "outlet_1",
                date = Timestamp.now(),
                cashRevenue = 100_000L,
                qrisRevenue = 50_000L,
                totalSold = 30
            )
        }

        val vm = OutletViewModel(
            outletRepository = OutletRepository(outletDs),
            userRepository = UserRepository(userDs),
            materialRepository = MaterialRepository(FakeMaterialRemoteDataSource()),
            productRepository = ProductRepository(productDs),
            distributionRepository = MaterialDistributionRepository(FakeMaterialDistributionRemoteDataSource()),
            dailyReportRepository = DailyReportRepository(reportDs),
            authRepository = object : AuthRepository() {}
        )

        assertNotNull(vm.state.value.todayReport)
        assertEquals("rep_today", vm.state.value.todayReport?.id)

        // 1. Attempt creating a new report for today -> rejected and prompts confirmation alert
        vm.onEvent(OutletUiEvent.UpdateCashRevenue("200000"))
        vm.onEvent(OutletUiEvent.SubmitDailyReport)

        assertEquals(1, reportDs.reports.size)
        assertEquals(100_000L, reportDs.reports["rep_today"]?.cashRevenue)
        assertEquals("rep_today", vm.state.value.reportForm.pendingDuplicateReport?.id)

        // 2. Confirm overwrite/update -> updates the exact same document ID without duplicating
        vm.onEvent(OutletUiEvent.ConfirmOverwriteDuplicateReport("rep_today"))

        assertEquals(1, reportDs.reports.size)
        assertEquals(200_000L, reportDs.reports["rep_today"]?.cashRevenue)
        assertNull(vm.state.value.reportForm.pendingDuplicateReport)
    }

    @Test
    fun appContainerFactoryCreatesAllRoleViewModels() {
        // Use StandardTestDispatcher so init coroutines stay queued and do not hit real Firestore in JVM tests
        Dispatchers.setMain(StandardTestDispatcher())
        val factory = DefaultAppContainer().viewModelFactory

        assertEquals(OwnerViewModel::class.java, factory.create(OwnerViewModel::class.java, CreationExtras.Empty).javaClass)
        assertEquals(WarehouseViewModel::class.java, factory.create(WarehouseViewModel::class.java, CreationExtras.Empty).javaClass)
        assertEquals(CatalogViewModel::class.java, factory.create(CatalogViewModel::class.java, CreationExtras.Empty).javaClass)
        assertEquals(OutletViewModel::class.java, factory.create(OutletViewModel::class.java, CreationExtras.Empty).javaClass)
    }
}
