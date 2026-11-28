package com.sudutcorp.sudutbulat.feature.owner

import com.sudutcorp.sudutbulat.core.util.buildInitials
import com.sudutcorp.sudutbulat.core.util.formatCoordinate
import com.sudutcorp.sudutbulat.core.util.formatDecimal
import com.sudutcorp.sudutbulat.core.util.formatRupiah
import com.sudutcorp.sudutbulat.core.util.formatShortRupiah
import com.sudutcorp.sudutbulat.core.util.orDefaultMedan
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
private val SHORT_DATE_FORMATTER: DateTimeFormatter =
    DateTimeFormatter.ofPattern("dd MMM", ID_LOCALE)
private val SHORT_DAY_FORMATTER: DateTimeFormatter =
    DateTimeFormatter.ofPattern("EEE", ID_LOCALE)
private val SHORT_MONTH_FORMATTER: DateTimeFormatter =
    DateTimeFormatter.ofPattern("MMM", ID_LOCALE)
private val DAY_MONTH_NUM_FORMATTER: DateTimeFormatter =
    DateTimeFormatter.ofPattern("d/M", ID_LOCALE)
private val DASHBOARD_HEADER_DATE_FORMATTER: DateTimeFormatter =
    DateTimeFormatter.ofPattern("EEEE, dd MMM yyyy", ID_LOCALE)

private val SENT_OR_CONFIRMED_STATUSES = setOf(
    DistributionStatus.SENT,
    DistributionStatus.ARRIVED,
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

        val resolvedCoord = outlet.coordinate.orDefaultMedan()
        OutletPerformanceUi(
            id = outlet.id,
            rank = 0,
            name = outlet.name.removePrefix("Outlet ").trim(),
            displayName = if (outlet.name.startsWith("Outlet", ignoreCase = true)) outlet.name else "Outlet ${outlet.name}",
            address = outlet.address,
            latitude = resolvedCoord.latitude,
            longitude = resolvedCoord.longitude,
            coordinateLabel = formatCoordinate(resolvedCoord.latitude, resolvedCoord.longitude),
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
                isActive = user.isActive,
                photoUrl = user.photoUrl
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
    val todayInTransitDistributionsCount = todayDistributions.count {
        it.status == DistributionStatus.SENT || it.status == DistributionStatus.ARRIVED
    }
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
    val rawReport = state.reports.firstOrNull { it.id == reportId }
    val productMap = state.products.ifEmpty { defaultSeededProducts() }.associateBy { it.id }
    val materialMap = state.materials.ifEmpty { defaultSeededMaterials() }.associateBy { it.id }
    val userMap = state.users.ifEmpty { defaultSeededUsers() }.associateBy { it.id }

    val createdByName = rawReport?.createdBy?.takeIf { it.isNotBlank() }?.let { creatorId ->
        userMap[creatorId]?.name?.takeIf { it.isNotBlank() } ?: creatorId
    }

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
        val loss = dmg.quantity.toLong() * (mat?.cost ?: 0L)
        ReportDamagedLineUi(dmg.id, mat?.name ?: dmg.materialId, "${dmg.quantity} $unit", dmg.note, estimatedLoss = loss)
    }
    val damagedEstimatedLoss = dmgLines.sumOf { it.estimatedLoss }

    val fullDateFmt = SimpleDateFormat("EEEE, dd MMMM yyyy", ID_LOCALE)
    val timeFmt = SimpleDateFormat("HH.mm 'WIB'", ID_LOCALE)
    val dateObj = Date(item.dateMillis)

    return SingleReportDetailUi(
        id = item.id, outletName = item.outletName, dateLabel = item.dateLabel,
        fullDateLabel = fullDateFmt.format(dateObj), timeLabel = timeFmt.format(dateObj),
        createdByName = createdByName,
        cashRevenue = item.cashRevenue, qrisRevenue = item.qrisRevenue,
        totalRevenue = item.totalRevenue, totalSold = item.totalSold,
        productLines = prodLines, expenseLines = expLines,
        totalExpense = expLines.sumOf { it.amount }, damagedLines = dmgLines,
        damagedEstimatedLoss = damagedEstimatedLoss,
        hasAnomaly = item.hasAnomaly,
        anomalyReasonText = item.anomalyReasonText,
        reviewStatus = item.reviewStatus,
        reviewNote = item.reviewNote,
        reviewedByName = item.reviewedByName,
        reviewedAtLabel = item.reviewedAtLabel
    )
}

