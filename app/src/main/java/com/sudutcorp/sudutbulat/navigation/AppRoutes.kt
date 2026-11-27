package com.sudutcorp.sudutbulat.navigation

import kotlinx.serialization.Serializable

sealed interface AppRoutes {
    @Serializable
    data object Login : AppRoutes

    @Serializable
    data object Main : AppRoutes
}

@Serializable
sealed class TabRoutes {
    @Serializable
    data object Home : TabRoutes()

    @Serializable
    data object Outlet : TabRoutes()

    @Serializable
    data object GudangStock : TabRoutes()

    @Serializable
    data object OutletStock : TabRoutes()

    @Serializable
    data object OwnerReport : TabRoutes()

    @Serializable
    data object Distribution : TabRoutes()

    @Serializable
    data object OutletReport : TabRoutes()

    @Serializable
    data object Profile : TabRoutes()
}

@Serializable
sealed interface OwnerSubRoutes {
    @Serializable
    data object TabsRoot : OwnerSubRoutes

    @Serializable
    data object OutletList : OwnerSubRoutes

    @Serializable
    data class DetailOutlet(val outletId: String) : OwnerSubRoutes

    @Serializable
    data class DetailStaff(val userId: String) : OwnerSubRoutes

    @Serializable
    data object AddOutlet : OwnerSubRoutes

    @Serializable
    data class AddUser(val preselectedOutletId: String? = null) : OwnerSubRoutes

    @Serializable
    data class DetailReport(
        val period: String = "30 Hari",
        val reportId: String? = null
    ) : OwnerSubRoutes

    @Serializable
    data object Catalog : OwnerSubRoutes

    @Serializable
    data object Notification : OwnerSubRoutes

    @Serializable
    data object CreateDistribution : OwnerSubRoutes
}
