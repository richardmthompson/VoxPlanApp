package com.voxplanapp.ui.focusmode

import android.os.SystemClock
import android.util.Log
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.voxplanapp.R
import com.voxplanapp.data.Event
import com.voxplanapp.data.EventRepository
import com.voxplanapp.data.GoalWithSubGoals
import com.voxplanapp.data.Quota
import com.voxplanapp.data.RecurrenceType
import com.voxplanapp.data.QuotaRepository
import com.voxplanapp.data.TimeBankRepository
import com.voxplanapp.data.TodoRepository
import com.voxplanapp.navigation.VoxPlanScreen
import com.voxplanapp.shared.SharedViewModel
import com.voxplanapp.shared.SoundPlayer
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.temporal.ChronoUnit

class FocusViewModel(
    private val savedStateHandle: SavedStateHandle,
    private val todoRepository: TodoRepository,
    private val eventRepository: EventRepository,
    private val timeBankRepository: TimeBankRepository,
    private val quotaRepository: QuotaRepository,
    private val sharedViewModel: SharedViewModel,
    private val soundPlayer: SoundPlayer
): ViewModel() {

    companion object {
        private const val KEY_START_TIMESTAMP = "focus_start_timestamp"
        private const val KEY_CURRENT_TIME = "focus_current_time"
        private const val KEY_TIMER_STATE = "focus_timer_state"
        private const val KEY_TIMER_STARTED = "focus_timer_started"
        private const val KEY_MEDALS_VALUES = "focus_medals_values"
        private const val KEY_MEDALS_TYPES = "focus_medals_types"
        private const val KEY_CLOCK_FACE_MINS = "focus_clock_face_mins"
        private const val KEY_IS_DISCRETE_MODE = "focus_is_discrete_mode"
        private const val KEY_DISCRETE_TASK_LEVEL = "focus_discrete_task_level"
    }

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

    // set up discrete task job
    private val _discreteTaskJob = MutableStateFlow<Job?>(null)
    private val discreteTaskJob: StateFlow<Job?> = _discreteTaskJob.asStateFlow()

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
        // set up quota progress tracking
        setupQuotaTracking()
        // restore saved state if present (survives process death)
        restoreSavedState()
    }

    private fun restoreSavedState() {
        // Check for start timestamp approach
        val startTimestamp = savedStateHandle.get<Long>(KEY_START_TIMESTAMP)

        if (startTimestamp != null) {
            // NEW APPROACH: Calculate elapsed time from start timestamp
            val savedTimerState = savedStateHandle.get<Int>(KEY_TIMER_STATE)?.let { ordinal ->
                TimerState.values().getOrNull(ordinal)
            } ?: TimerState.IDLE

            if (savedTimerState == TimerState.RUNNING) {
                // Calculate total elapsed time (includes process death period)
                val totalElapsedTime = SystemClock.elapsedRealtime() - startTimestamp

                // Restore saved settings
                val savedTimerStarted = savedStateHandle[KEY_TIMER_STARTED] ?: false
                val savedClockFaceMins = savedStateHandle[KEY_CLOCK_FACE_MINS] ?: 30f
                val isDiscreteMode = savedStateHandle[KEY_IS_DISCRETE_MODE] ?: false
                val discreteTaskLevel = savedStateHandle.get<Int>(KEY_DISCRETE_TASK_LEVEL)?.let { ordinal ->
                    DiscreteTaskLevel.values().getOrNull(ordinal)
                } ?: DiscreteTaskLevel.EASY

                // MEDAL CALCULATION: Calculate medals for complete revolutions
                val revolutionMillis = savedClockFaceMins * 60000f
                val completeRevolutions = (totalElapsedTime / revolutionMillis).toInt()

                // Restore existing medals
                val existingMedals = restoreMedals()

                // Award missed medals for revolutions during process death
                val missedMedals = List(completeRevolutions - existingMedals.size) {
                    Medal(savedClockFaceMins.toInt(), MedalType.MINUTES)
                }.takeIf { it.size > 0 } ?: emptyList()

                val allMedals = existingMedals + missedMedals

                // Calculate remainder time (current partial revolution)
                val remainderTime = totalElapsedTime % revolutionMillis.toLong()

                // Update state with calculated values
                focusUiState = focusUiState.copy(
                    currentTime = remainderTime,
                    timerState = TimerState.RUNNING,
                    timerStarted = savedTimerStarted,
                    medals = allMedals,
                    clockFaceMins = savedClockFaceMins,
                    isDiscreteMode = isDiscreteMode,
                    currentTaskLevel = discreteTaskLevel,
                    currentTheme = ColorScheme.WORK
                )

                // Auto-restart timer job
                startTimerJobFromTimestamp(startTimestamp)

                Log.d("FocusViewModel", "Timer restored: totalElapsed=${totalElapsedTime}ms, revolutions=$completeRevolutions, medals=${allMedals.size}, remainder=${remainderTime}ms, auto-restarted")
                return
            }
        }

        // BACKWARD COMPATIBILITY: Old approach
        if (!savedStateHandle.contains(KEY_CURRENT_TIME)) {
            Log.d("FocusViewModel", "No saved state found - starting fresh")
            return
        }

        val savedTime = savedStateHandle[KEY_CURRENT_TIME] ?: 0L
        val savedTimerState = savedStateHandle.get<Int>(KEY_TIMER_STATE)?.let { ordinal ->
            TimerState.values().getOrNull(ordinal)
        } ?: TimerState.IDLE
        val savedTimerStarted = savedStateHandle[KEY_TIMER_STARTED] ?: false
        val medals = restoreMedals()
        val clockFaceMins = savedStateHandle[KEY_CLOCK_FACE_MINS] ?: 30f
        val isDiscreteMode = savedStateHandle[KEY_IS_DISCRETE_MODE] ?: false
        val discreteTaskLevel = savedStateHandle.get<Int>(KEY_DISCRETE_TASK_LEVEL)?.let { ordinal ->
            DiscreteTaskLevel.values().getOrNull(ordinal)
        } ?: DiscreteTaskLevel.EASY

        focusUiState = focusUiState.copy(
            currentTime = savedTime,
            timerState = if (savedTimerState == TimerState.RUNNING) TimerState.PAUSED else savedTimerState,
            timerStarted = savedTimerStarted,
            medals = medals,
            clockFaceMins = clockFaceMins,
            isDiscreteMode = isDiscreteMode,
            currentTaskLevel = discreteTaskLevel
        )

        Log.d("FocusViewModel", "Restored using legacy approach")
    }

    private fun restoreMedals(): List<Medal> {
        val medalsValues = savedStateHandle.get<IntArray>(KEY_MEDALS_VALUES)
        val medalsTypes = savedStateHandle.get<IntArray>(KEY_MEDALS_TYPES)
        return if (medalsValues != null && medalsTypes != null) {
            medalsValues.zip(medalsTypes).map { (value, typeOrdinal) ->
                Medal(value, MedalType.values()[typeOrdinal])
            }
        } else {
            emptyList()
        }
    }

    private fun startTimerJobFromTimestamp(startTimestamp: Long) {
        _timerJob.value = viewModelScope.launch {
            while (isActive) {
                val timestamp = savedStateHandle.get<Long>(KEY_START_TIMESTAMP) ?: return@launch
                val elapsed = SystemClock.elapsedRealtime() - timestamp
                updateTimerState(elapsed)
                delay(1000)
            }
        }
    }

    private fun saveCurrentState() {
        with(focusUiState) {
            // Save medals and settings
            savedStateHandle[KEY_MEDALS_VALUES] = medals.map { it.value }.toIntArray()
            savedStateHandle[KEY_MEDALS_TYPES] = medals.map { it.type.ordinal }.toIntArray()
            savedStateHandle[KEY_CLOCK_FACE_MINS] = clockFaceMins
            savedStateHandle[KEY_IS_DISCRETE_MODE] = isDiscreteMode
            savedStateHandle[KEY_DISCRETE_TASK_LEVEL] = currentTaskLevel.ordinal
        }

        Log.d("FocusViewModel", "Saved medals and settings: medals=${focusUiState.medals.size}")
    }

    private fun clearSavedState() {
        savedStateHandle.remove<Long>(KEY_START_TIMESTAMP)
        savedStateHandle.remove<Long>(KEY_CURRENT_TIME)
        savedStateHandle.remove<Int>(KEY_TIMER_STATE)
        savedStateHandle.remove<Boolean>(KEY_TIMER_STARTED)
        savedStateHandle.remove<IntArray>(KEY_MEDALS_VALUES)
        savedStateHandle.remove<IntArray>(KEY_MEDALS_TYPES)
        savedStateHandle.remove<Float>(KEY_CLOCK_FACE_MINS)
        savedStateHandle.remove<Boolean>(KEY_IS_DISCRETE_MODE)
        savedStateHandle.remove<Int>(KEY_DISCRETE_TASK_LEVEL)

        Log.d("FocusViewModel", "Cleared saved state")
    }

    fun toggleFocusMode() {
        focusUiState = focusUiState.copy(
            isDiscreteMode = !focusUiState.isDiscreteMode
        )
        resetTimer()
        saveCurrentState()
    }

    /* functions relating to timed tasks */

    // this runs upon completion of a revolution of the timer clock
    fun resetTimer() {

        // initialise timer variables for a fresh start
        _timerJob.value?.cancel()
        _timerJob.value = null
        _discreteTaskJob.value?.cancel()
        _discreteTaskJob.value = null

        focusUiState = focusUiState.copy(
            timerState = TimerState.IDLE,
            discreteTaskState = DiscreteTaskState.IDLE,
            currentTime = 0L,
            clockProgress = 0f,
            currentTheme = ColorScheme.REST,
            currentTaskLevel = DiscreteTaskLevel.EASY,
            // we set the end time so that the subsequently scheduled event records actual work done, not simply presence on focus screen
            endTime = LocalTime.now()
        )
    }

    // this is run when we click the start button.  it starts from scratch, or from pause.
    fun startTimer() {
        if (!focusUiState.timerStarted) {       // sets 'true' start time when first timer is begun
            focusUiState = focusUiState.copy(   // start time is otherwise set to start of opening focus mode
                timerStarted = true,
                startTime = LocalTime.now()
            )
        }
        if (focusUiState.timerState == TimerState.IDLE) {       // if timer is starting from idle, play sound
            viewModelScope.launch {
                delay(1500)
                soundPlayer.playSound(R.raw.countdown_start)
            }
        }

        // Calculate start timestamp using monotonic clock
        val startTimestamp = if (focusUiState.timerState == TimerState.PAUSED) {
            // If resuming from pause, calculate when timer originally started
            SystemClock.elapsedRealtime() - focusUiState.currentTime
        } else {
            // Starting fresh
            SystemClock.elapsedRealtime()
        }

        // Save start timestamp immediately (BEFORE any backgrounding)
        savedStateHandle[KEY_START_TIMESTAMP] = startTimestamp
        savedStateHandle[KEY_TIMER_STATE] = TimerState.RUNNING.ordinal

        Log.d("Timer","starting timer @ ${startTimestamp}")

        // Start timer job using the new helper function
        startTimerJobFromTimestamp(startTimestamp)

        focusUiState = focusUiState.copy(
            timerState = TimerState.RUNNING,
            currentTheme = ColorScheme.WORK
        )
        saveCurrentState()
    }

    // set up time-based variables for the focus mode screen
    private fun setupTimeBank() {
        val clockFaceMins = 30
        focusUiState = focusUiState.copy(clockFaceMins = 30f)
    }

    // set up quota progress tracking
    private fun setupQuotaTracking() {
        viewModelScope.launch {
            // Get the goal ID
            val currentGoalId = goalId ?: return@launch

            combine(
                quotaRepository.getQuotaForGoal(currentGoalId),
                timeBankRepository.getEntriesForDate(LocalDate.now()),
                snapshotFlow { focusUiState }
            ) { quota, timeBankEntries, currentFocusState ->

                //Log.d("TAG", "Is quota active for goal ${goalId} date? ${if (quota != null) { quotaRepository.isQuotaActiveForDate(quota, LocalDate.now())} else { "" }}")

                // Only process if quota exists and is active for today
                if (quota == null || !quotaRepository.isQuotaActiveForDate(quota, LocalDate.now())) {
                    return@combine QuotaProgressData(
                        quota = null,
                        bankedMinutes = 0,
                        totalMinutes = 0,
                        progress = 0f,
                        isComplete = false
                    )
                }

                // Calculate banked minutes for this goal today
                val bankedMinutes = timeBankEntries
                    .filter { it.goalId == currentGoalId }
                    .sumOf { it.duration }

                // Calculate Time Vault (medals) minutes
                val vaultMinutes = currentFocusState.medals.sumOf { it.value }

                // Calculate current timer minutes (currentTime is in milliseconds)
                val timerMinutes = (currentFocusState.currentTime / 60000).toInt()

                // Total progress (for the progress bar fill)
                val totalMinutes = bankedMinutes + vaultMinutes + timerMinutes

                // Progress ratio for visual display
                val progress = if (quota.dailyMinutes > 0) {
                    totalMinutes.toFloat() / quota.dailyMinutes.toFloat()
                } else 0f

                // Important: yellow state only when BANKED time >= quota
                // (not just vault + timer, but actually banked for the day)
                val isComplete = bankedMinutes >= quota.dailyMinutes

                QuotaProgressData(
                    quota = quota,
                    bankedMinutes = bankedMinutes,
                    totalMinutes = totalMinutes,
                    progress = progress,
                    isComplete = isComplete
                )

            }.collect { progressData ->

                // Update state with quota progress
                focusUiState = focusUiState.copy(
                    quota = progressData.quota,
                    bankedMinutesToday = progressData.bankedMinutes,
                    totalProgressMinutes = progressData.totalMinutes,
                    quotaProgress = progressData.progress,
                    isQuotaComplete = progressData.isComplete
                )
            }
        }
    }

    // runs every second while the clock is ticking.
    // calculates the current time and the progress position of the ticking 'pie'
    private fun updateTimerState(elapsedTime: Long) {
        val clockFaceMillis = focusUiState.clockFaceMins * 60000f
        val progress = (elapsedTime / clockFaceMillis).coerceAtMost(1f)    // 180000 ms = 30 mins

        // if we hit a revolution, award a medal with that amount of time,
        // re-set clock and start next round
        if (progress == 1f) {
            awardMedal(Medal(focusUiState.clockFaceMins.toInt(), MedalType.MINUTES))
            resetTimer()
            // stops playing the start countdown sound every time the clock ticks round
            focusUiState = focusUiState.copy(timerState = TimerState.PAUSED)
            startTimer()
        }

        focusUiState = focusUiState.copy(
            currentTime = elapsedTime,
            clockProgress = progress
        )

        if (timerSettingsState.usePomodoro) {

            val totalTimeMillis = focusUiState.clockFaceMins * 60000L
            val totalRatioParts = timerSettingsState.workDuration + timerSettingsState.restDuration
            val workRatio = timerSettingsState.workDuration.toFloat() / totalRatioParts

            val workPeriodMillis = (totalTimeMillis * workRatio).toLong()
            val restPeriodMillis = totalTimeMillis - workPeriodMillis

            val currentCycleTime = elapsedTime % totalTimeMillis
            val isRestPeriod = currentCycleTime >= workPeriodMillis

            // if we are at the beginning of the rest period
            if (isRestPeriod != focusUiState.isRestPeriod) {
                focusUiState = focusUiState.copy(isRestPeriod = isRestPeriod)
                if (isRestPeriod) soundPlayer.playSound(R.raw.countdown)
            }
        }
    }

    fun toggleTimer() {
        if (focusUiState.timerState == TimerState.IDLE) startTimer()

        // if timer has been paused, when it starts, we want to retain the elapsed time
        // and add newly elapsed time to the previous number.
        else if (focusUiState.timerState == TimerState.PAUSED) {
            startTimer()
            // todo: play sound for starting timer from pause
        }
        else {
            pauseTimer()
            // todo: play sound for pausing timer
        }
    }

    fun pauseTimer() {
        Log.d("Timer","pausing timer... ")

        // Calculate and save current elapsed time before pausing
        val startTimestamp = savedStateHandle.get<Long>(KEY_START_TIMESTAMP)
        if (startTimestamp != null) {
            val elapsedTime = SystemClock.elapsedRealtime() - startTimestamp
            savedStateHandle[KEY_CURRENT_TIME] = elapsedTime
        }

        // Remove start timestamp since we're no longer running
        savedStateHandle.remove<Long>(KEY_START_TIMESTAMP)
        savedStateHandle[KEY_TIMER_STATE] = TimerState.PAUSED.ordinal

        // timer state is used to determine state of start/pause button
        focusUiState = focusUiState.copy(
            timerState = TimerState.PAUSED,
            currentTheme = ColorScheme.REST
        )

        // this cancels the ticking.
        _timerJob.value?.cancel()
        _timerJob.value = null
        saveCurrentState()
    }

    fun updateClockFaceMinutes(minutes: Float) {
        focusUiState = focusUiState.copy(clockFaceMins = minutes)
        saveCurrentState()
    }

    // this is the 'bank time' button in the timer controls.
    // it grabs a worthwhile chunk of time from the timer and plops it into the vault.
    fun bankTimer() {
        // determine number of minutes on current time (displayed on timer)
        val totalSeconds = (focusUiState.currentTime / 1000).toInt()
        val minutes = totalSeconds / 60
        val remainingSeconds = totalSeconds % 60

        if (minutes > 0) {

            // add medal to vault
            awardMedal(Medal(minutes, MedalType.MINUTES))

            Log.d("TimeBank", "seconds on clock: ${totalSeconds}, minutes: ${minutes}, rem. secs: ${remainingSeconds}")

            // remove minutes from focusUiState current time
            val newCurrentTime = remainingSeconds * 1000L

            Log.d(
                "TimeBank", "awarded medal $minutes minutes." +
                        "old curTime = ${focusUiState.currentTime}, new curTime = ${newCurrentTime}"
            )

            focusUiState = focusUiState.copy(currentTime = newCurrentTime)

            // pause timer
            pauseTimer()
        } else {
            Log.d("TimeBank","No full minutes to bank.")
        }
    }

    /* functions relating to pomodoro timer */

    fun togglePomodoro() {
        timerSettingsState = timerSettingsState.copy(usePomodoro = !timerSettingsState.usePomodoro)
    }

    //
    fun incrementPomodoroRatio() {
        if (timerSettingsState.workDuration < 6) {
            timerSettingsState = timerSettingsState.copy(
                workDuration = timerSettingsState.workDuration + 1
            )
        } else {
            togglePomodoro()
        }
    }

    fun decrementPomodorRatio() {
        if (timerSettingsState.workDuration > 1) {
            timerSettingsState = timerSettingsState.copy(
                workDuration = timerSettingsState.workDuration - 1
            )
        }
        if (!timerSettingsState.usePomodoro) {
            // if pomo is off, initialise at 6:1
            timerSettingsState = timerSettingsState.copy(workDuration = 6)
            togglePomodoro()
        }
    }

    /* functions relating to discrete tasks */

    fun startDiscreteTask() {
        if (focusUiState.discreteTaskState != DiscreteTaskState.IDLE) return

        focusUiState = focusUiState.copy(
            discreteTaskState = DiscreteTaskState.COMPLETING,
            currentTaskLevel = DiscreteTaskLevel.EASY,
            clockProgress = 0f
        )

        _discreteTaskJob.value = viewModelScope.launch {
            var elapsedTime = 0L
            val totalTime = 5000L   // 5 seconds for 1 revolution

            while (true) {
                delay(50)   // update every 50 millisecs
                elapsedTime += 50
                val progress = (elapsedTime.toFloat() / totalTime).coerceIn(0f,1f)
                updateDiscreteTaskProgress(progress)

                if (elapsedTime >= totalTime) {
                    advanceDiscreteTaskLevel()
                    elapsedTime = 0L
                }
            }
        }
    }

    fun stopDiscreteTask() {
        _discreteTaskJob.value?.cancel()
        _discreteTaskJob.value = null

        // award medal based on the current level
        val medal = when (focusUiState.currentTaskLevel) {
            DiscreteTaskLevel.EASY -> Medal(15, MedalType.MINUTES)
            DiscreteTaskLevel.CHALLENGE -> Medal(30, MedalType.MINUTES)
            DiscreteTaskLevel.DISCIPLINE -> Medal(1, MedalType.HOURS)
            DiscreteTaskLevel.EPIC_WIN -> Medal(2, MedalType.HOURS)
            else -> null
        }

        if (medal != null) {
            emptyMedalList()
            awardMedal(medal)
        } else {
            focusUiState = focusUiState.copy(
                discreteTaskState = DiscreteTaskState.IDLE
            )
        }
    }

    private fun updateDiscreteTaskProgress(progress: Float) {
        focusUiState = focusUiState.copy(
            clockProgress = progress
        )
    }

    private fun advanceDiscreteTaskLevel() {
        val nextLevel = when (focusUiState.currentTaskLevel) {
            DiscreteTaskLevel.EASY -> DiscreteTaskLevel.CHALLENGE
            DiscreteTaskLevel.CHALLENGE -> DiscreteTaskLevel.DISCIPLINE
            DiscreteTaskLevel.DISCIPLINE -> DiscreteTaskLevel.EPIC_WIN
            DiscreteTaskLevel.EPIC_WIN -> DiscreteTaskLevel.EASY
        }
        focusUiState = focusUiState.copy(
            currentTaskLevel = nextLevel,
            clockProgress = 0f
        )
    }

    /* functions relating to both types of tasks */

    fun emptyMedalList() {
        focusUiState = focusUiState.copy(
            medals = emptyList()
        )
    }

    fun awardMedal(medal: Medal) {

        soundPlayer.playSound(R.raw.mario_coin)

        focusUiState = focusUiState.copy(
            medals = focusUiState.medals + medal
        )
        Log.d("focusmode","medals: ${focusUiState.medals} just added 1 x $medal")
        saveCurrentState()
    }

    fun bankTime() {
        val medalTime = focusUiState.medals.sumOf { it.value }

        if (medalTime > 0) {
            viewModelScope.launch {
                val goalId = goalUiState?.goal?.id ?: return@launch
                // TimeBank is source of truth for ad-hoc focus sessions
                timeBankRepository.addTimeBankEntry(goalId, medalTime)
            }

            // clear medals
            focusUiState = focusUiState.copy(medals = emptyList())
            soundPlayer.playSound(R.raw.chaching)

            // log what happened
            if (goalId != null) Log.d("TimeBank", "Banked $medalTime minutes into goalId $goalId?")
            else Log.d("TimeBank", "Trying to bank time failed, no goal id")

            // Clear saved state (session complete)
            clearSavedState()

            // Update goal or event with accrued time
        }
    }

    /** focus mode creates an event automatically on exit, as long as >5m focus has been achieved
     *
      */

    fun onExit() {
        createOrUpdateEvent()
        clearSavedState()
    }

    private fun createOrUpdateEvent(): Boolean {
        val startTime = focusUiState.startTime ?: return false
        val endTime = focusUiState.endTime ?: return false

        // Calculate minutes spent
        val minutesSpent = ChronoUnit.MINUTES.between(startTime, endTime)
        if (minutesSpent < 15) return false  // Skip if less than 10 minutes

        viewModelScope.launch {
            if (focusUiState.isFromEvent) {
                // Update existing scheduled event
                eventUiState?.let { existingEvent ->
                    val updatedEvent = existingEvent.copy(
                        startTime = startTime,
                        endTime = endTime
                    )
                    eventRepository.updateEvent(updatedEvent)
                }
            }
            // Note: Ad-hoc focus sessions (not from scheduled event) are tracked
            // via TimeBank only. Events are reserved for explicitly scheduled blocks.
        }
        return true
    }

    /* functions for loading event and goal data into the screen from nav args */

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
enum class DiscreteTaskState {
    IDLE, COMPLETING, COMPLETED
}
enum class DiscreteTaskLevel(val text: String, val description: String, val color: Color) {
    EASY("EASY", "SMALL TASK", Color(0xFFFFEB3B)),
    CHALLENGE("CHALLENGE", "LARGE TASK", Color(0xFFFF9800)),
    DISCIPLINE("DISCIPLINE", "WORTHY GOAL", Color(0xFFF57C00)),
    EPIC_WIN("EPIC WIN", "MAJOR ACCOMPLISHMENT", Color(0xFFE65100))
}

