package com.sudutcorp.sudutbulat.data.remote

import com.google.firebase.firestore.FirebaseFirestore
import com.sudutcorp.sudutbulat.data.model.Outlet
import com.sudutcorp.sudutbulat.data.model.RoutinePresetItem
import com.sudutcorp.sudutbulat.data.remote.FirestoreMappers.toFirestoreMap
import com.sudutcorp.sudutbulat.data.remote.FirestoreMappers.toOutlet
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

open class OutletRemoteDataSource(
    firestore: FirebaseFirestore? = null
) {
    private val db: FirebaseFirestore? by lazy {
        firestore ?: runCatching { FirebaseFirestore.getInstance() }.getOrNull()
    }

    private val outletsCollection
        get() = db?.collection(FirestoreCollections.OUTLETS)

    open fun getOutletsFlow(onlyActive: Boolean = false): Flow<List<Outlet>> = callbackFlow {
        val col = outletsCollection
        if (col == null) {
            close(IllegalStateException("Layanan Firestore tidak tersedia."))
            return@callbackFlow
        }

        val query = if (onlyActive) col.whereEqualTo("is_active", true) else col
        val registration = query.addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }
            val outlets = snapshot?.documents?.map { it.toOutlet() } ?: emptyList()
            trySend(outlets)
        }

        awaitClose { registration.remove() }
    }

    open suspend fun getOutlets(onlyActive: Boolean = false): Result<List<Outlet>> = runCatching {
        val col = outletsCollection ?: throw IllegalStateException("Layanan Firestore tidak tersedia.")
        val query = if (onlyActive) col.whereEqualTo("is_active", true) else col
        val snapshot = query.get().await()
        snapshot.documents.map { it.toOutlet() }
    }

    open suspend fun getOutletById(outletId: String): Result<Outlet?> = runCatching {
        val col = outletsCollection ?: throw IllegalStateException("Layanan Firestore tidak tersedia.")
        val snapshot = col.document(outletId).get().await()
        if (snapshot.exists()) snapshot.toOutlet() else null
    }

    open fun getOutletFlow(outletId: String): Flow<Outlet?> = callbackFlow {
        val col = outletsCollection
        if (col == null) {
            close(IllegalStateException("Layanan Firestore tidak tersedia."))
            return@callbackFlow
        }

        val registration = col.document(outletId).addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }
            trySend(snapshot?.takeIf { it.exists() }?.toOutlet())
        }

        awaitClose { registration.remove() }
    }

    open suspend fun saveOutlet(outlet: Outlet): Result<String> = runCatching {
        val col = outletsCollection ?: throw IllegalStateException("Layanan Firestore tidak tersedia.")
        val docRef = if (outlet.id.isNotBlank()) col.document(outlet.id) else col.document()
        val dataToSave = if (outlet.id.isBlank()) outlet.copy(id = docRef.id) else outlet
        docRef.set(dataToSave.toFirestoreMap()).await()
        docRef.id
    }

    open suspend fun updateOutlet(outlet: Outlet): Result<Unit> = runCatching {
        val col = outletsCollection ?: throw IllegalStateException("Layanan Firestore tidak tersedia.")
        require(outlet.id.isNotBlank()) { "Outlet ID tidak boleh kosong saat memperbarui outlet." }
        col.document(outlet.id).set(outlet.toFirestoreMap()).await()
    }

    open suspend fun setOutletActive(outletId: String, isActive: Boolean): Result<Unit> = runCatching {
        val col = outletsCollection ?: throw IllegalStateException("Layanan Firestore tidak tersedia.")
        col.document(outletId).update("is_active", isActive).await()
    }

    open suspend fun setOutletOpen(outletId: String, isOpen: Boolean): Result<Unit> = runCatching {
        val col = outletsCollection ?: throw IllegalStateException("Layanan Firestore tidak tersedia.")
        col.document(outletId).update("is_open", isOpen).await()
    }

    open suspend fun updateOutletDailySummary(
        outletId: String,
        totalRevenue: Long,
        totalPiecesSold: Int,
        targetPercentage: Float
    ): Result<Unit> = runCatching {
        val col = outletsCollection ?: throw IllegalStateException("Layanan Firestore tidak tersedia.")
        col.document(outletId).update(
            mapOf(
                "total_revenue" to totalRevenue,
                "total_pieces_sold" to totalPiecesSold,
                "target_percentage" to targetPercentage.toDouble()
            )
        ).await()
    }

    open suspend fun updateRoutinePreset(
        outletId: String,
        items: List<RoutinePresetItem>
    ): Result<Unit> = runCatching {
        val col = outletsCollection ?: throw IllegalStateException("Layanan Firestore tidak tersedia.")
        val normalized = items.filter { it.materialId.isNotBlank() && it.quantity > 0 }.map { it.toFirestoreMap() }
        col.document(outletId).update("routine_preset", normalized).await()
    }
}
