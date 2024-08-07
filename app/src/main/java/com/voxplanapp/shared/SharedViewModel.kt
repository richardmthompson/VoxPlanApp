package com.voxplanapp.shared

import android.util.Log
import androidx.lifecycle.ViewModel
import com.voxplanapp.data.GoalWithSubGoals
import com.voxplanapp.data.TodoItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.StateFlow

class SharedViewModel: ViewModel() {
    private val _breadcrumbs = MutableStateFlow<List<GoalWithSubGoals>>(emptyList())
    val breadcrumbs: StateFlow<List<GoalWithSubGoals>> = _breadcrumbs.asStateFlow()

    private val MAX_DEPTH = 3

    // receives list of todos (from repository) and optional parent,
    // returns list of goals at that level and their sub-goals
    fun processGoals(todos: List<TodoItem>, parentId: Int?, depth: Int = 1): List<GoalWithSubGoals> {
        return todos.filter { it.parentId == parentId }
            .sortedBy { it.order }
            .map { goal ->
                val subGoals = if (depth < MAX_DEPTH) {
                    processGoals(todos, goal.id, depth + 1)
                } else {
                    emptyList()
                }
                GoalWithSubGoals(
                    goal = goal,
                    subGoals = subGoals
                )
            }
    }

    // will  return null if not found
    // returns a specific GoalWithSubGoals, mainly for the GoalEditScreen.
    fun getGoalWithSubGoals(todos: List<TodoItem>, goalId: Int, depth: Int = 1): GoalWithSubGoals? {
        // retrieve goal from repository
        val goal = todos.find { it.id == goalId } ?: return null

        // convert to GoalWithSubGoals.
        val subGoals =
            todos.filter { it.parentId == goal?.parentId }
                .sortedBy { it.order }
                .map { subGoal -> GoalWithSubGoals(goal = subGoal, subGoals = emptyList())}

        return GoalWithSubGoals(goal = goal, subGoals = subGoals)
    }


    fun clearBreadcrumbs() {
        _breadcrumbs.value = emptyList()
    }

    fun getTopBreadCrumb(): GoalWithSubGoals? {
        val currentBreadcrumbs = breadcrumbs.value
        return currentBreadcrumbs.lastOrNull()
    }

    fun navigateToSubGoal(goal: GoalWithSubGoals, parentGoal: GoalWithSubGoals?) {
        // we arrive here from a breadcrumb click or a goal/sub-goal icon click
        Log.d("SharedViewModel","Navigating to subgoal ${goal.goal.title} with parentgoal ${parentGoal?.goal?.title}")
        val currentBreadcrumbs = breadcrumbs.value

        // if goal doesn't have a parent, we've clicked straight into a top level goal.
        if (parentGoal == null) {
            _breadcrumbs.value = listOf(goal)
            return
        }

        // find goal in breadcrumb trail (in case we arrive from breadcrumb)
        val index = breadcrumbs.value.indexOfFirst {
            it.goal.id == goal.goal.id
        }
        // if goal is in breadcrumb trail, navigate to it
        if (index != -1) {
            Log.d("SharedViewModel", "Navigated to: ${goal.goal.title}, Breadcrumbs: ${_breadcrumbs.value.map { it.goal.title }}")
            _breadcrumbs.value = currentBreadcrumbs.take(index + 1)
            return
        }

        val parentIndex = currentBreadcrumbs.indexOfFirst { it.goal.id == parentGoal.goal.id }
        if (parentIndex != -1) {
            // parent is in the breadcrumb trail (we've clicked top level goal in a sub hierarchy)
            // so grab the breadcrumbs inc parent and add goal to end
            Log.d("SharedViewModel", "Navigated to: ${goal.goal.title}, Breadcrumbs: ${_breadcrumbs.value.map { it.goal.title }}")
            _breadcrumbs.value = currentBreadcrumbs.take(parentIndex +1) + goal
            return
        }

        // parent not in breadcrumb trail; we must have clicked a sub-goal.
        // so add both parent and goal to current breadcrumbs
        Log.d("SharedViewModel", "Navigated to: ${goal.goal.title}, Breadcrumbs: ${_breadcrumbs.value.map { it.goal.title }}")
        _breadcrumbs.value = currentBreadcrumbs + parentGoal + goal
}

    fun navigateUp() {
        _breadcrumbs.value = _breadcrumbs.value.dropLast(1)
    }

}
