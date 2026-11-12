package com.sudutcorp.sudutbulat.data.model

import com.google.firebase.firestore.PropertyName

enum class DistributionStatus {
    @PropertyName("DRAFT")
    DRAFT,

    @PropertyName("SENT")
    SENT,

    @PropertyName("RECEIVED")
    RECEIVED,

    @PropertyName("PARTIALLY_RECEIVED")
    PARTIALLY_RECEIVED,

    @PropertyName("NOT_RECEIVED")
    NOT_RECEIVED,

    @PropertyName("CANCELLED")
    CANCELLED
}
