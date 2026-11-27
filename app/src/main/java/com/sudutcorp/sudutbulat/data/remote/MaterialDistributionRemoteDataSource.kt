package com.sudutcorp.sudutbulat.data.remote

import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.sudutcorp.sudutbulat.data.model.DistributionStatus
import com.sudutcorp.sudutbulat.data.model.MaterialDistribution
import com.sudutcorp.sudutbulat.data.model.MaterialDistributionItem
import com.sudutcorp.sudutbulat.data.remote.FirestoreMappers.toFirestoreItemMap
import com.sudutcorp.sudutbulat.data.remote.FirestoreMappers.toFirestoreMap
import com.sudutcorp.sudutbulat.data.remote.FirestoreMappers.toMaterialDistribution
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

open class MaterialDistributionRemoteDataSource(
    firestore: FirebaseFirestore? = null
) {
    private val db: FirebaseFirestore? by lazy {
        firestore ?: runCatching { FirebaseFirestore.getInstance() }.getOrNull()
    }

    private val distributionsCollection
        get() = db?.collection(FirestoreCollections.MATERIAL_DISTRIBUTIONS)

    open fun getDistributionsFlow(outletId: String? = null): Flow<List<MaterialDistribution>> = callbackFlow {
        val col = distributionsCollection
        if (col == null) {
            close(IllegalStateException("Layanan Firestore tidak tersedia."))
            return@callbackFlow
        }

        // Avoid requiring a composite index when filtering by outlet_id; sort client-side when filtered
        val query: Query = if (!outletId.isNullOrBlank()) {
            col.whereEqualTo("outlet_id", outletId)
        } else {
            col.orderBy("created_at", Query.Direction.DESCENDING)
        }

        val registration = query.addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }
            val items = snapshot?.documents
                ?.map { it.toMaterialDistribution() }
                ?.sortedByDescending { it.createdAt.seconds }
                ?: emptyList()
            trySend(items)
        }

        awaitClose { registration.remove() }
    }

    open suspend fun getDistributions(
        outletId: String? = null,
        status: DistributionStatus? = null
    ): Result<List<MaterialDistribution>> = runCatching {
        val col = distributionsCollection ?: throw IllegalStateException("Layanan Firestore tidak tersedia.")
        var query: Query = col
        if (!outletId.isNullOrBlank()) {
            query = query.whereEqualTo("outlet_id", outletId)
        }
        if (status != null) {
            query = query.whereEqualTo("status", status.name)
        }
        val snapshot = query.get().await()
        snapshot.documents.map { it.toMaterialDistribution() }.sortedByDescending { it.createdAt.seconds }
    }

    open suspend fun getDistributionById(id: String): Result<MaterialDistribution?> = runCatching {
        val col = distributionsCollection ?: throw IllegalStateException("Layanan Firestore tidak tersedia.")
        val snapshot = col.document(id).get().await()
        if (snapshot.exists()) snapshot.toMaterialDistribution() else null
    }

    open fun getDistributionFlow(id: String): Flow<MaterialDistribution?> = callbackFlow {
        val col = distributionsCollection
        if (col == null) {
            close(IllegalStateException("Layanan Firestore tidak tersedia."))
            return@callbackFlow
        }

        val registration = col.document(id).addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }
            trySend(snapshot?.takeIf { it.exists() }?.toMaterialDistribution())
        }

        awaitClose { registration.remove() }
    }

    open suspend fun getDistributionItems(distributionId: String): Result<List<MaterialDistributionItem>> = runCatching {
        getDistributionById(distributionId).getOrThrow()?.items ?: emptyList()
    }

    open fun getDistributionItemsFlow(distributionId: String): Flow<List<MaterialDistributionItem>> =
        getDistributionFlow(distributionId).map { it?.items ?: emptyList() }

    open fun getAllDistributionItemsFlow(): Flow<List<MaterialDistributionItem>> =
        getDistributionsFlow(outletId = null).map { list -> list.flatMap { it.items } }

    /**
     * Atomically creates a MaterialDistribution document with embedded line items.
     */
    open suspend fun createDistribution(
        distribution: MaterialDistribution,
        items: List<MaterialDistributionItem>
    ): Result<String> = runCatching {
        val distCol = distributionsCollection ?: throw IllegalStateException("Layanan Firestore tidak tersedia.")
        val distDoc = if (distribution.id.isNotBlank()) distCol.document(distribution.id) else distCol.document()
        val distId = distDoc.id
        val normalizedItems = items.mapIndexed { idx, item ->
            item.copy(
                id = item.id.ifBlank { "${distId}_${item.materialId.ifBlank { (idx + 1).toString() }}" },
                distributionId = distId
            )
        }
        val distToSave = distribution.copy(id = distId, items = normalizedItems)
        distDoc.set(distToSave.toFirestoreMap()).await()
        distId
    }

    open suspend fun updateDistributionStatus(
        id: String,
        status: DistributionStatus,
        confirmedBy: String? = null,
        confirmationNote: String? = null,
        sentAt: Timestamp? = null,
        confirmedAt: Timestamp? = null
    ): Result<Unit> = runCatching {
        val col = distributionsCollection ?: throw IllegalStateException("Layanan Firestore tidak tersedia.")
        val updates = mutableMapOf<String, Any>("status" to status.name)
        if (confirmedBy != null) updates["confirmed_by"] = confirmedBy
        if (confirmationNote != null) updates["confirmation_note"] = confirmationNote
        if (sentAt != null) updates["sent_at"] = sentAt
        if (confirmedAt != null) updates["confirmed_at"] = confirmedAt

        col.document(id).update(updates).await()
    }

    /**
     * Atomically confirms distribution receipt in a single document update,
     * updating status, notes, and embedded items array.
     */
    open suspend fun confirmDistribution(
        distributionId: String,
        confirmedBy: String,
        confirmationNote: String?,
        updatedItems: List<MaterialDistributionItem>,
        status: DistributionStatus = DistributionStatus.RECEIVED
    ): Result<Unit> = runCatching {
        val distCol = distributionsCollection ?: throw IllegalStateException("Layanan Firestore tidak tersedia.")
        val updates = mutableMapOf<String, Any>(
            "status" to status.name,
            "confirmed_by" to confirmedBy,
            "confirmed_at" to Timestamp.now(),
            "items" to updatedItems.map { it.toFirestoreItemMap() }
        )
        if (confirmationNote != null) {
            updates["confirmation_note"] = confirmationNote
        }
        distCol.document(distributionId).update(updates).await()
    }

    open suspend fun deleteDistribution(id: String): Result<Unit> = runCatching {
        val col = distributionsCollection ?: throw IllegalStateException("Layanan Firestore tidak tersedia.")
        col.document(id).delete().await()
    }
}
