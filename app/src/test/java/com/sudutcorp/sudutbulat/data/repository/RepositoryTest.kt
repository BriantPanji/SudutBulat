package com.sudutcorp.sudutbulat.data.repository

import com.google.firebase.Timestamp
import com.google.firebase.firestore.GeoPoint
import com.sudutcorp.sudutbulat.data.AppContainer
import com.sudutcorp.sudutbulat.data.DefaultAppContainer
import com.sudutcorp.sudutbulat.data.model.DailyReport
import com.sudutcorp.sudutbulat.data.model.DailyReportDamagedMaterial
import com.sudutcorp.sudutbulat.data.model.DailyReportExpense
import com.sudutcorp.sudutbulat.data.model.DailyReportProduct
import com.sudutcorp.sudutbulat.data.model.DistributionStatus
import com.sudutcorp.sudutbulat.data.model.DistributionWithItems
import com.sudutcorp.sudutbulat.data.model.ExpenseCategory
import com.sudutcorp.sudutbulat.data.model.FullDailyReport
import com.sudutcorp.sudutbulat.data.model.Material
import com.sudutcorp.sudutbulat.data.model.MaterialDistribution
import com.sudutcorp.sudutbulat.data.model.MaterialDistributionItem
import com.sudutcorp.sudutbulat.data.model.Outlet
import com.sudutcorp.sudutbulat.data.model.Product
import com.sudutcorp.sudutbulat.data.model.Role
import com.sudutcorp.sudutbulat.data.model.UnitType
import com.sudutcorp.sudutbulat.data.model.User
import com.sudutcorp.sudutbulat.data.remote.DailyReportRemoteDataSource
import com.sudutcorp.sudutbulat.data.remote.MaterialDistributionRemoteDataSource
import com.sudutcorp.sudutbulat.data.remote.MaterialRemoteDataSource
import com.sudutcorp.sudutbulat.data.remote.OutletRemoteDataSource
import com.sudutcorp.sudutbulat.data.remote.ProductRemoteDataSource
import com.sudutcorp.sudutbulat.data.remote.UserRemoteDataSource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

// ==========================================
// FAKE REMOTE DATA SOURCES
// ==========================================

class FakeUserRemoteDataSource : UserRemoteDataSource() {
    val users = mutableMapOf<String, User>()
    var shouldFail: Boolean = false

    override suspend fun getUserById(userId: String): Result<User?> = runCatching {
        if (shouldFail) throw RuntimeException("Simulated network failure")
        users[userId]
    }

    override fun getUserFlow(userId: String): Flow<User?> = flowOf(users[userId])

    override fun getAllUsersFlow(): Flow<List<User>> = flowOf(users.values.toList())

    override suspend fun getAllUsers(): Result<List<User>> = runCatching {
        if (shouldFail) throw RuntimeException("Simulated network failure")
        users.values.toList()
    }

    override suspend fun getUsersByRole(role: Role): Result<List<User>> = runCatching {
        if (shouldFail) throw RuntimeException("Simulated network failure")
        users.values.filter { it.role == role }
    }

    override suspend fun getUsersByOutlet(outletId: String): Result<List<User>> = runCatching {
        if (shouldFail) throw RuntimeException("Simulated network failure")
        users.values.filter { it.outletId == outletId }
    }

    override suspend fun saveUser(user: User): Result<Unit> = runCatching {
        if (shouldFail) throw RuntimeException("Simulated network failure")
        val id = if (user.id.isBlank()) "generated_${users.size + 1}" else user.id
        users[id] = user.copy(id = id)
    }

    override suspend fun updateUser(user: User): Result<Unit> = runCatching {
        if (shouldFail) throw RuntimeException("Simulated network failure")
        require(user.id.isNotBlank())
        users[user.id] = user
    }

    override suspend fun setUserActive(userId: String, isActive: Boolean): Result<Unit> = runCatching {
        if (shouldFail) throw RuntimeException("Simulated network failure")
        val existing = users[userId] ?: throw NoSuchElementException()
        users[userId] = existing.copy(isActive = isActive)
    }

    override suspend fun assignUsersToOutlet(userIds: List<String>, outletId: String): Result<Unit> = runCatching {
        if (shouldFail) throw RuntimeException("Simulated network failure")
        userIds.forEach { uid ->
            val existing = users[uid] ?: return@forEach
            users[uid] = existing.copy(outletId = outletId)
        }
    }

