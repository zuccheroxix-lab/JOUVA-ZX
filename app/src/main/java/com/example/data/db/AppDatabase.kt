package com.example.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        CrosshairPresetEntity::class,
        SensitivityProfileEntity::class,
        GameProfileEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun crosshairPresetDao(): CrosshairPresetDao
    abstract fun sensitivityProfileDao(): SensitivityProfileDao
    abstract fun gameProfileDao(): GameProfileDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "zx_gaming_database"
                )
                .fallbackToDestructiveMigration()
                .addCallback(object : Callback() {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        super.onCreate(db)
                        CoroutineScope(Dispatchers.IO).launch {
                            val database = getInstance(context)
                            populateInitialData(database)
                        }
                    }
                })
                .build()
                INSTANCE = instance
                instance
            }
        }

        private suspend fun populateInitialData(db: AppDatabase) {
            val crosshairDao = db.crosshairPresetDao()
            crosshairDao.insertPreset(
                CrosshairPresetEntity(
                    id = 1,
                    name = "DEFAULT PLUS",
                    style = "PLUS",
                    colorHex = "#00FF88",
                    sizePercent = 40,
                    opacityPercent = 100,
                    thicknessDp = 2.5f,
                    rotationDeg = 0f,
                    offsetX = 0,
                    offsetY = 0,
                    isDefault = true
                )
            )
            crosshairDao.insertPreset(
                CrosshairPresetEntity(
                    id = 2,
                    name = "SNIPER DOT",
                    style = "DOT",
                    colorHex = "#FF3366",
                    sizePercent = 30,
                    opacityPercent = 100,
                    thicknessDp = 3f,
                    rotationDeg = 0f,
                    offsetX = 0,
                    offsetY = 0,
                    isDefault = false
                )
            )
            crosshairDao.insertPreset(
                CrosshairPresetEntity(
                    id = 3,
                    name = "TACTICAL CIRCLE",
                    style = "CIRCLE",
                    colorHex = "#00E5FF",
                    sizePercent = 50,
                    opacityPercent = 90,
                    thicknessDp = 2f,
                    rotationDeg = 0f,
                    offsetX = 0,
                    offsetY = 0,
                    isDefault = false
                )
            )
            crosshairDao.insertPreset(
                CrosshairPresetEntity(
                    id = 4,
                    name = "T-CROSS PRO",
                    style = "TCROSS",
                    colorHex = "#FFDD00",
                    sizePercent = 45,
                    opacityPercent = 95,
                    thicknessDp = 2.5f,
                    rotationDeg = 0f,
                    offsetX = 0,
                    offsetY = 0,
                    isDefault = false
                )
            )

            val sensiDao = db.sensitivityProfileDao()
            sensiDao.insertProfile(
                SensitivityProfileEntity(
                    id = 1,
                    name = "DEFAULT BALANCED",
                    sensitivityX = 1.00f,
                    sensitivityY = 1.20f,
                    touchAcceleration = false,
                    globalEnabled = true,
                    zone = "Semua",
                    isDefault = true,
                    scope2xSensitivity = 1.10f,
                    scope4xSensitivity = 0.95f,
                    redDotSensitivity = 1.20f,
                    gyroSensitivity = 1.00f,
                    description = "Balanced touch coordinator for daily FPS gaming"
                )
            )
            sensiDao.insertProfile(
                SensitivityProfileEntity(
                    id = 2,
                    name = "AGGRESSIVE AIM",
                    sensitivityX = 2.30f,
                    sensitivityY = 2.50f,
                    touchAcceleration = true,
                    globalEnabled = true,
                    zone = "Kanan",
                    isDefault = false,
                    scope2xSensitivity = 1.80f,
                    scope4xSensitivity = 1.50f,
                    redDotSensitivity = 2.20f,
                    gyroSensitivity = 2.00f,
                    description = "High sensitivity curve for fast flick-shots"
                )
            )
            sensiDao.insertProfile(
                SensitivityProfileEntity(
                    id = 3,
                    name = "STEADY CONTROL",
                    sensitivityX = 1.00f,
                    sensitivityY = 1.00f,
                    touchAcceleration = false,
                    globalEnabled = true,
                    zone = "Kiri",
                    isDefault = false,
                    scope2xSensitivity = 0.90f,
                    scope4xSensitivity = 0.80f,
                    redDotSensitivity = 1.00f,
                    gyroSensitivity = 0.90f,
                    description = "Linear 1:1 control with no artificial acceleration"
                )
            )
            sensiDao.insertProfile(
                SensitivityProfileEntity(
                    id = 4,
                    name = "HEADSHOT PRO (Y-CURVE)",
                    sensitivityX = 1.75f,
                    sensitivityY = 2.85f,
                    touchAcceleration = true,
                    globalEnabled = true,
                    zone = "Semua",
                    isDefault = false,
                    scope2xSensitivity = 1.60f,
                    scope4xSensitivity = 1.30f,
                    redDotSensitivity = 2.00f,
                    gyroSensitivity = 1.50f,
                    description = "High vertical Y curve designed for upward drag headshots"
                )
            )

            val gameDao = db.gameProfileDao()
            gameDao.insertGameProfile(
                GameProfileEntity(
                    id = 1,
                    appName = "Free Fire MAX",
                    packageName = "com.dts.freefiremax",
                    customSensitivityX = 2.10f,
                    customSensitivityY = 2.70f,
                    touchAcceleration = true,
                    sensitivityZone = "Semua",
                    sensitivityProfileId = 4,
                    crosshairPresetId = 1,
                    performanceTier = "PERFORMANCE",
                    targetFps = 60,
                    notes = "Headshot drag configuration with max touch acceleration",
                    isUserDefined = false,
                    lastLaunchedTimestamp = System.currentTimeMillis() - 3600000
                )
            )
            gameDao.insertGameProfile(
                GameProfileEntity(
                    id = 2,
                    appName = "PUBG Mobile",
                    packageName = "com.tencent.ig",
                    customSensitivityX = 1.35f,
                    customSensitivityY = 1.55f,
                    touchAcceleration = false,
                    sensitivityZone = "Kanan",
                    sensitivityProfileId = 1,
                    crosshairPresetId = 2,
                    performanceTier = "BALANCED",
                    targetFps = 90,
                    notes = "Steady recoil control with dot reticle",
                    isUserDefined = false,
                    lastLaunchedTimestamp = System.currentTimeMillis() - 7200000
                )
            )
            gameDao.insertGameProfile(
                GameProfileEntity(
                    id = 3,
                    appName = "Call of Duty: Mobile",
                    packageName = "com.activision.callofduty.shooter",
                    customSensitivityX = 1.50f,
                    customSensitivityY = 1.80f,
                    touchAcceleration = true,
                    sensitivityZone = "Semua",
                    sensitivityProfileId = 2,
                    crosshairPresetId = 3,
                    performanceTier = "PERFORMANCE",
                    targetFps = 60,
                    notes = "Fast snap aiming profile",
                    isUserDefined = false,
                    lastLaunchedTimestamp = System.currentTimeMillis() - 10800000
                )
            )
            gameDao.insertGameProfile(
                GameProfileEntity(
                    id = 4,
                    appName = "Mobile Legends: Bang Bang",
                    packageName = "com.mobile.legends",
                    customSensitivityX = 1.05f,
                    customSensitivityY = 1.05f,
                    touchAcceleration = false,
                    sensitivityZone = "Semua",
                    sensitivityProfileId = 3,
                    crosshairPresetId = null,
                    performanceTier = "BALANCED",
                    targetFps = 60,
                    notes = "Precision skill aiming with zero jitter",
                    isUserDefined = false,
                    lastLaunchedTimestamp = System.currentTimeMillis() - 14400000
                )
            )
        }
    }
}
