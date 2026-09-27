package com.example.service

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.ZXGamingApplication
import com.example.data.model.CrosshairConfig
import com.example.data.model.CrosshairStyle
import com.example.ui.components.CrosshairRenderer
import com.example.ui.components.DpadPositionController
import com.example.ui.components.FuturisticHudGauge
import com.example.ui.components.GamingSliderWithSteps
import com.example.ui.components.QuickActionButton
import com.example.ui.theme.GamingBorderGlow
import com.example.ui.theme.GamingGlassBg
import com.example.ui.theme.GamingPanelBg
import com.example.ui.theme.GamingSurface
import com.example.ui.theme.GamingSurfaceVariant
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.NeonRed
import com.example.ui.theme.NeonWhite
import com.example.ui.theme.NeonYellow
import com.example.ui.theme.SliderActive
import com.example.ui.theme.SliderTrack
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlin.math.abs

class OverlayService : Service() {

    companion object {
        const val ACTION_START = "com.example.service.OVERLAY_START"
        const val ACTION_STOP = "com.example.service.OVERLAY_STOP"
        const val ACTION_UPDATE_CONFIG = "com.example.service.OVERLAY_UPDATE_CONFIG"

        private const val NOTIFICATION_CHANNEL_ID = "zx_overlay_channel"
        private const val NOTIFICATION_ID = 1002

        var isRunning: Boolean = false
            private set
    }

    private lateinit var windowManager: WindowManager
    private val serviceScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private val overlayLifecycleOwner = OverlayLifecycleOwner()

    private var floatingButtonView: ComposeView? = null
    private var floatingBtnParams: WindowManager.LayoutParams? = null

    private var hudOverlayView: ComposeView? = null
    private var hudOverlayParams: WindowManager.LayoutParams? = null

    private var crosshairView: ComposeView? = null
    private var crosshairParams: WindowManager.LayoutParams? = null

    private val isHudOpen = mutableStateOf(false)
    private val currentHudSubMenu = mutableStateOf<String?>(null) // null, "CROSSHAIR", "SENSI", "KEYMAP"

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        isRunning = true
        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        createNotificationChannel()
        // Critical: Satisfy ForegroundService startForeground contract immediately
        startForegroundWithNotification()

        overlayLifecycleOwner.onCreate()
        overlayLifecycleOwner.onStart()
        overlayLifecycleOwner.onResume()

        setupFloatingButton()
        setupCrosshairOverlay()
        setupHudOverlay()

