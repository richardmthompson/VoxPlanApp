package com.voxplanapp.ui.goals

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.isDebugInspectorInfoEnabled
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.voxplanapp.AppViewModelProvider
import com.voxplanapp.R
import com.voxplanapp.navigation.VoxPlanTopAppBar
import com.voxplanapp.ui.constants.PrimaryDarkColor
import com.voxplanapp.ui.constants.PrimaryLightColor
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GoalEditScreen(
    canNavigateBack: Boolean = true,
    onNavigateUp: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: GoalEditViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val uiState = viewModel.goalUiState
    val coroutineScope = rememberCoroutineScope()
    val parentGoalTitle = runBlocking { viewModel.getParentGoalTitle() }

    Scaffold(
        topBar = {
            VoxPlanTopAppBar(
                title = stringResource(R.string.goal_edit_screen),
                canNavigateBack = canNavigateBack,
                navigateUp = onNavigateUp
            )
        },
    ) { innerPadding ->

        // for now, let's just show the desired info and allow ourselves to back out of it too.
        GoalEntryBody(
            goalDetailsUiState = uiState,
            parentGoalTitle = parentGoalTitle,
            onValueChange = viewModel::updateUiState,
            onSaveClick = {
                          coroutineScope.launch {
                              viewModel.saveGoal()
                              onNavigateUp()
                          }
            },
            modifier = Modifier
                .padding(
                    start = innerPadding.calculateStartPadding(LocalLayoutDirection.current),
                    top = innerPadding.calculateTopPadding(),
                    end = innerPadding.calculateEndPadding(LocalLayoutDirection.current)
                )
                .verticalScroll(rememberScrollState())
                .fillMaxWidth()
        )

    }
}


@Composable
fun GoalEntryBody(
    goalDetailsUiState: GoalDetailsUiState,
    parentGoalTitle: String?,
    onValueChange: (GoalDetails) -> Unit,
    onSaveClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .padding(16.dp)
            .fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        ItemIdRow(
            goalDetails = goalDetailsUiState.goalDetails,
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.LightGray)
                .padding(horizontal = 16.dp, vertical = 4.dp)
        )
        GoalHierarchyDisplay(
            goalDetails = goalDetailsUiState.goalDetails,
            parentGoalTitle = parentGoalTitle,
            modifier = Modifier
                .fillMaxWidth()
        )
        GoalInputForm(
            // loads the input form with the current goal details from the Ui State
            goalDetails = goalDetailsUiState.goalDetails,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth()
        )
        Button(
            onClick = { onSaveClick() },
            shape = MaterialTheme.shapes.small,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(text = stringResource(id = R.string.save_button))
        }
    }
}

@Composable
fun ItemIdRow(
    goalDetails: GoalDetails,
    modifier: Modifier = Modifier
) {
    // item id row
    Row(modifier = modifier) {
        Spacer(modifier = Modifier.weight(1f))
        Text(stringResource(id = R.string.goal_id_label))
        Spacer(modifier = Modifier.padding(end = 16.dp))
        Text(text = goalDetails.id.toString())
    }
}

@Composable
fun GoalHierarchyDisplay(
    goalDetails: GoalDetails,
    parentGoalTitle: String?,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = "Goal Hierarchy - ${if (goalDetails.parentId != null) "SubGoal" else "TopLevelGoal"}:",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(4.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(start = 8.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Home,
                contentDescription = "Top Level Goal",
                tint = PrimaryDarkColor
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = if (goalDetails.parentId == null) goalDetails.title else "${parentGoalTitle}",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold
            )
        }

        if (goalDetails.parentId != null) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(start = 32.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowForward,
                    contentDescription = "Sub Goal",
                    tint = PrimaryLightColor
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = goalDetails.title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun GoalInputForm(
    goalDetails: GoalDetails,
    onValueChange: (GoalDetails) -> Unit = {},
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        OutlinedTextField(
            value = goalDetails.title,
            // updates the Ui State with any changes to the text field as they happen
            onValueChange = { onValueChange(goalDetails.copy(title = it)) },
            label = { Text(stringResource(id = R.string.goal_title)) },
            modifier = Modifier.fillMaxWidth(),
            enabled = enabled,
            singleLine = true
        )
        OutlinedTextField(
            value = goalDetails.notes ?: "",
            onValueChange = { onValueChange(goalDetails.copy(notes = it)) },
            label = { Text(stringResource(R.string.goal_notes)) },
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp),
            enabled = enabled,
            singleLine = false
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = goalDetails.isDone,
                onCheckedChange = { onValueChange(goalDetails.copy(isDone = it)) },
                enabled = enabled
            )
            Text(
                text = stringResource(id = R.string.is_goal_done),
                modifier = Modifier.padding(start=8.dp)
            )
        }

    }
}

@Preview
@Composable
private fun GoalEditScreenPreview() {
    GoalEntryBody(
        goalDetailsUiState = GoalDetailsUiState(
            GoalDetails(
                title = "Shwangy preview",
                isDone = true
            )
        ),
        onValueChange = { /* nothing */ },
        parentGoalTitle = "",
        onSaveClick = { }
    )
}
