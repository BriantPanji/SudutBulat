package com.sudutcorp.sudutbulat.data.model

import com.google.firebase.firestore.PropertyName

enum class Role {
    @PropertyName("DEV")
    DEV,

    @PropertyName("OWNER")
    OWNER,

    @PropertyName("WAREHOUSE")
    WAREHOUSE,

    @PropertyName("OUTLET")
    OUTLET;

    companion object {
        val GUDANG: Role get() = WAREHOUSE
    }
}
