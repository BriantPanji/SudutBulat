package com.sudutcorp.sudutbulat.navigation

import kotlinx.serialization.Serializable

sealed interface AppRoutes {
    @Serializable
    data object Login : AppRoutes

    @Serializable
    data object Register : AppRoutes

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
    data object OwnerReport : TabRoutes()

    @Serializable
    data object Distribution : TabRoutes()

    @Serializable
    data object OutletReport : TabRoutes()

    @Serializable
    data object Profile : TabRoutes()
}
