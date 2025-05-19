package com.androidkotlin.timestampkt.di.module

import android.content.Context
import com.androidkotlin.timestampkt.data.local.dao.TimeRecordDao
import com.androidkotlin.timestampkt.data.preferences.SettingsManager
import com.androidkotlin.timestampkt.data.local.database.TimeRecordDatabase
import com.androidkotlin.timestampkt.data.repository.TimeRecordRepositoryImpl
import com.androidkotlin.timestampkt.domain.repository.TimeRecordRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): TimeRecordDatabase {
        return TimeRecordDatabase.getDatabase(context)
    }

    @Provides
    @Singleton
    fun provideTimeRecordDao(database: TimeRecordDatabase) = database.timeRecordDao()

    @Provides
    @Singleton
    fun provideSettingsManager(@ApplicationContext context: Context): SettingsManager {
        return SettingsManager(context)
    }

    @Provides
    @Singleton
    fun provideTimeRecordRepository(timeRecordDao: TimeRecordDao): TimeRecordRepository {
        return TimeRecordRepositoryImpl(timeRecordDao)
    }
}