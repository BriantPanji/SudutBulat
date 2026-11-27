package com.sudutcorp.sudutbulat.data.model

import com.google.firebase.firestore.PropertyName

enum class DistributionType(val label: String) {
    @PropertyName("ROUTINE")
    ROUTINE("Rutin"),

    @PropertyName("ADDITIONAL")
    ADDITIONAL("Tambahan")
}
