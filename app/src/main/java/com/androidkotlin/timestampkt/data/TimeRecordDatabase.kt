// data/TimeRecordDatabase.kt
package com.androidkotlin.timestampkt.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [TimeRecord::class], version = 1, exportSchema = false)
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