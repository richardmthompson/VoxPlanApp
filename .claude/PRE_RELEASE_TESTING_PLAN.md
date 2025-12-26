# VoxPlan MVP Pre-Release Testing Plan

**Target Release**: This Week (December 2025)
**Version**: 3.3-MVP
**Test Duration**: 2-3 days (focused testing)

---

## Critical Blockers (P0) - MUST FIX BEFORE ANY TESTING

### 1. Release Build Configuration
- [ ] Verify `isDebuggable = false` in release build type
- [ ] Remove all `Log.d/v/i()` calls (or use ProGuard rules)
- [ ] Test ProGuard/R8 doesn't break functionality

---

## Testing Strategy Overview

### Phase 1: Core Functionality (Day 1) - 1.5 hours
Focus: Verify all MVP features work (Goals, FocusMode, Progress)

### Phase 2: Database & State (Day 2) - 2 hours
Focus: Data persistence, Room migrations, app lifecycle

### Phase 3: Configurations & Accessibility (Day 3) - 2 hours
Focus: Dark mode, orientations, TalkBack, edge cases

### Phase 4: Release Build & Final (Day 4) - 1 hour
Focus: Release APK testing, final checklist

**Total Testing Time**: ~6.5 hours across 3-4 days

**Note**: Dailies and Schedule features are hidden for MVP, so testing is simplified to core features only.

---

## Test Devices & Configurations

### Minimum Required: 1 Physical Device
**Recommended Primary Device**:
- Android phone, API 31-34 (Android 12-14)
- 1080x1920 (xxhdpi) or higher
- 2GB+ RAM

### Configuration Matrix (Test on Primary Device)
- [ ] Portrait orientation
- [ ] Landscape orientation (critical screens only)
- [ ] Light mode
- [ ] Dark mode
- [ ] Default font size
- [ ] Large font size (Settings → Display → Font size → Large)

### Optional: Second Device (if available)
- Older device: Android 8.1 (API 27) - minimum SDK
- Different screen density/size
- Tablet (if targeting tablets)

---

## Phase 1: Core Functionality Testing (Day 1)

### Test Environment Setup
- [ ] Fresh install (uninstall any previous versions)
- [ ] Enable "Don't keep activities" in Developer Options (test state restoration)
- [ ] Monitor logcat for errors: `adb logcat | grep -E "FATAL|ERROR|AndroidRuntime"`

### 1.1 Goals Management (30 min)

**Test Case 1.1.1: Create First Goal**
- [ ] Launch app (should show empty state)
- [ ] Tap "+" FAB or create button
- [ ] Enter goal title "Test Goal 1"
- [ ] Save goal
- [ ] **Expected**: Goal appears in list, no crashes

**Test Case 1.1.2: Goal Hierarchy (3 levels)**
- [ ] Create parent goal "Work"
- [ ] Tap "Work" to navigate into it
- [ ] Create subgoal "Programming"
- [ ] Tap "Programming" to navigate into it
- [ ] Create sub-subgoal "Kotlin"
- [ ] **Expected**: Breadcrumb trail shows: Root → Work → Programming
- [ ] Navigate back via breadcrumbs
- [ ] **Expected**: Breadcrumb navigation works correctly

**Test Case 1.1.3: Goal Editing & Deletion**
- [ ] Long-press or tap edit on a goal
- [ ] Change title, save
- [ ] **Expected**: Title updates in list
- [ ] Delete goal
- [ ] **Expected**: Goal removed, no orphaned subgoals

**Test Case 1.1.4: Goal Reordering**
- [ ] Create 3 goals at same level
- [ ] Tap ActionMode button (vertical up/down)
- [ ] Select a goal to move
- [ ] **Expected**: Goal moves up/down in list
- [ ] Exit ActionMode
- [ ] **Expected**: Order persists after app restart

### 1.2 Daily Quotas (15 min)

**Test Case 1.2.1: Set Quota on Goal**
- [ ] Edit a goal
- [ ] Set quota: 60 minutes
- [ ] Select active days: Mon-Fri (5 days)
- [ ] Save
- [ ] **Expected**: Quota indicator appears on goal

**Test Case 1.2.2: Quota Inheritance**
- [ ] Create parent goal with 120-minute quota
- [ ] Create subgoal (no quota set)
- [ ] Enter Focus Mode for subgoal
- [ ] **Expected**: Power bar shows parent's quota (120 min)

### 1.3 Focus Mode (45 min)

