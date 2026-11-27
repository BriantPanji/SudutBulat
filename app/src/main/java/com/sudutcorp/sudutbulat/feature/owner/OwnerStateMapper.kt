package com.sudutcorp.sudutbulat.feature.owner

import com.sudutcorp.sudutbulat.core.util.buildInitials
import com.sudutcorp.sudutbulat.core.util.formatRupiah
import com.sudutcorp.sudutbulat.data.model.ExpenseCategory
import com.sudutcorp.sudutbulat.data.model.Material
import com.sudutcorp.sudutbulat.data.model.Product
import com.sudutcorp.sudutbulat.data.model.ProductSaleType
import com.sudutcorp.sudutbulat.data.model.Role
import com.sudutcorp.sudutbulat.data.model.UnitType
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.Locale

private val ID_LOCALE: Locale = Locale.forLanguageTag("id-ID")
private val REPORT_DATE_CHIP_FORMATTER: DateTimeFormatter =
    DateTimeFormatter.ofPattern("dd MMM yyyy", ID_LOCALE)

fun computeOwnerDerivedState(raw: OwnerUiState): OwnerUiState {
    val activeOutlets = raw.outlets.ifEmpty { defaultSeededOutlets() }
    val activeUsers = raw.users.ifEmpty { defaultSeededUsers() }
    val activeReports = raw.reports.ifEmpty { defaultSeededReports() }
    val activeExpenses = raw.expenses
    val activeDamaged = raw.damagedMaterials
    val activeMaterials = raw.materials.ifEmpty { defaultSeededMaterials() }
    val activeProducts = raw.products.ifEmpty { defaultSeededProducts() }

    val outletMap = activeOutlets.associateBy { it.id }
    val reportsByOutlet = activeReports.groupBy { it.outletId }
    val reportIdToOutletId = activeReports.associate { it.id to it.outletId }

    val outletModels = activeOutlets.map { outlet ->
        val latestReport = reportsByOutlet[outlet.id]?.firstOrNull()
        val revenue = when {
            outlet.totalRevenue > 0L -> outlet.totalRevenue
            latestReport != null -> latestReport.totalRevenue
            else -> 0L
        }
        val cashRev = latestReport?.cashRevenue ?: (revenue * 60 / 100)
        val qrisRev = latestReport?.qrisRevenue ?: (revenue - cashRev)
        val pieces = when {
            outlet.totalPiecesSold > 0 -> outlet.totalPiecesSold
            latestReport != null -> latestReport.totalSold
            else -> 0
        }
        val dailyTarget = if (outlet.dailyTarget > 0L) outlet.dailyTarget else 2_500_000L
        val rawTarget = if (outlet.targetPercentage > 0f) {
            if (outlet.targetPercentage > 1f) outlet.targetPercentage / 100f else outlet.targetPercentage
        } else {
            (revenue.toFloat() / dailyTarget.toFloat()).coerceIn(0f, 1f)
        }
        val progress = rawTarget.coerceIn(0f, 1f)

        val assignedStaff = activeUsers.filter {
            it.isActive && it.role == Role.OUTLET && it.outletId == outlet.id
        }
        val outletExpenses = activeExpenses.filter { reportIdToOutletId[it.dailyReportId] == outlet.id }
        val expenseTotal = if (outletExpenses.isNotEmpty()) {
            outletExpenses.sumOf { it.amount }
        } else {
            when (outlet.id) {
                "outlet_sudirman" -> 185_000L
                "outlet_gajah_mada" -> 210_000L
                "outlet_cikini" -> 165_000L
                else -> 140_000L
            }
        }
        val outletDamaged = activeDamaged.filter { reportIdToOutletId[it.dailyReportId] == outlet.id }
        val damagedQty = if (outletDamaged.isNotEmpty()) {
            outletDamaged.sumOf { it.quantity }
        } else {
            if (outlet.id == "outlet_sudirman" || outlet.id == "outlet_cikini") 12 else 4
        }

        OutletPerformanceUi(
            id = outlet.id,
            rank = 0,
            name = outlet.name.removePrefix("Outlet ").trim(),
            displayName = if (outlet.name.startsWith("Outlet", ignoreCase = true)) outlet.name else "Outlet ${outlet.name}",
            address = outlet.address,
            coordinateLabel = String.format(Locale.US, "%.4f, %.4f", outlet.coordinate.latitude, outlet.coordinate.longitude),
            isOpen = outlet.isOpen,
            isActive = outlet.isActive,
            openTime = outlet.openTime.ifBlank { "09.00" },
            closeTime = outlet.closeTime.ifBlank { "21.00" },
            dailyTarget = dailyTarget,
            revenue = revenue,
            cashRevenue = cashRev,
            qrisRevenue = qrisRev,
            piecesSold = pieces,
            targetProgress = progress,
            targetPercentText = "${(progress * 100).toInt()}% target",
            activeStaffCount = assignedStaff.size,
            staffIds = assignedStaff.map { it.id },
            staffNames = assignedStaff.map { it.name },
            expenseTotal = expenseTotal,
            expenseCount = outletExpenses.size.takeIf { it > 0 } ?: 3,
            damagedCount = damagedQty,
            damagedReportCount = outletDamaged.size.takeIf { it > 0 } ?: 1,
            netDepositEstimate = (revenue - expenseTotal).coerceAtLeast(0L)
        )
    }

    val rankedByRevenue = outletModels
        .sortedByDescending { it.revenue }
        .mapIndexed { index, item -> item.copy(rank = index + 1) }

    val sortedOutlets = sortOutletItems(rankedByRevenue, raw.outletSortField, raw.outletSortDirection)

    val staffList = activeUsers
        .filter { it.role != Role.DEV }
        .map { user ->
            val assignedOutlet = user.outletId?.let { outletMap[it] }
            StaffItemUi(
                id = user.id,
                name = user.name,
                initials = buildInitials(user.name),
                email = user.email,
                username = user.username.ifBlank { user.id },
                role = user.role,
                outletId = user.outletId,
                outletName = assignedOutlet?.let {
                    if (it.name.startsWith("Outlet", ignoreCase = true)) it.name else "Outlet ${it.name}"
                },
                isActive = user.isActive
            )
        }
        .sortedWith(compareByDescending<StaffItemUi> { it.isActive }.thenBy { it.name.lowercase(Locale.ROOT) })

    val dateFmt = SimpleDateFormat("dd MMM yyyy, HH.mm", ID_LOCALE)
    val expensesByReport = activeExpenses.groupBy { it.dailyReportId }
    val damagedByReport = activeDamaged.groupBy { it.dailyReportId }

    val reportList = activeReports
        .sortedByDescending { it.date.seconds }
        .map { report ->
            val outletName = outletMap[report.outletId]?.let {
                if (it.name.startsWith("Outlet", ignoreCase = true)) it.name else "Outlet ${it.name}"
            } ?: report.outletId
            val repExpenses = expensesByReport[report.id].orEmpty()
            val repDamaged = damagedByReport[report.id].orEmpty()
            val dmgSum = repDamaged.sumOf { it.quantity }
            val millis = report.date.toDate().time

            DailyReportListItemUi(
                id = report.id,
                outletId = report.outletId,
                outletName = outletName,
                dateMillis = millis,
                dateLabel = dateFmt.format(Date(millis)),
                cashRevenue = report.cashRevenue,
                qrisRevenue = report.qrisRevenue,
                totalRevenue = report.totalRevenue,
                totalSold = report.totalSold,
                totalExpense = repExpenses.sumOf { it.amount },
                damagedCount = dmgSum,
                hasAlert = dmgSum >= 10 || repExpenses.any { it.category == ExpenseCategory.OTHER && it.amount >= 200_000L }
            )
        }

    return raw.copy(
        outletItems = sortedOutlets,
        staffItems = staffList,
        reportListItems = reportList,
        todayTotalRevenue = rankedByRevenue.sumOf { it.revenue }.takeIf { it > 0L } ?: 8_420_000L,
        activeOutletsCount = rankedByRevenue.size,
        openOutletsCount = rankedByRevenue.count { it.isOpen },
        closedOutletsCount = rankedByRevenue.count { !it.isOpen },
        todayTotalSold = rankedByRevenue.sumOf { it.piecesSold }.takeIf { it > 0 } ?: 1_684,
        todayTotalExpenses = activeExpenses.sumOf { it.amount }.takeIf { it > 0L } ?: 940_000L,
        todayExpenseTransactionsCount = activeExpenses.size.takeIf { it > 0 } ?: 11,
        catalogItems = buildCatalogItems(activeMaterials, activeProducts),
        addUserForm = raw.addUserForm.copy(
            selectedOutletId = raw.addUserForm.selectedOutletId.ifBlank { rankedByRevenue.firstOrNull()?.id.orEmpty() }
        )
    )
}

