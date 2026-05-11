package com.androidkotlin.timestampkt.data.repository

import com.androidkotlin.timestampkt.data.local.dao.LeaveDao
import com.androidkotlin.timestampkt.data.local.entity.LeaveRecordEntity
import com.androidkotlin.timestampkt.data.local.entity.LeaveYearSettingEntity
import com.androidkotlin.timestampkt.domain.model.LeaveRecord
import com.androidkotlin.timestampkt.domain.model.LeaveYearSetting
import com.androidkotlin.timestampkt.domain.repository.LeaveRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class LeaveRepositoryImpl @Inject constructor(
    private val leaveDao: LeaveDao
) : LeaveRepository {
    override fun getAllLeaveRecords(): Flow<List<LeaveRecord>> {
        return leaveDao.getAllLeaveRecords().map { records ->
            records.map { it.toDomainModel() }
        }
    }

    override fun getLeaveRecordsByYear(year: Int): Flow<List<LeaveRecord>> {
        return leaveDao.getLeaveRecordsByYear(year.toString()).map { records ->
            records.map { it.toDomainModel() }
        }
    }

    override fun getYearSetting(year: Int): Flow<LeaveYearSetting?> {
        return leaveDao.getYearSetting(year).map { it?.toDomainModel() }
    }

    override fun getAllYearSettings(): Flow<List<LeaveYearSetting>> {
        return leaveDao.getAllYearSettings().map { settings ->
            settings.map { it.toDomainModel() }
        }
    }

    override suspend fun addLeaveRecord(record: LeaveRecord) {
        leaveDao.insertLeaveRecord(LeaveRecordEntity.fromDomainModel(record))
    }

    override suspend fun addLeaveRecords(records: List<LeaveRecord>) {
        leaveDao.insertLeaveRecords(records.map { LeaveRecordEntity.fromDomainModel(it) })
    }

    override suspend fun updateLeaveRecord(record: LeaveRecord) {
        leaveDao.updateLeaveRecord(LeaveRecordEntity.fromDomainModel(record))
    }

    override suspend fun deleteLeaveRecord(record: LeaveRecord) {
        leaveDao.deleteLeaveRecord(LeaveRecordEntity.fromDomainModel(record))
    }

    override suspend fun saveYearSetting(setting: LeaveYearSetting) {
        leaveDao.upsertYearSetting(LeaveYearSettingEntity.fromDomainModel(setting))
    }

    override suspend fun replaceAll(records: List<LeaveRecord>, settings: List<LeaveYearSetting>) {
        leaveDao.replaceAll(
            records = records.map { LeaveRecordEntity.fromDomainModel(it) },
            settings = settings.map { LeaveYearSettingEntity.fromDomainModel(it) }
        )
    }
}
