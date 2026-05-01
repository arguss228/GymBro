package com.gymbro.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "one_rm")
data class OneRmEntity(
    @PrimaryKey
    val id: Long,

    val benchKg: Double,
    val squatKg: Double,
    val deadliftKg: Double
) {

    // Пустой конструктор + конструктор с значениями по умолчанию
    constructor() : this(1L, 0.0, 0.0, 0.0)

    constructor(
        benchKg: Double = 0.0,
        squatKg: Double = 0.0,
        deadliftKg: Double = 0.0
    ) : this(1L, benchKg, squatKg, deadliftKg)
}