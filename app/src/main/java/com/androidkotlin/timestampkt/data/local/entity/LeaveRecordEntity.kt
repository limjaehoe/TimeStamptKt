package com.androidkotlin.timestampkt.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.androidkotlin.timestampkt.domain.model.LeaveRecord

@Entity(tableName = "leave_records")
data class LeaveRecordEntity(
    @PrimaryKey
    val id: String,
    val type: String,
    val date: String,
    val amount: Double,
    val note: String
) {
    fun toDomainModel(): LeaveRecord {
        return LeaveRecord(
            id = id,
            type = type,
            date = date,
            amount = amount,
            note = note
        )
    }

    companion object {
        fun fromDomainModel(record: LeaveRecord): LeaveRecordEntity {
            return LeaveRecordEntity(
                id = record.id,
                type = record.type,
                date = record.date,
                amount = record.amount,
                note = record.note
            )
        }
    }
}
