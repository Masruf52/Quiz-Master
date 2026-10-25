package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Leaderboard
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Redeem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.ui.screens.AdminDashboardScreen
import com.example.ui.screens.CategoriesScreen
import com.example.ui.screens.CelebrationOverlay
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.LeaderboardScreen
import com.example.ui.screens.LoginScreen
import com.example.ui.screens.ProfileScreen
import com.example.ui.screens.QuizPlayScreen
import com.example.ui.screens.RegisterScreen
import com.example.ui.screens.ResultScreen
import com.example.ui.screens.RewardsLogScreen
import com.example.ui.screens.RewardsScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.FrostedBackgroundBox
import com.example.ui.viewmodel.QuizViewModel
import com.example.data.model.Quiz

class MainActivity : ComponentActivity() {

    private val viewModel: QuizViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val darkMode by viewModel.darkModeEnabled.collectAsState()

            MyApplicationTheme(darkTheme = darkMode) {
                val navController = rememberNavController()
                val currentUserId by viewModel.currentUserId.collectAsState()
                val celebrationEvent by viewModel.celebrationEvent.collectAsState()
                val soundEffectsEnabled by viewModel.soundEffectsEnabled.collectAsState()

                Box(modifier = Modifier.fillMaxSize()) {
                    NavHost(
                        navController = navController,
                        startDestination = if (currentUserId == null) "login" else "dashboard",
                        modifier = Modifier
                            .fillMaxSize()
                            .background(MaterialTheme.colorScheme.background)
                    ) {
                        // 1. Login Screen
                        composable("login") {
                            LoginScreen(
                                viewModel = viewModel,
                                onAuthSuccess = {
                                    navController.navigate("dashboard") {
                                        popUpTo("login") { inclusive = true }
                                    }
                                },
                                onNavigateToRegister = {
                                    navController.navigate("register")
                                }
                            )
                        }

                        // 2. Register Screen
                        composable("register") {
                            RegisterScreen(
                                viewModel = viewModel,
                                onAuthSuccess = {
                                    navController.navigate("dashboard") {
                                        popUpTo("register") { inclusive = true }
                                        popUpTo("login") { inclusive = true }
                                    }
                                },
                                onNavigateToLogin = {
                                    navController.navigate("login") {
                                        popUpTo("register") { inclusive = true }
                                    }
                                }
                            )
                        }

                        // 3. Dashboard Container (with bottom navigation)
                        composable("dashboard") {
                            DashboardContainerScreen(
                                viewModel = viewModel,
                                onNavigateToQuizPlay = { quiz ->
                                    viewModel.startQuiz(quiz)
                                    navController.navigate("quiz_play")
                                },
                                onNavigateToSettings = {
                                    navController.navigate("settings")
                                },
                                onNavigateToRewardsLog = {
                                    navController.navigate("rewards_log")
                                },
                                onLogoutSuccess = {
                                    navController.navigate("login") {
                                        popUpTo("dashboard") { inclusive = true }
                                    }
                                }
                            )
                        }

                        // 4. Quiz Gameplay Play Screen
                        composable("quiz_play") {
                            QuizPlayScreen(
                                viewModel = viewModel,
                                onNavigateToResult = {
                                    navController.navigate("result") {
                                        popUpTo("quiz_play") { inclusive = true }
                                    }
                                },
                                onBackToHome = {
                                    navController.popBackStack()
                                }
                            )
                        }

                        // 5. Result Summary Screen
                        composable("result") {
                            ResultScreen(
                                viewModel = viewModel,
                                onRetry = {
                                    val active = viewModel.activeQuiz.value
                                    if (active != null) {
                                        viewModel.startQuiz(active)
                                        navController.navigate("quiz_play") {
                                            popUpTo("result") { inclusive = true }
                                        }
                                    }
                                },
                                onBackHome = {
                                    viewModel.forceResetQuizPlay()
                                    navController.navigate("dashboard") {
                                        popUpTo("result") { inclusive = true }
                                    }
                                }
                            )
                        }

                        // 6. Settings Screen
                        composable("settings") {
                            SettingsScreen(
                                viewModel = viewModel,
                                onBack = {
                                    navController.popBackStack()
                                },
                                onNavigateToAdmin = {
                                    navController.navigate("admin")
                                },
                                onLogoutSuccess = {
                                    navController.navigate("login") {
                                        popUpTo("settings") { inclusive = true }
                                        popUpTo("dashboard") { inclusive = true }
                                    }
                                }
                            )
                        }

                        // 7. Admin Panel Screen
                        composable("admin") {
                            AdminDashboardScreen(
                                viewModel = viewModel,
                                onBack = {
                                    navController.popBackStack()
                                }
                            )
                        }

                        // 8. Chronological Rewards & Achievements Log Screen
                        composable("rewards_log") {
                            RewardsLogScreen(
                                viewModel = viewModel,
                                onBack = {
                                    navController.popBackStack()
                                }
                            )
                        }
                    }

                    CelebrationOverlay(
                        celebration = celebrationEvent,
                        onDismiss = { viewModel.dismissCelebration() },
                        soundEnabled = soundEffectsEnabled
                    )
                }
            }
        }
    }
}

@Composable
fun DashboardContainerScreen(
    viewModel: QuizViewModel,
    onNavigateToQuizPlay: (Quiz) -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToRewardsLog: () -> Unit = {},
    onLogoutSuccess: () -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) }

    FrostedBackgroundBox(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = Color.Transparent,
            bottomBar = {
                NavigationBar(
                    containerColor = Color.Transparent,
                    tonalElevation = 0.dp,
                    modifier = Modifier
                        .testTag("dashboard_bottom_navigation")
                        .background(Color(0x0DFFFFFF)) // subtle frosted glass bottom navigation overlay
                ) {
                    val items = listOf(
                        Triple(0, Icons.Default.Home, "Home"),
                        Triple(1, Icons.Default.Category, "Categories"),
                        Triple(2, Icons.Default.Leaderboard, "Leaderboard"),
                        Triple(3, Icons.Default.Redeem, "Rewards"),
                        Triple(4, Icons.Default.Person, "Profile")
                    )

                    items.forEach { (index, icon, label) ->
                        NavigationBarItem(
                            selected = selectedTab == index,
                            onClick = { selectedTab = index },
                            icon = { Icon(icon, contentDescription = label) },
                            label = { Text(label, fontSize = 10.sp, fontWeight = FontWeight.Bold) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = MaterialTheme.colorScheme.primary,
                                selectedTextColor = MaterialTheme.colorScheme.primary,
                                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                indicatorColor = Color(0x1AFFFFFF)
                            )
                        )
                    }
                }
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = innerPadding.calculateBottomPadding())
            ) {
                when (selectedTab) {
                    0 -> HomeScreen(
                        viewModel = viewModel,
                        onNavigateToQuizPlay = onNavigateToQuizPlay,
                        onNavigateToCategories = { selectedTab = 1 },
                        onNavigateToRewards = { selectedTab = 3 }
                    )
                    1 -> CategoriesScreen(
                        viewModel = viewModel,
                        onNavigateToQuizPlay = onNavigateToQuizPlay
                    )
                    2 -> LeaderboardScreen(
                        viewModel = viewModel
                    )
                    3 -> RewardsScreen(
                        viewModel = viewModel
                    )
                    4 -> ProfileScreen(
                        viewModel = viewModel,
                        onNavigateToSettings = onNavigateToSettings,
                        onNavigateToRewardsLog = onNavigateToRewardsLog,
                        onLogoutSuccess = onLogoutSuccess
                    )
                }
            }
        }
    }
}