fun buildCatalogItems(materials: List<Material>, products: List<Product>): List<CatalogItemUi> {
    val matItems = materials.map { mat ->
        val lower = mat.name.lowercase(Locale.ROOT)
        val category = when {
            lower.contains("tahu") -> "Tahu"
            lower.contains("saus") || lower.contains("chilly") || lower.contains("oil") -> "Saus"
            else -> "Bumbu"
        }
        val unitText = when (mat.unit) {
            UnitType.PCS -> "pcs"
            UnitType.GR -> "gr"
            UnitType.ML -> "ml"
            UnitType.UNIT -> if (lower.contains("bumbu")) "pak" else "botol"
        }
        CatalogItemUi(
            id = mat.id, name = mat.name, category = category, isProduct = false,
            priceOrCost = mat.cost, unit = mat.unit, unitLabel = unitText,
            subtitleText = "Modal ${formatRupiah(mat.cost)} / $unitText",
            statusBadge = if (mat.isActive) category else "Nonaktif",
            isWarning = !mat.isActive,
            isActive = mat.isActive
        )
    }
    val prodItems = products.map { prod ->
        val unitText = if (prod.saleType == ProductSaleType.BUNGKUS) {
            "bungkus (${prod.pcsPerPack} pcs)"
        } else {
            "satuan"
        }
        CatalogItemUi(
            id = prod.id, name = prod.name, category = "Produk Jual", isProduct = true,
            priceOrCost = prod.price,
            unit = UnitType.PCS,
            saleType = prod.saleType,
            pcsPerPack = prod.pcsPerPack,
            unitLabel = unitText,
            subtitleText = "${formatRupiah(prod.price)} / $unitText",
            statusBadge = if (prod.isActive) "Dijual" else "Tidak dijual",
            isWarning = !prod.isActive,
            isActive = prod.isActive
        )
    }
    return matItems + prodItems
}

