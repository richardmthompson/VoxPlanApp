package com.voxplanapp.ui.goals

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.voxplanapp.ui.constants.*
import java.time.DayOfWeek

@Composable
fun QuotaSettingsSection(
    quotaMinutes: Int,
    activeDays: Set<DayOfWeek>,
    onQuotaMinutesChanged: (Int) -> Unit,
    onActiveDaysChanged: (Set<DayOfWeek>) -> Unit,
    onRemoveQuota: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(color = ToolbarColor, shape = RoundedCornerShape(MediumDp))
            .border(width = 1.dp, ToolbarBorderColor, shape = RoundedCornerShape(MediumDp))
            .padding(MediumDp)
    ) {
        Text(
            text = "Daily Quota",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = MediumDp)
        )

        // Minutes Selector
        Column(modifier = Modifier.padding(bottom = LargeDp)) {
            Text(
                text = "Target minutes per day",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray,
                modifier = Modifier.padding(bottom = SmallDp)
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(vertical = SmallDp)
            ) {
                IconButton(
                    onClick = {
                        if (quotaMinutes >= 30) {
                            onQuotaMinutesChanged(quotaMinutes - 15)
                        }
                    },
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(Icons.Default.Remove, "Decrease minutes")
                }

                Text(
                    text = quotaMinutes.toString(),
                    style = MaterialTheme.typography.titleLarge,
                    fontFamily = FontFamily.Monospace,
                    modifier = Modifier.width(80.dp),
                    textAlign = TextAlign.Center
                )

                IconButton(
                    onClick = { onQuotaMinutesChanged(quotaMinutes + 15) },
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(Icons.Default.Add, "Increase minutes")
                }

                Text(
                    text = "minutes",
                    color = Color.Gray,
                    modifier = Modifier.padding(start = MediumDp)
                )
            }
        }

        // Day Selector
        Column {
            Text(
                text = "Active days",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray,
                modifier = Modifier.padding(bottom = SmallDp)
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(SmallDp),
                modifier = Modifier.fillMaxWidth()
            ) {
                DayOfWeek.values().forEach { day ->
                    DayButton(
                        day = day,
                        isSelected = day in activeDays,
                        onClick = {
                            val newDays = activeDays.toMutableSet()
                            if (day in activeDays) {
                                newDays.remove(day)
                            } else {
                                newDays.add(day)
                            }
                            onActiveDaysChanged(newDays)
                        }
                    )
                }
            }
        }

        // Quick Presets
        Row(
            horizontalArrangement = Arrangement.spacedBy(SmallDp),
            modifier = Modifier.padding(top = MediumDp)
        ) {
            QuickPresetButton("Weekdays") {
                onActiveDaysChanged(setOf(
                    DayOfWeek.MONDAY,
                    DayOfWeek.TUESDAY,
                    DayOfWeek.WEDNESDAY,
                    DayOfWeek.THURSDAY,
                    DayOfWeek.FRIDAY
                ))
            }

            QuickPresetButton("Weekends") {
                onActiveDaysChanged(setOf(
                    DayOfWeek.SATURDAY,
                    DayOfWeek.SUNDAY
                ))
            }

            QuickPresetButton("Every Day") {
                onActiveDaysChanged(DayOfWeek.values().toSet())
            }

            QuickPresetButton("Clear") {
                onRemoveQuota()
            }

        }
    }
}

@Composable
private fun DayButton(
    day: DayOfWeek,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(44.dp)
            .clip(CircleShape)
            .background(
                if (isSelected) MaterialTheme.colorScheme.primary
                else Color.LightGray.copy(alpha = 0.2f)
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = day.name.take(1),
            color = if (isSelected) Color.White else Color.Gray,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun QuickPresetButton(
    text: String,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .border(1.dp, Color.Gray.copy(alpha = 0.3f), RoundedCornerShape(50))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Gray
        )
    }
}