package com.gymbro.app.domain.model

/**
 * Для каждого упражнения — пороги 1RM (кг) для каждого из 12 рангов.
 * Индекс 0 = Дерево, 11 = Божество.
 */
object ExerciseRankThresholds {

    /** Возвращает индекс ранга (0..11) по 1RM для данного упражнения. */
    fun rankIndexFor(exerciseName: String, oneRmKg: Double): Int {
        val thresholds = thresholdMap[exerciseName] ?: defaultThresholds(exerciseName)
        var idx = 0
        for (i in thresholds.indices) {
            if (oneRmKg >= thresholds[i]) idx = i else break
        }
        return idx
    }

    /** Маппинг: имя упражнения → мышечная группа */
    val exerciseToMuscleGroup: Map<String, MuscleGroupId> = mapOf(
        // Бицепс
        "Подъём штанги на бицепс"  to MuscleGroupId.BICEPS,
        "Молотки с гантелями"       to MuscleGroupId.BICEPS,
        // Трицепс
        "Французский жим"           to MuscleGroupId.TRICEPS,
        "Разгибания на блоке"       to MuscleGroupId.TRICEPS,
        "Отжимания на брусьях"      to MuscleGroupId.TRICEPS,
        // Квадрицепсы
        "Присед со штангой"         to MuscleGroupId.QUADS,
        "Фронтальный присед"        to MuscleGroupId.QUADS,
        "Жим ногами"                to MuscleGroupId.QUADS,
        "Выпады с гантелями"        to MuscleGroupId.QUADS,
        // Бицепс бедра
        "Румынская тяга"            to MuscleGroupId.HAMSTRINGS,
        "Сгибание ног лёжа"         to MuscleGroupId.HAMSTRINGS,
        // Ягодицы
        "Становая тяга"             to MuscleGroupId.GLUTES,
        // Икры
        "Подъём на носки стоя"      to MuscleGroupId.CALVES,
        // Пресс
        "Скручивания"               to MuscleGroupId.ABS,
        "Подъём ног в висе"         to MuscleGroupId.ABS,
        "Планка"                    to MuscleGroupId.ABS,
        // Передние дельты
        "Армейский жим"             to MuscleGroupId.FRONT_DELT,
        "Жим гантелей сидя"         to MuscleGroupId.FRONT_DELT,
        // Средние дельты
        "Махи гантелями в стороны"  to MuscleGroupId.MID_DELT,
        // Задние дельты
        "Разводка в наклоне"        to MuscleGroupId.REAR_DELT,
        // Верх груди
        "Жим штанги на наклонной"   to MuscleGroupId.UPPER_CHEST,
        // Низ груди / грудь
        "Жим лёжа"                  to MuscleGroupId.LOWER_CHEST,
        "Жим гантелей лёжа"         to MuscleGroupId.LOWER_CHEST,
        "Разводка гантелей"         to MuscleGroupId.LOWER_CHEST,
        // Широчайшие
        "Подтягивания"              to MuscleGroupId.LATS,
        "Тяга штанги в наклоне"     to MuscleGroupId.LATS,
        "Тяга гантели одной рукой"  to MuscleGroupId.LATS,
        "Тяга верхнего блока"       to MuscleGroupId.LATS,
    )

    val muscleGroupMeta: Map<MuscleGroupId, Pair<String, String>> = mapOf(
        MuscleGroupId.BICEPS       to ("Бицепсы"                  to "Руки"),
        MuscleGroupId.TRICEPS      to ("Трицепсы"                 to "Руки"),
        MuscleGroupId.FOREARMS     to ("Предплечья"               to "Руки"),
        MuscleGroupId.QUADS        to ("Квадрицепсы"              to "Ноги"),
        MuscleGroupId.HAMSTRINGS   to ("Задняя поверхность бедра" to "Ноги"),
        MuscleGroupId.GLUTES       to ("Ягодицы"                  to "Ноги"),
        MuscleGroupId.CALVES       to ("Икры"                     to "Ноги"),
        MuscleGroupId.ABS          to ("Пресс"                    to "Кор"),
        MuscleGroupId.OBLIQUES     to ("Косые мышцы"              to "Кор"),
        MuscleGroupId.FRONT_DELT   to ("Передние дельты"          to "Плечи"),
        MuscleGroupId.MID_DELT     to ("Средние дельты"           to "Плечи"),
        MuscleGroupId.REAR_DELT    to ("Задние дельты"            to "Плечи"),
        MuscleGroupId.UPPER_CHEST  to ("Верхняя часть груди"      to "Грудь"),
        MuscleGroupId.LOWER_CHEST  to ("Нижняя часть груди"       to "Грудь"),
        MuscleGroupId.LATS         to ("Широчайшие"               to "Спина"),
        MuscleGroupId.LOWER_BACK   to ("Нижняя часть спины"       to "Спина"),
        MuscleGroupId.TRAPS        to ("Трапеция"                 to "Спина"),
        MuscleGroupId.UPPER_BACK   to ("Верхняя часть спины"      to "Спина"),
    )

