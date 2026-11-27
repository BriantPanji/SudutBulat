package com.sudutcorp.sudutbulat.data.repository

import com.google.firebase.Timestamp
import com.sudutcorp.sudutbulat.data.model.DistributionStatus
import com.sudutcorp.sudutbulat.data.model.DistributionWithItems
import com.sudutcorp.sudutbulat.data.model.MaterialDistribution
import com.sudutcorp.sudutbulat.data.model.MaterialDistributionItem
import com.sudutcorp.sudutbulat.data.remote.MaterialDistributionRemoteDataSource
import kotlinx.coroutines.flow.Flow

/**
 * Repository for managing warehouse-to-outlet material distributions and confirmations.
 * Wraps [MaterialDistributionRemoteDataSource].
 */
class MaterialDistributionRepository(
    private val remoteDataSource: MaterialDistributionRemoteDataSource
) {
    fun getDistributionsFlow(outletId: String? = null): Flow<List<MaterialDistribution>> {
        return remoteDataSource.getDistributionsFlow(outletId)
    }

    suspend fun getDistributions(
        outletId: String? = null,
        status: DistributionStatus? = null
    ): Result<List<MaterialDistribution>> {
        return remoteDataSource.getDistributions(outletId, status)
    }

    suspend fun getDistributionById(id: String): Result<MaterialDistribution?> {
        return remoteDataSource.getDistributionById(id)
    }

    fun getDistributionFlow(id: String): Flow<MaterialDistribution?> {
        return remoteDataSource.getDistributionFlow(id)
    }

    suspend fun getDistributionItems(distributionId: String): Result<List<MaterialDistributionItem>> {
        return remoteDataSource.getDistributionItems(distributionId)
    }

    fun getDistributionItemsFlow(distributionId: String): Flow<List<MaterialDistributionItem>> {
        return remoteDataSource.getDistributionItemsFlow(distributionId)
    }

    fun getAllDistributionItemsFlow(): Flow<List<MaterialDistributionItem>> {
        return remoteDataSource.getAllDistributionItemsFlow()
    }

    /**
     * Retrieves distribution header and its line items together.
     * Returns null if distribution with [distributionId] does not exist.
     */
    suspend fun getDistributionWithItems(distributionId: String): Result<DistributionWithItems?> = runCatching {
        val dist = remoteDataSource.getDistributionById(distributionId).getOrThrow() ?: return@runCatching null
        val items = dist.items.ifEmpty { remoteDataSource.getDistributionItems(distributionId).getOrThrow() }
        DistributionWithItems(distribution = dist, items = items)
    }

    /**
     * Atomically creates a distribution and all its associated line items.
     */
    suspend fun createDistribution(
        distribution: MaterialDistribution,
        items: List<MaterialDistributionItem>
    ): Result<String> {
        return remoteDataSource.createDistribution(distribution, items)
    }

    suspend fun updateDistributionStatus(
        id: String,
        status: DistributionStatus,
        confirmedBy: String? = null,
        confirmationNote: String? = null,
        sentAt: Timestamp? = null,
        confirmedAt: Timestamp? = null
    ): Result<Unit> {
        return remoteDataSource.updateDistributionStatus(
            id = id,
            status = status,
            confirmedBy = confirmedBy,
            confirmationNote = confirmationNote,
            sentAt = sentAt,
            confirmedAt = confirmedAt
        )
    }

    /**
     * Confirms distribution receipt, updating status, notes, and received quantities on each item.
     */
    suspend fun confirmDistribution(
        distributionId: String,
        confirmedBy: String,
        confirmationNote: String?,
        updatedItems: List<MaterialDistributionItem>,
        status: DistributionStatus = DistributionStatus.RECEIVED
    ): Result<Unit> {
        return remoteDataSource.confirmDistribution(
            distributionId = distributionId,
            confirmedBy = confirmedBy,
            confirmationNote = confirmationNote,
            updatedItems = updatedItems,
            status = status
        )
    }

    suspend fun deleteDistribution(id: String): Result<Unit> {
        return remoteDataSource.deleteDistribution(id)
    }
}
