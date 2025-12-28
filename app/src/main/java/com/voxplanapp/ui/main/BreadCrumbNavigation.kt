package com.voxplanapp.ui.main

import android.text.Layout
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.voxplanapp.data.GoalWithSubGoals
import com.voxplanapp.ui.constants.TertiaryBorderColor
import com.voxplanapp.ui.constants.ToolbarBorderColor
import com.voxplanapp.ui.constants.ToolbarColor
import com.voxplanapp.ui.constants.ToolbarIconColor
import kotlin.math.truncate

@Composable
fun BreadcrumbNavigation(
    breadcrumbs: List<GoalWithSubGoals>,
    onMainClick: () -> Unit,
    onBreadcrumbClick: (GoalWithSubGoals) -> Unit,
    modifier: Modifier = Modifier
) {
    // defining some layout specifics here for now
    var availableWidth by remember { mutableIntStateOf(0) }
    val density = LocalDensity.current
    val fontScale = LocalDensity.current.fontScale
    val layoutDims = LayoutDims(fontSize = 18.sp, chevronSize = 38.dp, chevronPad = 8.dp, density, fontScale)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 0.dp)
    ) {
        Column(
            modifier = modifier
                .fillMaxWidth()
                .border(1.dp, TertiaryBorderColor)
                .onSizeChanged { size -> availableWidth = size.width }
        ) {
            val lines = createBreadcrumbLines(breadcrumbs, availableWidth, layoutDims)

            lines.forEach { line ->
                DisplayBreadCrumbLine(line, onMainClick, onBreadcrumbClick, layoutDims)
            }

        }
    }
}

// this function takes the breadcrumb list and creates a list of breadcrumb lines
// by analysing the length of breadcrumbs and assigning them to lines, if multi-line breadcrumbs
// are required.
fun createBreadcrumbLines(
    breadcrumbs: List<GoalWithSubGoals>,
    availableWidth: Int,
    layoutDims: LayoutDims,
): List<List<LayoutBreadcrumb>> {

    val lines = mutableListOf<MutableList<LayoutBreadcrumb>>()
    var currentLine = mutableListOf<LayoutBreadcrumb>()
    var currentLineWidth = 0

    val mainText = "MAIN"
    val mainWidth = estimateWidth(mainText, true, layoutDims)
    Log.d("breadcrumbs", "estimated width for MAIN: $mainWidth")
    currentLine.add(LayoutBreadcrumb(mainText, true, null))
    currentLineWidth = mainWidth

    for (goal in breadcrumbs) {

        val maxWords = 2
        val words = goal.goal.title.split(" ")
        val truncatedText = if (words.size > maxWords) {
            words.take(maxWords).joinToString(" ") + "..."
        } else {
            words.joinToString(" ")
        }
        val itemWidth = estimateWidth(truncatedText, currentLine.isEmpty(), layoutDims)
        Log.d("breadcrumbs", "estimatd width for breadcrumb $truncatedText = $itemWidth (available width = $availableWidth)")

        if (currentLineWidth + itemWidth >  availableWidth && currentLine.isNotEmpty()) {
            lines.add(currentLine)
            Log.d("breadcrumbs", "adding line # ${ lines.size -1 } : $currentLine")
            currentLine = mutableListOf()
            currentLineWidth = 0
        }

        currentLine.add(LayoutBreadcrumb(truncatedText, false, goal))
        currentLineWidth += itemWidth
    }

    if (currentLine.isNotEmpty()) {
        lines.add(currentLine)
    }

    return lines

}

fun estimateWidth(
    text: String,
    isFirst: Boolean,
    layoutDims: LayoutDims
): Int {
    val fontMult = 0.6f

    val fontSize = with(layoutDims.density) { layoutDims.fontSize.toPx() * layoutDims.fontScale}
    val textWidth = (text.length * fontSize * fontMult).toInt()
    val iconWidth = with(layoutDims.density) { layoutDims.chevronSize.roundToPx() }
    val iconPad = with(layoutDims.density) { layoutDims.chevronPad.roundToPx() }

    val iconSpace = if (isFirst) 0 else iconWidth + iconPad
    return textWidth + iconSpace
}

@Composable
fun DisplayBreadCrumbLine(
    line: List<LayoutBreadcrumb>,
    onMainClick: () -> Unit,
    onBreadcrumbClick: (GoalWithSubGoals) -> Unit,
    layoutDims: LayoutDims
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .background(color = ToolbarColor, shape = RoundedCornerShape(2.dp))
            .border(1.dp, color = ToolbarBorderColor, shape = RoundedCornerShape(2.dp))
            .padding(top = 0.dp)
    ) {
        line.forEach { item ->
            Text(
                text = item.text,
                fontSize = layoutDims.fontSize,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .clickable { if (item.isMain) onMainClick() else item.originalGoal?.let { onBreadcrumbClick(it) } }
                    .padding(start = 12.dp, top = 12.dp, bottom = 12.dp, end = 12.dp)
            )
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = "Navigate to",
                tint = ToolbarIconColor,
                modifier = Modifier
                    .size(layoutDims.chevronSize)
                    .padding(top = 8.dp, start = 0.dp, end = 0.dp)
            )
        }
    }
}


// a derivative class used for displaying the breadcrumbs.
data class LayoutBreadcrumb(
    val text: String,
    val isMain: Boolean,
    val originalGoal: GoalWithSubGoals?
)

data class LayoutDims(
    val fontSize: TextUnit,
    val chevronSize: Dp,
    val chevronPad: Dp,
    val density: Density,
    val fontScale: Float,
)