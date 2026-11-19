package com.sudutcorp.sudutbulat.data.remote

import com.google.firebase.firestore.FirebaseFirestore
import com.sudutcorp.sudutbulat.data.model.Product
import com.sudutcorp.sudutbulat.data.remote.FirestoreMappers.toFirestoreMap
import com.sudutcorp.sudutbulat.data.remote.FirestoreMappers.toProduct
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

open class ProductRemoteDataSource(
    firestore: FirebaseFirestore? = null
) {
    private val db: FirebaseFirestore? by lazy {
        firestore ?: runCatching { FirebaseFirestore.getInstance() }.getOrNull()
    }

    private val productsCollection
        get() = db?.collection(FirestoreCollections.PRODUCTS)

    open fun getProductsFlow(onlyActive: Boolean = false): Flow<List<Product>> = callbackFlow {
        val col = productsCollection
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
            val products = snapshot?.documents?.map { it.toProduct() } ?: emptyList()
            trySend(products)
        }

        awaitClose { registration.remove() }
    }

    open suspend fun getProducts(onlyActive: Boolean = false): Result<List<Product>> = runCatching {
        val col = productsCollection ?: throw IllegalStateException("Layanan Firestore tidak tersedia.")
        val query = if (onlyActive) col.whereEqualTo("is_active", true) else col
        val snapshot = query.get().await()
        snapshot.documents.map { it.toProduct() }
    }

    open suspend fun getActiveProducts(): Result<List<Product>> = getProducts(onlyActive = true)

    open suspend fun getProductById(productId: String): Result<Product?> = runCatching {
        val col = productsCollection ?: throw IllegalStateException("Layanan Firestore tidak tersedia.")
        val snapshot = col.document(productId).get().await()
        if (snapshot.exists()) snapshot.toProduct() else null
    }

    open suspend fun saveProduct(product: Product): Result<String> = runCatching {
        val col = productsCollection ?: throw IllegalStateException("Layanan Firestore tidak tersedia.")
        val docRef = if (product.id.isNotBlank()) col.document(product.id) else col.document()
        val dataToSave = if (product.id.isBlank()) product.copy(id = docRef.id) else product
        docRef.set(dataToSave.toFirestoreMap()).await()
        docRef.id
    }

    open suspend fun updateProduct(product: Product): Result<Unit> = runCatching {
        val col = productsCollection ?: throw IllegalStateException("Layanan Firestore tidak tersedia.")
        require(product.id.isNotBlank()) { "Product ID tidak boleh kosong saat memperbarui produk." }
        col.document(product.id).set(product.toFirestoreMap()).await()
    }

    open suspend fun setProductActive(productId: String, isActive: Boolean): Result<Unit> = runCatching {
        val col = productsCollection ?: throw IllegalStateException("Layanan Firestore tidak tersedia.")
        col.document(productId).update("is_active", isActive).await()
    }

    open suspend fun deleteProduct(productId: String): Result<Unit> = runCatching {
        val col = productsCollection ?: throw IllegalStateException("Layanan Firestore tidak tersedia.")
        col.document(productId).delete().await()
    }
}
