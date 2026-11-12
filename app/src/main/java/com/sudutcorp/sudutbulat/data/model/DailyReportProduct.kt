package com.sudutcorp.sudutbulat.data.model

import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.PropertyName

/**
 * Data model for daily report product line items in Firestore collection `daily_report_products`.
 *
 * @property quantity Sold quantity for the product, non-negative (>= 0).
 * @property price Price snapshot at report creation, non-negative (>= 0).
 */
data class DailyReportProduct(
    @DocumentId
    val id: String = "",

    @get:PropertyName("daily_report_id")
    @set:PropertyName("daily_report_id")
    var dailyReportId: String = "",

    @get:PropertyName("product_id")
    @set:PropertyName("product_id")
    var productId: String = "",

    val quantity: Int = 0,

    val price: Long = 0L
) {
    init {
        require(quantity >= 0) { "Sold quantity must not be negative: $quantity" }
        require(price >= 0L) { "Product price must not be negative: $price" }
    }

    /**
     * Subtotal amount for this product item
     */
    val subtotal: Long get() = quantity.toLong() * price
}
