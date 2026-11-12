package com.sudutcorp.sudutbulat.data.model

import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.GeoPoint
import com.google.firebase.firestore.PropertyName

/**
 * Data model for outlets in Firestore collection `outlets`.
 */
data class Outlet(
    @DocumentId
    val id: String = "",

    val name: String = "",

    val address: String = "",

    val coordinate: GeoPoint = GeoPoint(0.0, 0.0),

    @get:PropertyName("is_active")
    @set:PropertyName("is_active")
    var isActive: Boolean = true,

    // Optional UI / dashboard compatibility fields
    @get:PropertyName("is_open")
    @set:PropertyName("is_open")
    var isOpen: Boolean = true,

    @get:PropertyName("target_percentage")
    @set:PropertyName("target_percentage")
    var targetPercentage: Float = 0f,

    @get:PropertyName("total_revenue")
    @set:PropertyName("total_revenue")
    var totalRevenue: Long = 0L,

    @get:PropertyName("total_pieces_sold")
    @set:PropertyName("total_pieces_sold")
    var totalPiecesSold: Int = 0
)
