package com.sudutcorp.sudutbulat.navigation

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.google.firebase.auth.FirebaseAuth
import com.sudutcorp.sudutbulat.core.components.AIFab
import com.sudutcorp.sudutbulat.data.model.Role
import com.sudutcorp.sudutbulat.feature.auth.LoginScreen
import kotlinx.coroutines.launch

@Composable
fun AppNavigation(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
    userRole: Role = Role.OWNER
) {
    NavHost(
        navController = navController,
        startDestination = AppRoutes.Login,
        modifier = modifier
    ) {
        composable<AppRoutes.Login> {
            LoginScreen(
                onLoginSuccess = {
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
                userRole = userRole,
                onLogout = {
                    runCatching { FirebaseAuth.getInstance().signOut() }
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
    onLogout: () -> Unit = {}
) {
    val nestedNavController = rememberNavController()
    val navItems = remember(userRole) { getNavItemsForRole(userRole) }
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
