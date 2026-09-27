package com.example

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Games
import androidx.compose.material.icons.filled.GpsFixed
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.ui.screens.CrosshairScreen
import com.example.ui.screens.GameProfileScreen
import com.example.ui.screens.MainDashboardScreen
import com.example.ui.screens.PerformanceScreen
import com.example.ui.screens.RecordingScreen
import com.example.ui.screens.SensitivityScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.SimulationOverlayScreen
import com.example.ui.theme.GamingBorderGlow
import com.example.ui.theme.GamingGlassBg
import com.example.ui.theme.GamingPanelBg
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.CompanionViewModel

sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    object Dashboard : Screen("dashboard", "Dashboard", Icons.Default.Dashboard)
    object Crosshair : Screen("crosshair", "Crosshair", Icons.Default.GpsFixed)
    object Sensitivity : Screen("sensitivity", "Sensi", Icons.Default.Tune)
    object Performance : Screen("performance", "Perform", Icons.Default.Speed)
    object Games : Screen("games", "Games", Icons.Default.Games)
    object Recording : Screen("recording", "Record", Icons.Default.Speed)
    object Settings : Screen("settings", "Settings", Icons.Outlined.Settings)
}

class MainActivity : ComponentActivity() {

    private val viewModel: CompanionViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MyApplicationTheme {
                // Runtime Notification Permission Request for Android 13+
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    val permissionLauncher = rememberLauncherForActivityResult(
                        contract = ActivityResultContracts.RequestPermission()
                    ) { /* granted or denied handled gracefully */ }

                    LaunchedEffect(Unit) {
                        if (ContextCompat.checkSelfPermission(
                                this@MainActivity,
                                Manifest.permission.POST_NOTIFICATIONS
                            ) != PackageManager.PERMISSION_GRANTED
                        ) {
                            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                        }
                    }
                }

                ZXGamingCompanionApp(viewModel = viewModel)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.refreshOverlayState()
    }
}

@Composable
fun ZXGamingCompanionApp(viewModel: CompanionViewModel) {
    var currentRoute by remember { mutableStateOf("dashboard") }
    var isSimulationOpen by remember { mutableStateOf(false) }

    if (isSimulationOpen) {
        SimulationOverlayScreen(
            viewModel = viewModel,
            onCloseSimulation = { isSimulationOpen = false }
        )
    } else {
        val navItems = listOf(
            Screen.Dashboard,
            Screen.Crosshair,
            Screen.Sensitivity,
            Screen.Performance,
            Screen.Games,
            Screen.Settings
        )

        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = Color(0xFF070B0E),
            contentWindowInsets = WindowInsets.navigationBars,
            bottomBar = {
                NavigationBar(
                    containerColor = GamingPanelBg,
                    tonalElevation = 8.dp,
                    modifier = Modifier.testTag("bottom_nav_bar")
                ) {
                    navItems.forEach { screen ->
                        val isSelected = currentRoute == screen.route
                        NavigationBarItem(
                            selected = isSelected,
                            onClick = { currentRoute = screen.route },
                            icon = {
                                Icon(
                                    imageVector = screen.icon,
                                    contentDescription = screen.title
                                )
                            },
                            label = {
                                Text(
                                    text = screen.title,
                                    fontSize = 10.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = Color.Black,
                                selectedTextColor = NeonGreen,
                                indicatorColor = NeonGreen,
                                unselectedIconColor = TextMuted,
                                unselectedTextColor = TextMuted
                            ),
                            modifier = Modifier.testTag("nav_item_${screen.route}")
                        )
                    }
                }
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .background(Color(0xFF070B0E))
            ) {
                AnimatedContent(
                    targetState = currentRoute,
                    transitionSpec = { fadeIn() togetherWith fadeOut() },
                    label = "screen_transition"
                ) { route ->
                    when (route) {
                        Screen.Dashboard.route -> MainDashboardScreen(
                            viewModel = viewModel,
                            onNavigate = { currentRoute = it },
                            onOpenSimulation = { isSimulationOpen = true }
                        )
                        Screen.Crosshair.route -> CrosshairScreen(
                            viewModel = viewModel,
                            onBack = { currentRoute = Screen.Dashboard.route }
                        )
                        Screen.Sensitivity.route -> SensitivityScreen(
                            viewModel = viewModel,
                            onBack = { currentRoute = Screen.Dashboard.route }
                        )
                        Screen.Performance.route -> PerformanceScreen(
                            viewModel = viewModel,
                            onBack = { currentRoute = Screen.Dashboard.route }
                        )
                        Screen.Games.route -> GameProfileScreen(
                            viewModel = viewModel,
                            onBack = { currentRoute = Screen.Dashboard.route }
                        )
                        Screen.Recording.route -> RecordingScreen(
                            viewModel = viewModel,
                            onBack = { currentRoute = Screen.Dashboard.route }
                        )
                        Screen.Settings.route -> SettingsScreen(
                            viewModel = viewModel,
                            onBack = { currentRoute = Screen.Dashboard.route }
                        )
                        else -> MainDashboardScreen(
                            viewModel = viewModel,
                            onNavigate = { currentRoute = it },
                            onOpenSimulation = { isSimulationOpen = true }
                        )
                    }
                }
            }
        }
    }
}
