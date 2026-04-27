package com.gymbro.app.data.seed

import com.gymbro.app.data.local.entity.TrainingDayEntity
import com.gymbro.app.data.local.entity.TrainingDayExerciseEntity
import com.gymbro.app.data.local.entity.WorkoutPlanEntity
import com.gymbro.app.domain.model.BigThreeLift

/**
 * Готовые планы, которые создаются в БД при первом запуске.
 * Разделены по уровню подготовки (Новичок / Средний / Опытный).
 *
 * Внутри каждой сборки — план + его дни + упражнения.
 * ID упражнений берутся через [ExerciseRegistry.idFor], т.к. для не-Big-Three
 * ID автогенерируются при вставке seed-данных.
 */
object WorkoutPlanSeed {

    data class PlanBundle(
        val plan: WorkoutPlanEntity,
        /** Лямбда строит список дней и их упражнений, получая маппинг имя→id из seed. */
        val buildDays: (ExerciseResolver) -> List<DayBundle>
    )

    data class DayBundle(
        val day: TrainingDayEntity,
        val exercises: List<TrainingDayExerciseEntity>,
    )

    /** Резолвер для получения ID упражнений по имени (заполняется в SeedRunner). */
    fun interface ExerciseResolver {
        fun idOf(name: String): Long
    }

