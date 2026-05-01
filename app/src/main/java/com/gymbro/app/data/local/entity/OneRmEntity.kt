package com.gymbro.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Единственная запись с актуальными 1RM пользователя.
 * id = 1 всегда (singleton).
 */
@Entity(tableName = "one_rm")
data class OneRmEntity(
    @PrimaryKey
    val id: Long = 1L,
    val benchKg: Double    = 0.0,
    val squatKg: Double    = 0.0,
    val deadliftKg: Double = 0.0,
)