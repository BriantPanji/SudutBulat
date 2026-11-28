package com.sudutcorp.sudutbulat.feature.owner

import com.sudutcorp.sudutbulat.core.util.buildInitials
import com.sudutcorp.sudutbulat.core.util.formatRupiah
import com.sudutcorp.sudutbulat.data.model.DistributionStatus
import com.sudutcorp.sudutbulat.data.model.DistributionType
import com.sudutcorp.sudutbulat.data.model.ExpenseCategory
import com.sudutcorp.sudutbulat.data.model.Material
import com.sudutcorp.sudutbulat.data.model.MaterialCategory
import com.sudutcorp.sudutbulat.data.model.Product
import com.sudutcorp.sudutbulat.data.model.ProductSaleType
import com.sudutcorp.sudutbulat.data.model.ReportReviewStatus
import com.sudutcorp.sudutbulat.data.model.Role
import com.sudutcorp.sudutbulat.data.model.UnitType
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.Locale
import kotlin.math.abs

private val ID_LOCALE: Locale = Locale.forLanguageTag("id-ID")
private val REPORT_DATE_CHIP_FORMATTER: DateTimeFormatter =
    DateTimeFormatter.ofPattern("dd MMM yyyy", ID_LOCALE)
private val DASHBOARD_HEADER_DATE_FORMATTER: DateTimeFormatter =
    DateTimeFormatter.ofPattern("EEEE, dd MMM yyyy", ID_LOCALE)

private val SENT_OR_CONFIRMED_STATUSES = setOf(
    DistributionStatus.SENT,
    DistributionStatus.RECEIVED,
    DistributionStatus.PARTIALLY_RECEIVED,
    DistributionStatus.NOT_RECEIVED
)

