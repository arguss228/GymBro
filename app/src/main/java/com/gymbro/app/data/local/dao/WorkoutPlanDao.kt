package com.gymbro.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.gymbro.app.data.local.entity.WorkoutPlanEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface WorkoutPlanDao {

    @Query("SELECT * FROM workout_plans ORDER BY isPreset DESC, minLevel ASC, name ASC")
    fun observeAll(): Flow<List<WorkoutPlanEntity>>

    @Query("SELECT * FROM workout_plans WHERE minLevel <= :userLevel ORDER BY minLevel DESC, name ASC")
    fun observeAvailableFor(userLevel: Int): Flow<List<WorkoutPlanEntity>>

    @Query("SELECT * FROM workout_plans WHERE isActive = 1 LIMIT 1")
    fun observeActive(): Flow<WorkoutPlanEntity?>

    @Query("SELECT * FROM workout_plans WHERE id = :id")
    suspend fun getById(id: Long): WorkoutPlanEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(plan: WorkoutPlanEntity): Long

    @Update
    suspend fun update(plan: WorkoutPlanEntity)

    @Transaction
    suspend fun setActive(planId: Long) {
        clearActive()
        setActiveFlag(planId)
    }

    @Query("UPDATE workout_plans SET isActive = 0")
    suspend fun clearActive()

    @Query("UPDATE workout_plans SET isActive = 1 WHERE id = :planId")
    suspend fun setActiveFlag(planId: Long)

    @Query("DELETE FROM workout_plans WHERE id = :id AND isPreset = 0")
    suspend fun deleteUserPlan(id: Long): Int

    @Query("SELECT COUNT(*) FROM workout_plans WHERE isPreset = 1")
    suspend fun presetCount(): Int
}