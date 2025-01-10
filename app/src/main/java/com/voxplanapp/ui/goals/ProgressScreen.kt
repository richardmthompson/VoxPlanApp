package com.voxplanapp.ui.goals

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.voxplanapp.AppViewModelProvider
import com.voxplanapp.ui.constants.MediumDp
import com.voxplanapp.ui.constants.TopAppBarBgColor
import com.voxplanapp.ui.main.Diamond
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.DateTimeFormatter

// ProgressScreen.kt
@Composable
fun ProgressScreen(
    modifier: Modifier = Modifier,
    viewModel: ProgressViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val uiState by viewModel.uiState.collectAsState()
    val today = remember { LocalDate.now().dayOfWeek }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Progress",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            WeekNavigator(
                currentWeek = uiState.currentWeek,
                onPreviousWeek = viewModel::previousWeek,
                onNextWeek = viewModel::nextWeek
            )
        }

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            shape = RoundedCornerShape(2.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFF002702),
            ),
        ) {
            WeeklySummary(
                weekTotal = uiState.weekTotal,
                completedDays = uiState.completedDays
            )
        }

        // Daily Progress
        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(uiState.dailyProgress) { dayProgress ->
                DayProgressCard(
                    dayProgress = dayProgress,
                    currentWeek = uiState.currentWeek
                )
            }
        }
    }
}

@Composable
private fun WeeklySummary(
    weekTotal: WeekTotal,
    completedDays: Int,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(TopAppBarBgColor)
            .padding(10.dp)
    ) {
        Text(
            text = "WEEK TOTAL",
            style = MaterialTheme.typography.titleMedium,
            color = Color(0xFF3F51B5),
            modifier = Modifier.padding(bottom = 8.dp)
        )

        // Emerald tracker
        Row(
            modifier = Modifier.padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            repeat(7) { index ->
                val filled = index < completedDays
                EmeraldIcon(filled = filled)
            }
        }

        // Goal summaries
        weekTotal.goalTotals.forEach { goalTotal ->
            GoalSummaryRow(goalTotal)
        }
    }
}

@Composable
private fun DayProgressCard(
    // the dayProgress argument corresponds to information in a single day
    dayProgress: DayProgress,
    currentWeek: LocalDate,
    modifier: Modifier = Modifier
) {
    val today = LocalDate.now()
    val isCurrentWeek = today.with(DayOfWeek.MONDAY).isEqual(currentWeek)

    val isToday = dayProgress.dayOfWeek == today.dayOfWeek && isCurrentWeek
    val isBeforeToday = dayProgress.dayOfWeek.value < today.dayOfWeek.value && isCurrentWeek
    val isFuture = dayProgress.dayOfWeek.value > today.dayOfWeek.value

    val cardColor = when {
        isToday -> Color(0xFFBAF7FF)
        isBeforeToday -> Color(0xFFFFF8DC)
        else -> Color(0xFFEEEEEE)
    }
    val borderColor = when {
        isToday -> Color(0xFF3F51B5)
        isBeforeToday -> Color(0xC1FF9800)
        else -> Color.LightGray
    }

    val textColor = when {
        isFuture -> Color.Gray
        isToday -> Color(0xFF3F51B5)
        else -> Color.Black
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .border(
                2.dp,
                borderColor,
                RoundedCornerShape(8.dp)
            ),
            colors = CardDefaults.cardColors(containerColor = cardColor)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Day header with emerald/diamonds
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (dayProgress.isComplete) {
                    EmeraldIcon(filled = true)
                }
                Text(
                    text = dayProgress.dayOfWeek.name.take(3),
                    color = textColor,
                    style = MaterialTheme.typography.titleMedium
                )
                repeat(dayProgress.diamonds) {
                    DiamondIcon()
                }
            }

            // Goal progress
            dayProgress.goalProgress.forEach { goalProgress ->
                GoalProgressRow(goalProgress)
            }
        }
    }
}

// Additional ProgressScreen.kt components

@Composable
fun DiamondIcon(
    modifier: Modifier = Modifier,
    size: Dp = 24.dp
) {
    Diamond(size)
}

