package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.ui.navigation.Screen
import com.example.ui.screens.*
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.VehicleViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: VehicleViewModel by viewModels { VehicleViewModel.Factory }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        this.enableEdgeToEdge()
        
        setContent {
            MyApplicationTheme {
                val navController = rememberNavController()
                
                NavHost(
                    navController = navController,
                    startDestination = Screen.Home.route,
                    modifier = Modifier.fillMaxSize()
                ) {
                    composable(route = Screen.Home.route) {
                        HomeScreen(navController = navController, viewModel = viewModel)
                    }
                    
                    composable(
                        route = Screen.VehicleForm.route,
                        arguments = listOf(
                            navArgument("vehicleId") {
                                type = NavType.StringType
                                nullable = true
                                defaultValue = null
                            }
                        )
                    ) { backStackEntry ->
                        val vehicleIdStr = backStackEntry.arguments?.getString("vehicleId")
                        val vehicleId = vehicleIdStr?.toLongOrNull()
                        VehicleFormScreen(
                            navController = navController,
                            viewModel = viewModel,
                            vehicleId = vehicleId
                        )
                    }
                    
                    composable(
                        route = Screen.MaintenanceForm.route,
                        arguments = listOf(
                            navArgument("vehicleId") {
                                type = NavType.StringType
                            }
                        )
                    ) { backStackEntry ->
                        val vehicleIdStr = backStackEntry.arguments?.getString("vehicleId")
                        val vehicleId = vehicleIdStr?.toLongOrNull() ?: 0L
                        MaintenanceScreen(
                            navController = navController,
                            viewModel = viewModel,
                            vehicleId = vehicleId
                        )
                    }
                    
                    composable(
                        route = Screen.FuelForm.route,
                        arguments = listOf(
                            navArgument("vehicleId") {
                                type = NavType.StringType
                            }
                        )
                    ) { backStackEntry ->
                        val vehicleIdStr = backStackEntry.arguments?.getString("vehicleId")
                        val vehicleId = vehicleIdStr?.toLongOrNull() ?: 0L
                        FuelScreen(
                            navController = navController,
                            viewModel = viewModel,
                            vehicleId = vehicleId
                        )
                    }
                    
                    composable(route = Screen.Reminders.route) {
                        RemindersScreen(navController = navController, viewModel = viewModel)
                    }
                    
                    composable(route = Screen.Statistics.route) {
                        StatisticsScreen(navController = navController, viewModel = viewModel)
                    }
                    
                    composable(route = Screen.Settings.route) {
                        SettingsScreen(navController = navController, viewModel = viewModel)
                    }
                }
            }
        }
    }
}
