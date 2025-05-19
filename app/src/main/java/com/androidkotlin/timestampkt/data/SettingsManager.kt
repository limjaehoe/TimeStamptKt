// SettingsManager.kt
package com.androidkotlin.timestampkt.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore

// Context 확장 함수로 DataStore 인스턴스 생성
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

