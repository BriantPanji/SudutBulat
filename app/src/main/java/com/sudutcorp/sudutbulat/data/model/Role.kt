package com.sudutcorp.sudutbulat.data.model

import com.google.firebase.firestore.PropertyName

enum class Role(val firestoreValue: String, val displayName: String) {
    @PropertyName("dev")
    DEV("dev", "Developer"),

    @PropertyName("owner")
    OWNER("owner", "Owner"),

    @PropertyName("warehouse")
    WAREHOUSE("warehouse", "Admin Gudang"),

    @PropertyName("outlet")
    OUTLET("outlet", "Staf Outlet");

    companion object {
        val GUDANG: Role get() = WAREHOUSE

        fun fromString(raw: String?): Role? {
            return when (raw?.trim()?.lowercase()) {
                "dev", "developer" -> DEV
                "owner" -> OWNER
                "warehouse", "gudang", "admin" -> WAREHOUSE
                "outlet", "staff" -> OUTLET
                else -> null
            }
        }

        fun fromEmailOrCode(input: String?): Role? {
            val clean = input?.trim()?.lowercase() ?: return null
            return when (clean) {
                "sdb.dev@gmail.com", "dev" -> DEV
                "sdb.owner@gmail.com", "owner" -> OWNER
                "sdb.gudang@gmail.com", "gudang", "admin", "warehouse" -> WAREHOUSE
                "sdb.staff@gmail.com", "staff", "outlet" -> OUTLET
                else -> null
            }
        }

        fun emailForLoginCode(input: String?): String? {
            return when (input?.trim()?.lowercase()) {
                "dev", "sdb.dev@gmail.com" -> "sdb.dev@gmail.com"
                "owner", "sdb.owner@gmail.com" -> "sdb.owner@gmail.com"
                "gudang", "admin", "warehouse", "sdb.gudang@gmail.com" -> "sdb.gudang@gmail.com"
                "staff", "outlet", "sdb.staff@gmail.com" -> "sdb.staff@gmail.com"
                else -> null
            }
        }
    }
}
