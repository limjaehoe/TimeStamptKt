package com.androidkotlin.timestampkt.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.androidkotlin.timestampkt.data.local.entity.LeaveRecordEntity
import com.androidkotlin.timestampkt.data.local.entity.LeaveYearSettingEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface LeaveDao {
    @Query("SELECT * FROM leave_records ORDER BY date DESC")
    fun getAllLeaveRecords(): Flow<List<LeaveRecordEntity>>

    @Query("SELECT * FROM leave_records WHERE substr(date, 1, 4) = :year ORDER BY date DESC")
    fun getLeaveRecordsByYear(year: String): Flow<List<LeaveRecordEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLeaveRecord(record: LeaveRecordEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLeaveRecords(records: List<LeaveRecordEntity>)

    @Update
    suspend fun updateLeaveRecord(record: LeaveRecordEntity)

    @Delete
    suspend fun deleteLeaveRecord(record: LeaveRecordEntity)

    @Query("DELETE FROM leave_records")
    suspend fun deleteAllLeaveRecords()

    @Query("SELECT * FROM leave_year_settings WHERE year = :year")
    fun getYearSetting(year: Int): Flow<LeaveYearSettingEntity?>

    @Query("SELECT * FROM leave_year_settings ORDER BY year DESC")
    fun getAllYearSettings(): Flow<List<LeaveYearSettingEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertYearSetting(setting: LeaveYearSettingEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertYearSettings(settings: List<LeaveYearSettingEntity>)

    @Query("DELETE FROM leave_year_settings")
    suspend fun deleteAllYearSettings()

    @Transaction
    suspend fun replaceAll(
        records: List<LeaveRecordEntity>,
        settings: List<LeaveYearSettingEntity>
    ) {
        deleteAllLeaveRecords()
        deleteAllYearSettings()
        insertLeaveRecords(records)
        upsertYearSettings(settings)
    }
}
