package com.androidkotlin.timestampkt.presentation.calendar

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.androidkotlin.timestampkt.domain.model.LeaveRecord
import com.androidkotlin.timestampkt.domain.model.TimeRecord
import com.androidkotlin.timestampkt.presentation.leave.LeaveViewModel
import com.androidkotlin.timestampkt.presentation.main.MainViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.KOREA)
private val timeFormat = SimpleDateFormat("HH:mm:ss", Locale.KOREA)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarScreen(
    mainViewModel: MainViewModel,
    leaveViewModel: LeaveViewModel
) {
    val timeRecords by mainViewModel.allRecords.collectAsState(initial = emptyList())
    val leaveRecords by leaveViewModel.allLeaveRecords.collectAsState()
    val today = remember { dateFormat.format(Date()) }
    var selectedDate by remember { mutableStateOf(today) }
    var visibleMonth by remember {
        mutableStateOf(Calendar.getInstance().apply { set(Calendar.DAY_OF_MONTH, 1) })
    }

    val timeRecordsByDate = remember(timeRecords) {
        timeRecords.groupBy { dateFormat.format(Date(it.timestamp)) }
    }
    val leaveRecordsByDate = remember(leaveRecords) {
        leaveRecords.groupBy { it.date }
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("캘린더") })
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                CalendarMonthCard(
                    visibleMonth = visibleMonth,
                    selectedDate = selectedDate,
                    timeRecordsByDate = timeRecordsByDate,
                    leaveRecordsByDate = leaveRecordsByDate,
                    onPreviousMonth = {
                        visibleMonth = (visibleMonth.clone() as Calendar).apply {
                            add(Calendar.MONTH, -1)
                        }
                    },
                    onNextMonth = {
                        visibleMonth = (visibleMonth.clone() as Calendar).apply {
                            add(Calendar.MONTH, 1)
                        }
                    },
                    onSelectDate = { selectedDate = it }
                )
            }

            item {
                SelectedDateCard(
                    selectedDate = selectedDate,
                    timeRecords = timeRecordsByDate[selectedDate].orEmpty(),
                    leaveRecords = leaveRecordsByDate[selectedDate].orEmpty()
                )
            }
        }
    }
}

@Composable
private fun CalendarMonthCard(
    visibleMonth: Calendar,
    selectedDate: String,
    timeRecordsByDate: Map<String, List<TimeRecord>>,
    leaveRecordsByDate: Map<String, List<LeaveRecord>>,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit,
    onSelectDate: (String) -> Unit
) {
    val days = remember(visibleMonth.timeInMillis) { buildMonthCells(visibleMonth) }
    val monthTitle = remember(visibleMonth.timeInMillis) {
        SimpleDateFormat("yyyy년 MM월", Locale.KOREA).format(visibleMonth.time)
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
                TextButton(onClick = onPreviousMonth) {
                    Text("이전")
                }
                Text(
                    text = monthTitle,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                TextButton(onClick = onNextMonth) {
                    Text("다음")
                }
            }

            Row(modifier = Modifier.fillMaxWidth()) {
                listOf("일", "월", "화", "수", "목", "금", "토").forEach { dayName ->
                    Text(
                        text = dayName,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            days.chunked(7).forEach { week ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    week.forEach { day ->
                        CalendarDayCell(
                            day = day,
                            selectedDate = selectedDate,
                            hasTimeRecord = day?.let { timeRecordsByDate.containsKey(it.date) } == true,
                            hasLeaveRecord = day?.let { leaveRecordsByDate.containsKey(it.date) } == true,
                            onSelectDate = onSelectDate,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                LegendDot("출근", MaterialTheme.colorScheme.primary)
                LegendDot("연차", MaterialTheme.colorScheme.tertiary)
            }
        }
    }
}

@Composable
private fun CalendarDayCell(
    day: CalendarDay?,
    selectedDate: String,
    hasTimeRecord: Boolean,
    hasLeaveRecord: Boolean,
    onSelectDate: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val isSelected = day?.date == selectedDate
    val background = if (isSelected) {
        MaterialTheme.colorScheme.primaryContainer
    } else {
        MaterialTheme.colorScheme.surface
    }

    Card(
        onClick = { day?.let { onSelectDate(it.date) } },
        modifier = modifier.aspectRatio(1f),
        enabled = day != null
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(background)
                .padding(4.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = day?.dayOfMonth?.toString().orEmpty(),
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
            )
            Spacer(modifier = Modifier.height(4.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                if (hasTimeRecord) Dot(MaterialTheme.colorScheme.primary)
                if (hasLeaveRecord) Dot(MaterialTheme.colorScheme.tertiary)
            }
        }
    }
}

@Composable
private fun SelectedDateCard(
    selectedDate: String,
    timeRecords: List<TimeRecord>,
    leaveRecords: List<LeaveRecord>
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = selectedDate,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            if (timeRecords.isEmpty() && leaveRecords.isEmpty()) {
                Text(
                    text = "등록된 기록이 없습니다.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            timeRecords.forEach { record ->
                Text("출근 기록: ${timeFormat.format(Date(record.timestamp))}")
            }

            leaveRecords.forEach { record ->
                Text("연차 기록: ${record.type} ${formatAmount(record.amount)}일 / ${record.note}")
            }
        }
    }
}

@Composable
private fun LegendDot(label: String, color: androidx.compose.ui.graphics.Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Dot(color)
        Spacer(modifier = Modifier.size(6.dp))
        Text(label, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun Dot(color: androidx.compose.ui.graphics.Color) {
    Box(
        modifier = Modifier
            .size(8.dp)
            .clip(CircleShape)
            .background(color)
    )
}

private data class CalendarDay(
    val date: String,
    val dayOfMonth: Int
)

private fun buildMonthCells(month: Calendar): List<CalendarDay?> {
    val firstDay = (month.clone() as Calendar).apply {
        set(Calendar.DAY_OF_MONTH, 1)
    }
    val maxDay = firstDay.getActualMaximum(Calendar.DAY_OF_MONTH)
    val leadingBlankCount = firstDay.get(Calendar.DAY_OF_WEEK) - 1
    val cells = mutableListOf<CalendarDay?>()

    repeat(leadingBlankCount) {
        cells.add(null)
    }

    for (day in 1..maxDay) {
        val dateCalendar = (firstDay.clone() as Calendar).apply {
            set(Calendar.DAY_OF_MONTH, day)
        }
        cells.add(
            CalendarDay(
                date = dateFormat.format(dateCalendar.time),
                dayOfMonth = day
            )
        )
    }

    while (cells.size % 7 != 0) {
        cells.add(null)
    }

    return cells
}

private fun formatAmount(amount: Double): String {
    return if (amount % 1.0 == 0.0) {
        amount.toInt().toString()
    } else {
        amount.toString()
    }
}
