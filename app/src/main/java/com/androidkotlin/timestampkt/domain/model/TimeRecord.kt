package com.androidkotlin.timestampkt.domain.model


import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "time_records")
data class TimeRecord(
    @PrimaryKey
    val id: String = java.util.UUID.randomUUID().toString(),
    val timestamp: Long = System.currentTimeMillis(),
    val note: String = ""
)