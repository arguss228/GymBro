package com.gymbro.app.data.repository

import com.gymbro.app.data.local.dao.OneRmDao
import com.gymbro.app.data.local.entity.OneRmEntity
import com.gymbro.app.domain.model.StrengthRank
import com.gymbro.app.domain.model.StrengthRanks
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

data class RankState(
    val bench: Double       = 0.0,
    val squat: Double       = 0.0,
    val deadlift: Double    = 0.0,
    val currentRank: StrengthRank = StrengthRanks.all.first(),
    val nextRank: StrengthRank?   = StrengthRanks.all.getOrNull(1),
    val progress: Float           = 0f,
    val kgToBench: Double         = 0.0,
    val kgToSquat: Double         = 0.0,
    val kgToDeadlift: Double      = 0.0,
    val hasData: Boolean          = false,
)

@Singleton
class RankRepository @Inject constructor(private val dao: OneRmDao) {

    fun observeRankState(): Flow<RankState> = dao.observe().map { entity ->
        if (entity == null || (entity.benchKg == 0.0 && entity.squatKg == 0.0 && entity.deadliftKg == 0.0)) {
            return@map RankState(hasData = false)
        }
        val b = entity.benchKg; val s = entity.squatKg; val d = entity.deadliftKg
        val cur  = StrengthRanks.currentRankFor(b, s, d)
        val next = StrengthRanks.nextRank(cur)
        val (kb, ks, kd) = StrengthRanks.kgToNext(b, s, d)
        RankState(
            bench        = b, squat = s, deadlift = d,
            currentRank  = cur,
            nextRank     = next,
            progress     = StrengthRanks.progressToNext(b, s, d),
            kgToBench    = kb, kgToSquat = ks, kgToDeadlift = kd,
            hasData      = true,
        )
    }

    suspend fun save1Rm(bench: Double, squat: Double, deadlift: Double) =
        dao.upsert(OneRmEntity(benchKg = bench, squatKg = squat, deadliftKg = deadlift))

    suspend fun hasData(): Boolean {
        val e = dao.get() ?: return false
        return e.benchKg > 0.0 || e.squatKg > 0.0 || e.deadliftKg > 0.0
    }
}