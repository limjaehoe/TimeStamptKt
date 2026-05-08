package com.androidkotlin.timestampkt.presentation.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.androidkotlin.timestampkt.domain.model.TimeRecord
import com.androidkotlin.timestampkt.domain.usecase.AddTimeRecordUseCase
import com.androidkotlin.timestampkt.domain.usecase.DeleteAllTimeRecordsUseCase
import com.androidkotlin.timestampkt.domain.usecase.DeleteTimeRecordUseCase
import com.androidkotlin.timestampkt.domain.usecase.ExportTimeRecordsBackupUseCase
import com.androidkotlin.timestampkt.domain.usecase.GetPagedTimeRecordsUseCase
import com.androidkotlin.timestampkt.domain.usecase.GetTimeRecordsUseCase
import com.androidkotlin.timestampkt.domain.usecase.ImportTimeRecordsBackupUseCase
import com.androidkotlin.timestampkt.domain.usecase.UpdateTimeRecordUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
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
    private val deleteAllTimeRecordsUseCase: DeleteAllTimeRecordsUseCase,
    private val exportTimeRecordsBackupUseCase: ExportTimeRecordsBackupUseCase,
    private val importTimeRecordsBackupUseCase: ImportTimeRecordsBackupUseCase
) : ViewModel() {

    val allRecords: Flow<List<TimeRecord>> = getTimeRecordsUseCase()
    val pagedRecords: Flow<PagingData<TimeRecord>> = getPagedTimeRecordsUseCase().cachedIn(viewModelScope)

    private val _backupMessage = MutableStateFlow<String?>(null)
    val backupMessage: StateFlow<String?> = _backupMessage.asStateFlow()

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

    fun createBackupJson(onBackupCreated: (String) -> Unit) {
        viewModelScope.launch {
            runCatching {
                exportTimeRecordsBackupUseCase()
            }.onSuccess { backupJson ->
                onBackupCreated(backupJson)
            }.onFailure { error ->
                _backupMessage.value = error.message ?: "백업 파일을 만들지 못했습니다."
            }
        }
    }

    fun restoreBackup(backupJson: String) {
        viewModelScope.launch {
            runCatching {
                importTimeRecordsBackupUseCase(backupJson)
            }.onSuccess { count ->
                _backupMessage.value = "${count}개의 기록을 복원했습니다."
            }.onFailure { error ->
                _backupMessage.value = error.message ?: "백업 파일을 불러오지 못했습니다."
            }
        }
    }

    fun showBackupMessage(message: String) {
        _backupMessage.value = message
    }

    fun clearBackupMessage() {
        _backupMessage.value = null
    }
}