**Test Case 1.3.1: Basic Focus Session**
- [ ] From Goals screen, tap a goal to enter Focus Mode
- [ ] **Expected**: Timer starts at 00:00:00, power bar visible (if quota set)
- [ ] Let timer run for 30 seconds
- [ ] Pause timer
- [ ] **Expected**: Timer pauses
- [ ] Resume timer
- [ ] **Expected**: Timer resumes from pause point
- [ ] Exit Focus Mode (back button)
- [ ] **Expected**: Returns to Goals screen

**Test Case 1.3.2: Medal System**
- [ ] Start Focus Mode
- [ ] Let timer run (or manually advance time in ViewModel for testing)
- [ ] Accumulate 30+ minutes
- [ ] **Expected**: Bronze medal awarded (sound plays)
- [ ] Continue to 60 minutes
- [ ] **Expected**: Silver medal awarded
- [ ] Bank medals to vault
- [ ] **Expected**: Medals cleared, "cha-ching" sound plays

**Test Case 1.3.3: Discrete Tasks**
- [ ] In Focus Mode, tap "+" to add discrete task
- [ ] Enter task name "Write unit test"
- [ ] Mark task as complete
- [ ] **Expected**: Task appears with checkmark
- [ ] Exit Focus Mode
- [ ] Re-enter Focus Mode for same goal
- [ ] **Expected**: Discrete tasks from previous session NOT shown (fresh session)

**Test Case 1.3.4: Time Banking**
- [ ] Complete focus session with 30+ minutes
- [ ] Bank time
- [ ] Exit Focus Mode
- [ ] Navigate to Progress screen
- [ ] **Expected**: Banked time appears in TimeBank for today's date

**Test Case 1.3.5: Pomodoro Mode**
- [ ] Toggle Pomodoro mode ON
- [ ] Set work duration: 5 minutes
- [ ] Set rest duration: 1 minute
- [ ] Start timer
- [ ] **Expected**: After 5 min, rest period starts (visual/audio cue)
- [ ] **Expected**: After 1 min rest, next work period starts

**Test Case 1.3.6: Quota Progress Bar**
- [ ] Focus on goal with 60-minute quota
- [ ] Bank 30 minutes
- [ ] **Expected**: Power bar shows ~50% filled
- [ ] Bank another 30 minutes
- [ ] **Expected**: Power bar shows ~100% filled

### 1.4 Progress Tracking (15 min)

**Test Case 1.4.1: Weekly View**
- [ ] Navigate to Progress screen
- [ ] **Expected**: Shows current week (Mon-Sun)
- [ ] **Expected**: Goals with quotas listed
- [ ] **Expected**: Time bars show logged time vs quota

**Test Case 1.4.2: Week Navigation**
- [ ] Tap "Previous Week" button
- [ ] **Expected**: Shows previous week's data
- [ ] Tap "Next Week" button
- [ ] **Expected**: Returns to current week

**Test Case 1.4.3: No Data State**
- [ ] Create new goal with quota, no time logged
- [ ] **Expected**: Progress bar empty (0%)
- [ ] Log time via Focus Mode
- [ ] Return to Progress
- [ ] **Expected**: Progress bar updates

### 1.5 Navigation (10 min)

**Test Case 1.5.1: Bottom Navigation**
- [ ] Tap "Goals" tab
- [ ] **Expected**: MainScreen displays
- [ ] Tap "Progress" tab
- [ ] **Expected**: ProgressScreen displays
- [ ] Tap "Goals" again
- [ ] **Expected**: Returns to MainScreen (state preserved)

**Test Case 1.5.2: Deep Navigation**
- [ ] Navigate: Goals → Edit Goal → Back
- [ ] **Expected**: Returns to Goals
- [ ] Navigate: Goals → Goal (level 2) → Goal (level 3) → Back button
- [ ] **Expected**: Navigates back through hierarchy correctly

---

## Phase 2: Database & State Testing (Day 2)

### 2.1 Data Persistence (30 min)

**Test Case 2.1.1: App Restart**
- [ ] Create 3 goals with quotas
- [ ] Log time in Focus Mode
- [ ] Force stop app: `adb shell am force-stop com.voxplanapp`
- [ ] Relaunch app
- [ ] **Expected**: All goals, quotas, and logged time persisted

**Test Case 2.1.2: Process Death Simulation**
- [ ] Enable "Don't keep activities" in Developer Options
- [ ] Navigate deep into app (Goals → Subgoal → Edit)
- [ ] Press Home button
- [ ] Wait 10 seconds
- [ ] Return to app
- [ ] **Expected**: App restores state gracefully (no crash)

**Test Case 2.1.3: Foreign Key Cascades**
- [ ] Create goal with quota
- [ ] Delete goal
- [ ] Check database: `adb shell "run-as com.voxplanapp sqlite3 /data/data/com.voxplanapp/databases/voxplan_database 'SELECT * FROM Quota;'"`
- [ ] **Expected**: Quota for deleted goal also deleted (cascade)

