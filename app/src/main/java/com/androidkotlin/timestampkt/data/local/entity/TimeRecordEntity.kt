// data/local/entity/TimeRecordEntity.kt
package com.androidkotlin.timestampkt.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.androidkotlin.timestampkt.domain.model.TimeRecord

@Entity(tableName = "time_records")
data class TimeRecordEntity(
    @PrimaryKey
    val id: String,
    val timestamp: Long,
    val note: String
) {
    fun toDomainModel(): TimeRecord {
        return TimeRecord(
            id = id,
            timestamp = timestamp,
            note = note
        )
    }

    companion object {
        fun fromDomainModel(domainModel: TimeRecord): TimeRecordEntity {
            return TimeRecordEntity(
                id = domainModel.id,
                timestamp = domainModel.timestamp,
                note = domainModel.note
            )
        }
    }
}