    override suspend fun setUserOutlet(userId: String, outletId: String?): Result<Unit> = runCatching {
        if (shouldFail) throw RuntimeException("Simulated network failure")
        val existing = users[userId] ?: throw NoSuchElementException()
        users[userId] = existing.copy(outletId = outletId)
    }
}

class FakeOutletRemoteDataSource : OutletRemoteDataSource() {
    val outlets = mutableMapOf<String, Outlet>()
    var shouldFail: Boolean = false

    override fun getOutletsFlow(onlyActive: Boolean): Flow<List<Outlet>> =
        flowOf(outlets.values.filter { !onlyActive || it.isActive })

    override suspend fun getOutlets(onlyActive: Boolean): Result<List<Outlet>> = runCatching {
        if (shouldFail) throw RuntimeException("Simulated network failure")
        outlets.values.filter { !onlyActive || it.isActive }
    }

    override suspend fun getOutletById(outletId: String): Result<Outlet?> = runCatching {
        if (shouldFail) throw RuntimeException("Simulated network failure")
        outlets[outletId]
    }

    override fun getOutletFlow(outletId: String): Flow<Outlet?> = flowOf(outlets[outletId])

    override suspend fun saveOutlet(outlet: Outlet): Result<String> = runCatching {
        if (shouldFail) throw RuntimeException("Simulated network failure")
        val id = if (outlet.id.isBlank()) "generated_out_${outlets.size + 1}" else outlet.id
        outlets[id] = outlet.copy(id = id)
        id
    }

    override suspend fun updateOutlet(outlet: Outlet): Result<Unit> = runCatching {
        if (shouldFail) throw RuntimeException("Simulated network failure")
        require(outlet.id.isNotBlank())
        outlets[outlet.id] = outlet
    }

    override suspend fun setOutletActive(outletId: String, isActive: Boolean): Result<Unit> = runCatching {
        if (shouldFail) throw RuntimeException("Simulated network failure")
        val existing = outlets[outletId] ?: throw NoSuchElementException()
        outlets[outletId] = existing.copy(isActive = isActive)
    }

    override suspend fun setOutletOpen(outletId: String, isOpen: Boolean): Result<Unit> = runCatching {
        if (shouldFail) throw RuntimeException("Simulated network failure")
        val existing = outlets[outletId] ?: throw NoSuchElementException()
        outlets[outletId] = existing.copy(isOpen = isOpen)
    }
}

class FakeMaterialRemoteDataSource : MaterialRemoteDataSource() {
    val materials = mutableMapOf<String, Material>()
    var shouldFail: Boolean = false

    override fun getMaterialsFlow(): Flow<List<Material>> = flowOf(materials.values.toList())

    override suspend fun getMaterials(): Result<List<Material>> = runCatching {
        if (shouldFail) throw RuntimeException("Simulated network failure")
        materials.values.toList()
    }

    override suspend fun getMaterialById(materialId: String): Result<Material?> = runCatching {
        if (shouldFail) throw RuntimeException("Simulated network failure")
        materials[materialId]
    }

    override suspend fun saveMaterial(material: Material): Result<String> = runCatching {
        if (shouldFail) throw RuntimeException("Simulated network failure")
        val id = if (material.id.isBlank()) "mat_${materials.size + 1}" else material.id
        materials[id] = material.copy(id = id)
        id
    }

    override suspend fun updateMaterial(material: Material): Result<Unit> = runCatching {
        if (shouldFail) throw RuntimeException("Simulated network failure")
        require(material.id.isNotBlank())
        materials[material.id] = material
    }

    override suspend fun deleteMaterial(materialId: String): Result<Unit> = runCatching {
        if (shouldFail) throw RuntimeException("Simulated network failure")
        materials.remove(materialId)
    }
}

class FakeProductRemoteDataSource : ProductRemoteDataSource() {
    val products = mutableMapOf<String, Product>()
    var shouldFail: Boolean = false

    override fun getProductsFlow(onlyActive: Boolean): Flow<List<Product>> =
        flowOf(products.values.filter { !onlyActive || it.isActive })

    override suspend fun getProducts(onlyActive: Boolean): Result<List<Product>> = runCatching {
        if (shouldFail) throw RuntimeException("Simulated network failure")
        products.values.filter { !onlyActive || it.isActive }
    }

    override suspend fun getActiveProducts(): Result<List<Product>> = getProducts(onlyActive = true)

    override suspend fun getProductById(productId: String): Result<Product?> = runCatching {
        if (shouldFail) throw RuntimeException("Simulated network failure")
        products[productId]
    }

