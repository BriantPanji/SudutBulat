package com.sudutcorp.sudutbulat.data.model

import com.google.firebase.firestore.PropertyName

enum class ProductSaleType(val firestoreValue: String, val label: String) {
    @PropertyName("satuan")
    SATUAN("satuan", "Satuan"),

    @PropertyName("bungkus")
    BUNGKUS("bungkus", "Bungkus");

    companion object {
        fun fromString(raw: String?): ProductSaleType? = when (raw?.trim()?.lowercase()) {
            "satuan", "pcs" -> SATUAN
            "bungkus", "pack", "pak" -> BUNGKUS
            else -> null
        }
    }
}
