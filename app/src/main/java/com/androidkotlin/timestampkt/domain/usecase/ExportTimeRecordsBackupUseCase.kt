package com.androidkotlin.timestampkt.domain.usecase

import com.androidkotlin.timestampkt.domain.repository.TimeRecordRepository
import com.androidkotlin.timestampkt.domain.repository.LeaveRepository
import kotlinx.coroutines.flow.first
import org.json.JSONArray
import org.json.JSONObject
import javax.inject.Inject

class ExportTimeRecordsBackupUseCase @Inject constructor(
    private val repository: TimeRecordRepository,
    private val leaveRepository: LeaveRepository
) {
    suspend operator fun invoke(): String {
        val records = repository.getAllRecords().first()
        val leaveRecords = leaveRepository.getAllLeaveRecords().first()
        val leaveYearSettings = leaveRepository.getAllYearSettings().first()
        val recordArray = JSONArray()
        val leaveRecordArray = JSONArray()
        val leaveYearSettingArray = JSONArray()

        records.forEach { record ->
            recordArray.put(
                JSONObject()
                    .put("id", record.id)
                    .put("timestamp", record.timestamp)
                    .put("note", record.note)
            )
        }

        leaveRecords.forEach { record ->
            leaveRecordArray.put(
                JSONObject()
                    .put("id", record.id)
                    .put("type", record.type)
                    .put("date", record.date)
                    .put("amount", record.amount)
                    .put("note", record.note)
            )
        }

        leaveYearSettings.forEach { setting ->
            leaveYearSettingArray.put(
                JSONObject()
                    .put("year", setting.year)
                    .put("totalDays", setting.totalDays)
            )
        }

        return JSONObject()
            .put("schemaVersion", 1)
            .put("createdAt", System.currentTimeMillis())
            .put("records", recordArray)
            .put("leaveRecords", leaveRecordArray)
            .put("leaveYearSettings", leaveYearSettingArray)
            .toString(2)
    }
}
