package com.sudutcorp.sudutbulat.feature.owner

import com.sudutcorp.sudutbulat.data.auth.AuthRepository
import com.sudutcorp.sudutbulat.data.model.DailyReport
import com.sudutcorp.sudutbulat.data.model.DailyReportDamagedMaterial
import com.sudutcorp.sudutbulat.data.model.DailyReportExpense
import com.sudutcorp.sudutbulat.data.model.DailyReportProduct
import com.sudutcorp.sudutbulat.data.model.DistributionStatus
import com.sudutcorp.sudutbulat.data.model.ExpenseCategory
import com.sudutcorp.sudutbulat.data.model.Material
import com.sudutcorp.sudutbulat.data.model.MaterialDistribution
import com.sudutcorp.sudutbulat.data.model.MaterialDistributionItem
import com.sudutcorp.sudutbulat.data.model.Product
import com.sudutcorp.sudutbulat.data.model.ProductSaleType
import com.sudutcorp.sudutbulat.data.model.Role
import com.sudutcorp.sudutbulat.data.model.UnitType
import com.sudutcorp.sudutbulat.data.model.User
import com.sudutcorp.sudutbulat.data.remote.FirestoreMappers
import com.sudutcorp.sudutbulat.data.remote.FirestoreMappers.toFirestoreItemMap
import com.sudutcorp.sudutbulat.data.remote.FirestoreMappers.toFirestoreMap
import com.sudutcorp.sudutbulat.data.remote.FirestoreMappers.toReportDamagedDocMap
import com.sudutcorp.sudutbulat.data.remote.FirestoreMappers.toReportExpensesDocMap
import com.sudutcorp.sudutbulat.data.remote.FirestoreMappers.toReportProductsDocMap
import com.sudutcorp.sudutbulat.data.repository.FakeMaterialRemoteDataSource
import com.sudutcorp.sudutbulat.data.repository.FakeOutletRemoteDataSource
import com.sudutcorp.sudutbulat.data.repository.FakeProductRemoteDataSource
import com.sudutcorp.sudutbulat.data.repository.FakeUserRemoteDataSource
import com.sudutcorp.sudutbulat.data.repository.MaterialRepository
import com.sudutcorp.sudutbulat.data.repository.OutletRepository
import com.sudutcorp.sudutbulat.data.repository.ProductRepository
import com.sudutcorp.sudutbulat.data.repository.UserRepository
import com.sudutcorp.sudutbulat.feature.stock.CatalogUiEvent
import com.sudutcorp.sudutbulat.feature.stock.CatalogViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

