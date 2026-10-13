package com.sudutcorp.sudutbulat.navigation

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
import com.sudutcorp.sudutbulat.data.model.Role
import com.sudutcorp.sudutbulat.feature.distribution.DistributionScreen
import com.sudutcorp.sudutbulat.feature.home.HomeScreen
import com.sudutcorp.sudutbulat.feature.outlet.OutletScreen
import com.sudutcorp.sudutbulat.feature.profile.ProfileScreen
import com.sudutcorp.sudutbulat.feature.report.OutletReportScreen
import com.sudutcorp.sudutbulat.feature.report.OwnerReportScreen

data class NavItem(
    val title: String,
    val icon: Int,
    val roles: List<Role>,
    val route: TabRoutes,
    val screen: @Composable (navController: NavHostController, onLogout: () -> Unit) -> Unit
)

fun getNavItemsForRole(role: Role): List<NavItem> {
    return when (role) {
        Role.OWNER -> listOf(
            NavItem("Home", R.drawable.ic_home, listOf(Role.OWNER), TabRoutes.Home) { nav, _ -> HomeScreen(nav) },
            NavItem("Outlet", R.drawable.ic_store, listOf(Role.OWNER), TabRoutes.Outlet) { nav, _ -> OutletScreen(nav) },
            NavItem("Laporan", R.drawable.ic_bar_chart, listOf(Role.OWNER), TabRoutes.OwnerReport) { nav, _ -> OwnerReportScreen(nav) },
            NavItem("Profil", R.drawable.ic_user, listOf(Role.OWNER), TabRoutes.Profile) { nav, onLogout -> ProfileScreen(nav, onLogout) }
        )
        Role.GUDANG -> listOf(
            NavItem("Home", R.drawable.ic_home, listOf(Role.GUDANG), TabRoutes.Home) { nav, _ -> HomeScreen(nav) },
            NavItem("Outlet", R.drawable.ic_store, listOf(Role.GUDANG), TabRoutes.Outlet) { nav, _ -> OutletScreen(nav) },
            NavItem("Distribution", R.drawable.ic_wallet, listOf(Role.GUDANG), TabRoutes.Distribution) { nav, _ -> DistributionScreen(nav) },
            NavItem("Profil", R.drawable.ic_user, listOf(Role.GUDANG), TabRoutes.Profile) { nav, onLogout -> ProfileScreen(nav, onLogout) }
        )
        Role.OUTLET -> listOf(
            NavItem("Home", R.drawable.ic_home, listOf(Role.OUTLET), TabRoutes.Home) { nav, _ -> HomeScreen(nav) },
            NavItem("Outlet", R.drawable.ic_store, listOf(Role.OUTLET), TabRoutes.Outlet) { nav, _ -> OutletScreen(nav) },
            NavItem("OutletReport", R.drawable.ic_bar_chart, listOf(Role.OUTLET), TabRoutes.OutletReport) { nav, _ -> OutletReportScreen(nav) },
            NavItem("Profil", R.drawable.ic_user, listOf(Role.OUTLET), TabRoutes.Profile) { nav, onLogout -> ProfileScreen(nav, onLogout) }
        )
    }
}

@Composable
fun BottomNavigationBar(
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
                    Icon(
                        painter = painterResource(item.icon),
                        contentDescription = "NavbarItem_${item.title}"
                    )
                },
                label = { Text(item.title) }
            )
        }
    }
}

@Composable
fun BottomNavigationBar(
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
                    Icon(
                        painter = painterResource(item.icon),
                        contentDescription = "NavbarItem_${item.title}"
                    )
                },
                label = { Text(item.title) }
            )
        }
    }
}
