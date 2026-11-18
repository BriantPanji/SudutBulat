package com.sudutcorp.sudutbulat.navigation

import androidx.activity.compose.BackHandler
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
import kotlinx.coroutines.launch

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
    val nestedNavController = rememberNavController()
    val navItems = remember(userRole, userEmail) { getNavItemsForRole(userRole, userEmail) }
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
                        pagerState.scrollToPage(index)
                    }
                }
            )
        },
        floatingActionButton = { AIFab() },
        modifier = modifier
    ) { innerPadding ->
        HorizontalPager(
            state = pagerState,
            beyondViewportPageCount = 1,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) { pageIndex ->
            navItems.getOrNull(pageIndex)?.screen?.invoke(nestedNavController, onLogout)
        }
    }
}
