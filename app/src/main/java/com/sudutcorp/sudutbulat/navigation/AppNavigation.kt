package com.sudutcorp.sudutbulat.navigation

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
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
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.sudutcorp.sudutbulat.core.components.AIFab
import com.sudutcorp.sudutbulat.data.auth.AuthRepository
import com.sudutcorp.sudutbulat.data.model.Role
import com.sudutcorp.sudutbulat.feature.auth.LoginScreen
import com.sudutcorp.sudutbulat.feature.home.WarehouseRoleBottomSheet
import kotlinx.coroutines.launch

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
        modifier = modifier
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
    val isDevAccount = userRole == Role.DEV
    var activeViewRole by rememberSaveable(userRole) {
        mutableStateOf(if (isDevAccount) Role.OWNER else userRole)
    }
    var showRoleBottomSheet by rememberSaveable { mutableStateOf(false) }

    val nestedNavController = rememberNavController()
    val navItems = remember(activeViewRole, userEmail, isDevAccount) {
        getNavItemsForRole(
            role = activeViewRole,
            userEmail = userEmail,
            isDevAccount = isDevAccount,
            onOpenRoleSwitcher = {
                if (isDevAccount) showRoleBottomSheet = true
            }
        )
    }
    val pagerState = rememberPagerState(initialPage = 0) { navItems.size }
    val coroutineScope = rememberCoroutineScope()

    BackHandler(enabled = pagerState.currentPage != 0) {
        coroutineScope.launch {
            pagerState.animateScrollToPage(0)
        }
    }

    Scaffold(
        bottomBar = {
            BottomNavigationBar(
                items = navItems,
                selectedPageIndex = pagerState.currentPage,
                onPageSelected = { index ->
                    coroutineScope.launch {
                        pagerState.animateScrollToPage(
                            page = index,
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioNoBouncy,
                                stiffness = Spring.StiffnessMediumLow
                            )
                        )
                    }
                }
            )
        },
        floatingActionButton = { AIFab() },
        modifier = modifier
    ) { innerPadding ->
        AnimatedContent(
            targetState = activeViewRole,
            transitionSpec = {
                val forward = targetState.workspaceOrder() >= initialState.workspaceOrder()
                val direction = if (forward) 1 else -1
                val slideSpec = spring<androidx.compose.ui.unit.IntOffset>(
                    dampingRatio = Spring.DampingRatioNoBouncy,
                    stiffness = Spring.StiffnessMediumLow
                )
                val scaleSpec = spring<Float>(
                    dampingRatio = Spring.DampingRatioLowBouncy,
                    stiffness = Spring.StiffnessMediumLow
                )

                (slideInHorizontally(animationSpec = slideSpec) { fullWidth ->
                    direction * (fullWidth / 4)
                } + scaleIn(
                    initialScale = 0.94f,
                    animationSpec = scaleSpec
                ) + fadeIn(animationSpec = tween(220))) togetherWith
                    (slideOutHorizontally(animationSpec = slideSpec) { fullWidth ->
                        -direction * (fullWidth / 4)
                    } + scaleOut(
                        targetScale = 0.94f,
                        animationSpec = scaleSpec
                    ) + fadeOut(animationSpec = tween(160)))
            },
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            label = "RoleWorkspaceTransition"
        ) { targetRole ->
            val roleNavItems = remember(targetRole, userEmail, isDevAccount) {
                getNavItemsForRole(
                    role = targetRole,
                    userEmail = userEmail,
                    isDevAccount = isDevAccount,
                    onOpenRoleSwitcher = {
                        if (isDevAccount) showRoleBottomSheet = true
                    }
                )
            }
            HorizontalPager(
                state = pagerState,
                beyondViewportPageCount = 1,
                modifier = Modifier.fillMaxSize()
            ) { pageIndex ->
                roleNavItems.getOrNull(pageIndex)?.screen?.invoke(nestedNavController, onLogout)
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
}
