package com.sudutcorp.sudutbulat.data.remote

import com.google.firebase.firestore.FirebaseFirestore
import com.sudutcorp.sudutbulat.data.model.Material
import com.sudutcorp.sudutbulat.data.remote.FirestoreMappers.toFirestoreMap
import com.sudutcorp.sudutbulat.data.remote.FirestoreMappers.toMaterial
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

open class MaterialRemoteDataSource(
    firestore: FirebaseFirestore? = null
) {
    private val db: FirebaseFirestore? by lazy {
        firestore ?: runCatching { FirebaseFirestore.getInstance() }.getOrNull()
    }

    private val materialsCollection
        get() = db?.collection(FirestoreCollections.MATERIALS)

    open fun getMaterialsFlow(): Flow<List<Material>> = callbackFlow {
        val col = materialsCollection
        if (col == null) {
            close(IllegalStateException("Layanan Firestore tidak tersedia."))
            return@callbackFlow
        }

        val registration = col.addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }
            val materials = snapshot?.documents?.map { it.toMaterial() } ?: emptyList()
            trySend(materials)
        }

        awaitClose { registration.remove() }
    }

    open suspend fun getMaterials(): Result<List<Material>> = runCatching {
        val col = materialsCollection ?: throw IllegalStateException("Layanan Firestore tidak tersedia.")
        val snapshot = col.get().await()
        snapshot.documents.map { it.toMaterial() }
    }

    open suspend fun getMaterialById(materialId: String): Result<Material?> = runCatching {
        val col = materialsCollection ?: throw IllegalStateException("Layanan Firestore tidak tersedia.")
        val snapshot = col.document(materialId).get().await()
        if (snapshot.exists()) snapshot.toMaterial() else null
    }

    open suspend fun saveMaterial(material: Material): Result<String> = runCatching {
        val col = materialsCollection ?: throw IllegalStateException("Layanan Firestore tidak tersedia.")
        val docRef = if (material.id.isNotBlank()) col.document(material.id) else col.document()
        val dataToSave = if (material.id.isBlank()) material.copy(id = docRef.id) else material
        docRef.set(dataToSave.toFirestoreMap()).await()
        docRef.id
    }

    open suspend fun updateMaterial(material: Material): Result<Unit> = runCatching {
        val col = materialsCollection ?: throw IllegalStateException("Layanan Firestore tidak tersedia.")
        require(material.id.isNotBlank()) { "Material ID tidak boleh kosong saat memperbarui bahan." }
        col.document(material.id).set(material.toFirestoreMap()).await()
    }

    open suspend fun setMaterialActive(materialId: String, isActive: Boolean): Result<Unit> = runCatching {
        val col = materialsCollection ?: throw IllegalStateException("Layanan Firestore tidak tersedia.")
        col.document(materialId).update("is_active", isActive).await()
    }

    open suspend fun deleteMaterial(materialId: String): Result<Unit> = runCatching {
        val col = materialsCollection ?: throw IllegalStateException("Layanan Firestore tidak tersedia.")
        col.document(materialId).delete().await()
    }
}
