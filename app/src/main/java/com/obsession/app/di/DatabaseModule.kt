package com.obsession.app.di

import android.content.Context
import androidx.room.Room
import com.obsession.app.data.local.ObsessionDatabase
import com.obsession.app.data.local.dao.ExerciseDao
import com.obsession.app.data.local.dao.ExerciseMaxDao
import com.obsession.app.data.local.dao.OneRmDao
import com.obsession.app.data.local.dao.PersonalRecordDao
import com.obsession.app.data.local.dao.SetLogDao
import com.obsession.app.data.local.dao.TrainingDayDao
import com.obsession.app.data.local.dao.UserProfileDao
import com.obsession.app.data.local.dao.WorkoutPlanDao
import com.obsession.app.data.local.dao.WorkoutSessionDao
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
    fun provideDatabase(@ApplicationContext context: Context): ObsessionDatabase =
        Room.databaseBuilder(context, ObsessionDatabase::class.java, ObsessionDatabase.DATABASE_NAME)
            .fallbackToDestructiveMigration()
            .build()

    @Provides fun provideExerciseDao(db: ObsessionDatabase): ExerciseDao             = db.exerciseDao()
    @Provides fun provideWorkoutPlanDao(db: ObsessionDatabase): WorkoutPlanDao       = db.workoutPlanDao()
    @Provides fun provideTrainingDayDao(db: ObsessionDatabase): TrainingDayDao       = db.trainingDayDao()
    @Provides fun provideSetLogDao(db: ObsessionDatabase): SetLogDao                 = db.setLogDao()
    @Provides fun providePersonalRecordDao(db: ObsessionDatabase): PersonalRecordDao = db.personalRecordDao()
    @Provides fun provideUserProfileDao(db: ObsessionDatabase): UserProfileDao       = db.userProfileDao()
    @Provides fun provideOneRmDao(db: ObsessionDatabase): OneRmDao                   = db.oneRmDao()
    @Provides fun provideExerciseMaxDao(db: ObsessionDatabase): ExerciseMaxDao       = db.exerciseMaxDao()
    @Provides fun provideWorkoutSessionDao(db: ObsessionDatabase): WorkoutSessionDao = db.workoutSessionDao()

    @Provides
    @Singleton
    fun provideApplicationScope(): CoroutineScope =
        CoroutineScope(SupervisorJob() + Dispatchers.Default)
}