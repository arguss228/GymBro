package com.gymbro.app.ui.workouts

import androidx.compose.ui.graphics.Color

// ── Категории мышечных групп для карточки ────────────────────────
enum class MuscleTarget(val displayName: String, val emoji: String) {
    CHEST("Грудь", "💪"),
    TRICEPS("Трицепс", "🔱"),
    BACK("Спина", "🔙"),
    BICEPS("Бицепс", "💪"),
    LEGS("Ноги", "🦵"),
    SHOULDERS("Плечи", "🏋️"),
    CORE("Пресс", "🎯"),
    FULL_BODY("Всё тело", "⚡"),
    GLUTES("Ягодицы", "🍑"),
    HAMSTRINGS("Бицепс бедра", "🦵"),
}

enum class PlanDifficulty(val label: String, val color: Color) {
    BEGINNER("Новичок", Color(0xFF4CAF50)),
    INTERMEDIATE("Средний", Color(0xFF2196F3)),
    ADVANCED("Опытный", Color(0xFFFF9800)),
    ELITE("Элита", Color(0xFF7C4DFF)),
}

// ── Модель предустановленного плана ──────────────────────────────
data class PresetPlan(
    val id: String,
    val name: String,
    val subtitle: String,
    val muscles: List<MuscleTarget>,
    val difficulty: PlanDifficulty,
    val daysPerWeek: Int,
    val setsCount: Int,
    val duration: String,   // "45–60 мин"
    val gradientColors: List<Color>,
    val accentColor: Color,
    val emoji: String,
    val description: String,
)

object WorkoutPresets {

    // ── НОВИЧОК ───────────────────────────────────────────────────

    val beginnerPlans: List<PresetPlan> = listOf(

        PresetPlan(
            id = "b_chest_tri",
            name = "Грудь + Трицепс",
            subtitle = "Push Day A",
            muscles = listOf(MuscleTarget.CHEST, MuscleTarget.TRICEPS, MuscleTarget.SHOULDERS),
            difficulty = PlanDifficulty.BEGINNER,
            daysPerWeek = 2,
            setsCount = 12,
            duration = "40–55 мин",
            gradientColors = listOf(Color(0xFFFF6B35), Color(0xFFFF8C69), Color(0xFFC0392B)),
            accentColor = Color(0xFFFF6B35),
            emoji = "🔥",
            description = "Жим лёжа, жим гантелей, разводка, французский жим",
        ),
        PresetPlan(
            id = "b_back_bi",
            name = "Спина + Бицепс",
            subtitle = "Pull Day A",
            muscles = listOf(MuscleTarget.BACK, MuscleTarget.BICEPS),
            difficulty = PlanDifficulty.BEGINNER,
            daysPerWeek = 2,
            setsCount = 12,
            duration = "40–55 мин",
            gradientColors = listOf(Color(0xFF2196F3), Color(0xFF42A5F5), Color(0xFF0D47A1)),
            accentColor = Color(0xFF2196F3),
            emoji = "🏋️",
            description = "Тяга штанги, подтягивания, тяга блока, подъём на бицепс",
        ),
        PresetPlan(
            id = "b_legs",
            name = "Ноги",
            subtitle = "Leg Day A",
            muscles = listOf(MuscleTarget.LEGS, MuscleTarget.GLUTES, MuscleTarget.HAMSTRINGS),
            difficulty = PlanDifficulty.BEGINNER,
            daysPerWeek = 2,
            setsCount = 10,
            duration = "45–60 мин",
            gradientColors = listOf(Color(0xFF4CAF50), Color(0xFF66BB6A), Color(0xFF1B5E20)),
            accentColor = Color(0xFF4CAF50),
            emoji = "🦵",
            description = "Приседания, жим ногами, румынская тяга, выпады",
        ),
        PresetPlan(
            id = "b_fullbody",
            name = "Full Body",
            subtitle = "Всё тело",
            muscles = listOf(MuscleTarget.FULL_BODY),
            difficulty = PlanDifficulty.BEGINNER,
            daysPerWeek = 3,
            setsCount = 15,
            duration = "50–70 мин",
            gradientColors = listOf(Color(0xFF9C27B0), Color(0xFFCE93D8), Color(0xFF4A148C)),
            accentColor = Color(0xFF9C27B0),
            emoji = "⚡",
            description = "Базовые движения: жим, присед, тяга, подтягивания",
        ),
    )

    // ── ПРОДВИНУТЫЙ ───────────────────────────────────────────────

