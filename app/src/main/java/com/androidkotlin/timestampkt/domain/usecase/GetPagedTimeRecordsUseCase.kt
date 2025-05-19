package com.androidkotlin.timestampkt.domain.usecase

import androidx.paging.PagingData
import com.androidkotlin.timestampkt.domain.model.TimeRecord
import com.androidkotlin.timestampkt.domain.repository.TimeRecordRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetPagedTimeRecordsUseCase @Inject constructor(
    private val repository: TimeRecordRepository
) {
    operator fun invoke(): Flow<PagingData<TimeRecord>> {
        return repository.getPagedRecords()
    }
}