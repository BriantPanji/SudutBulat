package com.sudutcorp.sudutbulat.data.repository

import com.sudutcorp.sudutbulat.data.model.Product
import com.sudutcorp.sudutbulat.data.remote.ProductRemoteDataSource
import kotlinx.coroutines.flow.Flow

/**
 * Repository for managing product menu items.
 * Wraps [ProductRemoteDataSource].
 */
class ProductRepository(
    private val remoteDataSource: ProductRemoteDataSource
) {
    fun getProductsFlow(onlyActive: Boolean = false): Flow<List<Product>> {
        return remoteDataSource.getProductsFlow(onlyActive)
    }

    suspend fun getProducts(onlyActive: Boolean = false): Result<List<Product>> {
        return remoteDataSource.getProducts(onlyActive)
    }

    suspend fun getActiveProducts(): Result<List<Product>> {
        return remoteDataSource.getActiveProducts()
    }

    suspend fun getProductById(productId: String): Result<Product?> {
        return remoteDataSource.getProductById(productId)
    }

    suspend fun saveProduct(product: Product): Result<String> {
        return remoteDataSource.saveProduct(product)
    }

    suspend fun updateProduct(product: Product): Result<Unit> {
        return remoteDataSource.updateProduct(product)
    }

    suspend fun setProductActive(productId: String, isActive: Boolean): Result<Unit> {
        return remoteDataSource.setProductActive(productId, isActive)
    }

    suspend fun deleteProduct(productId: String): Result<Unit> {
        return remoteDataSource.deleteProduct(productId)
    }
}
