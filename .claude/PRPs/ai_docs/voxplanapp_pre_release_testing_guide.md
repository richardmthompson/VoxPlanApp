# VoxPlanApp Pre-Release Testing Guide
## Google Play Store Submission Testing Plan

**App Context**: VoxPlanApp is an Android productivity app with hierarchical goal management, time tracking, daily quotas, scheduling, and gamified focus sessions.

**Tech Stack**:
- Jetpack Compose (UI)
- MVVM architecture with manual DI
- Room database (version 13 with migrations)
- Kotlin Flow/StateFlow
- Target SDK 34, Min SDK 27

---

## 1. GOOGLE PLAY STORE REQUIREMENTS

### 1.1 Mandatory Requirements Checklist

#### API Level Compliance
- ✅ **Target SDK**: Must be API level 34 (Android 14) for existing apps
- ✅ **Min SDK**: 27 (Android 8.1) - Verify compatibility with older devices
- 📅 **2025 Update**: New apps will need to target API level 35 starting August 31, 2025

#### Pre-Launch Report Setup
- ✅ Upload APK/Bundle to closed or open testing track
- ✅ Provide test account credentials if login required (VoxPlanApp: likely not needed)
- ⚠️ **Critical**: Pre-launch reports test on Android 9+ devices only
- ✅ Pre-launch report includes: stability, performance, privacy tests

#### Testing Requirements for New Personal Accounts
⚠️ **Only applies to personal accounts created after November 13, 2023**:
- Run closed test with minimum 12 testers
- Testers must be opted-in for at least 14 days continuously
- Organization accounts are exempt from this requirement

### 1.2 Privacy & Security Requirements

#### Privacy Policy
- ✅ **Required**: Valid, accessible privacy policy URL
- ✅ Must accurately describe data collection practices
- ✅ No broken or misleading links
- 📝 **For VoxPlanApp**: Declare local database storage, no cloud sync (if applicable)

#### Data Safety Disclosures
- ✅ Complete all Data Safety sections in Play Console
- ✅ Accurately describe what data is collected and stored
- ✅ **For VoxPlanApp**: Goals, time tracking, focus session data (all local)

#### User Account Deletion
- ⚠️ **If app has user authentication**: Must provide account deletion mechanism
- ✅ **For VoxPlanApp**: No user authentication = not required (verify this assumption)

#### Permissions
- ✅ Request only necessary permissions
- ✅ Verify all `<uses-permission>` entries in AndroidManifest.xml
- 📝 **VoxPlanApp Permissions to Review**:
  - Notifications (for timers/reminders)
  - Media playback (ExoPlayer for sound effects)
  - No network, location, or storage permissions needed

### 1.3 App Listing Requirements

- ✅ Complete, accurate app description (no placeholders)
- ✅ High-quality screenshots (minimum 2, recommended 4-8)
- ✅ Proper content rating (fill questionnaire honestly)
- ✅ Original icon and graphics (no copyright infringement)
- ✅ Accurate feature graphic (1024x500px)

### 1.4 Technical Performance Thresholds

Google Play downranks apps that exceed:
- ❌ **Crash Rate**: > 1.09%
- ❌ **ANR Rate**: > 0.47%

**Goal**: Achieve < 0.5% crash rate and < 0.2% ANR rate

---

## 2. COMPREHENSIVE TESTING CATEGORIES

### 2.1 Functional Testing (Core Features)

Test all features against user requirements and design specifications.

#### Goals Management Testing
| Test Scenario | Steps | Expected Result | Priority |
|--------------|-------|-----------------|----------|
| Create root-level goal | Navigate to Main > Add Goal | Goal appears in list | P0 |
| Create subgoal (depth 1) | Tap goal > Add Subgoal | Subgoal appears nested | P0 |
| Create subgoal (depth 2) | Tap subgoal > Add Subgoal | Subgoal appears nested | P0 |
| Test depth limit (depth 3) | Try to add subgoal to depth-2 goal | Should reach max depth (verify) | P1 |
| Edit goal title | Long-press > Edit > Change title | Title updates in hierarchy | P0 |
| Delete goal | Long-press > Delete | Goal removed, children cascade deleted | P0 |
| Reorder vertically | ActionMode > VerticalUp/Down > Select goal | Goal moves in list | P1 |
| Reorder hierarchically | ActionMode > HierarchyUp/Down > Select goal | Goal changes parent | P1 |
| Navigate breadcrumbs | Tap subgoal > Tap breadcrumb | Navigation works correctly | P0 |
| Quota settings | Edit goal > Set quota days/minutes | Quota saved and displayed | P0 |

#### Focus Mode Testing
| Test Scenario | Steps | Expected Result | Priority |
|--------------|-------|-----------------|----------|
| Start focus session (goal) | Main > Goal > Focus Mode | Timer starts, UI displays correctly | P0 |
| Start focus session (event) | Daily/Schedule > Event > Focus Mode | Timer starts with event details | P0 |
| Pause timer | Tap pause button | Timer pauses, state preserved | P0 |
| Resume timer | Tap resume button | Timer continues from paused time | P0 |
| Complete 30-min session | Run timer for 30 mins | Bronze medal awarded | P1 |
| Complete 60-min session | Run timer for 60 mins | Silver medal awarded | P1 |
| Complete 90-min session | Run timer for 90 mins | Gold medal awarded | P1 |
| Complete 120-min session | Run timer for 120+ mins | Diamond medal awarded | P1 |
| Add discrete task | Tap "Add Task" > Enter title | Task appears in list | P1 |
| Complete discrete task | Check task checkbox | 15 points awarded, task marked done | P1 |
| Bank time | Accrue time > Tap "Bank Time" | Event created with scheduled duration | P1 |
| Timer persistence | Start timer > Exit app > Return | Timer continues running | P0 |
| Sound effects | Complete session | Sound plays via ExoPlayer | P2 |

#### Daily Tasks Testing
| Test Scenario | Steps | Expected Result | Priority |
|--------------|-------|-----------------|----------|
| View daily tasks | Navigate to Daily tab | Shows parent dailies for today | P0 |
| Create daily manually | Add Daily > Select goal > Set title | Daily appears in list | P0 |
| Add quota tasks | Tap "Add Quota Tasks" | Dailies created for active quota goals | P0 |
| Reorder dailies | ActionMode > VerticalUp/Down > Select | Order changes | P1 |
| Delete daily only | Delete dialog > "Delete child only" | Daily deleted, scheduled events remain | P0 |
| Delete with scheduled | Delete dialog > "Delete with parent" | Daily + scheduled events deleted | P0 |
| Navigate to focus mode | Tap daily > Focus Mode | Focus mode starts for goal | P0 |
| **⚠️ KNOWN BUG** | Delete dialog crashes | **FIX REQUIRED**: DaySchedule.kt lines 110-126 | P0 |

#### Scheduling Testing
| Test Scenario | Steps | Expected Result | Priority |
|--------------|-------|-----------------|----------|
| View day schedule | Navigate to Schedule tab | Shows hourly grid for today | P0 |
| Change date | Tap date picker > Select date | Schedule updates for new date | P0 |
| Drag daily to schedule | Long-press daily > Drag to time slot | Scheduled event created with start/end times | P0 |
| Edit scheduled event | Tap event > Edit times | Times update, event repositions | P0 |
| Delete scheduled event | Tap event > Delete | Event removed from schedule | P0 |
| Navigate to focus mode | Tap event > Focus Mode | Focus mode starts with event details | P0 |
| Scroll to 6 AM | Open schedule | Default scroll position is 6 AM | P2 |
| **❌ INCOMPLETE** | Create event from empty slot | **NOT IMPLEMENTED** | P2 |
| **❌ INCOMPLETE** | Week/month view | **NOT IMPLEMENTED** | P3 |

