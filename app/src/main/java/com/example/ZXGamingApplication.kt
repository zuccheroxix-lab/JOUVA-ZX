package com.example

import android.app.Application
import com.example.data.db.AppDatabase
import com.example.data.monitoring.SystemMonitor
import com.example.data.repository.PresetsRepository
import com.example.data.repository.SettingsRepository

class ZXGamingApplication : Application() {

    lateinit var database: AppDatabase
        private set

    lateinit var settingsRepository: SettingsRepository
        private set

    lateinit var presetsRepository: PresetsRepository
        private set

    lateinit var systemMonitor: SystemMonitor
        private set

    override fun onCreate() {
        super.onCreate()
        instance = this
        database = AppDatabase.getInstance(this)
        settingsRepository = SettingsRepository(this)
        presetsRepository = PresetsRepository(database)
        systemMonitor = SystemMonitor(this)
    }

    companion object {
        lateinit var instance: ZXGamingApplication
            private set
    }
}
