package com.sudutcorp.sudutbulat.data.remote

import com.google.firebase.firestore.FirebaseFirestore
import com.sudutcorp.sudutbulat.data.model.Role
import com.sudutcorp.sudutbulat.data.model.User
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class UserRemoteDataSource(
    firestore: FirebaseFirestore? = null
) {
    private val db: FirebaseFirestore? by lazy {
        firestore ?: runCatching { FirebaseFirestore.getInstance() }.getOrNull()
    }

    private val usersCollection
        get() = db?.collection(FirestoreCollections.USERS)

    suspend fun getUserById(userId: String): Result<User?> = runCatching {
        val col = usersCollection ?: throw IllegalStateException("Layanan Firestore tidak tersedia.")
        val snapshot = col.document(userId).get().await()
        if (snapshot.exists()) snapshot.toObject(User::class.java) else null
    }

    fun getUserFlow(userId: String): Flow<User?> = callbackFlow {
        val col = usersCollection
        if (col == null) {
            trySend(null)
            close()
            return@callbackFlow
        }

        val registration = col.document(userId).addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }
            trySend(snapshot?.toObject(User::class.java))
        }

        awaitClose { registration.remove() }
    }

    suspend fun getAllUsers(): Result<List<User>> = runCatching {
        val col = usersCollection ?: throw IllegalStateException("Layanan Firestore tidak tersedia.")
        val snapshot = col.get().await()
        snapshot.toObjects(User::class.java)
    }

    suspend fun getUsersByRole(role: Role): Result<List<User>> = runCatching {
        val col = usersCollection ?: throw IllegalStateException("Layanan Firestore tidak tersedia.")
        val snapshot = col.whereEqualTo("role", role.name).get().await()
        snapshot.toObjects(User::class.java)
    }

    suspend fun getUsersByOutlet(outletId: String): Result<List<User>> = runCatching {
        val col = usersCollection ?: throw IllegalStateException("Layanan Firestore tidak tersedia.")
        val snapshot = col.whereEqualTo("outlet_id", outletId).get().await()
        snapshot.toObjects(User::class.java)
    }

    suspend fun saveUser(user: User): Result<Unit> = runCatching {
        val col = usersCollection ?: throw IllegalStateException("Layanan Firestore tidak tersedia.")
        val docRef = if (user.id.isNotBlank()) {
            col.document(user.id)
        } else {
            col.document()
        }
        val dataToSave = if (user.id.isBlank()) user.copy(id = docRef.id) else user
        docRef.set(dataToSave).await()
    }

    suspend fun updateUser(user: User): Result<Unit> = runCatching {
        val col = usersCollection ?: throw IllegalStateException("Layanan Firestore tidak tersedia.")
        require(user.id.isNotBlank()) { "User ID tidak boleh kosong saat memperbarui data." }
        col.document(user.id).set(user).await()
    }

    suspend fun setUserActive(userId: String, isActive: Boolean): Result<Unit> = runCatching {
        val col = usersCollection ?: throw IllegalStateException("Layanan Firestore tidak tersedia.")
        col.document(userId).update("is_active", isActive).await()
    }
}
