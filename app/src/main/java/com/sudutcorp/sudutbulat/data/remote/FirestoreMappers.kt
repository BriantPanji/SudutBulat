package com.sudutcorp.sudutbulat.data.remote

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.GeoPoint
import com.sudutcorp.sudutbulat.data.model.DailyReport
import com.sudutcorp.sudutbulat.data.model.DailyReportDamagedMaterial
import com.sudutcorp.sudutbulat.data.model.DailyReportExpense
import com.sudutcorp.sudutbulat.data.model.DailyReportProduct
import com.sudutcorp.sudutbulat.data.model.DistributionStatus
import com.sudutcorp.sudutbulat.data.model.ExpenseCategory
import com.sudutcorp.sudutbulat.data.model.Material
import com.sudutcorp.sudutbulat.data.model.MaterialDistribution
import com.sudutcorp.sudutbulat.data.model.MaterialDistributionItem
import com.sudutcorp.sudutbulat.data.model.Outlet
import com.sudutcorp.sudutbulat.data.model.Product
import com.sudutcorp.sudutbulat.data.model.ProductSaleType
import com.sudutcorp.sudutbulat.data.model.Role
import com.sudutcorp.sudutbulat.data.model.UnitType
import com.sudutcorp.sudutbulat.data.model.User

/**
 * Explicit Firestore mappers that handle lowercase/uppercase enum strings safely
 * (Android Firestore SDK's toObject() ignores @PropertyName on enum entries)
 * and write clean snake_case fields without leaking computed getters.
 */
object FirestoreMappers {

    fun parseDistributionStatus(raw: String?): DistributionStatus {
        val clean = raw?.trim()?.uppercase() ?: return DistributionStatus.DRAFT
        return DistributionStatus.entries.firstOrNull { it.name == clean } ?: DistributionStatus.DRAFT
    }

    fun parseExpenseCategory(raw: String?): ExpenseCategory {
        val clean = raw?.trim()?.uppercase() ?: return ExpenseCategory.OTHER
        return ExpenseCategory.entries.firstOrNull { it.name == clean } ?: ExpenseCategory.OTHER
    }

    fun DocumentSnapshot.toUser(): User {
        val email = getString("email").orEmpty()
        val role = Role.fromString(getString("role"))
            ?: Role.fromEmailOrCode(email)
            ?: Role.fromEmailOrCode(id)
            ?: Role.OUTLET
        return User(
            id = id,
            name = getString("name").orEmpty().ifBlank { getString("username").orEmpty().ifBlank { id } },
            email = email,
            username = getString("username").orEmpty().ifBlank { id },
            role = role,
            outletId = getString("outlet_id")?.takeIf { it.isNotBlank() },
            isActive = getBoolean("is_active") ?: true,
            authUid = getString("uid")?.takeIf { it.isNotBlank() }
        )
    }

    fun User.toFirestoreMap(): Map<String, Any?> = buildMap {
        put("name", name)
        put("email", email)
        put("username", username.ifBlank { id })
        put("role", role.firestoreValue)
        put("outlet_id", outletId)
        put("is_active", isActive)
        if (!authUid.isNullOrBlank()) {
            put("uid", authUid)
        }
    }

    fun DocumentSnapshot.toOutlet(): Outlet = Outlet(
        id = id,
        name = getString("name").orEmpty(),
        address = getString("address").orEmpty(),
        coordinate = getGeoPoint("coordinate") ?: GeoPoint(0.0, 0.0),
        isActive = getBoolean("is_active") ?: true,
        isOpen = getBoolean("is_open") ?: true,
        targetPercentage = (getDouble("target_percentage") ?: 0.0).toFloat(),
        totalRevenue = getLong("total_revenue") ?: 0L,
        totalPiecesSold = (getLong("total_pieces_sold") ?: 0L).toInt(),
        openTime = getString("open_time").orEmpty().ifBlank { "08.00" },
        closeTime = getString("close_time").orEmpty().ifBlank { "21.00" },
        dailyTarget = getLong("daily_target") ?: 2_000_000L
    )

    fun Outlet.toFirestoreMap(): Map<String, Any?> = mapOf(
        "name" to name,
        "address" to address,
        "coordinate" to coordinate,
        "is_active" to isActive,
        "is_open" to isOpen,
        "target_percentage" to targetPercentage.toDouble(),
        "total_revenue" to totalRevenue,
        "total_pieces_sold" to totalPiecesSold,
        "open_time" to openTime,
        "close_time" to closeTime,
        "daily_target" to dailyTarget
    )

    fun DocumentSnapshot.toMaterial(): Material = Material(
        id = id,
        name = getString("name").orEmpty(),
        cost = (getLong("cost") ?: 0L).coerceAtLeast(0L),
        unit = UnitType.fromString(getString("unit")) ?: UnitType.PCS,
        isActive = getBoolean("is_active") ?: true
    )

    fun Material.toFirestoreMap(): Map<String, Any?> = mapOf(
        "name" to name,
        "cost" to cost,
        "unit" to unit.firestoreValue,
        "is_active" to isActive
    )

