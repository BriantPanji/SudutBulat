package com.sudutcorp.sudutbulat.data.model

data class User(
    val uid: String = "",
    val email: String = "",
    val name: String = "",
    val role: Role = Role.OWNER
)
