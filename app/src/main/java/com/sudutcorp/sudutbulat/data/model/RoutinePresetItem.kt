package com.sudutcorp.sudutbulat.data.model

import com.google.firebase.firestore.PropertyName

data class RoutinePresetItem(
    @get:PropertyName("material_id")
    @set:PropertyName("material_id")
    var materialId: String = "",

    var quantity: Int = 0
) {
    init {
        require(quantity >= 0) { "Preset quantity must not be negative: $quantity" }
    }
}
