package com.gymbro.app.domain.model

import androidx.compose.ui.graphics.Color

enum class RankGroup { EARTH, HEAVEN }

data class StrengthRank(
    val name: String,
    val symbol: String,
    val group: RankGroup,
    val bench1RmKg: Double,
    val squat1RmKg: Double,
    val deadlift1RmKg: Double,
    val primaryColor: Color,
    val secondaryColor: Color,
    val glowColor: Color,
    val description: String,
)

object StrengthRanks {

    val all: List<StrengthRank> = listOf(

        // ── ЗЕМНАЯ ГРУППА ─────────────────────────────────────────
        StrengthRank(
            name          = "Дерево",
            symbol        = "🌱",
            group         = RankGroup.EARTH,
            bench1RmKg    = 40.0,
            squat1RmKg    = 55.0,
            deadlift1RmKg = 65.0,
            primaryColor  = Color(0xFF2E7D32),
            secondaryColor= Color(0xFF66BB6A),
            glowColor     = Color(0xFF1B5E20),
            description   = "Первый шаг к силе. Корни уходят глубоко в землю.",
        ),
        StrengthRank(
            name          = "Бронза",
            symbol        = "🥉",
            group         = RankGroup.EARTH,
            bench1RmKg    = 55.0,
            squat1RmKg    = 75.0,
            deadlift1RmKg = 90.0,
            primaryColor  = Color(0xFF8D6E63),
            secondaryColor= Color(0xFFBCAAA4),
            glowColor     = Color(0xFF6D4C41),
            description   = "Металл начинает поддаваться. Тело крепнет с каждой тренировкой.",
        ),
        StrengthRank(
            name          = "Серебро",
            symbol        = "🥈",
            group         = RankGroup.EARTH,
            bench1RmKg    = 70.0,
            squat1RmKg    = 95.0,
            deadlift1RmKg = 115.0,
            primaryColor  = Color(0xFF78909C),
            secondaryColor= Color(0xFFB0BEC5),
            glowColor     = Color(0xFF455A64),
            description   = "Серебряный атлет. Техника оттачивается, веса растут.",
        ),
        StrengthRank(
            name          = "Золото",
            symbol        = "🥇",
            group         = RankGroup.EARTH,
            bench1RmKg    = 85.0,
            squat1RmKg    = 115.0,
            deadlift1RmKg = 135.0,
            primaryColor  = Color(0xFFF9A825),
            secondaryColor= Color(0xFFFFD54F),
            glowColor     = Color(0xFFF57F17),
            description   = "Золотой стандарт. Большинство атлетов мечтают об этом.",
        ),
        StrengthRank(
            name          = "Платина",
            symbol        = "💠",
            group         = RankGroup.EARTH,
            bench1RmKg    = 92.0,
            squat1RmKg    = 125.0,
            deadlift1RmKg = 142.0,
            primaryColor  = Color(0xFF00ACC1),
            secondaryColor= Color(0xFF80DEEA),
            glowColor     = Color(0xFF006064),
            description   = "Элитный уровень. Сила становится образом жизни.",
        ),
        StrengthRank(
            name          = "Алмаз",
            symbol        = "💎",
            group         = RankGroup.EARTH,
            bench1RmKg    = 100.0,
            squat1RmKg    = 135.0,
            deadlift1RmKg = 150.0,
            primaryColor  = Color(0xFF00E5FF),
            secondaryColor= Color(0xFF84FFFF),
            glowColor     = Color(0xFF00B8D4),
            description   = "Вершина земного мира. Ты — алмаз, рождённый под давлением.",
        ),

        // ── НЕБЕСНАЯ ГРУППА ───────────────────────────────────────
        StrengthRank(
            name          = "Чемпион",
            symbol        = "🏆",
            group         = RankGroup.HEAVEN,
            bench1RmKg    = 120.0,
            squat1RmKg    = 160.0,
            deadlift1RmKg = 180.0,
            primaryColor  = Color(0xFF1565C0),
            secondaryColor= Color(0xFF42A5F5),
            glowColor     = Color(0xFF0D47A1),
            description   = "Ты преодолел земные пределы. Начало небесного пути.",
        ),
        StrengthRank(
            name          = "Герой",
            symbol        = "⚡",
            group         = RankGroup.HEAVEN,
            bench1RmKg    = 140.0,
            squat1RmKg    = 185.0,
            deadlift1RmKg = 210.0,
            primaryColor  = Color(0xFF6A1B9A),
            secondaryColor= Color(0xFFCE93D8),
            glowColor     = Color(0xFF4A148C),
            description   = "Твоя сила — легенда. Герои вдохновляют целые поколения.",
        ),
        StrengthRank(
            name          = "Спартанец",
            symbol        = "🛡️",
            group         = RankGroup.HEAVEN,
            bench1RmKg    = 160.0,
            squat1RmKg    = 215.0,
            deadlift1RmKg = 245.0,
            primaryColor  = Color(0xFFC62828),
            secondaryColor= Color(0xFFEF9A9A),
            glowColor     = Color(0xFFB71C1C),
            description   = "Воля железная, дух несломим. Настоящий спартанец.",
        ),
        StrengthRank(
            name          = "Титан",
            symbol        = "⚔️",
            group         = RankGroup.HEAVEN,
            bench1RmKg    = 185.0,
            squat1RmKg    = 250.0,
            deadlift1RmKg = 285.0,
            primaryColor  = Color(0xFFE65100),
            secondaryColor= Color(0xFFFFCC02),
            glowColor     = Color(0xFFBF360C),
            description   = "Мощь титана. Земля дрожит под твоими ногами.",
        ),
        StrengthRank(
            name          = "Олимпиец",
            symbol        = "🌟",
            group         = RankGroup.HEAVEN,
            bench1RmKg    = 215.0,
            squat1RmKg    = 290.0,
            deadlift1RmKg = 330.0,
            primaryColor  = Color(0xFFFFAB00),
            secondaryColor= Color(0xFFFFFF00),
            glowColor     = Color(0xFFFF6F00),
            description   = "Один из немногих избранных. Уровень Олимпа.",
        ),
        StrengthRank(
            name          = "Божество",
            symbol        = "👑",
            group         = RankGroup.HEAVEN,
            bench1RmKg    = 250.0,
            squat1RmKg    = 340.0,
            deadlift1RmKg = 385.0,
            primaryColor  = Color(0xFFFFD700),
            secondaryColor= Color(0xFFFFF9C4),
            glowColor     = Color(0xFFFF8F00),
            description   = "Абсолютная вершина. Ты — легенда, которую помнят века.",
        ),
    )