    val intermediatePlans: List<PresetPlan> = listOf(

        PresetPlan(
            id = "i_chest_tri",
            name = "Грудь + Трицепс",
            subtitle = "Push Day B",
            muscles = listOf(MuscleTarget.CHEST, MuscleTarget.TRICEPS, MuscleTarget.SHOULDERS),
            difficulty = PlanDifficulty.INTERMEDIATE,
            daysPerWeek = 2,
            setsCount = 16,
            duration = "55–70 мин",
            gradientColors = listOf(Color(0xFFE53935), Color(0xFFFF7043), Color(0xFFB71C1C)),
            accentColor = Color(0xFFE53935),
            emoji = "💥",
            description = "Наклонный жим, жим гантелей, разводка с паузой, отжимания на брусьях, трицепсовый блок",
        ),
        PresetPlan(
            id = "i_back_bi",
            name = "Спина + Бицепс",
            subtitle = "Pull Day B",
            muscles = listOf(MuscleTarget.BACK, MuscleTarget.BICEPS),
            difficulty = PlanDifficulty.INTERMEDIATE,
            daysPerWeek = 2,
            setsCount = 16,
            duration = "55–70 мин",
            gradientColors = listOf(Color(0xFF1565C0), Color(0xFF42A5F5), Color(0xFF0A2472)),
            accentColor = Color(0xFF1565C0),
            emoji = "🎯",
            description = "Тяга Пендлея, тяга Т-грифа, подтягивания с весом, концентрированный подъём",
        ),
        PresetPlan(
            id = "i_legs",
            name = "Ноги",
            subtitle = "Leg Day B",
            muscles = listOf(MuscleTarget.LEGS, MuscleTarget.GLUTES, MuscleTarget.HAMSTRINGS),
            difficulty = PlanDifficulty.INTERMEDIATE,
            daysPerWeek = 2,
            setsCount = 14,
            duration = "60–75 мин",
            gradientColors = listOf(Color(0xFF2E7D32), Color(0xFF81C784), Color(0xFF1B5E20)),
            accentColor = Color(0xFF2E7D32),
            emoji = "🦾",
            description = "Присед с паузой, фронтальный присед, сгибания ног, икры стоя",
        ),
        PresetPlan(
            id = "i_fullbody",
            name = "Full Body",
            subtitle = "Гипертрофия",
            muscles = listOf(MuscleTarget.FULL_BODY),
            difficulty = PlanDifficulty.INTERMEDIATE,
            daysPerWeek = 3,
            setsCount = 18,
            duration = "65–80 мин",
            gradientColors = listOf(Color(0xFF7B1FA2), Color(0xFFBA68C8), Color(0xFF4A148C)),
            accentColor = Color(0xFF7B1FA2),
            emoji = "🌊",
            description = "Upper/Lower сплит, периодизация объёма",
        ),
    )

    // ── ОПЫТНЫЙ ───────────────────────────────────────────────────

    val advancedPlans: List<PresetPlan> = listOf(

        PresetPlan(
            id = "a_chest_tri",
            name = "Грудь + Трицепс",
            subtitle = "Push — PPL",
            muscles = listOf(MuscleTarget.CHEST, MuscleTarget.TRICEPS, MuscleTarget.SHOULDERS),
            difficulty = PlanDifficulty.ADVANCED,
            daysPerWeek = 2,
            setsCount = 20,
            duration = "70–90 мин",
            gradientColors = listOf(Color(0xFFFF6F00), Color(0xFFFFAB00), Color(0xFFE65100)),
            accentColor = Color(0xFFFF6F00),
            emoji = "🔱",
            description = "Паузный жим, жим 3-4-3, наклонный жим узким, дипы с весом, изоляция трицепса суперсетами",
        ),
        PresetPlan(
            id = "a_back_bi",
            name = "Спина + Бицепс",
            subtitle = "Pull — PPL",
            muscles = listOf(MuscleTarget.BACK, MuscleTarget.BICEPS),
            difficulty = PlanDifficulty.ADVANCED,
            daysPerWeek = 2,
            setsCount = 20,
            duration = "70–90 мин",
            gradientColors = listOf(Color(0xFF006064), Color(0xFF00ACC1), Color(0xFF004D40)),
            accentColor = Color(0xFF00ACC1),
            emoji = "👑",
            description = "Становая на плинтах, тяга сумо, подтягивания широким, молотки 21, пронированный подъём",
        ),
        PresetPlan(
            id = "a_legs",
            name = "Ноги",
            subtitle = "Legs — PPL",
            muscles = listOf(MuscleTarget.LEGS, MuscleTarget.GLUTES, MuscleTarget.HAMSTRINGS),
            difficulty = PlanDifficulty.ADVANCED,
            daysPerWeek = 2,
            setsCount = 18,
            duration = "75–100 мин",
            gradientColors = listOf(Color(0xFF37474F), Color(0xFF78909C), Color(0xFF263238)),
            accentColor = Color(0xFF78909C),
            emoji = "⚔️",
            description = "Присед ATG, болгарские выпады, RDL одной ногой, жим ногами дроп-сет",
        ),
        PresetPlan(
            id = "a_fullbody",
            name = "Full Body",
            subtitle = "Сила + Масса",
            muscles = listOf(MuscleTarget.FULL_BODY),
            difficulty = PlanDifficulty.ADVANCED,
            daysPerWeek = 4,
            setsCount = 24,
            duration = "80–100 мин",
            gradientColors = listOf(Color(0xFF880E4F), Color(0xFFEC407A), Color(0xFF4A148C)),
            accentColor = Color(0xFFEC407A),
            emoji = "🏆",
            description = "5/3/1 Wendler, периодизация, AMRAP-сеты, вспомогательная работа",
        ),
    )
}