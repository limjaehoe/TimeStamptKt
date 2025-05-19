package com.androidkotlin.timestampkt.presentation.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.androidkotlin.timestampkt.domain.model.TimeRecord
import com.androidkotlin.timestampkt.domain.usecase.AddTimeRecordUseCase
import com.androidkotlin.timestampkt.domain.usecase.DeleteAllTimeRecordsUseCase
import com.androidkotlin.timestampkt.domain.usecase.DeleteTimeRecordUseCase
import com.androidkotlin.timestampkt.domain.usecase.GetPagedTimeRecordsUseCase
import com.androidkotlin.timestampkt.domain.usecase.GetTimeRecordsUseCase
import com.androidkotlin.timestampkt.domain.usecase.UpdateTimeRecordUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val getTimeRecordsUseCase: GetTimeRecordsUseCase,
    private val getPagedTimeRecordsUseCase: GetPagedTimeRecordsUseCase,
    private val addTimeRecordUseCase: AddTimeRecordUseCase,
    private val updateTimeRecordUseCase: UpdateTimeRecordUseCase,
    private val deleteTimeRecordUseCase: DeleteTimeRecordUseCase,
    private val deleteAllTimeRecordsUseCase: DeleteAllTimeRecordsUseCase
) : ViewModel() {

    val allRecords: Flow<List<TimeRecord>> = getTimeRecordsUseCase()
    val pagedRecords: Flow<PagingData<TimeRecord>> = getPagedTimeRecordsUseCase().cachedIn(viewModelScope)

    fun addRecord(note: String = "") {
        viewModelScope.launch {
            addTimeRecordUseCase(note)
        }
    }

    fun updateRecord(record: TimeRecord) {
        viewModelScope.launch {
            updateTimeRecordUseCase(record)
        }
    }

    fun deleteRecord(record: TimeRecord) {
        viewModelScope.launch {
            deleteTimeRecordUseCase(record)
        }
    }

    fun deleteAllRecords() {
        viewModelScope.launch {
            deleteAllTimeRecordsUseCase()
        }
    }
}