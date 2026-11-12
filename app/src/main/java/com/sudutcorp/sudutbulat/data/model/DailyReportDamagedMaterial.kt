package com.sudutcorp.sudutbulat.data.model

import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.PropertyName

/**
 * Data model for damaged / wasted materials in daily reports
 * Firestore collection `daily_report_damaged_materials`.
 *
 * @property quantity Quantity of damaged material, non-negative (>= 0).
 * @property note Optional notes (max 512 chars).
 */
data class DailyReportDamagedMaterial(
    @DocumentId
    val id: String = "",

    @get:PropertyName("daily_report_id")
    @set:PropertyName("daily_report_id")
    var dailyReportId: String = "",

    @get:PropertyName("material_id")
    @set:PropertyName("material_id")
    var materialId: String = "",

    val quantity: Int = 0,

    val note: String? = null
) {
    init {
        require(quantity >= 0) { "Damaged material quantity must not be negative: $quantity" }
        require(note == null || note.length <= 512) { "Note must not exceed 512 characters" }
    }
}
