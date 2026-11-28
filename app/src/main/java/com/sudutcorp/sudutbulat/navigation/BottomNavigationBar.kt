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
import com.sudutcorp.sudutbulat.feature.distribution.WarehouseUiEffect
import com.sudutcorp.sudutbulat.feature.distribution.WarehouseUiEvent
import com.sudutcorp.sudutbulat.feature.distribution.WarehouseUiState
import com.sudutcorp.sudutbulat.feature.home.HomeScreen
import com.sudutcorp.sudutbulat.feature.home.OutletHomeScreen
import com.sudutcorp.sudutbulat.feature.home.WarehouseHomeScreen
import com.sudutcorp.sudutbulat.feature.outlet.OutletScreen
import com.sudutcorp.sudutbulat.feature.outlet.OutletUiEffect
import com.sudutcorp.sudutbulat.feature.outlet.OutletUiEvent
import com.sudutcorp.sudutbulat.feature.outlet.OutletUiState
import com.sudutcorp.sudutbulat.feature.owner.OutletTabMode
import com.sudutcorp.sudutbulat.feature.owner.OwnerUiEffect
import com.sudutcorp.sudutbulat.feature.owner.OwnerUiEvent
import com.sudutcorp.sudutbulat.feature.owner.OwnerUiState
import com.sudutcorp.sudutbulat.feature.owner.ReportPeriodSummaryUi
import com.sudutcorp.sudutbulat.feature.owner.ReportReviewFilter
import com.sudutcorp.sudutbulat.feature.owner.ReportTabMode
import com.sudutcorp.sudutbulat.feature.profile.ProfileScreen
import com.sudutcorp.sudutbulat.feature.report.OutletReportScreen
import com.sudutcorp.sudutbulat.feature.report.OwnerReportScreen
import com.sudutcorp.sudutbulat.feature.report.defaultReportPeriodSummary
import com.sudutcorp.sudutbulat.feature.stock.CatalogUiEffect
import com.sudutcorp.sudutbulat.feature.stock.CatalogUiEvent
import com.sudutcorp.sudutbulat.feature.stock.CatalogUiState
import com.sudutcorp.sudutbulat.feature.stock.GudangStockScreen
import com.sudutcorp.sudutbulat.feature.stock.OutletStockScreen
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow

data class NavItem(
    val title: String,
    val icon: Int,
    val roles: List<Role>,
    val route: TabRoutes
)

private val OwnerNavItems = listOf(
    NavItem("Beranda", R.drawable.ic_home, listOf(Role.OWNER, Role.DEV), TabRoutes.Home),
    NavItem("Outlet", R.drawable.ic_store, listOf(Role.OWNER, Role.DEV), TabRoutes.Outlet),
    NavItem("Laporan", R.drawable.ic_bar_chart, listOf(Role.OWNER, Role.DEV), TabRoutes.OwnerReport),
    NavItem("Profil", R.drawable.ic_user, listOf(Role.OWNER, Role.DEV), TabRoutes.Profile)
)

private val WarehouseNavItems = listOf(
    NavItem("Beranda", R.drawable.ic_home, listOf(Role.WAREHOUSE), TabRoutes.Home),
    NavItem("Stok", R.drawable.ic_box, listOf(Role.WAREHOUSE), TabRoutes.GudangStock),
    NavItem("Distribusi", R.drawable.ic_truck, listOf(Role.WAREHOUSE), TabRoutes.Distribution),
    NavItem("Profil", R.drawable.ic_user, listOf(Role.WAREHOUSE), TabRoutes.Profile)
)

private val OutletNavItems = listOf(
    NavItem("Beranda", R.drawable.ic_home, listOf(Role.OUTLET), TabRoutes.Home),
    NavItem("Stok", R.drawable.ic_box, listOf(Role.OUTLET), TabRoutes.OutletStock),
    NavItem("Laporan", R.drawable.ic_bar_chart, listOf(Role.OUTLET), TabRoutes.OutletReport),
    NavItem("Profil", R.drawable.ic_user, listOf(Role.OUTLET), TabRoutes.Profile)
)

fun getNavItemsForRole(role: Role): List<NavItem> = when (role) {
    Role.DEV, Role.OWNER -> OwnerNavItems
    Role.WAREHOUSE -> WarehouseNavItems
    Role.OUTLET -> OutletNavItems
}