@Composable
fun EmeraldIcon(
    filled: Boolean,
    modifier: Modifier = Modifier,
    size: Dp = 24.dp
) {
    Gem(
        modifier = modifier.size(size),
        tint = if (filled) {
            MaterialTheme.colorScheme.tertiary
        } else {
            MaterialTheme.colorScheme.outline
        }
    )
}
@Composable
fun Gem(
    modifier: Modifier = Modifier,
    tint: androidx.compose.ui.graphics.Color,
    filled: Boolean = true
) {
    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height

        val path = Path().apply {
            // Create gem shape
            moveTo(width * 0.125f, height * 0.083f) // Start at top-left (3, 2)
            lineTo(width * 0.875f, height * 0.083f) // Top line to (21, 2)
            lineTo(width * 0.5f, height * 0.75f)    // Bottom point (12, 18)
            close()

            // Add internal lines for facets
            moveTo(width * 0.5f, height * 0.75f)    // Center bottom (12, 18)
            lineTo(width * 0.125f, height * 0.083f) // To top-left (3, 2)
            moveTo(width * 0.5f, height * 0.75f)    // Center bottom (12, 18)
            lineTo(width * 0.875f, height * 0.083f) // To top-right (21, 2)

            // Add horizontal line for middle facet
            moveTo(width * 0.3125f, height * 0.333f) // Left middle point (7.5, 8)
            lineTo(width * 0.6875f, height * 0.333f) // Right middle point (16.5, 8)
        }

        // Draw filled shape if requested
        if (filled) {
            drawPath(
                path = path,
                color = tint,
                style = Fill
            )
        }

        // Draw outline
        drawPath(
            path = path,
            color = tint,
            style = Stroke(
                width = size.width * 0.083f, // Stroke width proportional to size
                cap = StrokeCap.Round,
                join = StrokeJoin.Round
            )
        )
    }
}

@Composable
fun GoalProgressRow(
    goalProgress: GoalProgress,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {

        Text(
            text = goalProgress.title,
            style = MaterialTheme.typography.bodyLarge,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.width(120.dp),
        )

        Row(
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Show stars based on hours achieved
            val hoursAchieved = goalProgress.minutesAchieved / 60
            val quotaHours = goalProgress.quotaMinutes / 60

            repeat(quotaHours) { index ->
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = "Hour ${index + 1}",
                    tint = if (index < hoursAchieved) {
                        Color(0xFFFFC107)
                    } else {
                        MaterialTheme.colorScheme.outline
                    },
                    modifier = Modifier.size(24.dp)
                )
            }

            // Show overachievement stars in different color
            repeat(hoursAchieved - quotaHours) {
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = "Extra Hour",
                    tint = Color(0xFFDA70D6),
                    modifier = Modifier.size(16.dp)
                )
            }
        }

        // Show actual time
        Text(
            text = "${goalProgress.minutesAchieved / 60}h ${goalProgress.minutesAchieved % 60}m",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun GoalSummaryRow(
    goalTotal: GoalTotal,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = goalTotal.title,
            style = MaterialTheme.typography.bodyLarge
        )

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                repeat(goalTotal.diamonds) {
                    DiamondIcon(size = 20.dp)
                }
                repeat(goalTotal.stars) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = "Hour Achievement",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Text(
                text = "${goalTotal.totalMinutes / 60}h ${goalTotal.totalMinutes % 60}m",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun WeekNavigator(
    currentWeek: LocalDate,
    onPreviousWeek: () -> Unit,
    onNextWeek: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onPreviousWeek) {
            Icon(
                imageVector = Icons.Default.ChevronLeft,
                contentDescription = "Previous Week"
            )
        }

        Text(
            text = "${currentWeek.format(DateTimeFormatter.ofPattern("MMM d"))} - " +
                    "${currentWeek.plusDays(6).format(DateTimeFormatter.ofPattern("MMM d"))}",
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Medium
        )

        IconButton(onClick = onNextWeek) {
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = "Next Week"
            )
        }
    }
}