        ZXGamingApplication.instance.systemMonitor.startMonitoring(serviceScope)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_STOP -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    stopForeground(STOP_FOREGROUND_REMOVE)
                } else {
                    @Suppress("DEPRECATION")
                    stopForeground(true)
                }
                stopSelf()
            }
            ACTION_START -> {
                startForegroundWithNotification()
            }
            ACTION_UPDATE_CONFIG -> {
                // Trigger recomposition/update on active overlays
            }
            else -> {
                startForegroundWithNotification()
            }
        }
        return START_NOT_STICKY
    }

    private fun startForegroundWithNotification() {
        val openIntent = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE
        )

        val stopIntent = PendingIntent.getService(
            this,
            1,
            Intent(this, OverlayService::class.java).apply { action = ACTION_STOP },
            PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setContentTitle("ZX Gaming Companion")
            .setContentText("HUD overlay and crosshair assistant active")
            .setSmallIcon(android.R.drawable.ic_menu_compass)
            .setContentIntent(openIntent)
            .addAction(android.R.drawable.ic_menu_close_clear_cancel, "Close Overlay", stopIntent)
            .setOngoing(true)
            .build()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(
                NOTIFICATION_ID,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
            )
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setupFloatingButton() {
        val app = ZXGamingApplication.instance
        val initialX = app.settingsRepository.floatingButtonX.value
        val initialY = app.settingsRepository.floatingButtonY.value

        val layoutFlag = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            @Suppress("DEPRECATION")
            WindowManager.LayoutParams.TYPE_PHONE
        }

        floatingBtnParams = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            layoutFlag,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = initialX
            y = initialY
        }

        floatingButtonView = ComposeView(this).apply {
            overlayLifecycleOwner.attachTo(this)
            setContent {
                MyApplicationTheme {
                    FloatingButtonComposable(
                        onTap = {
                            isHudOpen.value = !isHudOpen.value
                            updateHudVisibility()
                        }
                    )
                }
            }

            // Draggable touch listener
            setOnTouchListener(object : View.OnTouchListener {
                private var startX = 0
                private var startY = 0
                private var touchDownX = 0f
                private var touchDownY = 0f
                private var isDragging = false

                override fun onTouch(v: View?, event: MotionEvent): Boolean {
                    val params = floatingBtnParams ?: return false
                    when (event.action) {
                        MotionEvent.ACTION_DOWN -> {
                            startX = params.x
                            startY = params.y
                            touchDownX = event.rawX
                            touchDownY = event.rawY
                            isDragging = false
                            return false // allow Compose click handler to get down event
                        }
                        MotionEvent.ACTION_MOVE -> {
                            val dx = (event.rawX - touchDownX).toInt()
                            val dy = (event.rawY - touchDownY).toInt()
                            if (abs(dx) > 10 || abs(dy) > 10) {
                                isDragging = true
                                params.x = startX + dx
                                params.y = startY + dy
                                windowManager.updateViewLayout(floatingButtonView, params)
                            }
                            return isDragging
                        }
                        MotionEvent.ACTION_UP -> {
                            if (isDragging) {
                                app.settingsRepository.saveFloatingButtonPosition(params.x, params.y)
                                return true
                            }
                            return false
                        }
                    }
                    return false
                }
            })
        }

        windowManager.addView(floatingButtonView, floatingBtnParams)
    }

    private fun setupCrosshairOverlay() {
        val layoutFlag = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            @Suppress("DEPRECATION")
            WindowManager.LayoutParams.TYPE_PHONE
        }

        crosshairParams = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            layoutFlag,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE or
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            PixelFormat.TRANSLUCENT
        )

        crosshairView = ComposeView(this).apply {
            overlayLifecycleOwner.attachTo(this)
            setContent {
                MyApplicationTheme {
                    val config by ZXGamingApplication.instance.settingsRepository.crosshairConfig.collectAsState()
                    if (config.isEnabled) {
                        CrosshairRenderer(
                            config = config,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            }
        }

        windowManager.addView(crosshairView, crosshairParams)
    }

    private fun setupHudOverlay() {
        val layoutFlag = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            @Suppress("DEPRECATION")
            WindowManager.LayoutParams.TYPE_PHONE
        }

        hudOverlayParams = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            layoutFlag,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            PixelFormat.TRANSLUCENT
        )

        hudOverlayView = ComposeView(this).apply {
            overlayLifecycleOwner.attachTo(this)
            visibility = View.GONE
            setContent {
                MyApplicationTheme {
                    if (isHudOpen.value) {
                        HudDashboardComposable(
                            onClose = {
                                isHudOpen.value = false
                                currentHudSubMenu.value = null
                                updateHudVisibility()
                            },
                            currentSubMenu = currentHudSubMenu.value,
                            onSubMenuSelect = { currentHudSubMenu.value = it },
                            onLaunchApp = {
                                val intent = Intent(this@OverlayService, MainActivity::class.java).apply {
                                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                }
                                startActivity(intent)
                                isHudOpen.value = false
                                updateHudVisibility()
                            },
                            onExit = {
                                stopSelf()
                            }
                        )
                    }
                }
            }
        }

        windowManager.addView(hudOverlayView, hudOverlayParams)
    }

    private fun updateHudVisibility() {
        hudOverlayView?.visibility = if (isHudOpen.value) View.VISIBLE else View.GONE
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                NOTIFICATION_CHANNEL_ID,
                "ZX Gaming Overlay",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Shows status while floating overlay is running"
            }
            val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            nm.createNotificationChannel(channel)
        }
    }

    override fun onDestroy() {
        isRunning = false
        floatingButtonView?.let { windowManager.removeView(it) }
        crosshairView?.let { windowManager.removeView(it) }
        hudOverlayView?.let { windowManager.removeView(it) }
        overlayLifecycleOwner.onPause()
        overlayLifecycleOwner.onStop()
        overlayLifecycleOwner.onDestroy()
        serviceScope.cancel()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            stopForeground(STOP_FOREGROUND_REMOVE)
        } else {
            @Suppress("DEPRECATION")
            stopForeground(true)
        }
        super.onDestroy()
    }
}