    override suspend fun saveProduct(product: Product): Result<String> = runCatching {
        if (shouldFail) throw RuntimeException("Simulated network failure")
        val id = if (product.id.isBlank()) "prod_${products.size + 1}" else product.id
        products[id] = product.copy(id = id)
        id
    }

    override suspend fun updateProduct(product: Product): Result<Unit> = runCatching {
        if (shouldFail) throw RuntimeException("Simulated network failure")
        require(product.id.isNotBlank())
        products[product.id] = product
    }

    override suspend fun setProductActive(productId: String, isActive: Boolean): Result<Unit> = runCatching {
        if (shouldFail) throw RuntimeException("Simulated network failure")
        val existing = products[productId] ?: throw NoSuchElementException()
        products[productId] = existing.copy(isActive = isActive)
    }

    override suspend fun deleteProduct(productId: String): Result<Unit> = runCatching {
        if (shouldFail) throw RuntimeException("Simulated network failure")
        products.remove(productId)
    }
}

class FakeMaterialDistributionRemoteDataSource : MaterialDistributionRemoteDataSource() {
    val distributions = mutableMapOf<String, MaterialDistribution>()
    val distributionItems = mutableMapOf<String, MutableList<MaterialDistributionItem>>()
    var shouldFail: Boolean = false

    override fun getDistributionsFlow(outletId: String?): Flow<List<MaterialDistribution>> = flowOf(
        distributions.values.filter { outletId == null || it.outletId == outletId }
    )

    override suspend fun getDistributions(
        outletId: String?,
        status: DistributionStatus?
    ): Result<List<MaterialDistribution>> = runCatching {
        if (shouldFail) throw RuntimeException("Simulated network failure")
        distributions.values.filter {
            (outletId == null || it.outletId == outletId) &&
            (status == null || it.status == status)
        }
    }

    override suspend fun getDistributionById(id: String): Result<MaterialDistribution?> = runCatching {
        if (shouldFail) throw RuntimeException("Simulated network failure")
        distributions[id]
    }

    override fun getDistributionFlow(id: String): Flow<MaterialDistribution?> = flowOf(distributions[id])

    override suspend fun getDistributionItems(distributionId: String): Result<List<MaterialDistributionItem>> = runCatching {
        if (shouldFail) throw RuntimeException("Simulated network failure")
        distributionItems[distributionId] ?: emptyList()
    }

    override fun getDistributionItemsFlow(distributionId: String): Flow<List<MaterialDistributionItem>> =
        flowOf(distributionItems[distributionId] ?: emptyList())

    override suspend fun createDistribution(
        distribution: MaterialDistribution,
        items: List<MaterialDistributionItem>
    ): Result<String> = runCatching {
        if (shouldFail) throw RuntimeException("Simulated network failure")
        val id = if (distribution.id.isBlank()) "dist_${distributions.size + 1}" else distribution.id
        val savedDist = distribution.copy(id = id)
        distributions[id] = savedDist
        distributionItems[id] = items.mapIndexed { idx, itm ->
            itm.copy(
                id = if (itm.id.isBlank()) "item_${id}_$idx" else itm.id,
                distributionId = id
            )
        }.toMutableList()
        id
    }

    override suspend fun updateDistributionStatus(
        id: String,
        status: DistributionStatus,
        confirmedBy: String?,
        confirmationNote: String?,
        sentAt: Timestamp?,
        confirmedAt: Timestamp?
    ): Result<Unit> = runCatching {
        if (shouldFail) throw RuntimeException("Simulated network failure")
        val dist = distributions[id] ?: throw NoSuchElementException()
        distributions[id] = dist.copy(
            status = status,
            confirmedBy = confirmedBy ?: dist.confirmedBy,
            confirmationNote = confirmationNote ?: dist.confirmationNote,
            sentAt = sentAt ?: dist.sentAt,
            confirmedAt = confirmedAt ?: dist.confirmedAt
        )
    }

    override suspend fun confirmDistribution(
        distributionId: String,
        confirmedBy: String,
        confirmationNote: String?,
        updatedItems: List<MaterialDistributionItem>,
        status: DistributionStatus
    ): Result<Unit> = runCatching {
        if (shouldFail) throw RuntimeException("Simulated network failure")
        val dist = distributions[distributionId] ?: throw NoSuchElementException()
        distributions[distributionId] = dist.copy(
            status = status,
            confirmedBy = confirmedBy,
            confirmationNote = confirmationNote,
            confirmedAt = Timestamp.now()
        )
        val itemsList = distributionItems[distributionId] ?: mutableListOf()
        updatedItems.forEach { updated ->
            val idx = itemsList.indexOfFirst { it.id == updated.id }
            if (idx >= 0) {
                itemsList[idx] = updated
            }
        }
    }
}

