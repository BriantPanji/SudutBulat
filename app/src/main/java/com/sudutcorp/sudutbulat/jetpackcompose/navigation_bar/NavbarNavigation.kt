package com.sudutcorp.sudutbulat.jetpackcompose.navigation_bar

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
import androidx.navigation.compose.rememberNavController
import com.sudutcorp.sudutbulat.AIFab
import com.sudutcorp.sudutbulat.jetpackcompose.Role
import kotlinx.coroutines.launch

@Composable
fun NavbarNavigation(
    modifier: Modifier = Modifier,
    userRole: Role = Role.OWNER,
    onLogout: () -> Unit = {}
) {
    val navController = rememberNavController()
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
            SBNavbar(
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
            navItems.getOrNull(pageIndex)?.screen?.invoke(navController, onLogout)
        }
    }
}