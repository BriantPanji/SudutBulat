package com.sudutcorp.sudutbulat.data.model

import com.google.firebase.firestore.PropertyName

enum class UnitType {
    @PropertyName("pcs")
    PCS,

    @PropertyName("gr")
    GR,

    @PropertyName("ml")
    ML,

    @PropertyName("unit")
    UNIT
}
