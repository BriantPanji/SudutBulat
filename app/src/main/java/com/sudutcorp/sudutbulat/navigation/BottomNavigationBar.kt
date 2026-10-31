package com.sudutcorp.sudutbulat.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBarDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role as SemanticsRole
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.sudutcorp.sudutbulat.R
import com.sudutcorp.sudutbulat.core.ui.theme.SBColor
import com.sudutcorp.sudutbulat.core.ui.theme.SudutBulatTheme
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
            NavItem("Beranda", R.drawable.ic_home, listOf(Role.OWNER), TabRoutes.Home) { nav, _ -> HomeScreen(nav) },
            NavItem("Outlet", R.drawable.ic_store, listOf(Role.OWNER), TabRoutes.Outlet) { nav, _ -> OutletScreen(nav) },
            NavItem("Laporan", R.drawable.ic_bar_chart, listOf(Role.OWNER), TabRoutes.OwnerReport) { nav, _ -> OwnerReportScreen(nav) },
            NavItem("Profil", R.drawable.ic_user, listOf(Role.OWNER), TabRoutes.Profile) { nav, onLogout -> ProfileScreen(nav, onLogout) }
        )
        Role.GUDANG -> listOf(
            NavItem("Beranda", R.drawable.ic_home, listOf(Role.GUDANG), TabRoutes.Home) { nav, _ -> HomeScreen(nav) },
            NavItem("Outlet", R.drawable.ic_store, listOf(Role.GUDANG), TabRoutes.Outlet) { nav, _ -> OutletScreen(nav) },
            NavItem("Distribusi", R.drawable.ic_wallet, listOf(Role.GUDANG), TabRoutes.Distribution) { nav, _ -> DistributionScreen(nav) },
            NavItem("Profil", R.drawable.ic_user, listOf(Role.GUDANG), TabRoutes.Profile) { nav, onLogout -> ProfileScreen(nav, onLogout) }
        )
        Role.OUTLET -> listOf(
            NavItem("Beranda", R.drawable.ic_home, listOf(Role.OUTLET), TabRoutes.Home) { nav, _ -> HomeScreen(nav) },
            NavItem("Outlet", R.drawable.ic_store, listOf(Role.OUTLET), TabRoutes.Outlet) { nav, _ -> OutletScreen(nav) },
            NavItem("Laporan", R.drawable.ic_bar_chart, listOf(Role.OUTLET), TabRoutes.OutletReport) { nav, _ -> OutletReportScreen(nav) },
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
    Surface(
        color = Color.White,
        tonalElevation = 0.dp,
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .windowInsetsPadding(NavigationBarDefaults.windowInsets)
        ) {
            HorizontalDivider(thickness = 1.dp, color = SBColor.Grey_100)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .selectableGroup()
                    .padding(top = 10.dp, bottom = 14.dp),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                items.forEachIndexed { index, item ->
                    BottomNavItem(
                        item = item,
                        isSelected = selectedPageIndex == index,
                        onClick = { onPageSelected(index) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun BottomNavItem(
    item: NavItem,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val contentColor = if (isSelected) SBColor.Primary_550 else SBColor.Grey_500
    val indicatorColor = if (isSelected) SBColor.Primary_100 else Color.Transparent

    Column(
        modifier = modifier
            .selectable(
                selected = isSelected,
                onClick = onClick,
                role = SemanticsRole.Tab,
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(width = 48.dp, height = 28.dp)
                .clip(CircleShape)
                .background(indicatorColor),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(item.icon),
                contentDescription = "NavbarItem_${item.title}",
                tint = contentColor,
                modifier = Modifier.size(20.dp)
            )
        }
        Text(
            text = item.title,
            fontSize = 12.sp,
            lineHeight = 16.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
            color = contentColor
        )
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
    val items = remember(userRole) { getNavItemsForRole(userRole) }
    val selectedIndex = items.indexOfFirst { item ->
        currentDestination?.hierarchy?.any { it.hasRoute(item.route::class) } == true
    }.coerceAtLeast(0)

    BottomNavigationBar(
        items = items,
        selectedPageIndex = selectedIndex,
        onPageSelected = { index ->
            val item = items[index]
            navController.navigate(item.route) {
                popUpTo(navController.graph.startDestinationId) {
                    saveState = true
                }
                launchSingleTop = true
                restoreState = true
            }
        },
        modifier = modifier
    )
}

@Preview(showBackground = true, widthDp = 390)
@Composable
private fun BottomNavigationBarPreview() {
    SudutBulatTheme {
        BottomNavigationBar(
            items = getNavItemsForRole(Role.OWNER),
            selectedPageIndex = 0,
            onPageSelected = {}
        )
    }
}