#### Progress Tracking Testing
| Test Scenario | Steps | Expected Result | Priority |
|--------------|-------|-----------------|----------|
| View weekly progress | Navigate to Progress tab | Shows current week quotas | P0 |
| Check quota fulfillment | Complete focus session | Progress bar updates for goal | P0 |
| Navigate weeks | Swipe left/right | Previous/next week data loads | P1 |
| View power bar calculation | Accrue 60 minutes | One full power bar displayed | P1 |
| Active days filtering | Check quota with specific active days | Only active days show quotas | P1 |

### 2.2 Database & Data Persistence Testing

#### Room Database Testing
| Test Scenario | Steps | Expected Result | Priority |
|--------------|-------|-----------------|----------|
| Fresh install | Install app > Create goals | Database created at version 13 | P0 |
| App restart persistence | Create data > Kill app > Restart | All data persists correctly | P0 |
| Transaction integrity | Reorder multiple goals | All updates atomic (all succeed or fail) | P0 |
| Foreign key cascades | Delete goal with quota | Quota auto-deleted (ON DELETE CASCADE) | P0 |
| Parent-child relationships | Create daily > Add scheduled event | `parentDailyId` correctly set | P0 |
| Time bank entries | Bank time from focus mode | TimeBank entry created with correct date/minutes | P0 |
| Quota active days encoding | Set quota "1111100" (Mon-Fri) | Only Mon-Fri show quotas | P0 |

#### Migration Testing (Critical for Updates)
⚠️ **HIGH PRIORITY**: Test all migration paths before releasing updates

| Migration Path | Test Approach | Priority |
|----------------|---------------|----------|
| Fresh install (v13) | Install new app > Create data | P0 |
| V12 → V13 | Restore v12 database > Update app | P0 |
| V11 → V13 | Restore v11 database > Update app | P1 |
| Earlier → V13 | Test all earlier versions if possible | P2 |

**Migration Test Checklist**:
- ✅ Schema changes apply correctly
- ✅ Existing data preserves integrity
- ✅ No data loss on upgrade
- ✅ Default values for new columns correct
- ✅ Foreign keys remain valid
- ✅ Indexes rebuild successfully

**Testing Tools**:
- Use `androidx.room:room-testing` artifact
- Use `MigrationTestHelper` for automated tests
- Export schemas to `app/schemas/` directory (already configured)

**Example Test**:
```kotlin
@Test
fun migrate12To13_preservesData() {
    helper.createDatabase(TEST_DB, 12).apply {
        // Insert test data for v12
        execSQL("INSERT INTO TodoItem VALUES (...)")
        close()
    }

    helper.runMigrationsAndValidate(TEST_DB, 13, true, MIGRATION_12_13)

    helper.getMigrationDatabase().use { db ->
        // Verify data integrity after migration
    }
}
```

### 2.3 UI/UX Testing

#### Navigation Testing
| Test Scenario | Steps | Expected Result | Priority |
|--------------|-------|-----------------|----------|
| Bottom nav transitions | Tap each tab | Correct screen loads, state preserved | P0 |
| Deep navigation | Navigate 3 levels deep in goals | Breadcrumb navigation works | P0 |
| Back button behavior | Navigate deep > Press back | Navigates up hierarchy | P0 |
| Screen rotation | Rotate device on each screen | Layout adapts, state persists | P1 |
| Multi-window mode | Split screen with another app | UI resizes correctly | P2 |

#### Compose UI Testing
**Test with semantics and accessibility in mind**:

```kotlin
@Test
fun goalList_displaysCorrectly() {
    composeTestRule.setContent {
        VoxPlanAppTheme {
            MainScreen(viewModel = mainViewModel)
        }
    }

    composeTestRule.onNodeWithText("My Goal").assertIsDisplayed()
    composeTestRule.onNodeWithContentDescription("Add Goal").performClick()
}
```

**Key Compose Testing Patterns**:
- Use `onNodeWithText()`, `onNodeWithContentDescription()` for finding elements
- Test user interactions: `performClick()`, `performTextInput()`, `performScrollTo()`
- Verify state: `assertIsDisplayed()`, `assertTextEquals()`, `assertExists()`
- Test recomposition: Change state, verify UI updates

#### Visual Testing Checklist
- ✅ All text readable at smallest system font size
- ✅ All text readable at largest system font size
- ✅ Touch targets minimum 48dp (Material Design requirement)
- ✅ Colors meet WCAG contrast ratio (4.5:1 for text)
- ✅ Dark mode theme displays correctly
- ✅ Light mode theme displays correctly
- ✅ No text truncation or overlap
- ✅ Loading states display appropriately
- ✅ Empty states have helpful messages

### 2.4 Accessibility Testing

#### TalkBack Testing (Critical for Play Store)
**Enable TalkBack**: Settings > Accessibility > TalkBack > On

| Test Scenario | Expected Result | Priority |
|--------------|-----------------|----------|
| Navigate goal list | Each goal announced with title | P0 |
| Add goal button | "Add goal" button announced | P0 |
| Focus mode timer | Timer value announced on change | P0 |
| Edit fields | Hints and labels announced | P0 |
| Icons without text | Content descriptions present | P0 |
| Navigation tabs | Tab names announced | P0 |

**Accessibility Checklist**:
- ✅ All interactive elements have content descriptions
- ✅ All images have meaningful content descriptions (or marked decorative)
- ✅ Text fields have input hints
- ✅ Focus order is logical (top to bottom, left to right)
- ✅ No redundant announcements
- ✅ Custom composables export semantics correctly
- ✅ Test with Accessibility Scanner app

**VoxPlanApp Specific Checks**:
- ✅ Timer announcements in Focus Mode
- ✅ Goal hierarchy navigation clear with TalkBack
- ✅ Medal awards announced
- ✅ Quota progress announced

