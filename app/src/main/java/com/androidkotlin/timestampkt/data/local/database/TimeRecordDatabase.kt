package com.androidkotlin.timestampkt.data.local.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.androidkotlin.timestampkt.data.local.dao.TimeRecordDao
import com.androidkotlin.timestampkt.data.local.entity.TimeRecordEntity
import com.androidkotlin.timestampkt.domain.model.TimeRecord

@Database(entities = [TimeRecordEntity::class], version = 2, exportSchema = false)
abstract class TimeRecordDatabase : RoomDatabase() {

    abstract fun timeRecordDao(): TimeRecordDao

    companion object {
        @Volatile
        private var INSTANCE: TimeRecordDatabase? = null

        fun getDatabase(context: Context): TimeRecordDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    TimeRecordDatabase::class.java,
                    "timerecord_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}