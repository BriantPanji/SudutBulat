package com.sudutcorp.sudutbulat.data.model

import com.google.firebase.firestore.DocumentId

/**
 * Data model for materials / raw ingredients in Firestore collection `materials`.
 *
 * @property cost Cost per unit, must be non-negative (>= 0).
 */
data class Material(
    @DocumentId
    val id: String = "",

    val name: String = "",

    val cost: Long = 0L,

    val unit: UnitType = UnitType.PCS
) {
    init {
        require(cost >= 0L) { "Material cost must not be negative: $cost" }
    }
}
