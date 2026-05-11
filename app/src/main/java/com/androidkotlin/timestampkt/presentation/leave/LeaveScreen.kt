package com.androidkotlin.timestampkt.presentation.leave

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import com.androidkotlin.timestampkt.domain.model.LeaveRecord
import com.androidkotlin.timestampkt.presentation.leave.HalfDayPosition.FIRST_DAY
import com.androidkotlin.timestampkt.presentation.leave.HalfDayPosition.LAST_DAY
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

private val amountPresets = listOf(1.0, 2.0, 3.0, 1.5, 0.5, 0.25)

@Composable
fun LeaveScreen(
    viewModel: LeaveViewModel
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    var editingRecord by remember { mutableStateOf<LeaveRecord?>(null) }
    var showRecordDialog by remember { mutableStateOf(false) }
    var showImportDialog by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.message) {
        uiState.message?.let { message ->
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
            viewModel.clearMessage()
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            LeaveSummaryCard(
                uiState = uiState,
                onPreviousYear = { viewModel.setSelectedYear(uiState.selectedYear - 1) },
                onNextYear = { viewModel.setSelectedYear(uiState.selectedYear + 1) },
                onSaveTotalDays = viewModel::saveTotalDays
            )
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = {
                        editingRecord = null
                        showRecordDialog = true
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("연차 추가")
                }

                OutlinedButton(
                    onClick = { showImportDialog = true },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.ContentPaste, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("붙여넣기")
                }
            }
        }

        if (uiState.records.isEmpty()) {
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "등록된 연차 기록이 없습니다.",
                        modifier = Modifier.padding(20.dp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            items(uiState.records, key = { it.id }) { record ->
                LeaveRecordItem(
                    record = record,
                    onEdit = {
                        editingRecord = record
                        showRecordDialog = true
                    },
                    onDelete = { viewModel.deleteLeaveRecord(record) }
                )
            }
        }
    }

    if (showRecordDialog) {
        LeaveRecordDialog(
            initialRecord = editingRecord,
            selectedYear = uiState.selectedYear,
            onDismiss = { showRecordDialog = false },
            onSave = { date, amount, note, halfDayPosition ->
                if (editingRecord == null) {
                    viewModel.addLeaveRecordsFromStartDate(
                        startDate = date,
                        amount = amount,
                        note = note,
                        halfDayPosition = halfDayPosition
                    )
                } else {
                    viewModel.updateLeaveRecord(
                        editingRecord!!.copy(
                            type = typeForAmount(amount),
                            date = date,
                            amount = amount,
                            note = note.ifBlank { LeaveViewModel.DEFAULT_LEAVE_NOTE }
                        )
                    )
                }
                showRecordDialog = false
            }
        )
    }

    if (showImportDialog) {
        ImportLeaveDialog(
            onDismiss = { showImportDialog = false },
            onImport = { text ->
                viewModel.importFromText(text)
                showImportDialog = false
            }
        )
    }
}

