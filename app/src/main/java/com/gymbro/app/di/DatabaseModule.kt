package com.gymbro.app.di

import android.content.Context
import androidx.room.Room
import com.gymbro.app.data.local.GymBroDatabase
import com.gymbro.app.data.local.dao.ExerciseDao
import com.gymbro.app.data.local.dao.OneRmDao
import com.gymbro.app.data.local.dao.PersonalRecordDao
import com.gymbro.app.data.local.dao.SetLogDao
import com.gymbro.app.data.local.dao.TrainingDayDao
import com.gymbro.app.data.local.dao.UserProfileDao
import com.gymbro.app.data.local.dao.WorkoutPlanDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): GymBroDatabase =
        Room.databaseBuilder(context, GymBroDatabase::class.java, GymBroDatabase.DATABASE_NAME)
            .fallbackToDestructiveMigration()   // для MVP — дроп при смене версии
            .build()

    @Provides fun provideExerciseDao(db: GymBroDatabase): ExerciseDao         = db.exerciseDao()
    @Provides fun provideWorkoutPlanDao(db: GymBroDatabase): WorkoutPlanDao   = db.workoutPlanDao()
    @Provides fun provideTrainingDayDao(db: GymBroDatabase): TrainingDayDao   = db.trainingDayDao()
    @Provides fun provideSetLogDao(db: GymBroDatabase): SetLogDao             = db.setLogDao()
    @Provides fun providePersonalRecordDao(db: GymBroDatabase): PersonalRecordDao = db.personalRecordDao()
    @Provides fun provideUserProfileDao(db: GymBroDatabase): UserProfileDao   = db.userProfileDao()
    @Provides fun provideOneRmDao(db: GymBroDatabase): OneRmDao               = db.oneRmDao()
    @Provides fun provideExerciseMaxDao(db: GymBroDatabase): ExerciseMaxDao   = db.exerciseMaxDao()

    @Provides
    @Singleton
    fun provideApplicationScope(): CoroutineScope =
        CoroutineScope(SupervisorJob() + Dispatchers.Default)
}