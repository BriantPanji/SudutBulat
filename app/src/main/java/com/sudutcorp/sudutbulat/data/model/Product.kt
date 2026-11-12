package com.sudutcorp.sudutbulat.data.model

import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.PropertyName

/**
 * Data model for products in Firestore collection `products`.
 *
 * @property price Selling price, non-negative (>= 0).
 */
data class Product(
    @DocumentId
    val id: String = "",

    val name: String = "",

    val price: Long = 0L,

    @get:PropertyName("is_active")
    @set:PropertyName("is_active")
    var isActive: Boolean = true
) {
    init {
        require(price >= 0L) { "Product price must not be negative: $price" }
    }
}
