package com.example.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface CrosshairPresetDao {
    @Query("SELECT * FROM crosshair_presets ORDER BY id ASC")
    fun getAllPresets(): Flow<List<CrosshairPresetEntity>>

    @Query("SELECT * FROM crosshair_presets WHERE id = :id LIMIT 1")
    suspend fun getPresetById(id: Int): CrosshairPresetEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPreset(preset: CrosshairPresetEntity): Long

    @Update
    suspend fun updatePreset(preset: CrosshairPresetEntity)

    @Delete
    suspend fun deletePreset(preset: CrosshairPresetEntity)

    @Query("DELETE FROM crosshair_presets WHERE id = :id")
    suspend fun deletePresetById(id: Int)
}

@Dao
interface SensitivityProfileDao {
    @Query("SELECT * FROM sensitivity_profiles ORDER BY id ASC")
    fun getAllProfiles(): Flow<List<SensitivityProfileEntity>>

    @Query("SELECT * FROM sensitivity_profiles WHERE id = :id LIMIT 1")
    suspend fun getProfileById(id: Int): SensitivityProfileEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProfile(profile: SensitivityProfileEntity): Long

    @Update
    suspend fun updateProfile(profile: SensitivityProfileEntity)

    @Delete
    suspend fun deleteProfile(profile: SensitivityProfileEntity)

    @Query("DELETE FROM sensitivity_profiles WHERE id = :id")
    suspend fun deleteProfileById(id: Int)
}

@Dao
interface GameProfileDao {
    @Query("SELECT * FROM game_profiles ORDER BY lastLaunchedTimestamp DESC")
    fun getAllGameProfiles(): Flow<List<GameProfileEntity>>

    @Query("SELECT * FROM game_profiles WHERE id = :id LIMIT 1")
    suspend fun getGameProfileById(id: Long): GameProfileEntity?

    @Query("SELECT * FROM game_profiles WHERE packageName = :packageName")
    fun getGameProfilesByPackage(packageName: String): Flow<List<GameProfileEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGameProfile(profile: GameProfileEntity): Long

    @Update
    suspend fun updateGameProfile(profile: GameProfileEntity)

    @Delete
    suspend fun deleteGameProfile(profile: GameProfileEntity)

    @Query("DELETE FROM game_profiles WHERE id = :id")
    suspend fun deleteGameProfileById(id: Long)
}
