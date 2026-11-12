package com.sudutcorp.sudutbulat.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.PropertyName

/**
 * Data model for daily sales reports in Firestore collection `daily_reports`.
 *
 * @property cashRevenue Cash revenue received, non-negative (>= 0).
 * @property qrisRevenue QRIS revenue received, non-negative (>= 0).
 * @property totalSold Total item quantity sold, non-negative (>= 0).
 */
data class DailyReport(
    @DocumentId
    val id: String = "",

    @get:PropertyName("outlet_id")
    @set:PropertyName("outlet_id")
    var outletId: String = "",

    val date: Timestamp = Timestamp.now(),

    @get:PropertyName("cash_revenue")
    @set:PropertyName("cash_revenue")
    var cashRevenue: Long = 0L,

    @get:PropertyName("qris_revenue")
    @set:PropertyName("qris_revenue")
    var qrisRevenue: Long = 0L,

    @get:PropertyName("total_sold")
    @set:PropertyName("total_sold")
    var totalSold: Int = 0
) {
    init {
        require(cashRevenue >= 0L) { "Cash revenue must not be negative: $cashRevenue" }
        require(qrisRevenue >= 0L) { "QRIS revenue must not be negative: $qrisRevenue" }
        require(totalSold >= 0) { "Total sold must not be negative: $totalSold" }
    }

    /**
     * Total combined revenue (cash + QRIS)
     */
    val totalRevenue: Long get() = cashRevenue + qrisRevenue
}