**Tools**:
- Download [Accessibility Scanner](https://play.google.com/store/apps/details?id=com.google.android.apps.accessibility.auditor)
- Use Android Lint accessibility checks
- Follow WCAG 2.1 AA guidelines

### 2.5 Performance Testing

#### Memory Management
| Test Scenario | Tool/Method | Pass Criteria | Priority |
|--------------|-------------|---------------|----------|
| Memory leaks | LeakCanary integration | No leaks detected | P0 |
| Large goal lists (100+ items) | Monitor memory usage | < 100MB RAM usage | P1 |
| Long focus sessions (2+ hours) | Monitor memory over time | No memory growth | P1 |
| Rapid navigation | Navigate quickly between screens | No jank, smooth 60fps | P1 |

**Testing on Low-End Devices**:
- ⚠️ **Critical**: Test on devices with < 2GB RAM
- Test bitmap loading (if any large images used)
- Verify no OutOfMemoryErrors

**Memory Testing Tools**:
- Android Studio Profiler
- LeakCanary library
- `adb shell dumpsys meminfo com.voxplanapp`

#### ANR Prevention Testing
**ANR triggers when main thread blocked > 5 seconds**

| Risk Area | Test Approach | Mitigation | Priority |
|-----------|---------------|------------|----------|
| Database operations | Large batch inserts/updates | Ensure coroutines/background threads | P0 |
| Goal hierarchy processing | 100+ nested goals | Profile `processGoals()` function | P1 |
| Migration execution | Migrate large database | Test with 1000+ records | P1 |

**Common ANR Causes in VoxPlanApp**:
- ❌ Database queries on main thread (verify all use Flow/suspend)
- ❌ Heavy computation in `SharedViewModel.processGoals()`
- ❌ BroadcastReceiver `onReceive()` takes too long

**Testing Approach**:
```bash
# Simulate slow network (not applicable for VoxPlanApp)
# Simulate CPU throttling
adb shell "settings put global window_animation_scale 10"

# Monitor for ANRs
adb logcat | grep "ANR in"
```

#### Battery Efficiency
| Test Scenario | Expected Result | Priority |
|--------------|-----------------|----------|
| Focus mode running 2+ hours | < 5% battery drain per hour | P1 |
| Background timer (if applicable) | Minimal battery impact | P1 |
| Idle app in background | No battery drain | P0 |

**Battery Testing**:
- Use Android Studio Energy Profiler
- Monitor wakelocks: `adb shell dumpsys batterystats`
- Verify no excessive wake-ups

### 2.6 Edge Cases & Error Scenarios

#### App Lifecycle Edge Cases
| Test Scenario | Steps | Expected Result | Priority |
|--------------|-------|-----------------|----------|
| Phone call during focus mode | Start timer > Receive call > Return | Timer pauses and resumes | P0 |
| App killed by system | Start timer > Kill app from settings > Reopen | State restored (or clear message) | P0 |
| Battery saver mode | Enable battery saver > Use app | Graceful degradation | P1 |
| Low storage | Fill device storage > Create goals | Error message or success | P1 |
| Low memory | Simulate low memory > Navigate app | No crashes | P0 |
| Screen timeout during timer | Let screen turn off > Wake device | Timer continues | P0 |
| Lock screen during timer | Lock device > Unlock | Timer continues | P0 |

#### Data Integrity Edge Cases
| Test Scenario | Steps | Expected Result | Priority |
|--------------|-------|-----------------|----------|
| Delete parent with children | Delete goal with subgoals | All children deleted (verify) | P0 |
| Delete goal with active quota | Delete goal | Quota cascade deleted | P0 |
| Delete daily with scheduled events | Choose "delete both" option | Both deleted | P0 |
| Reorder during database update | Reorder while another operation running | No data corruption | P1 |
| Concurrent updates | Multiple rapid updates | Transactions handle correctly | P1 |

#### UI Edge Cases
| Test Scenario | Steps | Expected Result | Priority |
|--------------|-------|-----------------|----------|
| Very long goal titles (100+ chars) | Create goal with long title | Truncates or wraps gracefully | P1 |
| Special characters in titles | Use emoji, symbols | Displays correctly | P1 |
| Empty goal title | Try to save empty title | Validation prevents or defaults | P0 |
| 100+ goals in single level | Create many goals | Performance acceptable, no crashes | P1 |
| Date edge cases | Test Feb 29, year transitions | Dates handle correctly | P1 |

#### System Interruptions
| Test Scenario | Expected Result | Priority |
|--------------|-----------------|----------|
| Bluetooth headphone disconnect | Sound continues on speaker | P2 |
| Notification during focus | Timer continues, notification shown | P1 |
| System update prompt | App state preserved | P1 |
| Language change | UI updates to new language (if i18n) | P2 |

---

## 3. DEVICE & CONFIGURATION TESTING MATRIX

### 3.1 Recommended Device Configuration Matrix

#### Minimum Test Devices
Test on at least **2 physical devices**: 1 phone + 1 tablet

**Recommended Testing Matrix**:

| Device Type | Screen Size | Density | Android Version | Priority | Example Devices |
|-------------|-------------|---------|-----------------|----------|-----------------|
| **Phone (Small)** | 480x800 - 720x1280 | hdpi/xhdpi | Android 8.1 (API 27) | P0 | Pixel 3, Galaxy A10 |
| **Phone (Standard)** | 1080x1920 | xxhdpi | Android 12 (API 31) | P0 | Pixel 5, Galaxy S21 |
| **Phone (Large)** | 1440x2960 | xxxhdpi | Android 14 (API 34) | P0 | Pixel 8 Pro, Galaxy S24 Ultra |
| **Tablet (7-10")** | 1200x1920 | xhdpi | Android 11+ | P1 | Galaxy Tab S8, Pixel Tablet |
| **Foldable** | Variable | Various | Android 12+ | P2 | Galaxy Z Fold, Pixel Fold |

#### Screen Density Coverage
**Android uses 6 density buckets**: ldpi, mdpi, hdpi, xhdpi, xxhdpi, xxxhdpi

**VoxPlanApp Priority Coverage**:
- ✅ **xxhdpi** (480 dpi) - Most common (1080p phones)
- ✅ **xhdpi** (320 dpi) - Mid-range devices
- ✅ **xxxhdpi** (640 dpi) - High-end flagship phones
- ⚠️ hdpi (240 dpi) - Older/budget devices (test if possible)

**User Analytics Tip**: 60% of installations typically run on xhdpi, check your analytics to prioritize

### 3.2 Android Version Coverage

**Support Range**: API 27 (Android 8.1) to API 34 (Android 14)

**Priority Testing**:
- ✅ **API 27 (Android 8.1)** - Minimum supported (critical)
- ✅ **API 31 (Android 12)** - Material You, splash screen changes
- ✅ **API 33 (Android 13)** - Notification permission changes
- ✅ **API 34 (Android 14)** - Target SDK (critical)

**Testing Focus by Version**:
- **API 27-28**: Basic compatibility, older UI behaviors
- **API 29-30**: Scoped storage, dark theme
- **API 31-32**: Material You theming, predictive back
- **API 33-34**: Notification permissions, foreground service restrictions

### 3.3 Configuration Testing

#### Orientation Testing
- ✅ Portrait mode (primary)
- ✅ Landscape mode (all screens should adapt)
- ✅ Auto-rotate transitions (state preservation)

**VoxPlanApp Landscape Tests**:
- Main screen goal list (horizontal breadcrumbs?)
- Focus mode timer (landscape layout exists?)
- Day schedule (hourly grid in landscape)
- Goal edit form (keyboard + form visibility)

#### Theme Testing
- ✅ Light mode (system default)
- ✅ Dark mode (system default)
- ✅ Dynamic color (Material You, API 31+)
- ✅ Theme transitions (no flash/flicker)

#### Font Size Testing
**Settings > Display > Font Size**:
- ✅ Smallest (0.85x)
- ✅ Default (1.0x)
- ✅ Large (1.15x)
- ✅ Largest (1.3x)

**Verify**:
- No text truncation
- Touch targets remain accessible
- Layouts don't break

#### Display Size Testing
**Settings > Display > Display Size**:
- ✅ Smallest
- ✅ Default
- ✅ Large
- ✅ Largest

### 3.4 Emulator vs Physical Device Strategy

**Use Physical Devices For**:
- ✅ Performance testing (real hardware)
- ✅ Battery testing
- ✅ Touch interactions and gestures
- ✅ Accessibility testing (TalkBack)
- ✅ Camera, sensors, real network conditions

**Use Emulators For**:
- ✅ Testing multiple Android versions quickly
- ✅ Testing rare screen densities
- ✅ Automated UI testing
- ✅ Testing configurations you don't own physically

**Firebase Test Lab**: Access real devices in Google data center
- Run tests on 20+ physical devices
- Generate pre-launch report compatible results
- Cost-effective for comprehensive device coverage

---

## 4. PRE-SUBMISSION FINAL CHECKLIST

### 4.1 Code & Build Configuration

**Release Build Settings** (`app/build.gradle.kts`):
```kotlin
buildTypes {
    release {
        isMinifyEnabled = true
        isShrinkResources = true
        proguardFiles(
            getDefaultProguardFile("proguard-android-optimize.txt"),
            "proguard-rules.pro"
        )
        isDebuggable = false // CRITICAL: Must be false
    }
}
```

**Checklist**:
- ✅ `isDebuggable = false` in release build type
- ✅ Remove all `Log.d()`, `Log.v()`, `Log.i()` calls (keep `Log.e()` for crash reports)
- ✅ Remove all test/debug code and libraries from release
- ✅ ProGuard/R8 enabled for code shrinking
- ✅ Test release build thoroughly (shrinking can cause runtime issues)

**Version Code & Name**:
```kotlin
defaultConfig {
    versionCode = 1 // Increment for each release
    versionName = "1.0.0" // Semantic versioning
}
```

**Verify AndroidManifest.xml**:
- ✅ `android:icon` points to app icon
- ✅ `android:label` is app name
- ✅ Only necessary `<uses-permission>` entries
- ✅ No test/debug activities in manifest

### 4.2 Asset & Resource Cleanup

- ✅ Remove unused drawable resources
- ✅ Remove unused layout files
- ✅ Remove unused string resources
- ✅ Clean up `res/raw/` (sound files optimized?)
- ✅ No proprietary/test data in `assets/`
- ✅ Optimize image assets (use WebP where possible)

**Lint Check**:
```bash
./gradlew lint
# Review build/reports/lint-results.html
```

### 4.3 Signing & Security

**App Signing**:
- ✅ Generate release keystore (keep secure backup!)
- ✅ Key validity must extend beyond October 22, 2033
- ✅ Configure signing in `build.gradle.kts` or use Play App Signing
- ✅ **Never commit keystore to version control**

**Security Checklist**:
- ✅ No hardcoded API keys or secrets in code
- ✅ No test URLs pointing to development servers
- ✅ Certificate pinning (if network requests used)
- ✅ WebView debugging disabled (if using WebView)

### 4.4 Legal & Policy

- ✅ Privacy policy URL (even for local-only apps, recommended)
- ✅ EULA if desired (optional)
- ✅ No copyright/trademark infringement in:
  - App name
  - Icon
  - Screenshots
  - Description text
  - In-app content

### 4.5 Play Console Setup

**App Listing**:
- ✅ App title (max 50 characters)
- ✅ Short description (max 80 characters)
- ✅ Full description (max 4000 characters)
- ✅ App icon (512x512px PNG)
- ✅ Feature graphic (1024x500px JPG/PNG)
- ✅ Screenshots:
  - Minimum 2 per supported device type
  - Phone: 320-3840px (min dimension)
  - Tablet: Same as phone (if tablet support)
  - 16:9 or 9:16 aspect ratio recommended

**Content Rating**:
- ✅ Complete questionnaire honestly
- ✅ VoxPlanApp likely: "Everyone" or "Everyone 10+"

**Pricing & Distribution**:
- ✅ Select countries/regions
- ✅ Set price (free or paid)
- ✅ Opt-in to Google Play for Education (if appropriate)

**Data Safety**:
- ✅ Complete all data safety sections
- ✅ Declare data collection practices
- ✅ Declare data storage (local only for VoxPlanApp)
- ✅ Declare no data sharing with third parties (if true)

---

## 5. TESTING WORKFLOW & EXECUTION PLAN

### 5.1 Testing Phases

#### Phase 1: Internal Development Testing
**Duration**: Ongoing during development
**Who**: Developer
**Focus**: Feature functionality, basic stability

- Unit tests for ViewModels and repositories
- Compose UI tests for screens
- Room migration tests
- Manual testing on developer device

#### Phase 2: Internal Testing Track (Play Console)
**Duration**: 1-2 weeks
**Who**: Developer + up to 100 internal testers
**Focus**: Smoke testing, basic functionality

**Setup**:
1. Upload APK/Bundle to Internal Testing track
2. Add internal testers (email addresses)
3. Share testing link
4. Gather feedback via Google Forms or direct communication

**Exit Criteria**:
- No P0 bugs
- Core functionality verified on at least 2 devices
- Pre-launch report generated and reviewed

#### Phase 3: Closed Testing Track
**Duration**: 2-4 weeks
**Who**: Broader tester group (friends, colleagues, community)
**Focus**: Real-world usage, edge cases, diverse devices

**Requirements**:
- Minimum 12 testers (if new personal account post-Nov 13, 2023)
- Testers opted-in for at least 14 continuous days
- Feedback collection mechanism

**Testing Focus**:
- Device diversity (different manufacturers, Android versions)
- Extended usage patterns (use app for days/weeks)
- Real-world scenarios

**Exit Criteria**:
- < 1% crash rate
- < 0.5% ANR rate
- All P0 and P1 bugs fixed
- Accessibility issues resolved
- Positive tester feedback

#### Phase 4: Open Testing (Optional)
**Duration**: 1-2 weeks (optional before production)
**Who**: Public testers via Play Store
**Focus**: Large-scale validation, final polish

#### Phase 5: Production Release
**Who**: All users
**Strategy**: Staged rollout (recommended)

**Staged Rollout Plan**:
- Week 1: 5% of users
- Week 2: 20% of users
- Week 3: 50% of users
- Week 4: 100% of users

**Monitor**:
- Crash reports (Firebase Crashlytics or Play Console)
- ANR reports
- User reviews and ratings
- Uninstall rate

### 5.2 Test Execution Strategy

#### Daily Testing Routine (During Development)
1. Run unit tests: `./gradlew test`
2. Run instrumented tests: `./gradlew connectedAndroidTest`
3. Manual testing on developer device
4. Check Lint warnings: `./gradlew lint`

#### Pre-Release Testing Routine (Before Upload)
**Day 1-2: Core Functionality**
- Execute all P0 functional tests (Section 2.1)
- Test on minimum 2 physical devices (phone + tablet)
- Test both light and dark modes

**Day 3: Database & Persistence**
- Run Room migration tests
- Test data persistence scenarios
- Verify foreign key cascades

**Day 4: Performance & Accessibility**
- Run LeakCanary and check for memory leaks
- Test with TalkBack enabled
- Run Accessibility Scanner
- Monitor battery usage during extended focus session

**Day 5: Edge Cases & Configurations**
- Test app lifecycle edge cases (calls, low memory, etc.)
- Test different font sizes and display sizes
- Test landscape orientation on all screens

**Day 6: Release Build Testing**
- Build release APK: `./gradlew assembleRelease`
- Install release APK on devices
- Repeat critical P0 tests on release build
- Verify ProGuard didn't break functionality

**Day 7: Final Checklist & Upload**
- Complete final checklist (Section 4)
- Upload to Internal Testing track
- Generate pre-launch report
- Review pre-launch report for issues

### 5.3 Bug Tracking & Prioritization

**Priority Levels**:
- **P0 (Blocker)**: Prevents release, must fix immediately
  - App crashes on launch
  - Data loss or corruption
  - Critical feature completely broken

- **P1 (High)**: Should fix before release
  - Major feature partially broken
  - Performance issues (ANR, jank)
  - Accessibility violations

- **P2 (Medium)**: Nice to fix, can defer to next release
  - Minor UI issues
  - Edge case bugs
  - Feature enhancements

- **P3 (Low)**: Backlog
  - Cosmetic issues
  - Future feature requests

**Known VoxPlanApp Bugs**:
- ⚠️ **P0**: Delete dialog bug in `DaySchedule.kt` (lines 110-126) - undefined `event` variable
  - **Fix**: Use `showDeleteParentDialog.value` instead of `event`
  - **Status**: MUST FIX BEFORE RELEASE

- ⚠️ **P2**: `QuickScheduleScreen.kt` entirely commented out (lines 26-86)
  - **Fix**: Either implement properly or remove file
  - **Status**: Can defer if feature not needed for v1.0

---

## 6. JETPACK COMPOSE SPECIFIC TESTING

### 6.1 Compose UI Test Setup

**Dependencies** (already should be in `build.gradle.kts`):
```kotlin
androidTestImplementation("androidx.compose.ui:ui-test-junit4:$compose_version")
debugImplementation("androidx.compose.ui:ui-test-manifest:$compose_version")
```

**Basic Test Structure**:
```kotlin
@RunWith(AndroidJUnit4::class)
class MainScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun goalList_displaysCorrectly() {
        // Setup test data
        val fakeGoals = listOf(
            TodoItem(id = 1, content = "Test Goal", parentId = null)
        )

        // Set Compose content
        composeTestRule.setContent {
            VoxPlanAppTheme {
                MainScreen(/* inject test ViewModel */)
            }
        }

        // Find and interact with elements
        composeTestRule.onNodeWithText("Test Goal").assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription("Add Goal").performClick()

        // Verify behavior
        composeTestRule.onNodeWithText("New Goal").assertExists()
    }
}
```

### 6.2 Key Compose Testing Patterns

#### Finding Elements (Finders)
```kotlin
// By text
onNodeWithText("Continue")
onNodeWithText("Continue", substring = true) // Partial match

// By content description
onNodeWithContentDescription("Add Goal")

// By semantic property
onNode(hasClickAction())
onNode(isEnabled())

// By test tag (add Modifier.testTag("myTag") in composable)
onNodeWithTag("goal_list")

// Collections
onAllNodesWithText("Goal").onFirst()
onAllNodesWithContentDescription("Delete").assertCountEquals(3)
```

#### Performing Actions (Actions)
```kotlin
performClick()
performTextInput("My Goal")
performTextClearance()
performScrollTo()
performTouchInput { swipeLeft() }
performGesture { longClick() }
```

#### Making Assertions (Assertions)
```kotlin
assertIsDisplayed()
assertIsNotDisplayed()
assertExists()
assertDoesNotExist()
assertTextEquals("Expected Text")
assertTextContains("Partial")
assertIsEnabled()
assertIsNotEnabled()
assertIsSelected()
```

### 6.3 VoxPlanApp Compose Test Scenarios

#### Test: Main Screen Goal List
```kotlin
@Test
fun mainScreen_displaysGoalsCorrectly() {
    val testGoals = listOf(
        TodoItem(id = 1, content = "Programming", parentId = null, order = 0),
        TodoItem(id = 2, content = "Exercise", parentId = null, order = 1)
    )

    composeTestRule.setContent {
        VoxPlanAppTheme {
            MainScreen(uiState = MainUiState(goalList = testGoals))
        }
    }

    composeTestRule.onNodeWithText("Programming").assertIsDisplayed()
    composeTestRule.onNodeWithText("Exercise").assertIsDisplayed()
}
```

#### Test: Focus Mode Timer
```kotlin
@Test
fun focusMode_timerStartsAndPauses() {
    composeTestRule.setContent {
        VoxPlanAppTheme {
            FocusModeScreen(/* test ViewModel */)
        }
    }

    // Verify initial state
    composeTestRule.onNodeWithText("00:00:00").assertIsDisplayed()

    // Start timer
    composeTestRule.onNodeWithContentDescription("Start Timer").performClick()

    // Advance time (using MainTestClock)
    composeTestRule.mainClock.advanceTimeBy(1000L)

    // Verify timer updated
    composeTestRule.onNodeWithText("00:00:01").assertIsDisplayed()

    // Pause timer
    composeTestRule.onNodeWithContentDescription("Pause Timer").performClick()

    // Verify pause state
    composeTestRule.onNodeWithContentDescription("Resume Timer").assertExists()
}
```

#### Test: Daily Screen with Empty State
```kotlin
@Test
fun dailyScreen_showsEmptyState_whenNoTasks() {
    composeTestRule.setContent {
        VoxPlanAppTheme {
            DailyScreen(uiState = DailyUiState(dailies = emptyList()))
        }
    }

    composeTestRule.onNodeWithText("No daily tasks yet").assertIsDisplayed()
    composeTestRule.onNodeWithText("Add Quota Tasks").assertIsDisplayed()
}
```

#### Test: ActionMode Reordering
```kotlin
@Test
fun mainScreen_actionModeReordering_works() {
    composeTestRule.setContent {
        VoxPlanAppTheme {
            MainScreen(/* test ViewModel */)
        }
    }

    // Enter ActionMode
    composeTestRule.onNodeWithContentDescription("Vertical Up").performClick()

    // Select goal to reorder
    composeTestRule.onNodeWithText("Exercise").performClick()

    // Verify reordering happened (check order in list)
    // This requires access to ViewModel state or using test tags
}
```

### 6.4 Testing State Management & Recomposition

#### Test: State Changes Trigger Recomposition
```kotlin
@Test
fun mainScreen_recomposesOnStateChange() {
    val viewModel = MainViewModel(/* test dependencies */)

    composeTestRule.setContent {
        VoxPlanAppTheme {
            val uiState by viewModel.mainUiState.collectAsState()
            MainScreen(uiState = uiState, viewModel = viewModel)
        }
    }

    // Initial state
    composeTestRule.onNodeWithText("No goals").assertIsDisplayed()

    // Change state
    viewModel.addGoal("New Goal")

    // Verify recomposition
    composeTestRule.onNodeWithText("New Goal").assertIsDisplayed()
}
```

#### Test: Loading States
```kotlin
@Test
fun progressScreen_showsLoadingState() {
    composeTestRule.setContent {
        VoxPlanAppTheme {
            ProgressScreen(uiState = ProgressUiState(isLoading = true))
        }
    }

    composeTestRule.onNodeWithContentDescription("Loading").assertIsDisplayed()
}
```

### 6.5 Compose Testing Best Practices

✅ **DO**:
- Use semantic properties (text, content description) over test tags
- Test user-visible behavior, not implementation details
- Use `createComposeRule()` for pure composables, `createAndroidComposeRule<Activity>()` when you need Activity context
- Inject test data into composables (don't rely on real repositories)
- Use `mainClock.advanceTimeBy()` for time-based animations/effects
- Test in isolation (mock ViewModels and dependencies)

❌ **DON'T**:
- Test Compose framework itself (Google already does this)
- Test every single composable in isolation (focus on screens/features)
- Rely on pixel-perfect UI tests (use semantic assertions)
- Use Thread.sleep() (use mainClock instead)
- Test private composable functions (test public API only)

### 6.6 Accessibility in Compose Tests

```kotlin
@Test
fun focusMode_isAccessible() {
    composeTestRule.setContent {
        VoxPlanAppTheme {
            FocusModeScreen(/* test ViewModel */)
        }
    }

    // Verify content descriptions exist
    composeTestRule.onNodeWithContentDescription("Start Timer").assertExists()
    composeTestRule.onNodeWithContentDescription("Add Discrete Task").assertExists()

    // Verify clickable items have reasonable size (48dp minimum)
    composeTestRule.onNodeWithContentDescription("Start Timer")
        .assertHeightIsAtLeast(48.dp)
        .assertWidthIsAtLeast(48.dp)
}
```

---

## 7. COMMON PITFALLS TO AVOID

### 7.1 Technical Pitfalls

❌ **Leaving Debug Code in Release**:
- `isDebuggable = true` → Reject or security risk
- `Log.d()` statements everywhere → Performance hit
- Test data hardcoded → Confusing user experience

❌ **Database Migration Issues**:
- Not testing migrations → Data loss on update
- Missing migration paths → App crashes on update
- Not exporting schemas → Can't write migration tests

❌ **Memory Leaks**:
- Non-static inner classes in ViewModels → Activity leaks
- Long-running coroutines without lifecycle awareness → Memory growth
- Bitmap loading without recycling → OutOfMemoryError

❌ **ANR Violations**:
- Database operations on main thread → App freezes
- Heavy computation in ViewModel init → UI hangs
- BroadcastReceiver doing too much work → System ANR dialog

❌ **ProGuard/R8 Issues**:
- Not testing release build → Runtime crashes
- Missing ProGuard rules for Room, Compose → Reflection failures
- Over-aggressive shrinking → Features break

### 7.2 Play Store Policy Pitfalls

❌ **Privacy Policy Mistakes**:
- No privacy policy when collecting data → Rejection
- Broken privacy policy link → Rejection
- Privacy policy doesn't match data collection → Rejection

❌ **Data Safety Disclosure Errors**:
- Incomplete disclosures → Rejection
- Inaccurate disclosures → User distrust
- Claiming "no data collection" when you store user data → Violation

❌ **Permission Over-Requesting**:
- Requesting unnecessary permissions → User distrust
- Not explaining why permissions needed → Uninstalls
- Requesting dangerous permissions without justification → Rejection

❌ **Content Rating Mistakes**:
- Under-rating mature content → Rejection or suspension
- Over-rating harmless content → Limits audience reach

❌ **Copyright Infringement**:
- Using stock icons without license → Rejection
- App name too similar to existing app → Rejection
- Screenshots containing third-party logos → Violation

### 7.3 Testing Pitfalls

❌ **Testing Only on High-End Devices**:
- App crashes on budget phones → Bad ratings
- Performance acceptable on flagship but terrible on mid-range → Uninstalls

❌ **Not Testing Migrations**:
- Users update from v1.0 → v2.0 → Data loss → 1-star reviews

❌ **Ignoring Accessibility**:
- App unusable with TalkBack → Exclusion of users, potential legal issues
- Low contrast text → Poor readability

❌ **Not Testing Edge Cases**:
- App crashes when phone call received during critical operation
- Data corruption when low storage
- ANR when device under heavy load

❌ **Skipping Release Build Testing**:
- ProGuard breaks feature that worked in debug → Users report crashes
- Shrinking removes necessary classes → App won't launch

### 7.4 User Experience Pitfalls

❌ **Poor Error Handling**:
- App crashes instead of showing error message → Bad reviews
- Error messages too technical ("NullPointerException") → Confusion
- No recovery path from errors → User frustration

❌ **State Loss**:
- User data lost when rotating device → Frustration
- Timer resets when app goes to background → Uninstall
- Form inputs cleared on error → Re-entry burden

❌ **Confusing Navigation**:
- No way to go back from screen → User stuck
- Breadcrumb navigation unclear → Disorientation
- Bottom nav doesn't show current tab → Confusion

---

## 8. TOOLS & RESOURCES

### 8.1 Testing Tools

**Android Studio Built-In**:
- Android Profiler (CPU, memory, network, energy)
- Layout Inspector (UI hierarchy debugging)
- Database Inspector (Room database viewer)
- Logcat (runtime logs)

**Third-Party Libraries**:
- **LeakCanary** - Automatic memory leak detection
  ```gradle
  debugImplementation 'com.squareup.leakcanary:leakcanary-android:2.12'
  ```
- **Accessibility Scanner** - Google's accessibility testing app (Play Store)
- **Android Lint** - Static code analysis (built-in)

**Testing Frameworks**:
- **JUnit 4** - Unit testing (already in project)
- **Compose UI Test** - Compose UI testing (already in project)
- **Room Testing** - Migration testing
  ```gradle
  androidTestImplementation 'androidx.room:room-testing:2.6.1'
  ```
- **Espresso** - View-based UI testing (if needed for Views)

**Cloud Testing**:
- **Firebase Test Lab** - Real device testing in cloud
- **Google Play Console Pre-Launch Reports** - Automated testing

**Monitoring & Analytics**:
- **Firebase Crashlytics** - Crash reporting
- **Firebase Performance Monitoring** - Performance metrics
- **Google Play Console Vitals** - ANR, crash rates

### 8.2 Official Documentation

**Android Testing**:
- [Testing in Android](https://developer.android.com/training/testing)
- [Test your Compose layout](https://developer.android.com/develop/ui/compose/testing)
- [Room Migration Testing](https://developer.android.com/training/data-storage/room/migrating-db-versions)
- [Prepare for release](https://developer.android.com/studio/publish/preparing)

**Google Play**:
- [Pre-launch reports](https://support.google.com/googleplay/android-developer/answer/9842757)
- [Core app quality guidelines](https://developer.android.com/docs/quality-guidelines/core-app-quality)
- [App testing requirements](https://support.google.com/googleplay/android-developer/answer/14151465)

**Accessibility**:
- [Accessibility principles](https://developer.android.com/guide/topics/ui/accessibility/principles)
- [TalkBack testing guide](https://accessibility.huit.harvard.edu/test-android-talkback)

### 8.3 Useful Commands

**Build & Testing**:
```bash
# Run unit tests
./gradlew test

# Run instrumented tests
./gradlew connectedAndroidTest

# Run specific test
./gradlew test --tests com.voxplanapp.MainViewModelTest

# Build debug APK
./gradlew assembleDebug

# Build release APK
./gradlew assembleRelease

# Run lint checks
./gradlew lint

# Clean build
./gradlew clean build
```

**Device Testing**:
```bash
# List connected devices
adb devices

# Install APK
adb install -r app/build/outputs/apk/release/app-release.apk

# View logs
adb logcat

# Filter logs by app
adb logcat | grep "com.voxplanapp"

# Check for ANRs
adb logcat | grep "ANR in"

# Dump memory info
adb shell dumpsys meminfo com.voxplanapp

# Dump battery stats
adb shell dumpsys batterystats

# Kill app process (simulate low memory)
adb shell am kill com.voxplanapp

# Clear app data
adb shell pm clear com.voxplanapp

# Take screenshot
adb shell screencap /sdcard/screenshot.png
adb pull /sdcard/screenshot.png

# Record screen video
adb shell screenrecord /sdcard/demo.mp4
adb pull /sdcard/demo.mp4
```

**Database Inspection**:
```bash
# Pull database from device
adb pull /data/data/com.voxplanapp/databases/voxplan_database .

# Inspect with sqlite3
sqlite3 voxplan_database
.tables
.schema TodoItem
SELECT * FROM TodoItem;
```

---

## 9. VOXPLANAPP-SPECIFIC TEST SCENARIOS

### 9.1 Critical User Flows to Test

#### Flow 1: First-Time User Experience
1. Install app
2. See empty state on Main screen
3. Tap "Add Goal"
4. Create first root goal "Programming"
5. Navigate to Daily tab → See empty state
6. Navigate to Progress tab → See no quotas
7. Navigate back to Main
8. Tap "Programming" to enter
9. Add subgoal "Learn Kotlin"
10. Set quota for "Programming" (30 mins/day, Mon-Fri)
11. Navigate to Daily tab
12. Tap "Add Quota Tasks"
13. See "Programming" daily task appear
14. Tap "Programming" daily → Start Focus Mode
15. Timer starts successfully
16. Complete 30-minute session
17. Bronze medal awarded
18. Time banked to TimeBank
19. Navigate to Progress tab
20. See progress for "Programming" goal

**Validation**:
- ✅ Empty states are helpful and actionable
- ✅ Navigation is intuitive
- ✅ Quota system is understandable
- ✅ Focus Mode is discoverable and engaging

#### Flow 2: Power User - Complex Hierarchy
1. Create root goal "Professional Development"
2. Add subgoals:
   - "Programming" (set quota 2 hrs/day)
     - "Kotlin"
     - "Android"
   - "Design"
     - "Figma"
3. Create 5 more root goals (total 6)
4. Navigate 3 levels deep: Professional Development → Programming → Kotlin
5. Breadcrumb navigation shows full path
6. Tap breadcrumb to navigate up
7. Use ActionMode to reorder goals
8. Use HierarchyUp to move "Design" under "Professional Development"

**Validation**:
- ✅ Depth limit enforced (max 3)
- ✅ Breadcrumb navigation clear and functional
- ✅ Reordering (vertical and hierarchical) works correctly
- ✅ State preserved during navigation

#### Flow 3: Daily Planning & Scheduling
1. Navigate to Daily tab (Monday)
2. Tap "Add Quota Tasks"
3. See all quota goals with active Monday
4. Reorder dailies using ActionMode
5. Navigate to Schedule tab
6. Long-press "Programming" daily, drag to 9:00 AM - 11:00 AM slot
7. Event appears on schedule
8. Tap event → Edit to 9:00 AM - 10:30 AM
9. Tap event → Focus Mode
10. Complete focus session
11. Return to Schedule → Event shows completed state (verify implementation)
12. Delete scheduled event
13. Return to Daily → Daily task still exists
14. Delete daily with "Delete both" option
15. Both daily and any scheduled events deleted

**Validation**:
- ✅ Quota task generation works for correct active days
- ✅ Drag-to-schedule intuitive and responsive
- ✅ Parent-child relationship between daily and scheduled maintained
- ✅ Delete dialog works (BUG: currently broken, must fix)
- ✅ Focus Mode accessible from both daily and scheduled views

#### Flow 4: Extended Focus Session
1. Start Focus Mode for 2+ hour goal
2. Run timer for 30 minutes → Bronze medal
3. Continue to 60 minutes → Silver medal
4. Add 3 discrete tasks during session
5. Complete discrete tasks → 15 points each
6. Continue to 90 minutes → Gold medal
7. Continue to 120 minutes → Diamond medal
8. Pause timer
9. Lock phone screen → Return → Timer still paused
10. Resume timer
11. Receive phone call → Return → Timer still running (or paused, verify behavior)
12. Tap "Bank Time"
13. Event created with accrued duration
14. Navigate to Schedule → See banked event

**Validation**:
- ✅ Timer persistence across app lifecycle
- ✅ Medals awarded correctly at intervals
- ✅ Discrete tasks add engagement
- ✅ Time banking creates scheduled event correctly
- ✅ State preserved during interruptions

### 9.2 VoxPlanApp Edge Cases

| Edge Case | Scenario | Expected Behavior | Priority |
|-----------|----------|-------------------|----------|
| **Empty goal title** | Try to save goal with empty title | Validation prevents save or defaults to "Untitled" | P0 |
| **Max depth enforcement** | Add subgoal to depth-3 goal | Button disabled or error message | P0 |
| **Delete root with large tree** | Delete root with 10 subgoals and 20 sub-subgoals | All cascade deleted, confirmation dialog | P0 |
| **Quota all days inactive** | Create quota with activeDays "0000000" | No daily tasks generated or validation error | P1 |
| **Focus session across midnight** | Start timer at 11:50 PM, run past midnight | Timer continues, correct date for time banking | P1 |
| **Scheduled event past midnight** | Schedule event 11:00 PM - 1:00 AM | Event displays correctly (spanning days?) | P2 |
| **100+ goals at single level** | Create 100 root goals | Performance acceptable, scrolling smooth | P1 |
| **Very long goal titles** | Goal title 200+ characters | Truncates in list, full title in edit | P1 |
| **Emoji in goal titles** | Use 🎯 📚 ⚽ in titles | Displays correctly across app | P1 |
| **Reorder while timer running** | Start focus session, go to Main, reorder | No crashes, timer continues | P1 |
| **Delete goal with active focus session** | Start focus for goal, delete goal | Graceful handling (prevent delete or clear session) | P0 |
| **Database at 1GB+ size** | Simulate large database | App remains responsive | P2 |
| **Quota edge times** | Quota from 11:59 PM to 12:01 AM | Handles correctly | P2 |

### 9.3 VoxPlanApp Performance Benchmarks

**Target Performance**:

| Metric | Target | Measurement Method | Priority |
|--------|--------|-------------------|----------|
| **App startup (cold)** | < 2 seconds | Logcat Displayed time | P0 |
| **App startup (warm)** | < 1 second | Logcat Displayed time | P1 |
| **Main screen render (100 goals)** | < 500ms | Android Profiler | P1 |
| **Focus timer precision** | ± 1 second over 2 hours | Manual verification | P1 |
| **Database query (1000 goals)** | < 100ms | Room query time logging | P1 |
| **Migration 12→13 (1000 records)** | < 5 seconds | MigrationTestHelper timing | P0 |
| **Memory usage (idle)** | < 100 MB | Profiler memory view | P1 |
| **Memory usage (focus session 2hr)** | < 150 MB | Profiler memory view | P1 |
| **Battery drain (2hr focus)** | < 10% | Battery historian | P1 |

**Testing Commands**:
```bash
# Measure cold startup time
adb shell am force-stop com.voxplanapp
adb shell am start -W -n com.voxplanapp/.MainActivity
# Look for "TotalTime" in output

# Monitor memory
adb shell dumpsys meminfo com.voxplanapp | grep TOTAL

# Monitor battery
adb shell dumpsys batterystats --reset
# Use app for test period
adb shell dumpsys batterystats com.voxplanapp
```

---

## 10. FINAL PRE-RELEASE CHECKLIST

### Must-Fix Before Release

- [ ] **P0 BUG**: Fix delete dialog in `DaySchedule.kt` (lines 110-126)
- [ ] **P0**: Remove all `Log.d()`, `Log.v()`, `Log.i()` calls
- [ ] **P0**: Set `isDebuggable = false` in release build type
- [ ] **P0**: Test release build on at least 2 physical devices
- [ ] **P0**: Run all Room migration tests (especially 12→13)
- [ ] **P0**: Verify no P0 bugs in bug tracker
- [ ] **P0**: Test with TalkBack, fix critical accessibility issues
- [ ] **P0**: Complete Data Safety section in Play Console
- [ ] **P0**: Verify privacy policy (or confirm not needed)
- [ ] **P0**: Test on Android 8.1 (API 27) and Android 14 (API 34)

### Highly Recommended

- [ ] **P1**: Integrate LeakCanary and fix any memory leaks
- [ ] **P1**: Run Lint and fix high-priority warnings
- [ ] **P1**: Test on low-end device (< 2GB RAM)
- [ ] **P1**: Test landscape orientation on all screens
- [ ] **P1**: Test dark mode and light mode
- [ ] **P1**: Test largest and smallest font sizes
- [ ] **P1**: Complete closed testing with 12+ testers for 14 days (if required)
- [ ] **P1**: Achieve < 1% crash rate in pre-launch report
- [ ] **P1**: Achieve < 0.5% ANR rate in pre-launch report
- [ ] **P1**: Write Compose UI tests for core screens
- [ ] **P1**: Verify app icon and feature graphic meet guidelines

### Nice to Have

- [ ] **P2**: Integrate Firebase Crashlytics
- [ ] **P2**: Test on tablet device
- [ ] **P2**: Test on foldable (emulator acceptable)
- [ ] **P2**: Run Accessibility Scanner and address suggestions
- [ ] **P2**: Optimize images to WebP format
- [ ] **P2**: Enable ProGuard/R8 optimizations
- [ ] **P2**: Create promotional video (optional for Play Store)
- [ ] **P2**: Prepare social media assets for launch

---

## CONCLUSION

This comprehensive testing guide provides a roadmap for ensuring VoxPlanApp meets Google Play Store standards and delivers a high-quality user experience. Key takeaways:

1. **Fix Critical Bugs First**: Delete dialog bug is a blocker (P0)
2. **Test Migrations Thoroughly**: 13 migrations in your database - validate each path
3. **Don't Skip Accessibility**: TalkBack testing is non-negotiable
4. **Use Real Devices**: Emulators are supplements, not replacements
5. **Monitor Crash/ANR Rates**: Stay below 1.09% crash and 0.47% ANR thresholds
6. **Complete Pre-Launch Report**: Free testing from Google, use it
7. **Staged Rollout**: Release to 5% → 20% → 50% → 100% to catch issues early

**Next Steps**:
1. Review this guide with development team
2. Create tracking sheet for test scenarios (spreadsheet/project management tool)
3. Fix P0 bugs before any testing begins
4. Execute Phase 1 (Internal) testing
5. Upload to Internal Testing track → Generate pre-launch report
6. Iterate based on findings
7. Proceed through Closed Testing → Production

Good luck with your release! 🚀

---

## Sources & References

### Google Play Store Requirements
- [App testing requirements for new personal developer accounts](https://support.google.com/googleplay/android-developer/answer/14151465?hl=en)
- [Set up an open, closed, or internal test](https://support.google.com/googleplay/android-developer/answer/9845334?hl=en/)
- [Prepare and roll out a release](https://support.google.com/googleplay/android-developer/answer/9859348?hl=en)
- [Meet Google Play's target API level requirement](https://developer.android.com/google/play/requirements/target-sdk)
- [The essential Google Play Store app pre-release checklist](https://techbeacon.com/app-dev-testing/essential-google-play-store-app-pre-release-checklist)

### Pre-Launch Reports
- [Pre-launch reports | Google Play Console](https://play.google.com/console/about/pre-launchreports/)
- [Use a pre-launch report to identify issues](https://support.google.com/googleplay/android-developer/answer/9842757?hl=en)
- [Understand your pre-launch report](https://support.google.com/googleplay/android-developer/answer/9844487?hl=en)

### Jetpack Compose Testing
- [Test your Compose layout | Android Developers](https://developer.android.com/develop/ui/compose/testing)
- [Common patterns | Jetpack Compose Testing](https://developer.android.com/develop/ui/compose/testing/common-patterns)
- [Testing in Jetpack Compose | Android Developers Codelab](https://developer.android.com/codelabs/jetpack-compose-testing)
- [Testing cheatsheet | Jetpack Compose](https://developer.android.com/jetpack/compose/testing-cheatsheet)

### Room Database Testing
- [Migrate your Room database | Android Developers](https://developer.android.com/training/data-storage/room/migrating-db-versions)
- [Testing Room migrations | Android Developers Blog](https://medium.com/androiddevelopers/testing-room-migrations-be93cdb0d975)

### Manual Testing Best Practices
- [Android Mobile App Testing Checklist for Testing in 2025](https://www.globalapptesting.com/blog/android-mobile-app-testing-checklist)
- [Mobile App Testing Checklist | BrowserStack](https://www.browserstack.com/guide/mobile-app-testing-checklist)
- [Prepare your app for release | Android Studio](https://developer.android.com/studio/publish/preparing)

### Accessibility Testing
- [Guidelines for Accessibility in Android | BrowserStack](https://www.browserstack.com/guide/android-accessibility-guidelines)
- [Principles for improving app accessibility | Android Developers](https://developer.android.com/guide/topics/ui/accessibility/principles)
- [How to Test Accessibility with Android TalkBack](https://accessibility.huit.harvard.edu/test-android-talkback)
- [10 Android Accessibility Best Practices 2024](https://daily.dev/blog/10-android-accessibility-best-practices-2024)

### App Rejection Reasons
- [11 Common Google Play Store Rejections And How to Avoid Them](https://onemobile.ai/common-google-play-store-rejections/)
- [Why Apps Get Rejected from the App Store and Google Play](https://www.adalo.com/posts/why-apps-get-rejected-app-store-google-play)
- [Google Play Store & App Store App Submission Guidelines in 2025](https://ripenapps.com/blog/app-submission-guidelines/)

### Performance & Edge Cases
- [ANRs | App quality | Android Developers](https://developer.android.com/topic/performance/vitals/anr)
- [How to diagnose Android memory issues in low-end devices](https://embrace.io/blog/diagnose-android-memory-issues-low-end-devices/)
- [How to Use Android Vitals to Improve App Stability](https://bugfender.com/blog/android-vitals/)

### Device Testing
- [Support different pixel densities | Android Developers](https://developer.android.com/training/multiscreen/screendensities)
- [Screen compatibility overview | Android Developers](https://developer.android.com/guide/practices/screens_support)
- [Device Metrics for Any Screen - Material Design 3](https://m3.material.io/blog/device-metrics/)
