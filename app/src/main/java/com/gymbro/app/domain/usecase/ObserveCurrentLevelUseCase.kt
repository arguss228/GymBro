package com.gymbro.app.domain.usecase

import com.gymbro.app.data.repository.LevelRepository
import com.gymbro.app.data.repository.ProgressRepository
import com.gymbro.app.domain.model.StrengthLevel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import javax.inject.Inject

/**
 * Реактивный поток [StrengthLevel], который перевычисляется при изменении истории подходов.
 *
 * FIX (баг 5): оригинальная реализация подписывалась на observeLatestLevel() — поток,
 * в который сама же писала через calculateLevel(). Это создавало петлю:
 *   observeTotalSessions() или observeLatestLevel() изменился
 *   → emit(calc())
 *   → calc() вызывает recordLevel()
 *   → observeLatestLevel() эмитит новое значение
 *   → снова emit(calc()) → ...
 *
 * Решение: триггером для пересчёта служит ТОЛЬКО observeTotalSessions() — количество
 * сессий. Оно меняется только когда пользователь логирует подход. Level_progress-таблица
 * при этом не используется как триггер, чтобы не создавать петлю.
 *
 * Результирующий [StrengthLevel] возвращается напрямую из calc() и кешируется
 * в StateFlow у ViewModel через stateIn().
 */
class ObserveCurrentLevelUseCase @Inject constructor(
    private val calc: CalculateStrengthLevelUseCase,
    private val progressRepo: ProgressRepository,
    private val levelRepo: LevelRepository,
) {
    operator fun invoke(): Flow<StrengthLevel> = flow {
        // Первоначальный расчёт при подписке.
        emit(calc())

        // Пересчитываем только при изменении числа сессий.
        // distinctUntilChanged() защищает от лишних пересчётов если Flow
        // эмитит одно и то же значение несколько раз подряд.
        progressRepo.observeTotalSessions()
            .distinctUntilChanged()
            .collect {
                emit(calc())
            }
    }.flowOn(Dispatchers.IO)
}