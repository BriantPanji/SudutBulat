package com.sudutcorp.sudutbulat.data.remote

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.GeoPoint
import com.sudutcorp.sudutbulat.data.model.DailyReport
import com.sudutcorp.sudutbulat.data.model.DailyReportDamagedMaterial
import com.sudutcorp.sudutbulat.data.model.DailyReportExpense
import com.sudutcorp.sudutbulat.data.model.DailyReportProduct
import com.sudutcorp.sudutbulat.data.model.DistributionStatus
import com.sudutcorp.sudutbulat.data.model.DistributionType
import com.sudutcorp.sudutbulat.data.model.ExpenseCategory
import com.sudutcorp.sudutbulat.data.model.Material
import com.sudutcorp.sudutbulat.data.model.MaterialCategory
import com.sudutcorp.sudutbulat.data.model.MaterialDistribution
import com.sudutcorp.sudutbulat.data.model.MaterialDistributionItem
import com.sudutcorp.sudutbulat.data.model.Outlet
import com.sudutcorp.sudutbulat.data.model.Product
import com.sudutcorp.sudutbulat.data.model.ProductSaleType
import com.sudutcorp.sudutbulat.data.model.ReportReviewStatus
import com.sudutcorp.sudutbulat.data.model.Role
import com.sudutcorp.sudutbulat.data.model.RoutinePresetItem
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

    fun parseDistributionType(raw: String?): DistributionType {
        val clean = raw?.trim()?.uppercase() ?: return DistributionType.ROUTINE
        return DistributionType.entries.firstOrNull { it.name == clean } ?: DistributionType.ROUTINE
    }

    fun parseExpenseCategory(raw: String?): ExpenseCategory {
        val clean = raw?.trim()?.uppercase() ?: return ExpenseCategory.OTHER
        return ExpenseCategory.entries.firstOrNull { it.name == clean } ?: ExpenseCategory.OTHER
    }

    fun parseReportReviewStatus(raw: String?): ReportReviewStatus =
        ReportReviewStatus.fromString(raw)

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

    @Suppress("UNCHECKED_CAST")
    fun DocumentSnapshot.toOutlet(): Outlet {
        val rawPreset = (get("routine_preset") as? List<*>)
            ?.mapNotNull { it as? Map<String, Any?> }
            ?.mapNotNull { raw ->
                val matId = (raw["material_id"] as? String)?.trim().orEmpty()
                val qty = (raw["quantity"] as? Number)?.toInt() ?: 0
                if (matId.isNotBlank() && qty > 0) RoutinePresetItem(materialId = matId, quantity = qty) else null
            }
            ?: emptyList()
        return Outlet(
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
            dailyTarget = getLong("daily_target") ?: 2_000_000L,
            routinePreset = rawPreset
        )
    }

    fun RoutinePresetItem.toFirestoreMap(): Map<String, Any> = mapOf(
        "material_id" to materialId,
        "quantity" to quantity
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
        "daily_target" to dailyTarget,
        "routine_preset" to routinePreset.filter { it.materialId.isNotBlank() && it.quantity > 0 }.map { it.toFirestoreMap() }
    )

    fun DocumentSnapshot.toMaterialCategory(): MaterialCategory {
        val rawName = getString("name").orEmpty().trim()
        val rawLower = getString("name_lower").orEmpty().trim().ifBlank { rawName.lowercase(java.util.Locale.ROOT) }
        return MaterialCategory(
            id = id,
            name = rawName,
            nameLower = rawLower
        )
    }

    fun MaterialCategory.toFirestoreMap(): Map<String, Any?> {
        val cleanName = name.trim()
        val cleanLower = nameLower.trim().ifBlank { cleanName.lowercase(java.util.Locale.ROOT) }
        return mapOf(
            "name" to cleanName,
            "name_lower" to cleanLower
        )
    }

    fun DocumentSnapshot.toMaterial(): Material = Material(
        id = id,
        name = getString("name").orEmpty(),
        cost = (getLong("cost") ?: 0L).coerceAtLeast(0L),
        unit = UnitType.fromString(getString("unit")) ?: UnitType.PCS,
        categoryId = getString("category_id").orEmpty(),
        isActive = getBoolean("is_active") ?: true
    )

    fun Material.toFirestoreMap(): Map<String, Any?> = mapOf(
        "name" to name,
        "cost" to cost,
        "unit" to unit.firestoreValue,
        "category_id" to categoryId,
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
        totalSold = (getLong("total_sold") ?: 0L).toInt().coerceAtLeast(0),
        createdBy = getString("created_by").orEmpty(),
        reviewStatus = parseReportReviewStatus(getString("review_status")),
        reviewNote = getString("review_note")?.take(512)?.takeIf { it.isNotBlank() },
        reviewedBy = getString("reviewed_by")?.takeIf { it.isNotBlank() },
        reviewedAt = getTimestamp("reviewed_at")
    )

    fun DailyReport.toFirestoreMap(): Map<String, Any?> = buildMap {
        put("outlet_id", outletId)
        put("date", date)
        put("cash_revenue", cashRevenue)
        put("qris_revenue", qrisRevenue)
        put("total_sold", totalSold)
        if (createdBy.isNotBlank()) {
            put("created_by", createdBy)
        }
        put("review_status", reviewStatus.name)
        put("review_note", reviewNote)
        put("reviewed_by", reviewedBy)
        put("reviewed_at", reviewedAt)
    }

    private fun Any?.asLong(): Long = (this as? Number)?.toLong() ?: 0L

    private fun Any?.asInt(): Int = (this as? Number)?.toInt() ?: 0

    private fun Any?.asNullableInt(): Int? = (this as? Number)?.toInt()

    @Suppress("UNCHECKED_CAST")
    private fun DocumentSnapshot.rawItemsList(): List<Map<String, Any?>> =
        (get("items") as? List<*>)?.mapNotNull { it as? Map<String, Any?> } ?: emptyList()

    fun DocumentSnapshot.toDailyReportProducts(): List<DailyReportProduct> {
        val reportId = getString("daily_report_id").orEmpty().ifBlank { id }
        return rawItemsList().mapIndexed { idx, raw ->
            DailyReportProduct(
                id = "${reportId}_prod_${idx + 1}",
                dailyReportId = reportId,
                productId = (raw["product_id"] as? String).orEmpty(),
                quantity = raw["quantity"].asInt().coerceAtLeast(0),
                price = raw["price"].asLong().coerceAtLeast(0L)
            )
        }
    }

    fun DailyReportProduct.toFirestoreItemMap(): Map<String, Any?> = mapOf(
        "product_id" to productId,
        "quantity" to quantity,
        "price" to price
    )

    fun List<DailyReportProduct>.toReportProductsDocMap(dailyReportId: String): Map<String, Any?> = mapOf(
        "daily_report_id" to dailyReportId,
        "items" to map { it.toFirestoreItemMap() }
    )

    fun DocumentSnapshot.toDailyReportExpenses(): List<DailyReportExpense> {
        val reportId = getString("daily_report_id")?.takeIf { it.isNotBlank() } ?: id
        return rawItemsList().mapIndexed { idx, raw ->
            DailyReportExpense(
                id = "${reportId}_exp_${idx + 1}",
                dailyReportId = reportId,
                category = parseExpenseCategory(raw["category"] as? String),
                amount = raw["amount"].asLong().coerceAtLeast(0L),
                note = (raw["note"] as? String)?.take(512)
            )
        }
    }

    fun DailyReportExpense.toFirestoreItemMap(): Map<String, Any?> = mapOf(
        "category" to category.name,
        "amount" to amount,
        "note" to note
    )

    fun List<DailyReportExpense>.toReportExpensesDocMap(dailyReportId: String): Map<String, Any?> = mapOf(
        "daily_report_id" to dailyReportId,
        "items" to map { it.toFirestoreItemMap() }
    )

    fun DocumentSnapshot.toDailyReportDamagedMaterials(): List<DailyReportDamagedMaterial> {
        val reportId = getString("daily_report_id").orEmpty().ifBlank { id }
        return rawItemsList().mapIndexed { idx, raw ->
            DailyReportDamagedMaterial(
                id = "${reportId}_dmg_${idx + 1}",
                dailyReportId = reportId,
                materialId = (raw["material_id"] as? String).orEmpty(),
                quantity = raw["quantity"].asInt().coerceAtLeast(0),
                note = (raw["note"] as? String)?.take(512)
            )
        }
    }

    fun DailyReportDamagedMaterial.toFirestoreItemMap(): Map<String, Any?> = mapOf(
        "material_id" to materialId,
        "quantity" to quantity,
        "note" to note
    )

    fun List<DailyReportDamagedMaterial>.toReportDamagedDocMap(dailyReportId: String): Map<String, Any?> = mapOf(
        "daily_report_id" to dailyReportId,
        "items" to map { it.toFirestoreItemMap() }
    )

    fun DocumentSnapshot.toMaterialDistribution(): MaterialDistribution {
        val parsedItems = rawItemsList().mapIndexed { idx, raw ->
            val matId = (raw["material_id"] as? String).orEmpty()
            MaterialDistributionItem(
                id = (raw["id"] as? String)?.takeIf { it.isNotBlank() } ?: "${id}_${matId.ifBlank { (idx + 1).toString() }}",
                distributionId = id,
                materialId = matId,
                quantitySent = raw["quantity_sent"].asInt().coerceAtLeast(0),
                quantityReceived = raw["quantity_received"].asNullableInt()?.coerceAtLeast(0),
                note = (raw["note"] as? String)?.take(512)
            )
        }
        return MaterialDistribution(
            id = id,
            outletId = getString("outlet_id").orEmpty(),
            createdBy = getString("created_by").orEmpty(),
            confirmedBy = getString("confirmed_by")?.takeIf { it.isNotBlank() },
            status = parseDistributionStatus(getString("status")),
            type = parseDistributionType(getString("type")),
            note = getString("note")?.take(512),
            confirmationNote = getString("confirmation_note")?.take(512),
            createdAt = getTimestamp("created_at") ?: Timestamp.now(),
            sentAt = getTimestamp("sent_at"),
            confirmedAt = getTimestamp("confirmed_at"),
            items = parsedItems
        )
    }

    fun MaterialDistributionItem.toFirestoreItemMap(): Map<String, Any?> = mapOf(
        "material_id" to materialId,
        "quantity_sent" to quantitySent,
        "quantity_received" to quantityReceived,
        "note" to note
    )

    fun MaterialDistribution.toFirestoreMap(): Map<String, Any?> = mapOf(
        "outlet_id" to outletId,
        "created_by" to createdBy,
        "confirmed_by" to confirmedBy,
        "status" to status.name,
        "type" to type.name,
        "note" to note,
        "confirmation_note" to confirmationNote,
        "created_at" to createdAt,
        "sent_at" to sentAt,
        "confirmed_at" to confirmedAt,
        "items" to items.map { it.toFirestoreItemMap() }
    )
}
