package com.androidkotlin.timestampkt.domain.usecase

import com.androidkotlin.timestampkt.domain.model.LeaveRecord
import com.androidkotlin.timestampkt.domain.model.LeaveYearSetting
import com.androidkotlin.timestampkt.domain.model.TimeRecord
import com.androidkotlin.timestampkt.domain.repository.LeaveRepository
import com.androidkotlin.timestampkt.domain.repository.TimeRecordRepository
import org.json.JSONObject
import javax.inject.Inject

class ImportTimeRecordsBackupUseCase @Inject constructor(
    private val repository: TimeRecordRepository,
    private val leaveRepository: LeaveRepository
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

        val leaveRecordsJson = root.optJSONArray("leaveRecords")
        val leaveRecords = buildList {
            if (leaveRecordsJson != null) {
                for (index in 0 until leaveRecordsJson.length()) {
                    val item = leaveRecordsJson.getJSONObject(index)
                    add(
                        LeaveRecord(
                            id = item.getString("id"),
                            type = item.getString("type"),
                            date = item.getString("date"),
                            amount = item.getDouble("amount"),
                            note = item.optString("note", "")
                        )
                    )
                }
            }
        }

        val leaveYearSettingsJson = root.optJSONArray("leaveYearSettings")
        val leaveYearSettings = buildList {
            if (leaveYearSettingsJson != null) {
                for (index in 0 until leaveYearSettingsJson.length()) {
                    val item = leaveYearSettingsJson.getJSONObject(index)
                    add(
                        LeaveYearSetting(
                            year = item.getInt("year"),
                            totalDays = item.getDouble("totalDays")
                        )
                    )
                }
            }
        }

        repository.replaceAllRecords(records)
        leaveRepository.replaceAll(leaveRecords, leaveYearSettings)
        return records.size
    }
}
