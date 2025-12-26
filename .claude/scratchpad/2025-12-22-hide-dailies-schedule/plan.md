# Hide Dailies and Schedule Features for MVP Release

**Date**: 2025-12-22
**Tier**: 1 (Simple)
**Confidence**: High

---

## Background and Motivation

VoxPlanApp is preparing for MVP release this week. The Dailies and Schedule features are nice-to-have but not critical for the core value proposition (Goals + FocusMode + Progress). To ship faster, these features should be hidden from the UI without deleting any code, allowing for easy re-enablement post-MVP.

**User Requirements**:
- Ship MVP this week with core features only
- Preserve all existing Dailies/Schedule code for future use
- Clean UI with only essential navigation items
- Minimal risk, maximum speed

---

## Feature Goal

Hide Dailies and Schedule screens from bottom navigation bar by commenting out their nav items, keeping all ViewModels, Screens, and Routes intact in the codebase for future re-enablement.

---

## Context

### Affected Files
- **Primary**: `/app/src/main/java/com/voxplanapp/navigation/VoxPlanApp.kt:244-269`
  - Contains `items` list with 4 bottom nav items
  - Lines 251-256: Daily nav item (index 1)
  - Lines 263-268: Schedule nav item (index 3)

### Pattern to Follow
- **Approach**: Comment out items from list (Kotlin list literal)
- **Pattern**: Standard multi-line comment `/* ... */` or line comments `//`
- **Reference**: Simple list modification, no special pattern needed

### Known Gotchas
- **Index shift**: After commenting out Daily (index 1), Progress shifts from index 2 → 1
- **Navigation state**: May need to verify selectedItemIndex logic handles 2-item list correctly
- **No code deletion**: ALL ViewModels, Screens, DAOs, Routes remain in codebase (just inaccessible via UI)

### Files Preserved (Untouched)
- `/app/src/main/java/com/voxplanapp/ui/daily/DailyScreen.kt` - KEPT
- `/app/src/main/java/com/voxplanapp/ui/daily/DailyViewModel.kt` - KEPT
- `/app/src/main/java/com/voxplanapp/ui/calendar/DaySchedule.kt` - KEPT
- `/app/src/main/java/com/voxplanapp/ui/calendar/SchedulerViewModel.kt` - KEPT
- `/app/src/main/java/com/voxplanapp/navigation/VoxPlanNavHost.kt` - KEPT (routes remain)
- `/app/src/main/java/com/voxplanapp/data/Event.kt` - KEPT
- `/app/src/main/java/com/voxplanapp/data/EventDao.kt` - KEPT
- `/app/src/main/java/com/voxplanapp/data/EventRepository.kt` - KEPT

