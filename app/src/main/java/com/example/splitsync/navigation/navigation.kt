package com.example.splitsync.navigation

import androidx.compose.runtime.*
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.example.splitsync.data.DataManager
import com.example.splitsync.screens.*

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val dataManager = remember { DataManager() }

    NavHost(navController = navController, startDestination = "login") {
        composable("login") {
            LoginScreen(
                onLoginSuccess = { email ->
                    dataManager.currentUserEmail = email
                    navController.navigate("main") {
                        popUpTo("login") { inclusive = true }
                    }
                }
            )
        }

        composable("main") {
            MainScreen(
                dataManager = dataManager,
                onNavigateToTrip = { tripId ->
                    navController.navigate("trip/$tripId")
                },
                onNavigateToSettings = { navController.navigate("settings") },
                onNavigateToProfile = { navController.navigate("profile") },
                onNavigateToMap = { navController.navigate("map") },
                // UPDATED: Pointing to the new History route
                onNavigateToHistory = { navController.navigate("history") },
                onNavigateToCreateTrip = { navController.navigate("createTrip") }
            )
        }

        composable("createTrip") {
            CreateTripScreen(
                dataManager = dataManager,
                onTripCreated = { navController.popBackStack() },
                onBack = { navController.popBackStack() }
            )
        }

        composable(
            "trip/{tripId}",
            arguments = listOf(navArgument("tripId") { type = NavType.StringType })
        ) { backStackEntry ->
            val tripId = backStackEntry.arguments?.getString("tripId") ?: ""
            TripDetailScreen(
                tripId = tripId,
                dataManager = dataManager,
                onBack = { navController.popBackStack() },
                onNavigateToAddExpense = { navController.navigate("addExpense/$tripId") }
            )
        }

        composable(
            "addExpense/{tripId}",
            arguments = listOf(navArgument("tripId") { type = NavType.StringType })
        ) { backStackEntry ->
            val tripId = backStackEntry.arguments?.getString("tripId") ?: ""
            AddExpenseScreen(
                tripId = tripId,
                dataManager = dataManager,
                onExpenseAdded = { navController.popBackStack() },
                onBack = { navController.popBackStack() }
            )
        }

        composable("settings") {
            SettingsScreen(onBack = { navController.popBackStack() })
        }

        composable("profile") {
            ProfileScreen(
                dataManager = dataManager,
                onBack = { navController.popBackStack() }
            )
        }

        composable("map") {
            MapScreen(onBack = { navController.popBackStack() })
        }

        // REMOVED: Conversations route

        composable("history") {
            HistoryScreen(
                dataManager = dataManager,
                onBack = { navController.popBackStack() }
            )
        }
    }
}