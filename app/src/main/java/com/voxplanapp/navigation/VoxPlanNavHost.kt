package com.voxplanapp.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.voxplanapp.ui.goals.GoalEditDestination
import com.voxplanapp.ui.goals.GoalEditScreen
import com.voxplanapp.ui.main.MainScreen

@Composable
fun VoxPlanNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    // define the routes to different screens on the app
    NavHost(
        navController = navController,
        VoxPlanScreen.Main.name,
        modifier = Modifier.padding(10.dp)
    ) {

        // this is the main goal listing screen
        composable(route = VoxPlanScreen.Main.name) {
            MainScreen(
                navigateToGoalEdit = {
                    // creates a route string e.g. GoalEditDestination.route/123
                    // for editing specific Goal selected by clicking an item in todoList on MainScreen
                    navController.navigate("${GoalEditDestination.route}/${it}")
                }
            )
        }

        // if goal selected on main screen has children, we should open this screen:
        composable(
            route = GoalEditDestination.routeWithArgs,
            arguments = listOf(
                navArgument(GoalEditDestination.goalIdArg) {
                    type = NavType.IntType
                })
        ) {
            GoalEditScreen(
                //navigateToGoalEdit = { navController.navigate("${GoalEditDestination.route}/$it") },
                onNavigateUp = { navController.navigateUp() },
            )
        }

        // if goal selected on main screen doesn't have children, we should open this screen:
        composable(route = VoxPlanScreen.LeafEdit.name) {
        }
    }
}