class FakeDailyReportRemoteDataSource : DailyReportRemoteDataSource() {
    val reports = mutableMapOf<String, DailyReport>()
    val reportProducts = mutableMapOf<String, MutableList<DailyReportProduct>>()
    val reportExpenses = mutableMapOf<String, MutableList<DailyReportExpense>>()
    val reportDamaged = mutableMapOf<String, MutableList<DailyReportDamagedMaterial>>()
    var shouldFail: Boolean = false

    override fun getDailyReportsFlow(outletId: String?): Flow<List<DailyReport>> = flowOf(
        reports.values.filter { outletId == null || it.outletId == outletId }
    )

    override suspend fun getDailyReports(outletId: String?): Result<List<DailyReport>> = runCatching {
        if (shouldFail) throw RuntimeException("Simulated network failure")
        reports.values.filter { outletId == null || it.outletId == outletId }
    }

    override suspend fun getDailyReportById(id: String): Result<DailyReport?> = runCatching {
        if (shouldFail) throw RuntimeException("Simulated network failure")
        reports[id]
    }

    override fun getDailyReportFlow(id: String): Flow<DailyReport?> = flowOf(reports[id])

    override suspend fun getDailyReportByOutletAndDate(
        outletId: String,
        startDate: Timestamp,
        endDate: Timestamp
    ): Result<DailyReport?> = runCatching {
        if (shouldFail) throw RuntimeException("Simulated network failure")
        reports.values.firstOrNull {
            it.outletId == outletId &&
            it.date.seconds >= startDate.seconds &&
            it.date.seconds <= endDate.seconds
        }
    }

    override suspend fun createDailyReport(
        report: DailyReport,
        products: List<DailyReportProduct>,
        expenses: List<DailyReportExpense>,
        damagedMaterials: List<DailyReportDamagedMaterial>
    ): Result<String> = runCatching {
        if (shouldFail) throw RuntimeException("Simulated network failure")
        val id = if (report.id.isBlank()) "rep_${reports.size + 1}" else report.id
        reports[id] = report.copy(id = id)
        reportProducts[id] = products.map { it.copy(dailyReportId = id) }.toMutableList()
        reportExpenses[id] = expenses.map { it.copy(dailyReportId = id) }.toMutableList()
        reportDamaged[id] = damagedMaterials.map { it.copy(dailyReportId = id) }.toMutableList()
        id
    }

    override suspend fun getDailyReportProducts(dailyReportId: String): Result<List<DailyReportProduct>> = runCatching {
        if (shouldFail) throw RuntimeException("Simulated network failure")
        reportProducts[dailyReportId] ?: emptyList()
    }

    override suspend fun getDailyReportExpenses(dailyReportId: String): Result<List<DailyReportExpense>> = runCatching {
        if (shouldFail) throw RuntimeException("Simulated network failure")
        reportExpenses[dailyReportId] ?: emptyList()
    }

    override suspend fun getDailyReportDamagedMaterials(dailyReportId: String): Result<List<DailyReportDamagedMaterial>> = runCatching {
        if (shouldFail) throw RuntimeException("Simulated network failure")
        reportDamaged[dailyReportId] ?: emptyList()
    }

    override suspend fun addExpense(expense: DailyReportExpense): Result<String> = runCatching {
        if (shouldFail) throw RuntimeException("Simulated network failure")
        val repId = expense.dailyReportId ?: "standalone"
        val list = reportExpenses.getOrPut(repId) { mutableListOf() }
        val id = if (expense.id.isBlank()) "exp_${list.size + 1}" else expense.id
        list.add(expense.copy(id = id))
        id
    }

    override suspend fun addDamagedMaterial(damagedMaterial: DailyReportDamagedMaterial): Result<String> = runCatching {
        if (shouldFail) throw RuntimeException("Simulated network failure")
        val repId = damagedMaterial.dailyReportId
        val list = reportDamaged.getOrPut(repId) { mutableListOf() }
        val id = if (damagedMaterial.id.isBlank()) "dmg_${list.size + 1}" else damagedMaterial.id
        list.add(damagedMaterial.copy(id = id))
        id
    }

    override fun getAllReportProductsFlow(): Flow<List<DailyReportProduct>> =
        flowOf(reportProducts.values.flatten())

    override fun getAllExpensesFlow(): Flow<List<DailyReportExpense>> =
        flowOf(reportExpenses.values.flatten())

