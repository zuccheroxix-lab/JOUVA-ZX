package com.example.data.model

data class CrosshairConfig(
    val isEnabled: Boolean = false,
    val style: CrosshairStyle = CrosshairStyle.PLUS,
    val colorHex: String = "#00FF88",
    val sizePercent: Int = 40,
    val opacityPercent: Int = 100,
    val thicknessDp: Float = 2.5f,
    val rotationDeg: Float = 0f,
    val offsetX: Int = 0,
    val offsetY: Int = 0
)

enum class CrosshairStyle(val displayName: String) {
    DOT("Dot"),
    PLUS("Plus"),
    CROSS("Cross"),
    CIRCLE("Circle"),
    TCROSS("T-Cross"),
    DIAMOND("Diamond")
}