    /** Текущий ранг — наивысший, где ВСЕ 3 лифта >= порогов */
    fun currentRankFor(bench: Double, squat: Double, deadlift: Double): StrengthRank =
        all.lastOrNull { r ->
            bench    >= r.bench1RmKg &&
            squat    >= r.squat1RmKg &&
            deadlift >= r.deadlift1RmKg
        } ?: all.first()

    fun nextRank(current: StrengthRank): StrengthRank? {
        val idx = all.indexOf(current)
        return if (idx in 0 until all.lastIndex) all[idx + 1] else null
    }

    /** 0f..1f — прогресс к следующему рангу */
    fun progressToNext(bench: Double, squat: Double, deadlift: Double): Float {
        val current = currentRankFor(bench, squat, deadlift)
        val next    = nextRank(current) ?: return 1f
        var total   = 0.0; var gained = 0.0
        listOf(
            bench    to (current.bench1RmKg    to next.bench1RmKg),
            squat    to (current.squat1RmKg    to next.squat1RmKg),
            deadlift to (current.deadlift1RmKg to next.deadlift1RmKg),
        ).forEach { (best, range) ->
            val (cur, nxt) = range
            val step = (nxt - cur).coerceAtLeast(0.01)
            total  += step
            gained += (best - cur).coerceIn(0.0, step)
        }
        return (gained / total).toFloat().coerceIn(0f, 1f)
    }

    /** кг до следующего ранга по каждому лифту */
    fun kgToNext(bench: Double, squat: Double, deadlift: Double): Triple<Double, Double, Double> {
        val next = nextRank(currentRankFor(bench, squat, deadlift))
            ?: return Triple(0.0, 0.0, 0.0)
        return Triple(
            (next.bench1RmKg    - bench).coerceAtLeast(0.0),
            (next.squat1RmKg    - squat).coerceAtLeast(0.0),
            (next.deadlift1RmKg - deadlift).coerceAtLeast(0.0),
        )
    }
}