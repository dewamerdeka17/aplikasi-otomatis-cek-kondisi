package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.data.database.HpCheckDatabase
import com.example.data.repository.HpCheckRepository
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.DiagnosticScreen
import com.example.ui.screens.InputHpScreen
import com.example.ui.screens.OnboardingScreen
import com.example.ui.screens.ReportDetailScreen
import com.example.ui.screens.ResultScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.HpCheckViewModel
import com.example.ui.viewmodel.HpCheckViewModelFactory

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // 1. Initialize local persistent database, dao, and repository
        val database = HpCheckDatabase.getDatabase(applicationContext)
        val repository = HpCheckRepository(database.hpCheckDao())

        // 2. Initialize viewModel safely using our Factory
        val viewModel: HpCheckViewModel by viewModels { HpCheckViewModelFactory(repository) }

        setContent {
            MyApplicationTheme {
                val navController = rememberNavController()

                Scaffold(
                    modifier = Modifier.fillMaxSize()
                ) { innerPadding ->
                    NavHost(
                        navController = navController,
                        startDestination = "dashboard",
                        modifier = Modifier.padding(innerPadding)
                    ) {
                        // 1. Onboarding/Splash Screen
                        composable("onboarding") {
                            OnboardingScreen(
                                onStartChecking = {
                                    navController.navigate("dashboard") {
                                        popUpTo("onboarding") { inclusive = true }
                                    }
                                }
                            )
                        }

                        // 2. Dashboard Screen
                        composable("dashboard") {
                            DashboardScreen(
                                viewModel = viewModel,
                                onNavigateToInput = {
                                    viewModel.resetDiagnostics()
                                    navController.navigate("input_hp")
                                },
                                onNavigateToDetail = { report ->
                                    viewModel.currentReportDetail = report
                                    navController.navigate("report_detail")
                                },
                                onNavigateToSettings = {
                                    navController.navigate("settings")
                                }
                            )
                        }

                        // 3. Input HP Specs Form
                        composable("input_hp") {
                            InputHpScreen(
                                viewModel = viewModel,
                                onNavigateBack = { navController.popBackStack() },
                                onStartChecking = {
                                    navController.navigate("diagnostic")
                                }
                            )
                        }

                        // 4. Unified Hardware Diagnostic Wizard (Steps 1 to 8)
                        composable("diagnostic") {
                            DiagnosticScreen(
                                viewModel = viewModel,
                                onNavigateBack = { navController.navigate("dashboard") { popUpTo("dashboard") { inclusive = false } } },
                                onCompleted = {
                                    navController.navigate("result") {
                                        popUpTo("diagnostic") { inclusive = true }
                                    }
                                }
                            )
                        }

                        // 5. Success Scores Result page
                        composable("result") {
                            ResultScreen(
                                viewModel = viewModel,
                                onNavigateToDashboard = {
                                    navController.navigate("dashboard") {
                                        popUpTo("dashboard") { inclusive = true }
                                    }
                                },
                                onNavigateToReport = {
                                    navController.navigate("report_detail")
                                }
                            )
                        }

                        // 6. Professional print report sheet detail
                        composable("report_detail") {
                            ReportDetailScreen(
                                viewModel = viewModel,
                                onNavigateBack = { navController.popBackStack() },
                                onDeleted = {
                                    navController.navigate("dashboard") {
                                        popUpTo("dashboard") { inclusive = true }
                                    }
                                }
                            )
                        }

                        // 7. Settings & About
                        composable("settings") {
                            SettingsScreen(
                                onNavigateBack = { navController.popBackStack() }
                            )
                        }
                    }
                }
            }
        }
    }
}
