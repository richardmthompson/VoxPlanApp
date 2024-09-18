package com.voxplanapp.ui.focusmode

import android.util.Log
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.voxplanapp.data.Event
import com.voxplanapp.data.EventRepository
import com.voxplanapp.data.GoalWithSubGoals
import com.voxplanapp.data.TimeBankRepository
import com.voxplanapp.data.TodoRepository
import com.voxplanapp.navigation.VoxPlanScreen
import com.voxplanapp.shared.SharedViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

class FocusViewModel(
    savedStateHandle: SavedStateHandle,
    private val todoRepository: TodoRepository,
    private val eventRepository: EventRepository,
    private val timeBankRepository: TimeBankRepository,
    private val sharedViewModel: SharedViewModel
): ViewModel() {

    // process either the goalId or the eventId, depending on whether we navigate from
    // the GoalEditScreen or the DayScheduler directly.
    private val goalId: Int? = savedStateHandle.get<String>(VoxPlanScreen.FocusMode.goalIdArg)?.toIntOrNull()
    private val eventId: Int? = savedStateHandle.get<String>(VoxPlanScreen.FocusMode.eventIdArg)?.toIntOrNull()

    // set up state variables

    // goalUiState is separate to other state variables as it shouldn't be changed
    var goalUiState by mutableStateOf<GoalWithSubGoals?>(null)
        private set
    // same with eventUiState
    var eventUiState by mutableStateOf<Event?>(null)
        private set
    var focusUiState by mutableStateOf(FocusUiState(isLoading = true))
        private set
    var timerSettingsState by mutableStateOf(TimerSettingsState())
        private set

    // set up clock timer state variable
    private val _timerJob = MutableStateFlow<Job?>(null)
    private val timerJob: StateFlow<Job?> = _timerJob.asStateFlow()

    init {
        // if we load from DaySchedule, we load the event, and then the goal from there.
        // if we load from MainScreen, we load the goal directly, and create the event later.
        // in either case, we're not loaded until we have a goal to work with.

        // load our goal and event data into the screen
        loadInitialData()
        // let's set up start time.
        checkStartTime()
        // initialise timer
        setupTimeBank()
    }

    fun resetTimer() {
        // this is an init function only for now
        _timerJob.value?.cancel()
        _timerJob.value = null
        focusUiState = focusUiState.copy(
            timerState = TimerState.IDLE,
            currentTime = 0L,
            clockProgress = 0f,
            currentTheme = ColorScheme.REST
        )
    }

    fun startTimer() {
        var startTime: Long = 0L

        // if coming from a paused state, we accrue previous time to the start time.
        if (focusUiState.timerState == TimerState.PAUSED) {
            val previousElapsedTime = focusUiState.currentTime
            startTime = System.currentTimeMillis() - previousElapsedTime
        } else startTime = System.currentTimeMillis()

        Log.d("Timer","starting timer @ ${startTime}")

            _timerJob.value = viewModelScope.launch {
                while (isActive) {
                    val currentTime = System.currentTimeMillis()
                    Log.d("Timer","currentTime = ${currentTime}")
                    val elapsedTime = currentTime - startTime
                    Log.d("Timer","elapsed time = ${elapsedTime}")
                    updateTimerState(elapsedTime)
                    delay(1000)
                }
            }
        focusUiState = focusUiState.copy(
            timerState = TimerState.RUNNING,
            currentTheme = ColorScheme.WORK
        )
    }

    // set up time-based variables for the focus mode screen
    private fun setupTimeBank() {
        val clockFaceMins = 30
        focusUiState = focusUiState.copy(clockFaceMins = 30)
    }

    private fun updateTimerState(elapsedTime: Long) {
        val clockFaceMillis = focusUiState.clockFaceMins * 60000f
        val progress = (elapsedTime / clockFaceMillis).coerceAtMost(1f)    // 180000 ms = 30 mins

        // if we hit a revolution, award a medal with that amount of time and re-set clock
        if (progress == 1f) awardMedal()

        focusUiState = focusUiState.copy(
            currentTime = elapsedTime,
            clockProgress = progress
        )
    }

    fun toggleTimer() {
        if (focusUiState.timerState == TimerState.IDLE) startTimer()

        // if timer has been paused, when it starts, we want to retain the elapsed time
        // and add newly elapsed time to the previous number.
        else if (focusUiState.timerState == TimerState.PAUSED) startTimer()
        else pauseTimer()
    }

    fun pauseTimer() {
        Log.d("Timer","pausing timer... ")
        // timer state is used to determine state of start/pause button
        focusUiState = focusUiState.copy(
            timerState = TimerState.PAUSED,
            currentTheme = ColorScheme.REST
        )

        // this cancels the ticking.
        _timerJob.value?.cancel()
        _timerJob.value = null
    }

    fun updateClockFaceMinutes(minutes: Int) {
        focusUiState = focusUiState.copy(clockFaceMins = minutes)
    }

    fun awardMedal() {
        val newMedal = Medal(focusUiState.clockFaceMins)

        focusUiState = focusUiState.copy(
            medals = focusUiState.medals + newMedal
        )
        resetTimer()
        startTimer()
    }

    // this is the 'bank time' button in the timer controls.
    // it grabs a worthwhile chunk of time from the timer and plops it into the vault.
    fun bankTimer() {
        // determine number of minutes on current time (displayed on timer)
        val minutes = (focusUiState.currentTime % (1000 * 60 * 60)) / (1000 * 60)

        // add medal to vault

        // remove minutes from focusUiState current time

        // pause timer

    }

    fun bankTime() {
        val medalTime = focusUiState.medals.sumOf { it.minutes }

        viewModelScope.launch {
            val goalId = goalUiState?.goal?.id ?: return@launch
            timeBankRepository.addTimeBankEntry(goalId, medalTime)
        }

        // clear medals
        focusUiState = focusUiState.copy(medals = emptyList())

        // log what happened
        if (goalId != null) Log.d("TimeBank","Banked $medalTime minutes into goalId $goalId?")
        else Log.d("TimeBank", "Trying to bank time failed, no goal id")

        // Update goal or event with accrued time
    }

    private fun loadInitialData() {
        viewModelScope.launch {
            try {
                when {
                    eventId != null -> loadEventData(eventId)
                    goalId != null -> loadGoalData(goalId)
                    else -> throw IllegalArgumentException("Neither eventId or goalId provided")
                }
            } catch (e: Exception) {
                loadErrorScreen(e.message ?: "Unknown error occurred")
                Log.e("FocusViewModel", "Error loading initial data", e)
            }
        }
    }

    private fun loadEventData(eventId: Int) {

        viewModelScope.launch {

            // set up our focusUiState around the event if we've come from scheduler.
            val event = eventRepository.getEvent(eventId)
            event?.let { event ->
                Log.d("FocusViewModel","Retrieved event id ${event.id}")
                focusUiState = focusUiState.copy(
                    isFromEvent = true,
                    eventId = eventId,
                )
                eventUiState = event
                Log.d("FocusViewModel","set eventUistate @ startTime ${event.startTime}.  About to load associated goal data.")

                // now load the associated goal.
                loadGoalData(event.goalId)
            } ?: run {
                Log.d("FocusViewModel","[ERROR] No EVENT found, quitting")
                loadErrorScreen("Event not found")
            }

        }
    }

    private fun loadGoalData(goalId: Int) {
        viewModelScope.launch {
            Log.d("FocusViewModel", "loadGoalData: about to get all todos")
            val todos = todoRepository.getAllTodos().first()
            Log.d("FocusViewModel", "loadGoalData: getting goal with subgoals ${goalId}")
            val goalWithSubGoals = sharedViewModel.getGoalWithSubGoals(todos, goalId)

            // do error checking in case we had a problem loading the goal.
            // in which case, we can't do this screen.
            goalWithSubGoals.let {
                Log.d("FocusViewModel", "loadGoalData: we have a goalUiState! \"${goalWithSubGoals?.goal?.title}\"")
                // set the loading state to false here, because we've successfully loaded a goal.
                goalUiState = it
                focusUiState = focusUiState.copy(isLoading = false)
            } ?: run {
                Log.d("FocusViewModel","[ERROR] No GOAL found, quitting")
                loadErrorScreen("Can't find a goal")
            }
        }
    }

    // check we're within bounds if we come from an event.
    private fun checkStartTime() {
        // result: set focusUiState's startTime and date so we can use it in Pomo timer.
        val dateNow = LocalDate.now()
        val timeNow = LocalTime.now()

        // if no event, we set the start time to now and exit.
        val event = eventUiState ?: run {

            // we're not coming from an event, but we must have a goal.
            // we want end-time so we can print the event box
            val endTime = timeNow.plusMinutes(60)
            focusUiState = focusUiState.copy(
                startTime = timeNow,
                endTime = endTime,
                date = dateNow
            )
            return
        }

        when {
            // if we try to focus mode an event on different day, quit
            event.startDate != dateNow -> {
                loadErrorScreen("Trying to run focus mode on an event for a different day to today.\n" +
                        "Event date: ${event.startDate}.  Today: ${dateNow}")
                return
            }
            // we're running focus mode within the prescribed timeslot, so set vars.
            timeNow > event.startTime && timeNow < event.endTime -> {
                focusUiState = focusUiState.copy(
                    startTime = timeNow,
                    date = dateNow
                )
            }
            else -> {
                loadErrorScreen("Not within time-boundaries for designated event\n" +
                        "Event start: ${event.startTime}, end: ${event.endTime}, Time Now: $timeNow")
                return
            }
        }
    }

    fun loadErrorScreen(error: String) {
        focusUiState = focusUiState.copy(
            isLoading = false,
            error = error
        )
    }

    fun endSession() {
        focusUiState = focusUiState.copy(endTime = LocalTime.now())
        viewModelScope.launch {
            // todo: updateGoalOrEvent()
        }
    }

    fun switchTheme() {
        focusUiState = focusUiState.copy(
            currentTheme = if (focusUiState.currentTheme == ColorScheme.WORK) ColorScheme.REST else ColorScheme.WORK
        )
    }
}

enum class TimerState {
    IDLE, RUNNING, PAUSED
}
enum class ColorScheme {
    WORK, REST
}

data class FocusUiState (
    val eventId: Int? = null,
    val timerState: TimerState = TimerState.IDLE,
    val currentTime: Long = 0L,
    val startTime : LocalTime? = null,
    val endTime: LocalTime? = null,
    val date: LocalDate? = null,
    val totalAccruedTime: Long = 0L,
    val clockProgress: Float = 0f,
    val isFromEvent: Boolean = false,
    val currentTheme: ColorScheme = ColorScheme.REST,
    val isLoading: Boolean = true,
    val error: String ?= null,
    val clockFaceMins: Int = 30,
    val medals: List<Medal> = emptyList()
)

data class Medal(val minutes: Int)

data class TimerSettingsState(
    val workDuration: Int = 25,
    val restDuration: Int = 5,
    val usePomodoro: Boolean = false,
    val numWorkBlocks: Int? = null
)
