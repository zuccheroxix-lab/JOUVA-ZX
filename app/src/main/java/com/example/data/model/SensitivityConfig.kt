package com.example.data.model

data class SensitivityConfig(
    val profileName: String = "Sensitivity 1",
    val sensitivityX: Float = 1.00f,
    val sensitivityY: Float = 1.20f,
    val touchAcceleration: Boolean = false,
    val globalEnabled: Boolean = true,
    val zone: SensiZone = SensiZone.SEMUA
)

enum class SensiZone(val label: String) {
    KIRI("Kiri"),
    SEMUA("Semua"),
    KANAN("Kanan")
}

enum class PerformanceTier(val label: String, val description: String) {
    BALANCED("Balanced", "Optimal balance between frame stability and battery consumption."),
    PERFORMANCE("Performance", "Maximized system responsiveness and unlocked display refresh rate."),
    BATTERY_SAVER("Battery Saver", "Conserves battery by reducing background refresh overhead.")
}