fun buildReportPeriodSummary(
    state: OwnerUiState,
    period: String,
    nowMillis: Long = System.currentTimeMillis(),
    zoneId: ZoneId = ZoneId.systemDefault()
): ReportPeriodSummaryUi {
    val activeOutlets = if (state.hasFirestoreSync) state.outlets else state.outlets.ifEmpty { defaultSeededOutlets() }
    val activeReports = if (state.hasFirestoreSync) state.reports else state.reports.ifEmpty { defaultSeededReports() }
    val activeMaterials = if (state.hasFirestoreSync) state.materials else state.materials.ifEmpty { defaultSeededMaterials() }
    val activeProducts = if (state.hasFirestoreSync) state.products else state.products.ifEmpty { defaultSeededProducts() }

    val outletMap = activeOutlets.associateBy { it.id }
    val materialMap = activeMaterials.associateBy { it.id }
    val productMap = activeProducts.associateBy { it.id }

    val todayDate = Instant.ofEpochMilli(nowMillis).atZone(zoneId).toLocalDate()
    val (periodLabel, windowDays) = when {
        period.equals("7 Hari", ignoreCase = true) -> "7 hari" to 7L
        period.equals("3 Bulan", ignoreCase = true) -> "3 bulan" to 90L
        else -> "30 hari" to 30L
    }

    val startDate = todayDate.minusDays(windowDays - 1L)
    val prevEndDate = startDate.minusDays(1L)
    val prevStartDate = prevEndDate.minusDays(windowDays - 1L)

    val dateRangeText = if (startDate.year == todayDate.year) {
        "${startDate.format(SHORT_DATE_FORMATTER)} – ${todayDate.format(REPORT_DATE_CHIP_FORMATTER)}"
    } else {
        "${startDate.format(REPORT_DATE_CHIP_FORMATTER)} – ${todayDate.format(REPORT_DATE_CHIP_FORMATTER)}"
    }

    val reportsWithDate = activeReports.map { report ->
        val localDate = Instant.ofEpochMilli(report.date.toDate().time).atZone(zoneId).toLocalDate()
        report to localDate
    }

    val periodReports = reportsWithDate.filter { (_, date) ->
        !date.isBefore(startDate) && !date.isAfter(todayDate)
    }
    val prevReports = reportsWithDate.filter { (_, date) ->
        !date.isBefore(prevStartDate) && !date.isAfter(prevEndDate)
    }

    val periodReportIds = periodReports.map { it.first.id }.toSet()
    val periodExpenses = state.expenses.filter { it.dailyReportId in periodReportIds }
    val periodDamaged = state.damagedMaterials.filter { it.dailyReportId in periodReportIds }
    val periodProducts = state.reportProducts.filter { it.dailyReportId in periodReportIds }

    val cashRevenue = periodReports.sumOf { it.first.cashRevenue }
    val qrisRevenue = periodReports.sumOf { it.first.qrisRevenue }
    val totalRevenue = cashRevenue + qrisRevenue
    val cashShareFraction = if (totalRevenue > 0L) {
        (cashRevenue.toFloat() / totalRevenue.toFloat()).coerceIn(0f, 1f)
    } else {
        0f
    }
    val totalSold = periodReports.sumOf { it.first.totalSold }

    val gasExpense = periodExpenses.filter { it.category == ExpenseCategory.GAS }.sumOf { it.amount }
    val plasticExpense = periodExpenses.filter { it.category == ExpenseCategory.PLASTIC_BAG }.sumOf { it.amount }
    val otherExpense = periodExpenses.filter { it.category == ExpenseCategory.OTHER }.sumOf { it.amount }
    val totalExpense = gasExpense + plasticExpense + otherExpense
    val netRevenue = (totalRevenue - totalExpense).coerceAtLeast(0L)

    val operationalCostPercentText = if (totalRevenue > 0L) {
        val pct = (totalExpense.toDouble() / totalRevenue.toDouble()) * 100.0
        "${String.format(ID_LOCALE, "%.1f", pct)}% dari omzet"
    } else {
        "0% dari omzet"
    }

    val prevRevenue = prevReports.sumOf { it.first.totalRevenue }
    val (deltaTrend, deltaText) = if (prevRevenue <= 0L) {
        null to "Belum ada pembanding periode lalu"
    } else {
        val diff = totalRevenue - prevRevenue
        val pct = (abs(diff).toDouble() / prevRevenue.toDouble()) * 100.0
        val pctText = String.format(ID_LOCALE, "%.1f", pct)
        when {
            diff > 0L -> 1 to "+$pctText% dibanding periode lalu"
            diff < 0L -> -1 to "-$pctText% dibanding periode lalu"
            else -> 0 to "Sama dengan periode lalu"
        }
    }

    // Build real trend bars for the selected period
    val revenueByDate = periodReports
        .groupBy { it.second }
        .mapValues { (_, list) -> list.sumOf { it.first.totalRevenue } }

    data class RawBucket(val label: String, val detailLabel: String, val revenue: Long, val isLatest: Boolean)
    val rawBuckets: List<RawBucket> = when (windowDays) {
        7L -> (0L until 7L).map { offset ->
            val d = startDate.plusDays(offset)
            val rev = revenueByDate[d] ?: 0L
            RawBucket(
                label = d.format(SHORT_DAY_FORMATTER).take(3),
                detailLabel = d.format(SHORT_DATE_FORMATTER),
                revenue = rev,
                isLatest = d == todayDate
            )
        }
        90L -> {
            // 3 calendar months ending at todayDate's month
            val startMonth = todayDate.withDayOfMonth(1).minusMonths(2)
            (0L until 3L).map { offset ->
                val mStart = startMonth.plusMonths(offset)
                val mEnd = mStart.plusMonths(1).minusDays(1)
                val rev = periodReports
                    .filter { (_, d) -> !d.isBefore(mStart) && !d.isAfter(mEnd) }
                    .sumOf { it.first.totalRevenue }
                val monthLabel = mStart.format(SHORT_MONTH_FORMATTER)
                RawBucket(
                    label = monthLabel,
                    detailLabel = "$monthLabel ${mStart.year}",
                    revenue = rev,
                    isLatest = offset == 2L
                )
            }
        }
        else -> {
            // 30 days -> 6 buckets of 5 days each
            (0 until 6).map { idx ->
                val bStart = startDate.plusDays(idx * 5L)
                val bEnd = if (idx == 5) todayDate else bStart.plusDays(4L)
                val rev = periodReports
                    .filter { (_, d) -> !d.isBefore(bStart) && !d.isAfter(bEnd) }
                    .sumOf { it.first.totalRevenue }
                RawBucket(
                    label = bStart.format(DAY_MONTH_NUM_FORMATTER),
                    detailLabel = "${bStart.format(SHORT_DATE_FORMATTER)} – ${bEnd.format(SHORT_DATE_FORMATTER)}",
                    revenue = rev,
                    isLatest = idx == 5
                )
            }
        }
    }

    val maxBucketRevenue = rawBuckets.maxOfOrNull { it.revenue } ?: 0L
    val peakIndex = if (maxBucketRevenue > 0L) rawBuckets.indexOfLast { it.revenue == maxBucketRevenue } else -1
    val trendBars = rawBuckets.mapIndexed { idx, bucket ->
        val fraction = if (maxBucketRevenue > 0L && bucket.revenue > 0L) {
            (bucket.revenue.toFloat() / maxBucketRevenue.toFloat()).coerceIn(0.08f, 1f)
        } else {
            0.04f
        }
        RevenueTrendBarUi(
            label = bucket.label,
            detailLabel = bucket.detailLabel,
            revenue = bucket.revenue,
            heightFraction = fraction,
            isHighlighted = if (peakIndex >= 0) idx == peakIndex else bucket.isLatest
        )
    }

    // Per-outlet breakdown
    val expensesByReportId = periodExpenses.groupBy { it.dailyReportId }
    val damagedByReportId = periodDamaged.groupBy { it.dailyReportId }
    val outletBreakdowns = periodReports
        .groupBy { it.first.outletId }
        .map { (outletId, list) ->
            val outletName = outletMap[outletId]?.let {
                if (it.name.startsWith("Outlet", ignoreCase = true)) it.name else "Outlet ${it.name}"
            } ?: outletId
            val rev = list.sumOf { it.first.totalRevenue }
            val sold = list.sumOf { it.first.totalSold }
            val exp = list.sumOf { (rep, _) -> expensesByReportId[rep.id].orEmpty().sumOf { it.amount } }
            val dmg = list.sumOf { (rep, _) -> damagedByReportId[rep.id].orEmpty().sumOf { it.quantity } }
            OutletPeriodBreakdownUi(
                outletId = outletId,
                outletName = outletName,
                reportCount = list.size,
                totalRevenue = rev,
                totalSold = sold,
                totalExpense = exp,
                damagedUnits = dmg
            )
        }
        .sortedByDescending { it.totalRevenue }

    // Aggregated product breakdown
    val productBreakdowns = periodProducts
        .groupBy { it.productId }
        .map { (productId, items) ->
            ProductPeriodBreakdownUi(
                productId = productId,
                productName = productMap[productId]?.name ?: productId,
                totalQuantity = items.sumOf { it.quantity },
                totalSubtotal = items.sumOf { it.subtotal }
            )
        }
        .sortedByDescending { it.totalSubtotal }

    // Damaged material summary & estimated loss
    val damagedItems = periodDamaged
        .groupBy { it.materialId }
        .map { (matId, items) ->
            val mat = materialMap[matId]
            val unit = when (mat?.unit) {
                UnitType.UNIT -> "pak"
                UnitType.GR -> "gr"
                UnitType.ML -> "ml"
                else -> "pcs"
            }
            val qty = items.sumOf { it.quantity }
            val loss = qty.toLong() * (mat?.cost ?: 0L)
            DamagedSummaryItemUi(
                materialId = matId,
                name = mat?.name ?: matId,
                quantityText = "${formatDecimal(qty)} $unit",
                estimatedLoss = loss
            )
        }
        .sortedByDescending { it.estimatedLoss }

    val damagedTotalUnits = periodDamaged.sumOf { it.quantity }
    val damagedEstimatedLoss = damagedItems.sumOf { it.estimatedLoss }

    return ReportPeriodSummaryUi(
        periodLabel = periodLabel,
        dateRangeText = dateRangeText,
        reportCount = periodReports.size,
        contributingOutletsCount = outletBreakdowns.size,
        totalRevenue = totalRevenue,
        cashRevenue = cashRevenue,
        qrisRevenue = qrisRevenue,
        cashShareFraction = cashShareFraction,
        totalSold = totalSold,
        totalExpense = totalExpense,
        netRevenue = netRevenue,
        damagedTotalUnits = damagedTotalUnits,
        damagedEstimatedLoss = damagedEstimatedLoss,
        deltaTrend = deltaTrend,
        deltaText = deltaText,
        maxBarRevenueText = if (maxBucketRevenue > 0L) "Maks ${formatShortRupiah(maxBucketRevenue)}" else "Belum ada data",
        netRevenueText = formatRupiah(netRevenue),
        totalSalesValueText = formatShortRupiah(totalRevenue),
        totalProductsSoldText = "${formatDecimal(totalSold)} pcs terjual",
        totalProductsPcsText = "${formatDecimal(totalSold)} pcs",
        operationalCostValueText = formatShortRupiah(totalExpense),
        operationalCostPercentText = operationalCostPercentText,
        estimatedProfitValueText = formatShortRupiah(netRevenue),
        cashSalesText = formatRupiah(cashRevenue),
        qrisSalesText = formatRupiah(qrisRevenue),
        gasExpenseText = formatRupiah(gasExpense),
        plasticExpenseText = formatRupiah(plasticExpense),
        otherExpenseText = formatRupiah(otherExpense),
        totalExpenseText = formatRupiah(totalExpense),
        damagedLossValueText = formatShortRupiah(damagedEstimatedLoss),
        trendBars = trendBars,
        outletBreakdowns = outletBreakdowns,
        productBreakdowns = productBreakdowns,
        damagedItems = damagedItems
    )
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
