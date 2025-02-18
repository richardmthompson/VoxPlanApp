package com.voxplanapp.ui.daily

import android.util.Log
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.voxplanapp.data.Event
import com.voxplanapp.data.EventRepository
import com.voxplanapp.data.QuotaRepository
import com.voxplanapp.data.TodoRepository
import com.voxplanapp.model.ActionMode
import com.voxplanapp.navigation.ActionModeHandler
import com.voxplanapp.navigation.VoxPlanScreen
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime
import java.time.temporal.ChronoUnit

data class DailyUiState(
    val date: LocalDate = LocalDate.now(),
    val dailyTasks: List<Event> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null,
    val eventNeedingDuration: Int? = null       // holds event Id when passed from
)

class DailyViewModel(
    savedStateHandle: SavedStateHandle,
    private val eventRepository: EventRepository,
    private val todoRepository: TodoRepository,
    private val quotaRepository: QuotaRepository
) : ViewModel() {

    private val _actionMode = mutableStateOf<ActionMode>(ActionMode.Normal)
    val actionMode: State<ActionMode> = _actionMode
    val actionModeHandler = ActionModeHandler(_actionMode)

    private val newEventId: Int? = savedStateHandle.get<String>(VoxPlanScreen.Daily.newEventIdArg)?.toIntOrNull()

    private val _uiState = MutableStateFlow(DailyUiState(
        date = savedStateHandle.get<String>(VoxPlanScreen.Daily.dateArg)?.let {
            LocalDate.parse(it)
        } ?: LocalDate.now(),
        eventNeedingDuration = newEventId       // pass new event Id into ui state so we can pop the duration dialog on entry
    ))
    val uiState: StateFlow<DailyUiState> = _uiState.asStateFlow()

    // state management for deletion confirmation dialog, used to prevent orphaned Events in Scheduler when parent daily is deleted
    private val _showDeleteConfirmation = MutableStateFlow<Event?>(null)
    val showDeleteConfirmation: StateFlow<Event?> = _showDeleteConfirmation.asStateFlow()

    init {
        viewModelScope.launch {
            snapshotFlow { _uiState.value.date }
                .flatMapLatest { date ->
                    eventRepository.getDailiesForDate(date)
                }
                .collect { events ->
                    _uiState.value = _uiState.value.copy(
                        dailyTasks = events,
                        isLoading = false
                    )
                }
        }
        Log.d("Daily", "init: event ID found as ${uiState.value.eventNeedingDuration}")

    }

    fun updateDate(newDate: LocalDate) {
        _uiState.value = _uiState.value.copy(
            date = newDate,
            isLoading = true
        )
    }

    fun reorderTask(task: Event) {
        val currentTasks = _uiState.value.dailyTasks.toMutableList()
        val currentIndex = currentTasks.indexOfFirst { it.id == task.id }

        if (currentIndex == -1) return

        val newIndex = when (_actionMode.value) {
            ActionMode.VerticalUp -> (currentIndex - 1).coerceAtLeast(0)
            ActionMode.VerticalDown -> (currentIndex + 1).coerceAtMost(currentTasks.lastIndex)
            else -> return
        }

        if (newIndex != currentIndex) {
            currentTasks.removeAt(currentIndex)
            currentTasks.add(newIndex, task)

            viewModelScope.launch {
                // Update order in repository
                currentTasks.forEachIndexed { index, event ->
                    eventRepository.updateEvent(event.copy(order = index))
                }

                _uiState.value = _uiState.value.copy(dailyTasks = currentTasks)
            }
        }
    }

    fun addQuotaTasks() {
        viewModelScope.launch {
            val date = uiState.value.date
            // using first as getAllActiveQuotas returns a flow
            val quotas = quotaRepository.getAllActiveQuotas(date).first()

            // create a daily for each quota'd goal
            quotas.forEach { quota ->
                // get the source goal the quota refers to, from the todo repository.
                val goal = todoRepository.getItemStream(quota.goalId).first()
                // create the event, but with no start or end time.
                if (goal != null) {
                    val event = Event(
                        goalId = quota.goalId,
                        title = goal.title,
                        startDate = date,
                        quotaDuration = quota.dailyMinutes,     // add quota duration so we can show our duration display boxes
                        scheduledDuration = 0,
                        completedDuration = 0
                        // Other fields with default/null values
                    )
                    eventRepository.insertEvent(event)
                }
            }
        }
    }

    fun scheduleTask(task: Event, startTime: LocalTime, endTime: LocalTime) {
        viewModelScope.launch {
            val duration = ChronoUnit.MINUTES.between(startTime, endTime).toInt()

            // create new scheduled event block
            val scheduledTask = task.copy(
                startTime = startTime,
                endTime = endTime,
                parentDailyId = task.id,
                quotaDuration = duration
            )
            // update the existing - parent - task with the scheduled duration
            val updatedParent = task.copy(
                scheduledDuration = (task.scheduledDuration ?: 0) + duration
            )

            eventRepository.insertEvent(scheduledTask)
            eventRepository.updateEvent(updatedParent)

        }
    }

    fun setTaskDuration(task: Event, duration: Int) {
        viewModelScope.launch {
            val updatedEvent = task.copy(
                quotaDuration = duration,
            )
            eventRepository.updateEvent(updatedEvent)
        }

    }

    fun deleteTask(task: Event) {
        _showDeleteConfirmation.value = task
    }

    fun confirmDelete(task: Event) {
        viewModelScope.launch {
            // Get all child events
            val childEvents = eventRepository.getEventsWithParentId(task.id).first()
            // Delete all children first
            childEvents.forEach { eventRepository.deleteEvent(it.id) }
            // Then delete the parent
            eventRepository.deleteEvent(task.id)
            _showDeleteConfirmation.value = null
        }
    }

    fun cancelDelete() {
        _showDeleteConfirmation.value = null
    }
}
