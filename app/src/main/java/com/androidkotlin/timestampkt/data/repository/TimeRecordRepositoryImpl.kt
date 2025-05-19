// data/repository/TimeRecordRepositoryImpl.kt
package com.androidkotlin.timestampkt.data.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.androidkotlin.timestampkt.data.local.dao.TimeRecordDao
import com.androidkotlin.timestampkt.domain.model.TimeRecord
import com.androidkotlin.timestampkt.domain.repository.TimeRecordRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject


class TimeRecordRepositoryImpl @Inject constructor(
    private val timeRecordDao: TimeRecordDao
) : TimeRecordRepository {

    override fun getAllRecords(): Flow<List<TimeRecord>> {
        return timeRecordDao.getAllRecords() // 변환 없이 직접 사용
    }

    override fun getPagedRecords(): Flow<PagingData<TimeRecord>> {
        return Pager(
            config = PagingConfig(
                pageSize = 20,
                enablePlaceholders = false,
                initialLoadSize = 40
            )
        ) {
            timeRecordDao.getPagedRecords()
        }.flow // 변환 없이 직접 사용
    }

    override suspend fun addRecord(record: TimeRecord) {
        timeRecordDao.insert(record) // 변환 없이 직접 사용
    }

    override suspend fun updateRecord(record: TimeRecord) {
        timeRecordDao.update(record) // 변환 없이 직접 사용
    }

    override suspend fun deleteRecord(record: TimeRecord) {
        timeRecordDao.delete(record) // 변환 없이 직접 사용
    }

    override suspend fun deleteAllRecords() {
        timeRecordDao.deleteAll()
    }

    override suspend fun getRecordCount(): Int {
        return timeRecordDao.getCount()
    }
}