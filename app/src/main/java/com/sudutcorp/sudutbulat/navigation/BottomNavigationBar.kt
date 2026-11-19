package com.sudutcorp.sudutbulat.navigation

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
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
import androidx.compose.ui.graphics.graphicsLayer
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
import com.sudutcorp.sudutbulat.feature.home.OutletHomeScreen
import com.sudutcorp.sudutbulat.feature.home.WarehouseHomeScreen
import com.sudutcorp.sudutbulat.feature.outlet.OutletScreen
import com.sudutcorp.sudutbulat.feature.owner.OutletTabMode
import com.sudutcorp.sudutbulat.feature.owner.OwnerUiEvent
import com.sudutcorp.sudutbulat.feature.owner.OwnerUiState
import com.sudutcorp.sudutbulat.feature.owner.ReportPeriodSummaryUi
import com.sudutcorp.sudutbulat.feature.profile.ProfileScreen
import com.sudutcorp.sudutbulat.feature.report.OutletReportScreen
import com.sudutcorp.sudutbulat.feature.report.OwnerReportScreen
import com.sudutcorp.sudutbulat.feature.report.defaultReportPeriodSummary
import com.sudutcorp.sudutbulat.feature.stock.GudangStockScreen
import com.sudutcorp.sudutbulat.feature.stock.OutletStockScreen

data class NavItem(
    val title: String,
    val icon: Int,
    val roles: List<Role>,
    val route: TabRoutes,
    val screen: @Composable (navController: NavHostController, onLogout: () -> Unit) -> Unit
)

