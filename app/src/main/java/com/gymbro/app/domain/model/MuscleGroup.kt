package com.gymbro.app.domain.model

enum class MuscleGroupId {
    BICEPS, TRICEPS, FOREARMS,
    QUADS, HAMSTRINGS, GLUTES, CALVES,
    ABS, OBLIQUES,
    FRONT_DELT, MID_DELT, REAR_DELT,
    UPPER_CHEST, LOWER_CHEST,
    LATS, LOWER_BACK, TRAPS, UPPER_BACK
}

data class MuscleGroupRank(
    val groupId: MuscleGroupId,
    val displayName: String,
    val bodyPartName: String,   // "Руки", "Ноги", etc.
    val exerciseRanks: List<ExerciseRank>,
) {
    val averageRankIndex: Double
        get() = if (exerciseRanks.isEmpty()) -1.0
                else exerciseRanks.map { it.rankIndex }.average()

    val rank: StrengthRank?
        get() {
            val avg = averageRankIndex
            if (avg < 0) return null
            val idx = avg.toInt().coerceIn(0, StrengthRanks.all.lastIndex)
            return StrengthRanks.all[idx]
        }
}

data class ExerciseRank(
    val exerciseId: Long,
    val exerciseName: String,
    val best1Rm: Double,        // лучший 1RM пользователя
    val rankIndex: Int,         // индекс в StrengthRanks.all
) {
    val rank: StrengthRank get() = StrengthRanks.all[rankIndex]
}

data class UserBodyRank(
    val muscleGroups: List<MuscleGroupRank>,
    val overallRankIndex: Double,   // среднее по всем группам
    val overallRank: StrengthRank,
    val nextRank: StrengthRank?,
    val progressToNext: Float,
) {
    val hasData: Boolean get() = muscleGroups.any { it.exerciseRanks.isNotEmpty() }
}