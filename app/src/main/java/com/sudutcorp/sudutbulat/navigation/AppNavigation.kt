package com.sudutcorp.sudutbulat.navigation

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.sudutcorp.sudutbulat.SudutBulatApplication
import com.sudutcorp.sudutbulat.core.components.AIFab
import com.sudutcorp.sudutbulat.data.DefaultAppContainer
import com.sudutcorp.sudutbulat.data.auth.AuthRepository
import com.sudutcorp.sudutbulat.data.model.Role
import com.sudutcorp.sudutbulat.feature.auth.LoginScreen
import com.sudutcorp.sudutbulat.feature.home.WarehouseRoleBottomSheet
import com.sudutcorp.sudutbulat.feature.notification.OwnerNotificationScreen
import com.sudutcorp.sudutbulat.feature.outlet.AddOutletScreen
import com.sudutcorp.sudutbulat.feature.outlet.OutletScreen
import com.sudutcorp.sudutbulat.feature.outlet.OwnerDetailOutletScreen
import com.sudutcorp.sudutbulat.feature.owner.OwnerUiEvent
import com.sudutcorp.sudutbulat.feature.owner.OwnerViewModel
import com.sudutcorp.sudutbulat.feature.report.OwnerDetailReportScreen
import com.sudutcorp.sudutbulat.feature.stock.GudangStockScreen
import com.sudutcorp.sudutbulat.feature.user.AddUserScreen
import kotlinx.coroutines.launch

private val SmoothSlideEasing = CubicBezierEasing(0.22f, 1f, 0.36f, 1f)

private fun Role.workspaceOrder(): Int = when (this) {
    Role.DEV, Role.OWNER -> 0
    Role.WAREHOUSE -> 1
    Role.OUTLET -> 2
}

@Composable
fun AppNavigation(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
    userRole: Role? = null,
    authRepository: AuthRepository = remember { AuthRepository() }
) {
    var currentRole by rememberSaveable {
        mutableStateOf(userRole ?: authRepository.getInitialRole())
    }
    var currentEmail by rememberSaveable {
        mutableStateOf(authRepository.getCurrentUserEmail())
    }

    LaunchedEffect(userRole) {
        if (userRole != null) {
            currentRole = userRole
        } else if (authRepository.isUserLoggedIn()) {
            currentEmail = authRepository.getCurrentUserEmail()
            authRepository.fetchCurrentUserRole { resolvedRole ->
                currentRole = resolvedRole
            }
        }
    }

    val startDestination = remember {
        if (authRepository.isUserLoggedIn()) AppRoutes.Main else AppRoutes.Login
    }

    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier,
        enterTransition = {
            slideInHorizontally(tween(300, easing = SmoothSlideEasing)) { it }
        },
        exitTransition = {
            slideOutHorizontally(tween(300, easing = SmoothSlideEasing)) { -it / 4 }
        },
        popEnterTransition = {
            slideInHorizontally(tween(280, easing = SmoothSlideEasing)) { -it / 4 }
        },
        popExitTransition = {
            slideOutHorizontally(tween(280, easing = SmoothSlideEasing)) { it }
        }
    ) {
        composable<AppRoutes.Login> {
            LoginScreen(
                onLoginSuccess = { resolvedRole ->
                    currentRole = resolvedRole
                    currentEmail = authRepository.getCurrentUserEmail()
                    navController.navigate(AppRoutes.Main) {
                        popUpTo<AppRoutes.Login> {
                            inclusive = true
                        }
                        launchSingleTop = true
                    }
                },
                onNavigateToRegister = {
                    navController.navigate(AppRoutes.Register)
                }
            )
        }

        composable<AppRoutes.Main> {
            MainScreen(
                userRole = currentRole,
                userEmail = currentEmail,
                onLogout = {
                    authRepository.logout()
                    currentEmail = null
                    navController.navigate(AppRoutes.Login) {
                        popUpTo<AppRoutes.Main> {
                            inclusive = true
                        }
                        launchSingleTop = true
                    }
                }
            )
        }
    }
}

