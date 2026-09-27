package com.example.ui.viewmodel

import android.app.Application
import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Environment
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.ZXGamingApplication
import com.example.data.db.CrosshairPresetEntity
import com.example.data.db.GameProfileEntity
import com.example.data.db.SensitivityProfileEntity
import com.example.data.model.CrosshairConfig
import com.example.data.model.CrosshairStyle
import com.example.data.model.PerformanceTier
import com.example.data.model.SensiZone
import com.example.data.model.SensitivityConfig
import com.example.data.model.TelemetryData
import com.example.service.OverlayService
import com.example.service.RealtimeMetricsOverlayService
import com.example.service.RecordingState
import com.example.service.ScreenRecordingService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class InstalledAppItem(
    val packageName: String,
    val appName: String,
    val icon: Drawable?,
    val isGameCandidate: Boolean
)

class CompanionViewModel(application: Application) : AndroidViewModel(application) {

    private val app = application as ZXGamingApplication
    private val settingsRepo = app.settingsRepository
    private val presetsRepo = app.presetsRepository
    val systemMonitor = app.systemMonitor

    val telemetry: StateFlow<TelemetryData> = systemMonitor.telemetry

    val crosshairConfig: StateFlow<CrosshairConfig> = settingsRepo.crosshairConfig
    val sensitivityConfig: StateFlow<SensitivityConfig> = settingsRepo.sensitivityConfig
    val performanceTier: StateFlow<PerformanceTier> = settingsRepo.performanceTier
    val graphicsQuality: StateFlow<String> = settingsRepo.graphicsQuality
    val enableBlur: StateFlow<Boolean> = settingsRepo.enableBlur
    val panelAlpha: StateFlow<Float> = settingsRepo.panelAlpha
    val showMiniMonitoring: StateFlow<Boolean> = settingsRepo.showMiniMonitoring

