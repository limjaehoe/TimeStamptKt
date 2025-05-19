package com.androidkotlin.timestampkt.domain.usecase

import com.androidkotlin.timestampkt.domain.model.TimeRecord
import com.androidkotlin.timestampkt.domain.repository.TimeRecordRepository
import javax.inject.Inject

class UpdateTimeRecordUseCase @Inject constructor(
    private val repository: TimeRecordRepository
) {
    suspend operator fun invoke(record: TimeRecord) {
        repository.updateRecord(record)
    }
}