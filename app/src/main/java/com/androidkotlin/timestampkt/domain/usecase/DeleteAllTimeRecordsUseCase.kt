package com.androidkotlin.timestampkt.domain.usecase

import com.androidkotlin.timestampkt.domain.repository.TimeRecordRepository
import javax.inject.Inject

class DeleteAllTimeRecordsUseCase @Inject constructor(
    private val repository: TimeRecordRepository
) {
    suspend operator fun invoke() {
        repository.deleteAllRecords()
    }
}