package com.gymbro.app.data.repository

import com.gymbro.app.data.local.dao.PersonalRecordDao
import com.gymbro.app.data.local.dao.SetLogDao
import com.gymbro.app.data.local.entity.PersonalRecordEntity
import com.gymbro.app.data.local.entity.PrType
import com.gymbro.app.data.local.entity.SetLogEntity
import com.gymbro.app.domain.model.OneRmFormula
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProgressRepository @Inject constructor(
    private val setLogDao: SetLogDao,
    private val prDao: PersonalRecordDao,
) {
    fun observeSession(sessionId: Long): Flow<List<SetLogEntity>> =
        setLogDao.observeSession(sessionId)

    fun observeExerciseHistory(exerciseId: Long): Flow<List<SetLogEntity>> =
        setLogDao.observeForExercise(exerciseId)

    /**
     * Реактивный поток подходов для упражнения за указанный период (0L = всё время).
     * Используется для построения графика исторических максимумов.
     */
    fun observeExerciseHistorySince(exerciseId: Long, sinceMillis: Long): Flow<List<SetLogEntity>> =
        setLogDao.observeForExerciseSince(exerciseId, sinceMillis)

    fun observeAllPersonalRecords(): Flow<List<PersonalRecordEntity>> = prDao.observeAll()

    fun observeTotalSessions(): Flow<Int> = setLogDao.observeTotalSessions()

    suspend fun getMax5Rm(exerciseId: Long, sinceMillis: Long): Double? =
        setLogDao.getMaxWeightForReps(exerciseId, minReps = 5, sinceMillis = sinceMillis)

    suspend fun getMaxEstimated1Rm(exerciseId: Long, sinceMillis: Long): Double? =
        setLogDao.getMaxEstimated1Rm(exerciseId, sinceMillis)

    suspend fun getNextSessionId(): Long = (setLogDao.getLastSessionId() ?: 0L) + 1L

    /**
     * Логирует подход и пересчитывает личные рекорды.
     * Возвращает список типов PR, которые были обновлены (для показа поздравлений).
     */
    suspend fun logSet(
        sessionId: Long,
        exerciseId: Long,
        setNumber: Int,
        weightKg: Double,
        reps: Int,
        rpe: Double? = null,
        isWarmup: Boolean = false,
        performedAt: Long = System.currentTimeMillis(),
    ): LogSetResult {
        val estimated1Rm = OneRmFormula.epley(weightKg, reps)

        val logId = setLogDao.insert(
            SetLogEntity(
                sessionId = sessionId,
                exerciseId = exerciseId,
                setNumber = setNumber,
                weightKg = weightKg,
                reps = reps,
                rpe = rpe,
                isWarmup = isWarmup,
                performedAt = performedAt,
                estimated1Rm = estimated1Rm,
            )
        )

        if (isWarmup) return LogSetResult(logId, emptyList())

        val newPrs = mutableListOf<PrType>()

        // 1RM PR
        val current1Rm = prDao.getPr(exerciseId, PrType.ONE_RM)
        if (current1Rm == null || estimated1Rm > current1Rm.estimated1Rm) {
            prDao.upsert(
                PersonalRecordEntity(
                    id = current1Rm?.id ?: 0L,
                    exerciseId = exerciseId,
                    type = PrType.ONE_RM,
                    weightKg = weightKg,
                    reps = reps,
                    estimated1Rm = estimated1Rm,
                    setLogId = logId,
                    achievedAt = performedAt,
                )
            )
            newPrs += PrType.ONE_RM
        }

        // 5RM PR — считается только для подходов c reps >= 5
        if (reps >= 5) {
            val current5Rm = prDao.getPr(exerciseId, PrType.FIVE_RM)
            if (current5Rm == null || weightKg > current5Rm.weightKg) {
                prDao.upsert(
                    PersonalRecordEntity(
                        id = current5Rm?.id ?: 0L,
                        exerciseId = exerciseId,
                        type = PrType.FIVE_RM,
                        weightKg = weightKg,
                        reps = reps,
                        estimated1Rm = estimated1Rm,
                        setLogId = logId,
                        achievedAt = performedAt,
                    )
                )
                newPrs += PrType.FIVE_RM
            }
        }

        return LogSetResult(logId, newPrs)
    }

    data class LogSetResult(
        val setLogId: Long,
        val newPrTypes: List<PrType>,
    )
}