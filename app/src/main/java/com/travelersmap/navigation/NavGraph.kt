package com.travelersmap.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountBalanceWallet
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Map
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.travelersmap.ui.features.ai.AiPlannerScreen
import com.travelersmap.ui.features.budget.BudgetScreen
import com.travelersmap.ui.features.favorites.FavoritesScreen
import com.travelersmap.ui.features.map.MapHomeScreen
import com.travelersmap.ui.features.place.PlaceDetailRoute
import com.travelersmap.ui.features.route.RouteScreen
import com.travelersmap.ui.features.settings.SettingsScreen

object Routes {
    const val MAP = "map"
    const val FAVORITES = "favorites"
    const val AI = "ai"
    const val BUDGET = "budget"
    const val SETTINGS = "settings"
    const val PLACE = "place/{placeId}"
    const val ROUTE = "route/{placeId}"
    fun place(id: String) = "place/$id"
    fun route(id: String) = "route/$id"
}

private data class Tab(val route: String, val label: String, val icon: ImageVector)

@Composable
fun TravelersNavHost() {
    val nav = rememberNavController()
    val tabs = listOf(
        Tab(Routes.MAP, "Map", Icons.Outlined.Map),
        Tab(Routes.FAVORITES, "Saved", Icons.Outlined.FavoriteBorder),
        Tab(Routes.AI, "AI", Icons.Outlined.AutoAwesome),
        Tab(Routes.BUDGET, "Budget", Icons.Outlined.AccountBalanceWallet),
        Tab(Routes.SETTINGS, "Settings", Icons.Outlined.Settings)
    )
    val backStack by nav.currentBackStackEntryAsState()
    val current = backStack?.destination?.route
    val showBottom = current in tabs.map { it.route }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            if (showBottom) {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.94f),
                    tonalElevation = 0.dp
                ) {
                    tabs.forEach { tab ->
                        val selected = current == tab.route
                        NavigationBarItem(
                            selected = selected,
                            onClick = {
                                nav.navigate(tab.route) {
                                    popUpTo(nav.graph.findStartDestination().id) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = { Icon(tab.icon, contentDescription = tab.label) },
                            label = { Text(tab.label) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = MaterialTheme.colorScheme.primary,
                                selectedTextColor = MaterialTheme.colorScheme.primary,
                                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                indicatorColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        )
                    }
                }
            }
        }
    ) { padding ->
        NavHost(
            navController = nav,
            startDestination = Routes.MAP,
            modifier = Modifier.padding(padding),
            enterTransition = {
                fadeIn(tween(220)) + slideIntoContainer(
                    AnimatedContentTransitionScope.SlideDirection.Start, tween(280)
                )
            },
            exitTransition = { fadeOut(tween(180)) },
            popEnterTransition = {
                fadeIn(tween(220)) + slideIntoContainer(
                    AnimatedContentTransitionScope.SlideDirection.End, tween(280)
                )
            },
            popExitTransition = { fadeOut(tween(180)) }
        ) {
            composable(Routes.MAP) {
                MapHomeScreen(
                    onOpenPlace = { nav.navigate(Routes.place(it)) },
                    onSearchSelect = { nav.navigate(Routes.place(it)) }
                )
            }
            composable(Routes.FAVORITES) {
                FavoritesScreen(onOpenPlace = { nav.navigate(Routes.place(it)) })
            }
            composable(Routes.AI) {
                AiPlannerScreen(onOpenPlace = { nav.navigate(Routes.place(it)) })
            }
            composable(Routes.BUDGET) { BudgetScreen() }
            composable(Routes.SETTINGS) { SettingsScreen() }
            composable(
                route = Routes.PLACE,
                arguments = listOf(navArgument("placeId") { type = NavType.StringType })
            ) {
                PlaceDetailRoute(
                    onBack = { nav.popBackStack() },
                    onNavigate = { id -> nav.navigate(Routes.route(id)) },
                    onOpenNearby = { id -> nav.navigate(Routes.place(id)) }
                )
            }
            composable(
                route = Routes.ROUTE,
                arguments = listOf(navArgument("placeId") { type = NavType.StringType })
            ) {
                RouteScreen(onBack = { nav.popBackStack() })
            }
        }
    }
}
