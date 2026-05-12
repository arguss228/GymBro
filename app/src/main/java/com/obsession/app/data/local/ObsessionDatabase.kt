package com.obsession.app.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.obsession.app.data.local.converter.Converters
import com.obsession.app.data.local.dao.ExerciseDao
import com.obsession.app.data.local.dao.ExerciseMaxDao
import com.obsession.app.data.local.dao.OneRmDao
import com.obsession.app.data.local.dao.PersonalRecordDao
import com.obsession.app.data.local.dao.SetLogDao
import com.obsession.app.data.local.dao.TrainingDayDao
import com.obsession.app.data.local.dao.UserProfileDao
import com.obsession.app.data.local.dao.WorkoutPlanDao
import com.obsession.app.data.local.entity.ExerciseEntity
import com.obsession.app.data.local.entity.ExerciseMaxEntity
import com.obsession.app.data.local.entity.OneRmEntity
import com.obsession.app.data.local.entity.PersonalRecordEntity
import com.obsession.app.data.local.entity.SetLogEntity
import com.obsession.app.data.local.entity.TrainingDayEntity
import com.obsession.app.data.local.entity.TrainingDayExerciseEntity
import com.obsession.app.data.local.entity.UserProfileEntity
import com.obsession.app.data.local.entity.WorkoutPlanEntity

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
    version = 4,
    exportSchema = true,
)
@TypeConverters(Converters::class)
abstract class ObsessionDatabase : RoomDatabase() {

    abstract fun exerciseDao(): ExerciseDao
    abstract fun workoutPlanDao(): WorkoutPlanDao
    abstract fun trainingDayDao(): TrainingDayDao
    abstract fun setLogDao(): SetLogDao
    abstract fun personalRecordDao(): PersonalRecordDao
    abstract fun userProfileDao(): UserProfileDao
    abstract fun oneRmDao(): OneRmDao
    abstract fun exerciseMaxDao(): ExerciseMaxDao

    companion object {
        const val DATABASE_NAME = "obsession.db"
    }
}