package com.example.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "crosshair_presets")
data class CrosshairPresetEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val style: String,
    val colorHex: String,
    val sizePercent: Int,
    val opacityPercent: Int,
    val thicknessDp: Float,
    val rotationDeg: Float,
    val offsetX: Int,
    val offsetY: Int,
    val isDefault: Boolean = false
)

@Entity(tableName = "sensitivity_profiles")
data class SensitivityProfileEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val sensitivityX: Float,
    val sensitivityY: Float,
    val touchAcceleration: Boolean,
    val globalEnabled: Boolean,
    val zone: String,
    val isDefault: Boolean = false,
    val scope2xSensitivity: Float = 1.0f,
    val scope4xSensitivity: Float = 1.0f,
    val redDotSensitivity: Float = 1.0f,
    val gyroSensitivity: Float = 1.0f,
    val description: String = ""
)

@Entity(tableName = "game_profiles")
data class GameProfileEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val packageName: String,
    val appName: String,
    val customSensitivityX: Float = 1.00f,
    val customSensitivityY: Float = 1.20f,
    val touchAcceleration: Boolean = false,
    val sensitivityZone: String = "SEMUA",
    val sensitivityProfileId: Int? = null,
    val crosshairPresetId: Int? = null,
    val performanceTier: String = "BALANCED",
    val targetFps: Int = 60,
    val notes: String = "",
    val isUserDefined: Boolean = true,
    val lastLaunchedTimestamp: Long = System.currentTimeMillis()
)
