package com.sudutcorp.sudutbulat.data.model

import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.PropertyName
import java.util.Locale

/**
 * Data model for material categories in Firestore collection `material_categories`.
 */
data class MaterialCategory(
    @DocumentId
    val id: String = "",

    val name: String = "",

    @get:PropertyName("name_lower")
    @set:PropertyName("name_lower")
    var nameLower: String = name.trim().lowercase(Locale.ROOT)
)
