package com.sudutcorp.sudutbulat.data.remote

import com.google.firebase.firestore.FirebaseFirestore
import com.sudutcorp.sudutbulat.data.model.Outlet
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class OutletRemoteDataSource(
    firestore: FirebaseFirestore? = null
) {
    private val db: FirebaseFirestore? by lazy {
        firestore ?: runCatching { FirebaseFirestore.getInstance() }.getOrNull()
    }

    private val outletsCollection
        get() = db?.collection(FirestoreCollections.OUTLETS)

    fun getOutletsFlow(onlyActive: Boolean = false): Flow<List<Outlet>> = callbackFlow {
        val col = outletsCollection
        if (col == null) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }

        val query = if (onlyActive) col.whereEqualTo("is_active", true) else col
        val registration = query.addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }
            val outlets = snapshot?.toObjects(Outlet::class.java) ?: emptyList()
            trySend(outlets)
        }

        awaitClose { registration.remove() }
    }

    suspend fun getOutlets(onlyActive: Boolean = false): Result<List<Outlet>> = runCatching {
        val col = outletsCollection ?: throw IllegalStateException("Layanan Firestore tidak tersedia.")
        val query = if (onlyActive) col.whereEqualTo("is_active", true) else col
        val snapshot = query.get().await()
        snapshot.toObjects(Outlet::class.java)
    }

    suspend fun getOutletById(outletId: String): Result<Outlet?> = runCatching {
        val col = outletsCollection ?: throw IllegalStateException("Layanan Firestore tidak tersedia.")
        val snapshot = col.document(outletId).get().await()
        if (snapshot.exists()) snapshot.toObject(Outlet::class.java) else null
    }

    fun getOutletFlow(outletId: String): Flow<Outlet?> = callbackFlow {
        val col = outletsCollection
        if (col == null) {
            trySend(null)
            close()
            return@callbackFlow
        }

        val registration = col.document(outletId).addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }
            trySend(snapshot?.toObject(Outlet::class.java))
        }

        awaitClose { registration.remove() }
    }

    suspend fun saveOutlet(outlet: Outlet): Result<String> = runCatching {
        val col = outletsCollection ?: throw IllegalStateException("Layanan Firestore tidak tersedia.")
        val docRef = if (outlet.id.isNotBlank()) {
            col.document(outlet.id)
        } else {
            col.document()
        }
        val dataToSave = if (outlet.id.isBlank()) outlet.copy(id = docRef.id) else outlet
        docRef.set(dataToSave).await()
        docRef.id
    }

    suspend fun updateOutlet(outlet: Outlet): Result<Unit> = runCatching {
        val col = outletsCollection ?: throw IllegalStateException("Layanan Firestore tidak tersedia.")
        require(outlet.id.isNotBlank()) { "Outlet ID tidak boleh kosong saat memperbarui outlet." }
        col.document(outlet.id).set(outlet).await()
    }

    suspend fun setOutletActive(outletId: String, isActive: Boolean): Result<Unit> = runCatching {
        val col = outletsCollection ?: throw IllegalStateException("Layanan Firestore tidak tersedia.")
        col.document(outletId).update("is_active", isActive).await()
    }

    suspend fun setOutletOpen(outletId: String, isOpen: Boolean): Result<Unit> = runCatching {
        val col = outletsCollection ?: throw IllegalStateException("Layanan Firestore tidak tersedia.")
        col.document(outletId).update("is_open", isOpen).await()
    }
}
