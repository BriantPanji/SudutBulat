package com.sudutcorp.sudutbulat.data.model

import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.PropertyName

/**
 * Data model for distribution line items in Firestore collection `material_distribution_items`.
 *
 * @property quantitySent Quantity sent by warehouse, non-negative (>= 0).
 * @property quantityReceived Quantity received and confirmed by outlet, nullable until confirmed, non-negative (>= 0) when set.
 */
data class MaterialDistributionItem(
    @DocumentId
    val id: String = "",

    @get:PropertyName("distribution_id")
    @set:PropertyName("distribution_id")
    var distributionId: String = "",

    @get:PropertyName("material_id")
    @set:PropertyName("material_id")
    var materialId: String = "",

    @get:PropertyName("quantity_sent")
    @set:PropertyName("quantity_sent")
    var quantitySent: Int = 0,

    @get:PropertyName("quantity_received")
    @set:PropertyName("quantity_received")
    var quantityReceived: Int? = null,

    val note: String? = null
) {
    init {
        require(quantitySent >= 0) { "Quantity sent must not be negative: $quantitySent" }
        require(quantityReceived == null || quantityReceived!! >= 0) {
            "Quantity received must not be negative: $quantityReceived"
        }
        require(note == null || note.length <= 512) { "Note must not exceed 512 characters" }
    }

    /**
     * Difference between quantity sent and quantity received (sent - received).
     * Returns null if quantityReceived has not been set yet.
     * Can be negative if outlet received more than sent, or positive if received less.
     */
    val quantityDifference: Int?
        get() = quantityReceived?.let { quantitySent - it }
}
