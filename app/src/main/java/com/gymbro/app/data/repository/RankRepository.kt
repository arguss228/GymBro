package com.gymbro.app.data.repository

import com.gymbro.app.data.local.dao.OneRmDao
import com.gymbro.app.data.local.dao.UserProfileDao
import com.gymbro.app.data.local.entity.OneRmEntity
import com.gymbro.app.domain.model.StrengthRank
import com.gymbro.app.domain.model.StrengthRanks
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

data class RankState(
    val bench: Double            = 0.0,
    val squat: Double            = 0.0,
    val deadlift: Double         = 0.0,
    val currentRank: StrengthRank = StrengthRanks.all.first(),
    val nextRank: StrengthRank?   = StrengthRanks.all.getOrNull(1),
    val progress: Float           = 0f,
    val kgToBench: Double         = 0.0,
    val kgToSquat: Double         = 0.0,
    val kgToDeadlift: Double      = 0.0,
    val hasData: Boolean          = false,
)

@Singleton
class RankRepository @Inject constructor(
    private val oneRmDao: OneRmDao,
    private val profileDao: UserProfileDao,
) {
    fun observeRankState(): Flow<RankState> = oneRmDao.observe().map { entity ->
        if (entity == null ||
            (entity.benchKg == 0.0 && entity.squatKg == 0.0 && entity.deadliftKg == 0.0)
        ) return@map RankState(hasData = false)

        buildRankState(entity.benchKg, entity.squatKg, entity.deadliftKg)
    }

    suspend fun save1Rm(bench: Double, squat: Double, deadlift: Double) {
        oneRmDao.upsert(OneRmEntity(benchKg = bench, squatKg = squat, deadliftKg = deadlift))
    }

    /**
     * Обновляет 1RM по каждому лифту — берём максимум из текущего и нового.
     * Возвращает новый ранг если он повысился, иначе null.
     */
    suspend fun updateIfBetter(bench: Double?, squat: Double?, deadlift: Double?): StrengthRank? {
        val current = oneRmDao.get() ?: OneRmEntity()
        val prevRank = StrengthRanks.currentRankFor(
            current.benchKg, current.squatKg, current.deadliftKg
        )
        val newBench    = maxOf(current.benchKg,    bench    ?: 0.0)
        val newSquat    = maxOf(current.squatKg,    squat    ?: 0.0)
        val newDeadlift = maxOf(current.deadliftKg, deadlift ?: 0.0)

        oneRmDao.upsert(OneRmEntity(benchKg = newBench, squatKg = newSquat, deadliftKg = newDeadlift))

        val newRank = StrengthRanks.currentRankFor(newBench, newSquat, newDeadlift)
        return if (newRank.name != prevRank.name) newRank else null
    }

    suspend fun hasData(): Boolean {
        val e = oneRmDao.get() ?: return false
        return e.benchKg > 0.0 || e.squatKg > 0.0 || e.deadliftKg > 0.0
    }

    suspend fun isOnboardingDone(): Boolean =
        profileDao.get()?.onboardingCompleted == true

    suspend fun completeOnboarding() {
        val profile = profileDao.get()
            ?: com.gymbro.app.data.local.entity.UserProfileEntity()
        profileDao.upsert(profile.copy(onboardingCompleted = true))
    }

    private fun buildRankState(b: Double, s: Double, d: Double): RankState {
        val cur = StrengthRanks.currentRankFor(b, s, d)
        val nxt = StrengthRanks.nextRank(cur)
        val (kb, ks, kd) = StrengthRanks.kgToNext(b, s, d)
        return RankState(
            bench        = b, squat = s, deadlift = d,
            currentRank  = cur, nextRank = nxt,
            progress     = StrengthRanks.progressToNext(b, s, d),
            kgToBench    = kb, kgToSquat = ks, kgToDeadlift = kd,
            hasData      = true,
        )
    }
}