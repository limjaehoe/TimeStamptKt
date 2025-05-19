package com.androidkotlin.timestampkt.domain.repository

import androidx.paging.PagingData
import com.androidkotlin.timestampkt.domain.model.TimeRecord
import kotlinx.coroutines.flow.Flow

interface TimeRecordRepository {
    fun getAllRecords(): Flow<List<TimeRecord>>
    fun getPagedRecords(): Flow<PagingData<TimeRecord>>
    suspend fun addRecord(record: TimeRecord)
    suspend fun updateRecord(record: TimeRecord)
    suspend fun deleteRecord(record: TimeRecord)
    suspend fun deleteAllRecords()
    suspend fun getRecordCount(): Int
}