@Composable
fun RoleTabContent(
    role: Role,
    pageIndex: Int,
    navController: NavHostController,
    onLogout: () -> Unit,
    userEmail: String? = null,
    isDevAccount: Boolean = false,
    onOpenRoleSwitcher: () -> Unit = {},
    ownerUiState: OwnerUiState = OwnerUiState(),
    reportSummary: ReportPeriodSummaryUi = defaultReportPeriodSummary(),
    onOwnerEvent: (OwnerUiEvent) -> Unit = {},
    warehouseUiState: WarehouseUiState = WarehouseUiState(),
    warehouseEffects: Flow<WarehouseUiEffect> = emptyFlow(),
    onWarehouseEvent: (WarehouseUiEvent) -> Unit = {},
    catalogUiState: CatalogUiState = CatalogUiState(),
    catalogEffects: Flow<CatalogUiEffect> = emptyFlow(),
    onCatalogEvent: (CatalogUiEvent) -> Unit = {},
    outletUiState: OutletUiState = OutletUiState(),
    outletEffects: Flow<OutletUiEffect> = emptyFlow(),
    onOutletEvent: (OutletUiEvent) -> Unit = {},
    onUpdatePassword: suspend (String, String) -> Result<Unit> = { _, _ ->
        Result.failure(IllegalStateException("Tidak tersedia"))
    },
    onShowMessage: (String) -> Unit = {},
    onSelectTab: (Int) -> Unit = {}
) {
    when (role) {
        Role.DEV,
        Role.OWNER -> when (pageIndex) {
            0 -> HomeScreen(
                navController = navController,
                uiState = ownerUiState,
                isDevMode = isDevAccount,
                onSwitchRoleClick = onOpenRoleSwitcher,
                onSeeAllOutletsClick = {
                    onOwnerEvent(OwnerUiEvent.OutletTabSelected(OutletTabMode.PERFORMANCE))
                    onSelectTab(1)
                },
                onOutletClick = { outletId ->
                    navController.navigateSingle(OwnerSubRoutes.DetailOutlet(outletId))
                },
                onReviewReportsClick = {
                    onOwnerEvent(OwnerUiEvent.ReportTabSelected(ReportTabMode.LIST))
                    onOwnerEvent(OwnerUiEvent.ReportReviewFilterSelected(ReportReviewFilter.NEEDS_REVIEW))
                    onSelectTab(2)
                },
                onOpenReportsSummaryClick = {
                    onOwnerEvent(OwnerUiEvent.ReportTabSelected(ReportTabMode.SUMMARY))
                    onSelectTab(2)
                },
                onOpenReportsListClick = {
                    onOwnerEvent(OwnerUiEvent.ReportTabSelected(ReportTabMode.LIST))
                    onOwnerEvent(OwnerUiEvent.ReportReviewFilterSelected(ReportReviewFilter.ALL))
                    onSelectTab(2)
                },
                onReportClick = { reportId ->
                    navController.navigateSingle(
                        OwnerSubRoutes.DetailReport(
                            period = ownerUiState.selectedReportPeriod,
                            reportId = reportId
                        )
                    )
                },
                onAddOutletClick = {
                    navController.navigateSingle(
                        route = OwnerSubRoutes.AddOutlet,
                        beforeNavigate = { onOwnerEvent(OwnerUiEvent.ResetAddOutletForm) }
                    )
                },
                onAddUserClick = {
                    navController.navigateSingle(
                        route = OwnerSubRoutes.AddUser(null),
                        beforeNavigate = { onOwnerEvent(OwnerUiEvent.InitAddUserForm(null)) }
                    )
                },
                onOpenCatalogClick = {
                    navController.navigateSingle(OwnerSubRoutes.Catalog)
                }
            )
            1 -> OutletScreen(
                navController = navController,
                uiState = ownerUiState,
                onEvent = onOwnerEvent,
                isDevMode = isDevAccount,
                onSwitchRoleClick = onOpenRoleSwitcher,
                onOutletClick = { outletId ->
                    navController.navigateSingle(OwnerSubRoutes.DetailOutlet(outletId))
                },
                onAddOutletClick = {
                    navController.navigateSingle(
                        route = OwnerSubRoutes.AddOutlet,
                        beforeNavigate = { onOwnerEvent(OwnerUiEvent.ResetAddOutletForm) }
                    )
                },
                onStaffClick = { userId ->
                    navController.navigateSingle(OwnerSubRoutes.DetailStaff(userId))
                },
                onAddStaffAccountClick = {
                    navController.navigateSingle(
                        route = OwnerSubRoutes.AddUser(null),
                        beforeNavigate = { onOwnerEvent(OwnerUiEvent.InitAddUserForm(null)) }
                    )
                }
            )
            2 -> OwnerReportScreen(
                navController = navController,
                uiState = ownerUiState,
                reportSummary = reportSummary,
                onEvent = onOwnerEvent,
                isDevMode = isDevAccount,
                onSwitchRoleClick = onOpenRoleSwitcher,
                onSeeDetailClick = { period ->
                    navController.navigateSingle(OwnerSubRoutes.DetailReport(period = period, reportId = null))
                },
                onReportItemClick = { reportId ->
                    navController.navigateSingle(OwnerSubRoutes.DetailReport(period = ownerUiState.selectedReportPeriod, reportId = reportId))
                }
            )
            3 -> ProfileScreen(
                navController = navController,
                onLogout = onLogout,
                userRole = role,
                userEmail = userEmail,
                isDevAccount = isDevAccount,
                onSwitchRoleClick = onOpenRoleSwitcher,
                ownerUiState = ownerUiState,
                onNavigateToNotifications = {
                    navController.navigateSingle(OwnerSubRoutes.Notification)
                },
                onNavigateToAddUser = {
                    navController.navigateSingle(
                        route = OwnerSubRoutes.AddUser(null),
                        beforeNavigate = { onOwnerEvent(OwnerUiEvent.InitAddUserForm(null)) }
                    )
                },
                onNavigateToOutletList = {
                    navController.navigateSingle(
                        route = OwnerSubRoutes.OutletList,
                        beforeNavigate = { onOwnerEvent(OwnerUiEvent.OutletTabSelected(OutletTabMode.OUTLET_LIST)) }
                    )
                },
                onNavigateToCatalog = {
                    navController.navigateSingle(OwnerSubRoutes.Catalog)
                },
                onUpdatePassword = onUpdatePassword,
                onPasswordUpdated = onShowMessage
            )
        }
        Role.WAREHOUSE -> when (pageIndex) {
            0 -> WarehouseHomeScreen(
                navController = navController,
                uiState = warehouseUiState,
                effects = warehouseEffects,
                onEvent = onWarehouseEvent,
                isDevMode = isDevAccount,
                onSwitchRoleClick = onOpenRoleSwitcher,
                onOpenCatalogTab = { onSelectTab(1) },
                onOpenDistributionTab = { onSelectTab(2) }
            )
            1 -> GudangStockScreen(
                navController = navController,
                uiState = catalogUiState,
                effects = catalogEffects,
                onEvent = onCatalogEvent,
                roleEyebrow = "Admin Gudang",
                initials = if (isDevAccount) "DV" else "BS"
            )
            2 -> DistributionScreen(
                navController = navController,
                uiState = warehouseUiState,
                effects = warehouseEffects,
                onEvent = onWarehouseEvent,
                initials = if (isDevAccount) "DV" else "BS"
            )
            3 -> ProfileScreen(
                navController = navController,
                onLogout = onLogout,
                userRole = role,
                userEmail = userEmail,
                isDevAccount = isDevAccount,
                onSwitchRoleClick = onOpenRoleSwitcher,
                onNavigateToNotifications = {
                    navController.navigateSingle(OwnerSubRoutes.Notification)
                },
                onUpdatePassword = onUpdatePassword,
                onPasswordUpdated = onShowMessage
            )
        }
        Role.OUTLET -> when (pageIndex) {
            0 -> OutletHomeScreen(
                navController = navController,
                uiState = outletUiState,
                onEvent = onOutletEvent,
                isDevMode = isDevAccount,
                onSwitchRoleClick = onOpenRoleSwitcher,
                onOpenStockTab = { onSelectTab(1) },
                onOpenReportTab = { onSelectTab(2) }
            )
            1 -> OutletStockScreen(
                navController = navController,
                uiState = outletUiState,
                effects = outletEffects,
                onEvent = onOutletEvent,
                isDevMode = isDevAccount,
                onSwitchRoleClick = onOpenRoleSwitcher
            )
            2 -> OutletReportScreen(
                navController = navController,
                uiState = outletUiState,
                effects = outletEffects,
                onEvent = onOutletEvent,
                isDevMode = isDevAccount,
                onSwitchRoleClick = onOpenRoleSwitcher
            )
            3 -> ProfileScreen(
                navController = navController,
                onLogout = onLogout,
                userRole = role,
                userEmail = userEmail,
                isDevAccount = isDevAccount,
                onSwitchRoleClick = onOpenRoleSwitcher,
                onNavigateToNotifications = {
                    navController.navigateSingle(OwnerSubRoutes.Notification)
                },
                onUpdatePassword = onUpdatePassword,
                onPasswordUpdated = onShowMessage
            )
        }
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
