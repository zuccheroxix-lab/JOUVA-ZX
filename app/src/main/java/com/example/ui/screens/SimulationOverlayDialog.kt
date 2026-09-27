package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.GpsFixed
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material.icons.outlined.Dashboard
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.TouchApp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.service.OverlayCrosshairSubPanel
import com.example.service.OverlayGeneralSubPanel
import com.example.service.OverlayIndicatorSubPanel
import com.example.service.OverlayMonitoringSubPanel
import com.example.service.OverlayResolutionSubPanel
import com.example.service.OverlaySensiSubPanel
import com.example.ui.components.CrosshairRenderer
import com.example.ui.components.FuturisticHudGauge
import com.example.ui.components.MetricsOverlayMode
import com.example.ui.components.QuickActionButton
import com.example.ui.components.RealtimeMetricsOverlayComponent
import com.example.ui.theme.GamingBorderGlow
import com.example.ui.theme.GamingPanelBg
import com.example.ui.theme.GamingSurfaceVariant
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.SliderTrack
import com.example.ui.theme.TextMuted
import com.example.ui.viewmodel.CompanionViewModel
import kotlin.math.roundToInt

@Composable
fun SimulationOverlayScreen(
    viewModel: CompanionViewModel,
    onCloseSimulation: () -> Unit
) {
    val telemetry by viewModel.telemetry.collectAsState()
    val crosshairConfig by viewModel.crosshairConfig.collectAsState()
    val sensiConfig by viewModel.sensitivityConfig.collectAsState()

    var isHudOpen by remember { mutableStateOf(false) }
    var activeSubMenu by remember { mutableStateOf<String?>(null) } // null, "CROSSHAIR", "SENSI", "KEYMAP"

    var showMetricsOverlayInSim by remember { mutableStateOf(true) }
    var metricsOffsetX by remember { mutableFloatStateOf(60f) }
    var metricsOffsetY by remember { mutableFloatStateOf(80f) }

    var buttonOffsetX by remember { mutableFloatStateOf(40f) }
    var buttonOffsetY by remember { mutableFloatStateOf(160f) }

    var musicVolume by remember { mutableIntStateOf(viewModel.systemMonitor.getMusicVolumePercent()) }
    var brightness by remember { mutableIntStateOf(viewModel.systemMonitor.getScreenBrightnessPercent()) }

    // Dynamic Game Landscape simulation
    val infiniteTransition = rememberInfiniteTransition(label = "game_sim")
    val pulse by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0D1117))
    ) {
        val maxWidthPx = constraints.maxWidth.toFloat()
        val maxHeightPx = constraints.maxHeight.toFloat()

        // 1. Simulated Action Game Backdrop (3D Arena with Cyber Grid & Targets)
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height

            // Sky gradient
            drawRect(
                brush = Brush.verticalGradient(
                    colors = listOf(Color(0xFF0F1A2A), Color(0xFF1B2E3D), Color(0xFF0A1218))
                )
            )

            // Perspective Grid Floor
            val horizonY = height * 0.55f
            val floorPath = Path().apply {
                moveTo(0f, horizonY)
                lineTo(width, horizonY)
                lineTo(width, height)
                lineTo(0f, height)
                close()
            }
            drawPath(
                floorPath,
                brush = Brush.verticalGradient(
                    colors = listOf(Color(0xFF051016), Color(0xFF0D252D))
                )
            )

            // Perspective lines
            for (i in 0..10) {
                val bottomX = (width / 10f) * i
                val topX = (width / 2f) + (bottomX - width / 2f) * 0.15f
                drawLine(
                    color = NeonCyan.copy(alpha = 0.25f),
                    start = Offset(topX, horizonY),
                    end = Offset(bottomX, height),
                    strokeWidth = 1.5f
                )
            }

            // Distant sci-fi structures
            drawRect(
                color = Color(0xFF081820),
                topLeft = Offset(width * 0.15f, horizonY - 120f),
                size = androidx.compose.ui.geometry.Size(width * 0.2f, 120f)
            )
            drawRect(
                color = Color(0xFF0A222B),
                topLeft = Offset(width * 0.45f, horizonY - 180f),
                size = androidx.compose.ui.geometry.Size(width * 0.25f, 180f)
            )

            // Simulated Enemy Target
            val targetCenter = Offset(width * 0.5f, horizonY + 60f)
            drawCircle(
                color = Color.Red.copy(alpha = 0.35f * pulse),
                radius = 24.dp.toPx() * pulse,
                center = targetCenter
            )
            drawCircle(
                color = Color.Red,
                radius = 6.dp.toPx(),
                center = targetCenter
            )
        }

        // 2. Active Crosshair Layer (if enabled)
        if (crosshairConfig.isEnabled) {
            CrosshairRenderer(
                config = crosshairConfig,
                modifier = Modifier.fillMaxSize()
            )
        }

        // Top Game Simulation Info Overlay
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 36.dp, start = 16.dp, end = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color.Black.copy(alpha = 0.6f))
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Text(
                    text = "IN-GAME SIMULATION MODE",
                    color = NeonCyan,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = { showMetricsOverlayInSim = !showMetricsOverlayInSim },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (showMetricsOverlayInSim) NeonGreen.copy(alpha = 0.2f) else Color.Black.copy(alpha = 0.7f)
                    ),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (showMetricsOverlayInSim) NeonGreen else TextMuted
                    ),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.height(32.dp).testTag("sim_toggle_metrics_btn")
                ) {
                    Text(
                        text = if (showMetricsOverlayInSim) "METRICS HUD: ON" else "SHOW METRICS HUD",
                        color = if (showMetricsOverlayInSim) NeonGreen else TextMuted,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Button(
                    onClick = onCloseSimulation,
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Black.copy(alpha = 0.7f)),
                    border = androidx.compose.foundation.BorderStroke(1.dp, NeonGreen.copy(alpha = 0.4f)),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.height(32.dp)
                ) {
                    Text(text = "EXIT GAME", color = NeonGreen, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        // Real-Time Metrics Overlay Component (Draggable over simulated game)
        if (showMetricsOverlayInSim) {
            Box(
                modifier = Modifier
                    .offset { IntOffset(metricsOffsetX.roundToInt(), metricsOffsetY.roundToInt()) }
            ) {
                RealtimeMetricsOverlayComponent(
                    telemetry = telemetry,
                    initialMode = MetricsOverlayMode.COMPACT_PILL,
                    onClose = { showMetricsOverlayInSim = false },
                    onDragDelta = { dx, dy ->
                        metricsOffsetX = (metricsOffsetX + dx).coerceIn(0f, maxWidthPx - 180f)
                        metricsOffsetY = (metricsOffsetY + dy).coerceIn(40f, maxHeightPx - 100f)
                    }
                )
            }
        }

        // 3. Movable Floating "ZX" Button (matching video)
        Box(
            modifier = Modifier
                .offset { IntOffset(buttonOffsetX.roundToInt(), buttonOffsetY.roundToInt()) }
                .pointerInput(Unit) {
                    detectDragGestures { change, dragAmount ->
                        change.consume()
                        buttonOffsetX = (buttonOffsetX + dragAmount.x).coerceIn(0f, maxWidthPx - 160f)
                        buttonOffsetY = (buttonOffsetY + dragAmount.y).coerceIn(60f, maxHeightPx - 160f)
                    }
                }
        ) {
            val interactionSource = remember { MutableInteractionSource() }
            val isPressed by interactionSource.collectIsPressedAsState()
            val scale by animateFloatAsState(if (isPressed) 0.88f else 1.0f, label = "btn_scale")

            Box(
                modifier = Modifier
                    .scale(scale)
                    .size(54.dp)
                    .clip(CircleShape)
                    .background(GamingPanelBg)
                    .border(2.dp, NeonGreen, CircleShape)
                    .clickable(
                        interactionSource = interactionSource,
                        indication = null,
                        onClick = { isHudOpen = !isHudOpen }
                    )
                    .testTag("simulation_floating_zx_btn"),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "ZX",
                    color = NeonGreen,
                    fontWeight = FontWeight.Black,
                    fontSize = 18.sp,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 1.sp
                )
            }
        }

        // 4. Centered Translucent Gaming HUD Panel Overlay (matching video 00:02)
        AnimatedVisibility(
            visible = isHudOpen,
            enter = fadeIn(tween(200)) + scaleIn(tween(200), initialScale = 0.9f),
            exit = fadeOut(tween(150)) + scaleOut(tween(150), targetScale = 0.9f)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.5f))
                    .clickable {
                        isHudOpen = false
                        activeSubMenu = null
                    },
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .wrapContentSize()
                        .clickable(enabled = false) {}
                        .padding(16.dp)
                ) {
                    if (activeSubMenu == null) {
                        // Main Gaming HUD Panel
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .clip(RoundedCornerShape(24.dp))
                                .background(GamingPanelBg.copy(alpha = 0.92f))
                                .border(1.dp, GamingBorderGlow, RoundedCornerShape(24.dp))
                                .padding(horizontal = 20.dp, vertical = 16.dp)
                        ) {
                            // Header Bar: Time & Close
                            Row(
                                modifier = Modifier.fillMaxWidth(0.9f),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = telemetry.currentTimeString,
                                    color = NeonGreen,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                )
                                Text(
                                    text = "ZX GAMING COMPANION",
                                    color = Color.White,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = 1.sp
                                )
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier
                                        .size(28.dp)
                                        .clip(CircleShape)
                                        .background(GamingSurfaceVariant)
                                        .clickable { isHudOpen = false }
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Close",
                                        tint = NeonGreen,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // Futuristic Curve HUD Display (RAM - 60 FPS - CPU)
                            FuturisticHudGauge(
                                ramPercent = telemetry.ramPercent,
                                cpuPercent = telemetry.cpuUsagePercent,
                                fps = telemetry.fps,
                                refreshRateHz = telemetry.refreshRateHz
                            )

                            Spacer(modifier = Modifier.height(14.dp))

                            // Action buttons
                            Row(
                                modifier = Modifier.fillMaxWidth(0.9f),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                // Left group
                                Column {
                                    Row {
                                        QuickActionButton(
                                            icon = Icons.Outlined.TouchApp,
                                            title = "Edit keymap",
                                            onClick = { activeSubMenu = "KEYMAP" }
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        QuickActionButton(
                                            icon = Icons.Outlined.Settings,
                                            title = "Pengaturan Umum",
                                            onClick = { activeSubMenu = "GENERAL" }
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Row {
                                        QuickActionButton(
                                            icon = Icons.Default.Tune,
                                            title = "Pengaturan Sensitivity",
                                            onClick = { activeSubMenu = "SENSI" }
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        QuickActionButton(
                                            icon = Icons.Outlined.Dashboard,
                                            title = "Skala Resolusi",
                                            onClick = { activeSubMenu = "RESOLUTION" }
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.width(16.dp))

                                // Right group
                                Column {
                                    Row {
                                        QuickActionButton(
                                            icon = Icons.Default.GpsFixed,
                                            title = "Crosshair Assistant",
                                            isActive = crosshairConfig.isEnabled,
                                            onClick = { activeSubMenu = "CROSSHAIR" }
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        QuickActionButton(
                                            icon = Icons.Default.Speed,
                                            title = "Monitoring Info",
                                            onClick = { activeSubMenu = "MONITORING" }
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Row {
                                        QuickActionButton(
                                            icon = Icons.Default.Home,
                                            title = "Kembali ke Aplikasi",
                                            onClick = onCloseSimulation
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        QuickActionButton(
                                            icon = Icons.Default.PowerSettingsNew,
                                            title = "Matikan Aplikasi",
                                            onClick = onCloseSimulation
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            // Bottom Sliders: Brightness & Volume
                            Row(
                                modifier = Modifier.fillMaxWidth(0.9f),
                                horizontalArrangement = Arrangement.spacedBy(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.WbSunny,
                                        contentDescription = "Brightness",
                                        tint = NeonCyan,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
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
                                }

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                                        contentDescription = "Volume",
                                        tint = NeonGreen,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
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
                                }
                            }
                        }
                    } else if (activeSubMenu == "CROSSHAIR") {
                        OverlayCrosshairSubPanel(
                            config = crosshairConfig,
                            onConfigChange = { viewModel.updateCrosshair(it) },
                            onBack = { activeSubMenu = null }
                        )
                    } else if (activeSubMenu == "SENSI") {
                        OverlaySensiSubPanel(
                            config = sensiConfig,
                            onConfigChange = { viewModel.updateSensitivity(it) },
                            onBack = { activeSubMenu = null }
                        )
                    } else if (activeSubMenu == "KEYMAP") {
                        OverlayIndicatorSubPanel(
                            onBack = { activeSubMenu = null }
                        )
                    } else if (activeSubMenu == "GENERAL") {
                        OverlayGeneralSubPanel(
                            onBack = { activeSubMenu = null }
                        )
                    } else if (activeSubMenu == "RESOLUTION") {
                        OverlayResolutionSubPanel(
                            onBack = { activeSubMenu = null }
                        )
                    } else if (activeSubMenu == "MONITORING") {
                        OverlayMonitoringSubPanel(
                            onBack = { activeSubMenu = null }
                        )
                    } else {
                        OverlayIndicatorSubPanel(
                            onBack = { activeSubMenu = null }
                        )
                    }
                }
            }
        }
    }
}
