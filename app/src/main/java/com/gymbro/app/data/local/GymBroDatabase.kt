package com.gymbro.app.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.gymbro.app.data.local.converter.Converters
import com.gymbro.app.data.local.dao.ExerciseDao
import com.gymbro.app.data.local.dao.OneRmDao
import com.gymbro.app.data.local.dao.PersonalRecordDao
import com.gymbro.app.data.local.dao.SetLogDao
import com.gymbro.app.data.local.dao.TrainingDayDao
import com.gymbro.app.data.local.dao.UserProfileDao
import com.gymbro.app.data.local.dao.WorkoutPlanDao
import com.gymbro.app.data.local.dao.ExerciseMaxDao
import com.gymbro.app.data.local.entity.ExerciseEntity
import com.gymbro.app.data.local.entity.OneRmEntity
import com.gymbro.app.data.local.entity.PersonalRecordEntity
import com.gymbro.app.data.local.entity.SetLogEntity
import com.gymbro.app.data.local.entity.TrainingDayEntity
import com.gymbro.app.data.local.entity.TrainingDayExerciseEntity
import com.gymbro.app.data.local.entity.UserProfileEntity
import com.gymbro.app.data.local.entity.WorkoutPlanEntity
import com.gymbro.app.data.local.entity.ExerciseMaxEntity

@Database(
    entities = [
        ExerciseEntity::class,
        WorkoutPlanEntity::class,
        TrainingDayEntity::class,
        TrainingDayExerciseEntity::class,
        SetLogEntity::class,
        PersonalRecordEntity::class,
        UserProfileEntity::class,
        ExerciseMaxEntity::class,
        OneRmEntity::class,         
        // LevelProgressEntity УДАЛЕНА
    ],
    version = 3,                      // ← версия +1 после удаления таблицы
    exportSchema = true,
)
@TypeConverters(Converters::class)
abstract class GymBroDatabase : RoomDatabase() {

    abstract fun exerciseDao(): ExerciseDao
    abstract fun workoutPlanDao(): WorkoutPlanDao
    abstract fun trainingDayDao(): TrainingDayDao
    abstract fun setLogDao(): SetLogDao
    abstract fun personalRecordDao(): PersonalRecordDao
    abstract fun userProfileDao(): UserProfileDao
    abstract fun exerciseMaxDao(): ExerciseMaxDao
    abstract fun oneRmDao(): OneRmDao   

    companion object {
        const val DATABASE_NAME = "gymbro.db"
    }
}