    // ── Пороги 1RM по рангам (12 значений = 12 рангов) ────────────

    private val thresholdMap: Map<String, List<Double>> = mapOf(
        // Жим лёжа: Дерево=40, Бронза=55, Серебро=70, Золото=85, Платина=92, Алмаз=100,
        //           Чемпион=120, Герой=140, Спартанец=160, Титан=185, Олимпиец=215, Божество=250
        "Жим лёжа"              to listOf(40.0,55.0,70.0,85.0,92.0,100.0,120.0,140.0,160.0,185.0,215.0,250.0),
        "Жим гантелей лёжа"    to listOf(24.0,32.0,40.0,50.0,56.0,62.0,72.0,84.0,96.0,110.0,128.0,150.0),
        "Жим штанги на наклонной" to listOf(35.0,48.0,62.0,75.0,82.0,90.0,108.0,126.0,144.0,166.0,194.0,225.0),
        "Присед со штангой"    to listOf(55.0,75.0,95.0,115.0,125.0,135.0,160.0,185.0,215.0,250.0,290.0,340.0),
        "Фронтальный присед"   to listOf(40.0,55.0,70.0,85.0,95.0,105.0,125.0,145.0,170.0,200.0,230.0,270.0),
        "Жим ногами"           to listOf(80.0,110.0,140.0,170.0,190.0,210.0,250.0,290.0,340.0,390.0,450.0,520.0),
        "Становая тяга"        to listOf(65.0,90.0,115.0,135.0,142.0,150.0,180.0,210.0,245.0,285.0,330.0,385.0),
        "Румынская тяга"       to listOf(50.0,70.0,90.0,110.0,120.0,130.0,155.0,180.0,210.0,245.0,285.0,330.0),
        "Армейский жим"        to listOf(25.0,35.0,45.0,55.0,60.0,65.0,80.0,95.0,110.0,128.0,148.0,172.0),
        "Подтягивания"         to listOf(0.0,5.0,10.0,20.0,30.0,40.0,55.0,65.0,75.0,90.0,105.0,120.0),
        "Тяга штанги в наклоне" to listOf(35.0,50.0,65.0,80.0,88.0,95.0,115.0,135.0,155.0,180.0,210.0,245.0),
        "Подъём штанги на бицепс" to listOf(20.0,28.0,37.0,46.0,52.0,58.0,70.0,82.0,94.0,108.0,126.0,147.0),
        "Французский жим"      to listOf(15.0,22.0,30.0,37.0,42.0,47.0,57.0,67.0,77.0,90.0,105.0,122.0),
        "Жим гантелей сидя"    to listOf(16.0,22.0,28.0,35.0,40.0,45.0,55.0,65.0,75.0,88.0,102.0,120.0),
        "Разгибания на блоке"  to listOf(20.0,28.0,37.0,46.0,52.0,58.0,70.0,82.0,94.0,108.0,126.0,147.0),
        "Тяга верхнего блока"  to listOf(40.0,55.0,70.0,85.0,95.0,105.0,125.0,145.0,168.0,195.0,227.0,265.0),
        "Молотки с гантелями"  to listOf(16.0,22.0,28.0,35.0,40.0,45.0,55.0,65.0,75.0,88.0,102.0,120.0),
        "Сгибание ног лёжа"    to listOf(20.0,30.0,40.0,50.0,58.0,65.0,80.0,95.0,110.0,128.0,150.0,175.0),
        "Махи гантелями в стороны" to listOf(6.0,9.0,12.0,16.0,19.0,22.0,27.0,33.0,39.0,46.0,55.0,65.0),
        "Разводка в наклоне"   to listOf(6.0,9.0,12.0,16.0,19.0,22.0,27.0,33.0,39.0,46.0,55.0,65.0),
        "Разводка гантелей"    to listOf(10.0,14.0,18.0,23.0,27.0,31.0,38.0,46.0,54.0,64.0,75.0,88.0),
        "Тяга гантели одной рукой" to listOf(20.0,28.0,37.0,46.0,52.0,58.0,70.0,82.0,95.0,112.0,130.0,152.0),
        "Отжимания на брусьях" to listOf(0.0,5.0,10.0,20.0,30.0,40.0,55.0,65.0,80.0,95.0,112.0,132.0),
        "Выпады с гантелями"   to listOf(10.0,14.0,18.0,24.0,28.0,32.0,40.0,48.0,57.0,67.0,80.0,94.0),
        "Подъём на носки стоя" to listOf(40.0,60.0,80.0,100.0,115.0,130.0,160.0,190.0,220.0,260.0,305.0,360.0),
    )

    // Универсальные пороги для упражнений без специфики (планка в сек, etc.)
    private fun defaultThresholds(exerciseName: String): List<Double> =
        listOf(5.0,10.0,20.0,30.0,40.0,50.0,60.0,75.0,90.0,110.0,135.0,160.0)
}