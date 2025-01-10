package com.voxplanapp.ui.goals

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.voxplanapp.data.Quota
import com.voxplanapp.data.QuotaRepository
import com.voxplanapp.data.TimeBank
import com.voxplanapp.data.TimeBankRepository
import com.voxplanapp.data.TodoItem
import com.voxplanapp.data.TodoRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalDate

// ProgressViewModel.kt
class ProgressViewModel(
    private val todoRepository: TodoRepository,
    private val timeBankRepository: TimeBankRepository,
    private val quotaRepository: QuotaRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProgressUiState())
    val uiState: StateFlow<ProgressUiState> = _uiState.asStateFlow()

    private var currentWeekStart = LocalDate.now().with(DayOfWeek.MONDAY)

    init {
        loadWeekProgress()
    }

    fun previousWeek() {
        currentWeekStart = currentWeekStart.minusWeeks(1)
        loadWeekProgress()
    }

    fun nextWeek() {
        currentWeekStart = currentWeekStart.plusWeeks(1)
        loadWeekProgress()
    }

    private fun loadWeekProgress() {
        viewModelScope.launch {
            // Get all goals that have quotas assigned to them.
            val quotas = quotaRepository.getAllQuotas().first()
            val goalIds = quotas.map { it.goalId }

            // retrieve only goals that haven't been completed
            val goals = todoRepository.getItemsByIds(goalIds).first()
                .filter { it.completedDate == null }

            // check if we need to remove orphaned quotas
            if (goals.size < quotas.size) {
                viewModelScope.launch {
                    val activeIds = goals.map { it.id }.toSet()     // map the ids of active goals
                    quotas.forEach { quota ->
                        if (quota.goalId !in activeIds) {
                            quotaRepository.deleteQuotaForGoal(quota.goalId)
                        }
                    }
                }
            }

            // Get time bank entries for the week
            val weekEntries = timeBankRepository.getEntriesForDateRange(
                currentWeekStart,
                currentWeekStart.plusDays(6)
            ).first()

            // Process entries into UI state
            val dailyProgress = processDailyProgress(goals, quotas, weekEntries)
            val weekTotal = calculateWeekTotal(weekEntries, goals, quotas)
            val completedDays = dailyProgress.count { it.isComplete }

            _uiState.value = ProgressUiState(
                currentWeek = currentWeekStart,
                dailyProgress = dailyProgress,
                weekTotal = weekTotal,
                completedDays = completedDays
            )
        }
    }

// Additional ProgressViewModel.kt functions

    private fun processDailyProgress(
        goals: List<TodoItem>,
        quotas: List<Quota>,
        entries: List<TimeBank>
    ): List<DayProgress> {
        return (0..6).map { dayOffset ->
            val date = currentWeekStart.plusDays(dayOffset.toLong())
            val dayOfWeek = date.dayOfWeek

            // Get entries for this day
            val dayEntries = entries.filter { it.date == date }

            // Calculate progress for each goal
            val goalProgress = goals.map { goal ->
                val quota = quotas.find { it.goalId == goal.id }
                val achieved = dayEntries
                    .filter { it.goalId == goal.id }
                    .sumOf { it.duration }

                GoalProgress(
                    goalId = goal.id,
                    title = goal.title,
                    minutesAchieved = achieved,
                    quotaMinutes = quota?.dailyMinutes ?: 0
                )
            }

            // Calculate total minutes for diamonds
            val totalMinutes = dayEntries.sumOf { it.duration }
            val diamonds = totalMinutes / 240 // One diamond per 4 hours

            // Check if all quotas were met
            val isComplete = goalProgress.all { progress ->
                progress.minutesAchieved >= progress.quotaMinutes
            }

            DayProgress(
                dayOfWeek = dayOfWeek,
                goalProgress = goalProgress,
                isComplete = isComplete,
                diamonds = diamonds
            )
        }
    }

    private fun calculateWeekTotal(
        entries: List<TimeBank>,
        goals: List<TodoItem>,
        quotas: List<Quota>
    ): WeekTotal {
        val goalTotals = goals.map { goal ->
            val totalMinutes = entries
                .filter { it.goalId == goal.id }
                .sumOf { it.duration }

            val diamonds = totalMinutes / 240 // Full 4-hour chunks
            val remainingHours = (totalMinutes % 240) / 60 // Remaining full hours

            GoalTotal(
                goalId = goal.id,
                title = goal.title,
                totalMinutes = totalMinutes,
                diamonds = diamonds,
                stars = remainingHours
            )
        }

        return WeekTotal(goalTotals)
    }
}

data class ProgressUiState(
    val currentWeek: LocalDate = LocalDate.now().with(DayOfWeek.MONDAY),
    val dailyProgress: List<DayProgress> = emptyList(),
    val weekTotal: WeekTotal = WeekTotal(emptyList()),
    val completedDays: Int = 0
)

data class DayProgress(
    val dayOfWeek: DayOfWeek,
    val goalProgress: List<GoalProgress>,
    val isComplete: Boolean,
    val diamonds: Int
)

data class GoalProgress(
    val goalId: Int,
    val title: String,
    val minutesAchieved: Int,
    val quotaMinutes: Int
)

data class WeekTotal(
    val goalTotals: List<GoalTotal>
)

data class GoalTotal(
    val goalId: Int,
    val title: String,
    val totalMinutes: Int,
    val diamonds: Int,
    val stars: Int
)