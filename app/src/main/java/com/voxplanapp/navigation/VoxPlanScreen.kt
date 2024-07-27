package com.voxplanapp.navigation

import androidx.navigation.NavType
import androidx.navigation.navArgument

sealed class VoxPlanScreen(val route: String) {
    object Main: VoxPlanScreen("main")

    object GoalEdit : VoxPlanScreen("goal_edit") {
        // todo: ensure we can still access titleRes on screen
        val titleRes = "Edit Goal"

        const val goalIdArg = "goalId"
        val routeWithArgs = "$route/{$goalIdArg}"
    }

    object DaySchedule : VoxPlanScreen("day_schedule")


}
