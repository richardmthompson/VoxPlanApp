package com.voxplanapp.data

data class GoalWithSubGoals(
    val goal: TodoItem,
    val subGoals: List<GoalWithSubGoals>
)