### 2.2 Room Database (30 min)

**Test Case 2.2.1: Fresh Install (v13)**
- [ ] Uninstall app completely
- [ ] Install release APK
- [ ] Create test data
- [ ] **Expected**: No migration errors, database v13 created

**Test Case 2.2.2: Migration Testing (CRITICAL)**
*Note: This requires having v12 APK to test migration path*
- [ ] If v12 APK available, install it
- [ ] Create test data in v12
- [ ] Install v13 APK (upgrade)
- [ ] Launch app
- [ ] **Expected**: No "Migration didn't handle" errors
- [ ] Verify all data intact

**Automated Migration Test** (recommended):
```kotlin
// Add to androidTest/
@RunWith(AndroidJUnit4::class)
class MigrationTest {
    @get:Rule
    val helper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        AppDatabase::class.java
    )

    @Test
    fun migrate12To13() {
        // Test migration path
        helper.createDatabase(TEST_DB, 12).apply {
            // Insert v12 data
            close()
        }
        helper.runMigrationsAndValidate(TEST_DB, 13, true, MIGRATION_12_13)
    }
}
```

### 2.3 Concurrency & Edge Cases (20 min)

**Test Case 2.3.1: Rapid Actions**
- [ ] Rapidly create 10 goals (tap Create → Save repeatedly)
- [ ] **Expected**: All goals saved, no duplicates or crashes
- [ ] Rapidly delete 5 goals
- [ ] **Expected**: All deleted correctly, no orphaned data

**Test Case 2.3.2: Long Titles & Special Characters**
- [ ] Create goal with 200-character title
- [ ] **Expected**: Title truncated or scrollable, no layout break
- [ ] Create goal with emoji: "💪 Workout Goals 🏋️"
- [ ] **Expected**: Emojis display correctly
- [ ] Create goal with special characters: "C++ & C# & <HTML>"
- [ ] **Expected**: Characters display correctly (not escaped)

**Test Case 2.3.3: Large Data Sets**
- [ ] Create 50 goals (can use script or automated test)
- [ ] Navigate list
- [ ] **Expected**: Smooth scrolling, no lag
- [ ] Search/filter (if implemented)
- [ ] **Expected**: Responsive performance

---

## Phase 3: Configurations & Accessibility (Day 3)

### 3.1 Theme & Orientation (30 min)

**Test Case 3.1.1: Dark Mode**
- [ ] System Settings → Display → Dark theme ON
- [ ] Launch app
- [ ] **Expected**: App uses dark theme colors
- [ ] Navigate all screens (Goals, Progress, FocusMode, Edit)
- [ ] **Expected**: All text readable, no white-on-white or black-on-black

**Test Case 3.1.2: Light Mode**
- [ ] Switch to Light theme
- [ ] Verify all screens readable

**Test Case 3.1.3: Landscape Orientation**
- [ ] Rotate device to landscape
- [ ] Test critical screens: Goals, FocusMode, Progress
- [ ] **Expected**: Layouts adapt gracefully (no clipping or overlap)
- [ ] Start Focus Mode in portrait, rotate to landscape
- [ ] **Expected**: Timer continues, no state loss

### 3.2 Accessibility (TalkBack) (45 min)

**Setup TalkBack**:
- [ ] Settings → Accessibility → TalkBack → Enable
- [ ] Tutorial: Learn gestures (swipe right to move, double-tap to activate)

**Test Case 3.2.1: Navigation with TalkBack**
- [ ] Launch app with TalkBack on
- [ ] Swipe through all elements on MainScreen
- [ ] **Expected**: All interactive elements announced
- [ ] Double-tap to activate goal
- [ ] **Expected**: Navigates correctly

**Test Case 3.2.2: Content Descriptions**
- [ ] Check critical elements have descriptions:
  - [ ] FAB button (e.g., "Create new goal")
  - [ ] Edit/Delete icons (e.g., "Edit goal", "Delete goal")
  - [ ] Bottom nav tabs (e.g., "Goals tab", "Progress tab")
  - [ ] Focus Mode timer controls (e.g., "Pause timer", "Resume timer")
- [ ] **Expected**: No "Unlabeled button" or "Image 1" announcements

**Test Case 3.2.3: Focus Mode with TalkBack**
- [ ] Enter Focus Mode with TalkBack on
- [ ] Swipe to timer
- [ ] **Expected**: Timer value announced (e.g., "15 minutes 30 seconds")
- [ ] Double-tap pause button
- [ ] **Expected**: "Timer paused" announced

