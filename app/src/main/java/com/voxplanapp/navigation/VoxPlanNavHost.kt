package com.voxplanapp.navigation

import android.util.Log
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavArgument
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.voxplanapp.ui.calendar.DaySchedule
import com.voxplanapp.ui.daily.DailyScreen
import com.voxplanapp.ui.focusmode.FocusModeScreen
import com.voxplanapp.ui.goals.GoalEditScreen
import com.voxplanapp.ui.goals.ProgressScreen
import com.voxplanapp.ui.main.MainScreen
import java.time.LocalDate

@Composable
fun VoxPlanNavHost(
    navController: NavHostController,
    innerPadding: PaddingValues,
    modifier: Modifier = Modifier
) {
    // define the routes to different screens on the app
    NavHost(
        navController = navController,
        startDestination = VoxPlanScreen.Main.route,
    ) {

        /* MAIN SCREEN */

        composable(route = VoxPlanScreen.Main.route) {
            Log.d("Navigation", "NavHost: Composing Main route in NavHost")
            MainScreen(
                navigateToGoalEdit = {
                    // creates a route string e.g. GoalEditDestination.route/123
                    // for editing specific Goal selected by clicking an item in todoList on MainScreen
                    navController.navigate("${VoxPlanScreen.GoalEdit.route}/${it}")
                },
                onEnterFocusMode = { goalId ->
                        navController.navigate("${VoxPlanScreen.FocusMode.createRouteFromGoal(goalId)}")
                    },
                modifier = modifier.padding(innerPadding)
            )
        }

        /* GOAL EDIT SCREEN */

        composable(
            route = VoxPlanScreen.GoalEdit.routeWithArgs,
            arguments = listOf(
                navArgument(VoxPlanScreen.GoalEdit.goalIdArg) {
                    type = NavType.IntType
                })
        ) {
            Log.d("Navigation", "NavHost: Composing GoalEdit route in NavHost")
            GoalEditScreen(
                onNavigateUp = { navController.navigateUp() },
                onNavigateToScheduler = { date ->
                        navController.navigate("${VoxPlanScreen.DaySchedule.createRouteWithDate(date)}") {
                        popUpTo(navController.graph.startDestinationId)
                        launchSingleTop = true
                    }
                },

                onNavigateToFocusMode = { goalId ->
                    navController.navigate("${VoxPlanScreen.FocusMode.createRouteFromGoal(goalId)}")
                },

                modifier = modifier.padding(innerPadding)
            )
        }

        /* QUOTA PROGRESS REPORTS */

        composable(route = VoxPlanScreen.Progress.route) {
            ProgressScreen(
                modifier = Modifier.padding(innerPadding)
            )
        }

        /* DAY SCHEDULER */

        composable(
            route = VoxPlanScreen.DaySchedule.routeWithArgs,
            arguments = listOf(navArgument(VoxPlanScreen.DaySchedule.dateArg) {
                type = NavType.StringType
            })
        ) {
            Log.d("Navigation", "NavHost: Composing DaySchedule route in NavHost")
            DaySchedule(
                onEnterFocusMode = { event -> navController.navigate(VoxPlanScreen.FocusMode.createRouteFromEvent(event.id)) {
                    popUpTo(VoxPlanScreen.DaySchedule.route)
                    launchSingleTop = true
                } },
                modifier = modifier.padding(innerPadding)
            )
        }

        /* FOCUS MODE */

        composable(
            route = VoxPlanScreen.FocusMode.routeWithArgs,
            arguments = listOf(navArgument(VoxPlanScreen.FocusMode.goalIdArg) {
                type = NavType.StringType
            })
        ) {
            FocusModeScreen(
                onNavigateUp = { navController.navigateUp() }
            )
        }

        composable(
            route = VoxPlanScreen.FocusMode.routeWithEventArg,
            arguments = listOf(navArgument(VoxPlanScreen.FocusMode.eventIdArg) {
                type = NavType.StringType
            })
        ) {
            FocusModeScreen(
                onNavigateUp = { navController.navigateUp() }
            )
        }

        /** DAILY SCREEN */
        composable(
            route = VoxPlanScreen.Daily.routeWithArgs,
            arguments = listOf(navArgument(VoxPlanScreen.Daily.dateArg) {
                type = NavType.StringType
            })
        ) {
            DailyScreen(

                modifier = modifier.padding(innerPadding)
            )
        }


    }
}
