package com.example.service

import android.annotation.SuppressLint
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
import android.provider.Settings
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.ComposeView
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.ZXGamingApplication
import com.example.ui.components.MetricsOverlayMode
import com.example.ui.components.RealtimeMetricsOverlayComponent
import com.example.ui.theme.MyApplicationTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.abs

/**
 * Foreground Service that hosts the Compose-based real-time FPS and CPU/GPU usage metrics overlay.
 * Conforms to modern Android foreground service policies with specialUse FGS type.
 */
class RealtimeMetricsOverlayService : Service() {

    companion object {
        const val ACTION_START = "com.example.service.METRICS_OVERLAY_START"
        const val ACTION_STOP = "com.example.service.METRICS_OVERLAY_STOP"
        const val ACTION_TOGGLE_EXPANDED = "com.example.service.METRICS_OVERLAY_TOGGLE_EXPANDED"

        private const val NOTIFICATION_CHANNEL_ID = "zx_metrics_overlay_channel"
        private const val NOTIFICATION_ID = 2002

        private val _isRunningFlow = MutableStateFlow(false)
        val isRunningFlow: StateFlow<Boolean> = _isRunningFlow.asStateFlow()

        val isRunning: Boolean
            get() = _isRunningFlow.value
    }

    private lateinit var windowManager: WindowManager
    private val serviceScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private val overlayLifecycleOwner = OverlayLifecycleOwner()

    private var metricsView: ComposeView? = null
    private var windowParams: WindowManager.LayoutParams? = null
    private var notificationUpdateJob: Job? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        _isRunningFlow.value = true
        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        createNotificationChannel()

        // Critical: Satisfy ForegroundService startForeground contract immediately
        startForegroundWithNotification()

        overlayLifecycleOwner.onCreate()
        overlayLifecycleOwner.onStart()
        overlayLifecycleOwner.onResume()

        ZXGamingApplication.instance.systemMonitor.startMonitoring(serviceScope)
        setupOverlayView()
        startNotificationUpdater()
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
            else -> {
                startForegroundWithNotification()
            }
        }
        return START_NOT_STICKY
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                NOTIFICATION_CHANNEL_ID,
                "ZX Real-time Metrics HUD",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Shows live FPS, CPU, and GPU performance metrics while overlay is active"
            }
            val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            nm.createNotificationChannel(channel)
        }
    }

    private fun startForegroundWithNotification() {
        val notification = buildMetricsNotification(
            fps = 60,
            frameTimeMs = 16.6f,
            cpu = -1,
            gpu = -1
        )

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

    private fun buildMetricsNotification(fps: Int, frameTimeMs: Float, cpu: Int, gpu: Int): android.app.Notification {
        val openIntent = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE
        )

        val stopIntent = PendingIntent.getService(
            this,
            1,
            Intent(this, RealtimeMetricsOverlayService::class.java).apply { action = ACTION_STOP },
            PendingIntent.FLAG_IMMUTABLE
        )

        val cpuStr = if (cpu >= 0) "$cpu%" else "active"
        val gpuStr = if (gpu >= 0) "$gpu%" else "pipeline"
        val latencyStr = if (frameTimeMs > 0f) " (${frameTimeMs}ms)" else ""
        val contentText = "FPS: $fps$latencyStr | CPU: $cpuStr | GPU: $gpuStr"

        return NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setContentTitle("ZX Real-Time Performance HUD")
            .setContentText(contentText)
            .setSmallIcon(android.R.drawable.ic_menu_info_details)
            .setContentIntent(openIntent)
            .addAction(android.R.drawable.ic_menu_close_clear_cancel, "Stop HUD", stopIntent)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .build()
    }

    private fun startNotificationUpdater() {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationUpdateJob = serviceScope.launch(Dispatchers.Default) {
            while (isActive) {
                delay(2000L) // Update notification text every 2s
                val telemetry = ZXGamingApplication.instance.systemMonitor.telemetry.value
                val notification = buildMetricsNotification(
                    fps = telemetry.fps,
                    frameTimeMs = telemetry.frameTimeMs,
                    cpu = telemetry.cpuUsagePercent,
                    gpu = telemetry.gpuUsagePercent
                )
                notificationManager.notify(NOTIFICATION_ID, notification)
            }
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setupOverlayView() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(this)) {
            // Cannot add overlay window without permission
            return
        }

        val layoutFlag = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            @Suppress("DEPRECATION")
            WindowManager.LayoutParams.TYPE_PHONE
        }

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            layoutFlag,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = 60
            y = 120
        }
        windowParams = params

        metricsView = ComposeView(this).apply {
            overlayLifecycleOwner.attachTo(this)
            setContent {
                MyApplicationTheme {
                    val app = ZXGamingApplication.instance
                    val telemetry by app.systemMonitor.telemetry.collectAsState()

                    RealtimeMetricsOverlayComponent(
                        telemetry = telemetry,
                        initialMode = MetricsOverlayMode.COMPACT_PILL,
                        onClose = { stopSelf() },
                        onDragDelta = { dx, dy ->
                            val currentParams = windowParams ?: return@RealtimeMetricsOverlayComponent
                            currentParams.x += dx.toInt()
                            currentParams.y += dy.toInt()
                            try {
                                windowManager.updateViewLayout(metricsView, currentParams)
                            } catch (_: Exception) {}
                        }
                    )
                }
            }

            // Also support direct native touch dragging for maximum precision
            setOnTouchListener(object : View.OnTouchListener {
                private var initialX = 0
                private var initialY = 0
                private var initialTouchX = 0f
                private var initialTouchY = 0f
                private var isDragging = false

                override fun onTouch(v: View?, event: MotionEvent): Boolean {
                    val p = windowParams ?: return false
                    when (event.action) {
                        MotionEvent.ACTION_DOWN -> {
                            initialX = p.x
                            initialY = p.y
                            initialTouchX = event.rawX
                            initialTouchY = event.rawY
                            isDragging = false
                            return false
                        }
                        MotionEvent.ACTION_MOVE -> {
                            val dx = (event.rawX - initialTouchX).toInt()
                            val dy = (event.rawY - initialTouchY).toInt()
                            if (abs(dx) > 10 || abs(dy) > 10 || isDragging) {
                                isDragging = true
                                p.x = initialX + dx
                                p.y = initialY + dy
                                try {
                                    windowManager.updateViewLayout(metricsView, p)
                                } catch (_: Exception) {}
                                return true
                            }
                        }
                    }
                    return false
                }
            })
        }

        try {
            windowManager.addView(metricsView, windowParams)
        } catch (_: Exception) {}
    }

    override fun onDestroy() {
        _isRunningFlow.value = false
        notificationUpdateJob?.cancel()
        metricsView?.let {
            try {
                windowManager.removeView(it)
            } catch (_: Exception) {}
        }
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