    override fun getAllDamagedMaterialsFlow(): Flow<List<DailyReportDamagedMaterial>> =
        flowOf(reportDamaged.values.flatten())
}

// ==========================================
// TEST SUITE
// ==========================================

class RepositoryTest {

    @Test
    fun testAppContainerProvidesAllRepositories() {
        val container: AppContainer = DefaultAppContainer()
        assertNotNull(container.authRepository)
        assertNotNull(container.userRepository)
        assertNotNull(container.outletRepository)
        assertNotNull(container.materialRepository)
        assertNotNull(container.productRepository)
        assertNotNull(container.materialDistributionRepository)
        assertNotNull(container.dailyReportRepository)
    }

    @Test
    fun testUserRepositoryWithFakeDataSource() = runBlocking {
        val fakeDataSource = FakeUserRemoteDataSource()
        val repo = UserRepository(fakeDataSource)

        val user = User(
            id = "usr_1",
            name = "Budi Santoso",
            email = "budi@sudutbulat.com",
            username = "budi_s",
            role = Role.WAREHOUSE,
            outletId = null,
            isActive = true
        )
        val saveResult = repo.saveUser(user)
        assertTrue(saveResult.isSuccess)

        val fetched = repo.getUserById("usr_1")
        assertTrue(fetched.isSuccess)
        assertEquals("Budi Santoso", fetched.getOrNull()?.name)
        assertEquals(Role.WAREHOUSE, fetched.getOrNull()?.role)

        val flowUser = repo.getUserFlow("usr_1").first()
        assertEquals("usr_1", flowUser?.id)

        val byRole = repo.getUsersByRole(Role.WAREHOUSE)
        assertTrue(byRole.isSuccess)
        assertEquals(1, byRole.getOrNull()?.size)

        repo.setUserActive("usr_1", false)
        assertFalse(repo.getUserById("usr_1").getOrNull()!!.isActive)

        // Test failure handling
        fakeDataSource.shouldFail = true
        val failResult = repo.getUserById("usr_1")
        assertTrue(failResult.isFailure)
    }

    @Test
    fun testOutletRepositoryWithFakeDataSource() = runBlocking {
        val fakeDataSource = FakeOutletRemoteDataSource()
        val repo = OutletRepository(fakeDataSource)

        val outlet = Outlet(
            id = "out_1",
            name = "Cabang Medan Merdeka",
            address = "Jl. Merdeka No. 45",
            coordinate = GeoPoint(3.5952, 98.6722),
            targetPercentage = 92.5f,
            totalRevenue = 25_000_000L,
            totalPiecesSold = 1200,
            isActive = true,
            isOpen = true
        )
        val saveRes = repo.saveOutlet(outlet)
        assertTrue(saveRes.isSuccess)
        assertEquals("out_1", saveRes.getOrNull())

        val fetched = repo.getOutletById("out_1")
        assertTrue(fetched.isSuccess)
        val out = fetched.getOrNull()!!
        assertEquals("Cabang Medan Merdeka", out.name)
        assertEquals(3.5952, out.coordinate.latitude, 0.0001)
        assertEquals(25_000_000L, out.totalRevenue)
        assertEquals(1200, out.totalPiecesSold)

        repo.setOutletOpen("out_1", false)
        assertFalse(repo.getOutletById("out_1").getOrNull()!!.isOpen)

        val activeList = repo.getOutlets(onlyActive = true).getOrNull()
        assertEquals(1, activeList?.size)

        val flowList = repo.getOutletsFlow(onlyActive = true).first()
        assertEquals(1, flowList.size)
    }

    @Test
    fun testMaterialRepositoryWithFakeDataSource() = runBlocking {
        val fakeDataSource = FakeMaterialRemoteDataSource()
        val repo = MaterialRepository(fakeDataSource)

        val mat = Material(
            id = "mat_1",
            name = "Susu UHT",
            cost = 18_000L,
            unit = UnitType.ML
        )
        val saveRes = repo.saveMaterial(mat)
        assertTrue(saveRes.isSuccess)

        val fetched = repo.getMaterialById("mat_1")
        assertTrue(fetched.isSuccess)
        assertEquals(18_000L, fetched.getOrNull()?.cost)
        assertEquals(UnitType.ML, fetched.getOrNull()?.unit)

        val deleteRes = repo.deleteMaterial("mat_1")
        assertTrue(deleteRes.isSuccess)
        assertNull(repo.getMaterialById("mat_1").getOrNull())
    }

