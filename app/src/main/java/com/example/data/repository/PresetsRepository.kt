package com.example.data.repository

import com.example.data.db.AppDatabase
import com.example.data.db.CrosshairPresetEntity
import com.example.data.db.GameProfileEntity
import com.example.data.db.SensitivityProfileEntity
import kotlinx.coroutines.flow.Flow

class PresetsRepository(private val db: AppDatabase) {
    val allCrosshairPresets: Flow<List<CrosshairPresetEntity>> =
        db.crosshairPresetDao().getAllPresets()

    val allSensitivityProfiles: Flow<List<SensitivityProfileEntity>> =
        db.sensitivityProfileDao().getAllProfiles()

    val allGameProfiles: Flow<List<GameProfileEntity>> =
        db.gameProfileDao().getAllGameProfiles()

    suspend fun insertCrosshairPreset(preset: CrosshairPresetEntity): Long =
        db.crosshairPresetDao().insertPreset(preset)

    suspend fun deleteCrosshairPreset(id: Int) =
        db.crosshairPresetDao().deletePresetById(id)

    suspend fun insertSensitivityProfile(profile: SensitivityProfileEntity): Long =
        db.sensitivityProfileDao().insertProfile(profile)

    suspend fun updateSensitivityProfile(profile: SensitivityProfileEntity) =
        db.sensitivityProfileDao().updateProfile(profile)

    suspend fun deleteSensitivityProfile(id: Int) =
        db.sensitivityProfileDao().deleteProfileById(id)

    suspend fun getSensitivityProfileById(id: Int): SensitivityProfileEntity? =
        db.sensitivityProfileDao().getProfileById(id)

    suspend fun insertGameProfile(profile: GameProfileEntity): Long =
        db.gameProfileDao().insertGameProfile(profile)

    suspend fun updateGameProfile(profile: GameProfileEntity) =
        db.gameProfileDao().updateGameProfile(profile)

    suspend fun deleteGameProfile(profile: GameProfileEntity) =
        db.gameProfileDao().deleteGameProfile(profile)

    suspend fun deleteGameProfileById(id: Long) =
        db.gameProfileDao().deleteGameProfileById(id)

    suspend fun getGameProfileById(id: Long): GameProfileEntity? =
        db.gameProfileDao().getGameProfileById(id)
}
