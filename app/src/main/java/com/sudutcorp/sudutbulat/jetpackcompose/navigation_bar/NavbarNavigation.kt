package com.sudutcorp.sudutbulat.jetpackcompose.navigation_bar

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.sudutcorp.sudutbulat.AIFab

@Composable
fun NavbarNavigation(
    modifier: Modifier = Modifier,
    onLogout: () -> Unit = {}
) {

    val navController = rememberNavController()

    Scaffold(
        bottomBar = {
            SBNavbar(navController)
        },
        floatingActionButton = { AIFab() },
        modifier = modifier
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = NavbarRoutes.Home,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable<NavbarRoutes.Home> {
                NavbarHomeScreen(navController)
            }

            composable<NavbarRoutes.Outlet> {
                NavbarOutletScreen(navController)
            }

            composable<NavbarRoutes.OwnerReport> {
                NavbarOwnerReportScreen(navController)
            }

            composable<NavbarRoutes.Profile> {
                NavbarProfileScreen(navController = navController, onLogout = onLogout)
            }
        }
    }
}