**Fix Common Issues**:
```kotlin
// Add contentDescription to icons
Icon(
    imageVector = Icons.Default.Add,
    contentDescription = "Create new goal"
)

// Add semantics to custom composables
Text("00:15:30", modifier = Modifier.semantics {
    contentDescription = "Timer: 15 minutes 30 seconds"
})
```

### 3.3 Font Sizes & Display Sizes (15 min)

**Test Case 3.3.1: Large Font**
- [ ] Settings → Display → Font size → Largest
- [ ] Relaunch app
- [ ] **Expected**: All text scales appropriately
- [ ] **Expected**: No text truncated or buttons overlapping

**Test Case 3.3.2: Small Display Size**
- [ ] Settings → Display → Display size → Smallest
- [ ] **Expected**: UI still usable, touch targets ≥ 48dp

### 3.4 System Interruptions (20 min)

**Test Case 3.4.1: Phone Call During Focus Mode**
- [ ] Start Focus Mode
- [ ] Simulate incoming call (or use `adb shell am start -a android.intent.action.CALL`)
- [ ] Answer call, hang up
- [ ] **Expected**: Timer pauses during call, resumes after (or stays paused)
- [ ] **Expected**: No data loss, app resumes gracefully

**Test Case 3.4.2: Low Battery / Doze Mode**
- [ ] Start Focus Mode
- [ ] Let device enter Doze mode (screen off for 30+ min) OR simulate: `adb shell dumpsys deviceidle force-idle`
- [ ] Wake device
- [ ] **Expected**: Timer state preserved (or gracefully handles disruption)

**Test Case 3.4.3: App Switching**
- [ ] Start Focus Mode
- [ ] Switch to another app (YouTube, Browser)
- [ ] Use other app for 2 minutes
- [ ] Return to VoxPlan
- [ ] **Expected**: Focus Mode state preserved, timer continues or resumes

---

## Phase 4: Release Build & Final Checks (Day 4)

### 4.1 Build Release APK (15 min)

```bash
# Generate signed release APK
./gradlew assembleRelease

# Or generate AAB for Play Store
./gradlew bundleRelease

# Verify ProGuard/R8 not breaking app
# Install: adb install app/build/outputs/apk/release/app-release.apk
```

**Test Case 4.1.1: Release Build Smoke Test**
- [ ] Install release APK on clean device
- [ ] Run core flow: Create goal → FocusMode → Bank time → Progress
- [ ] **Expected**: No crashes, features work identically to debug build

**Test Case 4.1.2: ProGuard Verification**
- [ ] Check no reflection-based crashes
- [ ] Room queries work (common ProGuard issue)
- [ ] Navigation arguments parsed correctly

### 4.2 Performance Checks (20 min)

**Test Case 4.2.1: Memory Leaks** (if LeakCanary integrated)
- [ ] Navigate through all screens 5x
- [ ] **Expected**: LeakCanary reports no leaks

**Test Case 4.2.2: Battery Usage**
- [ ] Charge device to 100%
- [ ] Run 2-hour Focus Mode session (or simulate)
- [ ] Check battery drain: Settings → Battery
- [ ] **Expected**: < 10% battery drain for 2hr session

**Test Case 4.2.3: Storage Usage**
- [ ] Create 100 goals, log 30 days of time
- [ ] Settings → Apps → VoxPlan → Storage
- [ ] **Expected**: < 50MB storage used

### 4.3 Lint & Code Quality (15 min)

```bash
# Run Lint
./gradlew lint

# Review report: app/build/reports/lint-results-debug.html
```

**Fix High-Priority Issues**:
- [ ] No hardcoded strings (use string resources)
- [ ] No missing translations (if targeting multiple languages)
- [ ] No security issues (e.g., exported components without permissions)
- [ ] No performance issues (e.g., nested layout depths > 10)

### 4.4 Final Pre-Upload Checklist

**Code Cleanup**:
- [ ] Remove all `Log.d/v/i()` calls (keep `Log.e()` for crashes)
- [ ] Remove all `TODO` comments or move to issue tracker
- [ ] Remove commented-out code (QuickScheduleScreen.kt)
- [ ] Set `isDebuggable = false` in release build

**App Metadata**:
- [ ] App name finalized in `strings.xml`
- [ ] Version name: "3.3-MVP" (or "1.0.0")
- [ ] Version code incremented

**Assets**:
- [ ] App icon present (mipmap-xxxhdpi, xxhdpi, xhdpi, hdpi, mdpi)
- [ ] Feature graphic (1024x500 for Play Store)
- [ ] Screenshots ready (2-8 images)

