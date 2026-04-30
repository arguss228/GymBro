package com.gymbro.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.gymbro.app.data.local.entity.PersonalRecordEntity
import com.gymbro.app.data.local.entity.PrType
import kotlinx.coroutines.flow.Flow

@Dao
interface PersonalRecordDao {

    @Query("SELECT * FROM personal_records ORDER BY achieved_at DESC")
    fun observeAll(): Flow<List<PersonalRecordEntity>>

    @Query("SELECT * FROM personal_records WHERE exercise_id = :exerciseId")
    fun observeForExercise(exerciseId: Long): Flow<List<PersonalRecordEntity>>

    @Query("SELECT * FROM personal_records WHERE exercise_id = :exerciseId AND type = :type LIMIT 1")
    suspend fun getPr(exerciseId: Long, type: PrType): PersonalRecordEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(pr: PersonalRecordEntity): Long
}