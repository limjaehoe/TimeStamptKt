// presentation/main/MainScreen.kt
package com.androidkotlin.timestampkt.presentation.main

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemKey
import com.androidkotlin.timestampkt.data.preferences.SettingsManager
import com.androidkotlin.timestampkt.domain.model.TimeRecord
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    viewModel: MainViewModel,
    settingsManager: SettingsManager,
    onNavigateToSettings: () -> Unit
) {
    var showEditDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var currentRecord by remember { mutableStateOf<TimeRecord?>(null) }
    var noteText by remember { mutableStateOf("") }

    // 앱 시작 시 자동 기록 추가
    val scope = rememberCoroutineScope()
    val autoRecordEnabled = settingsManager.autoRecordEnabled.collectAsState(initial = false)

    // 앱 시작 시 한 번만 실행
    LaunchedEffect(key1 = Unit) {
        val isAutoEnabled = settingsManager.autoRecordEnabled.first()
        if (isAutoEnabled) {
            viewModel.addRecord("앱 시작 시 자동 기록")
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text("회사 출근 기록") },
                actions = {
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "설정"
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        TimeStampScreen(
            modifier = Modifier.padding(innerPadding),
            viewModel = viewModel,
            onEditRecord = { record ->
                currentRecord = record
                noteText = record.note
                showEditDialog = true
            },
            onDeleteRecord = { record ->
                currentRecord = record
                showDeleteDialog = true
            }
        )
    }

    // 수정 다이얼로그
    if (showEditDialog && currentRecord != null) {
        AlertDialog(
            onDismissRequest = { showEditDialog = false },
            title = { Text("기록 수정") },
            text = {
                OutlinedTextField(
                    value = noteText,
                    onValueChange = { noteText = it },
                    label = { Text("메모") },
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        currentRecord?.let {
                            viewModel.updateRecord(
                                it.copy(note = noteText)
                            )
                        }
                        showEditDialog = false
                    }
                ) {
                    Text("저장")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showEditDialog = false }
                ) {
                    Text("취소")
                }
            }
        )
    }

    // 삭제 확인 다이얼로그
    if (showDeleteDialog && currentRecord != null) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("기록 삭제") },
            text = { Text("이 출근 기록을 삭제하시겠습니까?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        currentRecord?.let {
                            viewModel.deleteRecord(it)
                        }
                        showDeleteDialog = false
                    }
                ) {
                    Text("삭제")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showDeleteDialog = false }
                ) {
                    Text("취소")
                }
            }
        )
    }
}

@Composable
fun TimeStampScreen(
    modifier: Modifier = Modifier,
    viewModel: MainViewModel,
    onEditRecord: (TimeRecord) -> Unit,
    onDeleteRecord: (TimeRecord) -> Unit
) {
    var currentTime by remember { mutableStateOf(System.currentTimeMillis()) }
    val lazyPagingItems = viewModel.pagedRecords.collectAsLazyPagingItems()
    val allRecords by viewModel.allRecords.collectAsState(initial = emptyList())

    // LazyListState 추가
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()

    // 데이터가 변경될 때 자동으로 맨 위로 스크롤
    LaunchedEffect(lazyPagingItems.itemCount) {
        if (lazyPagingItems.itemCount > 0) {
            scope.launch {
                listState.animateScrollToItem(0)
            }
        }
    }

    // 1초마다 시간 업데이트
    LaunchedEffect(key1 = true) {
        while (true) {
            delay(1000)
            currentTime = System.currentTimeMillis()
        }
    }

    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 현재 시간 표시 카드
        Card(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "현재 시간",
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.secondary
                )

                Spacer(modifier = Modifier.height(4.dp))

                val dateFormat = SimpleDateFormat("yyyy년 MM월 dd일 HH:mm:ss", Locale.KOREA)
                Text(
                    text = dateFormat.format(Date(currentTime)),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
            }
        }

        // 타임스탬프 리스트 헤더
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "출근 기록",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "총 ${allRecords.size}개의 기록",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.secondary
            )
        }

        // 페이징된 기록 목록
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            state = listState
        ) {
            items(
                count = lazyPagingItems.itemCount,
                key = lazyPagingItems.itemKey { it.id }
            ) { index ->
                val record = lazyPagingItems[index]
                if (record != null) {
                    TimeRecordItem(
                        record = record,
                        onEditClick = { onEditRecord(record) },
                        onDeleteClick = { onDeleteRecord(record) }
                    )
                } else {
                    LoadingTimeRecordItem()
                }
            }

            // 로딩 상태 표시
            when (lazyPagingItems.loadState.append) {
                is LoadState.Loading -> {
                    item { LoadingIndicator() }
                }
                is LoadState.Error -> {
                    item { ErrorItem { lazyPagingItems.retry() } }
                }
                else -> {}
            }
        }

        // 새 기록 추가 버튼
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Button(
                onClick = {
                    viewModel.addRecord("")
                    scope.launch {
                        delay(100)
                        listState.animateScrollToItem(0)
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add"
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "새 출근 기록 추가",
                    fontSize = 16.sp,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
        }
    }
}

// LoadingTimeRecordItem, LoadingIndicator, ErrorItem, TimeRecordItem 함수는 기존 코드 유지
@Composable
fun LoadingTimeRecordItem() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .height(100.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
        ) {
            CircularProgressIndicator(
                modifier = Modifier
                    .size(24.dp)
                    .align(Alignment.Center),
                strokeWidth = 2.dp
            )
        }
    }
}

@Composable
fun LoadingIndicator() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(32.dp),
            color = MaterialTheme.colorScheme.primary
        )
    }
}

@Composable
fun ErrorItem(onRetry: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "데이터를 불러오는 중 오류가 발생했습니다",
                color = MaterialTheme.colorScheme.error
            )
            Spacer(modifier = Modifier.height(8.dp))
            Button(onClick = onRetry) {
                Text("다시 시도")
            }
        }
    }
}

@Composable
fun TimeRecordItem(
    record: TimeRecord,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    val dateFormat = SimpleDateFormat("yyyy년 MM월 dd일 (E)", Locale.KOREA)
    val timeFormat = SimpleDateFormat("HH:mm:ss", Locale.KOREA)

    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = dateFormat.format(Date(record.timestamp)),
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Text(
                        text = timeFormat.format(Date(record.timestamp)),
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    if (record.note.isNotBlank()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = record.note,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Box {
                    IconButton(onClick = { expanded = true }) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "More Options"
                        )
                    }

                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("수정") },
                            onClick = {
                                onEditClick()
                                expanded = false
                            },
                            leadingIcon = {
                                Icon(
                                    Icons.Default.Edit,
                                    contentDescription = "Edit"
                                )
                            }
                        )

                        DropdownMenuItem(
                            text = { Text("삭제") },
                            onClick = {
                                onDeleteClick()
                                expanded = false
                            },
                            leadingIcon = {
                                Icon(
                                    Icons.Default.Delete,
                                    contentDescription = "Delete"
                                )
                            }
                        )
                    }
                }
            }
        }
    }
}