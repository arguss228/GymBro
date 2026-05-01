// domain/model/StrengthRank.kt
package com.gymbro.app.domain.model

import androidx.compose.ui.graphics.Color

enum class RankGroup { EARTH, HEAVEN }

data class StrengthRank(
    val name: String,
    val symbol: String,               // эмодзи-символ ранга
    val group: RankGroup,
    val bench1RmKg: Double,           // требуемый 1RM жим лёжа
    val squat1RmKg: Double,           // требуемый 1RM присед
    val deadlift1RmKg: Double,        // требуемый 1RM становая
    val primaryColor: Color,
    val secondaryColor: Color,
    val description: String,
)

/**
 * Реестр всех 12 рангов, отсортированных от низшего к высшему.
 * Прогрессия по 1RM.
 *
 * Diamond (Алмаз): Жим 100 / Присед 135 / Тяга 150
 * Остальные — логическая интерполяция вверх и вниз.
 */
object StrengthRanks {

    val all: List<StrengthRank> = listOf(

        // ── ЗЕМНАЯ ГРУППА ────────────────────────────────────────
        StrengthRank(
            name           = "Дерево",
            symbol         = "🌱",
            group          = RankGroup.EARTH,
            bench1RmKg     = 40.0,
            squat1RmKg     = 55.0,
            deadlift1RmKg  = 65.0,
            primaryColor   = Color(0xFF4CAF50),
            secondaryColor = Color(0xFFA5D6A7),
            description    = "Первый шаг к силе. Ты только начинаешь свой путь.",
        ),
        StrengthRank(
            name           = "Бронза",
            symbol         = "🥉",
            group          = RankGroup.EARTH,
            bench1RmKg     = 55.0,
            squat1RmKg     = 75.0,
            deadlift1RmKg  = 90.0,
            primaryColor   = Color(0xFFCD7F32),
            secondaryColor = Color(0xFFD4A066),
            description    = "Тело привыкает к нагрузке. Прогресс очевиден.",
        ),
        StrengthRank(
            name           = "Серебро",
            symbol         = "🥈",
            group          = RankGroup.EARTH,
            bench1RmKg     = 70.0,
            squat1RmKg     = 95.0,
            deadlift1RmKg  = 115.0,
            primaryColor   = Color(0xFF9E9E9E),
            secondaryColor = Color(0xFFBDBDBD),
            description    = "Стабильный атлет. Техника оттачивается, веса растут.",
        ),
        StrengthRank(
            name           = "Золото",
            symbol         = "🥇",
            group          = RankGroup.EARTH,
            bench1RmKg     = 85.0,
            squat1RmKg     = 115.0,
            deadlift1RmKg  = 135.0,
            primaryColor   = Color(0xFFFFD600),
            secondaryColor = Color(0xFFFFEE58),
            description    = "Золотой стандарт. Большинство атлетов мечтают об этом.",
        ),
        StrengthRank(
            name           = "Платина",
            symbol         = "💠",
            group          = RankGroup.EARTH,
            bench1RmKg     = 92.0,
            squat1RmKg     = 125.0,
            deadlift1RmKg  = 142.0,
            primaryColor   = Color(0xFF4DD0E1),
            secondaryColor = Color(0xFF80DEEA),
            description    = "Элитный уровень. Сила становится образом жизни.",
        ),
        StrengthRank(
            name           = "Алмаз",
            symbol         = "💎",
            group          = RankGroup.EARTH,
            bench1RmKg     = 100.0,
            squat1RmKg     = 135.0,
            deadlift1RmKg  = 150.0,
            primaryColor   = Color(0xFF00E5FF),
            secondaryColor = Color(0xFF84FFFF),
            description    = "Вершина земного мира. Ты — алмаз, отшлифованный тренировками.",
        ),

        // ── НЕБЕСНАЯ ГРУППА ──────────────────────────────────────
        StrengthRank(
            name           = "Чемпион",
            symbol         = "🏆",
            group          = RankGroup.HEAVEN,
            bench1RmKg     = 120.0,
            squat1RmKg     = 160.0,
            deadlift1RmKg  = 180.0,
            primaryColor   = Color(0xFF2979FF),
            secondaryColor = Color(0xFF82B1FF),
            description    = "Ты вышел за пределы обычного. Начало небесного пути.",
        ),
        StrengthRank(
            name           = "Герой",
            symbol         = "⚡",
            group          = RankGroup.HEAVEN,
            bench1RmKg     = 140.0,
            squat1RmKg     = 185.0,
            deadlift1RmKg  = 210.0,
            primaryColor   = Color(0xFF651FFF),
            secondaryColor = Color(0xFFB388FF),
            description    = "Твоя сила вдохновляет других. Ты — герой зала.",
        ),
        StrengthRank(
            name           = "Спартанец",
            symbol         = "🛡️",
            group          = RankGroup.HEAVEN,
            bench1RmKg     = 160.0,
            squat1RmKg     = 215.0,
            deadlift1RmKg  = 245.0,
            primaryColor   = Color(0xFFE040FB),
            secondaryColor = Color(0xFFEA80FC),
            description    = "Воля железная, тело стальное. Спартанская дисциплина.",
        ),
        StrengthRank(
            name           = "Титан",
            symbol         = "⚔️",
            group          = RankGroup.HEAVEN,
            bench1RmKg     = 185.0,
            squat1RmKg     = 250.0,
            deadlift1RmKg  = 285.0,
            primaryColor   = Color(0xFFFF6D00),
            secondaryColor = Color(0xFFFFAB40),
            description    = "Мощь титана. Твои подъёмы — легенды зала.",
        ),
        StrengthRank(
            name           = "Олимпиец",
            symbol         = "🌟",
            group          = RankGroup.HEAVEN,
            bench1RmKg     = 215.0,
            squat1RmKg     = 290.0,
            deadlift1RmKg  = 330.0,
            primaryColor   = Color(0xFFFFD600),
            secondaryColor = Color(0xFFFFF176),
            description    = "Один из немногих. Олимпийский уровень мощи.",
        ),
        StrengthRank(
            name           = "Божество",
            symbol         = "👑",
            group          = RankGroup.HEAVEN,
            bench1RmKg     = 250.0,
            squat1RmKg     = 340.0,
            deadlift1RmKg  = 385.0,
            primaryColor   = Color(0xFFFFAB00),
            secondaryColor = Color(0xFFFFD740),
            description    = "Абсолютная вершина. Ты — легенда, которую помнят века.",
        ),
    )

    /**
     * Определяет текущий ранг по наименьшему из трёх 1RM.
     * Ранг достигнут, если ВСЕ три показателя >= требуемых.
     */
    fun currentRankFor(
        bench1Rm: Double,
        squat1Rm: Double,
        deadlift1Rm: Double,
    ): StrengthRank {
        // Идём с конца — возвращаем самый высокий достигнутый ранг
        return all.lastOrNull { rank ->
            bench1Rm >= rank.bench1RmKg &&
            squat1Rm >= rank.squat1RmKg &&
            deadlift1Rm >= rank.deadlift1RmKg
        } ?: all.first()
    }

    /**
     * Следующий ранг после текущего (null если максимальный).
     */
    fun nextRank(current: StrengthRank): StrengthRank? {
        val idx = all.indexOf(current)
        return if (idx >= 0 && idx < all.lastIndex) all[idx + 1] else null
    }
}