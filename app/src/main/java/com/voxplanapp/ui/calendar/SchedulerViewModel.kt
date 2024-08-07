package com.voxplanapp.ui.calendar

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.voxplanapp.data.Event
import com.voxplanapp.data.EventRepository
import com.voxplanapp.navigation.VoxPlanScreen
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapConcat
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate

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

    init {
        // set up a stateFlow of today's Events to feed to the Ui for printing

        viewModelScope.launch {
            // observe the _currentDate flow set up above
            _currentDate
                // for each new date emitted (when date changes), switch to a new flow of events
                .flatMapLatest { date ->  eventRepository.getEventsForDate(date) }
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
        viewModelScope.launch {
            eventRepository.deleteEvent(event)
        }
    }

}