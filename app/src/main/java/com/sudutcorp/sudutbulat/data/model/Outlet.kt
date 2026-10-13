package com.sudutcorp.sudutbulat.data.model

data class Outlet(
    val id: String = "",
    val name: String = "",
    val address: String = "",
    val isOpen: Boolean = true,
    val targetPercentage: Float = 0f,
    val totalRevenue: Long = 0L,
    val totalPiecesSold: Int = 0
)
