package com.androidkotlin.timestampkt.data.local.dao

import androidx.paging.PagingSource
import androidx.room.*
import com.androidkotlin.timestampkt.data.local.entity.TimeRecordEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TimeRecordDao {
    @Query("SELECT * FROM time_records ORDER BY timestamp DESC")
    fun getAllRecords(): Flow<List<TimeRecordEntity>>

    @Query("SELECT * FROM time_records ORDER BY timestamp DESC")
    fun getPagedRecords(): PagingSource<Int, TimeRecordEntity>

    @Insert
    suspend fun insert(record: TimeRecordEntity)

    @Update
    suspend fun update(record: TimeRecordEntity)

    @Delete
    suspend fun delete(record: TimeRecordEntity)

    @Query("DELETE FROM time_records")
    suspend fun deleteAll()

    @Query("SELECT COUNT(*) FROM time_records")
    suspend fun getCount(): Int
}