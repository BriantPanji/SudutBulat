package com.sudutcorp.sudutbulat.jetpackcompose.navigation_bar

import kotlinx.serialization.Serializable

@Serializable
sealed class NavbarRoutes {

    @Serializable
    object Home: NavbarRoutes()

    @Serializable
    object Outlet: NavbarRoutes()

    @Serializable
    object OwnerReport: NavbarRoutes()

    @Serializable
    object Distribution: NavbarRoutes()

    @Serializable
    object OutletReport: NavbarRoutes()

    @Serializable
    object Profile: NavbarRoutes()

}