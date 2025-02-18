package com.voxplanapp.ui.goals

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.room.PrimaryKey
import com.voxplanapp.data.TodoItem
import com.voxplanapp.data.TodoRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filterNot
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import androidx.compose.runtime.setValue
import com.voxplanapp.data.Event
import com.voxplanapp.data.EventRepository
import com.voxplanapp.data.GoalWithSubGoals
import com.voxplanapp.data.Quota
import com.voxplanapp.data.QuotaRepository
import com.voxplanapp.data.RecurrenceType
import com.voxplanapp.navigation.VoxPlanScreen
import com.voxplanapp.shared.SharedViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime

// introduced new loading and error fields in case we can't find the goal
data class GoalUiState(
    val goal: GoalWithSubGoals?,
    val isLoading: Boolean = true,
    val error: String? = null,
    // quota state
    val quotaMinutes: Int = 60,
    val quotaActiveDays: Set<DayOfWeek> = setOf()
)

/*
    GoalEditViewModel * retrieves the TodoItemStream from repository
    * converts each TodoItem to GoalWithSubGoals object
    * emits goalUiState as uiState property, so we can keep the Ui updated.
 */

class GoalEditViewModel(
    savedStateHandle: SavedStateHandle,
    private val sharedViewModel: SharedViewModel,
    private val todoRepository: TodoRepository,
    private val eventRepository: EventRepository,
    private val quotaRepository: QuotaRepository
): ViewModel() {

    // retrieves goalId from the saved state handle, if the process has re-started
    private val goalId: Int = checkNotNull(savedStateHandle[VoxPlanScreen.GoalEdit.goalIdArg])

    var goalUiState by mutableStateOf(GoalUiState(goal = null, isLoading = true))
        private set

    init {
        viewModelScope.launch {
            // first, get the corresponding goal from repository
            val allTodos = todoRepository.getAllTodos().first()
            val result = sharedViewModel.getGoalWithSubGoals(allTodos, goalId)

            goalUiState = if (result != null) GoalUiState(goal = result, isLoading = false)
            else GoalUiState(goal = null, isLoading = false, error = "Goal not found")

            // now, get any relevant goal quotas
            val quota = quotaRepository.getQuotaForGoal(goalId).first()
            if (quota != null) {
                goalUiState = goalUiState.copy(
                    quotaMinutes = quota.dailyMinutes ?: 60,
                    // produces a set of currently active days according to the string code used for activeDays
                    // a set of DayOfWeek enum names (of actual days of the week) - our working set that we re-encode when exiting
                    quotaActiveDays = (quota.activeDays.mapIndexedNotNull { index, active ->
                        if (active == '1') DayOfWeek.of(index + 1) else null
                    } ?.toSet()) ?: setOf()
                )
            }
       }
    }

    fun updateQuotaMinutes(minutes: Int) {
        goalUiState = goalUiState.copy(quotaMinutes = minutes)

    }
    fun updateQuotaActiveDays(days: Set<DayOfWeek>) {
        goalUiState = goalUiState.copy(quotaActiveDays = days)
    }

    fun updateGoalAttribute(attribute: String, value: Any) {
        val currentGoal = goalUiState.goal?.goal ?: return
        val updatedTodoItem = when (attribute) {
            "title" -> currentGoal.copy(title = value as String)
            "isDone" -> currentGoal.copy(completedDate = value as LocalDate)
            "notes" -> currentGoal.copy(notes = value as String?)
            "preferredTime" -> currentGoal.copy(preferredTime = value as LocalTime?)
            "estDurationMins" -> currentGoal.copy(estDurationMins = value as Int?)
            "frequency" -> currentGoal.copy(frequency = value as RecurrenceType)
            else -> return
        }

        goalUiState = goalUiState.copy(goal = goalUiState.goal?.copy(goal = updatedTodoItem))
    }

    fun saveGoal() {
        viewModelScope.launch {

            // if we have a goal to work with, we have something to save
            goalUiState.goal?.let { goalWithSubGoals ->
                todoRepository.insert(goalWithSubGoals.goal)

                if (goalUiState.quotaActiveDays.isEmpty()) {        // remove quota if nothing set
                    // delete the quota from the repository
                    quotaRepository.deleteQuotaForGoal(goalWithSubGoals.goal.id)
                } else {
                    // save quota info
                    val activeDaysString = buildString {
                        for (i in 1..7) {
                            append(if (DayOfWeek.of(i) in goalUiState.quotaActiveDays) "1" else "0")
                        }
                    }
                    quotaRepository.insertQuota(
                        Quota(
                            goalId = goalWithSubGoals.goal.id,
                            dailyMinutes = goalUiState.quotaMinutes,
                            activeDays = activeDaysString
                        )
                    )
                }

            } ?: run {
                Log.d("GoalEditViewModel", "Can't save goal, it's null for some reason.")
                return@launch
            }
        }
    }

    // removes the visual component of the quota (repository is altered on save)
    fun removeQuota() {
        // reset the ui state values
        goalUiState = goalUiState.copy(
            quotaMinutes = 60,
            quotaActiveDays = setOf()
        )
    }

    suspend fun getParentGoalTitle(): String? {
        return goalUiState.goal?.goal?.parentId?.let { parentId ->
            todoRepository.getItemStream(parentId)
                .filterNotNull()
                .first()
                .title
        }
    }

    suspend fun scheduleGoal(date: LocalDate) {

        Log.d("scheduleGoal", "scheduling goal ${goalUiState.goal?.goal?.title} on date $date at time ${goalUiState.goal?.goal?.preferredTime}")

        val goal = goalUiState.goal?.goal
        if (goal == null) {
            Log.d("scheduleGoal", "No goal, returning")
            return@scheduleGoal
        }

        val startTime = goal.preferredTime ?: LocalTime.of(9,0)    // default 9am
        val duration = goal.estDurationMins?.toLong() ?:60

        // parent Event is the daily, any scheduled time details are inherited general guidelines from the goal, and not specific to a scheduled event.
        val parentDaily = Event(
            goalId = goal.id,
            title = goal.title,
            startDate = date,
            quotaDuration = goal.estDurationMins,
            scheduledDuration = goal.estDurationMins,
            completedDuration = 0,
            recurrenceType = goal.frequency
        )
        val parentId = eventRepository.insertEvent(parentDaily)

        // Create scheduled child event
        val childEvent = Event(
            goalId = goal.id,
            title = goal.title,
            parentDailyId = parentId,
            startTime = startTime,
            endTime = startTime.plusMinutes(duration),
            startDate = date,
            recurrenceType = goal.frequency,
            quotaDuration = goal.estDurationMins
        )
        eventRepository.insertEvent(childEvent)

        Log.d("scheduleGoal", "inserted parent daily and child event successfully!")
    }
}
