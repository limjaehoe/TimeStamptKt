package com.androidkotlin.timestampkt

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber

@HiltAndroidApp
class TimeStampKtApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        // Timber 초기화
        Timber.plant(Timber.DebugTree())
    }
}