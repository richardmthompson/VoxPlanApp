# VoxPlan

**A hierarchical goal management Android app with gamified focus sessions**

VoxPlan helps you break down ambitious goals into actionable tasks, set daily time quotas, and stay focused with a gamified timer that rewards sustained concentration.

> *"Time is the one truly limited resource. We all have the same 24 hours, but some people achieve far more than others."*

---

## Features

### Hierarchical Goal Management
Organize your goals in a natural tree structure up to 3 levels deep:

```
Learn Guitar (Level 1)
├── Master Chord Progressions (Level 2)
│   ├── Practice G-C-D progression (Level 3)
│   └── Learn F chord fingering (Level 3)
└── Learn to Read Tablature (Level 2)
```

- **Breadcrumb navigation** for easy traversal
- **Reorder goals** vertically or move them up/down the hierarchy
- **No overwhelm** — the 3-level limit keeps things manageable

### Daily Quotas
Set time commitments for each goal:
- Define **daily minute targets** (e.g., 60 minutes for "Programming")
- Configure **active days** (e.g., Monday–Friday only)
- Quotas appear in your weekly progress view

### Gamified Focus Mode
A timer that makes focused work rewarding:

- **Medals** — Earn a medal for every 30 minutes of focused work
- **Crystals** — Achieve a crystal when you log 4+ hours in a single day
- **Pomodoro-style work/rest cycles**
- **Sound effects** for medal awards
- **Time banking** — earned time is saved and tracked

### Progress Tracking
- **Weekly view** showing time spent on each goal
- **Visual progress bars** against quota targets
- **Navigate previous/next weeks** to review history

---

## Screenshots

*Coming soon*

---

## Tech Stack

- **Language:** Kotlin 1.9
- **UI:** Jetpack Compose with Material 3 components
- **Architecture:** MVVM with manual dependency injection
- **Database:** Room (SQLite) with migrations
- **Async:** Kotlin Coroutines & Flow/StateFlow
- **Audio:** ExoPlayer (Media3) for sound effects

### Build Configuration
- **Compile SDK:** 34
- **Min SDK:** 27 (Android 8.1 Oreo)
- **Target SDK:** 34

---

## Getting Started

### Prerequisites
- Android Studio Hedgehog (2023.1.1) or later
- JDK 17+
- Android device or emulator running Android 8.1+

### Build & Run

```bash
# Clone the repository
git clone https://github.com/richardmthompson/VoxPlanApp.git
cd VoxPlanApp

# Build debug APK
./gradlew assembleDebug

# Install on connected device
./gradlew installDebug

# Run tests
./gradlew test
```

### Project Structure