fun buildSingleReportDetail(state: OwnerUiState, reportId: String): SingleReportDetailUi? {
    val item = state.reportListItems.firstOrNull { it.id == reportId } ?: return null
    val productMap = state.products.ifEmpty { defaultSeededProducts() }.associateBy { it.id }
    val materialMap = state.materials.ifEmpty { defaultSeededMaterials() }.associateBy { it.id }

    val prodLines = state.reportProducts.filter { it.dailyReportId == reportId }.map { rp ->
        ReportProductLineUi(rp.id, productMap[rp.productId]?.name ?: rp.productId, rp.quantity, rp.price, rp.subtotal)
    }
    val expLines = state.expenses.filter { it.dailyReportId == reportId }.map { exp ->
        val label = when (exp.category) {
            ExpenseCategory.GAS -> "Gas & bahan bakar"
            ExpenseCategory.PLASTIC_BAG -> "Plastik & tusuk"
            ExpenseCategory.OTHER -> "Lainnya"
        }
        ReportExpenseLineUi(exp.id, label, exp.amount, exp.note)
    }
    val dmgLines = state.damagedMaterials.filter { it.dailyReportId == reportId }.map { dmg ->
        val mat = materialMap[dmg.materialId]
        val unit = when (mat?.unit) {
            UnitType.UNIT -> "pak"; UnitType.GR -> "gr"; UnitType.ML -> "ml"; else -> "pcs"
        }
        ReportDamagedLineUi(dmg.id, mat?.name ?: dmg.materialId, "${dmg.quantity} $unit", dmg.note)
    }

    val fullDateFmt = SimpleDateFormat("EEEE, dd MMMM yyyy", ID_LOCALE)
    val timeFmt = SimpleDateFormat("HH.mm 'WIB'", ID_LOCALE)
    val dateObj = Date(item.dateMillis)

    return SingleReportDetailUi(
        id = item.id, outletName = item.outletName, dateLabel = item.dateLabel,
        fullDateLabel = fullDateFmt.format(dateObj), timeLabel = timeFmt.format(dateObj),
        cashRevenue = item.cashRevenue, qrisRevenue = item.qrisRevenue,
        totalRevenue = item.totalRevenue, totalSold = item.totalSold,
        productLines = prodLines, expenseLines = expLines,
        totalExpense = expLines.sumOf { it.amount }, damagedLines = dmgLines
    )
}