### Integration Points
- **Bottom navigation bar**: Uses `items` list to render nav items (VoxPlanApp.kt:279-290)
- **Navigation state**: `selectedItemIndex` in NavigationViewModel may need verification
- **Routes**: All Daily/Schedule routes remain in NavHost (user just can't navigate to them via UI)

### Validation
- **Build**: `./gradlew assembleDebug` must succeed
- **Manual test**: App launches with only "Goals" and "Progress" in bottom nav
- **Navigation test**: Can switch between Goals and Progress successfully
- **No crashes**: No runtime errors from reduced nav item count

---

## Implementation Steps

### Step 1: Comment Out Daily Nav Item
**File**: `/app/src/main/java/com/voxplanapp/navigation/VoxPlanApp.kt`
**Lines**: 251-256

**Current**:
```kotlin
private val items = listOf(
    BottomNavigationItem(
        title = "Goals",
        selectedIcon = Icons.Filled.List,
        unselectedIcon = Icons.Outlined.List,
        route = VoxPlanScreen.Main.route
    ),
    BottomNavigationItem(
        title = "Daily",
        selectedIcon = Icons.Filled.Today,
        unselectedIcon = Icons.Outlined.Today,
        route = VoxPlanScreen.Daily.createRouteWithDate()
    ),
    BottomNavigationItem(
        title = "Progress",
        selectedIcon = Icons.Filled.Timeline,
        unselectedIcon = Icons.Outlined.Timeline,
        route = VoxPlanScreen.Progress.route
    ),
    BottomNavigationItem(
        title = "Schedule",
        selectedIcon = Icons.Filled.DateRange,
        unselectedIcon = Icons.Outlined.DateRange,
        route = VoxPlanScreen.DaySchedule.createRouteWithDate()
    )
)
```

**After** (Comment out Daily):
```kotlin
private val items = listOf(
    BottomNavigationItem(
        title = "Goals",
        selectedIcon = Icons.Filled.List,
        unselectedIcon = Icons.Outlined.List,
        route = VoxPlanScreen.Main.route
    ),
    // HIDDEN FOR MVP - Re-enable post-launch
    // BottomNavigationItem(
    //     title = "Daily",
    //     selectedIcon = Icons.Filled.Today,
    //     unselectedIcon = Icons.Outlined.Today,
    //     route = VoxPlanScreen.Daily.createRouteWithDate()
    // ),
    BottomNavigationItem(
        title = "Progress",
        selectedIcon = Icons.Filled.Timeline,
        unselectedIcon = Icons.Outlined.Timeline,
        route = VoxPlanScreen.Progress.route
    ),
    BottomNavigationItem(
        title = "Schedule",
        selectedIcon = Icons.Filled.DateRange,
        unselectedIcon = Icons.Outlined.DateRange,
        route = VoxPlanScreen.DaySchedule.createRouteWithDate()
    )
)
```

### Step 2: Comment Out Schedule Nav Item
**File**: `/app/src/main/java/com/voxplanapp/navigation/VoxPlanApp.kt`
**Lines**: 263-268

**After** (Comment out Schedule):
```kotlin
private val items = listOf(
    BottomNavigationItem(
        title = "Goals",
        selectedIcon = Icons.Filled.List,
        unselectedIcon = Icons.Outlined.List,
        route = VoxPlanScreen.Main.route
    ),
    // HIDDEN FOR MVP - Re-enable post-launch
    // BottomNavigationItem(
    //     title = "Daily",
    //     selectedIcon = Icons.Filled.Today,
    //     unselectedIcon = Icons.Outlined.Today,
    //     route = VoxPlanScreen.Daily.createRouteWithDate()
    // ),
    BottomNavigationItem(
        title = "Progress",
        selectedIcon = Icons.Filled.Timeline,
        unselectedIcon = Icons.Outlined.Timeline,
        route = VoxPlanScreen.Progress.route
    )
    // HIDDEN FOR MVP - Re-enable post-launch
    // BottomNavigationItem(
    //     title = "Schedule",
    //     selectedIcon = Icons.Filled.DateRange,
    //     unselectedIcon = Icons.Outlined.DateRange,
    //     route = VoxPlanScreen.DaySchedule.createRouteWithDate()
    // )
)
```

### Step 3: Build and Verify
**Command**: `./gradlew assembleDebug`

**Expected**:
- Build succeeds with no compilation errors
- Only warnings (if any) should be unused imports (acceptable)

### Step 4: Manual Testing
**Test 1 - App Launch**:
1. Install APK on device/emulator
2. Launch app
3. Verify bottom navigation shows only 2 items: "Goals" and "Progress"
4. Verify no "Daily" or "Schedule" tabs visible

**Test 2 - Navigation**:
1. Tap "Progress" → Should navigate to Progress screen
2. Tap "Goals" → Should navigate back to Main/Goals screen
3. Verify selected state updates correctly (highlight moves between items)

**Test 3 - Core Flow**:
1. From Goals screen, create a goal with quota
2. Enter FocusMode from goal
3. Bank time
4. Exit to Goals
5. Navigate to Progress → Verify banked time appears
6. No crashes throughout flow

---

## Success Definition

### Functional Requirements
- [ ] Build succeeds (`./gradlew assembleDebug` completes)
- [ ] Bottom navigation shows only 2 items (Goals, Progress)
- [ ] Can navigate between Goals and Progress
- [ ] Selected item indicator works correctly
- [ ] No Daily or Schedule tabs visible
- [ ] All code preserved (no files deleted)

### Validation Commands
```bash
# Build verification
./gradlew clean
./gradlew assembleDebug

# Expected: BUILD SUCCESSFUL
```

### Expected Outcomes
1. **UI**: Bottom nav has 2 items instead of 4
2. **Navigation**: Switching between Goals/Progress works smoothly
3. **Code**: All Daily/Schedule ViewModels, Screens, DAOs remain in codebase
4. **Routes**: Daily/Schedule routes still exist in NavHost (just inaccessible)
5. **Future**: Uncommenting 2 blocks re-enables features instantly

---

## Re-Enablement Instructions (Post-MVP)

**To restore Dailies and Schedule features**:

1. Open `/app/src/main/java/com/voxplanapp/navigation/VoxPlanApp.kt`
2. Uncomment lines 251-256 (Daily nav item)
3. Uncomment lines 263-268 (Schedule nav item)
4. Remove "HIDDEN FOR MVP" comments
5. Build and test

**Estimated time to re-enable**: 2 minutes

---

## Risk Assessment

**Risk Level**: Extremely Low

**Why**:
- No code deletion (100% reversible)
- Single file modification (VoxPlanApp.kt)
- Simple list modification (comment/uncomment)
- No database changes
- No architectural impact

**Mitigation**:
- All code preserved in codebase
- Can uncomment instantly if needed
- Build verification catches any issues
- Manual testing confirms navigation works

---

## Implementation Summary

**Files Modified**: 1
- `/app/src/main/java/com/voxplanapp/navigation/VoxPlanApp.kt`

**Lines Changed**: ~16 lines (adding comment markers)
- Lines 251-256: Comment out Daily
- Lines 263-268: Comment out Schedule

**Net Impact**:
- Bottom nav: 4 items → 2 items
- UI: Simpler, cleaner for MVP
- Code: 100% preserved for future

**Estimated Time**: 5 minutes (implementation + build verification)

---

## Notes

- This approach is superior to deletion because:
  - Zero risk of breaking anything
  - Instant re-enablement (uncomment 2 blocks)
  - Preserves all work done on Dailies/Schedule
  - No database migrations needed
  - Can gather user feedback on core features first

- Alternative considered:
  - Feature flag (`const val ENABLE_SCHEDULING = false`)
  - More code, same outcome
  - Comment approach is simpler for this use case
