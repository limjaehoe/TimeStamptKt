package com.androidkotlin.timestampkt.data.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.map
import com.androidkotlin.timestampkt.data.local.dao.TimeRecordDao
import com.androidkotlin.timestampkt.data.local.entity.TimeRecordEntity
import com.androidkotlin.timestampkt.domain.model.TimeRecord
import com.androidkotlin.timestampkt.domain.repository.TimeRecordRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class TimeRecordRepositoryImpl @Inject constructor(
    private val timeRecordDao: TimeRecordDao
) : TimeRecordRepository {

    override fun getAllRecords(): Flow<List<TimeRecord>> {
        return timeRecordDao.getAllRecords().map { entities ->
            entities.map { it.toDomainModel() }
        }
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
        }.flow.map { pagingData ->
            pagingData.map { it.toDomainModel() }
        }
    }

    override suspend fun addRecord(record: TimeRecord) {
        timeRecordDao.insert(TimeRecordEntity.fromDomainModel(record))
    }

    override suspend fun updateRecord(record: TimeRecord) {
        timeRecordDao.update(TimeRecordEntity.fromDomainModel(record))
    }

    override suspend fun deleteRecord(record: TimeRecord) {
        timeRecordDao.delete(TimeRecordEntity.fromDomainModel(record))
    }

    override suspend fun deleteAllRecords() {
        timeRecordDao.deleteAll()
    }

    override suspend fun getRecordCount(): Int {
        return timeRecordDao.getCount()
    }
}