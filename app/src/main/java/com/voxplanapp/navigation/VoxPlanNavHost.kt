package com.voxplanapp.navigation

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
import com.voxplanapp.ui.goals.GoalEditScreen
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
        modifier = Modifier.padding(10.dp)
    ) {

        // this is the main goal listing screen
        composable(route = VoxPlanScreen.Main.route) {
            MainScreen(
                navigateToGoalEdit = {
                    // creates a route string e.g. GoalEditDestination.route/123
                    // for editing specific Goal selected by clicking an item in todoList on MainScreen
                    navController.navigate("${VoxPlanScreen.GoalEdit.route}/${it}")
                },
                modifier = modifier.padding(innerPadding)
            )
        }

        composable(
            route = VoxPlanScreen.GoalEdit.routeWithArgs,
            arguments = listOf(
                navArgument(VoxPlanScreen.GoalEdit.goalIdArg) {
                    type = NavType.IntType
                })
        ) {
            GoalEditScreen(
                onNavigateUp = { navController.navigateUp() },
                onNavigateToScheduler = { date ->
                    navController.navigate("${VoxPlanScreen.DaySchedule.route}/${date}")
                },
                modifier = modifier.padding(innerPadding)
            )
        }
        // navigation route into day scheduler.
        composable(
            route = VoxPlanScreen.DaySchedule.routeWithArgs,
            arguments = listOf(navArgument(VoxPlanScreen.DaySchedule.dateArg) {
                type = NavType.StringType
            })
        ) {
            DaySchedule(
                modifier = modifier.padding(innerPadding)
            )
        }



    }
}
