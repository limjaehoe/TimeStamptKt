package com.androidkotlin.timestampkt.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.androidkotlin.timestampkt.domain.model.LeaveYearSetting

@Entity(tableName = "leave_year_settings")
data class LeaveYearSettingEntity(
    @PrimaryKey
    val year: Int,
    val totalDays: Double
) {
    fun toDomainModel(): LeaveYearSetting {
        return LeaveYearSetting(
            year = year,
            totalDays = totalDays
        )
    }

    companion object {
        fun fromDomainModel(setting: LeaveYearSetting): LeaveYearSettingEntity {
            return LeaveYearSettingEntity(
                year = setting.year,
                totalDays = setting.totalDays
            )
        }
    }
}