private class FakeAuthRepository(var shouldFailRegister: Boolean = false) : AuthRepository() {
    override suspend fun registerUserAccount(email: String, password: String): Result<String> = runCatching {
        if (shouldFailRegister) throw IllegalStateException("Email sudah terdaftar di Firebase Auth.")
        "uid_${email.substringBefore('@')}"
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
class BackendPersistenceTest {

    @Before
    fun setUpMainDispatcher() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @After
    fun tearDownMainDispatcher() {
        Dispatchers.resetMain()
    }

    @Test
    fun firestoreMappersWriteCleanLowercaseEnumsAndNoComputedGetters() {
        val userMap = User(
            id = "staff_andi",
            name = "Andi",
            email = "andi@sudutbulat.id",
            username = "andi",
            role = Role.OUTLET,
            outletId = "outlet_sudirman",
            isActive = true,
            authUid = "firebase_uid_1"
        ).toFirestoreMap()
        assertEquals("outlet", userMap["role"])
        assertEquals("firebase_uid_1", userMap["uid"])
        assertEquals("outlet_sudirman", userMap["outlet_id"])

        val materialMap = Material("mat_1", "Tahu", 500L, UnitType.PCS, isActive = false).toFirestoreMap()
        assertEquals("pcs", materialMap["unit"])
        assertEquals(false, materialMap["is_active"])

        val satuanMap = Product("p1", "Tahu Ori", 1000L, ProductSaleType.SATUAN, 5, true).toFirestoreMap()
        assertEquals("satuan", satuanMap["sale_type"])
        assertEquals(1, satuanMap["pcs_per_pack"])

        val bungkusMap = Product("p2", "Tahu Ori Bungkus", 5000L, ProductSaleType.BUNGKUS, 5, true).toFirestoreMap()
        assertEquals("bungkus", bungkusMap["sale_type"])
        assertEquals(5, bungkusMap["pcs_per_pack"])

        assertEquals(ProductSaleType.SATUAN, ProductSaleType.fromString("SATUAN"))
        assertEquals(ProductSaleType.BUNGKUS, ProductSaleType.fromString("bungkus"))

        val reportMap = DailyReport("r1", "o1", cashRevenue = 1000L, qrisRevenue = 500L, totalSold = 3).toFirestoreMap()
        assertFalse(reportMap.containsKey("totalRevenue"))

        val rpItemMap = DailyReportProduct("rp1", "r1", "p1", 2, 5000L).toFirestoreItemMap()
        assertFalse(rpItemMap.containsKey("subtotal"))
        assertFalse(rpItemMap.containsKey("daily_report_id"))

        val rpDocMap = listOf(
            DailyReportProduct("rp1", "r1", "p1", 2, 5000L),
            DailyReportProduct("rp2", "r1", "p2", 4, 6000L)
        ).toReportProductsDocMap("r1")
        assertEquals("r1", rpDocMap["daily_report_id"])
        assertEquals(2, (rpDocMap["items"] as List<*>).size)

        val expDocMap = listOf(
            DailyReportExpense("e1", "r1", ExpenseCategory.GAS, 22000L, "Gas 3kg")
        ).toReportExpensesDocMap("r1")
        assertEquals("r1", expDocMap["daily_report_id"])
        assertEquals(1, (expDocMap["items"] as List<*>).size)

        val dmgDocMap = listOf(
            DailyReportDamagedMaterial("d1", "r1", "m1", 5, "Pecah")
        ).toReportDamagedDocMap("r1")
        assertEquals("r1", dmgDocMap["daily_report_id"])
        assertEquals(1, (dmgDocMap["items"] as List<*>).size)

        val distItemMap = MaterialDistributionItem("di1", "d1", "m1", 10, 8).toFirestoreItemMap()
        assertFalse(distItemMap.containsKey("quantityDifference"))
        assertFalse(distItemMap.containsKey("distribution_id"))

        val distMap = MaterialDistribution(
            id = "d1",
            outletId = "o1",
            createdBy = "gudang",
            items = listOf(MaterialDistributionItem("di1", "d1", "m1", 10, 8))
        ).toFirestoreMap()
        assertEquals(1, (distMap["items"] as List<*>).size)

        assertEquals(UnitType.PCS, UnitType.fromString("pcs"))
        assertEquals(UnitType.GR, UnitType.fromString("GR"))
        assertEquals(DistributionStatus.SENT, FirestoreMappers.parseDistributionStatus("sent"))
        assertEquals(ExpenseCategory.PLASTIC_BAG, FirestoreMappers.parseExpenseCategory("plastic_bag"))
    }

    @Test
    fun submitNewOutletFailsLoudlyWithoutFakingLocalState() = runBlocking {
        val outletDs = FakeOutletRemoteDataSource().apply { shouldFail = true }
        val userDs = FakeUserRemoteDataSource()
        val state = MutableStateFlow(
            OwnerUiState(
                addOutletForm = AddOutletFormState(
                    name = "Outlet Kemang",
                    address = "Jl. Kemang No. 1",
                    openTime = "0800",
                    closeTime = "2100",
                    dailyTargetInput = "2500000"
                )
            )
        )
        val effects = Channel<OwnerUiEffect>(Channel.BUFFERED)
        val handler = OwnerActionsHandler(
            outletRepository = OutletRepository(outletDs),
            userRepository = UserRepository(userDs),
            authRepository = FakeAuthRepository(),
            state = state,
            effects = effects
        )

        handler.submitNewOutlet()

        assertTrue(state.value.outlets.isEmpty())
        assertFalse(state.value.addOutletForm.isSaving)
        assertNotNull(state.value.addOutletForm.errorMessage)
        val firstEffect = effects.tryReceive().getOrNull()
        assertTrue(firstEffect is OwnerUiEffect.ShowSnackbar)
        assertNull(effects.tryReceive().getOrNull()) // Must NOT emit NavigateBack on failure
    }

    @Test
    fun submitNewUserFailsWhenAuthFailsAndSucceedsWhenBothPass() = runBlocking {
        val userDs = FakeUserRemoteDataSource()
        val authRepo = FakeAuthRepository(shouldFailRegister = true)
        val state = MutableStateFlow(
            OwnerUiState(
                addUserForm = AddUserFormState(
                    selectedRole = Role.WAREHOUSE,
                    fullName = "Sari Dewi",
                    emailOrPhone = "sari@sudutbulat.id",
                    password = "password123"
                )
            )
        )
        val effects = Channel<OwnerUiEffect>(Channel.BUFFERED)
        val handler = OwnerActionsHandler(
            outletRepository = OutletRepository(FakeOutletRemoteDataSource()),
            userRepository = UserRepository(userDs),
            authRepository = authRepo,
            state = state,
            effects = effects
        )

        // 1. Fail on Auth
        handler.submitNewUser()
        assertTrue(userDs.users.isEmpty())
        assertNotNull(state.value.addUserForm.errorMessage)
        assertTrue(effects.tryReceive().getOrNull() is OwnerUiEffect.ShowSnackbar)

        // 2. Succeed on Auth + Firestore
        authRepo.shouldFailRegister = false
        handler.submitNewUser()
        assertEquals(1, userDs.users.size)
        val saved = userDs.users.values.first()
        assertEquals("uid_sari", saved.authUid)
        assertEquals(Role.WAREHOUSE, saved.role)
        assertTrue(effects.tryReceive().getOrNull() is OwnerUiEffect.ShowSnackbar)
        assertEquals(OwnerUiEffect.NavigateBack, effects.tryReceive().getOrNull())
    }

    @Test
    fun submitCatalogProductValidatesPackSizeAndPersistsSaleType() = runBlocking {
        val productDs = FakeProductRemoteDataSource()
        val vm = CatalogViewModel(
            materialRepository = MaterialRepository(FakeMaterialRemoteDataSource()),
            productRepository = ProductRepository(productDs)
        )

        // 1. Reject bungkus with < 2 pcs
        vm.onEvent(CatalogUiEvent.TypeChanged(true))
        vm.onEvent(CatalogUiEvent.NameChanged("Tahu Keju Bungkus"))
        vm.onEvent(CatalogUiEvent.PriceChanged("7000"))
        vm.onEvent(CatalogUiEvent.SaleTypeChanged(ProductSaleType.BUNGKUS))
        vm.onEvent(CatalogUiEvent.PcsPerPackChanged("1"))
        vm.submitCatalogItem()
        assertTrue(productDs.products.isEmpty())
        assertNotNull(vm.state.value.addCatalogForm.errorMessage)

        // 2. Accept bungkus with 5 pcs
        vm.onEvent(CatalogUiEvent.PcsPerPackChanged("5"))
        vm.submitCatalogItem()
        val savedPack = productDs.products.values.firstOrNull { it.name == "Tahu Keju Bungkus" }
        assertNotNull(savedPack)
        assertEquals(ProductSaleType.BUNGKUS, savedPack?.saleType)
        assertEquals(5, savedPack?.pcsPerPack)

        // 3. Satuan always persists pcsPerPack = 1
        vm.onEvent(CatalogUiEvent.ResetForm(defaultIsProduct = true))
        vm.onEvent(CatalogUiEvent.NameChanged("Tahu Ori Satuan"))
        vm.onEvent(CatalogUiEvent.PriceChanged("1000"))
        vm.onEvent(CatalogUiEvent.SaleTypeChanged(ProductSaleType.SATUAN))
        vm.onEvent(CatalogUiEvent.PcsPerPackChanged("9"))
        vm.submitCatalogItem()
        val savedUnit = productDs.products.values.firstOrNull { it.name == "Tahu Ori Satuan" }
        assertNotNull(savedUnit)
        assertEquals(ProductSaleType.SATUAN, savedUnit?.saleType)
        assertEquals(1, savedUnit?.pcsPerPack)
    }
}
