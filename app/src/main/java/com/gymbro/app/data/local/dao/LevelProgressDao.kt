package com.gymbro.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.gymbro.app.data.local.entity.LevelProgressEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface LevelProgressDao {

    @Query("SELECT * FROM level_progress ORDER BY achieved_at DESC")
    fun observeAll(): Flow<List<LevelProgressEntity>>

    @Query("SELECT * FROM level_progress ORDER BY achieved_at DESC LIMIT 1")
    fun observeLatest(): Flow<LevelProgressEntity?>

    @Query("SELECT * FROM level_progress ORDER BY achieved_at DESC LIMIT 1")
    suspend fun getLatest(): LevelProgressEntity?

    @Insert
    suspend fun insert(entry: LevelProgressEntity): Long

    @Update
    suspend fun update(entry: LevelProgressEntity)

    @Query("UPDATE level_progress SET celebration_shown = 1 WHERE id = :id")
    suspend fun markCelebrationShown(id: Long)

    @Query("SELECT * FROM level_progress WHERE celebration_shown = 0 ORDER BY achieved_at DESC LIMIT 1")
    fun observePendingCelebration(): Flow<LevelProgressEntity?>
}