    fun DocumentSnapshot.toProduct(): Product {
        val name = getString("name").orEmpty()
        val saleType = ProductSaleType.fromString(getString("sale_type"))
            ?: if (name.contains("bungkus", ignoreCase = true)) ProductSaleType.BUNGKUS else ProductSaleType.SATUAN
        val rawPcs = getLong("pcs_per_pack")?.toInt()
        val pcsPerPack = if (saleType == ProductSaleType.BUNGKUS) {
            (rawPcs ?: 5).coerceAtLeast(1)
        } else {
            1
        }
        return Product(
            id = id,
            name = name,
            price = (getLong("price") ?: 0L).coerceAtLeast(0L),
            saleType = saleType,
            pcsPerPack = pcsPerPack,
            isActive = getBoolean("is_active") ?: true
        )
    }

    fun Product.toFirestoreMap(): Map<String, Any?> = mapOf(
        "name" to name,
        "price" to price,
        "sale_type" to saleType.firestoreValue,
        "pcs_per_pack" to if (saleType == ProductSaleType.BUNGKUS) pcsPerPack else 1,
        "is_active" to isActive
    )

    fun DocumentSnapshot.toDailyReport(): DailyReport = DailyReport(
        id = id,
        outletId = getString("outlet_id").orEmpty(),
        date = getTimestamp("date") ?: Timestamp.now(),
        cashRevenue = (getLong("cash_revenue") ?: 0L).coerceAtLeast(0L),
        qrisRevenue = (getLong("qris_revenue") ?: 0L).coerceAtLeast(0L),
        totalSold = (getLong("total_sold") ?: 0L).toInt().coerceAtLeast(0)
    )

    fun DailyReport.toFirestoreMap(): Map<String, Any?> = mapOf(
        "outlet_id" to outletId,
        "date" to date,
        "cash_revenue" to cashRevenue,
        "qris_revenue" to qrisRevenue,
        "total_sold" to totalSold
    )

    fun DocumentSnapshot.toDailyReportProduct(): DailyReportProduct = DailyReportProduct(
        id = id,
        dailyReportId = getString("daily_report_id").orEmpty(),
        productId = getString("product_id").orEmpty(),
        quantity = (getLong("quantity") ?: 0L).toInt().coerceAtLeast(0),
        price = (getLong("price") ?: 0L).coerceAtLeast(0L)
    )

    fun DailyReportProduct.toFirestoreMap(): Map<String, Any?> = mapOf(
        "daily_report_id" to dailyReportId,
        "product_id" to productId,
        "quantity" to quantity,
        "price" to price
    )

    fun DocumentSnapshot.toDailyReportExpense(): DailyReportExpense = DailyReportExpense(
        id = id,
        dailyReportId = getString("daily_report_id")?.takeIf { it.isNotBlank() },
        category = parseExpenseCategory(getString("category")),
        amount = (getLong("amount") ?: 0L).coerceAtLeast(0L),
        note = getString("note")?.take(512)
    )

    fun DailyReportExpense.toFirestoreMap(): Map<String, Any?> = mapOf(
        "daily_report_id" to dailyReportId,
        "category" to category.name,
        "amount" to amount,
        "note" to note
    )

    fun DocumentSnapshot.toDailyReportDamagedMaterial(): DailyReportDamagedMaterial = DailyReportDamagedMaterial(
        id = id,
        dailyReportId = getString("daily_report_id").orEmpty(),
        materialId = getString("material_id").orEmpty(),
        quantity = (getLong("quantity") ?: 0L).toInt().coerceAtLeast(0),
        note = getString("note")?.take(512)
    )

    fun DailyReportDamagedMaterial.toFirestoreMap(): Map<String, Any?> = mapOf(
        "daily_report_id" to dailyReportId,
        "material_id" to materialId,
        "quantity" to quantity,
        "note" to note
    )

    fun DocumentSnapshot.toMaterialDistribution(): MaterialDistribution = MaterialDistribution(
        id = id,
        outletId = getString("outlet_id").orEmpty(),
        createdBy = getString("created_by").orEmpty(),
        confirmedBy = getString("confirmed_by")?.takeIf { it.isNotBlank() },
        status = parseDistributionStatus(getString("status")),
        note = getString("note")?.take(512),
        confirmationNote = getString("confirmation_note")?.take(512),
        createdAt = getTimestamp("created_at") ?: Timestamp.now(),
        sentAt = getTimestamp("sent_at"),
        confirmedAt = getTimestamp("confirmed_at")
    )

    fun MaterialDistribution.toFirestoreMap(): Map<String, Any?> = mapOf(
        "outlet_id" to outletId,
        "created_by" to createdBy,
        "confirmed_by" to confirmedBy,
        "status" to status.name,
        "note" to note,
        "confirmation_note" to confirmationNote,
        "created_at" to createdAt,
        "sent_at" to sentAt,
        "confirmed_at" to confirmedAt
    )

    fun DocumentSnapshot.toMaterialDistributionItem(): MaterialDistributionItem = MaterialDistributionItem(
        id = id,
        distributionId = getString("distribution_id").orEmpty(),
        materialId = getString("material_id").orEmpty(),
        quantitySent = (getLong("quantity_sent") ?: 0L).toInt().coerceAtLeast(0),
        quantityReceived = getLong("quantity_received")?.toInt()?.coerceAtLeast(0),
        note = getString("note")?.take(512)
    )

    fun MaterialDistributionItem.toFirestoreMap(): Map<String, Any?> = mapOf(
        "distribution_id" to distributionId,
        "material_id" to materialId,
        "quantity_sent" to quantitySent,
        "quantity_received" to quantityReceived,
        "note" to note
    )
}
