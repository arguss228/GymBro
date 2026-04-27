package com.gymbro.app.data.repository

import com.gymbro.app.data.local.dao.LevelProgressDao
import com.gymbro.app.data.local.dao.UserProfileDao
import com.gymbro.app.data.local.entity.LevelProgressEntity
import com.gymbro.app.data.local.entity.UserProfileEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LevelRepository @Inject constructor(
    private val levelDao: LevelProgressDao,
    private val profileDao: UserProfileDao,
) {
    fun observeLatestLevel(): Flow<LevelProgressEntity?> = levelDao.observeLatest()

    fun observePendingCelebration(): Flow<LevelProgressEntity?> =
        levelDao.observePendingCelebration()

    fun observeHistory(): Flow<List<LevelProgressEntity>> = levelDao.observeAll()

    fun observeProfile(): Flow<UserProfileEntity?> = profileDao.observe()

    suspend fun getProfile(): UserProfileEntity =
        profileDao.get() ?: UserProfileEntity().also { profileDao.upsert(it) }

    suspend fun getLatestLevel(): LevelProgressEntity? = levelDao.getLatest()

    suspend fun recordLevel(entry: LevelProgressEntity): Long = levelDao.insert(entry)

    suspend fun markCelebrationShown(id: Long) = levelDao.markCelebrationShown(id)

    suspend fun completeOnboarding(
        initialBench5Rm: Double?,
        initialSquat5Rm: Double?,
        initialDeadlift5Rm: Double?,
    ) {
        val profile = getProfile().copy(onboardingCompleted = true)
        profileDao.upsert(profile)
        // Начальные веса не сохраняем как SetLog — они попадут в лог при первой тренировке.
        // Но для стартового уровня используем их как snapshot, если пользователь ввёл.
        if (initialBench5Rm != null && initialSquat5Rm != null && initialDeadlift5Rm != null) {
            levelDao.insert(
                LevelProgressEntity(
                    level = 1, // будет пересчитано CalculateStrengthLevelUseCase'ом
                    bench5RmKg = initialBench5Rm,
                    squat5RmKg = initialSquat5Rm,
                    deadlift5RmKg = initialDeadlift5Rm,
                    celebrationShown = true, // стартовый — без анимации
                )
            )
        }
    }
}
