package com.voxplanapp.navigation

import androidx.navigation.NavType
import androidx.navigation.navArgument
import java.time.LocalDate

sealed class VoxPlanScreen(val route: String) {
    object Main: VoxPlanScreen("main")

    object GoalEdit : VoxPlanScreen("goal_edit") {
        // todo: ensure we can still access titleRes on screen
        val titleRes = "Edit Goal"

        const val goalIdArg = "goalId"
        val routeWithArgs = "$route/{$goalIdArg}"
    }

    object Progress: VoxPlanScreen("progress")

    object DaySchedule : VoxPlanScreen("day_schedule") {
        const val dateArg = "date"
        val routeWithArgs = "$route/{$dateArg}"

        fun createRouteWithDate(date: LocalDate = LocalDate.now()): String {
            return "$route/${date}"
        }
    }

    object FocusMode : VoxPlanScreen("focus_mode") {
        const val goalIdArg = "goalId"
        const val eventIdArg = "eventId"

        val routeWithArgs = "$route/{$goalIdArg}"
        val routeWithEventArg = "$route?$eventIdArg={$eventIdArg}"

        fun createRouteFromGoal(goalId: Int): String = "$route/$goalId"
        fun createRouteFromEvent(eventId: Int): String = "$route?$eventIdArg=$eventId"
    }

    object QuickSchedule : VoxPlanScreen("quick_schedule") {
        const val goalIdArg = "goalId"
        val routeWithArgs = "$route/{$goalIdArg}"
    }

}
