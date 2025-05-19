// MainActivity.kt
package com.androidkotlin.timestampkt.presentation.main

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.androidkotlin.timestampkt.presentation.settings.SettingsScreen
import com.androidkotlin.timestampkt.ui.theme.TimeStampKtTheme
import dagger.hilt.android.AndroidEntryPoint
import com.androidkotlin.timestampkt.data.preferences.SettingsManager
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    @Inject
    lateinit var settingsManager: SettingsManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TimeStampKtTheme {
                TimeStampApp(viewModel, settingsManager)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimeStampApp(
    viewModel: MainViewModel,
    settingsManager: SettingsManager
) {
    var currentScreen by remember { mutableStateOf("main") }

    when (currentScreen) {
        "main" -> {
            MainScreen(
                viewModel = viewModel,
                settingsManager = settingsManager,
                onNavigateToSettings = { currentScreen = "settings" }
            )
        }
        "settings" -> {
            SettingsScreen(
                viewModel = viewModel,
                settingsManager = settingsManager,
                onBackClick = { currentScreen = "main" }
            )
        }
    }
}