```
app/src/main/java/com/voxplanapp/
├── MainActivity.kt                 # App entry point
├── VoxPlanApplication.kt           # Application class with DI setup
├── AppViewModelProvider.kt         # ViewModel factory
│
├── data/                           # Data layer (entities, DAOs, repositories)
│   ├── AppDatabase.kt              # Room database (v13) with migrations
│   ├── AppContainer.kt             # Manual dependency injection container
│   ├── Constants.kt                # App constants (FULLBAR_MINS, etc.)
│   ├── Converters.kt               # Room type converters (LocalDate/LocalTime)
│   ├── TodoItem.kt                 # Goal entity
│   ├── TodoDao.kt                  # Goal data access object
│   ├── TodoRepository.kt           # Goal repository
│   ├── TimeBankEntry.kt            # Time tracking entity
│   ├── QuotaEntity.kt              # Quota entity
│   ├── QuotaDao.kt                 # Quota data access object
│   ├── QuotaRepository.kt          # Quota repository
│   ├── Event.kt                    # Event entity (for dailies/scheduling)
│   ├── EventDao.kt                 # Event data access object
│   ├── EventRepository.kt          # Event repository
│   ├── GoalWithSubGoals.kt         # Composite model for hierarchy
│   └── GoalEventMapper.kt          # Mapping utilities
│
├── model/                          # Domain models
│   └── ActionMode.kt               # Reordering state enum
│
├── navigation/                     # Navigation infrastructure
│   ├── VoxPlanApp.kt               # Main scaffold + ActionModeHandler
│   ├── VoxPlanNavHost.kt           # Route definitions
│   ├── VoxPlanScreen.kt            # Sealed class for type-safe routes
│   └── NavigationViewModel.kt      # Bottom nav state
│
├── shared/                         # Shared utilities
│   ├── SharedViewModel.kt          # Breadcrumb navigation logic
│   └── SoundPlayer.kt              # ExoPlayer wrapper for sound effects
│
└── ui/                             # UI layer (screens and components)
    ├── main/                       # Goal hierarchy screen
    │   ├── MainScreen.kt           # Main goal list UI
    │   ├── MainViewModel.kt        # Goal state management
    │   ├── GoalListContainer.kt    # Scrollable goal list
    │   ├── GoalItem.kt             # Individual goal card
    │   ├── TodoInputBar.kt         # Goal creation input
    │   ├── BreadCrumbNavigation.kt # Breadcrumb UI
    │   └── QuickScheduleScreen.kt  # (Incomplete - commented out)
    │
    ├── goals/                      # Goal editing and progress
    │   ├── GoalEditScreen.kt       # Goal editor UI
    │   ├── GoalEditViewModel.kt    # Edit state management
    │   ├── QuotaSettings.kt        # Quota configuration UI
    │   ├── ProgressScreen.kt       # Weekly progress view
    │   └── ProgressViewModel.kt    # Progress state management
    │
    ├── focusmode/                  # Focus mode (timer)
    │   ├── FocusModeScreen.kt      # Timer UI with medals
    │   └── FocusViewModel.kt       # Timer state management
    │
    ├── daily/                      # Daily tasks (hidden in MVP)
    │   ├── DailyScreen.kt          # Daily planning UI
    │   └── DailyViewModel.kt       # Daily state management
    │
    ├── calendar/                   # Scheduling (hidden in MVP)
    │   ├── DaySchedule.kt          # Day scheduler UI
    │   └── SchedulerViewModel.kt   # Scheduler state
    │
    ├── constants/                  # UI constants
    │   ├── Colors.kt               # Color definitions
    │   ├── Dimens.kt               # Dimension constants
    │   ├── DpValues.kt             # Dp value constants
    │   └── TextStyles.kt           # Text style definitions
    │
    └── theme/                      # Material theme
        ├── Color.kt                # Theme colors
        ├── ExtendedColors.kt       # Brand-specific colors
        ├── Theme.kt                # Material theme setup
        └── Type.kt                 # Typography
```

---

## Architecture

VoxPlan follows **MVVM** with a clean separation between data, domain, and presentation layers.

### Key Patterns

**1. Manual Dependency Injection**
```kotlin
// AppContainer.kt
class AppDataContainer(context: Context) : AppContainer {
    override val database: AppDatabase by lazy { ... }
    override val todoRepository: TodoRepository by lazy { ... }
}
```
*Why?* Simpler than Hilt, no reflection overhead, all dependencies visible.

**2. StateFlow for Reactive UI**
```kotlin
val mainUiState: StateFlow<MainUiState> = combine(
    repository.getAllTodos(),
    sharedViewModel.breadcrumbs
) { todos, breadcrumbs ->
    MainUiState(goalList = processGoals(todos, currentParentId), breadcrumbs)
}.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000L), MainUiState())
```

**3. SharedViewModel for Cross-Screen State**
The breadcrumb navigation state is shared across multiple ViewModels through a singleton SharedViewModel.

**4. Room TypeConverters**
`LocalDate` → `Long` (epoch day), `LocalTime` → `String`

For detailed architecture documentation, see [`agent/context/codebase_context.md`](agent/context/codebase_context.md).