fun computeOwnerDerivedState(
    raw: OwnerUiState,
    nowMillis: Long = System.currentTimeMillis(),
    zoneId: ZoneId = ZoneId.systemDefault()
): OwnerUiState {
    val activeOutlets = if (raw.hasFirestoreSync) raw.outlets else raw.outlets.ifEmpty { defaultSeededOutlets() }
    val activeUsers = if (raw.hasFirestoreSync) raw.users else raw.users.ifEmpty { defaultSeededUsers() }
    val activeReports = if (raw.hasFirestoreSync) raw.reports else raw.reports.ifEmpty { defaultSeededReports() }
    val activeExpenses = raw.expenses
    val activeDamaged = raw.damagedMaterials
    val activeMaterials = if (raw.hasFirestoreSync) raw.materials else raw.materials.ifEmpty { defaultSeededMaterials() }
    val activeProducts = if (raw.hasFirestoreSync) raw.products else raw.products.ifEmpty { defaultSeededProducts() }

    val nowZoned = Instant.ofEpochMilli(nowMillis).atZone(zoneId)
    val todayDate = nowZoned.toLocalDate()
    val yesterdayDate = todayDate.minusDays(1)
    val greetingPrefix = when (nowZoned.hour) {
        in 0..10 -> "Selamat pagi"
        in 11..14 -> "Selamat siang"
        in 15..17 -> "Selamat sore"
        else -> "Selamat malam"
    }
    val todayDateLabel = todayDate.format(DASHBOARD_HEADER_DATE_FORMATTER)

    val outletMap = activeOutlets.associateBy { it.id }
    val materialMap = activeMaterials.associateBy { it.id }
    val expensesByReport = activeExpenses.groupBy { it.dailyReportId }
    val damagedByReport = activeDamaged.groupBy { it.dailyReportId }

    val todayReports = activeReports.filter { report ->
        Instant.ofEpochMilli(report.date.toDate().time).atZone(zoneId).toLocalDate() == todayDate
    }
    val yesterdayReports = activeReports.filter { report ->
        Instant.ofEpochMilli(report.date.toDate().time).atZone(zoneId).toLocalDate() == yesterdayDate
    }
    val todayReportByOutlet = todayReports
        .groupBy { it.outletId }
        .mapValues { (_, list) -> list.maxBy { it.date.seconds } }

    val outletModels = activeOutlets.map { outlet ->
        val todayReport = todayReportByOutlet[outlet.id]
        val hasTodayReport = todayReport != null
        val revenue = todayReport?.totalRevenue ?: 0L
        val cashRev = todayReport?.cashRevenue ?: 0L
        val qrisRev = todayReport?.qrisRevenue ?: 0L
        val pieces = todayReport?.totalSold ?: 0
        val dailyTarget = if (outlet.dailyTarget > 0L) outlet.dailyTarget else 2_500_000L
        val progress = (revenue.toFloat() / dailyTarget.toFloat()).coerceIn(0f, 1f)

        val assignedStaff = activeUsers.filter {
            it.isActive && it.role == Role.OUTLET && it.outletId == outlet.id
        }
        val outletExpenses = todayReport?.let { expensesByReport[it.id] }.orEmpty()
        val expenseTotal = outletExpenses.sumOf { it.amount }
        val outletDamaged = todayReport?.let { damagedByReport[it.id] }.orEmpty()
        val damagedQty = outletDamaged.sumOf { it.quantity }

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
            targetPercentText = if (hasTodayReport) "${(progress * 100).toInt()}% target" else "Belum closing",
            activeStaffCount = assignedStaff.size,
            staffIds = assignedStaff.map { it.id },
            staffNames = assignedStaff.map { it.name },
            expenseTotal = expenseTotal,
            expenseCount = outletExpenses.size,
            damagedCount = damagedQty,
            damagedReportCount = outletDamaged.size,
            netDepositEstimate = (revenue - expenseTotal).coerceAtLeast(0L),
            routinePreset = outlet.routinePreset,
            hasTodayReport = hasTodayReport
        )
    }

    val rankedByRevenue = outletModels
        .sortedWith(
            compareByDescending<OutletPerformanceUi> { it.isActive }
                .thenByDescending { it.revenue }
                .thenBy { it.name.lowercase(Locale.ROOT) }
        )
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

    val userMap = activeUsers.associateBy { it.id }
    val dateFmt = SimpleDateFormat("dd MMM yyyy, HH.mm", ID_LOCALE)
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

            val hasHighDamage = dmgSum >= 10
            val highOtherExpense = repExpenses.firstOrNull {
                it.category == ExpenseCategory.OTHER && it.amount >= 200_000L
            }
            val hasAnomaly = hasHighDamage || highOtherExpense != null
            val anomalyReasonText = buildList {
                if (hasHighDamage) add("Bahan rusak tinggi ($dmgSum unit)")
                if (highOtherExpense != null) {
                    val noteSuffix = highOtherExpense.note?.takeIf { it.isNotBlank() }?.let { " ($it)" }.orEmpty()
                    add("Pengeluaran Lainnya ${formatRupiah(highOtherExpense.amount)}$noteSuffix")
                }
            }.takeIf { it.isNotEmpty() }?.joinToString(" • ")

            val hasAlert = hasAnomaly && report.reviewStatus == ReportReviewStatus.PENDING
            val reviewedByName = report.reviewedBy?.let { reviewerId ->
                userMap[reviewerId]?.name?.replace(" (Owner)", "")?.takeIf { it.isNotBlank() }
                    ?: if (reviewerId.equals("owner", ignoreCase = true)) "Owner" else reviewerId
            }
            val reviewedAtLabel = report.reviewedAt?.let { dateFmt.format(it.toDate()) }

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
                hasAlert = hasAlert,
                hasAnomaly = hasAnomaly,
                anomalyReasonText = anomalyReasonText,
                reviewStatus = report.reviewStatus,
                reviewNote = report.reviewNote,
                reviewedByName = reviewedByName,
                reviewedAtLabel = reviewedAtLabel
            )
        }

    // Today's financial & operational metrics from Firestore
    val todayCashRevenue = todayReports.sumOf { it.cashRevenue }
    val todayQrisRevenue = todayReports.sumOf { it.qrisRevenue }
    val todayTotalRevenue = todayCashRevenue + todayQrisRevenue
    val todayTotalSold = todayReports.sumOf { it.totalSold }

    val todayReportIds = todayReports.map { it.id }.toSet()
    val todayExpenseItems = activeExpenses.filter { it.dailyReportId in todayReportIds }
    val todayTotalExpenses = todayExpenseItems.sumOf { it.amount }
    val todayExpenseTransactionsCount = todayExpenseItems.size

    val todayDamagedItems = activeDamaged.filter { it.dailyReportId in todayReportIds }
    val todayDamagedUnits = todayDamagedItems.sumOf { it.quantity }
    val todayDamagedEstimatedLoss = todayDamagedItems.sumOf { dmg ->
        dmg.quantity.toLong() * (materialMap[dmg.materialId]?.cost ?: 0L)
    }
    val todayNetRevenue = (todayTotalRevenue - todayTotalExpenses).coerceAtLeast(0L)

    val operationalOutlets = rankedByRevenue.filter { it.isActive }
    val activeOutletIds = operationalOutlets.map { it.id }.toSet()
    val totalDailyTarget = operationalOutlets.sumOf { it.dailyTarget }
    val targetProgress = if (totalDailyTarget > 0L) {
        (todayTotalRevenue.toFloat() / totalDailyTarget.toFloat()).coerceIn(0f, 1f)
    } else {
        0f
    }
    val targetPercent = if (totalDailyTarget > 0L) {
        ((todayTotalRevenue * 100L) / totalDailyTarget).toInt().coerceAtLeast(0)
    } else {
        0
    }

    val yesterdayTotalRevenue = yesterdayReports.sumOf { it.totalRevenue }
    val (deltaTrend, deltaText) = if (yesterdayTotalRevenue <= 0L) {
        null to "Belum ada pembanding kemarin"
    } else {
        val diff = todayTotalRevenue - yesterdayTotalRevenue
        val pct = (abs(diff).toDouble() / yesterdayTotalRevenue.toDouble()) * 100.0
        val pctText = String.format(ID_LOCALE, "%.1f", pct)
        when {
            diff > 0L -> 1 to "+$pctText% dari kemarin"
            diff < 0L -> -1 to "-$pctText% dari kemarin"
            else -> 0 to "Sama dengan kemarin"
        }
    }

    val reviewReportsCount = reportList.count { it.hasAlert }

    // Warehouse distribution overview for today
    val todayDistributions = raw.distributions.filter { dist ->
        val ts = dist.sentAt ?: dist.createdAt
        Instant.ofEpochMilli(ts.toDate().time).atZone(zoneId).toLocalDate() == todayDate
    }
    val todayRoutineSentOutletsCount = todayDistributions
        .filter { it.type == DistributionType.ROUTINE && it.outletId in activeOutletIds && it.status in SENT_OR_CONFIRMED_STATUSES }
        .map { it.outletId }
        .toSet()
        .size
    val todayInTransitDistributionsCount = todayDistributions.count { it.status == DistributionStatus.SENT }
    val pendingAdditionalRequestsCount = raw.distributions.count {
        it.type == DistributionType.ADDITIONAL && it.status == DistributionStatus.DRAFT
    }
    val todayDistributionIssueCount = todayDistributions.count {
        it.status == DistributionStatus.PARTIALLY_RECEIVED || it.status == DistributionStatus.NOT_RECEIVED
    }

    val dashboardMetrics = OwnerDashboardMetrics(
        todayDateLabel = todayDateLabel,
        greetingPrefix = greetingPrefix,
        todayTotalRevenue = todayTotalRevenue,
        todayCashRevenue = todayCashRevenue,
        todayQrisRevenue = todayQrisRevenue,
        totalDailyTarget = totalDailyTarget,
        targetProgress = targetProgress,
        targetPercent = targetPercent,
        todayDeltaPercentText = deltaText,
        todayDeltaTrend = deltaTrend,
        activeOutletsCount = operationalOutlets.size,
        openOutletsCount = operationalOutlets.count { it.isOpen },
        closedOutletsCount = operationalOutlets.count { !it.isOpen },
        todayReportedOutletsCount = operationalOutlets.count { it.hasTodayReport },
        todayTotalSold = todayTotalSold,
        todayTotalExpenses = todayTotalExpenses,
        todayExpenseTransactionsCount = todayExpenseTransactionsCount,
        todayDamagedUnits = todayDamagedUnits,
        todayDamagedEstimatedLoss = todayDamagedEstimatedLoss,
        todayNetRevenue = todayNetRevenue,
        reviewReportsCount = reviewReportsCount,
        todayRoutineSentOutletsCount = todayRoutineSentOutletsCount,
        todayInTransitDistributionsCount = todayInTransitDistributionsCount,
        pendingAdditionalRequestsCount = pendingAdditionalRequestsCount,
        todayDistributionIssueCount = todayDistributionIssueCount
    )

    val dynamicNotifications = if (raw.hasFirestoreSync || raw.reports.isNotEmpty()) {
        reportList.take(8).map { item ->
            val notifId = "notif_${item.id}"
            val isUnreadAlert = item.hasAlert && notifId !in raw.readNotificationIds
            val title = when {
                item.hasAlert -> "Perlu ditinjau — ${item.outletName}"
                item.reviewStatus == ReportReviewStatus.REJECTED -> "Laporan ditolak — ${item.outletName}"
                item.reviewStatus == ReportReviewStatus.APPROVED -> "Laporan disetujui — ${item.outletName}"
                else -> "Laporan harian — ${item.outletName}"
            }
            val desc = when {
                item.hasAlert -> item.anomalyReasonText ?: "Terdapat stok rusak tinggi atau pengeluaran ekstra."
                item.reviewStatus == ReportReviewStatus.REJECTED ->
                    "Menunggu perbaikan staf: ${item.reviewNote ?: "Perlu revisi"}"
                else -> "Omzet ${formatRupiah(item.totalRevenue)} (${item.totalSold} pcs terjual)."
            }
            OwnerNotificationUi(
                id = notifId,
                title = title,
                description = desc,
                time = item.dateLabel,
                isNeedsReview = isUnreadAlert,
                targetOutletId = item.outletId,
                targetReportId = item.id
            )
        }
    } else {
        raw.notifications.map { n ->
            if (n.id in raw.readNotificationIds) n.copy(isNeedsReview = false) else n
        }
    }

    return raw.copy(
        todayDateLabel = todayDateLabel,
        outletItems = sortedOutlets,
        staffItems = staffList,
        reportListItems = reportList,
        dashboardMetrics = dashboardMetrics,
        todayTotalRevenue = todayTotalRevenue,
        todayDeltaPercentText = deltaText,
        activeOutletsCount = dashboardMetrics.activeOutletsCount,
        openOutletsCount = dashboardMetrics.openOutletsCount,
        closedOutletsCount = dashboardMetrics.closedOutletsCount,
        todayTotalSold = todayTotalSold,
        todayTotalExpenses = todayTotalExpenses,
        todayExpenseTransactionsCount = todayExpenseTransactionsCount,
        reviewReportsCount = reviewReportsCount,
        catalogItems = buildCatalogItems(activeMaterials, activeProducts),
        notifications = dynamicNotifications,
        addUserForm = raw.addUserForm.copy(
            selectedOutletId = raw.addUserForm.selectedOutletId.ifBlank { rankedByRevenue.firstOrNull()?.id.orEmpty() }
        )
    )
}

