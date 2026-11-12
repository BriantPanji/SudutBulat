package com.sudutcorp.sudutbulat.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.PropertyName

/**
 * Data model for material distributions in Firestore collection `material_distributions`.
 *
 * @property confirmedBy User ID who confirmed the delivery; nullable until confirmed.
 * @property sentAt Timestamp when distribution was sent; nullable until sent.
 * @property confirmedAt Timestamp when distribution was confirmed; nullable until confirmed.
 */
data class MaterialDistribution(
    @DocumentId
    val id: String = "",

    @get:PropertyName("outlet_id")
    @set:PropertyName("outlet_id")
    var outletId: String = "",

    @get:PropertyName("created_by")
    @set:PropertyName("created_by")
    var createdBy: String = "",

    @get:PropertyName("confirmed_by")
    @set:PropertyName("confirmed_by")
    var confirmedBy: String? = null,

    val status: DistributionStatus = DistributionStatus.DRAFT,

    val note: String? = null,

    @get:PropertyName("confirmation_note")
    @set:PropertyName("confirmation_note")
    var confirmationNote: String? = null,

    @get:PropertyName("created_at")
    @set:PropertyName("created_at")
    var createdAt: Timestamp = Timestamp.now(),

    @get:PropertyName("sent_at")
    @set:PropertyName("sent_at")
    var sentAt: Timestamp? = null,

    @get:PropertyName("confirmed_at")
    @set:PropertyName("confirmed_at")
    var confirmedAt: Timestamp? = null
) {
    init {
        require(note == null || note.length <= 512) { "Note must not exceed 512 characters" }
        val cNote = confirmationNote
        require(cNote == null || cNote.length <= 512) { "Confirmation note must not exceed 512 characters" }
    }
}
