package com.androidkotlin.timestampkt.presentation.settings

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.androidkotlin.timestampkt.data.preferences.SettingsManager
import com.androidkotlin.timestampkt.presentation.main.MainViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: MainViewModel,
    settingsManager: SettingsManager,
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val autoRecordEnabled = settingsManager.autoRecordEnabled.collectAsState(initial = false)
    val backupMessage by viewModel.backupMessage.collectAsState()
    var pendingBackupJson by remember { mutableStateOf<String?>(null) }

    // 삭제 확인 다이얼로그 상태
    var showDeleteAllDialog by remember { mutableStateOf(false) }

    val createBackupLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        val backupJson = pendingBackupJson
        pendingBackupJson = null

        if (uri == null || backupJson == null) return@rememberLauncherForActivityResult

        scope.launch {
            runCatching {
                withContext(Dispatchers.IO) {
                    val outputStream = context.contentResolver.openOutputStream(uri)
                        ?: error("백업 파일을 열 수 없습니다.")

                    outputStream.bufferedWriter(Charsets.UTF_8).use { writer ->
                        writer.write(backupJson)
                    }
                }
            }.onSuccess {
                viewModel.showBackupMessage("백업 파일을 저장했습니다.")
            }.onFailure { error ->
                viewModel.showBackupMessage(error.message ?: "백업 파일을 저장하지 못했습니다.")
            }
        }
    }

    val openBackupLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult

        scope.launch {
            runCatching {
                withContext(Dispatchers.IO) {
                    val inputStream = context.contentResolver.openInputStream(uri)
                        ?: error("백업 파일을 열 수 없습니다.")

                    inputStream.bufferedReader(Charsets.UTF_8).use { reader ->
                        reader.readText()
                    }
                }
            }.onSuccess { backupJson ->
                viewModel.restoreBackup(backupJson)
            }.onFailure { error ->
                viewModel.showBackupMessage(error.message ?: "백업 파일을 불러오지 못했습니다.")
            }
        }
    }

    LaunchedEffect(backupMessage) {
        backupMessage?.let { message ->
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
            viewModel.clearBackupMessage()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("설정") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "뒤로가기"
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 앱 기본 설정 카드
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "앱 기본 설정",
                        style = MaterialTheme.typography.titleMedium
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = "앱 시작 시 자동 기록",
                                style = MaterialTheme.typography.bodyLarge
                            )
                            Text(
                                text = "앱을 시작할 때 자동으로 출근 기록 추가",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Switch(
                            checked = autoRecordEnabled.value,
                            onCheckedChange = { checked ->
                                scope.launch {
                                    settingsManager.setAutoRecordEnabled(checked)
                                }
                            }
                        )
                    }
                }
            }

            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "백업 / 복원",
                        style = MaterialTheme.typography.titleMedium
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedButton(
                        onClick = {
                            viewModel.createBackupJson { backupJson ->
                                pendingBackupJson = backupJson
                                val timestamp = SimpleDateFormat(
                                    "yyyyMMdd_HHmmss",
                                    Locale.KOREA
                                ).format(Date())
                                createBackupLauncher.launch("timestamp_backup_$timestamp.json")
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = Icons.Default.FileDownload,
                            contentDescription = null
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("백업 파일 저장")
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedButton(
                        onClick = {
                            openBackupLauncher.launch(
                                arrayOf("application/json", "text/*", "*/*")
                            )
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = Icons.Default.FileUpload,
                            contentDescription = null
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("백업 파일 불러오기")
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "불러오면 현재 출근 기록이 백업 파일 내용으로 교체됩니다.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // 데이터 관리 카드 추가
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "데이터 관리",
                        style = MaterialTheme.typography.titleMedium
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // 전체 기록 삭제 버튼
                    OutlinedButton(
                        onClick = { showDeleteAllDialog = true },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        ),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.error)
                    ) {
                        Icon(
                            imageVector = Icons.Default.DeleteForever,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "모든 출근 기록 삭제",
                            color = MaterialTheme.colorScheme.error
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "경고: 이 작업은 실행 취소할 수 없습니다",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error.copy(alpha = 0.8f)
                    )
                }
            }
        }
    }

    // 전체 삭제 확인 다이얼로그
    if (showDeleteAllDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteAllDialog = false },
            icon = {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error
                )
            },
            title = { Text("모든 출근 기록 삭제") },
            text = {
                Text(
                    "정말로 모든 출근 기록을 삭제하시겠습니까?\n\n이 작업은 되돌릴 수 없으며 모든 기록이 영구적으로 삭제됩니다."
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteAllRecords()
                        showDeleteAllDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("삭제")
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { showDeleteAllDialog = false }
                ) {
                    Text("취소")
                }
            }
        )
    }
}
