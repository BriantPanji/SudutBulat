package com.sudutcorp.sudutbulat.data.model

import com.google.firebase.firestore.PropertyName

enum class DeliveryMethod(val firestoreValue: String, val label: String) {
    @PropertyName("WAREHOUSE_COURIER")
    WAREHOUSE_COURIER("WAREHOUSE_COURIER", "Diantar Gudang"),

    @PropertyName("SELF_PICKUP")
    SELF_PICKUP("SELF_PICKUP", "Dijemput Outlet"),

    @PropertyName("GOJEK")
    GOJEK("GOJEK", "Ojol (Gojek)"),

    @PropertyName("GRAB")
    GRAB("GRAB", "Ojol (Grab)"),

    @PropertyName("MAXIM")
    MAXIM("MAXIM", "Ojol (Maxim)"),

    @PropertyName("OTHER")
    OTHER("OTHER", "Kurir Lainnya");

    companion object {
        fun fromString(raw: String?): DeliveryMethod {
            val clean = raw?.trim()?.uppercase() ?: return WAREHOUSE_COURIER
            return entries.firstOrNull {
                it.name == clean || it.firestoreValue == clean
            } ?: WAREHOUSE_COURIER
        }
    }
}
