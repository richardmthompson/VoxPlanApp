package com.voxplanapp.ui.main

import android.util.Log
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.voxplanapp.R
import com.voxplanapp.data.EventRepository
import com.voxplanapp.data.FULLBAR_MINS
import com.voxplanapp.data.GoalWithSubGoals
import com.voxplanapp.data.TimeBankRepository
import com.voxplanapp.data.TodoItem
import com.voxplanapp.data.TodoRepository
import com.voxplanapp.data.pointsForItemCompletion
import com.voxplanapp.model.ActionMode
import com.voxplanapp.shared.SharedViewModel
import com.voxplanapp.shared.SoundPlayer
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.Collections

class MainViewModel (
    private val repository: TodoRepository,
    private val eventRepository: EventRepository,
    private val timeBankRepository: TimeBankRepository,
    private val soundPlayer: SoundPlayer,
    private val ioDispatcher: CoroutineDispatcher,
    private val sharedViewModel: SharedViewModel
) : ViewModel() {

    private var hadDiamond = false

    // transform list of Todos from repository into an (ordered) mainUiState for the UI
    // and update this viewModel with breadcrumbs from shared viewModel
    val mainUiState: StateFlow<MainUiState> = combine(
        repository.getAllTodos(),
        sharedViewModel.breadcrumbs
    ) { todos, breadcrumbs ->

        val currentParentId = breadcrumbs.lastOrNull()?.goal?.id
        MainUiState(
            goalList = sharedViewModel.processGoals(todos, currentParentId),
            breadcrumbs = breadcrumbs
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000L),
        initialValue = MainUiState()
    )

    // set up daily power bar.  this will collect total time accrued in time bank in minutes.
    val queryDate = LocalDate.now()

    val todayTotalTime: StateFlow<Int> = timeBankRepository.getTotalTimeForDate(LocalDate.now())

    // report 0 if we're not getting any report
        .map { value ->
            Log.d("datedebug", "1. Raw value from repository: $value")
            value ?: 0
        }

        .onEach { minutes ->
            Log.d("datedebug", "2. After null check: $minutes")
            val now = LocalDateTime.now()
            val date = LocalDate.now()
            val zoneId = ZoneId.systemDefault()
            val zoneDateTime = ZonedDateTime.now()
            val systemMillis = System.currentTimeMillis()

            val hasDiamond = minutes >= FULLBAR_MINS * 4
            if (hasDiamond && !hadDiamond) {
                soundPlayer.playSound(R.raw.power_up)
                hadDiamond = true
            }
        }

        // makes it a hot state flow, providing a real-time report from the time bank.
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 0
        )

    fun clearBreadcrumbs() {
        sharedViewModel.clearBreadcrumbs()
    }

    fun getTopBreadcrumb(): GoalWithSubGoals? {
        return sharedViewModel.getTopBreadCrumb()
    }

    fun navigateToSubGoals(goal: GoalWithSubGoals) {
        // if parent is in breadcrumbs, it will not be in mainUiState.
        // therefore, search both for the parent as we need it to navigate to the goal
        val parentGoal = when {
            goal.goal.parentId == null -> null
            else -> sharedViewModel.breadcrumbs.value.find { it.goal.id == goal.goal.parentId }
                ?: mainUiState.value.goalList.find { it.goal.id == goal.goal.parentId }
            }
        sharedViewModel.navigateToSubGoal(goal, parentGoal)
    }

    fun navigateUp() {
        sharedViewModel.navigateUp()
    }

    private val _actionMode = mutableStateOf<ActionMode>(ActionMode.Normal)
    val actionMode: State<ActionMode> = _actionMode

    private fun setActionMode(mode: ActionMode) {
        _actionMode.value = mode
    }
    private fun resetActionMode() {
        _actionMode.value = ActionMode.Normal
    }

    fun toggleUpActive() {
        if (actionMode.value == ActionMode.VerticalUp) resetActionMode()
        else { setActionMode(ActionMode.VerticalUp) }
    }
    // toggles the Vertical Down setting & button state
    fun toggleDownActive() {
        if (actionMode.value == ActionMode.VerticalDown) resetActionMode()
        else { setActionMode(ActionMode.VerticalDown) }
    }
    fun toggleHierarchyUp() {
        if (actionMode.value == ActionMode.HierarchyUp) resetActionMode()
        else { setActionMode(ActionMode.HierarchyUp) }
    }
    fun toggleHierarchyDown() {
        if (actionMode.value == ActionMode.HierarchyDown) resetActionMode()
        else { setActionMode(ActionMode.HierarchyDown) }
    }
    private fun deactivateButtons() {
        when (actionMode.value) {
            ActionMode.VerticalUp -> toggleUpActive()
            ActionMode.VerticalDown -> toggleDownActive()
            ActionMode.HierarchyUp -> toggleHierarchyUp()
            ActionMode.HierarchyDown -> toggleHierarchyDown()
            ActionMode.Normal -> {}
        }
    }
    fun reorderItem(goal: GoalWithSubGoals) {
        when (actionMode.value) {
            ActionMode.VerticalUp -> vOrderItem(goal)
            ActionMode.VerticalDown -> vOrderItem(goal)
            ActionMode.HierarchyUp -> moveToSuper(goal)
            ActionMode.HierarchyDown -> moveToSub(goal)
            ActionMode.Normal -> {}
        }
    }

    // for moving the goal Hierarchically.  It becomes subGoal of topLevelGoal above it.
    // for vertical moves, reorderItem()
    private fun moveToSub(goal: GoalWithSubGoals) {
        val currentState = mainUiState.value

        // get our necessary variables
        val (currentList, parentGoal, index) = findGoalContext(currentState.goalList, goal)

        // error checking:
        when (index) {
            -1 -> {
                Log.e("moveToSub", "quitting: cannot find a goal with index ${goal.goal.id} anywhere!")
                deactivateButtons()
                return
            }
            // if there is no goalAbove, or goal has no subGoals, exit.
            0 -> {
                Log.e("moveToSub", "quitting: no goal to move to (no goal above!)")
                deactivateButtons()
                return
            }
        }

        Log.d("moveToSub", "found it.  moving goal: ${goal.goal.title} @ index $index, id ${goal.goal.id}.  currentList $currentList, ")
        val currentGoalWithSubGoals = currentList[index]       // the goal to be moved

        // find goalAbove and its subGoal list
        val goalAbove = currentList[index - 1]
        val order = goalAbove.subGoals.size       // find length of subList (new order# for currentGoal)

        Log.d("moveToSub","adding goal ${goal.goal.id} to parent ${goalAbove.goal.title} ${goalAbove.goal.id}, subGoals: ${goalAbove.subGoals}")
        // update currentGoal's properties
        val updatedGoal = currentGoalWithSubGoals.goal.copy(
            order = order,
            parentId = goalAbove.goal.id
        )

        Log.d("moveToSub", "new parentId: ${goalAbove.goal.id}, new order: ${order}")
        // add the updated goal
        val goalsToUpdate = mutableListOf(updatedGoal)

        // change order# of all subsequent top level goals and add to update list
        for (i in (index + 1) until currentList.size) {
            val g = currentList[i]
            if (g.goal.order != i) {
                goalsToUpdate.add(g.goal.copy(order = i - 1))
            }
        }

        viewModelScope.launch(ioDispatcher) {
            goalsToUpdate.forEach { todoItem ->
                Log.d("moveToSub", "goalsToUpdate - sending to updateItem ${todoItem.title}, parent ${todoItem.parentId}, order ${todoItem.order}")
                repository.updateItem(todoItem)
            }
        }
        deactivateButtons()
    }

    private fun findGoalContext(goalList: List<GoalWithSubGoals>, targetGoal: GoalWithSubGoals):
            Triple<List<GoalWithSubGoals>, GoalWithSubGoals?, Int> {

        Log.d("moveToSub", "findGoalContext: find targetGoal \"${targetGoal.goal.title}\" " +
                "in goalList with size ${goalList.size}\n" +
                "commencing with ${goalList.first().goal.title} and ending with ${goalList.last().goal.title}")
        // return result if goal is in top level list
        goalList.forEachIndexed { index, goal ->
            if (goal.goal.id == targetGoal.goal.id) {
                Log.d("moveToSub","findGoalContext: returning TOP goal list of size ${goalList.size}, \n" +
                        "commencing with ${goalList.first().goal.title} and ending with ${goalList.last().goal.title}")
                return Triple(goalList, null, index)
            }
        }

        // return result if goal is in a sub-goal list
        goalList.forEach { topGoal ->
            topGoal.subGoals.forEachIndexed { index, goal ->
                if (goal.goal.id == targetGoal.goal.id) {
                    Log.d("moveToSub","findGoalContext: returning SUB goal list of size ${topGoal.subGoals.size},\n" +
                            "commencing with ${topGoal.subGoals.first().goal.title} and ending with ${topGoal.subGoals.last().goal.title}" +
                            " goal ${goal.goal.title} and index $index")
                    return Triple(topGoal.subGoals, goal, index)
                }
            }
        }

        return Triple(emptyList(), null, -1)
    }

    private fun moveToSuper(goal: GoalWithSubGoals) {
        // nb: we can now move to super in 2 cases:
        // 1: a sub-goal becoming top level goal in any depth of screen
        // 2: a 'top level goal' on a sub-screen being shunted up to same level as parent

        if (goal.goal.parentId == null) {
            Log.e("moveToSuper", "Goal isn't a sub-goal!  (doesn't have parent)")
            deactivateButtons()
            return
        }

        val currentState = mainUiState.value
        val topLevelList = currentState.goalList

        var parentGoal =
            if (goal in topLevelList) {
                // if goal is a top level goal in current screen, parent is most recent crumb.
                currentState.breadcrumbs.lastOrNull()
            } else {
                // if goal is a sub-goal on current screen, find its parent (a hi level goal)
                topLevelList.find {
                    // return the top level list item that contains the goal as its sub-goal.
                    it.subGoals.any { subGoal -> subGoal.goal.id == goal.goal.id }
                }
        }

        if (parentGoal == null) {
            // if we haven't found our goal's parent, somethings gone very wrong.
            Log.e("moveToSuper", "Couldn't find parent.  This might be a problem.")
            deactivateButtons()
            return
        }

        // either the parent has a grandparent -> we set moveGoal's parent to grandparent...
        // or, it doesn't -> we are adding moveGoal to root, i.e. no parent.
        // in either case, we can set moveGoal's parent to it's current parent's parent.
        val grandparentGoalId = parentGoal.goal.parentId

        // method: find order of parent in its goal list, and place moving-goal after it.
        // then adjust all subsequent goals in parent's goal list.
        val parentGoalIndex = parentGoal.goal.order
        val subGoalIndex = parentGoal.subGoals.indexOfFirst { it.goal.id == goal.goal.id }

        // prepare goal for promotion
        val newOrder = parentGoalIndex + 1
        val updatedGoal = goal.goal.copy(
            parentId = grandparentGoalId,
            order = newOrder
        )

        // initialise list of goals to update
        val goalsToUpdate = mutableListOf(updatedGoal)
        var targetGoalList: List<TodoItem> = emptyList()

        // find the destination list [topLevelList] =
        // if grandparent -> grandparent's subgoal list
        // else -> root goal list
        if (grandparentGoalId != null) {
            // find grandparent in crumbs
            val grandparentGoal = currentState.breadcrumbs.find { crumb -> crumb.goal.id == grandparentGoalId }

            if (grandparentGoal == null) {
                Log.e("moveToSuper", "grandparentGoalId $grandparentGoalId not found in breadcrumbs, aborting...")
                deactivateButtons()
                return
            } else {
                // targetGoalList should be a list of TodoItems:
                targetGoalList = grandparentGoal.subGoals.map { it.goal }
            }

        } else {
            // goal doesn't have grandparent, so we return the root list
            Log.d("moveToSuper", "launching viewmodelscope for repository.getRootTodos()")
            viewModelScope.launch(ioDispatcher) {
                targetGoalList = repository.getRootTodos()
            }
        }

        // bump goals that will be below <target goal> down one in the order
        targetGoalList.forEach { todoItem ->
            if (todoItem.order > parentGoalIndex) {
                goalsToUpdate.add(todoItem.copy(order = todoItem.order + 1))
            }
        }

        // bump subgoals that were after <target goal> up one (as its no longer in this list)
        parentGoal.subGoals.forEachIndexed { index, subGoal ->
            if (index > subGoalIndex) {
                goalsToUpdate.add(subGoal.goal.copy(order = subGoal.goal.order - 1))
            }
        }

        viewModelScope.launch(ioDispatcher) {
            repository.updateItemsInTransaction(goalsToUpdate)
        }
        deactivateButtons()
    }

    private fun vOrderItem(goal: GoalWithSubGoals) {
        val currentState = mainUiState.value
        val topLevelList = currentState.goalList.toMutableList()
        val movingUp = actionMode.value == ActionMode.VerticalUp

        // find the correct list / sublist to reorder
        val (currentList, parentGoal) =
            // if goal is in the top level list currently held in ui state
            if (topLevelList.any { it.goal.id == goal.goal.id }) {
                // return the top level list of goals into currentList
                // get the most recent addition to the breadcrumb trail, or return null if no crumbs
                topLevelList to currentState.breadcrumbs.lastOrNull()
            } else {
                // otherwise, we should find the goal in a sub-goal list,
                // find which parent contains a sub goal that matches the goal id
                // and return the sub goal list into currentList
                val parent = topLevelList.find { it.subGoals.any { subGoal -> subGoal.goal.id == goal.goal.id } }
                parent?.subGoals to parent?.goal
            }

        if (currentList == null) {
            Log.e("reOrderItem", "Could not find item ${goal.goal.title} to reorder")
            deactivateButtons()
            return
        }
        // if moving up and order 0, or moving down and order = last on list, nothing to do
        if ((movingUp && (goal.goal.order == 0)) ||
            (!movingUp && (goal.goal.order == currentList.lastIndex))) {
                Log.d("reOrderItem", "Nowhere for item ${goal.goal.title} to go (top or bottom of list already)")
                deactivateButtons()
                return
            }

        // find the index of the currently selected goal in our list
        val oldIndex = currentList.indexOfFirst { it.goal.id == goal.goal.id }
        Log.d("reOrderItem","ViewModel Reordering item ${goal.goal.title} @ currentIndex $oldIndex")

        // sets newIndex to an appropriate value
        val newIndex = if (movingUp) oldIndex - 1 else { oldIndex + 1 }

        // pull the todo items out and change them
        val goalToMove = currentList[oldIndex].goal
        val goalToSwap = currentList[newIndex].goal

        val goalsToUpdate = listOf(goalToMove.copy(order = newIndex), goalToSwap.copy(order = oldIndex))

        goalsToUpdate.forEach { item ->
            viewModelScope.launch(ioDispatcher) {
                repository.updateItem(item)
            }
        }
        //deactivateButtons()
    }

    fun addTodo(todo: String) {
        // get order for new todo
        val currentState = mainUiState.value
        val topLevelList = currentState.goalList.toMutableList()
        val order = topLevelList.size

        val parentGoal = getTopBreadcrumb()

        // add record to repository
        viewModelScope.launch(ioDispatcher) {
            repository.insert(TodoItem(title = todo, order = order, parentId = parentGoal?.goal?.id, expanded = true))
        }
    }

    fun saveExpandedSetting(todoId: Int, expanded: Boolean) {
        viewModelScope.launch(ioDispatcher) {
            repository.expandItem(todoId, expanded)
        }
    }

    fun completeItem(goal: TodoItem) {

        Log.d("Mainscreen","Triggered complete item checkbox.  Goal: ${goal.title}.  Comp_date: ${goal.completedDate} (pre-click)")

        if (goal.completedDate != null) {
            Log.d("Mainscreen", "About to Un-complete goal")

            // find completed time-bank entry for goal and delete it
            viewModelScope.launch(ioDispatcher) {
                // run toggle function to un-set completed date of todoitem
                repository.completeItem(goal)
                // remove time entry from when goal was marked complete.
                timeBankRepository.deleteCompletionBonus(goal.id, pointsForItemCompletion)
            }

        }
        else {
            Log.d("Mainscreen", "About to set completed date of goal to TODAY.")
            viewModelScope.launch(ioDispatcher) {
                // run toggle function to set completed date of todoitem
                repository.completeItem(goal)

                // accrue time points here.
                timeBankRepository.addTimeBankEntry(
                    goalId = goal.id,
                    duration = pointsForItemCompletion
                )
            }

            soundPlayer.playSound(R.raw.chaching)

        }
    }

    fun deleteItem(goal: GoalWithSubGoals) {
        Log.d("mainscreen", "accessed deleteItem function in mainviewModel: \"${goal.goal.title}\"")
        val parent = goal.goal
        viewModelScope.launch(ioDispatcher) {
            repository.deleteItemAndDescendents(parent)
        }
    }

}

data class MainUiState(
    val goalList: List<GoalWithSubGoals> = listOf(),
    val breadcrumbs: List<GoalWithSubGoals> = listOf()
)
