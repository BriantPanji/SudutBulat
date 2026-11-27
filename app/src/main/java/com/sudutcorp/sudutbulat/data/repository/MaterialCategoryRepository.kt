package com.sudutcorp.sudutbulat.data.repository

import com.sudutcorp.sudutbulat.data.model.MaterialCategory
import com.sudutcorp.sudutbulat.data.remote.MaterialCategoryRemoteDataSource
import kotlinx.coroutines.flow.Flow

/**
 * Repository for managing material categories (`material_categories`).
 */
class MaterialCategoryRepository(
    private val remoteDataSource: MaterialCategoryRemoteDataSource
) {
    fun getCategoriesFlow(): Flow<List<MaterialCategory>> =
        remoteDataSource.getCategoriesFlow()

    suspend fun findOrCreateByName(name: String): Result<MaterialCategory> =
        remoteDataSource.findOrCreateByName(name)
}
