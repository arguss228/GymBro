package com.gymbro.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.gymbro.app.data.local.entity.OneRmEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface OneRmDao {

    @Query("SELECT * FROM one_rm WHERE id = 1")
    fun observe(): Flow<OneRmEntity?>

    @Query("SELECT * FROM one_rm WHERE id = 1")
    suspend fun get(): OneRmEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: OneRmEntity)
}