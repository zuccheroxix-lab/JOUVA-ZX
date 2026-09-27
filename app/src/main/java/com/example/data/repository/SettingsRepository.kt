package com.example.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.example.data.model.CrosshairConfig
import com.example.data.model.CrosshairStyle
import com.example.data.model.PerformanceTier
import com.example.data.model.SensiZone
import com.example.data.model.SensitivityConfig
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class SettingsRepository(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("zx_gaming_prefs", Context.MODE_PRIVATE)

    private val _crosshairConfig = MutableStateFlow(loadCrosshairConfig())
    val crosshairConfig: StateFlow<CrosshairConfig> = _crosshairConfig.asStateFlow()

    private val _sensitivityConfig = MutableStateFlow(loadSensitivityConfig())
    val sensitivityConfig: StateFlow<SensitivityConfig> = _sensitivityConfig.asStateFlow()

    private val _performanceTier = MutableStateFlow(loadPerformanceTier())
    val performanceTier: StateFlow<PerformanceTier> = _performanceTier.asStateFlow()

    private val _graphicsQuality = MutableStateFlow(prefs.getString("graphics_quality", "HIGH") ?: "HIGH")
    val graphicsQuality: StateFlow<String> = _graphicsQuality.asStateFlow()

    private val _enableBlur = MutableStateFlow(prefs.getBoolean("enable_blur", true))
    val enableBlur: StateFlow<Boolean> = _enableBlur.asStateFlow()

    private val _panelAlpha = MutableStateFlow(prefs.getFloat("panel_alpha", 0.90f))
    val panelAlpha: StateFlow<Float> = _panelAlpha.asStateFlow()

    private val _floatingButtonSize = MutableStateFlow(prefs.getInt("floating_btn_size", 54))
    val floatingButtonSize: StateFlow<Int> = _floatingButtonSize.asStateFlow()

    private val _floatingButtonX = MutableStateFlow(prefs.getInt("floating_btn_x", 40))
    val floatingButtonX: StateFlow<Int> = _floatingButtonX.asStateFlow()

    private val _floatingButtonY = MutableStateFlow(prefs.getInt("floating_btn_y", 200))
    val floatingButtonY: StateFlow<Int> = _floatingButtonY.asStateFlow()

    private val _showMiniMonitoring = MutableStateFlow(prefs.getBoolean("show_mini_monitoring", false))
    val showMiniMonitoring: StateFlow<Boolean> = _showMiniMonitoring.asStateFlow()

    private fun loadCrosshairConfig(): CrosshairConfig {
        val styleStr = prefs.getString("crosshair_style", CrosshairStyle.PLUS.name) ?: CrosshairStyle.PLUS.name
        val style = try { CrosshairStyle.valueOf(styleStr) } catch (_: Exception) { CrosshairStyle.PLUS }
        return CrosshairConfig(
            isEnabled = prefs.getBoolean("crosshair_enabled", false),
            style = style,
            colorHex = prefs.getString("crosshair_color", "#00FF88") ?: "#00FF88",
            sizePercent = prefs.getInt("crosshair_size", 40),
            opacityPercent = prefs.getInt("crosshair_opacity", 100),
            thicknessDp = prefs.getFloat("crosshair_thickness", 2.5f),
            rotationDeg = prefs.getFloat("crosshair_rotation", 0f),
            offsetX = prefs.getInt("crosshair_offset_x", 0),
            offsetY = prefs.getInt("crosshair_offset_y", 0)
        )
    }

    fun updateCrosshairConfig(config: CrosshairConfig) {
        _crosshairConfig.value = config
        prefs.edit()
            .putBoolean("crosshair_enabled", config.isEnabled)
            .putString("crosshair_style", config.style.name)
            .putString("crosshair_color", config.colorHex)
            .putInt("crosshair_size", config.sizePercent)
            .putInt("crosshair_opacity", config.opacityPercent)
            .putFloat("crosshair_thickness", config.thicknessDp)
            .putFloat("crosshair_rotation", config.rotationDeg)
            .putInt("crosshair_offset_x", config.offsetX)
            .putInt("crosshair_offset_y", config.offsetY)
            .apply()
    }

    private fun loadSensitivityConfig(): SensitivityConfig {
        val zoneStr = prefs.getString("sensi_zone", SensiZone.SEMUA.name) ?: SensiZone.SEMUA.name
        val zone = try { SensiZone.valueOf(zoneStr) } catch (_: Exception) { SensiZone.SEMUA }
        return SensitivityConfig(
            profileName = prefs.getString("sensi_name", "Sensitivity 1") ?: "Sensitivity 1",
            sensitivityX = prefs.getFloat("sensi_x", 1.00f),
            sensitivityY = prefs.getFloat("sensi_y", 1.20f),
            touchAcceleration = prefs.getBoolean("sensi_accel", false),
            globalEnabled = prefs.getBoolean("sensi_global", true),
            zone = zone
        )
    }

    fun updateSensitivityConfig(config: SensitivityConfig) {
        _sensitivityConfig.value = config
        prefs.edit()
            .putString("sensi_name", config.profileName)
            .putFloat("sensi_x", config.sensitivityX)
            .putFloat("sensi_y", config.sensitivityY)
            .putBoolean("sensi_accel", config.touchAcceleration)
            .putBoolean("sensi_global", config.globalEnabled)
            .putString("sensi_zone", config.zone.name)
            .apply()
    }

    private fun loadPerformanceTier(): PerformanceTier {
        val tierStr = prefs.getString("perf_tier", PerformanceTier.BALANCED.name) ?: PerformanceTier.BALANCED.name
        return try { PerformanceTier.valueOf(tierStr) } catch (_: Exception) { PerformanceTier.BALANCED }
    }

    fun updatePerformanceTier(tier: PerformanceTier) {
        _performanceTier.value = tier
        prefs.edit().putString("perf_tier", tier.name).apply()
    }

    fun setGraphicsQuality(tier: String) {
        _graphicsQuality.value = tier
        prefs.edit().putString("graphics_quality", tier).apply()
    }

    fun setEnableBlur(enabled: Boolean) {
        _enableBlur.value = enabled
        prefs.edit().putBoolean("enable_blur", enabled).apply()
    }

    fun setPanelAlpha(alpha: Float) {
        _panelAlpha.value = alpha
        prefs.edit().putFloat("panel_alpha", alpha).apply()
    }

    fun saveFloatingButtonPosition(x: Int, y: Int) {
        _floatingButtonX.value = x
        _floatingButtonY.value = y
        prefs.edit().putInt("floating_btn_x", x).putInt("floating_btn_y", y).apply()
    }

    fun setFloatingButtonSize(size: Int) {
        _floatingButtonSize.value = size
        prefs.edit().putInt("floating_btn_size", size).apply()
    }

    fun setShowMiniMonitoring(show: Boolean) {
        _showMiniMonitoring.value = show
        prefs.edit().putBoolean("show_mini_monitoring", show).apply()
    }

    fun resetAllSettings() {
        prefs.edit().clear().apply()
        _crosshairConfig.value = CrosshairConfig()
        _sensitivityConfig.value = SensitivityConfig()
        _performanceTier.value = PerformanceTier.BALANCED
        _graphicsQuality.value = "HIGH"
        _enableBlur.value = true
        _panelAlpha.value = 0.90f
        _floatingButtonSize.value = 54
        _floatingButtonX.value = 40
        _floatingButtonY.value = 200
        _showMiniMonitoring.value = false
    }
}
