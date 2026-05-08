package com.androidkotlin.timestampkt.domain.usecase

import com.androidkotlin.timestampkt.domain.repository.TimeRecordRepository
import kotlinx.coroutines.flow.first
import org.json.JSONArray
import org.json.JSONObject
import javax.inject.Inject

class ExportTimeRecordsBackupUseCase @Inject constructor(
    private val repository: TimeRecordRepository
) {
    suspend operator fun invoke(): String {
        val records = repository.getAllRecords().first()
        val recordArray = JSONArray()

        records.forEach { record ->
            recordArray.put(
                JSONObject()
                    .put("id", record.id)
                    .put("timestamp", record.timestamp)
                    .put("note", record.note)
            )
        }

        return JSONObject()
            .put("schemaVersion", 1)
            .put("createdAt", System.currentTimeMillis())
            .put("records", recordArray)
            .toString(2)
    }
}