    @Test
    fun testProductRepositoryWithFakeDataSource() = runBlocking {
        val fakeDataSource = FakeProductRemoteDataSource()
        val repo = ProductRepository(fakeDataSource)

        val prod = Product(
            id = "prod_1",
            name = "Roti Cokelat Keju",
            price = 16_000L,
            isActive = true
        )
        val saveRes = repo.saveProduct(prod)
        assertTrue(saveRes.isSuccess)

        val activeList = repo.getActiveProducts()
        assertTrue(activeList.isSuccess)
        assertEquals(1, activeList.getOrNull()?.size)
        assertEquals(16_000L, activeList.getOrNull()?.first()?.price)

        repo.setProductActive("prod_1", false)
        assertEquals(0, repo.getActiveProducts().getOrNull()?.size)
    }

    @Test
    fun testMaterialDistributionRepositoryWithFakeDataSource() = runBlocking {
        val fakeDataSource = FakeMaterialDistributionRemoteDataSource()
        val repo = MaterialDistributionRepository(fakeDataSource)

        val dist = MaterialDistribution(
            id = "dist_101",
            outletId = "out_1",
            createdBy = "wh_user",
            status = DistributionStatus.DRAFT,
            createdAt = Timestamp.now()
        )
        val item1 = MaterialDistributionItem(
            id = "item_1",
            distributionId = "dist_101",
            materialId = "mat_1",
            quantitySent = 100,
            quantityReceived = null
        )
        assertNull(item1.quantityDifference)

        val createRes = repo.createDistribution(dist, listOf(item1))
        assertTrue(createRes.isSuccess)

        // Test composite model retrieval
        val compositeRes = repo.getDistributionWithItems("dist_101")
        assertTrue(compositeRes.isSuccess)
        val composite = compositeRes.getOrNull()
        assertNotNull(composite)
        assertEquals("dist_101", composite!!.distribution.id)
        assertEquals(1, composite.items.size)
        assertEquals(100, composite.items[0].quantitySent)

        // Confirm distribution
        val confirmedItem = item1.copy(quantityReceived = 98)
        assertEquals(2, confirmedItem.quantityDifference)

        val confirmRes = repo.confirmDistribution(
            distributionId = "dist_101",
            confirmedBy = "outlet_user",
            confirmationNote = "2 pcs bocor di perjalanan",
            updatedItems = listOf(confirmedItem),
            status = DistributionStatus.PARTIALLY_RECEIVED
        )
        assertTrue(confirmRes.isSuccess)

        val updatedComposite = repo.getDistributionWithItems("dist_101").getOrNull()!!
        assertEquals(DistributionStatus.PARTIALLY_RECEIVED, updatedComposite.distribution.status)
        assertEquals("outlet_user", updatedComposite.distribution.confirmedBy)
        assertEquals(98, updatedComposite.items[0].quantityReceived)
    }

    @Test
    fun testDailyReportRepositoryWithFakeDataSource() = runBlocking {
        val fakeDataSource = FakeDailyReportRemoteDataSource()
        val repo = DailyReportRepository(fakeDataSource)

        val report = DailyReport(
            id = "rep_201",
            outletId = "out_1",
            cashRevenue = 1_500_000L,
            qrisRevenue = 1_000_000L,
            totalSold = 200
        )
        assertEquals(2_500_000L, report.totalRevenue)

        val prod1 = DailyReportProduct(
            id = "rp_1",
            dailyReportId = "rep_201",
            productId = "prod_1",
            quantity = 80,
            price = 15_000L
        )
        assertEquals(1_200_000L, prod1.subtotal)

        val expense1 = DailyReportExpense(
            id = "exp_1",
            dailyReportId = "rep_201",
            category = ExpenseCategory.PLASTIC_BAG,
            amount = 30_000L,
            note = "Beli plastik kresek"
        )

        val damaged1 = DailyReportDamagedMaterial(
            id = "dmg_1",
            dailyReportId = "rep_201",
            materialId = "mat_1",
            quantity = 3,
            note = "Tepung apek"
        )

        val createRes = repo.createDailyReport(
            report = report,
            products = listOf(prod1),
            expenses = listOf(expense1),
            damagedMaterials = listOf(damaged1)
        )
        assertTrue(createRes.isSuccess)

        // Test FullDailyReport retrieval
        val fullReportRes = repo.getFullDailyReport("rep_201")
        assertTrue(fullReportRes.isSuccess)
        val fullReport = fullReportRes.getOrNull()
        assertNotNull(fullReport)
        assertEquals("rep_201", fullReport!!.report.id)
        assertEquals(2_500_000L, fullReport.report.totalRevenue)
        assertEquals(1, fullReport.products.size)
        assertEquals(1_200_000L, fullReport.products[0].subtotal)
        assertEquals(1, fullReport.expenses.size)
        assertEquals(ExpenseCategory.PLASTIC_BAG, fullReport.expenses[0].category)
        assertEquals(1, fullReport.damagedMaterials.size)
        assertEquals(3, fullReport.damagedMaterials[0].quantity)

        // Test standalone expense and damaged material
        val exp2 = DailyReportExpense(
            dailyReportId = "rep_201",
            category = ExpenseCategory.GAS,
            amount = 22_000L
        )
        val addExpRes = repo.addExpense(exp2)
        assertTrue(addExpRes.isSuccess)

        val dmg2 = DailyReportDamagedMaterial(
            dailyReportId = "rep_201",
            materialId = "mat_2",
            quantity = 1
        )
        val addDmgRes = repo.addDamagedMaterial(dmg2)
        assertTrue(addDmgRes.isSuccess)
    }

