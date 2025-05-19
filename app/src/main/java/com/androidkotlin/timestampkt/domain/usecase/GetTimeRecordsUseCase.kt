package com.androidkotlin.timestampkt.domain.usecase


import com.androidkotlin.timestampkt.domain.model.TimeRecord
import com.androidkotlin.timestampkt.domain.repository.TimeRecordRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetTimeRecordsUseCase @Inject constructor(
    private val repository: TimeRecordRepository
) {
    operator fun invoke(): Flow<List<TimeRecord>> {
        return repository.getAllRecords()
    }
}