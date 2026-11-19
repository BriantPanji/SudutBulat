package com.sudutcorp.sudutbulat.data.remote

import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.sudutcorp.sudutbulat.data.model.DistributionStatus
import com.sudutcorp.sudutbulat.data.model.MaterialDistribution
import com.sudutcorp.sudutbulat.data.model.MaterialDistributionItem
import com.sudutcorp.sudutbulat.data.remote.FirestoreMappers.toFirestoreMap
import com.sudutcorp.sudutbulat.data.remote.FirestoreMappers.toMaterialDistribution
import com.sudutcorp.sudutbulat.data.remote.FirestoreMappers.toMaterialDistributionItem
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

open class MaterialDistributionRemoteDataSource(
    firestore: FirebaseFirestore? = null
) {
    private val db: FirebaseFirestore? by lazy {
        firestore ?: runCatching { FirebaseFirestore.getInstance() }.getOrNull()
    }

    private val distributionsCollection
        get() = db?.collection(FirestoreCollections.MATERIAL_DISTRIBUTIONS)

    private val distributionItemsCollection
        get() = db?.collection(FirestoreCollections.MATERIAL_DISTRIBUTION_ITEMS)

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
        val itemCol = distributionItemsCollection ?: throw IllegalStateException("Layanan Firestore tidak tersedia.")
        val snapshot = itemCol.whereEqualTo("distribution_id", distributionId).get().await()
        snapshot.documents.map { it.toMaterialDistributionItem() }
    }

    open fun getDistributionItemsFlow(distributionId: String): Flow<List<MaterialDistributionItem>> = callbackFlow {
        val itemCol = distributionItemsCollection
        if (itemCol == null) {
            close(IllegalStateException("Layanan Firestore tidak tersedia."))
            return@callbackFlow
        }

        val registration = itemCol.whereEqualTo("distribution_id", distributionId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val items = snapshot?.documents?.map { it.toMaterialDistributionItem() } ?: emptyList()
                trySend(items)
            }

        awaitClose { registration.remove() }
    }

    open fun getAllDistributionItemsFlow(): Flow<List<MaterialDistributionItem>> = callbackFlow {
        val itemCol = distributionItemsCollection
        if (itemCol == null) {
            close(IllegalStateException("Layanan Firestore tidak tersedia."))
            return@callbackFlow
        }

        val registration = itemCol.addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }
            val items = snapshot?.documents?.map { it.toMaterialDistributionItem() } ?: emptyList()
            trySend(items)
        }

        awaitClose { registration.remove() }
    }

    /**
     * Atomically creates a MaterialDistribution and all associated line items using Firestore WriteBatch.
     */
    open suspend fun createDistribution(
        distribution: MaterialDistribution,
        items: List<MaterialDistributionItem>
    ): Result<String> = runCatching {
        val firestore = db ?: throw IllegalStateException("Layanan Firestore tidak tersedia.")
        val distCol = distributionsCollection ?: throw IllegalStateException("Layanan Firestore tidak tersedia.")
        val itemCol = distributionItemsCollection ?: throw IllegalStateException("Layanan Firestore tidak tersedia.")

        val batch = firestore.batch()

        val distDoc = if (distribution.id.isNotBlank()) distCol.document(distribution.id) else distCol.document()
        val distToSave = if (distribution.id.isBlank()) distribution.copy(id = distDoc.id) else distribution
        batch.set(distDoc, distToSave.toFirestoreMap())

        items.forEach { item ->
            val itemDoc = if (item.id.isNotBlank()) itemCol.document(item.id) else itemCol.document()
            val itemToSave = item.copy(
                id = itemDoc.id,
                distributionId = distDoc.id
            )
            batch.set(itemDoc, itemToSave.toFirestoreMap())
        }

        batch.commit().await()
        distDoc.id
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
     * Atomically confirms distribution receipt, updating header status & notes
     * along with received quantities on each item.
     */
    open suspend fun confirmDistribution(
        distributionId: String,
        confirmedBy: String,
        confirmationNote: String?,
        updatedItems: List<MaterialDistributionItem>,
        status: DistributionStatus = DistributionStatus.RECEIVED
    ): Result<Unit> = runCatching {
        val firestore = db ?: throw IllegalStateException("Layanan Firestore tidak tersedia.")
        val distCol = distributionsCollection ?: throw IllegalStateException("Layanan Firestore tidak tersedia.")
        val itemCol = distributionItemsCollection ?: throw IllegalStateException("Layanan Firestore tidak tersedia.")

        val batch = firestore.batch()

        val headerRef = distCol.document(distributionId)
        val headerUpdates = mutableMapOf<String, Any>(
            "status" to status.name,
            "confirmed_by" to confirmedBy,
            "confirmed_at" to Timestamp.now()
        )
        if (confirmationNote != null) {
            headerUpdates["confirmation_note"] = confirmationNote
        }
        batch.update(headerRef, headerUpdates)

        updatedItems.forEach { item ->
            require(item.id.isNotBlank()) { "Item ID tidak boleh kosong saat konfirmasi." }
            val itemRef = itemCol.document(item.id)
            val itemUpdates = mutableMapOf<String, Any>()
            if (item.quantityReceived != null) {
                itemUpdates["quantity_received"] = item.quantityReceived!!
            }
            if (item.note != null) {
                itemUpdates["note"] = item.note
            }
            if (itemUpdates.isNotEmpty()) {
                batch.update(itemRef, itemUpdates)
            }
        }

        batch.commit().await()
    }
}
