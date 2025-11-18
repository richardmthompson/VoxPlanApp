package com.voxplanapp.ui.calendar

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.voxplanapp.data.Event
import com.voxplanapp.data.EventRepository
import com.voxplanapp.navigation.VoxPlanScreen
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapConcat
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate

// flatmaplatest requires the following opt-in for some unknown reason.
@OptIn(ExperimentalCoroutinesApi::class)
class SchedulerViewModel(
    savedStateHandle: SavedStateHandle,
    private val eventRepository: EventRepository
): ViewModel() {

    // get the current date from the argument that's fed in from the navHost
    private val initialDateString: String? = savedStateHandle[VoxPlanScreen.DaySchedule.dateArg]

    // make the internal state a reflection of the current date, and provide a read-only Flow
    private val _currentDate = MutableStateFlow(
        initialDateString?.let { LocalDate.parse(it) } ?: LocalDate.now()
    )
    val currentDate: StateFlow<LocalDate> = _currentDate.asStateFlow()

    val _eventsForCurrentDate = MutableStateFlow<List<Event>>(emptyList())
    val eventsForCurrentDate: StateFlow<List<Event>> = _eventsForCurrentDate

    // used to show the delete parent dialog (do you want to delete parent daily?) when the only scheduled child event of a parent daily is deleted
    private val _showDeleteParentDialog = MutableStateFlow<Event?>(null)
    val showDeleteParentDialog: StateFlow<Event?> = _showDeleteParentDialog.asStateFlow()

    init {
        // set up a stateFlow of today's Events to feed to the Ui for printing

        viewModelScope.launch {
            // observe the _currentDate flow set up above
            _currentDate
                // for each new date emitted (when date changes), switch to a new flow of events
                .flatMapLatest { date ->
                    eventRepository.getEventsForDate(date)
                        .map { events ->
                            // filter for scheduled events with valid start-end times
                            events.filter { event ->
                                event.startTime != null &&
                                event.endTime != null &&
                                // we check for parent daily id.  if it has a parent, it must be a scheduled event.
                                // the parent daily refers to the daily, that itself should not appear in the schedule.
                                event.parentDailyId != null
                            }
                        }
                }
                // collect the new events into the state variable for events
                .collect { events ->
                    _eventsForCurrentDate.value = events
                }
        }
    }

    fun updateDate(newDate: LocalDate) {
        _currentDate.value = newDate
    }

    fun getEventsForDate(date: LocalDate): Flow<List<Event>> {
        return eventRepository.getEventsForDate(date)
    }

    fun addEvent(event: Event) {
        viewModelScope.launch {
            eventRepository.insertEvent(event)
        }
    }

    fun updateEvent(event: Event) {
        // update the repository
        viewModelScope.launch {
            eventRepository.updateEvent(event)
        }

        // immediately update the state with the changed event info, so UI updates immediately.
        if (event.startDate == currentDate.value) {
            val currentEvents = eventsForCurrentDate.value
            val updatedEvents = currentEvents.map {
                // insert the new event into the list
                if (it.id == event.id) event else it
            }
            _eventsForCurrentDate.value = updatedEvents
        }
    }

    fun deleteEvent(event: Event) {
        // We know this is a child event
        viewModelScope.launch {
            // Get all siblings of this event
            val siblings = eventRepository.getEventsWithParentId(event.parentDailyId!!)
                .first()
                .filter { it.id != event.id }

            if (siblings.isEmpty()) {
                // No other scheduled events for this daily
//                _showDeleteParentDialog.value = event.parentDailyId
            } else {
                // Has siblings, just delete this event
                eventRepository.deleteEvent(event.id)
            }
        }
    }

    fun confirmDeleteWithParent(event: Event) {
        viewModelScope.launch {
            eventRepository.deleteEvent(event.id)  // Delete child event
            event.parentDailyId?.let { parentId ->
                eventRepository.deleteEvent(parentId)  // Delete parent daily
            }
            _showDeleteParentDialog.value = null
        }
    }

    fun confirmDeleteChildOnly(event: Event) {
        viewModelScope.launch {
            eventRepository.deleteEvent(event.id)
            _showDeleteParentDialog.value = null
        }
    }

    fun cancelDelete() {
        _showDeleteParentDialog.value = null
    }
}