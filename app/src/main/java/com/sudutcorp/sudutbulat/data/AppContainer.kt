package com.sudutcorp.sudutbulat.data

import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.sudutcorp.sudutbulat.data.auth.AuthRepository
import com.sudutcorp.sudutbulat.data.remote.CloudinaryMediaUploader
import com.sudutcorp.sudutbulat.data.remote.DailyReportRemoteDataSource
import com.sudutcorp.sudutbulat.data.remote.MaterialCategoryRemoteDataSource
import com.sudutcorp.sudutbulat.data.remote.MaterialDistributionRemoteDataSource
import com.sudutcorp.sudutbulat.data.remote.MaterialRemoteDataSource
import com.sudutcorp.sudutbulat.data.remote.OutletRemoteDataSource
import com.sudutcorp.sudutbulat.data.remote.ProductRemoteDataSource
import com.sudutcorp.sudutbulat.data.remote.UserRemoteDataSource
import com.sudutcorp.sudutbulat.data.repository.DailyReportRepository
import com.sudutcorp.sudutbulat.data.repository.MaterialCategoryRepository
import com.sudutcorp.sudutbulat.data.repository.MaterialDistributionRepository
import com.sudutcorp.sudutbulat.data.repository.MaterialRepository
import com.sudutcorp.sudutbulat.data.repository.OutletRepository
import com.sudutcorp.sudutbulat.data.repository.ProductRepository
import com.sudutcorp.sudutbulat.data.repository.UserRepository
import com.sudutcorp.sudutbulat.feature.distribution.WarehouseViewModel
import com.sudutcorp.sudutbulat.feature.outlet.OutletViewModel
import com.sudutcorp.sudutbulat.feature.owner.OwnerViewModel
import com.sudutcorp.sudutbulat.feature.profile.ProfileViewModel
import com.sudutcorp.sudutbulat.feature.stock.CatalogViewModel

/**
 * Dependency container providing single-point manual dependency injection.
 */
interface AppContainer {
    val authRepository: AuthRepository
    val userRepository: UserRepository
    val outletRepository: OutletRepository
    val materialCategoryRepository: MaterialCategoryRepository
    val materialRepository: MaterialRepository
    val productRepository: ProductRepository
    val materialDistributionRepository: MaterialDistributionRepository
    val dailyReportRepository: DailyReportRepository
    val viewModelFactory: ViewModelProvider.Factory
}

/**
 * Default implementation of [AppContainer] that instantiates remote data sources
 * and injects them into repositories.
 */
class DefaultAppContainer : AppContainer {
    // Remote Data Sources (lazy loaded)
    private val userRemoteDataSource by lazy { UserRemoteDataSource() }
    private val outletRemoteDataSource by lazy { OutletRemoteDataSource() }
    private val materialCategoryRemoteDataSource by lazy { MaterialCategoryRemoteDataSource() }
    private val materialRemoteDataSource by lazy { MaterialRemoteDataSource() }
    private val productRemoteDataSource by lazy { ProductRemoteDataSource() }
    private val materialDistributionRemoteDataSource by lazy { MaterialDistributionRemoteDataSource() }
    private val dailyReportRemoteDataSource by lazy { DailyReportRemoteDataSource() }
    private val cloudinaryMediaUploader by lazy { CloudinaryMediaUploader() }

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

    override val materialCategoryRepository: MaterialCategoryRepository by lazy {
        MaterialCategoryRepository(materialCategoryRemoteDataSource)
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

    override val viewModelFactory: ViewModelProvider.Factory by lazy {
        viewModelFactory {
            initializer {
                OwnerViewModel(
                    outletRepository = outletRepository,
                    userRepository = userRepository,
                    dailyReportRepository = dailyReportRepository,
                    materialRepository = materialRepository,
                    productRepository = productRepository,
                    materialDistributionRepository = materialDistributionRepository,
                    authRepository = authRepository
                )
            }
            initializer {
                WarehouseViewModel(
                    outletRepository = outletRepository,
                    materialRepository = materialRepository,
                    distributionRepository = materialDistributionRepository,
                    userRepository = userRepository,
                    authRepository = authRepository
                )
            }
            initializer {
                CatalogViewModel(
                    materialRepository = materialRepository,
                    materialCategoryRepository = materialCategoryRepository,
                    productRepository = productRepository
                )
            }
            initializer {
                OutletViewModel(
                    outletRepository = outletRepository,
                    userRepository = userRepository,
                    materialRepository = materialRepository,
                    productRepository = productRepository,
                    distributionRepository = materialDistributionRepository,
                    dailyReportRepository = dailyReportRepository,
                    authRepository = authRepository
                )
            }
            initializer {
                ProfileViewModel(
                    userRepository = userRepository,
                    authRepository = authRepository,
                    mediaUploader = cloudinaryMediaUploader
                )
            }
        }
    }
}
