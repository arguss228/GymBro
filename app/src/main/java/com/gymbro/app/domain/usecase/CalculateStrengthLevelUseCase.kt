package com.gymbro.app.domain.usecase

import com.gymbro.app.data.local.entity.LevelProgressEntity
import com.gymbro.app.data.repository.LevelRepository
import com.gymbro.app.data.repository.ProgressRepository
import com.gymbro.app.data.seed.LevelThresholds
import com.gymbro.app.domain.model.BigThreeLift
import com.gymbro.app.domain.model.LevelTier
import com.gymbro.app.domain.model.StrengthLevel
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CalculateStrengthLevelUseCase @Inject constructor(
    private val progressRepo: ProgressRepository,
    private val levelRepo: LevelRepository,
) {

    suspend operator fun invoke(): StrengthLevel {
        val profile = levelRepo.getProfile()
        val windowMonths = profile.levelWindowMonths.coerceIn(1, 24)
        val sinceMillis = System.currentTimeMillis() -
                TimeUnit.DAYS.toMillis(windowMonths * 30L)

        val baseline = levelRepo.getLatestLevel()

        val best5RM: Map<BigThreeLift, Double> = BigThreeLift.values().associateWith { lift ->
            val fromHistory = progressRepo.getMax5Rm(lift.seedId, sinceMillis) ?: 0.0
            val fromBaseline = baseline?.let {
                when (lift) {
                    BigThreeLift.BENCH_PRESS -> it.bench5RmKg
                    BigThreeLift.BACK_SQUAT  -> it.squat5RmKg
                    BigThreeLift.DEADLIFT    -> it.deadlift5RmKg
                }
            } ?: 0.0
            maxOf(fromHistory, fromBaseline)
        }

        val levelByLift: Map<BigThreeLift, Int> = best5RM.mapValues { (lift, weight) ->
            highestLevelFor(lift, weight)
        }

        val overallLevel = levelByLift.values.min().coerceIn(1, StrengthLevel.MAX_LEVEL)
        val progressToNext = computeProgressToNext(overallLevel, best5RM)
        val kgToNext = computeKgToNext(overallLevel, best5RM)

        val result = StrengthLevel(
            level = overallLevel,
            tier = LevelTier.of(overallLevel),
            progressToNext = progressToNext,
            kgToNextByLift = kgToNext,
            best1RM = best5RM,  // исправлено: было best1RM
        )

        val latest = levelRepo.getLatestLevel()
        val shouldRecord = latest == null || latest.level != overallLevel

        if (shouldRecord) {
            val previousLevel = latest?.level ?: 0
            levelRepo.recordLevel(
                LevelProgressEntity(
                    level = overallLevel,
                    bench5RmKg    = best5RM[BigThreeLift.BENCH_PRESS] ?: 0.0,
                    squat5RmKg    = best5RM[BigThreeLift.BACK_SQUAT]  ?: 0.0,
                    deadlift5RmKg = best5RM[BigThreeLift.DEADLIFT]    ?: 0.0,
                    celebrationShown = previousLevel >= overallLevel,
                )
            )
        }

        return result
    }

    private fun highestLevelFor(lift: BigThreeLift, bestWeight5Rm: Double): Int {
        if (bestWeight5Rm <= 0.0) return 1
        for (level in StrengthLevel.MAX_LEVEL downTo 1) {
            val target = LevelThresholds.targetFor(level, lift)
            if (bestWeight5Rm >= target) return level
        }
        return 1
    }

    private fun computeProgressToNext(
        currentLevel: Int,
        best5RM: Map<BigThreeLift, Double>,
    ): Float {
        if (currentLevel >= StrengthLevel.MAX_LEVEL) return 1f

        val current = LevelThresholds.targetsFor(currentLevel)
        val next    = LevelThresholds.targetsFor(currentLevel + 1)

        var gained   = 0.0
        var required = 0.0
        BigThreeLift.values().forEach { lift ->
            val best      = best5RM[lift] ?: 0.0
            val curTarget = when (lift) {
                BigThreeLift.BENCH_PRESS -> current.bench
                BigThreeLift.BACK_SQUAT  -> current.squat
                BigThreeLift.DEADLIFT    -> current.deadlift
            }
            val nextTarget = when (lift) {
                BigThreeLift.BENCH_PRESS -> next.bench
                BigThreeLift.BACK_SQUAT  -> next.squat
                BigThreeLift.DEADLIFT    -> next.deadlift
            }
            val progress = (best - curTarget).coerceAtLeast(0.0)
            val step     = (nextTarget - curTarget).coerceAtLeast(0.01)
            gained   += progress.coerceAtMost(step)
            required += step
        }

        if (required <= 0.0) return 0f
        return (gained / required).toFloat().coerceIn(0f, 1f)
    }

    private fun computeKgToNext(
        currentLevel: Int,
        best5RM: Map<BigThreeLift, Double>,
    ): Map<BigThreeLift, Double> {
        if (currentLevel >= StrengthLevel.MAX_LEVEL) {
            return BigThreeLift.values().associateWith { 0.0 }
        }
        val next = LevelThresholds.targetsFor(currentLevel + 1)
        return BigThreeLift.values().associateWith { lift ->
            val best   = best5RM[lift] ?: 0.0
            val target = when (lift) {
                BigThreeLift.BENCH_PRESS -> next.bench
                BigThreeLift.BACK_SQUAT  -> next.squat
                BigThreeLift.DEADLIFT    -> next.deadlift
            }
            (target - best).coerceAtLeast(0.0)
        }
    }
}