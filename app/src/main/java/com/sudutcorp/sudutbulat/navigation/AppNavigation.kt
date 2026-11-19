package com.sudutcorp.sudutbulat.navigation

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.EnterExitState
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.animateDp
import androidx.compose.animation.core.tween
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.sudutcorp.sudutbulat.SudutBulatApplication
import com.sudutcorp.sudutbulat.core.components.AIFab
import com.sudutcorp.sudutbulat.core.ui.theme.SBColor
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
import com.sudutcorp.sudutbulat.feature.user.OwnerDetailStaffScreen
import kotlinx.coroutines.launch

private val SmoothSlideEasing = CubicBezierEasing(0.22f, 1f, 0.36f, 1f)

private fun Role.workspaceOrder(): Int = when (this) {
    Role.DEV, Role.OWNER -> 0
    Role.WAREHOUSE -> 1
    Role.OUTLET -> 2
}

/**
 * Wraps sub-screen destinations so Predictive Back & transition animations continuously
 * round screen corners (0.dp -> 32.dp) and apply a soft shadow/border in lockstep with
 * the gesture progress, eliminating harsh 90-degree sharp corners on white-over-white screens.
 */
@Composable
private fun AnimatedContentScope.PredictiveBackScreenContainer(
    content: @Composable () -> Unit
) {
    val cornerRadius by transition.animateDp(label = "ScreenCornerRadius") { state ->
        if (state == EnterExitState.Visible) 0.dp else 32.dp
    }
    val shadowElevation by transition.animateDp(label = "ScreenShadowElevation") { state ->
        if (state == EnterExitState.Visible) 0.dp else 18.dp
    }
    val borderStroke = if (cornerRadius > 0.5.dp) BorderStroke(1.dp, SBColor.Grey_100) else null

    Surface(
        modifier = Modifier.fillMaxSize(),
        shape = RoundedCornerShape(cornerRadius),
        color = Color.White,
        shadowElevation = shadowElevation,
        border = borderStroke
    ) {
        content()
    }
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
        },
        predictivePopEnterTransition = { swipeEdge ->
            slideInHorizontally { if (swipeEdge == 0) -it / 8 else it / 8 } +
                scaleIn(initialScale = 0.96f)
        },
        predictivePopExitTransition = { swipeEdge ->
            scaleOut(
                targetScale = 0.88f,
                transformOrigin = TransformOrigin(if (swipeEdge == 0) 0.72f else 0.28f, 0.5f)
            ) + slideOutHorizontally { if (swipeEdge == 0) it / 8 else -it / 8 }
        }
    ) {
        composable<AppRoutes.Login> {
            LoginScreen(
                onLoginSuccess = { resolvedRole ->
                    currentRole = resolvedRole
                    currentEmail = authRepository.getCurrentUserEmail()
                    navController.navigateSingle(AppRoutes.Main) {
                        popUpTo<AppRoutes.Login> {
                            inclusive = true
                        }
                    }
                },
                onNavigateToRegister = {
                    navController.navigateSingle(AppRoutes.Register)
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
                    navController.navigateSingle(AppRoutes.Login) {
                        popUpTo<AppRoutes.Main> {
                            inclusive = true
                        }
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
        },
        predictivePopEnterTransition = { swipeEdge ->
            slideInHorizontally { if (swipeEdge == 0) -it / 8 else it / 8 } +
                scaleIn(initialScale = 0.96f)
        },
        predictivePopExitTransition = { swipeEdge ->
            scaleOut(
                targetScale = 0.88f,
                transformOrigin = TransformOrigin(if (swipeEdge == 0) 0.72f else 0.28f, 0.5f)
            ) + slideOutHorizontally { if (swipeEdge == 0) it / 8 else -it / 8 }
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

        composable<OwnerSubRoutes.OutletList> {
            PredictiveBackScreenContainer {
                OutletScreen(
                    navController = nestedNavController,
                    uiState = ownerUiState,
                    onEvent = ownerViewModel::onEvent,
                    isDevMode = isDevAccount,
                    onSwitchRoleClick = { if (isDevAccount) showRoleBottomSheet = true },
                    onOutletClick = { outletId ->
                        nestedNavController.navigateSingle(OwnerSubRoutes.DetailOutlet(outletId))
                    },
                    onAddOutletClick = {
                        nestedNavController.navigateSingle(
                            route = OwnerSubRoutes.AddOutlet,
                            beforeNavigate = { ownerViewModel.onEvent(OwnerUiEvent.ResetAddOutletForm) }
                        )
                    },
                    onStaffClick = { userId ->
                        nestedNavController.navigateSingle(OwnerSubRoutes.DetailStaff(userId))
                    },
                    onAddStaffAccountClick = {
                        nestedNavController.navigateSingle(
                            route = OwnerSubRoutes.AddUser(null),
                            beforeNavigate = { ownerViewModel.onEvent(OwnerUiEvent.InitAddUserForm(null)) }
                        )
                    },
                    isStandaloneListScreen = true,
                    onBackClick = { nestedNavController.popBackStackSafe() }
                )
            }
        }

        composable<OwnerSubRoutes.DetailOutlet> { backStackEntry ->
            val route = backStackEntry.toRoute<OwnerSubRoutes.DetailOutlet>()
            PredictiveBackScreenContainer {
                OwnerDetailOutletScreen(
                    navController = nestedNavController,
                    outletId = route.outletId,
                    uiState = ownerUiState,
                    onEvent = ownerViewModel::onEvent,
                    onReportClick = { reportId ->
                        nestedNavController.navigateSingle(
                            OwnerSubRoutes.DetailReport(
                                period = ownerUiState.selectedReportPeriod,
                                reportId = reportId
                            )
                        )
                    }
                )
            }
        }

        composable<OwnerSubRoutes.DetailStaff> { backStackEntry ->
            val route = backStackEntry.toRoute<OwnerSubRoutes.DetailStaff>()
            PredictiveBackScreenContainer {
                OwnerDetailStaffScreen(
                    navController = nestedNavController,
                    userId = route.userId,
                    uiState = ownerUiState,
                    onEvent = ownerViewModel::onEvent,
                    onOutletClick = { outletId ->
                        nestedNavController.navigateSingle(OwnerSubRoutes.DetailOutlet(outletId))
                    }
                )
            }
        }

        composable<OwnerSubRoutes.AddOutlet> {
            PredictiveBackScreenContainer {
                AddOutletScreen(
                    navController = nestedNavController,
                    uiState = ownerUiState,
                    effects = ownerViewModel.effects,
                    onEvent = ownerViewModel::onEvent,
                    onAddNewUserClick = {
                        nestedNavController.navigateSingle(
                            route = OwnerSubRoutes.AddUser(null),
                            beforeNavigate = { ownerViewModel.onEvent(OwnerUiEvent.InitAddUserForm(null)) }
                        )
                    }
                )
            }
        }

        composable<OwnerSubRoutes.AddUser> { backStackEntry ->
            val route = backStackEntry.toRoute<OwnerSubRoutes.AddUser>()
            PredictiveBackScreenContainer {
                AddUserScreen(
                    navController = nestedNavController,
                    preselectedOutletId = route.preselectedOutletId,
                    uiState = ownerUiState,
                    effects = ownerViewModel.effects,
                    onEvent = ownerViewModel::onEvent
                )
            }
        }

        composable<OwnerSubRoutes.DetailReport> { backStackEntry ->
            val route = backStackEntry.toRoute<OwnerSubRoutes.DetailReport>()
            val periodSummary = ownerViewModel.getReportPeriodSummary(route.period)
            val singleReport = route.reportId?.let { ownerViewModel.getSingleReportDetail(it) }
            PredictiveBackScreenContainer {
                OwnerDetailReportScreen(
                    navController = nestedNavController,
                    reportSummary = periodSummary,
                    singleReport = singleReport
                )
            }
        }

        composable<OwnerSubRoutes.Catalog> {
            PredictiveBackScreenContainer {
                GudangStockScreen(
                    navController = nestedNavController,
                    uiState = ownerUiState,
                    onEvent = ownerViewModel::onEvent,
                    roleEyebrow = if (activeViewRole == Role.WAREHOUSE) "Admin Gudang" else "Owner",
                    initials = if (isDevAccount) "DV" else ownerUiState.ownerInitials,
                    isStandaloneScreen = true,
                    onBackClick = { nestedNavController.popBackStackSafe() }
                )
            }
        }

        composable<OwnerSubRoutes.Notification> {
            PredictiveBackScreenContainer {
                OwnerNotificationScreen(
                    navController = nestedNavController,
                    uiState = ownerUiState,
                    onEvent = ownerViewModel::onEvent,
                    onNotificationClick = { notif ->
                        if (!notif.targetOutletId.isNullOrBlank()) {
                            nestedNavController.navigateSingle(OwnerSubRoutes.DetailOutlet(notif.targetOutletId))
                        } else {
                            nestedNavController.navigateSingle(OwnerSubRoutes.DetailReport(period = ownerUiState.selectedReportPeriod))
                        }
                    }
                )
            }
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
