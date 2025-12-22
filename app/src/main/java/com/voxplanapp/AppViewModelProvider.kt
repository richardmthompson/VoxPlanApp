package com.voxplanapp

import android.app.Application
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.voxplanapp.navigation.NavigationViewModel
import com.voxplanapp.shared.SharedViewModel
import com.voxplanapp.ui.calendar.SchedulerViewModel
import com.voxplanapp.ui.daily.DailyViewModel
import com.voxplanapp.ui.focusmode.FocusViewModel
import com.voxplanapp.ui.main.MainViewModel
import com.voxplanapp.ui.goals.GoalEditViewModel
import com.voxplanapp.ui.goals.ProgressViewModel
import kotlinx.coroutines.Dispatchers

object AppViewModelProvider {

    val Factory = viewModelFactory {

        // initialise a single instance of the SharedViewModel
        // that is then used by all mainViewModels
        val sharedViewModel = SharedViewModel()

        initializer {
            sharedViewModel
        }

        initializer {
            MainViewModel(
                voxPlanApplication().container.todoRepository,
                voxPlanApplication().container.eventRepository,
                voxPlanApplication().container.timeBankRepository,
                voxPlanApplication().container.quotaRepository,
                soundPlayer = voxPlanApplication().container.soundPlayer,
                ioDispatcher = Dispatchers.IO,
                sharedViewModel = sharedViewModel
            )
        }

        initializer {
            GoalEditViewModel(
                this.createSavedStateHandle(),
                sharedViewModel = sharedViewModel,
                voxPlanApplication().container.todoRepository,
                voxPlanApplication().container.eventRepository,
                voxPlanApplication().container.quotaRepository
            )
        }

        initializer {
            NavigationViewModel()
        }

        initializer {
            SchedulerViewModel(
                this.createSavedStateHandle(),
                eventRepository = voxPlanApplication().container.eventRepository
            )
        }

        initializer {
            FocusViewModel(
                this.createSavedStateHandle(),
                todoRepository = voxPlanApplication().container.todoRepository,
                eventRepository = voxPlanApplication().container.eventRepository,
                timeBankRepository = voxPlanApplication().container.timeBankRepository,
                quotaRepository = voxPlanApplication().container.quotaRepository,
                focusSessionRepository = voxPlanApplication().container.focusSessionRepository,
                soundPlayer = voxPlanApplication().container.soundPlayer,
                sharedViewModel = sharedViewModel
            )
        }

        initializer {
            ProgressViewModel(
                todoRepository = voxPlanApplication().container.todoRepository,
                timeBankRepository = voxPlanApplication().container.timeBankRepository,
                quotaRepository = voxPlanApplication().container.quotaRepository
            )
        }

        initializer {
            DailyViewModel(
                savedStateHandle = this.createSavedStateHandle(),
                eventRepository = voxPlanApplication().container.eventRepository,
                todoRepository = voxPlanApplication().container.todoRepository,
                quotaRepository = voxPlanApplication().container.quotaRepository
            )
        }
    }
}

/**
 * Extension function to queries for [Application] object from voxPlanApplication()
 * and returns an instance of [VoxPlanApplication].
 */
fun CreationExtras.voxPlanApplication(): VoxPlanApplication =
    (this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as VoxPlanApplication)
