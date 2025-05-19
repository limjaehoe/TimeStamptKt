package com.androidkotlin.timestampkt.data.local.dao

import androidx.paging.PagingSource
import androidx.room.*
import com.androidkotlin.timestampkt.domain.model.TimeRecord
import kotlinx.coroutines.flow.Flow

@Dao
interface TimeRecordDao {
    // 기존 메서드는 유지하되, 페이징을 위한 메서드 추가
    @Query("SELECT * FROM time_records ORDER BY timestamp DESC")
    fun getAllRecords(): Flow<List<TimeRecord>>

    // PagingSource를 반환하는 메서드 추가
    @Query("SELECT * FROM time_records ORDER BY timestamp DESC")
    fun getPagedRecords(): PagingSource<Int, TimeRecord>

    @Insert
    suspend fun insert(record: TimeRecord)

    @Update
    suspend fun update(record: TimeRecord)

    @Delete
    suspend fun delete(record: TimeRecord)

    @Query("DELETE FROM time_records")
    suspend fun deleteAll()

    @Query("SELECT COUNT(*) FROM time_records")
    suspend fun getCount(): Int
}