fun buildReportPeriodSummary(state: OwnerUiState, period: String): ReportPeriodSummaryUi {
    val materialsMap = state.materials.ifEmpty { defaultSeededMaterials() }.associateBy { it.id }
    val gasBase = state.expenses.filter { it.category == ExpenseCategory.GAS }.sumOf { it.amount }
    val plasticBase = state.expenses.filter { it.category == ExpenseCategory.PLASTIC_BAG }.sumOf { it.amount }
    val otherBase = state.expenses.filter { it.category == ExpenseCategory.OTHER }.sumOf { it.amount }

    val damagedSummary = if (state.damagedMaterials.isNotEmpty()) {
        state.damagedMaterials.groupBy { it.materialId }.map { (matId, items) ->
            val mat = materialsMap[matId]
            val unit = when (mat?.unit) {
                UnitType.UNIT -> "pak"; UnitType.GR -> "gr"; UnitType.ML -> "ml"; else -> "pcs"
            }
            DamagedSummaryItemUi(matId, mat?.name ?: "Tahu Original", "${items.sumOf { it.quantity }} $unit")
        }
    } else {
        listOf(
            DamagedSummaryItemUi("mat_tahu_original", "Tahu Original", "86 pcs"),
            DamagedSummaryItemUi("mat_bumbu_balado", "Bumbu", "7 pak")
        )
    }

    return when (period) {
        "7 Hari", "7 hari" -> ReportPeriodSummaryUi(
            "7 hari", "18 – 24 Mei", "Rp42.380.000", "5,2% dibanding periode lalu",
            "Rp42,4jt", "8.476 produk", "8.476 pcs", "Rp5,4jt", "12,7% dari omzet", "Rp14,9jt",
            "Rp23.480.000", "Rp18.900.000",
            if (gasBase > 0) formatRupiah(gasBase * 7) else "Rp2.180.000",
            if (plasticBase > 0) formatRupiah(plasticBase * 7) else "Rp1.640.000",
            if (otherBase > 0) formatRupiah(otherBase * 7) else "Rp1.580.000",
            "Rp5.400.000", damagedSummary
        )
        "3 Bulan", "3 bulan" -> ReportPeriodSummaryUi(
            "3 bulan", "24 Feb – 24 Mei", "Rp538.900.000", "11,3% dibanding periode lalu",
            "Rp538,9jt", "107.780 produk", "107.780 pcs", "Rp66,8jt", "12,4% dari omzet", "Rp181,2jt",
            "Rp298.500.000", "Rp240.400.000", "Rp26.900.000", "Rp20.400.000", "Rp19.500.000",
            "Rp66.800.000", damagedSummary
        )
        else -> ReportPeriodSummaryUi(
            "30 hari", "24 Apr – 24 Mei", "Rp184.650.000", "8,7% dibanding periode lalu",
            "Rp184,6jt", "36.930 produk", "36.930 pcs", "Rp23,7jt", "12,8% dari omzet", "Rp62,4jt",
            "Rp102.340.000", "Rp82.310.000", "Rp9.480.000", "Rp7.120.000", "Rp7.100.000",
            "Rp23.700.000", damagedSummary
        )
    }
}