    val plans: List<PlanBundle> = listOf(

        // ============ НОВИЧОК — Full Body 3x/неделю ============
        PlanBundle(
            plan = WorkoutPlanEntity(
                name = "Full Body для новичка",
                description = "Три тренировки в неделю на всё тело. Фокус на базовые движения и прогрессию весов.",
                minLevel = 1,
                daysPerWeek = 3,
                isPreset = true,
            ),
            buildDays = { r ->
                listOf(
                    DayBundle(
                        day = TrainingDayEntity(planId = 0, name = "День A", orderIndex = 0),
                        exercises = listOf(
                            dayEx(BigThreeLift.BACK_SQUAT.seedId, 0, 3, 5, 180),
                            dayEx(BigThreeLift.BENCH_PRESS.seedId, 1, 3, 5, 180),
                            dayEx(r.idOf("Тяга штанги в наклоне"), 2, 3, 8, 120),
                            dayEx(r.idOf("Планка"), 3, 3, 1, 60, notes = "60 секунд"),
                        )
                    ),
                    DayBundle(
                        day = TrainingDayEntity(planId = 0, name = "День B", orderIndex = 1),
                        exercises = listOf(
                            dayEx(BigThreeLift.DEADLIFT.seedId, 0, 3, 5, 180),
                            dayEx(r.idOf("Армейский жим"), 1, 3, 5, 150),
                            dayEx(r.idOf("Подтягивания"), 2, 3, 8, 120),
                            dayEx(r.idOf("Подъём штанги на бицепс"), 3, 3, 10, 90),
                        )
                    ),
                    DayBundle(
                        day = TrainingDayEntity(planId = 0, name = "День C", orderIndex = 2),
                        exercises = listOf(
                            dayEx(BigThreeLift.BACK_SQUAT.seedId, 0, 3, 5, 180),
                            dayEx(BigThreeLift.BENCH_PRESS.seedId, 1, 3, 5, 180),
                            dayEx(r.idOf("Тяга верхнего блока"), 2, 3, 10, 120),
                            dayEx(r.idOf("Скручивания"), 3, 3, 15, 60),
                        )
                    ),
                )
            }
        ),

        // ============ СРЕДНИЙ — Upper/Lower 4x/неделю ============
        PlanBundle(
            plan = WorkoutPlanEntity(
                name = "Upper/Lower Split",
                description = "Четырёхдневный сплит верх/низ. Хорошо подходит для среднего уровня — больше объёма и разнообразия.",
                minLevel = 5,
                daysPerWeek = 4,
                isPreset = true,
            ),
            buildDays = { r ->
                listOf(
                    DayBundle(
                        day = TrainingDayEntity(planId = 0, name = "Верх — сила", orderIndex = 0),
                        exercises = listOf(
                            dayEx(BigThreeLift.BENCH_PRESS.seedId, 0, 4, 5, 180),
                            dayEx(r.idOf("Тяга штанги в наклоне"), 1, 4, 6, 150),
                            dayEx(r.idOf("Армейский жим"), 2, 3, 6, 150),
                            dayEx(r.idOf("Подтягивания"), 3, 3, 8, 120),
                        )
                    ),
                    DayBundle(
                        day = TrainingDayEntity(planId = 0, name = "Низ — сила", orderIndex = 1),
                        exercises = listOf(
                            dayEx(BigThreeLift.BACK_SQUAT.seedId, 0, 4, 5, 180),
                            dayEx(BigThreeLift.DEADLIFT.seedId, 1, 3, 5, 210),
                            dayEx(r.idOf("Выпады с гантелями"), 2, 3, 10, 120),
                            dayEx(r.idOf("Подъём на носки стоя"), 3, 4, 12, 60),
                        )
                    ),
                    DayBundle(
                        day = TrainingDayEntity(planId = 0, name = "Верх — объём", orderIndex = 2),
                        exercises = listOf(
                            dayEx(r.idOf("Жим штанги на наклонной"), 0, 4, 8, 150),
                            dayEx(r.idOf("Тяга гантели одной рукой"), 1, 4, 10, 120),
                            dayEx(r.idOf("Жим гантелей сидя"), 2, 3, 10, 120),
                            dayEx(r.idOf("Подъём штанги на бицепс"), 3, 3, 12, 90),
                            dayEx(r.idOf("Разгибания на блоке"), 4, 3, 12, 90),
                        )
                    ),
                    DayBundle(
                        day = TrainingDayEntity(planId = 0, name = "Низ — объём", orderIndex = 3),
                        exercises = listOf(
                            dayEx(r.idOf("Фронтальный присед"), 0, 3, 8, 150),
                            dayEx(r.idOf("Румынская тяга"), 1, 3, 8, 150),
                            dayEx(r.idOf("Жим ногами"), 2, 3, 12, 120),
                            dayEx(r.idOf("Сгибание ног лёжа"), 3, 3, 12, 90),
                            dayEx(r.idOf("Планка"), 4, 3, 1, 60, notes = "90 секунд"),
                        )
                    ),
                )
            }
        ),

        // ============ ОПЫТНЫЙ — PPL 6x/неделю ============
        PlanBundle(
            plan = WorkoutPlanEntity(
                name = "Push / Pull / Legs",
                description = "Шестидневный сплит для опытных. Высокий объём, прогрессия по мезоциклам.",
                minLevel = 10,
                daysPerWeek = 6,
                isPreset = true,
            ),
            buildDays = { r ->
                listOf(
                    DayBundle(
                        day = TrainingDayEntity(planId = 0, name = "Push A", orderIndex = 0),
                        exercises = listOf(
                            dayEx(BigThreeLift.BENCH_PRESS.seedId, 0, 5, 5, 210),
                            dayEx(r.idOf("Армейский жим"), 1, 4, 6, 150),
                            dayEx(r.idOf("Жим гантелей лёжа"), 2, 3, 10, 120),
                            dayEx(r.idOf("Махи гантелями в стороны"), 3, 4, 12, 60),
                            dayEx(r.idOf("Разгибания на блоке"), 4, 4, 12, 90),
                        )
                    ),
                    DayBundle(
                        day = TrainingDayEntity(planId = 0, name = "Pull A", orderIndex = 1),
                        exercises = listOf(
                            dayEx(BigThreeLift.DEADLIFT.seedId, 0, 4, 5, 240),
                            dayEx(r.idOf("Подтягивания"), 1, 4, 8, 120),
                            dayEx(r.idOf("Тяга штанги в наклоне"), 2, 4, 8, 150),
                            dayEx(r.idOf("Подъём штанги на бицепс"), 3, 4, 10, 90),
                            dayEx(r.idOf("Разводка в наклоне"), 4, 3, 15, 60),
                        )
                    ),
                    DayBundle(
                        day = TrainingDayEntity(planId = 0, name = "Legs A", orderIndex = 2),
                        exercises = listOf(
                            dayEx(BigThreeLift.BACK_SQUAT.seedId, 0, 5, 5, 210),
                            dayEx(r.idOf("Румынская тяга"), 1, 4, 8, 150),
                            dayEx(r.idOf("Жим ногами"), 2, 3, 12, 120),
                            dayEx(r.idOf("Сгибание ног лёжа"), 3, 3, 12, 90),
                            dayEx(r.idOf("Подъём на носки стоя"), 4, 4, 15, 60),
                        )
                    ),
                    DayBundle(
                        day = TrainingDayEntity(planId = 0, name = "Push B", orderIndex = 3),
                        exercises = listOf(
                            dayEx(r.idOf("Жим штанги на наклонной"), 0, 4, 6, 180),
                            dayEx(r.idOf("Жим гантелей сидя"), 1, 4, 8, 150),
                            dayEx(r.idOf("Отжимания на брусьях"), 2, 3, 10, 120),
                            dayEx(r.idOf("Разводка гантелей"), 3, 3, 12, 75),
                            dayEx(r.idOf("Французский жим"), 4, 3, 10, 90),
                        )
                    ),
                    DayBundle(
                        day = TrainingDayEntity(planId = 0, name = "Pull B", orderIndex = 4),
                        exercises = listOf(
                            dayEx(r.idOf("Тяга верхнего блока"), 0, 4, 10, 120),
                            dayEx(r.idOf("Тяга гантели одной рукой"), 1, 4, 10, 90),
                            dayEx(r.idOf("Молотки с гантелями"), 2, 3, 12, 75),
                            dayEx(r.idOf("Разводка в наклоне"), 3, 3, 15, 60),
                        )
                    ),
                    DayBundle(
                        day = TrainingDayEntity(planId = 0, name = "Legs B", orderIndex = 5),
                        exercises = listOf(
                            dayEx(r.idOf("Фронтальный присед"), 0, 4, 6, 180),
                            dayEx(r.idOf("Выпады с гантелями"), 1, 3, 10, 120),
                            dayEx(r.idOf("Сгибание ног лёжа"), 2, 4, 12, 90),
                            dayEx(r.idOf("Подъём ног в висе"), 3, 3, 12, 75),
                        )
                    ),
                )
            }
        ),
    )

    private fun dayEx(
        exerciseId: Long,
        orderIndex: Int,
        sets: Int,
        reps: Int,
        restSeconds: Int,
        notes: String? = null,
    ) = TrainingDayExerciseEntity(
        dayId = 0,
        exerciseId = exerciseId,
        orderIndex = orderIndex,
        targetSets = sets,
        targetReps = reps,
        restSeconds = restSeconds,
        notes = notes,
    )
}
