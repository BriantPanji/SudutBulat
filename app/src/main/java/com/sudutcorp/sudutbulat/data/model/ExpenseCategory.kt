package com.sudutcorp.sudutbulat.data.model

import com.google.firebase.firestore.PropertyName

enum class ExpenseCategory {
    @PropertyName("GAS")
    GAS,

    @PropertyName("PLASTIC_BAG")
    PLASTIC_BAG,

    @PropertyName("OTHER")
    OTHER
}