@Composable
fun FloatingButtonComposable(onTap: () -> Unit) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(if (isPressed) 0.88f else 1.0f, label = "btn_scale")

    Box(
        modifier = Modifier
            .scale(scale)
            .size(56.dp)
            .clip(CircleShape)
            .background(GamingPanelBg)
            .border(2.dp, NeonGreen, CircleShape)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onTap
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
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
}

@Composable
fun HudDashboardComposable(
    onClose: () -> Unit,
    currentSubMenu: String?,
    onSubMenuSelect: (String?) -> Unit,
    onLaunchApp: () -> Unit,
    onExit: () -> Unit
) {
    val app = ZXGamingApplication.instance
    val telemetry by app.systemMonitor.telemetry.collectAsState()
    var crosshairConfig by remember { mutableStateOf(app.settingsRepository.crosshairConfig.value) }
    var sensiConfig by remember { mutableStateOf(app.settingsRepository.sensitivityConfig.value) }

    var musicVolume by remember { mutableIntStateOf(app.systemMonitor.getMusicVolumePercent()) }
    var brightness by remember { mutableIntStateOf(app.systemMonitor.getScreenBrightnessPercent()) }

    // Fullscreen semi-transparent backdrop
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.45f))
            .clickable(onClick = onClose),
        contentAlignment = Alignment.Center
    ) {
        // Main Centered HUD Box (prevents closing on internal click)
        Box(
            modifier = Modifier
                .wrapContentSize()
                .clickable(enabled = false) {}
                .padding(16.dp)
        ) {
            if (currentSubMenu == null) {
                // Main Gaming Panel
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .clip(RoundedCornerShape(24.dp))
                        .background(GamingPanelBg)
                        .border(1.dp, GamingBorderGlow, RoundedCornerShape(24.dp))
                        .padding(horizontal = 20.dp, vertical = 16.dp)
                ) {
                    // Header Bar: Time & Close
                    Row(
                        modifier = Modifier.fillMaxWidth(0.85f),
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
                                .clickable(onClick = onClose)
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

                    // Center Curved HUD Display (RAM - FPS - CPU)
                    FuturisticHudGauge(
                        ramPercent = telemetry.ramPercent,
                        cpuPercent = telemetry.cpuUsagePercent,
                        fps = telemetry.fps,
                        refreshRateHz = telemetry.refreshRateHz
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Quick Actions Row (2x4 or two rows)
                    Row(
                        modifier = Modifier.fillMaxWidth(0.85f),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Left group
                        Column {
                            Row {
                                QuickActionButton(
                                    icon = Icons.Outlined.TouchApp,
                                    title = "Edit keymap",
                                    onClick = { onSubMenuSelect("KEYMAP") }
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                QuickActionButton(
                                    icon = Icons.Outlined.Settings,
                                    title = "Pengaturan Umum",
                                    onClick = { onSubMenuSelect("GENERAL") }
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Row {
                                QuickActionButton(
                                    icon = Icons.Default.Tune,
                                    title = "Pengaturan Sensitivity",
                                    onClick = { onSubMenuSelect("SENSI") }
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                QuickActionButton(
                                    icon = Icons.Outlined.Dashboard,
                                    title = "Skala Resolusi",
                                    onClick = { onSubMenuSelect("RESOLUTION") }
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
                                    onClick = { onSubMenuSelect("CROSSHAIR") }
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                QuickActionButton(
                                    icon = Icons.Default.Speed,
                                    title = "Monitoring Info",
                                    onClick = { onSubMenuSelect("MONITORING") }
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Row {
                                QuickActionButton(
                                    icon = Icons.Default.Home,
                                    title = "Kembali ke Aplikasi",
                                    onClick = onLaunchApp
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                QuickActionButton(
                                    icon = Icons.Default.PowerSettingsNew,
                                    title = "Matikan Aplikasi",
                                    onClick = onExit
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Bottom Sliders: Brightness & Volume (Matching Video at 00:03)
                    Row(
                        modifier = Modifier.fillMaxWidth(0.85f),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Brightness slider
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
                                    app.systemMonitor.setScreenBrightnessPercent(it.toInt())
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

                        // Volume slider
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
                                    app.systemMonitor.setMusicVolumePercent(it.toInt())
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
            } else if (currentSubMenu == "CROSSHAIR") {
                // Crosshair Assistant Panel
                OverlayCrosshairSubPanel(
                    config = crosshairConfig,
                    onConfigChange = {
                        crosshairConfig = it
                        app.settingsRepository.updateCrosshairConfig(it)
                    },
                    onBack = { onSubMenuSelect(null) }
                )
            } else if (currentSubMenu == "SENSI") {
                // Sensitivity Sub Panel
                OverlaySensiSubPanel(
                    config = sensiConfig,
                    onConfigChange = {
                        sensiConfig = it
                        app.settingsRepository.updateSensitivityConfig(it)
                    },
                    onBack = { onSubMenuSelect(null) }
                )
            } else if (currentSubMenu == "KEYMAP") {
                // Keymap / Overlay Indicator Alpha panel
                OverlayIndicatorSubPanel(
                    onBack = { onSubMenuSelect(null) }
                )
            } else if (currentSubMenu == "GENERAL") {
                OverlayGeneralSubPanel(
                    onBack = { onSubMenuSelect(null) }
                )
            } else if (currentSubMenu == "RESOLUTION") {
                OverlayResolutionSubPanel(
                    onBack = { onSubMenuSelect(null) }
                )
            } else if (currentSubMenu == "MONITORING") {
                OverlayMonitoringSubPanel(
                    onBack = { onSubMenuSelect(null) }
                )
            } else {
                OverlayIndicatorSubPanel(
                    onBack = { onSubMenuSelect(null) }
                )
            }
        }
    }
}

@Composable
fun OverlayCrosshairSubPanel(
    config: CrosshairConfig,
    onConfigChange: (CrosshairConfig) -> Unit,
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .width(420.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(GamingPanelBg)
            .border(1.dp, GamingBorderGlow, RoundedCornerShape(24.dp))
            .padding(20.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Crosshair Assist",
                    color = Color.White,
                    fontWeight = FontWeight.Black,
                    fontSize = 16.sp
                )
                Text(
                    text = "Custom style, posisi, dan ukuran",
                    color = TextMuted,
                    fontSize = 11.sp
                )
            }
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (config.isEnabled) NeonGreen else GamingSurfaceVariant)
                    .clickable { onConfigChange(config.copy(isEnabled = !config.isEnabled)) }
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text(
                    text = if (config.isEnabled) "ACTIVE" else "ENABLE",
                    color = if (config.isEnabled) Color.Black else TextPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Position & D-pad
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(text = "POSITION", color = TextSecondary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(6.dp))
                Row {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(GamingSurfaceVariant)
                            .clickable { onConfigChange(config.copy(offsetX = 0)) }
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(text = "RESET X", color = NeonGreen, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(GamingSurfaceVariant)
                            .clickable { onConfigChange(config.copy(offsetY = 0)) }
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(text = "RESET Y", color = NeonGreen, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
            DpadPositionController(
                offsetX = config.offsetX,
                offsetY = config.offsetY,
                onStepChange = { dx, dy ->
                    onConfigChange(
                        config.copy(
                            offsetX = (config.offsetX + dx * 2).coerceIn(-500, 500),
                            offsetY = (config.offsetY + dy * 2).coerceIn(-500, 500)
                        )
                    )
                }
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Style Selector
        Text(text = "STYLE", color = TextSecondary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(6.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            CrosshairStyle.entries.forEach { style ->
                val isSelected = config.style == style
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(42.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isSelected) NeonGreen.copy(alpha = 0.2f) else GamingSurfaceVariant)
                        .border(1.dp, if (isSelected) NeonGreen else Color.Transparent, RoundedCornerShape(8.dp))
                        .clickable { onConfigChange(config.copy(style = style)) }
                ) {
                    CrosshairRenderer(
                        config = config.copy(
                            style = style,
                            offsetX = 0,
                            offsetY = 0,
                            sizePercent = 60
                        ),
                        modifier = Modifier.size(32.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Color Choices
        Text(text = "COLOR", color = TextSecondary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(6.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            val colors = listOf("#00FF88", "#00E5FF", "#FFE600", "#FF3366", "#FFFFFF")
            colors.forEach { hex ->
                val isSelected = config.colorHex.equals(hex, ignoreCase = true)
                Box(
                    modifier = Modifier
                        .size(30.dp)
                        .clip(CircleShape)
                        .background(Color(android.graphics.Color.parseColor(hex)))
                        .border(
                            2.dp,
                            if (isSelected) Color.White else Color.Transparent,
                            CircleShape
                        )
                        .clickable { onConfigChange(config.copy(colorHex = hex)) }
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Size Slider
        GamingSliderWithSteps(
            label = "SIZE CROSSHAIR",
            value = config.sizePercent.toFloat(),
            min = 10f,
            max = 100f,
            step = 5f,
            formatString = "%.0f%%",
            onValueChange = { onConfigChange(config.copy(sizePercent = it.toInt())) },
            onReset = { onConfigChange(config.copy(sizePercent = 40)) }
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Close / Back button
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(GamingSurfaceVariant)
                    .clickable(onClick = onBack)
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text(text = "KEMBALI", color = NeonGreen, fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
        }
    }
}

@Composable
fun OverlaySensiSubPanel(
    config: com.example.data.model.SensitivityConfig,
    onConfigChange: (com.example.data.model.SensitivityConfig) -> Unit,
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .width(420.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(GamingPanelBg)
            .border(1.dp, GamingBorderGlow, RoundedCornerShape(24.dp))
            .padding(20.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Pengaturan Sensi1",
                    color = Color.White,
                    fontWeight = FontWeight.Black,
                    fontSize = 16.sp
                )
                Text(
                    text = "Mengatur sensitivity x y koordinat sentuhan",
                    color = TextMuted,
                    fontSize = 11.sp
                )
            }
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (config.globalEnabled) NeonGreen else GamingSurfaceVariant)
                    .clickable { onConfigChange(config.copy(globalEnabled = !config.globalEnabled)) }
                    .padding(horizontal = 10.dp, vertical = 5.dp)
            ) {
                Text(
                    text = if (config.globalEnabled) "ON" else "OFF",
                    color = if (config.globalEnabled) Color.Black else TextPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Sensitivity Y Slider
        GamingSliderWithSteps(
            label = "Sensitivity Y:",
            value = config.sensitivityY,
            min = 1.00f,
            max = 5.00f,
            step = 0.05f,
            formatString = "%.2f",
            onValueChange = { onConfigChange(config.copy(sensitivityY = it)) },
            onReset = { onConfigChange(config.copy(sensitivityY = 1.20f)) }
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Sensitivity X Slider
        GamingSliderWithSteps(
            label = "Sensitivity X:",
            value = config.sensitivityX,
            min = 1.00f,
            max = 5.00f,
            step = 0.05f,
            formatString = "%.2f",
            onValueChange = { onConfigChange(config.copy(sensitivityX = it)) },
            onReset = { onConfigChange(config.copy(sensitivityX = 1.00f)) }
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Akselerasi Sentuhan Toggle
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "Akselerasi Sentuhan", color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(if (config.touchAcceleration) NeonGreen else GamingSurfaceVariant)
                    .clickable { onConfigChange(config.copy(touchAcceleration = !config.touchAcceleration)) }
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Text(
                    text = if (config.touchAcceleration) "ENABLED" else "DISABLED",
                    color = if (config.touchAcceleration) Color.Black else TextMuted,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(GamingSurfaceVariant)
                    .clickable(onClick = onBack)
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text(text = "SIMPAN", color = NeonGreen, fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
        }
    }
}

@Composable
fun OverlayIndicatorSubPanel(onBack: () -> Unit) {
    val app = ZXGamingApplication.instance
    var alphaVal by remember { mutableFloatStateOf(app.settingsRepository.panelAlpha.value * 100f) }

    Column(
        modifier = Modifier
            .width(420.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(GamingPanelBg)
            .border(1.dp, GamingBorderGlow, RoundedCornerShape(24.dp))
            .padding(20.dp)
    ) {
        Text(
            text = "Pengaturan key map",
            color = Color.White,
            fontWeight = FontWeight.Black,
            fontSize = 16.sp
        )
        Text(
            text = "Mengatur opacity dan warna key map yang akan tampil di layar game Anda.",
            color = TextMuted,
            fontSize = 11.sp
        )

        Spacer(modifier = Modifier.height(16.dp))

        GamingSliderWithSteps(
            label = "Alpha Indikator",
            value = alphaVal,
            min = 20f,
            max = 100f,
            step = 5f,
            formatString = "%.1f",
            onValueChange = {
                alphaVal = it
                app.settingsRepository.setPanelAlpha(it / 100f)
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(GamingSurfaceVariant)
                    .clickable(onClick = onBack)
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text(text = "SIMPAN", color = NeonGreen, fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
        }
    }
}

@Composable
fun OverlayGeneralSubPanel(onBack: () -> Unit) {
    val app = ZXGamingApplication.instance
    var enableBlur by remember { mutableStateOf(app.settingsRepository.enableBlur.value) }
    var miniMonitoring by remember { mutableStateOf(app.settingsRepository.showMiniMonitoring.value) }
    var currentTier by remember { mutableStateOf(app.settingsRepository.performanceTier.value) }
    var panelAlpha by remember { mutableFloatStateOf(app.settingsRepository.panelAlpha.value * 100f) }

    Column(
        modifier = Modifier
            .width(420.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(GamingPanelBg)
            .border(1.dp, GamingBorderGlow, RoundedCornerShape(24.dp))
            .padding(20.dp)
    ) {
        Text(
            text = "Pengaturan Umum",
            color = Color.White,
            fontWeight = FontWeight.Black,
            fontSize = 16.sp
        )
        Text(
            text = "Konfigurasi performa, visual effect, dan monitoring overlay",
            color = TextMuted,
            fontSize = 11.sp
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Performance Mode Selector
        Text(text = "MODE PERFORMA", color = TextSecondary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(6.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .background(GamingSurfaceVariant)
                .padding(4.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            com.example.data.model.PerformanceTier.entries.forEach { tier ->
                val isSelected = currentTier == tier
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isSelected) NeonGreen else Color.Transparent)
                        .clickable {
                            currentTier = tier
                            app.settingsRepository.updatePerformanceTier(tier)
                        }
                        .padding(vertical = 6.dp)
                ) {
                    Text(
                        text = tier.label,
                        color = if (isSelected) Color.Black else TextSecondary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Blur toggle
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "Glassmorphism Blur", color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(if (enableBlur) NeonGreen else GamingSurfaceVariant)
                    .clickable {
                        enableBlur = !enableBlur
                        app.settingsRepository.setEnableBlur(enableBlur)
                    }
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Text(
                    text = if (enableBlur) "ON" else "OFF",
                    color = if (enableBlur) Color.Black else TextMuted,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Mini Monitoring Floating Pill toggle
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "Mini Monitoring Bar", color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(if (miniMonitoring) NeonGreen else GamingSurfaceVariant)
                    .clickable {
                        miniMonitoring = !miniMonitoring
                        app.settingsRepository.setShowMiniMonitoring(miniMonitoring)
                        val intent = Intent(app, RealtimeMetricsOverlayService::class.java).apply {
                            action = if (miniMonitoring) RealtimeMetricsOverlayService.ACTION_START else RealtimeMetricsOverlayService.ACTION_STOP
                        }
                        try {
                            if (miniMonitoring) {
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                    app.startForegroundService(intent)
                                } else {
                                    app.startService(intent)
                                }
                            } else {
                                app.stopService(intent)
                            }
                        } catch (_: Exception) {}
                    }
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Text(
                    text = if (miniMonitoring) "ACTIVE" else "OFF",
                    color = if (miniMonitoring) Color.Black else TextMuted,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        GamingSliderWithSteps(
            label = "Transparansi Panel",
            value = panelAlpha,
            min = 30f,
            max = 100f,
            step = 5f,
            formatString = "%.0f%%",
            onValueChange = {
                panelAlpha = it
                app.settingsRepository.setPanelAlpha(it / 100f)
            }
        )

        Spacer(modifier = Modifier.height(14.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(GamingSurfaceVariant)
                    .clickable(onClick = onBack)
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text(text = "SIMPAN", color = NeonGreen, fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
        }
    }
}

@Composable
fun OverlayResolutionSubPanel(onBack: () -> Unit) {
    val app = ZXGamingApplication.instance
    var selectedScale by remember { mutableStateOf("100% (Native)") }
    var selectedFps by remember { mutableStateOf("60 FPS") }
    var graphicsQuality by remember { mutableStateOf(app.settingsRepository.graphicsQuality.value) }

    Column(
        modifier = Modifier
            .width(420.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(GamingPanelBg)
            .border(1.dp, GamingBorderGlow, RoundedCornerShape(24.dp))
            .padding(20.dp)
    ) {
        Text(
            text = "Skala Resolusi & Display",
            color = Color.White,
            fontWeight = FontWeight.Black,
            fontSize = 16.sp
        )
        Text(
            text = "Optimalisasi resolusi render internal dan refresh rate layar",
            color = TextMuted,
            fontSize = 11.sp
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Resolution Scale
        Text(text = "SKALA RESOLUSI RENDER", color = TextSecondary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(6.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .background(GamingSurfaceVariant)
                .padding(4.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            listOf("50% (720p)", "75% (900p)", "100% (Native)").forEach { scale ->
                val isSelected = selectedScale == scale
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isSelected) NeonGreen else Color.Transparent)
                        .clickable { selectedScale = scale }
                        .padding(vertical = 6.dp)
                ) {
                    Text(
                        text = scale,
                        color = if (isSelected) Color.Black else TextSecondary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Target FPS Cap
        Text(text = "TARGET FRAME RATE", color = TextSecondary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(6.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .background(GamingSurfaceVariant)
                .padding(4.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            listOf("60 FPS", "90 FPS", "120 FPS").forEach { fps ->
                val isSelected = selectedFps == fps
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isSelected) NeonCyan else Color.Transparent)
                        .clickable { selectedFps = fps }
                        .padding(vertical = 6.dp)
                ) {
                    Text(
                        text = fps,
                        color = if (isSelected) Color.Black else TextSecondary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Quality Preset
        Text(text = "GRAPHICS QUALITY TIER", color = TextSecondary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(6.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .background(GamingSurfaceVariant)
                .padding(4.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            listOf("LOW", "MEDIUM", "HIGH").forEach { tier ->
                val isSelected = graphicsQuality.equals(tier, ignoreCase = true)
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isSelected) NeonGreen else Color.Transparent)
                        .clickable {
                            graphicsQuality = tier
                            app.settingsRepository.setGraphicsQuality(tier)
                        }
                        .padding(vertical = 6.dp)
                ) {
                    Text(
                        text = tier,
                        color = if (isSelected) Color.Black else TextSecondary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(GamingSurfaceVariant)
                    .clickable(onClick = onBack)
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text(text = "TERAPKAN", color = NeonGreen, fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
        }
    }
}

@Composable
fun OverlayMonitoringSubPanel(onBack: () -> Unit) {
    val app = ZXGamingApplication.instance
    val telemetry by app.systemMonitor.telemetry.collectAsState()

    Column(
        modifier = Modifier
            .width(420.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(GamingPanelBg)
            .border(1.dp, GamingBorderGlow, RoundedCornerShape(24.dp))
            .padding(20.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Hardware Telemetri Detail",
                    color = Color.White,
                    fontWeight = FontWeight.Black,
                    fontSize = 16.sp
                )
                Text(
                    text = "Live kernel & hardware diagnostics",
                    color = TextMuted,
                    fontSize = 11.sp
                )
            }
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(NeonGreen.copy(alpha = 0.2f))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "LIVE",
                    color = NeonGreen,
                    fontWeight = FontWeight.Bold,
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Telemetry stats grid
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(12.dp))
                    .background(GamingSurfaceVariant)
                    .padding(10.dp)
            ) {
                Column {
                    Text(text = "RAM USAGE", color = TextMuted, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${String.format("%.1f", telemetry.ramUsedGb)} / ${String.format("%.1f", telemetry.ramTotalGb)} GB",
                        color = Color.White,
                        fontWeight = FontWeight.Black,
                        fontSize = 13.sp,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(text = "${telemetry.ramPercent}% Allocated", color = NeonGreen, fontSize = 10.sp)
                }
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(12.dp))
                    .background(GamingSurfaceVariant)
                    .padding(10.dp)
            ) {
                Column {
                    Text(text = "CPU UTILIZATION", color = TextMuted, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (telemetry.cpuUsagePercent >= 0) "${telemetry.cpuUsagePercent}%" else "Unavailable",
                        color = Color.White,
                        fontWeight = FontWeight.Black,
                        fontSize = 13.sp,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(text = "Multi-Core Load", color = NeonCyan, fontSize = 10.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(12.dp))
                    .background(GamingSurfaceVariant)
                    .padding(10.dp)
            ) {
                Column {
                    Text(text = "BATTERY & THERMAL", color = TextMuted, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${telemetry.batteryPercent}% (${if (telemetry.batteryTemperatureC > 0) "${String.format("%.1f", telemetry.batteryTemperatureC)}°C" else "N/A"})",
                        color = Color.White,
                        fontWeight = FontWeight.Black,
                        fontSize = 13.sp,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(text = telemetry.batteryStatus, color = NeonGreen, fontSize = 10.sp)
                }
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(12.dp))
                    .background(GamingSurfaceVariant)
                    .padding(10.dp)
            ) {
                Column {
                    Text(text = "DISPLAY PACING", color = TextMuted, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${telemetry.fps} FPS / ${telemetry.refreshRateHz.toInt()}Hz",
                        color = Color.White,
                        fontWeight = FontWeight.Black,
                        fontSize = 13.sp,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(text = "Frame Pacing Stable", color = NeonCyan, fontSize = 10.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(GamingSurfaceVariant)
                    .clickable(onClick = onBack)
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text(text = "TUTUP", color = NeonGreen, fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
        }
    }
}