**Play Store Requirements**:
- [ ] Privacy policy URL ready
- [ ] Data Safety form completed
- [ ] App description drafted (short + full)
- [ ] Category selected (Productivity)
- [ ] Content rating questionnaire completed

---

## Critical Issues Found & Resolution

### Issue Tracker

| Issue | Severity | Status | Notes |
|-------|----------|--------|-------|
| QuickScheduleScreen.kt commented out | P3 | ⚠️ Known | Can delete, not affecting MVP |
| Deprecated icon warnings | P3 | ⚠️ Known | Cosmetic, low priority |
| Missing database index (QuotaEntity) | P2 | ⚠️ Known | Performance, not critical |
| DaySchedule delete dialog bug | P3 | ⚠️ Known | Not in MVP (feature hidden) |

---

## Test Results Template

**Date**: _____________
**Tester**: _____________
**Device**: _____________
**Android Version**: _____________
**Build**: debug / release

### Phase 1: Core Functionality
- [ ] Goals Management: PASS / FAIL
- [ ] Daily Quotas: PASS / FAIL
- [ ] Focus Mode: PASS / FAIL
- [ ] Progress Tracking: PASS / FAIL
- [ ] Navigation: PASS / FAIL

### Phase 2: Database & State
- [ ] Data Persistence: PASS / FAIL
- [ ] Room Database: PASS / FAIL
- [ ] Concurrency: PASS / FAIL

### Phase 3: Configurations & Accessibility
- [ ] Dark Mode: PASS / FAIL
- [ ] Landscape: PASS / FAIL
- [ ] TalkBack: PASS / FAIL
- [ ] Font Sizes: PASS / FAIL
- [ ] System Interruptions: PASS / FAIL

### Phase 4: Release Build
- [ ] Release APK: PASS / FAIL
- [ ] Performance: PASS / FAIL
- [ ] Lint: PASS / FAIL

**Overall Status**: PASS / FAIL / CONDITIONAL PASS
**Blocker Issues**: _____________
**Notes**: _____________

---

## Post-Testing: Play Store Upload

### Internal Testing Track (First Upload)
1. **Build AAB**: `./gradlew bundleRelease`
2. **Upload to Play Console**: Internal Testing track
3. **Add testers**: Up to 100 internal testers
4. **Wait**: 24-48 hours for pre-launch report
5. **Review**: Check crash rate, ANR rate, accessibility issues

### Pre-Launch Report Expectations
- **Crash rate**: Target < 1%, acceptable < 1.09%
- **ANR rate**: Target < 0.5%, acceptable < 0.47%
- **Accessibility**: Minor issues acceptable, major issues block release
- **Security**: No vulnerabilities

### Closed Testing (If New Account - Required)
- **Minimum**: 12+ testers for 14+ days
- **Purpose**: Google validates app quality before production
- **Checklist**:
  - [ ] Recruit 12 testers (friends, family, beta community)
  - [ ] Monitor crash/ANR rates daily
  - [ ] Fix critical bugs, upload new builds as needed
  - [ ] Complete 14 days with acceptable metrics

### Production Release (Staged Rollout)
- **Week 1**: 5% rollout
- **Week 2**: 20% rollout (if no issues)
- **Week 3**: 50% rollout (if metrics stable)
- **Week 4**: 100% rollout

---

## Resources

**Official Android Testing Docs**:
- [Test your Compose layout](https://developer.android.com/develop/ui/compose/testing)
- [Testing Room migrations](https://medium.com/androiddevelopers/testing-room-migrations-be93cdb0d975)
- [Pre-launch reports](https://play.google.com/console/about/pre-launchreports/)
- [App testing requirements](https://support.google.com/googleplay/android-developer/answer/14151465)

**Tools**:
- **LeakCanary**: https://square.github.io/leakcanary/
- **Accessibility Scanner**: https://play.google.com/store/apps/details?id=com.google.android.apps.accessibility.auditor
- **Firebase Test Lab**: https://firebase.google.com/docs/test-lab

**ADB Commands Reference**:
```bash
# Force stop app
adb shell am force-stop com.voxplanapp

# Clear app data
adb shell pm clear com.voxplanapp

# Simulate Doze mode
adb shell dumpsys deviceidle force-idle

# Monitor memory
adb shell dumpsys meminfo com.voxplanapp

# Check for ANRs
adb logcat | grep "ANR in"

# Take screenshot
adb shell screencap -p /sdcard/screenshot.png
adb pull /sdcard/screenshot.png
```

---

**Last Updated**: 2025-12-22
**Next Review**: After Phase 1 testing complete
