package com.sudutcorp.sudutbulat.data.repository

import com.sudutcorp.sudutbulat.data.model.Outlet
import com.sudutcorp.sudutbulat.data.remote.OutletRemoteDataSource
import kotlinx.coroutines.flow.Flow

/**
 * Repository for managing outlet branch data and operations.
 * Wraps [OutletRemoteDataSource] to provide a clean API.
 */
class OutletRepository(
    private val remoteDataSource: OutletRemoteDataSource
) {
    fun getOutletsFlow(onlyActive: Boolean = false): Flow<List<Outlet>> {
        return remoteDataSource.getOutletsFlow(onlyActive)
    }

    suspend fun getOutlets(onlyActive: Boolean = false): Result<List<Outlet>> {
        return remoteDataSource.getOutlets(onlyActive)
    }

    suspend fun getOutletById(outletId: String): Result<Outlet?> {
        return remoteDataSource.getOutletById(outletId)
    }

    fun getOutletFlow(outletId: String): Flow<Outlet?> {
        return remoteDataSource.getOutletFlow(outletId)
    }

    suspend fun saveOutlet(outlet: Outlet): Result<String> {
        return remoteDataSource.saveOutlet(outlet)
    }

    suspend fun updateOutlet(outlet: Outlet): Result<Unit> {
        return remoteDataSource.updateOutlet(outlet)
    }

    suspend fun setOutletActive(outletId: String, isActive: Boolean): Result<Unit> {
        return remoteDataSource.setOutletActive(outletId, isActive)
    }

    suspend fun setOutletOpen(outletId: String, isOpen: Boolean): Result<Unit> {
        return remoteDataSource.setOutletOpen(outletId, isOpen)
    }
}
