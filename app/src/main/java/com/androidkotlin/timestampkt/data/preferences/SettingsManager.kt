package com.androidkotlin.timestampkt.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// Context 확장 함수로 DataStore 인스턴스 생성
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")


class SettingsManager(private val context: Context) {

    // 자동 기록 설정의 키 정의
    private val AUTO_RECORD_KEY = booleanPreferencesKey("auto_record_enabled")
    // 마지막 자동 기록 날짜 키 추가
    private val LAST_AUTO_RECORD_DATE_KEY = stringPreferencesKey("last_auto_record_date")

    // 자동 기록 활성화 여부를 Flow로 제공
    val autoRecordEnabled: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            // 기본값은 false로 변경 (기본적으로는 자동 기록 비활성화)
            preferences[AUTO_RECORD_KEY] ?: false
        }

    // 현재 설정값을 직접 가져오는 일회성 함수 추가
    suspend fun getAutoRecordEnabled(): Boolean {
        return context.dataStore.data.first()[AUTO_RECORD_KEY] ?: false
    }

    // 자동 기록 설정 변경 함수
    suspend fun setAutoRecordEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[AUTO_RECORD_KEY] = enabled
        }
    }

    // 오늘 이미 자동 기록했는지 확인하는 함수
    suspend fun shouldAddAutoRecord(): Boolean {
        val preferences = context.dataStore.data.first()
        val isAutoEnabled = preferences[AUTO_RECORD_KEY] ?: false

        if (!isAutoEnabled) return false

        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val lastRecordDate = preferences[LAST_AUTO_RECORD_DATE_KEY]

        return lastRecordDate != today
    }

    // 자동 기록 날짜 업데이트
    suspend fun updateLastAutoRecordDate() {
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        context.dataStore.edit { preferences ->
            preferences[LAST_AUTO_RECORD_DATE_KEY] = today
        }
    }
}