package com.sudutcorp.sudutbulat.data.model

import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.PropertyName

/**
 * Data model for users in Firestore collection `users`.
 * Document ID typically corresponds to the user's login code / UID.
 */
data class User(
    @DocumentId
    val id: String = "",

    val name: String = "",

    val email: String = "",

    val username: String = "",

    val role: Role = Role.OUTLET,

    @get:PropertyName("outlet_id")
    @set:PropertyName("outlet_id")
    var outletId: String? = null,

    @get:PropertyName("is_active")
    @set:PropertyName("is_active")
    var isActive: Boolean = true
) {
    // Backwards-compatibility getter for uid
    val uid: String get() = id
}
