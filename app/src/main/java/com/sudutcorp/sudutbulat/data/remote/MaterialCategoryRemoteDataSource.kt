package com.sudutcorp.sudutbulat.data.remote

import com.google.firebase.firestore.FirebaseFirestore
import com.sudutcorp.sudutbulat.data.model.MaterialCategory
import com.sudutcorp.sudutbulat.data.remote.FirestoreMappers.toFirestoreMap
import com.sudutcorp.sudutbulat.data.remote.FirestoreMappers.toMaterialCategory
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.util.Locale

open class MaterialCategoryRemoteDataSource(
    firestore: FirebaseFirestore? = null
) {
    private val db: FirebaseFirestore? by lazy {
        firestore ?: runCatching { FirebaseFirestore.getInstance() }.getOrNull()
    }

    private val categoriesCollection
        get() = db?.collection(FirestoreCollections.MATERIAL_CATEGORIES)

    open fun getCategoriesFlow(): Flow<List<MaterialCategory>> = callbackFlow {
        val col = categoriesCollection
        if (col == null) {
            close(IllegalStateException("Layanan Firestore tidak tersedia."))
            return@callbackFlow
        }

        val registration = col.addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }
            val categories = snapshot?.documents
                ?.map { it.toMaterialCategory() }
                ?.filter { it.name.isNotBlank() }
                ?.sortedBy { it.nameLower }
                ?: emptyList()
            trySend(categories)
        }

        awaitClose { registration.remove() }
    }

    open suspend fun findOrCreateByName(rawName: String): Result<MaterialCategory> = runCatching {
        val col = categoriesCollection ?: throw IllegalStateException("Layanan Firestore tidak tersedia.")
        val normalizedName = normalizeCategoryDisplayName(rawName)
        require(normalizedName.isNotBlank()) { "Nama kategori tidak boleh kosong." }
        val lower = normalizedName.lowercase(Locale.ROOT)

        // 1. Indexed case-insensitive lookup via name_lower
        val byLower = col.whereEqualTo("name_lower", lower).limit(1).get().await()
        byLower.documents.firstOrNull()?.let { return@runCatching it.toMaterialCategory() }

        // 2. Fallback scan in case a document was created manually without name_lower
        val allDocs = col.get().await().documents.map { it.toMaterialCategory() }
        allDocs.firstOrNull {
            it.nameLower.equals(lower, ignoreCase = true) || it.name.trim().equals(lower, ignoreCase = true)
        }?.let { return@runCatching it }

        // 3. Not found: create deterministically with cat_<slug>
        val slug = lower.replace(Regex("[^a-z0-9]+"), "_").trim('_')
            .ifBlank { "${System.currentTimeMillis()}" }
        val docId = "cat_$slug"
        val created = MaterialCategory(id = docId, name = normalizedName, nameLower = lower)
        col.document(docId).set(created.toFirestoreMap()).await()
        created
    }

    companion object {
        fun normalizeCategoryDisplayName(raw: String): String {
            val collapsed = raw.trim().replace(Regex("\\s+"), " ")
            if (collapsed.isBlank()) return ""
            return if (collapsed == collapsed.lowercase(Locale.ROOT)) {
                collapsed.replaceFirstChar { it.titlecase(Locale.ROOT) }
            } else {
                collapsed
            }
        }
    }
}
