package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.model.CrosshairConfig
import com.example.data.model.CrosshairStyle
import com.example.data.model.PerformanceTier
import com.example.data.model.SensitivityConfig
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("ZX Gaming", appName)
    }

    @Test
    fun `crosshair default configuration is valid`() {
        val config = CrosshairConfig()
        assertEquals(false, config.isEnabled)
        assertEquals(CrosshairStyle.PLUS, config.style)
        assertEquals("#00FF88", config.colorHex)
        assertTrue(config.sizePercent in 10..100)
    }

    @Test
    fun `sensitivity default configuration is valid`() {
        val sensi = SensitivityConfig()
        assertTrue(sensi.globalEnabled)
        assertTrue(sensi.sensitivityX in 1.0f..5.0f)
        assertTrue(sensi.sensitivityY in 1.0f..5.0f)
    }

    @Test
    fun `performance tiers are defined`() {
        val tiers = PerformanceTier.entries
        assertEquals(3, tiers.size)
        assertNotNull(PerformanceTier.BALANCED)
        assertNotNull(PerformanceTier.PERFORMANCE)
        assertNotNull(PerformanceTier.BATTERY_SAVER)
    }

    @Test
    fun `telemetry data model contains real-time GPU and FPS metrics`() {
        val telemetry = com.example.data.model.TelemetryData(
            fps = 60,
            cpuUsagePercent = 25,
            gpuUsagePercent = 38,
            gpuFrequencyMhz = 587,
            refreshRateHz = 60f
        )
        assertEquals(60, telemetry.fps)
        assertEquals(25, telemetry.cpuUsagePercent)
        assertEquals(38, telemetry.gpuUsagePercent)
        assertEquals(587, telemetry.gpuFrequencyMhz)
        assertEquals(60f, telemetry.refreshRateHz)
    }

    @Test
    fun `real-time metrics overlay service actions and state are valid`() {
        assertEquals("com.example.service.METRICS_OVERLAY_START", com.example.service.RealtimeMetricsOverlayService.ACTION_START)
        assertEquals("com.example.service.METRICS_OVERLAY_STOP", com.example.service.RealtimeMetricsOverlayService.ACTION_STOP)
        assertNotNull(com.example.service.RealtimeMetricsOverlayService.isRunningFlow)
    }

    @Test
    fun `choreographer frame monitor calculates real-time frame stats`() {
        val monitor = com.example.data.monitoring.ChoreographerFrameMonitor()
        assertNotNull(monitor.frameStats)
        val stats = monitor.frameStats.value
        assertEquals(60, stats.fps)
        assertTrue(stats.frameTimeMs > 0f)
        assertEquals(60f, stats.measuredRefreshRateHz)
    }

    @Test
    fun `room database stores and retrieves user-defined game profiles with custom sensitivity`() = kotlinx.coroutines.runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val db = androidx.room.Room.inMemoryDatabaseBuilder(
            context,
            com.example.data.db.AppDatabase::class.java
        ).allowMainThreadQueries().build()

        val gameDao = db.gameProfileDao()
        val customProfile = com.example.data.db.GameProfileEntity(
            appName = "Free Fire MAX Ranked",
            packageName = "com.dts.freefiremax",
            customSensitivityX = 2.45f,
            customSensitivityY = 3.10f,
            touchAcceleration = true,
            sensitivityZone = "Semua",
            performanceTier = "PERFORMANCE",
            targetFps = 90,
            notes = "Aggressive headshot curve for sniper and desert eagle",
            isUserDefined = true
        )

        val profileId = gameDao.insertGameProfile(customProfile)
        assertTrue(profileId > 0)

        val retrieved = gameDao.getGameProfileById(profileId)
        assertNotNull(retrieved)
        assertEquals("Free Fire MAX Ranked", retrieved?.appName)
        assertEquals("com.dts.freefiremax", retrieved?.packageName)
        assertEquals(2.45f, retrieved?.customSensitivityX)
        assertEquals(3.10f, retrieved?.customSensitivityY)
        assertTrue(retrieved?.touchAcceleration == true)
        assertEquals("PERFORMANCE", retrieved?.performanceTier)
        assertEquals(90, retrieved?.targetFps)

        // Test update
        val updated = retrieved!!.copy(customSensitivityY = 3.50f, notes = "Updated headshot drag")
        gameDao.updateGameProfile(updated)
        val postUpdate = gameDao.getGameProfileById(profileId)
        assertEquals(3.50f, postUpdate?.customSensitivityY)
        assertEquals("Updated headshot drag", postUpdate?.notes)

        // Test delete
        gameDao.deleteGameProfileById(profileId)
        val postDelete = gameDao.getGameProfileById(profileId)
        org.junit.Assert.assertNull(postDelete)

        db.close()
    }

    @Test
    fun `room database stores and retrieves custom sensitivity profiles`() = kotlinx.coroutines.runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val db = androidx.room.Room.inMemoryDatabaseBuilder(
            context,
            com.example.data.db.AppDatabase::class.java
        ).allowMainThreadQueries().build()

        val sensiDao = db.sensitivityProfileDao()
        val customSensi = com.example.data.db.SensitivityProfileEntity(
            name = "TOURNAMENT ULTRA SENSI",
            sensitivityX = 2.20f,
            sensitivityY = 2.80f,
            touchAcceleration = true,
            globalEnabled = true,
            zone = "Kanan",
            isDefault = false,
            scope2xSensitivity = 1.90f,
            scope4xSensitivity = 1.60f,
            redDotSensitivity = 2.40f,
            gyroSensitivity = 2.10f,
            description = "High curve snap aiming"
        )

        val insertedId = sensiDao.insertProfile(customSensi)
        assertTrue(insertedId > 0)

        val fetched = sensiDao.getProfileById(insertedId.toInt())
        assertNotNull(fetched)
        assertEquals("TOURNAMENT ULTRA SENSI", fetched?.name)
        assertEquals(2.20f, fetched?.sensitivityX)
        assertEquals(2.80f, fetched?.sensitivityY)
        assertEquals(1.90f, fetched?.scope2xSensitivity)
        assertEquals(2.40f, fetched?.redDotSensitivity)
        assertEquals("High curve snap aiming", fetched?.description)

        db.close()
    }
}
