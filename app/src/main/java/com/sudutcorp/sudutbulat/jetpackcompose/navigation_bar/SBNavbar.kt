package com.sudutcorp.sudutbulat.jetpackcompose.navigation_bar

import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.res.painterResource
import androidx.core.R
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.sudutcorp.sudutbulat.jetpackcompose.Role

@Composable
fun SBNavbar(navController: NavHostController) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    val navItems = listOf(
        NavItem(
            "Home",
            com.sudutcorp.sudutbulat.R.drawable.ic_home,
            arrayOf(Role.OWNER, Role.GUDANG, Role.OUTLET),
            NavbarRoutes.Home
        ),
        NavItem(
            "Outlet",
            com.sudutcorp.sudutbulat.R.drawable.ic_store,
            arrayOf(Role.GUDANG, Role.OUTLET),
            NavbarRoutes.Outlet
        ),
        NavItem(
            "Laporan",
            com.sudutcorp.sudutbulat.R.drawable.ic_bar_chart,
            arrayOf(Role.OWNER, Role.OUTLET),
            NavbarRoutes.OwnerReport
        ),
        NavItem(
            "Profil",
            com.sudutcorp.sudutbulat.R.drawable.ic_user,
            arrayOf(Role.OWNER, Role.GUDANG, Role.OUTLET),
            NavbarRoutes.Profile
        ),
    )

    NavigationBar {
        navItems.forEach { item ->
            NavigationBarItem(
                selected = currentDestination?.hierarchy?.any { it.hasRoute(item.route::class) } == true,
                onClick = {
                    navController.navigate(item.route) {
                        popUpTo(navController.graph.startDestinationId) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                icon = {
                    Icon(painterResource(item.icon), "NavbarItem_${item.title}")
                },
                label = { Text(item.title) }
            )
        }
    }
}


data class NavItem(
    val title: String,
    val icon: Int,
    val role: Array<Role>,
    val route: NavbarRoutes
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as NavItem
        if (title != other.title) return false
        if (icon != other.icon) return false
        if (!role.contentEquals(other.role)) return false
        return true
    }

    override fun hashCode(): Int {
        var result = title.hashCode()
        result = 31 * result + icon
        result = 31 * result + role.contentHashCode()
        return result
    }
}



