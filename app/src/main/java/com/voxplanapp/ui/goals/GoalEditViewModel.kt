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
import com.voxplanapp.data.RecurrenceType
import com.voxplanapp.navigation.VoxPlanScreen
import com.voxplanapp.shared.SharedViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime

// introduced new loading and error fields in case we can't find the goal
data class GoalUiState(
    val goal: GoalWithSubGoals?,
    val isLoading: Boolean = true,
    val error: String? = null
)

/*
    GoalEditViewModel * retrieves the TodoItemStream from repository
    * converts each TodoItem to GoalWithSubGoals object
    * emits goalUiState as uiState property, so we can keep the Ui updated.
    (modified as GoalWithSubGoals to be more consistent with rest of app)
 */
class GoalEditViewModel(
    savedStateHandle: SavedStateHandle,
    private val sharedViewModel: SharedViewModel,
    private val todoRepository: TodoRepository,
    private val eventRepository: EventRepository,
): ViewModel() {

    // retrieves goalId from the saved state handle, if the process has re-started
    private val goalId: Int = checkNotNull(savedStateHandle[VoxPlanScreen.GoalEdit.goalIdArg])

    var goalUiState by mutableStateOf(GoalUiState(goal = null, isLoading = true))
        private set

    init {
        viewModelScope.launch {
            val allTodos = todoRepository.getAllTodos().first()
            val result = sharedViewModel.getGoalWithSubGoals(allTodos, goalId)

            goalUiState = if (result != null) {
                GoalUiState(goal = result, isLoading = false)
            } else {
                GoalUiState(goal = null, isLoading = false, error = "Goal not found")
            }
       }
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
            goalUiState.goal?.let { goalWithSubGoals ->
                todoRepository.insert(goalWithSubGoals.goal)
            } ?: run {
                Log.d("GoalEditViewModel", "Can't save goal, it's null for some reason.")
                return@launch
            }
        }
    }

    suspend fun getParentGoalTitle(): String? {
        return goalUiState.goal?.goal?.parentId?.let { parentId ->
            todoRepository.getItemStream(parentId)
                .filterNotNull()
                .first()
                .title
        }
    }

    fun scheduleGoal(date: LocalDate) {

        Log.d("scheduleGoal", "scheduling goal ${goalUiState.goal?.goal?.title} on date $date at time ${goalUiState.goal?.goal?.preferredTime}")

        val goal = goalUiState.goal?.goal
        if (goal == null) {
            Log.d("scheduleGoal", "No goal, returning")
            return@scheduleGoal
        }

        val startTime = goal.preferredTime ?: LocalTime.of(9,0)    // default 9am
        val duration = goal.estDurationMins?.toLong() ?:60

        val event = Event(
            goalId = goal.id,
            title = goal.title,
            startTime = startTime,
            endTime = startTime.plusMinutes(duration),
            startDate = date,
            recurrenceType = goal.frequency,
            recurrenceInterval = 1,
            recurrenceEndDate = null,
            color = 0
        )

        viewModelScope.launch {
            Log.d("scheduleGoal", "inserting Event into event Repository...")
            eventRepository.insertEvent(event)
            Log.d("scheduleGoal", "inserted successfully!")
        }

    }
}
