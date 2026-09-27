package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import com.example.ui.components.MetricsOverlayMode
import com.example.ui.components.RealtimeMetricsOverlayComponent
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Games
import androidx.compose.material.icons.filled.GpsFixed
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.service.OverlayService
import com.example.ui.components.FuturisticHudGauge
import com.example.ui.components.QuickActionButton
import com.example.ui.theme.GamingBorderGlow
import com.example.ui.theme.GamingPanelBg
import com.example.ui.theme.GamingSurface
import com.example.ui.theme.GamingSurfaceVariant
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.NeonYellow
import com.example.ui.theme.SliderTrack
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.CompanionViewModel

@Composable
fun MainDashboardScreen(
    viewModel: CompanionViewModel,
    onNavigate: (String) -> Unit,
    onOpenSimulation: () -> Unit
) {
    val context = LocalContext.current
    val telemetry by viewModel.telemetry.collectAsState()
    val isOverlayActive by viewModel.isOverlayRunning.collectAsState()
    val isMetricsOverlayRunning by viewModel.isMetricsOverlayRunning.collectAsState()
    val crosshairConfig by viewModel.crosshairConfig.collectAsState()
    val scrollState = rememberScrollState()

    var musicVolume by remember { mutableIntStateOf(viewModel.systemMonitor.getMusicVolumePercent()) }
    var brightness by remember { mutableIntStateOf(viewModel.systemMonitor.getScreenBrightnessPercent()) }

    val hasOverlayPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        Settings.canDrawOverlays(context)
    } else true

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // App Top Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "ZX GAMING COMPANION",
                    color = Color.White,
                    fontWeight = FontWeight.Black,
                    fontSize = 18.sp,
                    letterSpacing = 1.2.sp
                )
                Text(
                    text = "Pro Esports HUD & Aim Coordinator",
                    color = TextMuted,
                    fontSize = 11.sp
                )
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (isOverlayActive) NeonGreen.copy(alpha = 0.2f) else GamingSurfaceVariant)
                    .border(
                        1.dp,
                        if (isOverlayActive) NeonGreen else GamingBorderGlow.copy(alpha = 0.3f),
                        RoundedCornerShape(12.dp)
                    )
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(if (isOverlayActive) NeonGreen else Color.Gray)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (isOverlayActive) "OVERLAY ACTIVE" else "STANDBY",
                    color = if (isOverlayActive) NeonGreen else TextSecondary,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Center Futuristic Gauge matching the video reference
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            FuturisticHudGauge(
                ramPercent = telemetry.ramPercent,
                cpuPercent = telemetry.cpuUsagePercent,
                fps = telemetry.fps,
                refreshRateHz = telemetry.refreshRateHz,
                modifier = Modifier.fillMaxWidth()
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Real Telemetry Grid
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // RAM Detail Card
            TelemetryDetailCard(
                modifier = Modifier.weight(1f),
                icon = Icons.Default.Memory,
                label = "RAM USAGE",
                primaryValue = "${String.format("%.1f", telemetry.ramUsedGb)} / ${String.format("%.1f", telemetry.ramTotalGb)} GB",
                secondaryValue = "${telemetry.ramPercent}% Allocated",
                accentColor = NeonGreen
            )
            // CPU Detail Card
            TelemetryDetailCard(
                modifier = Modifier.weight(1f),
                icon = Icons.Default.Speed,
                label = "CPU LOAD",
                primaryValue = if (telemetry.cpuUsagePercent >= 0) "${telemetry.cpuUsagePercent}%" else "Unavailable",
                secondaryValue = "Active Cores",
                accentColor = NeonCyan
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Battery & Temp Card
            TelemetryDetailCard(
                modifier = Modifier.weight(1f),
                icon = Icons.Default.BatteryChargingFull,
                label = "BATTERY",
                primaryValue = "${telemetry.batteryPercent}%",
                secondaryValue = telemetry.batteryStatus,
                accentColor = NeonYellow
            )
            // Temperature Card
            TelemetryDetailCard(
                modifier = Modifier.weight(1f),
                icon = Icons.Default.Thermostat,
                label = "TEMPERATURE",
                primaryValue = if (telemetry.batteryTemperatureC > 0) "${String.format("%.1f", telemetry.batteryTemperatureC)}°C" else "Unavailable",
                secondaryValue = "Thermal: ${telemetry.thermalStatus}",
                accentColor = if (telemetry.batteryTemperatureC > 42f) Color.Red else NeonGreen
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // GPU Detail Card
            TelemetryDetailCard(
                modifier = Modifier.weight(1f),
                icon = Icons.Default.Speed,
                label = "GPU LOAD",
                primaryValue = if (telemetry.gpuUsagePercent >= 0) "${telemetry.gpuUsagePercent}%" else "Active",
                secondaryValue = if (telemetry.gpuFrequencyMhz > 0) "${telemetry.gpuFrequencyMhz} MHz Clock" else telemetry.gpuRenderer,
                accentColor = Color(0xFFE040FB)
            )
            // FPS & Refresh Rate Card
            TelemetryDetailCard(
                modifier = Modifier.weight(1f),
                icon = Icons.Default.Refresh,
                label = "DISPLAY FPS",
                primaryValue = "${telemetry.fps} FPS",
                secondaryValue = "Screen: ${telemetry.refreshRateHz.toInt()} Hz",
                accentColor = NeonGreen
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Real-Time Metrics Overlay HUD Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = GamingPanelBg),
            shape = RoundedCornerShape(18.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, GamingBorderGlow)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "REAL-TIME METRICS OVERLAY HUD",
                            color = Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = "Displays live FPS and CPU/GPU metrics over other apps",
                            color = TextSecondary,
                            fontSize = 11.sp
                        )
                    }
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isMetricsOverlayRunning) NeonGreen.copy(alpha = 0.2f) else GamingSurfaceVariant)
                            .border(
                                1.dp,
                                if (isMetricsOverlayRunning) NeonGreen else GamingBorderGlow.copy(alpha = 0.3f),
                                RoundedCornerShape(8.dp)
                            )
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = if (isMetricsOverlayRunning) "HUD ACTIVE" else "HUD STANDBY",
                            color = if (isMetricsOverlayRunning) NeonGreen else TextMuted,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Interactive Live Preview of the Component
                Text(
                    text = "COMPONENT PREVIEW (TAP TO EXPAND / COLLAPSE):",
                    color = TextMuted,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
                Spacer(modifier = Modifier.height(6.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color(0xFF04070A))
                        .border(1.dp, GamingBorderGlow.copy(alpha = 0.3f), RoundedCornerShape(14.dp))
                        .padding(10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    RealtimeMetricsOverlayComponent(
                        telemetry = telemetry,
                        initialMode = MetricsOverlayMode.COMPACT_PILL
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Start / Stop Toggle Button
                if (!hasOverlayPermission) {
                    Button(
                        onClick = {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                val intent = Intent(
                                    Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                                    Uri.parse("package:${context.packageName}")
                                )
                                context.startActivity(intent)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFB300)),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("grant_overlay_perm_metrics_btn")
                    ) {
                        Text(
                            text = "GRANT PERMISSION FOR METRICS HUD",
                            color = Color.Black,
                            fontWeight = FontWeight.Bold
                        )
                    }
                } else {
                    Button(
                        onClick = {
                            viewModel.toggleMetricsOverlay(context)
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isMetricsOverlayRunning) Color(0xFF882233) else NeonGreen
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("toggle_metrics_overlay_btn")
                    ) {
                        Text(
                            text = if (isMetricsOverlayRunning) "STOP METRICS OVERLAY HUD" else "START REAL-TIME METRICS OVERLAY",
                            color = if (isMetricsOverlayRunning) Color.White else Color.Black,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Floating Overlay Launcher Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = GamingPanelBg),
            shape = RoundedCornerShape(18.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, GamingBorderGlow)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "FLOATING CONTROL SERVICE",
                    color = Color.White,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "Displays the movable 'ZX' floating button on top of your games to toggle HUD and crosshair.",
                    color = TextSecondary,
                    fontSize = 11.sp,
                    modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)
                )

                if (!hasOverlayPermission) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0x33FFB300))
                            .padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = "Warning",
                            tint = Color(0xFFFFB300),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Overlay permission required to draw over games.",
                            color = Color(0xFFFFE082),
                            fontSize = 11.sp,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Button(
                        onClick = {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                val intent = Intent(
                                    Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                                    Uri.parse("package:${context.packageName}")
                                )
                                context.startActivity(intent)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFB300)),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("request_overlay_button")
                    ) {
                        Text(text = "GRANT OVERLAY PERMISSION", color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = {
                                val intent = Intent(context, OverlayService::class.java).apply {
                                    action = if (isOverlayActive) OverlayService.ACTION_STOP else OverlayService.ACTION_START
                                }
                                try {
                                    if (isOverlayActive) {
                                        context.stopService(intent)
                                    } else {
                                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                            context.startForegroundService(intent)
                                        } else {
                                            context.startService(intent)
                                        }
                                    }
                                } catch (_: Exception) {}
                                viewModel.refreshOverlayState()
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isOverlayActive) Color(0xFF882233) else NeonGreen
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("toggle_overlay_service_btn")
                        ) {
                            Text(
                                text = if (isOverlayActive) "STOP OVERLAY" else "START ZX OVERLAY",
                                color = if (isOverlayActive) Color.White else Color.Black,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }

                        Button(
                            onClick = onOpenSimulation,
                            colors = ButtonDefaults.buttonColors(containerColor = GamingSurfaceVariant),
                            border = androidx.compose.foundation.BorderStroke(1.dp, NeonCyan.copy(alpha = 0.5f)),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("test_in_game_sim_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Visibility,
                                contentDescription = "Test",
                                tint = NeonCyan,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "TEST HUD HUD",
                                color = NeonCyan,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Quick Actions 2x4 Grid
        Text(
            text = "QUICK ACTIONS",
            color = TextSecondary,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            QuickActionButton(
                icon = Icons.Default.GpsFixed,
                title = "Crosshair",
                isActive = crosshairConfig.isEnabled,
                onClick = { onNavigate("crosshair") },
                testTag = "quick_action_crosshair"
            )
            QuickActionButton(
                icon = Icons.Default.Tune,
                title = "Sensitivity",
                onClick = { onNavigate("sensitivity") },
                testTag = "quick_action_sensi"
            )
            QuickActionButton(
                icon = Icons.Default.Speed,
                title = "Performance",
                onClick = { onNavigate("performance") },
                testTag = "quick_action_performance"
            )
            QuickActionButton(
                icon = Icons.Default.Games,
                title = "Games",
                onClick = { onNavigate("games") },
                testTag = "quick_action_games"
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            QuickActionButton(
                icon = Icons.Default.Videocam,
                title = "Recording",
                onClick = { onNavigate("recording") },
                testTag = "quick_action_recording"
            )
            QuickActionButton(
                icon = Icons.Default.CameraAlt,
                title = "Screenshot",
                onClick = { onNavigate("recording") },
                testTag = "quick_action_screenshot"
            )
            QuickActionButton(
                icon = Icons.Outlined.Settings,
                title = "Settings",
                onClick = { onNavigate("settings") },
                testTag = "quick_action_settings"
            )
            QuickActionButton(
                icon = Icons.Default.Refresh,
                title = "Reset All",
                onClick = { viewModel.resetAllSettings() },
                testTag = "quick_action_reset"
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Sound & Brightness Controls
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = GamingPanelBg),
            shape = RoundedCornerShape(18.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, GamingBorderGlow.copy(alpha = 0.5f))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                // Brightness
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Default.WbSunny,
                        contentDescription = "Brightness",
                        tint = NeonCyan,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(text = "Brightness", color = TextPrimary, fontSize = 12.sp, modifier = Modifier.width(72.dp))
                    Slider(
                        value = brightness.toFloat(),
                        onValueChange = {
                            brightness = it.toInt()
                            viewModel.systemMonitor.setScreenBrightnessPercent(it.toInt())
                        },
                        valueRange = 0f..100f,
                        colors = SliderDefaults.colors(
                            thumbColor = NeonCyan,
                            activeTrackColor = NeonCyan,
                            inactiveTrackColor = SliderTrack
                        ),
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "$brightness%",
                        color = NeonCyan,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        modifier = Modifier.width(36.dp)
                    )
                }

                // Volume
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                        contentDescription = "Volume",
                        tint = NeonGreen,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(text = "Volume", color = TextPrimary, fontSize = 12.sp, modifier = Modifier.width(72.dp))
                    Slider(
                        value = musicVolume.toFloat(),
                        onValueChange = {
                            musicVolume = it.toInt()
                            viewModel.systemMonitor.setMusicVolumePercent(it.toInt())
                        },
                        valueRange = 0f..100f,
                        colors = SliderDefaults.colors(
                            thumbColor = NeonGreen,
                            activeTrackColor = NeonGreen,
                            inactiveTrackColor = SliderTrack
                        ),
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "$musicVolume%",
                        color = NeonGreen,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        modifier = Modifier.width(36.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun TelemetryDetailCard(
    icon: ImageVector,
    label: String,
    primaryValue: String,
    secondaryValue: String,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(GamingSurface)
            .border(1.dp, GamingBorderGlow.copy(alpha = 0.35f), RoundedCornerShape(16.dp))
            .padding(12.dp)
    ) {
        Column {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = label,
                    color = TextMuted,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = accentColor,
                    modifier = Modifier.size(16.dp)
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = primaryValue,
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Black,
                fontFamily = FontFamily.Monospace
            )
            Text(
                text = secondaryValue,
                color = accentColor,
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}
