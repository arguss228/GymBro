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

/**
 * Рассчитывает текущий уровень силы пользователя.
 *
 * Алгоритм:
 * 1. Для каждого из трёх лифтов (жим / присед / тяга) берём максимальный вес,
 *    сделанный хотя бы на 5 повторений за отчётное окно (по умолчанию 6 месяцев).
 * 2. Для каждого лифта определяем максимальный уровень из таблицы [LevelThresholds],
 *    целевой вес которого пользователь перекрыл.
 * 3. Общий уровень пользователя = **минимум** из трёх: нужно подтягивать
 *    отстающий лифт, а не только лучший. Это мотивирует к сбалансированной тренировке.
 * 4. Прогресс до следующего уровня считается по совокупному недобору кг
 *    по всем трём лифтам от текущего best-5RM до таргета уровня N+1.
 * 5. Если уровень вырос относительно последнего сохранённого — создаём запись
 *    в level_progress и триггерим анимацию повышения на Dashboard.
 *
 * Вызывается:
 *  — после завершения тренировки (в [com.gymbro.app.domain.usecase.LogSetUseCase]),
 *  — при первом открытии Dashboard,
 *  — после прохождения онбординга.
 */
@Singleton
class CalculateStrengthLevelUseCase @Inject constructor(
    private val progressRepo: ProgressRepository,
    private val levelRepo: LevelRepository,
) {

    /**
     * Пересчитывает уровень и, если нужно, сохраняет снимок в историю.
     * Возвращает актуальное состояние [StrengthLevel].
     */
    suspend operator fun invoke(): StrengthLevel {
        val profile = levelRepo.getProfile()
        val windowMonths = profile.levelWindowMonths.coerceIn(1, 24)
        val sinceMillis = System.currentTimeMillis() -
            TimeUnit.DAYS.toMillis(windowMonths * 30L)

        // 1. best 5RM для каждого из трёх лифтов в окне.
        //    Если истории нет — используем последний сохранённый snapshot из LevelProgressEntity
        //    (введённый при онбординге).
        val baseline = levelRepo.getLatestLevel()

        val best5RM: Map<BigThreeLift, Double> = BigThreeLift.values().associateWith { lift ->
            val fromHistory = progressRepo.getMax5Rm(lift.seedId, sinceMillis) ?: 0.0
            val fromBaseline = baseline?.let {
                when (lift) {
                    BigThreeLift.BENCH_PRESS -> it.bench5RmKg
                    BigThreeLift.BACK_SQUAT -> it.squat5RmKg
                    BigThreeLift.DEADLIFT -> it.deadlift5RmKg
                }
            } ?: 0.0
            maxOf(fromHistory, fromBaseline)
        }

        // 2. Максимальный достигнутый уровень по каждому лифту.
        val levelByLift: Map<BigThreeLift, Int> = best5RM.mapValues { (lift, weight) ->
            highestLevelFor(lift, weight)
        }

        // 3. Общий уровень = минимум.
        val overallLevel = levelByLift.values.min().coerceIn(1, StrengthLevel.MAX_LEVEL)

        // 4. Прогресс до следующего уровня.
        val progressToNext = computeProgressToNext(overallLevel, best5RM)
        val kgToNext = computeKgToNext(overallLevel, best5RM)

        val result = StrengthLevel(
            level = overallLevel,
            tier = LevelTier.of(overallLevel),
            progressToNext = progressToNext,
            kgToNextByLift = kgToNext,
            best5RM = best5RM,
        )

        // 5. Сохраняем новый уровень в историю, если он ИЗМЕНИЛСЯ.
        val latest = levelRepo.getLatestLevel()
        val levelChanged = latest == null || latest.level != overallLevel
        if (levelChanged) {
            levelRepo.recordLevel(
                LevelProgressEntity(
                    level = overallLevel,
                    bench5RmKg = best5RM[BigThreeLift.BENCH_PRESS] ?: 0.0,
                    squat5RmKg = best5RM[BigThreeLift.BACK_SQUAT] ?: 0.0,
                    deadlift5RmKg = best5RM[BigThreeLift.DEADLIFT] ?: 0.0,
                    // Анимацию показываем только при ПОВЫШЕНИИ уровня.
                    celebrationShown = (latest?.level ?: 0) >= overallLevel,
                )
            )
        }

        return result
    }

    /**
     * Максимальный уровень, у которого целевой вес ×5 <= [bestWeight5Rm].
     * Если вес меньше целевого для первого уровня — возвращаем 1 (стартовый).
     */
    private fun highestLevelFor(lift: BigThreeLift, bestWeight5Rm: Double): Int {
        if (bestWeight5Rm <= 0.0) return 1
        // Идём с 15 вниз до 1 и возвращаем первый уровень, цель которого взята.
        for (level in StrengthLevel.MAX_LEVEL downTo 1) {
            val target = LevelThresholds.targetFor(level, lift)
            if (bestWeight5Rm >= target) return level
        }
        return 1
    }

    /**
     * Прогресс к следующему уровню, 0f..1f.
     * Считается через совокупный прогресс по трём лифтам:
     *   сумма(текущий - цель_текущего_уровня)_i
     *   ───────────────────────────────────────────
     *   сумма(цель_след_уровня - цель_текущего_уровня)_i
     *
     * Такой подход честно отражает «сколько осталось сделать» и сглаживает
     * ситуацию, когда один лифт уже в следующем уровне, а два других отстают.
     */
    private fun computeProgressToNext(
        currentLevel: Int,
        best5RM: Map<BigThreeLift, Double>
    ): Float {
        if (currentLevel >= StrengthLevel.MAX_LEVEL) return 1f

        val current = LevelThresholds.targetsFor(currentLevel)
        val next = LevelThresholds.targetsFor(currentLevel + 1)

        var gained = 0.0
        var required = 0.0
        BigThreeLift.values().forEach { lift ->
            val best = best5RM[lift] ?: 0.0
            val curTarget = when (lift) {
                BigThreeLift.BENCH_PRESS -> current.bench
                BigThreeLift.BACK_SQUAT -> current.squat
                BigThreeLift.DEADLIFT -> current.deadlift
            }
            val nextTarget = when (lift) {
                BigThreeLift.BENCH_PRESS -> next.bench
                BigThreeLift.BACK_SQUAT -> next.squat
                BigThreeLift.DEADLIFT -> next.deadlift
            }
            // clamp: не даём отрицательных значений (если вдруг best < curTarget из-за rounding
            // при переходе уровня).
            val progress = (best - curTarget).coerceAtLeast(0.0)
            val step = (nextTarget - curTarget).coerceAtLeast(0.01)
            gained += progress.coerceAtMost(step)
            required += step
        }

        if (required <= 0.0) return 0f
        return (gained / required).toFloat().coerceIn(0f, 1f)
    }

    /**
     * Сколько кг осталось добавить к best 5RM по каждому лифту,
     * чтобы достичь следующего уровня. 0, если уже перекрыто.
     */
    private fun computeKgToNext(
        currentLevel: Int,
        best5RM: Map<BigThreeLift, Double>
    ): Map<BigThreeLift, Double> {
        if (currentLevel >= StrengthLevel.MAX_LEVEL) {
            return BigThreeLift.values().associateWith { 0.0 }
        }
        val next = LevelThresholds.targetsFor(currentLevel + 1)
        return BigThreeLift.values().associateWith { lift ->
            val best = best5RM[lift] ?: 0.0
            val target = when (lift) {
                BigThreeLift.BENCH_PRESS -> next.bench
                BigThreeLift.BACK_SQUAT -> next.squat
                BigThreeLift.DEADLIFT -> next.deadlift
            }
            (target - best).coerceAtLeast(0.0)
        }
    }
}
