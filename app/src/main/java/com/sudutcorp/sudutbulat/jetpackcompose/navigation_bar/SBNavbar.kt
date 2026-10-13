package com.sudutcorp.sudutbulat.jetpackcompose.navigation_bar

import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.sudutcorp.sudutbulat.R
import com.sudutcorp.sudutbulat.jetpackcompose.Role

data class NavItem(
    val title: String,
    val icon: Int,
    val roles: List<Role>,
    val route: NavbarRoutes,
    val screen: @Composable (navController: NavHostController, onLogout: () -> Unit) -> Unit
)

fun getNavItemsForRole(role: Role): List<NavItem> {
    return when (role) {
        Role.OWNER -> listOf(
            NavItem("Home", R.drawable.ic_home, listOf(Role.OWNER), NavbarRoutes.Home) { nav, _ -> NavbarHomeScreen(nav) },
            NavItem("Outlet", R.drawable.ic_store, listOf(Role.OWNER), NavbarRoutes.Outlet) { nav, _ -> NavbarOutletScreen(nav) },
            NavItem("Laporan", R.drawable.ic_bar_chart, listOf(Role.OWNER), NavbarRoutes.OwnerReport) { nav, _ -> NavbarOwnerReportScreen(nav) },
            NavItem("Profil", R.drawable.ic_user, listOf(Role.OWNER), NavbarRoutes.Profile) { nav, onLogout -> NavbarProfileScreen(nav, onLogout) }
        )
        Role.GUDANG -> listOf(
            NavItem("Home", R.drawable.ic_home, listOf(Role.GUDANG), NavbarRoutes.Home) { nav, _ -> NavbarHomeScreen(nav) },
            NavItem("Outlet", R.drawable.ic_store, listOf(Role.GUDANG), NavbarRoutes.Outlet) { nav, _ -> NavbarOutletScreen(nav) },
            NavItem("Distribution", R.drawable.ic_wallet, listOf(Role.GUDANG), NavbarRoutes.Distribution) { nav, _ -> NavbarDistributionScreen(nav) },
            NavItem("Profil", R.drawable.ic_user, listOf(Role.GUDANG), NavbarRoutes.Profile) { nav, onLogout -> NavbarProfileScreen(nav, onLogout) }
        )
        Role.OUTLET -> listOf(
            NavItem("Home", R.drawable.ic_home, listOf(Role.OUTLET), NavbarRoutes.Home) { nav, _ -> NavbarHomeScreen(nav) },
            NavItem("Outlet", R.drawable.ic_store, listOf(Role.OUTLET), NavbarRoutes.Outlet) { nav, _ -> NavbarOutletScreen(nav) },
            NavItem("OutletReport", R.drawable.ic_bar_chart, listOf(Role.OUTLET), NavbarRoutes.OutletReport) { nav, _ -> NavbarOutletReportScreen(nav) },
            NavItem("Profil", R.drawable.ic_user, listOf(Role.OUTLET), NavbarRoutes.Profile) { nav, onLogout -> NavbarProfileScreen(nav, onLogout) }
        )
    }
}

@Composable
fun SBNavbar(
    items: List<NavItem>,
    selectedPageIndex: Int,
    onPageSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    NavigationBar(modifier = modifier) {
        items.forEachIndexed { index, item ->
            NavigationBarItem(
                selected = selectedPageIndex == index,
                onClick = { onPageSelected(index) },
                icon = {
                    Icon(painterResource(item.icon), "NavbarItem_${item.title}")
                },
                label = { Text(item.title) }
            )
        }
    }
}

@Composable
fun SBNavbar(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    userRole: Role = Role.OWNER
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    val items = getNavItemsForRole(userRole)

    NavigationBar(modifier = modifier) {
        items.forEach { item ->
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