fun sortOutletItems(
    items: List<OutletPerformanceUi>,
    field: OutletSortField,
    direction: SortDirection
): List<OutletPerformanceUi> {
    val comparator: Comparator<OutletPerformanceUi> = when (field) {
        OutletSortField.PROFIT -> compareBy { it.netDepositEstimate }
        OutletSortField.NAME -> compareBy { it.name.lowercase(Locale.ROOT) }
        OutletSortField.TARGET -> compareBy { it.targetProgress }
    }
    return if (direction == SortDirection.ASC) items.sortedWith(comparator) else items.sortedWith(comparator.reversed())
}

fun sortMaterialCatalogItems(
    items: List<CatalogItemUi>,
    field: MaterialSortField,
    direction: SortDirection
): List<CatalogItemUi> {
    val comparator: Comparator<CatalogItemUi> = when (field) {
        MaterialSortField.NAME -> compareBy { it.name.lowercase(Locale.ROOT) }
        MaterialSortField.COST -> compareBy { it.priceOrCost }
        MaterialSortField.UNIT -> compareBy<CatalogItemUi> { it.unitLabel.lowercase(Locale.ROOT) }
            .thenBy { it.name.lowercase(Locale.ROOT) }
    }
    return if (direction == SortDirection.ASC) items.sortedWith(comparator) else items.sortedWith(comparator.reversed())
}

fun sortProductCatalogItems(
    items: List<CatalogItemUi>,
    field: ProductSortField,
    direction: SortDirection
): List<CatalogItemUi> {
    val comparator: Comparator<CatalogItemUi> = when (field) {
        ProductSortField.NAME -> compareBy { it.name.lowercase(Locale.ROOT) }
        ProductSortField.PRICE -> compareBy { it.priceOrCost }
        ProductSortField.STATUS -> compareBy<CatalogItemUi> { it.isWarning }
            .thenBy { it.name.lowercase(Locale.ROOT) }
    }
    return if (direction == SortDirection.ASC) items.sortedWith(comparator) else items.sortedWith(comparator.reversed())
}

// Material 3 DatePicker returns UTC midnight millis; convert via ZoneOffset.UTC before comparing against local report dates.
fun filterDailyReports(
    items: List<DailyReportListItemUi>,
    searchQuery: String,
    outletFilterId: String?,
    dateFilter: String,
    selectedDateMillis: Long?,
    nowMillis: Long = System.currentTimeMillis(),
    zoneId: ZoneId = ZoneId.systemDefault()
): List<DailyReportListItemUi> {
    val dayMillis = 86_400_000L
    val query = searchQuery.trim().lowercase(Locale.ROOT)
    val selectedLocalDate = selectedDateMillis?.let {
        Instant.ofEpochMilli(it).atZone(ZoneOffset.UTC).toLocalDate()
    }

    return items.filter { item ->
        val matchesOutlet = outletFilterId.isNullOrBlank() || item.outletId == outletFilterId
        val matchesDate = if (selectedLocalDate != null) {
            Instant.ofEpochMilli(item.dateMillis).atZone(zoneId).toLocalDate() == selectedLocalDate
        } else {
            val ageMillis = (nowMillis - item.dateMillis).coerceAtLeast(0L)
            when (dateFilter) {
                "Hari ini" -> ageMillis <= dayMillis
                "7 Hari" -> ageMillis <= 7L * dayMillis
                "30 Hari" -> ageMillis <= 30L * dayMillis
                "3 Bulan" -> ageMillis <= 90L * dayMillis
                else -> true
            }
        }
        val matchesQuery = query.isBlank() || item.outletName.lowercase(Locale.ROOT).contains(query)
        matchesOutlet && matchesDate && matchesQuery
    }
}

fun formatUtcDateMillis(dateMillis: Long): String =
    Instant.ofEpochMilli(dateMillis)
        .atZone(ZoneOffset.UTC)
        .toLocalDate()
        .format(REPORT_DATE_CHIP_FORMATTER)