    @Test
    fun testRoleParsingAndResolution() {
        // Firestore role strings
        assertEquals(Role.DEV, Role.fromString("dev"))
        assertEquals(Role.OWNER, Role.fromString("owner"))
        assertEquals(Role.WAREHOUSE, Role.fromString("warehouse"))
        assertEquals(Role.OUTLET, Role.fromString("outlet"))
        assertEquals(Role.DEV, Role.fromString("DEV"))
        assertEquals(Role.WAREHOUSE, Role.fromString("gudang"))
        assertEquals(Role.WAREHOUSE, Role.fromString("admin"))
        assertEquals(Role.OUTLET, Role.fromString("staff"))
        assertNull(Role.fromString("unknown_role"))

        // Official emails & login codes
        assertEquals(Role.DEV, Role.fromEmailOrCode("sdb.dev@gmail.com"))
        assertEquals(Role.OUTLET, Role.fromEmailOrCode("sdb.staff@gmail.com"))
        assertEquals(Role.WAREHOUSE, Role.fromEmailOrCode("sdb.gudang@gmail.com"))
        assertEquals(Role.OWNER, Role.fromEmailOrCode("sdb.owner@gmail.com"))

        assertEquals(Role.DEV, Role.fromEmailOrCode("dev"))
        assertEquals(Role.OWNER, Role.fromEmailOrCode("owner"))
        assertEquals(Role.WAREHOUSE, Role.fromEmailOrCode("gudang"))
        assertEquals(Role.WAREHOUSE, Role.fromEmailOrCode("admin"))
        assertEquals(Role.OUTLET, Role.fromEmailOrCode("staff"))

        // Login code to email resolution
        assertEquals("sdb.dev@gmail.com", Role.emailForLoginCode("dev"))
        assertEquals("sdb.owner@gmail.com", Role.emailForLoginCode("owner"))
        assertEquals("sdb.gudang@gmail.com", Role.emailForLoginCode("gudang"))
        assertEquals("sdb.gudang@gmail.com", Role.emailForLoginCode("admin"))
        assertEquals("sdb.staff@gmail.com", Role.emailForLoginCode("staff"))
        assertNull(Role.emailForLoginCode("unknown"))
    }

    @Test
    fun testAssignUsersToOutletAndOutletExtendedFields() = runBlocking {
        val fakeUserDs = FakeUserRemoteDataSource()
        val userRepo = UserRepository(fakeUserDs)
        val fakeOutletDs = FakeOutletRemoteDataSource()
        val outletRepo = OutletRepository(fakeOutletDs)

        userRepo.saveUser(
            User(
                id = "staff_kemang_1",
                name = "Dimas Prasetyo",
                email = "dimas@sudutbulat.id",
                username = "dimas",
                role = Role.OUTLET
            )
        )
        val savedOutletId = outletRepo.saveOutlet(
            Outlet(
                id = "outlet_kemang",
                name = "Kemang",
                address = "Jl. Kemang Raya No. 12",
                openTime = "08.00",
                closeTime = "21.00",
                dailyTarget = 2_000_000L
            )
        ).getOrThrow()

        userRepo.assignUsersToOutlet(listOf("staff_kemang_1"), savedOutletId)

        val assignedUser = userRepo.getUserById("staff_kemang_1").getOrNull()
        assertEquals("outlet_kemang", assignedUser?.outletId)

        val savedOutlet = outletRepo.getOutletById("outlet_kemang").getOrNull()
        assertNotNull(savedOutlet)
        assertEquals("08.00", savedOutlet?.openTime)
        assertEquals("21.00", savedOutlet?.closeTime)
        assertEquals(2_000_000L, savedOutlet?.dailyTarget)

        // Verify unassigning / reassigning single staff outlet
        userRepo.setUserOutlet("staff_kemang_1", null)
        assertNull(userRepo.getUserById("staff_kemang_1").getOrNull()?.outletId)
    }

