package com.androidkotlin.timestampkt.domain.repository

import com.androidkotlin.timestampkt.domain.model.LeaveRecord
import com.androidkotlin.timestampkt.domain.model.LeaveYearSetting
import kotlinx.coroutines.flow.Flow

interface LeaveRepository {
    fun getAllLeaveRecords(): Flow<List<LeaveRecord>>
    fun getLeaveRecordsByYear(year: Int): Flow<List<LeaveRecord>>
    fun getYearSetting(year: Int): Flow<LeaveYearSetting?>
    fun getAllYearSettings(): Flow<List<LeaveYearSetting>>
    suspend fun addLeaveRecord(record: LeaveRecord)
    suspend fun addLeaveRecords(records: List<LeaveRecord>)
    suspend fun updateLeaveRecord(record: LeaveRecord)
    suspend fun deleteLeaveRecord(record: LeaveRecord)
    suspend fun saveYearSetting(setting: LeaveYearSetting)
    suspend fun replaceAll(records: List<LeaveRecord>, settings: List<LeaveYearSetting>)
}
