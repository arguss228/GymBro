package com.gymbro.app.data.repository

import com.gymbro.app.data.local.dao.ExerciseDao
import com.gymbro.app.data.local.entity.ExerciseEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ExerciseRepository @Inject constructor(
    private val dao: ExerciseDao,
) {
    fun observeAll(): Flow<List<ExerciseEntity>> = dao.observeAll()

    fun search(query: String): Flow<List<ExerciseEntity>> =
        if (query.isBlank()) dao.observeAll() else dao.search(query.trim())

    suspend fun getById(id: Long): ExerciseEntity? = dao.getById(id)

    suspend fun create(exercise: ExerciseEntity): Long = dao.insert(exercise.copy(isSystem = false))

    suspend fun update(exercise: ExerciseEntity) = dao.update(exercise)

    suspend fun deleteUserExercise(id: Long): Boolean = dao.deleteUserExercise(id) > 0
}
