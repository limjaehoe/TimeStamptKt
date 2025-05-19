// TimeRecordViewModel.kt
package com.androidkotlin.timestampkt.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.androidkotlin.timestampkt.data.TimeRecord
import com.androidkotlin.timestampkt.data.TimeRecordDao
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class TimeRecordViewModel(private val timeRecordDao: TimeRecordDao) : ViewModel() {

    // 일반 Flow 목록은 유지 (일부 기능에서 사용할 수 있음)
    val allRecords: Flow<List<TimeRecord>> = timeRecordDao.getAllRecords()

    // 페이징된 데이터를 위한 Flow
    val pagedRecords: Flow<PagingData<TimeRecord>> = Pager(
        config = PagingConfig(
            pageSize = 20,          // 한 페이지에 로드할 항목 수
            enablePlaceholders = false,  // 플레이스홀더 비활성화
            initialLoadSize = 40    // 첫 로드 시 항목 수
        )
    ) {
        timeRecordDao.getPagedRecords()
    }.flow.cachedIn(viewModelScope)

    fun addRecord(record: TimeRecord) {
        viewModelScope.launch {
            timeRecordDao.insert(record)
        }
    }

    fun updateRecord(record: TimeRecord) {
        viewModelScope.launch {
            timeRecordDao.update(record)
        }
    }

    fun deleteRecord(record: TimeRecord) {
        viewModelScope.launch {
            timeRecordDao.delete(record)
        }
    }

    fun deleteAllRecords() {
        viewModelScope.launch {
            timeRecordDao.deleteAll()
        }
    }

    suspend fun isFirstLaunch(): Boolean {
        return timeRecordDao.getCount() == 0
    }
}

class TimeRecordViewModelFactory(private val timeRecordDao: TimeRecordDao) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TimeRecordViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return TimeRecordViewModel(timeRecordDao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}