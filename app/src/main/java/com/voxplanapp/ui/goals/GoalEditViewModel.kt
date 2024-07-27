package com.voxplanapp.ui.goals

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
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
import com.voxplanapp.navigation.VoxPlanScreen
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/*
    GoalEditViewModel * retrieves the TodoItemStream from repository
    * converts each TodoItem to GoalDetails object
    * emits goalUiState as uiState property, so we can keep the Ui updated.
 */
class GoalEditViewModel(
    savedStateHandle: SavedStateHandle,
    private val todoRepository: TodoRepository
): ViewModel() {

    // retrieves goalId from the saved state handle, if the process has re-started
    private val goalId: Int = checkNotNull(savedStateHandle[VoxPlanScreen.GoalEdit.goalIdArg])

    var goalUiState by mutableStateOf(GoalDetailsUiState())
        private set

    init {
        viewModelScope.launch {
            goalUiState = todoRepository.getItemStream(goalId)
                .filterNotNull()
                .first()
                .toGoalUiState()
        }
    }

    fun updateUiState(goalDetails: GoalDetails) {
        goalUiState =
            GoalDetailsUiState(goalDetails = goalDetails)
    }

    suspend fun saveGoal() {
        todoRepository.insert(goalUiState.goalDetails.toTodo())
    }

    suspend fun getParentGoalTitle(): String? {
        return goalUiState.goalDetails.parentId?.let { parentId ->
            todoRepository.getItemStream(parentId)
                .filterNotNull()
                .first()
                .title
        }
    }

}

data class GoalDetailsUiState(
    val goalDetails: GoalDetails = GoalDetails()
)

data class GoalDetails(
    val id: Int = 0,
    var title: String = "",
    var isDone: Boolean = false,
    var parentId: Int? = null,
    var order: Int = 0,
    var notes: String? = ""
)

// performs a mapping from TodoItem to GoalDetails object
fun TodoItem.toGoalDetails(): GoalDetails = GoalDetails(
    id = id,
    title = title,
    isDone = isDone,
    parentId = parentId,
    order = order,
    notes = notes,
)

fun TodoItem.toGoalUiState(): GoalDetailsUiState = GoalDetailsUiState(
    goalDetails = this.toGoalDetails()
)

// performs a mapping from GoalDetails to TodoItem
fun GoalDetails.toTodo(): TodoItem = TodoItem(
    id = id,
    title = title,
    isDone = isDone,
    parentId = parentId,
    order = order,
    notes = notes,
)