---

## Contributing

VoxPlan is a great project for learning Android development with Kotlin and Jetpack Compose!

### Good First Issues

**Easy:**
- Add unit tests for `SharedViewModel.processGoals()`
- Improve accessibility (content descriptions for icons)
- Add loading/error states to screens

**Medium:**
- Implement proper Material 3 theming (see "Known Limitations" below)
- Add completion checkboxes for goals
- Fix scroll position persistence in schedule view

**Advanced:**
- Implement the Dailies feature (code exists, needs integration)
- Add calendar/scheduling features
- Implement recurring events
- **Voice-controlled interface** — the original vision! Add speech recognition for hands-free goal entry, scheduling, and focus mode control (see "Future Vision" below)

### Development Notes

**Context for AI Coding Assistants:**
The `agent/context/` directory contains detailed documentation designed for AI pair programming:
- `project_context.md` — High-level project overview
- `codebase_context.md` — Technical implementation details
- `material3_context.md` — Material 3 migration plan

**Database Migrations:**
The app has 13 database migrations. When modifying entities:
1. Create a new migration in `AppDatabase.kt`
2. Increment the version number
3. Test the migration path

---

## Known Limitations

### Material 3 Theming
The app has Material 3 infrastructure but **doesn't fully use it**:
- Theme exists in `Theme.kt` but screens use hardcoded colors
- **No dark mode support** despite user demand
- 26 color constants + 42+ inline hex values scattered across the codebase

A migration plan exists in `agent/context/material3_context.md`.

### Deferred Features
These features have partial implementations but are hidden in the MVP:

**Dailies Feature (~70% complete)**
- Daily task list with quota integration
- Scheduling tasks into time blocks
- Missing: completion tracking, bulk operations

**Scheduling Feature (~65% complete)**
- Day view with drag-to-reschedule
- Missing: event creation from calendar, recurrence, week/month views

The code is preserved and can be re-enabled. See `docs/LLM-Generated/INCOMPLETE_FEATURES.md` for details.

### Minor Issues
- Some deprecated icon usage (cosmetic)
- `QuickScheduleScreen.kt` is entirely commented out
- No unit tests yet

---

## Future Vision

The long-term vision for VoxPlan is a **conversational personal assistant** — not just a passive tool, but a proactive coach that guides you through planning and execution.

**Planned versions:**
- **v4:** Voice-controlled goal entry and scheduling
- **v5:** Cloud sync and desktop connectivity
- **v6:** AI-driven "secretary conversations" for intelligent planning

---

## Documentation

| Document | Description |
|----------|-------------|
| [`agent/context/project_context.md`](agent/context/project_context.md) | High-level project overview, vision, domain concepts |
| [`agent/context/codebase_context.md`](agent/context/codebase_context.md) | Technical architecture, code patterns, file reference |
| [`docs/LLM-Generated/ARCHITECTURE.md`](docs/LLM-Generated/ARCHITECTURE.md) | Comprehensive architecture documentation |
| [`docs/LLM-Generated/INCOMPLETE_FEATURES.md`](docs/LLM-Generated/INCOMPLETE_FEATURES.md) | Analysis of partially implemented features |

---

## License

This project is licensed under the MIT License — see the [LICENSE](LICENSE) file for details.

---

## Acknowledgments

- Built with [Jetpack Compose](https://developer.android.com/jetpack/compose)
- Timer sound effects from [source]
- Developed with assistance from [Claude](https://claude.ai)

---

## Get Involved

**Interested in contributing?** We'd love to hear from you! Whether you want to:
- Pick up a "Good First Issue" to learn Android/Kotlin
- Tackle the voice control vision
- Help with Material 3 theming
- Or just chat about the project

**Open an issue** to introduce yourself, or reach out directly. This is a passion project and collaboration is welcome!

**Found a bug or have a feature idea?** Open an issue — all feedback helps shape VoxPlan's future.
