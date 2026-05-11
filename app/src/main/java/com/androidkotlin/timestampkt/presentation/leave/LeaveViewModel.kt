package com.androidkotlin.timestampkt.presentation.leave

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.androidkotlin.timestampkt.domain.model.LeaveRecord
import com.androidkotlin.timestampkt.domain.model.LeaveYearSetting
import com.androidkotlin.timestampkt.domain.repository.LeaveRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import javax.inject.Inject

data class LeaveUiState(
    val selectedYear: Int = Calendar.getInstance().get(Calendar.YEAR),
    val totalDays: Double = 15.0,
    val usedDays: Double = 0.0,
    val remainingDays: Double = 15.0,
    val records: List<LeaveRecord> = emptyList(),
    val allRecords: List<LeaveRecord> = emptyList(),
    val message: String? = null
)

@HiltViewModel
@OptIn(ExperimentalCoroutinesApi::class)
class LeaveViewModel @Inject constructor(
    private val leaveRepository: LeaveRepository
) : ViewModel() {
    private val selectedYear = MutableStateFlow(Calendar.getInstance().get(Calendar.YEAR))
    private val message = MutableStateFlow<String?>(null)

    private val yearRecords = selectedYear.flatMapLatest { year ->
        leaveRepository.getLeaveRecordsByYear(year)
    }

    private val yearSetting = selectedYear.flatMapLatest { year ->
        leaveRepository.getYearSetting(year)
    }

    val allLeaveRecords: StateFlow<List<LeaveRecord>> = leaveRepository.getAllLeaveRecords()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val uiState: StateFlow<LeaveUiState> = combine(
        selectedYear,
        yearRecords,
        yearSetting,
        allLeaveRecords,
        message
    ) { year, records, setting, allRecords, currentMessage ->
        val totalDays = setting?.totalDays ?: 15.0
        val usedDays = records.sumOf { it.amount }
        LeaveUiState(
            selectedYear = year,
            totalDays = totalDays,
            usedDays = usedDays,
            remainingDays = totalDays - usedDays,
            records = records,
            allRecords = allRecords,
            message = currentMessage
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), LeaveUiState())

    fun setSelectedYear(year: Int) {
        selectedYear.value = year
    }

    fun saveTotalDays(totalDays: Double) {
        viewModelScope.launch {
            leaveRepository.saveYearSetting(
                LeaveYearSetting(
                    year = selectedYear.value,
                    totalDays = totalDays
                )
            )
            message.value = "지급 연차를 저장했습니다."
        }
    }

    fun addLeaveRecord(type: String, date: String, amount: Double, note: String) {
        viewModelScope.launch {
            leaveRepository.addLeaveRecord(
                LeaveRecord(
                    type = type,
                    date = date,
                    amount = amount,
                    note = note.ifBlank { DEFAULT_LEAVE_NOTE }
                )
            )
        }
    }

    fun addLeaveRecordsFromStartDate(
        startDate: String,
        amount: Double,
        note: String,
        halfDayPosition: HalfDayPosition = HalfDayPosition.FIRST_DAY
    ) {
        val records = buildLeaveRecordsFromStartDate(
            startDate = startDate,
            amount = amount,
            note = note,
            halfDayPosition = halfDayPosition
        )
        if (records.isEmpty()) {
            message.value = "연차 기록을 추가할 수 없습니다."
            return
        }

        viewModelScope.launch {
            addRecordsSkippingExistingDates(records)
        }
    }

    fun updateLeaveRecord(record: LeaveRecord) {
        viewModelScope.launch {
            leaveRepository.updateLeaveRecord(record)
        }
    }

    fun deleteLeaveRecord(record: LeaveRecord) {
        viewModelScope.launch {
            leaveRepository.deleteLeaveRecord(record)
        }
    }

    fun importFromText(text: String) {
        val parsedRecords = text
            .lineSequence()
            .map { it.trim() }
            .filter { it.isNotBlank() }
            .flatMap { parseLeaveRecordLine(it) }
            .toList()

        if (parsedRecords.isEmpty()) {
            message.value = "가져올 수 있는 연차 기록이 없습니다."
            return
        }

        viewModelScope.launch {
            addRecordsSkippingExistingDates(parsedRecords)
        }
    }

    fun clearMessage() {
        message.value = null
    }

    private fun parseLeaveRecordLine(line: String): Sequence<LeaveRecord> {
        val parts = line.split(Regex("\\s+"), limit = 3)
        if (parts.size < 2) return emptySequence()

        val date = parts[0]
        val amount = parts[1].toDoubleOrNull() ?: return emptySequence()
        val note = parts.getOrNull(2).orEmpty()

        return buildLeaveRecordsFromStartDate(
            startDate = date,
            amount = amount,
            note = note,
            halfDayPosition = HalfDayPosition.FIRST_DAY
        ).asSequence()
    }

    private fun buildLeaveRecordsFromStartDate(
        startDate: String,
        amount: Double,
        note: String,
        halfDayPosition: HalfDayPosition
    ): List<LeaveRecord> {
        if (amount <= 0.0) return emptyList()
        val normalizedStartDate = normalizeDateInput(startDate) ?: return emptyList()

        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.KOREA).apply {
            isLenient = false
        }
        val start = runCatching { dateFormat.parse(normalizedStartDate) }.getOrNull() ?: return emptyList()
        val calendar = Calendar.getInstance(Locale.KOREA).apply {
            time = start
        }
        val normalizedNote = note.ifBlank { DEFAULT_LEAVE_NOTE }
        val dayAmounts = splitLeaveAmount(amount, halfDayPosition)
        val records = mutableListOf<LeaveRecord>()
        var amountIndex = 0

        while (amountIndex < dayAmounts.size) {
            if (calendar.isWeekday()) {
                val dayAmount = dayAmounts[amountIndex]
                records.add(
                    LeaveRecord(
                        type = leaveTypeForAmount(dayAmount),
                        date = dateFormat.format(calendar.time),
                        amount = dayAmount,
                        note = normalizedNote
                    )
                )
                amountIndex += 1
            }
            calendar.add(Calendar.DAY_OF_MONTH, 1)
        }

        return records
    }

    private suspend fun addRecordsSkippingExistingDates(records: List<LeaveRecord>) {
        val existingDates = allLeaveRecords.value.map { it.date }.toSet()
        val newRecords = records.filterNot { it.date in existingDates }
        val skippedCount = records.size - newRecords.size

        if (newRecords.isNotEmpty()) {
            leaveRepository.addLeaveRecords(newRecords)
        }

        message.value = when {
            newRecords.isNotEmpty() && skippedCount > 0 ->
                "${newRecords.size}개의 연차 기록을 추가했습니다. 중복 ${skippedCount}개는 건너뛰었습니다."
            newRecords.isNotEmpty() ->
                "${newRecords.size}개의 연차 기록을 추가했습니다."
            else ->
                "이미 등록된 날짜라 추가하지 않았습니다."
        }
    }

    private fun normalizeDateInput(date: String): String? {
        val match = Regex("""(\d{4})-(\d{1,2})-(\d{1,2})""").matchEntire(date.trim())
            ?: return null
        val year = match.groupValues[1].toInt()
        val month = match.groupValues[2].toInt()
        val day = match.groupValues[3].toInt()

        if (month !in 1..12 || day !in 1..31) return null

        return "%04d-%02d-%02d".format(year, month, day)
    }

    private fun Calendar.isWeekday(): Boolean {
        val dayOfWeek = get(Calendar.DAY_OF_WEEK)
        return dayOfWeek != Calendar.SATURDAY && dayOfWeek != Calendar.SUNDAY
    }

    private fun splitLeaveAmount(amount: Double, halfDayPosition: HalfDayPosition): List<Double> {
        val fullDays = amount.toInt()
        val fractionalDay = amount - fullDays
        val amounts = MutableList(fullDays) { 1.0 }

        if (fractionalDay > 0.0) {
            if (halfDayPosition == HalfDayPosition.FIRST_DAY) {
                amounts.add(0, fractionalDay)
            } else {
                amounts.add(fractionalDay)
            }
        }

        return amounts
    }

    private fun leaveTypeForAmount(amount: Double): String {
        return when (amount) {
            1.0 -> "휴가"
            0.5 -> "반휴"
            0.25 -> "반반휴"
            else -> "휴가"
        }
    }

    companion object {
        const val DEFAULT_LEAVE_NOTE = "개인사유"
    }
}

enum class HalfDayPosition {
    FIRST_DAY,
    LAST_DAY
}
