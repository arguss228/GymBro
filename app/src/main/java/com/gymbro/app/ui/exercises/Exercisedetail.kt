package com.gymbro.app.domain.model

import com.gymbro.app.data.local.entity.ExerciseCategory
import com.gymbro.app.data.local.entity.ExerciseEquipment

/**
 * Расширенная модель упражнения с данными техники выполнения.
 * Основные поля берутся из [ExerciseEntity], техника — из [ExerciseTechniqueData].
 */
data class ExerciseDetail(
    val id: Long,
    val name: String,
    val category: ExerciseCategory,
    val equipment: ExerciseEquipment,
    val primaryMuscle: String,
    val secondaryMuscles: List<String> = emptyList(),
    val isSystem: Boolean = false,

    // ── Technique ──────────────────────────────────────────────
    /** Краткое вступление / общее описание упражнения. */
    val overview: String = "",

    /** Пошаговая техника выполнения — каждый элемент = одна фаза или шаг. */
    val techniqueSteps: List<TechniqueStep> = emptyList(),

    // ── Rep recommendations by goal ────────────────────────────
    val repRecommendations: List<RepRecommendation> = emptyList(),

    // ── Common mistakes ────────────────────────────────────────
    val commonMistakes: List<Mistake> = emptyList(),

    // ── Tips ───────────────────────────────────────────────────
    val tips: List<String> = emptyList(),

    /** Варианты / прогрессии упражнения. */
    val variations: List<String> = emptyList(),
)

data class TechniqueStep(
    val phase: String,          // «Стартовая позиция», «Опускание», «Подъём», «Фиксация»
    val description: String,
)

data class RepRecommendation(
    val goal: String,           // «Сила», «Гипертрофия», «Выносливость / Техника»
    val repsRange: String,      // «1–5», «6–12», «12–20+»
    val setsRange: String,      // «3–5», «3–4», «2–4»
    val restSeconds: String,    // «3–5 мин», «1–2 мин», «30–60 сек»
    val intensity: String,      // «85–100% 1ПМ», «65–80% 1ПМ», «≤60% 1ПМ»
    val notes: String = "",
)

data class Mistake(
    val title: String,
    val description: String,
)