fun getNavItemsForRole(
    role: Role,
    userEmail: String? = null,
    isDevAccount: Boolean = false,
    onOpenRoleSwitcher: () -> Unit = {},
    ownerUiState: OwnerUiState = OwnerUiState(),
    reportSummary: ReportPeriodSummaryUi = defaultReportPeriodSummary(),
    onOwnerEvent: (OwnerUiEvent) -> Unit = {},
    onSelectTab: (Int) -> Unit = {}
): List<NavItem> {
    return when (role) {
        Role.DEV,
        Role.OWNER -> listOf(
            NavItem("Beranda", R.drawable.ic_home, listOf(Role.OWNER, Role.DEV), TabRoutes.Home) { nav, _ ->
                HomeScreen(
                    navController = nav,
                    uiState = ownerUiState,
                    isDevMode = isDevAccount,
                    onSwitchRoleClick = onOpenRoleSwitcher,
                    onSeeAllOutletsClick = {
                        onOwnerEvent(OwnerUiEvent.OutletTabSelected(OutletTabMode.PERFORMANCE))
                        onSelectTab(1)
                    },
                    onOutletClick = { outletId ->
                        nav.navigateSingle(OwnerSubRoutes.DetailOutlet(outletId))
                    },
                    onReviewReportsClick = {
                        nav.navigateSingle(OwnerSubRoutes.DetailReport(period = ownerUiState.selectedReportPeriod))
                    }
                )
            },
            NavItem("Outlet", R.drawable.ic_store, listOf(Role.OWNER, Role.DEV), TabRoutes.Outlet) { nav, _ ->
                OutletScreen(
                    navController = nav,
                    uiState = ownerUiState,
                    onEvent = onOwnerEvent,
                    isDevMode = isDevAccount,
                    onSwitchRoleClick = onOpenRoleSwitcher,
                    onOutletClick = { outletId ->
                        nav.navigateSingle(OwnerSubRoutes.DetailOutlet(outletId))
                    },
                    onAddOutletClick = {
                        nav.navigateSingle(
                            route = OwnerSubRoutes.AddOutlet,
                            beforeNavigate = { onOwnerEvent(OwnerUiEvent.ResetAddOutletForm) }
                        )
                    },
                    onStaffClick = { userId ->
                        nav.navigateSingle(OwnerSubRoutes.DetailStaff(userId))
                    },
                    onAddStaffAccountClick = {
                        nav.navigateSingle(
                            route = OwnerSubRoutes.AddUser(null),
                            beforeNavigate = { onOwnerEvent(OwnerUiEvent.InitAddUserForm(null)) }
                        )
                    }
                )
            },
            NavItem("Laporan", R.drawable.ic_bar_chart, listOf(Role.OWNER, Role.DEV), TabRoutes.OwnerReport) { nav, _ ->
                OwnerReportScreen(
                    navController = nav,
                    uiState = ownerUiState,
                    reportSummary = reportSummary,
                    onEvent = onOwnerEvent,
                    isDevMode = isDevAccount,
                    onSwitchRoleClick = onOpenRoleSwitcher,
                    onSeeDetailClick = { period ->
                        nav.navigateSingle(OwnerSubRoutes.DetailReport(period = period, reportId = null))
                    },
                    onReportItemClick = { reportId ->
                        nav.navigateSingle(OwnerSubRoutes.DetailReport(period = ownerUiState.selectedReportPeriod, reportId = reportId))
                    }
                )
            },
            NavItem("Profil", R.drawable.ic_user, listOf(Role.OWNER, Role.DEV), TabRoutes.Profile) { nav, onLogout ->
                ProfileScreen(
                    navController = nav,
                    onLogout = onLogout,
                    userRole = role,
                    userEmail = userEmail,
                    isDevAccount = isDevAccount,
                    onSwitchRoleClick = onOpenRoleSwitcher,
                    ownerUiState = ownerUiState,
                    onNavigateToNotifications = {
                        nav.navigateSingle(OwnerSubRoutes.Notification)
                    },
                    onNavigateToAddUser = {
                        nav.navigateSingle(
                            route = OwnerSubRoutes.AddUser(null),
                            beforeNavigate = { onOwnerEvent(OwnerUiEvent.InitAddUserForm(null)) }
                        )
                    },
                    onNavigateToOutletList = {
                        nav.navigateSingle(
                            route = OwnerSubRoutes.OutletList,
                            beforeNavigate = { onOwnerEvent(OwnerUiEvent.OutletTabSelected(OutletTabMode.OUTLET_LIST)) }
                        )
                    },
                    onNavigateToCatalog = {
                        nav.navigateSingle(OwnerSubRoutes.Catalog)
                    }
                )
            }
        )
        Role.WAREHOUSE -> listOf(
            NavItem("Beranda", R.drawable.ic_home, listOf(Role.WAREHOUSE), TabRoutes.Home) { nav, _ ->
                WarehouseHomeScreen(navController = nav, isDevMode = isDevAccount, onSwitchRoleClick = onOpenRoleSwitcher)
            },
            NavItem("Stok", R.drawable.ic_box, listOf(Role.WAREHOUSE), TabRoutes.GudangStock) { nav, _ ->
                GudangStockScreen(
                    navController = nav,
                    uiState = ownerUiState,
                    onEvent = onOwnerEvent,
                    roleEyebrow = "Admin Gudang",
                    initials = if (isDevAccount) "DV" else "BS"
                )
            },
            NavItem("Distribusi", R.drawable.ic_truck, listOf(Role.WAREHOUSE), TabRoutes.Distribution) { nav, _ ->
                DistributionScreen(nav)
            },
            NavItem("Profil", R.drawable.ic_user, listOf(Role.WAREHOUSE), TabRoutes.Profile) { nav, onLogout ->
                ProfileScreen(
                    navController = nav,
                    onLogout = onLogout,
                    userRole = role,
                    userEmail = userEmail,
                    isDevAccount = isDevAccount,
                    onSwitchRoleClick = onOpenRoleSwitcher,
                    ownerUiState = ownerUiState,
                    onNavigateToNotifications = {
                        nav.navigateSingle(OwnerSubRoutes.Notification)
                    }
                )
            }
        )
        Role.OUTLET -> listOf(
            NavItem("Beranda", R.drawable.ic_home, listOf(Role.OUTLET), TabRoutes.Home) { nav, _ ->
                OutletHomeScreen(navController = nav, isDevMode = isDevAccount, onSwitchRoleClick = onOpenRoleSwitcher)
            },
            NavItem("Stok", R.drawable.ic_box, listOf(Role.OUTLET), TabRoutes.OutletStock) { nav, _ ->
                OutletStockScreen(nav)
            },
            NavItem("Laporan", R.drawable.ic_bar_chart, listOf(Role.OUTLET), TabRoutes.OutletReport) { nav, _ ->
                OutletReportScreen(nav)
            },
            NavItem("Profil", R.drawable.ic_user, listOf(Role.OUTLET), TabRoutes.Profile) { nav, onLogout ->
                ProfileScreen(
                    navController = nav,
                    onLogout = onLogout,
                    userRole = role,
                    userEmail = userEmail,
                    isDevAccount = isDevAccount,
                    onSwitchRoleClick = onOpenRoleSwitcher,
                    ownerUiState = ownerUiState,
                    onNavigateToNotifications = {
                        nav.navigateSingle(OwnerSubRoutes.Notification)
                    }
                )
            }
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
    val contentColor by animateColorAsState(
        targetValue = if (isSelected) SBColor.Primary_550 else SBColor.Grey_500,
        animationSpec = tween(durationMillis = 220),
        label = "NavContentColor"
    )
    val indicatorColor by animateColorAsState(
        targetValue = if (isSelected) SBColor.Primary_100 else Color.Transparent,
        animationSpec = tween(durationMillis = 220),
        label = "NavIndicatorColor"
    )
    val indicatorWidth by animateDpAsState(
        targetValue = if (isSelected) 48.dp else 28.dp,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "NavIndicatorWidth"
    )
    val iconScale by animateFloatAsState(
        targetValue = if (isSelected) 1.12f else 1.0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "NavIconScale"
    )

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
        AnimatedContent(
            targetState = item.title to item.icon,
            transitionSpec = {
                (slideInVertically(
                    animationSpec = spring(stiffness = Spring.StiffnessMediumLow)
                ) { height -> height / 2 } + fadeIn(tween(180))) togetherWith
                    (slideOutVertically(
                        animationSpec = spring(stiffness = Spring.StiffnessMediumLow)
                    ) { height -> -height / 2 } + fadeOut(tween(120)))
            },
            label = "NavItemRoll"
        ) { (title, iconRes) ->
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(width = indicatorWidth, height = 28.dp)
                        .clip(CircleShape)
                        .background(indicatorColor),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(iconRes),
                        contentDescription = "NavbarItem_$title",
                        tint = contentColor,
                        modifier = Modifier
                            .size(20.dp)
                            .graphicsLayer {
                                scaleX = iconScale
                                scaleY = iconScale
                            }
                    )
                }
                Text(
                    text = title,
                    fontSize = 12.sp,
                    lineHeight = 16.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
                    color = contentColor
                )
            }
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
