package com.androidkotlin.timestampkt.domain.usecase

import com.androidkotlin.timestampkt.domain.model.TimeRecord
import com.androidkotlin.timestampkt.domain.repository.TimeRecordRepository
import java.util.UUID
import javax.inject.Inject

class AddTimeRecordUseCase @Inject constructor(
    private val repository: TimeRecordRepository
) {
    suspend operator fun invoke(note: String = "") {
        val record = TimeRecord(
            id = UUID.randomUUID().toString(),
            timestamp = System.currentTimeMillis(),
            note = note
        )
        repository.addRecord(record)
    }
}