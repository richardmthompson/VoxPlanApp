package com.voxplanapp.shared

import android.util.Log
import androidx.lifecycle.ViewModel
import com.voxplanapp.data.GoalWithSubGoals
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.StateFlow

class SharedViewModel: ViewModel() {
    private val _breadcrumbs = MutableStateFlow<List<GoalWithSubGoals>>(emptyList())
    val breadcrumbs: StateFlow<List<GoalWithSubGoals>> = _breadcrumbs.asStateFlow()

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