    val crosshairPresets: StateFlow<List<CrosshairPresetEntity>> =
        presetsRepo.allCrosshairPresets.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )

    val sensitivityProfiles: StateFlow<List<SensitivityProfileEntity>> =
        presetsRepo.allSensitivityProfiles.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )

    val gameProfiles: StateFlow<List<GameProfileEntity>> =
        presetsRepo.allGameProfiles.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )

    private val _installedApps = MutableStateFlow<List<InstalledAppItem>>(emptyList())
    val installedApps: StateFlow<List<InstalledAppItem>> = _installedApps.asStateFlow()

    val recordingStatus: StateFlow<RecordingState> = ScreenRecordingService.recordingStatus
    val lastRecordedFile: StateFlow<String?> = ScreenRecordingService.lastSavedFilePath

    private val _lastScreenshotMessage = MutableStateFlow<String?>(null)
    val lastScreenshotMessage: StateFlow<String?> = _lastScreenshotMessage.asStateFlow()

    private val _isOverlayRunning = MutableStateFlow(OverlayService.isRunning)
    val isOverlayRunning: StateFlow<Boolean> = _isOverlayRunning.asStateFlow()

    val isMetricsOverlayRunning: StateFlow<Boolean> = RealtimeMetricsOverlayService.isRunningFlow

    init {
        systemMonitor.startMonitoring(viewModelScope)
        loadInstalledApps()
    }

    fun refreshOverlayState() {
        _isOverlayRunning.value = OverlayService.isRunning
    }

    fun toggleMetricsOverlay(context: Context) {
        val nextRunning = !isMetricsOverlayRunning.value
        val intent = Intent(context, RealtimeMetricsOverlayService::class.java).apply {
            action = if (nextRunning) RealtimeMetricsOverlayService.ACTION_START else RealtimeMetricsOverlayService.ACTION_STOP
        }
        try {
            if (nextRunning) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    context.startForegroundService(intent)
                } else {
                    context.startService(intent)
                }
            } else {
                context.stopService(intent)
            }
        } catch (_: Exception) {}
    }

    fun updateCrosshair(config: CrosshairConfig) {
        settingsRepo.updateCrosshairConfig(config)
        // Notify overlay service if running
        if (OverlayService.isRunning) {
            val intent = Intent(getApplication(), OverlayService::class.java).apply {
                action = OverlayService.ACTION_UPDATE_CONFIG
            }
            getApplication<Application>().startService(intent)
        }
    }

    fun updateCrosshairOffset(dx: Int, dy: Int) {
        val current = crosshairConfig.value
        updateCrosshair(
            current.copy(
                offsetX = (current.offsetX + dx).coerceIn(-500, 500),
                offsetY = (current.offsetY + dy).coerceIn(-500, 500)
            )
        )
    }

    fun resetCrosshairX() {
        val current = crosshairConfig.value
        updateCrosshair(current.copy(offsetX = 0))
    }

    fun resetCrosshairY() {
        val current = crosshairConfig.value
        updateCrosshair(current.copy(offsetY = 0))
    }

    fun resetCrosshairSize() {
        val current = crosshairConfig.value
        updateCrosshair(current.copy(sizePercent = 40))
    }

    fun saveCrosshairPreset(name: String) {
        val current = crosshairConfig.value
        viewModelScope.launch {
            presetsRepo.insertCrosshairPreset(
                CrosshairPresetEntity(
                    name = name.ifBlank { "Preset ${System.currentTimeMillis() % 1000}" },
                    style = current.style.name,
                    colorHex = current.colorHex,
                    sizePercent = current.sizePercent,
                    opacityPercent = current.opacityPercent,
                    thicknessDp = current.thicknessDp,
                    rotationDeg = current.rotationDeg,
                    offsetX = current.offsetX,
                    offsetY = current.offsetY,
                    isDefault = false
                )
            )
        }
    }

    fun applyCrosshairPreset(preset: CrosshairPresetEntity) {
        val style = try { CrosshairStyle.valueOf(preset.style) } catch (_: Exception) { CrosshairStyle.PLUS }
        val newConfig = CrosshairConfig(
            isEnabled = true,
            style = style,
            colorHex = preset.colorHex,
            sizePercent = preset.sizePercent,
            opacityPercent = preset.opacityPercent,
            thicknessDp = preset.thicknessDp,
            rotationDeg = preset.rotationDeg,
            offsetX = preset.offsetX,
            offsetY = preset.offsetY
        )
        updateCrosshair(newConfig)
    }

    fun deleteCrosshairPreset(id: Int) {
        viewModelScope.launch {
            presetsRepo.deleteCrosshairPreset(id)
        }
    }

    fun updateSensitivity(config: SensitivityConfig) {
        settingsRepo.updateSensitivityConfig(config)
    }

    fun saveSensitivityProfile(
        name: String,
        description: String = "",
        scope2x: Float = 1.0f,
        scope4x: Float = 1.0f,
        redDot: Float = 1.0f,
        gyro: Float = 1.0f
    ) {
        val current = sensitivityConfig.value
        viewModelScope.launch {
            presetsRepo.insertSensitivityProfile(
                SensitivityProfileEntity(
                    name = name.ifBlank { "Profile ${System.currentTimeMillis() % 1000}" },
                    sensitivityX = current.sensitivityX,
                    sensitivityY = current.sensitivityY,
                    touchAcceleration = current.touchAcceleration,
                    globalEnabled = current.globalEnabled,
                    zone = current.zone.name,
                    isDefault = false,
                    scope2xSensitivity = scope2x,
                    scope4xSensitivity = scope4x,
                    redDotSensitivity = redDot,
                    gyroSensitivity = gyro,
                    description = description
                )
            )
        }
    }

    fun updateSensitivityProfile(profile: SensitivityProfileEntity) {
        viewModelScope.launch {
            presetsRepo.updateSensitivityProfile(profile)
        }
    }

    fun applySensitivityProfile(profile: SensitivityProfileEntity) {
        val zone = try { SensiZone.valueOf(profile.zone.uppercase()) } catch (_: Exception) { SensiZone.SEMUA }
        val newConfig = SensitivityConfig(
            profileName = profile.name,
            sensitivityX = profile.sensitivityX,
            sensitivityY = profile.sensitivityY,
            touchAcceleration = profile.touchAcceleration,
            globalEnabled = profile.globalEnabled,
            zone = zone
        )
        updateSensitivity(newConfig)
    }

    fun deleteSensitivityProfile(id: Int) {
        viewModelScope.launch {
            presetsRepo.deleteSensitivityProfile(id)
        }
    }

    fun setPerformanceTier(tier: PerformanceTier) {
        settingsRepo.updatePerformanceTier(tier)
    }

    fun setGraphicsQuality(tier: String) {
        settingsRepo.setGraphicsQuality(tier)
    }

    fun setEnableBlur(enabled: Boolean) {
        settingsRepo.setEnableBlur(enabled)
    }

    fun setPanelAlpha(alpha: Float) {
        settingsRepo.setPanelAlpha(alpha)
    }

    fun toggleMiniMonitoring() {
        val next = !showMiniMonitoring.value
        settingsRepo.setShowMiniMonitoring(next)
        val app = getApplication<Application>()
        val intent = Intent(app, RealtimeMetricsOverlayService::class.java).apply {
            action = if (next) RealtimeMetricsOverlayService.ACTION_START else RealtimeMetricsOverlayService.ACTION_STOP
        }
        try {
            if (next) {
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

    fun resetAllSettings() {
        settingsRepo.resetAllSettings()
    }

    fun loadInstalledApps() {
        viewModelScope.launch(Dispatchers.IO) {
            val pm = getApplication<Application>().packageManager
            val packages = pm.getInstalledApplications(PackageManager.GET_META_DATA)
            val list = mutableListOf<InstalledAppItem>()

            for (appInfo in packages) {
                // Ignore self and background tools without launch intent
                if (appInfo.packageName == getApplication<Application>().packageName) continue
                val launchIntent = pm.getLaunchIntentForPackage(appInfo.packageName) ?: continue

                val isSystem = (appInfo.flags and ApplicationInfo.FLAG_SYSTEM) != 0
                val isGame = (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    appInfo.category == ApplicationInfo.CATEGORY_GAME
                } else false) || (!isSystem && (
                    appInfo.packageName.contains("game", ignoreCase = true) ||
                    appInfo.packageName.contains("dts", ignoreCase = true) ||
                    appInfo.packageName.contains("pubg", ignoreCase = true) ||
                    appInfo.packageName.contains("mobile", ignoreCase = true) ||
                    appInfo.packageName.contains("freefire", ignoreCase = true)
                ))

                val name = try {
                    pm.getApplicationLabel(appInfo).toString()
                } catch (_: Exception) {
                    appInfo.packageName
                }

                val icon = try {
                    pm.getApplicationIcon(appInfo)
                } catch (_: Exception) {
                    null
                }

                list.add(
                    InstalledAppItem(
                        packageName = appInfo.packageName,
                        appName = name,
                        icon = icon,
                        isGameCandidate = isGame
                    )
                )
            }

            // Sort: games first, then alphabetically
            list.sortWith(compareByDescending<InstalledAppItem> { it.isGameCandidate }.thenBy { it.appName.lowercase() })
            _installedApps.value = list
        }
    }

    fun launchApp(packageName: String): Boolean {
        return try {
            val pm = getApplication<Application>().packageManager
            val launchIntent = pm.getLaunchIntentForPackage(packageName)
            if (launchIntent != null) {
                launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                getApplication<Application>().startActivity(launchIntent)
                true
            } else {
                false
            }
        } catch (_: Exception) {
            false
        }
    }

    fun addGameProfile(
        item: InstalledAppItem,
        customX: Float = sensitivityConfig.value.sensitivityX,
        customY: Float = sensitivityConfig.value.sensitivityY,
        accel: Boolean = sensitivityConfig.value.touchAcceleration,
        zone: String = sensitivityConfig.value.zone.name,
        tier: String = performanceTier.value.name,
        targetFps: Int = 60,
        notes: String = ""
    ) {
        viewModelScope.launch {
            presetsRepo.insertGameProfile(
                GameProfileEntity(
                    packageName = item.packageName,
                    appName = item.appName,
                    customSensitivityX = customX,
                    customSensitivityY = customY,
                    touchAcceleration = accel,
                    sensitivityZone = zone,
                    performanceTier = tier,
                    targetFps = targetFps,
                    notes = notes,
                    isUserDefined = true,
                    lastLaunchedTimestamp = System.currentTimeMillis()
                )
            )
        }
    }

    fun addUserDefinedGameProfile(profile: GameProfileEntity) {
        viewModelScope.launch {
            presetsRepo.insertGameProfile(profile)
        }
    }

    fun updateGameProfile(profile: GameProfileEntity) {
        viewModelScope.launch {
            presetsRepo.updateGameProfile(profile)
        }
    }

    fun deleteGameProfile(entity: GameProfileEntity) {
        viewModelScope.launch {
            presetsRepo.deleteGameProfile(entity)
        }
    }

    fun deleteGameProfileById(id: Long) {
        viewModelScope.launch {
            presetsRepo.deleteGameProfileById(id)
        }
    }

    fun applyGameProfile(profile: GameProfileEntity, launchNow: Boolean = true) {
        viewModelScope.launch {
            // Apply custom sensitivity settings from this profile
            val zone = try {
                SensiZone.valueOf(profile.sensitivityZone.uppercase())
            } catch (_: Exception) {
                SensiZone.SEMUA
            }
            val newSensi = SensitivityConfig(
                profileName = "${profile.appName} Custom",
                sensitivityX = profile.customSensitivityX,
                sensitivityY = profile.customSensitivityY,
                touchAcceleration = profile.touchAcceleration,
                globalEnabled = true,
                zone = zone
            )
            settingsRepo.updateSensitivityConfig(newSensi)

            // Apply performance tier
            try {
                val tier = PerformanceTier.valueOf(profile.performanceTier.uppercase())
                settingsRepo.updatePerformanceTier(tier)
            } catch (_: Exception) {}

            // Update timestamp in Room database
            presetsRepo.updateGameProfile(
                profile.copy(lastLaunchedTimestamp = System.currentTimeMillis())
            )

            // Launch app if requested
            if (launchNow && profile.packageName.isNotBlank()) {
                launchApp(profile.packageName)
            }
        }
    }

    fun captureScreenshot(bitmap: Bitmap) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val outputDir = getApplication<Application>().getExternalFilesDir(Environment.DIRECTORY_PICTURES)
                    ?: getApplication<Application>().filesDir
                if (!outputDir.exists()) outputDir.mkdirs()

                val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
                val file = File(outputDir, "ZX_SHOT_$timeStamp.png")

                val fos = FileOutputStream(file)
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos)
                fos.flush()
                fos.close()

                _lastScreenshotMessage.value = "Screenshot saved: ${file.name}"
            } catch (e: Exception) {
                _lastScreenshotMessage.value = "Screenshot failed: ${e.message}"
            }
        }
    }

    fun clearScreenshotMessage() {
        _lastScreenshotMessage.value = null
    }

    override fun onCleared() {
        super.onCleared()
        systemMonitor.stopMonitoring()
    }
}