    @Test
    fun testSortOutletItemsByProfitNameTargetAscDesc() {
        val sample = listOf(
            com.sudutcorp.sudutbulat.feature.owner.OutletPerformanceUi(
                id = "a", rank = 1, name = "Cikini", displayName = "Outlet Cikini", address = "",
                isOpen = true, isActive = true, openTime = "08.00", closeTime = "21.00", dailyTarget = 2_000_000L,
                revenue = 1_780_000L, cashRevenue = 1_000_000L, qrisRevenue = 780_000L, piecesSold = 356,
                targetProgress = 0.72f, targetPercentText = "72% target", activeStaffCount = 1,
                staffIds = emptyList(), staffNames = emptyList(), expenseTotal = 165_000L, expenseCount = 2,
                damagedCount = 0, damagedReportCount = 0, netDepositEstimate = 1_615_000L
            ),
            com.sudutcorp.sudutbulat.feature.owner.OutletPerformanceUi(
                id = "b", rank = 2, name = "Sudirman", displayName = "Outlet Sudirman", address = "",
                isOpen = true, isActive = true, openTime = "09.00", closeTime = "21.00", dailyTarget = 2_900_000L,
                revenue = 2_480_000L, cashRevenue = 1_480_000L, qrisRevenue = 1_000_000L, piecesSold = 496,
                targetProgress = 0.85f, targetPercentText = "85% target", activeStaffCount = 2,
                staffIds = emptyList(), staffNames = emptyList(), expenseTotal = 185_000L, expenseCount = 3,
                damagedCount = 0, damagedReportCount = 0, netDepositEstimate = 2_295_000L
            ),
            com.sudutcorp.sudutbulat.feature.owner.OutletPerformanceUi(
                id = "c", rank = 3, name = "Blok M", displayName = "Outlet Blok M", address = "",
                isOpen = false, isActive = true, openTime = "09.00", closeTime = "21.00", dailyTarget = 2_000_000L,
                revenue = 720_000L, cashRevenue = 420_000L, qrisRevenue = 300_000L, piecesSold = 144,
                targetProgress = 0.36f, targetPercentText = "36% target", activeStaffCount = 1,
                staffIds = emptyList(), staffNames = emptyList(), expenseTotal = 140_000L, expenseCount = 1,
                damagedCount = 0, damagedReportCount = 0, netDepositEstimate = 580_000L
            )
        )

        val byProfitDesc = com.sudutcorp.sudutbulat.feature.owner.sortOutletItems(
            sample,
            com.sudutcorp.sudutbulat.feature.owner.OutletSortField.PROFIT,
            com.sudutcorp.sudutbulat.feature.owner.SortDirection.DESC
        )
        assertEquals(listOf("Sudirman", "Cikini", "Blok M"), byProfitDesc.map { it.name })

        val byProfitAsc = com.sudutcorp.sudutbulat.feature.owner.sortOutletItems(
            sample,
            com.sudutcorp.sudutbulat.feature.owner.OutletSortField.PROFIT,
            com.sudutcorp.sudutbulat.feature.owner.SortDirection.ASC
        )
        assertEquals(listOf("Blok M", "Cikini", "Sudirman"), byProfitAsc.map { it.name })

        val byNameAsc = com.sudutcorp.sudutbulat.feature.owner.sortOutletItems(
            sample,
            com.sudutcorp.sudutbulat.feature.owner.OutletSortField.NAME,
            com.sudutcorp.sudutbulat.feature.owner.SortDirection.ASC
        )
        assertEquals(listOf("Blok M", "Cikini", "Sudirman"), byNameAsc.map { it.name })

        val byTargetDesc = com.sudutcorp.sudutbulat.feature.owner.sortOutletItems(
            sample,
            com.sudutcorp.sudutbulat.feature.owner.OutletSortField.TARGET,
            com.sudutcorp.sudutbulat.feature.owner.SortDirection.DESC
        )
        assertEquals(listOf("Sudirman", "Cikini", "Blok M"), byTargetDesc.map { it.name })
    }
}
