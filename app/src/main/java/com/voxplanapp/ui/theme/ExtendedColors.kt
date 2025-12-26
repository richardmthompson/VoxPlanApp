package com.voxplanapp.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.material3.MaterialTheme

/**
 * Extended colors for VoxPlanApp UI elements that don't fit Material 3 semantic roles.
 *
 * Contrast testing required: All extended colors must be tested on every background
 * they appear on using WebAIM Contrast Checker (https://webaim.org/resources/contrastchecker/).
 *
 * Minimum ratios: 4.5:1 for text (AA), 3:1 for UI components/large text.
 */
@Immutable
data class ExtendedColors(
    // Focus Mode: Medal colors (30, 60, 90, 120+ minute achievements)
    val medalBronze: Color = Color.Unspecified,
    val medalSilver: Color = Color.Unspecified,
    val medalGold: Color = Color.Unspecified,
    val medalDiamond: Color = Color.Unspecified,

    // Focus Mode: Power bar (timer progress indicator)
    val powerBarFill: Color = Color.Unspecified,
    val powerBarBackground: Color = Color.Unspecified,

    // Goals: Category colors (future use - currently not implemented)
    val goalCategory1: Color = Color.Unspecified,
    val goalCategory2: Color = Color.Unspecified,
    val goalCategory3: Color = Color.Unspecified,

    // Schedule: Event type colors
    val eventScheduled: Color = Color.Unspecified,
    val dailyTaskHighlight: Color = Color.Unspecified,

    // Quota: Progress indication
    val quotaComplete: Color = Color.Unspecified,
    val quotaProgress: Color = Color.Unspecified,
)

// Light mode palette (from color inventory + manual hardcoded colors analysis)
val lightExtendedColors = ExtendedColors(
    // Medal colors (from FocusModeScreen.kt analysis)
    medalBronze = Color(0xFFCD7F32),    // Bronze medal (30 min)
    medalSilver = Color(0xFFC0C0C0),    // Silver medal (60 min)
    medalGold = Color(0xFFFFD700),      // Gold medal (90 min)
    medalDiamond = Color(0xFFB9F2FF),   // Diamond medal (120+ min)

    // Power bar colors (from MainScreen.kt and FocusModeScreen.kt)
    powerBarFill = Color(0xFF4CAF50),     // Green fill
    powerBarBackground = Color(0xFFE0E0E0), // Light gray background

    // Goal colors (placeholder - not currently used in codebase)
    goalCategory1 = Color(0xFF82BD85),  // Light green (PrimaryLightColor)
    goalCategory2 = Color(0xFFC7E2C9),  // Pale green (SecondaryColor)
    goalCategory3 = Color(0xFF82babd),  // Teal (TertiaryColor)

    // Event colors (from EventRepository and DaySchedule.kt)
    eventScheduled = Color(0xFF41c300),      // EventBoxColor from Colors.kt
    dailyTaskHighlight = Color(0xFFFFA500),  // Orange (from hardcoded analysis)

    // Quota colors (from Colors.kt)
    quotaComplete = Color(0xFFFFC107),    // QuotaCompleteBackgroundColor
    quotaProgress = Color(0xFFFF5722),    // QuotaProgressFillColor (orange)
)

// Dark mode palette (future enhancement - not implemented in this task)
val darkExtendedColors = ExtendedColors(
    // TODO: Implement dark mode extended colors
)

// CompositionLocal for accessing extended colors
val LocalExtendedColors = staticCompositionLocalOf { ExtendedColors() }

// Extension property for MaterialTheme to access extended colors
val MaterialTheme.extendedColors: ExtendedColors
    @Composable
    @ReadOnlyComposable
    get() = LocalExtendedColors.current
