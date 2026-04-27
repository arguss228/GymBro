package com.gymbro.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.gymbro.app.data.local.entity.LevelProgressEntity
import com.gymbro.app.data.local.entity.PersonalRecordEntity
import com.gymbro.app.data.local.entity.PrType
import com.gymbro.app.data.local.entity.UserProfileEntity
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

@Dao
interface UserProfileDao {

    @Query("SELECT * FROM user_profile WHERE id = :id LIMIT 1")
    fun observe(id: Long = UserProfileEntity.SINGLETON_ID): Flow<UserProfileEntity?>

    @Query("SELECT * FROM user_profile WHERE id = :id LIMIT 1")
    suspend fun get(id: Long = UserProfileEntity.SINGLETON_ID): UserProfileEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(profile: UserProfileEntity)
}
