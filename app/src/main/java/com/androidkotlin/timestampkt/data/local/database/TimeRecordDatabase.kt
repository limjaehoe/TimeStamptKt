package com.androidkotlin.timestampkt.data.local.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.androidkotlin.timestampkt.data.local.dao.LeaveDao
import com.androidkotlin.timestampkt.data.local.dao.TimeRecordDao
import com.androidkotlin.timestampkt.data.local.entity.LeaveRecordEntity
import com.androidkotlin.timestampkt.data.local.entity.LeaveYearSettingEntity
import com.androidkotlin.timestampkt.data.local.entity.TimeRecordEntity

@Database(
    entities = [
        TimeRecordEntity::class,
        LeaveRecordEntity::class,
        LeaveYearSettingEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class TimeRecordDatabase : RoomDatabase() {

    abstract fun timeRecordDao(): TimeRecordDao
    abstract fun leaveDao(): LeaveDao

    companion object {
        @Volatile
        private var INSTANCE: TimeRecordDatabase? = null

        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS leave_records (
                        id TEXT NOT NULL PRIMARY KEY,
                        type TEXT NOT NULL,
                        date TEXT NOT NULL,
                        amount REAL NOT NULL,
                        note TEXT NOT NULL
                    )
                    """.trimIndent()
                )
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS leave_year_settings (
                        year INTEGER NOT NULL PRIMARY KEY,
                        totalDays REAL NOT NULL
                    )
                    """.trimIndent()
                )
            }
        }

        fun getDatabase(context: Context): TimeRecordDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    TimeRecordDatabase::class.java,
                    "timerecord_database"
                )
                    .addMigrations(MIGRATION_1_2)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