enum class MedalType {
    MINUTES, HOURS
}
enum class ColorScheme {
    WORK, REST
}

data class Medal(val value: Int, val type: MedalType)

data class FocusUiState (
    // focus screen general vars
    val isLoading: Boolean = true,
    val error: String ?= null,
    val currentTheme: ColorScheme = ColorScheme.REST,

    // useful data vars
    val eventId: Int? = null,
    val isFromEvent: Boolean = false,
    val startTime : LocalTime? = null,
    val endTime: LocalTime? = null,
    val date: LocalDate? = null,
    val timerStarted: Boolean = false, // tracks if we've started timing actual focus work.

    // function vars
    val clockProgress: Float = 0f,
    val medals: List<Medal> = emptyList(),

    // timer mode vars
    val totalAccruedTime: Long = 0L,
    val currentTime: Long = 0L,
    val timerState: TimerState = TimerState.IDLE,
    val clockFaceMins: Float = 30f,
    val isRestPeriod: Boolean = false,

    // discrete mode variables
    val isDiscreteMode: Boolean = false,
    val discreteTaskState: DiscreteTaskState = DiscreteTaskState.IDLE,
    val currentTaskLevel: DiscreteTaskLevel = DiscreteTaskLevel.EASY,

    // quota progress fields
    val quota: Quota? = null,
    val bankedMinutesToday: Int = 0,
    val totalProgressMinutes: Int = 0,
    val quotaProgress: Float = 0f,  // 0.0 to 1.0+ (can exceed 100%)
    val isQuotaComplete: Boolean = false,
)

data class TimerSettingsState(
    val workDuration: Int = 5,
    val restDuration: Int = 1,
    val usePomodoro: Boolean = false,
)

// Helper data class for quota progress calculations
private data class QuotaProgressData(
    val quota: Quota?,
    val bankedMinutes: Int,
    val totalMinutes: Int,
    val progress: Float,
    val isComplete: Boolean
)