private fun inferLegacyMaterialCategory(materialName: String): String {
    val lower = materialName.lowercase(Locale.ROOT)
    return when {
        lower.contains("tahu") -> "Tahu"
        lower.contains("saus") || lower.contains("chilly") || lower.contains("oil") -> "Saus"
        else -> "Bumbu"
    }
}

fun buildCatalogItems(
    materials: List<Material>,
    products: List<Product>,
    categories: List<MaterialCategory> = defaultSeededMaterialCategories()
): List<CatalogItemUi> {
    val categoryById = categories.associateBy { it.id }
    val matItems = materials.map { mat ->
        val lower = mat.name.lowercase(Locale.ROOT)
        val resolvedCategory = categoryById[mat.categoryId]?.name?.takeIf { it.isNotBlank() }
            ?: inferLegacyMaterialCategory(mat.name)
        val unitText = when (mat.unit) {
            UnitType.PCS -> "pcs"
            UnitType.GR -> "gr"
            UnitType.ML -> "ml"
            UnitType.UNIT -> if (lower.contains("bumbu")) "pak" else "botol"
        }
        CatalogItemUi(
            id = mat.id, name = mat.name, categoryId = mat.categoryId, category = resolvedCategory, isProduct = false,
            priceOrCost = mat.cost, unit = mat.unit, unitLabel = unitText,
            subtitleText = "Modal ${formatRupiah(mat.cost)} / $unitText",
            statusBadge = if (mat.isActive) resolvedCategory else "Nonaktif",
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
        totalExpense = expLines.sumOf { it.amount }, damagedLines = dmgLines,
        hasAnomaly = item.hasAnomaly,
        anomalyReasonText = item.anomalyReasonText,
        reviewStatus = item.reviewStatus,
        reviewNote = item.reviewNote,
        reviewedByName = item.reviewedByName,
        reviewedAtLabel = item.reviewedAtLabel
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
    reviewFilter: ReportReviewFilter = ReportReviewFilter.ALL,
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
        val matchesReview = when (reviewFilter) {
            ReportReviewFilter.ALL -> true
            ReportReviewFilter.NEEDS_REVIEW -> item.hasAlert
            ReportReviewFilter.REJECTED -> item.reviewStatus == ReportReviewStatus.REJECTED
            ReportReviewFilter.APPROVED -> item.reviewStatus == ReportReviewStatus.APPROVED
        }
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
        matchesOutlet && matchesReview && matchesDate && matchesQuery
    }
}

fun formatUtcDateMillis(dateMillis: Long): String =
    Instant.ofEpochMilli(dateMillis)
        .atZone(ZoneOffset.UTC)
        .toLocalDate()
        .format(REPORT_DATE_CHIP_FORMATTER)
