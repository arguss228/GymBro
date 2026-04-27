package com.gymbro.app.data.seed

import com.gymbro.app.data.local.entity.ExerciseCategory
import com.gymbro.app.data.local.entity.ExerciseEntity
import com.gymbro.app.data.local.entity.ExerciseEquipment
import com.gymbro.app.domain.model.BigThreeLift

/**
 * Seed-данные для справочника упражнений.
 * ID первых трёх упражнений зафиксированы — они совпадают с [BigThreeLift.seedId].
 */
object ExerciseSeed {

    val exercises: List<ExerciseEntity> = listOf(
        // --- Big Three — id зафиксированы ---
        ExerciseEntity(
            id = BigThreeLift.BENCH_PRESS.seedId,
            name = "Жим лёжа",
            category = ExerciseCategory.CHEST,
            equipment = ExerciseEquipment.BARBELL,
            primaryMuscle = "Грудные",
            isSystem = true,
            description = "Горизонтальный жим штанги. Основное упражнение на грудь."
        ),
        ExerciseEntity(
            id = BigThreeLift.BACK_SQUAT.seedId,
            name = "Присед со штангой",
            category = ExerciseCategory.LEGS,
            equipment = ExerciseEquipment.BARBELL,
            primaryMuscle = "Квадрицепсы",
            isSystem = true,
            description = "Приседания со штангой на спине. База для ног."
        ),
        ExerciseEntity(
            id = BigThreeLift.DEADLIFT.seedId,
            name = "Становая тяга",
            category = ExerciseCategory.BACK,
            equipment = ExerciseEquipment.BARBELL,
            primaryMuscle = "Спина/задняя цепь",
            isSystem = true,
            description = "Классическая становая. Работает практически всё тело."
        ),

        // --- Грудь ---
        ExerciseEntity(0, "Жим гантелей лёжа", ExerciseCategory.CHEST, ExerciseEquipment.DUMBBELL, "Грудные", isSystem = true),
        ExerciseEntity(0, "Жим штанги на наклонной", ExerciseCategory.CHEST, ExerciseEquipment.BARBELL, "Верх груди", isSystem = true),
        ExerciseEntity(0, "Разводка гантелей", ExerciseCategory.CHEST, ExerciseEquipment.DUMBBELL, "Грудные", isSystem = true),
        ExerciseEntity(0, "Отжимания на брусьях", ExerciseCategory.CHEST, ExerciseEquipment.BODYWEIGHT, "Низ груди/трицепс", isSystem = true),

        // --- Спина ---
        ExerciseEntity(0, "Подтягивания", ExerciseCategory.BACK, ExerciseEquipment.BODYWEIGHT, "Широчайшие", isSystem = true),
        ExerciseEntity(0, "Тяга штанги в наклоне", ExerciseCategory.BACK, ExerciseEquipment.BARBELL, "Широчайшие", isSystem = true),
        ExerciseEntity(0, "Тяга гантели одной рукой", ExerciseCategory.BACK, ExerciseEquipment.DUMBBELL, "Широчайшие", isSystem = true),
        ExerciseEntity(0, "Тяга верхнего блока", ExerciseCategory.BACK, ExerciseEquipment.CABLE, "Широчайшие", isSystem = true),
        ExerciseEntity(0, "Румынская тяга", ExerciseCategory.BACK, ExerciseEquipment.BARBELL, "Задняя поверхность бедра", isSystem = true),

        // --- Ноги ---
        ExerciseEntity(0, "Фронтальный присед", ExerciseCategory.LEGS, ExerciseEquipment.BARBELL, "Квадрицепсы", isSystem = true),
        ExerciseEntity(0, "Жим ногами", ExerciseCategory.LEGS, ExerciseEquipment.MACHINE, "Квадрицепсы", isSystem = true),
        ExerciseEntity(0, "Выпады с гантелями", ExerciseCategory.LEGS, ExerciseEquipment.DUMBBELL, "Квадрицепсы/ягодицы", isSystem = true),
        ExerciseEntity(0, "Сгибание ног лёжа", ExerciseCategory.LEGS, ExerciseEquipment.MACHINE, "Бицепс бедра", isSystem = true),
        ExerciseEntity(0, "Подъём на носки стоя", ExerciseCategory.LEGS, ExerciseEquipment.MACHINE, "Икры", isSystem = true),

        // --- Плечи ---
        ExerciseEntity(0, "Армейский жим", ExerciseCategory.SHOULDERS, ExerciseEquipment.BARBELL, "Дельты", isSystem = true),
        ExerciseEntity(0, "Жим гантелей сидя", ExerciseCategory.SHOULDERS, ExerciseEquipment.DUMBBELL, "Дельты", isSystem = true),
        ExerciseEntity(0, "Махи гантелями в стороны", ExerciseCategory.SHOULDERS, ExerciseEquipment.DUMBBELL, "Средние дельты", isSystem = true),
        ExerciseEntity(0, "Разводка в наклоне", ExerciseCategory.SHOULDERS, ExerciseEquipment.DUMBBELL, "Задние дельты", isSystem = true),

        // --- Руки ---
        ExerciseEntity(0, "Подъём штанги на бицепс", ExerciseCategory.ARMS, ExerciseEquipment.BARBELL, "Бицепс", isSystem = true),
        ExerciseEntity(0, "Молотки с гантелями", ExerciseCategory.ARMS, ExerciseEquipment.DUMBBELL, "Бицепс/плечевая", isSystem = true),
        ExerciseEntity(0, "Французский жим", ExerciseCategory.ARMS, ExerciseEquipment.BARBELL, "Трицепс", isSystem = true),
        ExerciseEntity(0, "Разгибания на блоке", ExerciseCategory.ARMS, ExerciseEquipment.CABLE, "Трицепс", isSystem = true),

        // --- Кор ---
        ExerciseEntity(0, "Планка", ExerciseCategory.CORE, ExerciseEquipment.BODYWEIGHT, "Кор", isSystem = true),
        ExerciseEntity(0, "Скручивания", ExerciseCategory.CORE, ExerciseEquipment.BODYWEIGHT, "Пресс", isSystem = true),
        ExerciseEntity(0, "Подъём ног в висе", ExerciseCategory.CORE, ExerciseEquipment.BODYWEIGHT, "Пресс", isSystem = true),
    )
}
