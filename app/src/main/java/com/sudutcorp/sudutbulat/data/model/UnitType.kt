package com.sudutcorp.sudutbulat.data.model

import com.google.firebase.firestore.PropertyName

enum class UnitType(val firestoreValue: String) {
    @PropertyName("pcs")
    PCS("pcs"),

    @PropertyName("gr")
    GR("gr"),

    @PropertyName("ml")
    ML("ml"),

    @PropertyName("unit")
    UNIT("unit");

    companion object {
        fun fromString(raw: String?): UnitType? = when (raw?.trim()?.lowercase()) {
            "pcs" -> PCS
            "gr", "gram" -> GR
            "ml" -> ML
            "unit", "pak", "botol" -> UNIT
            else -> null
        }
    }
}