@Composable
private fun LeaveSummaryCard(
    uiState: LeaveUiState,
    onPreviousYear: () -> Unit,
    onNextYear: () -> Unit,
    onSaveTotalDays: (Double) -> Unit
) {
    var totalDaysText by remember(uiState.selectedYear, uiState.totalDays) {
        mutableStateOf(formatDays(uiState.totalDays))
    }

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                TextButton(onClick = onPreviousYear) {
                    Text("이전")
                }
                Text(
                    text = "${uiState.selectedYear}년 연차",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                TextButton(onClick = onNextYear) {
                    Text("다음")
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                SummaryValue("지급", formatDays(uiState.totalDays))
                SummaryValue("사용", formatDays(uiState.usedDays))
                SummaryValue("잔여", formatDays(uiState.remainingDays))
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = totalDaysText,
                    onValueChange = { totalDaysText = it },
                    label = { Text("지급 연차") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
                Button(
                    onClick = { totalDaysText.toDoubleOrNull()?.let(onSaveTotalDays) }
                ) {
                    Icon(Icons.Default.Save, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("저장")
                }
            }
        }
    }
}

@Composable
private fun SummaryValue(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = label,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = "${value}일",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun LeaveRecordItem(
    record: LeaveRecord,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "${record.date}  ${record.type}  ${formatDays(record.amount)}일",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                if (record.note.isNotBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = record.note,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            IconButton(onClick = onEdit) {
                Icon(Icons.Default.Edit, contentDescription = "수정")
            }
            IconButton(onClick = onDelete) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "삭제",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@Composable
private fun LeaveRecordDialog(
    initialRecord: LeaveRecord?,
    selectedYear: Int,
    onDismiss: () -> Unit,
    onSave: (date: String, amount: Double, note: String, halfDayPosition: HalfDayPosition) -> Unit
) {
    var date by remember(initialRecord, selectedYear) {
        mutableStateOf(initialRecord?.date ?: defaultLeaveDate(selectedYear))
    }
    var dateFieldValue by remember(initialRecord, selectedYear) {
        mutableStateOf(TextFieldValue(date, selection = TextRange(date.length)))
    }
    var amountText by remember(initialRecord) {
        mutableStateOf(formatDays(initialRecord?.amount ?: 1.0))
    }
    var note by remember(initialRecord) { mutableStateOf(initialRecord?.note.orEmpty()) }
    var halfDayPosition by remember(initialRecord) { mutableStateOf(FIRST_DAY) }
    val visibleAmountPresets = if (initialRecord == null) {
        amountPresets
    } else {
        listOf(1.0, 0.5, 0.25)
    }
    val amount = amountText.toDoubleOrNull()
    val showHalfDayPosition = initialRecord == null && amount != null && hasFractionalDay(amount) && amount > 1.0

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (initialRecord == null) "연차 추가" else "연차 수정") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = dateFieldValue,
                    onValueChange = { newValue ->
                        val formattedDate = formatDateInput(newValue.text)
                        date = formattedDate
                        dateFieldValue = TextFieldValue(
                            text = formattedDate,
                            selection = TextRange(formattedDate.length)
                        )
                    },
                    label = {
                        Text(
                            if (initialRecord == null) {
                                "시작 날짜 (yyyy-MM-dd)"
                            } else {
                                "날짜 (yyyy-MM-dd)"
                            }
                        )
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                if (initialRecord == null) {
                    Text(
                        text = "사용 일수가 1일을 넘으면 주말을 제외하고 다음 근무일에 자동으로 나눠서 등록됩니다.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    visibleAmountPresets.chunked(3).forEach { presetRow ->
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            presetRow.forEach { amount ->
                                FilterChip(
                                    selected = amountText == formatDays(amount),
                                    onClick = {
                                        amountText = formatDays(amount)
                                    },
                                    label = { Text("${formatDays(amount)}일") }
                                )
                            }
                        }
                    }
                }

                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it },
                    label = { Text(if (initialRecord == null) "총 사용 일수" else "사용 일수") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                if (showHalfDayPosition) {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = "반차 위치",
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            FilterChip(
                                selected = halfDayPosition == FIRST_DAY,
                                onClick = { halfDayPosition = FIRST_DAY },
                                label = { Text("첫날") }
                            )
                            FilterChip(
                                selected = halfDayPosition == LAST_DAY,
                                onClick = { halfDayPosition = LAST_DAY },
                                label = { Text("마지막날") }
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text("사유") },
                    placeholder = { Text(LeaveViewModel.DEFAULT_LEAVE_NOTE) },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val amount = amountText.toDoubleOrNull() ?: return@TextButton
                    onSave(date, amount, note, halfDayPosition)
                }
            ) {
                Text("저장")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("취소")
            }
        }
    )
}

@Composable
private fun ImportLeaveDialog(
    onDismiss: () -> Unit,
    onImport: (String) -> Unit
) {
    var importText by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("엑셀 기록 붙여넣기") },
        text = {
            Column {
                Text(
                    text = "형식: 일자 기간 사유\n사유를 비우면 개인사유로 저장됩니다.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "입력 예시",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text("ex) 2026-01-31 1 설날")
                        Text("ex) 2026-02-05 1.5")
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = importText,
                    onValueChange = { importText = it },
                    minLines = 8,
                    label = { Text("연차 기록") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(onClick = { onImport(importText) }) {
                Text("가져오기")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("취소")
            }
        }
    )
}

private fun formatDays(value: Double): String {
    return DecimalFormat("0.##").format(value)
}

private fun typeForAmount(amount: Double): String {
    return when (amount) {
        0.5 -> "반휴"
        0.25 -> "반반휴"
        else -> "휴가"
    }
}

private fun hasFractionalDay(amount: Double): Boolean {
    return amount % 1.0 != 0.0
}

private fun defaultLeaveDate(selectedYear: Int): String {
    val calendar = Calendar.getInstance(Locale.KOREA).apply {
        set(Calendar.YEAR, selectedYear)
    }
    return SimpleDateFormat("yyyy-MM-dd", Locale.KOREA).format(calendar.time)
}

private fun formatDateInput(input: String): String {
    val digits = input.filter { it.isDigit() }.take(8)
    return buildString {
        digits.forEachIndexed { index, char ->
            if (index == 4 || index == 6) append('-')
            append(char)
        }
    }
}
