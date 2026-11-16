package com.sudutcorp.sudutbulat.data

import com.sudutcorp.sudutbulat.data.auth.AuthRepository
import com.sudutcorp.sudutbulat.data.remote.DailyReportRemoteDataSource
import com.sudutcorp.sudutbulat.data.remote.MaterialDistributionRemoteDataSource
import com.sudutcorp.sudutbulat.data.remote.MaterialRemoteDataSource
import com.sudutcorp.sudutbulat.data.remote.OutletRemoteDataSource
import com.sudutcorp.sudutbulat.data.remote.ProductRemoteDataSource
import com.sudutcorp.sudutbulat.data.remote.UserRemoteDataSource
import com.sudutcorp.sudutbulat.data.repository.DailyReportRepository
import com.sudutcorp.sudutbulat.data.repository.MaterialDistributionRepository
import com.sudutcorp.sudutbulat.data.repository.MaterialRepository
import com.sudutcorp.sudutbulat.data.repository.OutletRepository
import com.sudutcorp.sudutbulat.data.repository.ProductRepository
import com.sudutcorp.sudutbulat.data.repository.UserRepository

/**
 * Dependency container providing single-point manual dependency injection.
 */
interface AppContainer {
    val authRepository: AuthRepository
    val userRepository: UserRepository
    val outletRepository: OutletRepository
    val materialRepository: MaterialRepository
    val productRepository: ProductRepository
    val materialDistributionRepository: MaterialDistributionRepository
    val dailyReportRepository: DailyReportRepository
}

/**
 * Default implementation of [AppContainer] that instantiates remote data sources
 * and injects them into repositories.
 */
class DefaultAppContainer : AppContainer {
    // Remote Data Sources (lazy loaded)
    private val userRemoteDataSource by lazy { UserRemoteDataSource() }
    private val outletRemoteDataSource by lazy { OutletRemoteDataSource() }
    private val materialRemoteDataSource by lazy { MaterialRemoteDataSource() }
    private val productRemoteDataSource by lazy { ProductRemoteDataSource() }
    private val materialDistributionRemoteDataSource by lazy { MaterialDistributionRemoteDataSource() }
    private val dailyReportRemoteDataSource by lazy { DailyReportRemoteDataSource() }

    // Repositories
    override val authRepository: AuthRepository by lazy {
        AuthRepository()
    }

    override val userRepository: UserRepository by lazy {
        UserRepository(userRemoteDataSource)
    }

    override val outletRepository: OutletRepository by lazy {
        OutletRepository(outletRemoteDataSource)
    }

    override val materialRepository: MaterialRepository by lazy {
        MaterialRepository(materialRemoteDataSource)
    }

    override val productRepository: ProductRepository by lazy {
        ProductRepository(productRemoteDataSource)
    }

    override val materialDistributionRepository: MaterialDistributionRepository by lazy {
        MaterialDistributionRepository(materialDistributionRemoteDataSource)
    }

    override val dailyReportRepository: DailyReportRepository by lazy {
        DailyReportRepository(dailyReportRemoteDataSource)
    }
}
