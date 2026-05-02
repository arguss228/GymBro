package com.gymbro.app.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.gymbro.app.data.local.converter.Converters
import com.gymbro.app.data.local.dao.ExerciseDao
import com.gymbro.app.data.local.dao.ExerciseMaxDao
import com.gymbro.app.data.local.dao.OneRmDao
import com.gymbro.app.data.local.dao.PersonalRecordDao
import com.gymbro.app.data.local.dao.SetLogDao
import com.gymbro.app.data.local.dao.TrainingDayDao
import com.gymbro.app.data.local.dao.UserProfileDao
import com.gymbro.app.data.local.dao.WorkoutPlanDao
import com.gymbro.app.data.local.entity.ExerciseEntity
import com.gymbro.app.data.local.entity.ExerciseMaxEntity
import com.gymbro.app.data.local.entity.OneRmEntity
import com.gymbro.app.data.local.entity.PersonalRecordEntity
import com.gymbro.app.data.local.entity.SetLogEntity
import com.gymbro.app.data.local.entity.TrainingDayEntity
import com.gymbro.app.data.local.entity.TrainingDayExerciseEntity
import com.gymbro.app.data.local.entity.UserProfileEntity
import com.gymbro.app.data.local.entity.WorkoutPlanEntity

@Database(
    entities = [
        ExerciseEntity::class,
        WorkoutPlanEntity::class,
        TrainingDayEntity::class,
        TrainingDayExerciseEntity::class,
        SetLogEntity::class,
        PersonalRecordEntity::class,
        UserProfileEntity::class,
        OneRmEntity::class,
        ExerciseMaxEntity::class,
    ],
    version = 3,
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
    abstract fun oneRmDao(): OneRmDao
    abstract fun exerciseMaxDao(): ExerciseMaxDao

    companion object {
        const val DATABASE_NAME = "gymbro.db"
    }
}