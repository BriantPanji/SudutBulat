package com.sudutcorp.sudutbulat.data.repository

import com.sudutcorp.sudutbulat.data.model.Material
import com.sudutcorp.sudutbulat.data.remote.MaterialRemoteDataSource
import kotlinx.coroutines.flow.Flow

/**
 * Repository for managing raw material / inventory items.
 * Wraps [MaterialRemoteDataSource].
 */
class MaterialRepository(
    private val remoteDataSource: MaterialRemoteDataSource
) {
    fun getMaterialsFlow(): Flow<List<Material>> {
        return remoteDataSource.getMaterialsFlow()
    }

    suspend fun getMaterials(): Result<List<Material>> {
        return remoteDataSource.getMaterials()
    }

    suspend fun getMaterialById(materialId: String): Result<Material?> {
        return remoteDataSource.getMaterialById(materialId)
    }

    suspend fun saveMaterial(material: Material): Result<String> {
        return remoteDataSource.saveMaterial(material)
    }

    suspend fun updateMaterial(material: Material): Result<Unit> {
        return remoteDataSource.updateMaterial(material)
    }

    suspend fun deleteMaterial(materialId: String): Result<Unit> {
        return remoteDataSource.deleteMaterial(materialId)
    }
}
