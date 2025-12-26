# Feature: Improve Medal Display in Time Vault

## Background and Motivation

Currently medals show up individually in the Time Vault, and only about 6 consecutive medals fit on screen. This is inadequate for showing accumulated progress during long focus sessions. Users earning 10+ medals see overflow instead of their achievement summary.

## Feature Goal

Display aggregated medal counts instead of individual medals (e.g., "5m × 3" instead of three separate "5m" medals).

---

## Context

### Pattern to Follow
- `MainViewModel.kt:114` - Uses `groupBy { }` for aggregation:
  ```kotlin
  .groupBy { it.goalId }
  .mapValues { (_, entries) -> entries.sumOf { it.duration } }
  ```

### Target Files
- `FocusModeScreen.kt:766-807` - `MedalDisplay` and `MedalItem` composables

### Data Structure
- `Medal(value: Int, type: MedalType)` at `FocusViewModel.kt:904`
- `MedalType`: MINUTES (circle shape) or HOURS (rounded rectangle shape)

### Gotchas
- Must preserve visual distinction: MINUTES = circle, HOURS = rounded square
- Need to handle display when count = 1 (don't show "× 1")
- Sorting: Should show higher-value medals first for visual hierarchy

---

## Implementation Steps

### Step 1: Modify MedalDisplay (FocusModeScreen.kt:766-780)

**BEFORE:**
```kotlin
@Composable
fun MedalDisplay(medals: List<Medal>, modifier: Modifier = Modifier) {
    Text("Time Vault:", style = MaterialTheme.typography.headlineMedium)
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center
    ) {
        medals.forEach { medal ->
            MedalItem(medal)
            Spacer(modifier = Modifier.width(MediumDp))
        }
    }
}
```

**AFTER:**
```kotlin
@Composable
fun MedalDisplay(medals: List<Medal>, modifier: Modifier = Modifier) {
    Text("Time Vault:", style = MaterialTheme.typography.headlineMedium)

    // Group medals by (value, type) and count occurrences
    val groupedMedals = medals
        .groupBy { it.value to it.type }
        .map { (key, list) -> Triple(key.first, key.second, list.size) }
        .sortedByDescending { it.first }  // Show higher values first

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center
    ) {
        groupedMedals.forEach { (value, type, count) ->
            MedalItemWithCount(value = value, type = type, count = count)
            Spacer(modifier = Modifier.width(MediumDp))
        }
    }
}
```

### Step 2: Create MedalItemWithCount (new composable after line 807)

```kotlin
@Composable
fun MedalItemWithCount(value: Int, type: MedalType, count: Int) {
    val (backgroundColor, borderColor, textColor) = when (type) {
        MedalType.MINUTES -> Triple(Color(0xFFFFD700), Color(0xFFFF9800), Color.Black)
        MedalType.HOURS -> Triple(Color(0xFF4CAF50), Color(0xFF2E7D32), Color.White)
    }

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(60.dp)
                .clip(if (type == MedalType.HOURS) RoundedCornerShape(8.dp) else CircleShape)
                .background(backgroundColor)
                .border(
                    width = 2.dp,
                    color = borderColor,
                    shape = if (type == MedalType.HOURS) RoundedCornerShape(8.dp) else CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "${value}${if (type == MedalType.HOURS) "h" else "m"}",
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                color = textColor
            )
        }

        // Only show count if more than 1
        if (count > 1) {
            Text(
                text = "× $count",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}
```

### Step 3: Keep MedalItem for backward compatibility (optional)

The existing `MedalItem` can remain for any other uses, or be deprecated if not used elsewhere.

---

## Success Definition

### Functional Success
- [ ] Medals are grouped by (value, type) - e.g., all "5m" medals shown as one
- [ ] Count displayed below medal when > 1 (e.g., "× 3")
- [ ] Higher value medals appear first (30m before 5m)
- [ ] Visual distinction preserved (MINUTES = circle, HOURS = square)

### Technical Success
- [ ] Build passes (`./gradlew assembleDebug`)
- [ ] No regression - single medals still display correctly
- [ ] Overflow prevented - even 20+ medals fit on screen

### Validation Commands
```bash
./gradlew assembleDebug
./gradlew installDebug
# Manual testing: Earn 5+ medals of same type, verify aggregation
```

---

## Notes

**Confidence**: High

**Pattern Source**: MainViewModel.kt:114 groupBy pattern

**Beads Issue**: VoxPlanApp-p5c