@Composable
fun MainScreen(
    modifier: Modifier = Modifier,
    userRole: Role = Role.OWNER,
    userEmail: String? = null,
    onLogout: () -> Unit = {}
) {
    val context = LocalContext.current
    val appContainer = remember(context) {
        (context.applicationContext as? SudutBulatApplication)?.container ?: DefaultAppContainer()
    }
    val ownerViewModel = remember(appContainer) {
        OwnerViewModel(
            outletRepository = appContainer.outletRepository,
            userRepository = appContainer.userRepository,
            dailyReportRepository = appContainer.dailyReportRepository,
            materialRepository = appContainer.materialRepository,
            productRepository = appContainer.productRepository,
            authRepository = appContainer.authRepository
        )
    }
    val ownerUiState by ownerViewModel.state.collectAsStateWithLifecycle()
    val currentReportSummary = remember(
        ownerUiState.selectedReportPeriod,
        ownerUiState.expenses,
        ownerUiState.damagedMaterials,
        ownerUiState.materials
    ) {
        ownerViewModel.getReportPeriodSummary(ownerUiState.selectedReportPeriod)
    }

    val isDevAccount = userRole == Role.DEV
    var activeViewRole by rememberSaveable(userRole) {
        mutableStateOf(if (isDevAccount) Role.OWNER else userRole)
    }
    var showRoleBottomSheet by rememberSaveable { mutableStateOf(false) }

    val nestedNavController = rememberNavController()
    val pagerState = rememberPagerState(initialPage = 0) { 4 }
    val coroutineScope = rememberCoroutineScope()

    val onSelectTab: (Int) -> Unit = { tabIndex ->
        coroutineScope.launch {
            pagerState.scrollToPage(tabIndex.coerceIn(0, 3))
        }
    }

    val navItems = remember(
        activeViewRole,
        userEmail,
        isDevAccount,
        ownerUiState,
        currentReportSummary
    ) {
        getNavItemsForRole(
            role = activeViewRole,
            userEmail = userEmail,
            isDevAccount = isDevAccount,
            onOpenRoleSwitcher = {
                if (isDevAccount) showRoleBottomSheet = true
            },
            ownerUiState = ownerUiState,
            reportSummary = currentReportSummary,
            onOwnerEvent = ownerViewModel::onEvent,
            onSelectTab = onSelectTab
        )
    }

    NavHost(
        navController = nestedNavController,
        startDestination = OwnerSubRoutes.TabsRoot,
        modifier = modifier.fillMaxSize(),
        enterTransition = {
            slideInHorizontally(tween(300, easing = SmoothSlideEasing)) { it }
        },
        exitTransition = {
            slideOutHorizontally(tween(300, easing = SmoothSlideEasing)) { -it / 4 }
        },
        popEnterTransition = {
            slideInHorizontally(tween(280, easing = SmoothSlideEasing)) { -it / 4 }
        },
        popExitTransition = {
            slideOutHorizontally(tween(280, easing = SmoothSlideEasing)) { it }
        }
    ) {
        composable<OwnerSubRoutes.TabsRoot> {
            BackHandler(enabled = pagerState.targetPage != 0) {
                coroutineScope.launch {
                    pagerState.scrollToPage(0)
                }
            }

            Scaffold(
                bottomBar = {
                    BottomNavigationBar(
                        items = navItems,
                        selectedPageIndex = pagerState.targetPage,
                        onPageSelected = { index ->
                            if (pagerState.targetPage != index) {
                                coroutineScope.launch {
                                    pagerState.scrollToPage(index)
                                }
                            }
                        }
                    )
                },
                floatingActionButton = { AIFab() }
            ) { innerPadding ->
                AnimatedContent(
                    targetState = activeViewRole,
                    transitionSpec = {
                        val forward = targetState.workspaceOrder() >= initialState.workspaceOrder()
                        val direction = if (forward) 1 else -1
                        val slideSpec = tween<androidx.compose.ui.unit.IntOffset>(
                            durationMillis = 300,
                            easing = SmoothSlideEasing
                        )

                        slideInHorizontally(animationSpec = slideSpec) { fullWidth ->
                            direction * fullWidth
                        } togetherWith slideOutHorizontally(animationSpec = slideSpec) { fullWidth ->
                            -direction * (fullWidth / 4)
                        }
                    },
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    label = "RoleWorkspaceTransition"
                ) { targetRole ->
                    val roleNavItems = getNavItemsForRole(
                        role = targetRole,
                        userEmail = userEmail,
                        isDevAccount = isDevAccount,
                        onOpenRoleSwitcher = {
                            if (isDevAccount) showRoleBottomSheet = true
                        },
                        ownerUiState = ownerUiState,
                        reportSummary = currentReportSummary,
                        onOwnerEvent = ownerViewModel::onEvent,
                        onSelectTab = onSelectTab
                    )
                    HorizontalPager(
                        state = pagerState,
                        beyondViewportPageCount = 1,
                        modifier = Modifier.fillMaxSize()
                    ) { pageIndex ->
                        roleNavItems.getOrNull(pageIndex)?.screen?.invoke(nestedNavController, onLogout)
                    }
                }
            }
        }

        // Figma 204:5797 — Owner — Daftar Outlet
        composable<OwnerSubRoutes.OutletList> {
            OutletScreen(
                navController = nestedNavController,
                uiState = ownerUiState,
                onEvent = ownerViewModel::onEvent,
                isDevMode = isDevAccount,
                onSwitchRoleClick = { if (isDevAccount) showRoleBottomSheet = true },
                onOutletClick = { outletId ->
                    nestedNavController.navigate(OwnerSubRoutes.DetailOutlet(outletId))
                },
                onAddOutletClick = {
                    ownerViewModel.onEvent(OwnerUiEvent.ResetAddOutletForm)
                    nestedNavController.navigate(OwnerSubRoutes.AddOutlet)
                },
                isStandaloneListScreen = true,
                onBackClick = { nestedNavController.popBackStack() }
            )
        }

        // Figma 27:2447 — Owner — Detail Outlet
        composable<OwnerSubRoutes.DetailOutlet> { backStackEntry ->
            val route = backStackEntry.toRoute<OwnerSubRoutes.DetailOutlet>()
            OwnerDetailOutletScreen(
                navController = nestedNavController,
                outletId = route.outletId,
                uiState = ownerUiState,
                onEvent = ownerViewModel::onEvent,
                onAddStaffClick = { outletId ->
                    ownerViewModel.onEvent(OwnerUiEvent.InitAddUserForm(outletId))
                    nestedNavController.navigate(OwnerSubRoutes.AddUser(outletId))
                }
            )
        }

        // Figma 204:6480 — Owner — Tambah Outlet
        composable<OwnerSubRoutes.AddOutlet> {
            AddOutletScreen(
                navController = nestedNavController,
                uiState = ownerUiState,
                effects = ownerViewModel.effects,
                onEvent = ownerViewModel::onEvent,
                onAddNewUserClick = {
                    ownerViewModel.onEvent(OwnerUiEvent.InitAddUserForm(null))
                    nestedNavController.navigate(OwnerSubRoutes.AddUser(null))
                }
            )
        }

        // Figma 204:6610 — Owner — Tambah Pengguna
        composable<OwnerSubRoutes.AddUser> { backStackEntry ->
            val route = backStackEntry.toRoute<OwnerSubRoutes.AddUser>()
            AddUserScreen(
                navController = nestedNavController,
                preselectedOutletId = route.preselectedOutletId,
                uiState = ownerUiState,
                effects = ownerViewModel.effects,
                onEvent = ownerViewModel::onEvent
            )
        }

        // Figma 27:2847 — Owner — Detail Laporan
        composable<OwnerSubRoutes.DetailReport> { backStackEntry ->
            val route = backStackEntry.toRoute<OwnerSubRoutes.DetailReport>()
            val periodSummary = ownerViewModel.getReportPeriodSummary(route.period)
            OwnerDetailReportScreen(
                navController = nestedNavController,
                reportSummary = periodSummary
            )
        }

        // Figma 204:6037 — Daftar Barang (Katalog)
        composable<OwnerSubRoutes.Catalog> {
            GudangStockScreen(
                navController = nestedNavController,
                uiState = ownerUiState,
                onEvent = ownerViewModel::onEvent,
                roleEyebrow = if (activeViewRole == Role.WAREHOUSE) "Admin Gudang" else "Owner",
                initials = if (isDevAccount) "DV" else ownerUiState.ownerInitials,
                isStandaloneScreen = true,
                onBackClick = { nestedNavController.popBackStack() }
            )
        }

        // Figma 27:2291 — Owner — Notifikasi
        composable<OwnerSubRoutes.Notification> {
            OwnerNotificationScreen(
                navController = nestedNavController,
                uiState = ownerUiState,
                onEvent = ownerViewModel::onEvent,
                onNotificationClick = { notif ->
                    if (!notif.targetOutletId.isNullOrBlank()) {
                        nestedNavController.navigate(OwnerSubRoutes.DetailOutlet(notif.targetOutletId))
                    } else {
                        nestedNavController.navigate(OwnerSubRoutes.DetailReport(ownerUiState.selectedReportPeriod))
                    }
                }
            )
        }
    }

    if (isDevAccount && showRoleBottomSheet) {
        WarehouseRoleBottomSheet(
            selectedRole = activeViewRole,
            onDismiss = { showRoleBottomSheet = false },
            onRoleSelected = { chosenRole ->
                activeViewRole = chosenRole
                showRoleBottomSheet = false
            }
        )
    }
}
