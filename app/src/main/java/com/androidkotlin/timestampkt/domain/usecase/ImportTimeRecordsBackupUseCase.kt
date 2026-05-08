package com.androidkotlin.timestampkt.domain.usecase

import com.androidkotlin.timestampkt.domain.model.TimeRecord
import com.androidkotlin.timestampkt.domain.repository.TimeRecordRepository
import org.json.JSONObject
import javax.inject.Inject

class ImportTimeRecordsBackupUseCase @Inject constructor(
    private val repository: TimeRecordRepository
) {
    suspend operator fun invoke(backupJson: String): Int {
        val root = JSONObject(backupJson)
        val schemaVersion = root.optInt("schemaVersion", 0)
        require(schemaVersion == 1) { "지원하지 않는 백업 파일입니다." }

        val recordsJson = root.getJSONArray("records")
        val records = buildList {
            for (index in 0 until recordsJson.length()) {
                val item = recordsJson.getJSONObject(index)
                add(
                    TimeRecord(
                        id = item.getString("id"),
                        timestamp = item.getLong("timestamp"),
                        note = item.optString("note", "")
                    )
                )
            }
        }

        repository.replaceAllRecords(records)
        return records.size
    }
}
