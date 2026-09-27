package com.example.data.model

data class TelemetryData(
    val cpuUsagePercent: Int = -1, // -1 means unavailable
    val gpuUsagePercent: Int = -1, // -1 means unavailable, or 0-100%
    val gpuFrequencyMhz: Int = 0,
    val gpuRenderer: String = "Hardware Accelerated",
    val ramUsedBytes: Long = 0L,
    val ramTotalBytes: Long = 0L,
    val ramUsedGb: Float = 0f,
    val ramTotalGb: Float = 0f,
    val ramPercent: Int = 0,
    val fps: Int = 60,
    val fpsFloat: Float = 60.0f,
    val frameTimeMs: Float = 16.6f,
    val fpsOnePercentLow: Int = 58,
    val jankCount: Int = 0,
    val refreshRateHz: Float = 60f,
    val fpsHistory: List<Float> = emptyList(),
    val batteryPercent: Int = 0,
    val batteryTemperatureC: Float = -1f, // -1f means unavailable
    val batteryVoltageMv: Int = 0,
    val batteryStatus: String = "Normal",
    val thermalStatus: String = "Normal",
    val currentTimeString: String = "12:00 PM"
)
