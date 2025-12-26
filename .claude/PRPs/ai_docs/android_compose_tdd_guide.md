# Android Jetpack Compose TDD Guide
## Comprehensive Test-Driven Development for Modern Android Apps

**Last Updated:** December 2025
**Target:** Android apps using Kotlin, Jetpack Compose, Room, Flow, and MVVM architecture

---

## Table of Contents

1. [Testing Framework Overview](#testing-framework-overview)
2. [Project Setup & Dependencies](#project-setup--dependencies)
3. [TDD Workflow for Compose](#tdd-workflow-for-compose)
4. [Compose UI Testing Patterns](#compose-ui-testing-patterns)
5. [ViewModel & StateFlow Testing](#viewmodel--stateflow-testing)
6. [Room Database Testing](#room-database-testing)
7. [Navigation Testing](#navigation-testing)
8. [Testing Best Practices](#testing-best-practices)
9. [Complete Code Examples](#complete-code-examples)

---

## Testing Framework Overview

### Test Types in Android

**Unit Tests (JVM - Local)**
- Run on development machine (JVM)
- Fast execution
- No Android dependencies
- Location: `app/src/test/java/`
- Use for: ViewModels, business logic, pure Kotlin functions

**Instrumented Tests (Android - Device/Emulator)**
- Run on Android device or emulator
- Access to Android framework
- Location: `app/src/androidTest/java/`
- Use for: Compose UI, Room database, navigation

### Core Testing Libraries

| Library | Purpose | Type |
|---------|---------|------|
| **JUnit 4/5** | Test framework foundation | Both |
| **Compose UI Test** | Compose component testing | Instrumented |
| **Espresso** | View-based UI testing | Instrumented |
| **Mockk/Mockito** | Mocking dependencies | Both |
| **Turbine** | Flow testing library | Unit |
| **kotlinx-coroutines-test** | Coroutine testing utilities | Both |
| **Truth** | Fluent assertions | Both |
| **Robolectric** | Android framework on JVM | Unit |

---

## Project Setup & Dependencies

### build.gradle.kts (Module Level)

```kotlin
plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.devtools.ksp") // For Room
}

android {
    compileSdk = 34

    defaultConfig {
        minSdk = 27
        targetSdk = 34
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        compose = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.1"
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    val composeBom = platform("androidx.compose:compose-bom:2024.10.00")
    implementation(composeBom)
    androidTestImplementation(composeBom)

    // Compose
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.7.0")

    // Room
    implementation("androidx.room:room-runtime:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")
    ksp("androidx.room:room-compiler:2.6.1")

    // Navigation
    implementation("androidx.navigation:navigation-compose:2.7.7")

    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.0")

    // ===== UNIT TESTS (JVM) =====
    testImplementation("junit:junit:4.13.2")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.8.0")
    testImplementation("io.mockk:mockk:1.13.9")
    testImplementation("app.cash.turbine:turbine:1.1.0")
    testImplementation("com.google.truth:truth:1.4.0")

    // ===== INSTRUMENTED TESTS (Android Device) =====
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test:runner:1.5.2")
    androidTestImplementation("androidx.test:rules:1.5.0")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")

    // Compose Testing
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
    debugImplementation("androidx.compose.ui:ui-tooling")

    // Room Testing
    androidTestImplementation("androidx.room:room-testing:2.6.1")

    // Navigation Testing
    androidTestImplementation("androidx.navigation:navigation-testing:2.7.7")

    // Coroutine Testing
    androidTestImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.8.0")

    // Mockk for Android
    androidTestImplementation("io.mockk:mockk-android:1.13.9")

    // Truth for assertions
    androidTestImplementation("com.google.truth:truth:1.4.0")
}
```

### Test Directory Structure

```
app/
├── src/
│   ├── main/
│   │   └── java/com/voxplanapp/
│   │       ├── data/
│   │       │   ├── TodoItem.kt
│   │       │   ├── TodoDao.kt
│   │       │   └── TodoRepository.kt
│   │       ├── ui/
│   │       │   ├── MainScreen.kt
│   │       │   └── MainViewModel.kt
│   │       └── VoxPlanApplication.kt
│   │
│   ├── test/ (UNIT TESTS - JVM)
│   │   └── java/com/voxplanapp/
│   │       ├── viewmodel/
│   │       │   ├── MainViewModelTest.kt
│   │       │   └── FocusViewModelTest.kt
│   │       ├── repository/
│   │       │   └── TodoRepositoryTest.kt
│   │       └── utils/
│   │           └── DateUtilsTest.kt
│   │
│   └── androidTest/ (INSTRUMENTED TESTS - Android Device)
│       └── java/com/voxplanapp/
│           ├── ui/
│           │   ├── MainScreenTest.kt
│           │   ├── DailyScreenTest.kt
│           │   └── FocusModeScreenTest.kt
│           ├── database/
│           │   ├── TodoDaoTest.kt
│           │   └── MigrationTest.kt
│           └── navigation/
│               └── NavigationTest.kt
```

---

## TDD Workflow for Compose

### The Red-Green-Refactor Cycle

**Test-Driven Development follows this cycle:**

1. **RED** - Write a failing test
2. **GREEN** - Write minimal code to pass the test
3. **REFACTOR** - Improve code while keeping tests green

### TDD Principles (Robert C. Martin's Three Rules)

1. Write production code **only** to pass a failing unit test
2. Write **no more** of a unit test than sufficient to fail
3. Write **no more** production code than necessary to pass the one failing unit test

### Practical TDD Workflow for Compose

#### Example: Adding a "Mark Complete" Button to a Goal Item

**Step 1: Write the Test First (RED)**

```kotlin
// app/src/androidTest/java/com/voxplanapp/ui/GoalItemTest.kt
@RunWith(AndroidJUnit4::class)
class GoalItemTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun goalItem_clickCompleteButton_callsOnComplete() {
        // Arrange
        var completeCalled = false
        val goal = TodoItem(id = 1, title = "Test Goal", order = 0)

        composeTestRule.setContent {
            GoalItem(
                goal = goal,
                onComplete = { completeCalled = true }
            )
        }

        // Act
        composeTestRule
            .onNodeWithContentDescription("Mark complete")
            .performClick()

        // Assert
        assertThat(completeCalled).isTrue()
    }
}
```

**Step 2: Run the test - it FAILS (component doesn't exist)**

```bash
./gradlew connectedAndroidTest --tests GoalItemTest
# Expected: Composable 'GoalItem' not found
```

**Step 3: Write Minimal Code to Pass (GREEN)**

```kotlin
// app/src/main/java/com/voxplanapp/ui/GoalItem.kt
@Composable
fun GoalItem(
    goal: TodoItem,
    onComplete: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(goal.title)
        IconButton(
            onClick = onComplete,
            modifier = Modifier.semantics {
                contentDescription = "Mark complete"
            }
        ) {
            Icon(Icons.Default.Check, contentDescription = null)
        }
    }
}
```

**Step 4: Run test again - it PASSES**

```bash
./gradlew connectedAndroidTest --tests GoalItemTest
# ✓ All tests passed
```

**Step 5: Refactor (while keeping tests green)**

```kotlin
@Composable
fun GoalItem(
    goal: TodoItem,
    onComplete: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = goal.title,
                style = MaterialTheme.typography.bodyLarge
            )
            IconButton(
                onClick = onComplete,
                modifier = Modifier.semantics {
                    contentDescription = "Mark complete"
                }
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}
```

**Step 6: Verify tests still pass after refactoring**

```bash
./gradlew connectedAndroidTest --tests GoalItemTest
# ✓ All tests passed (refactoring successful)
```

### TDD for State Management

**Step 1: Write ViewModel test FIRST**

```kotlin
// app/src/test/java/com/voxplanapp/viewmodel/MainViewModelTest.kt
class MainViewModelTest {
    @Test
    fun `when completeGoal called, goal is marked complete`() = runTest {
        // Arrange
        val repository = FakeTodoRepository()
        val goal = TodoItem(id = 1, title = "Test", order = 0, isComplete = false)
        repository.insertItem(goal)

        val viewModel = MainViewModel(repository)

        // Act
        viewModel.completeGoal(1)

        // Assert
        val updatedGoal = repository.getItemById(1).first()
        assertThat(updatedGoal?.isComplete).isTrue()
    }
}
```

**Step 2: Implement ViewModel to pass the test**

```kotlin
class MainViewModel(
    private val repository: TodoRepository
) : ViewModel() {
    fun completeGoal(goalId: Int) {
        viewModelScope.launch {
            val goal = repository.getItemById(goalId).first()
            goal?.let {
                repository.updateItem(it.copy(isComplete = true))
            }
        }
    }
}
```

### Benefits of TDD in Compose

1. **Prevents over-engineering** - Only build what's tested
2. **Living documentation** - Tests show how components should be used
3. **Regression safety** - Changes won't break existing features
4. **Design feedback** - Difficult tests indicate design problems
5. **Confidence** - Refactor fearlessly with test coverage

---

## Compose UI Testing Patterns

### Basic Test Structure

```kotlin
@RunWith(AndroidJUnit4::class)
class MyScreenTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun myTest() {
        composeTestRule.setContent {
            MyAppTheme {
                MyScreen()
            }
        }

        // Interact and assert
    }
}
```

### Finding Nodes (Finders)

**By Text**
```kotlin
composeTestRule.onNodeWithText("Hello World")
composeTestRule.onNodeWithText("Hello", substring = true, ignoreCase = true)
```

**By Content Description**
```kotlin
composeTestRule.onNodeWithContentDescription("Profile picture")
```

**By Test Tag**
```kotlin
// In composable:
Text("Hello", modifier = Modifier.testTag("greeting"))

// In test:
composeTestRule.onNodeWithTag("greeting")
```

**By Semantics Property**
```kotlin
composeTestRule.onNode(hasTestTag("greeting"))
composeTestRule.onNode(hasText("Hello"))
composeTestRule.onNode(isSelectable())
```

**Multiple Nodes**
```kotlin
composeTestRule.onAllNodesWithText("Item")
    .assertCountEquals(3)

composeTestRule.onAllNodesWithText("Item")[0]
    .performClick()
```

### Assertions

```kotlin
// Visibility
.assertExists()
.assertDoesNotExist()
.assertIsDisplayed()
.assertIsNotDisplayed()

// Text
.assertTextEquals("Expected text")
.assertTextContains("partial text")

// State
.assertIsEnabled()
.assertIsNotEnabled()
.assertIsSelected()
.assertIsNotSelected()
.assertIsOn()
.assertIsOff()

// Focus
.assertIsFocused()
.assertIsNotFocused()

// Custom
.assert(hasContentDescription("Profile"))
```

### Actions

```kotlin
// Click
.performClick()

// Text input
.performTextInput("Hello")
.performTextClearance()
.performTextReplacement("New text")

// Gestures
.performTouchInput {
    swipeLeft()
    swipeRight()
    swipeUp()
    swipeDown()
}

// Scroll
.performScrollTo()
.performScrollToIndex(5)
.performScrollToNode(hasText("Item 10"))
```

### Testing LazyColumn/LazyRow

**Basic Scrolling Test**

```kotlin
@Test
fun lazyColumn_scrollsToItem() {
    composeTestRule.setContent {
        LazyColumn(modifier = Modifier.testTag("lazy_list")) {
            items(100) { index ->
                Text(
                    text = "Item $index",
                    modifier = Modifier.testTag("item_$index")
                )
            }
        }
    }

    // Scroll to specific item
    composeTestRule
        .onNodeWithTag("lazy_list")
        .performScrollToIndex(50)

    // Verify item is visible
    composeTestRule
        .onNodeWithText("Item 50")
        .assertIsDisplayed()
}
```

**Testing Item Click in LazyColumn**

```kotlin
@Test
fun lazyColumn_clickItem_triggersCallback() {
    val clickedItems = mutableListOf<Int>()

    composeTestRule.setContent {
        LazyColumn {
            items(10) { index ->
                Text(
                    text = "Item $index",
                    modifier = Modifier
                        .testTag("item_$index")
                        .clickable { clickedItems.add(index) }
                )
            }
        }
    }

    composeTestRule
        .onNodeWithTag("item_5")
        .performClick()

    assertThat(clickedItems).containsExactly(5)
}
```

**Advanced: Scrolling with Keys**

```kotlin
@Test
fun lazyColumn_scrollToKey() {
    val items = (1..100).map { "Item $it" }

    composeTestRule.setContent {
        LazyColumn {
            items(
                items = items,
                key = { it } // Use item as key
            ) { item ->
                Text(item)
            }
        }
    }

    composeTestRule
        .onNodeWithText("Item 1", substring = false)
        .performScrollTo()
}
```

### Testing Dialogs and Modals

```kotlin
@Test
fun dialog_showsOnButtonClick_dismissesOnConfirm() {
    composeTestRule.setContent {
        var showDialog by remember { mutableStateOf(false) }

        Column {
            Button(onClick = { showDialog = true }) {
                Text("Show Dialog")
            }

            if (showDialog) {
                AlertDialog(
                    onDismissRequest = { showDialog = false },
                    title = { Text("Delete Item?") },
                    confirmButton = {
                        TextButton(onClick = { showDialog = false }) {
                            Text("Confirm")
                        }
                    }
                )
            }
        }
    }

    // Dialog not shown initially
    composeTestRule
        .onNodeWithText("Delete Item?")
        .assertDoesNotExist()

    // Click button to show dialog
    composeTestRule
        .onNodeWithText("Show Dialog")
        .performClick()

    // Dialog is now visible
    composeTestRule
        .onNodeWithText("Delete Item?")
        .assertIsDisplayed()

    // Confirm dismisses dialog
    composeTestRule
        .onNodeWithText("Confirm")
        .performClick()

    composeTestRule
        .onNodeWithText("Delete Item?")
        .assertDoesNotExist()
}
```

### Custom Semantics Properties

**When to use:** Expose test-specific data that doesn't have built-in semantics

```kotlin
// Define custom property
val PickedDateKey = SemanticsPropertyKey<Long>("PickedDate")
var SemanticsPropertyReceiver.pickedDate by PickedDateKey

// Use in composable
@Composable
fun DatePicker(
    selectedDate: Long,
    onDateSelected: (Long) -> Unit
) {
    // ... UI implementation
    Box(
        modifier = Modifier.semantics {
            pickedDate = selectedDate
        }
    ) {
        // Date picker UI
    }
}

// Test the custom property
@Test
fun datePicker_hasCorrectSelectedDate() {
    val expectedDate = 1734567890000L

    composeTestRule.setContent {
        DatePicker(selectedDate = expectedDate, onDateSelected = {})
    }

    composeTestRule
        .onNode(SemanticsMatcher.expectValue(PickedDateKey, expectedDate))
        .assertExists()
}
```

### Testing Recomposition

**Verify UI updates when state changes**

```kotlin
@Test
fun counter_incrementsOnClick() {
    composeTestRule.setContent {
        var count by remember { mutableStateOf(0) }

        Column {
            Text("Count: $count")
            Button(onClick = { count++ }) {
                Text("Increment")
            }
        }
    }

    // Initial state
    composeTestRule
        .onNodeWithText("Count: 0")
        .assertIsDisplayed()

    // Trigger recomposition
    composeTestRule
        .onNodeWithText("Increment")
        .performClick()

    // Verify recomposition
    composeTestRule
        .onNodeWithText("Count: 1")
        .assertIsDisplayed()
}
```

### Testing State Hoisting

```kotlin
@Test
fun textField_stateHoisting_updatesParent() {
    var textValue = ""

    composeTestRule.setContent {
        var text by remember { mutableStateOf("") }
        textValue = text

        OutlinedTextField(
            value = text,
            onValueChange = { text = it },
            label = { Text("Enter name") }
        )
    }

    composeTestRule
        .onNodeWithText("Enter name")
        .performTextInput("John")

    assertThat(textValue).isEqualTo("John")
}
```

### Testing with Resources (Strings, Colors)

```kotlin
@get:Rule
val composeTestRule = createAndroidComposeRule<ComponentActivity>()

@Test
fun button_usesStringResource() {
    composeTestRule.setContent {
        Button(onClick = {}) {
            Text(stringResource(R.string.continue_button))
        }
    }

    val continueLabel = composeTestRule.activity.getString(R.string.continue_button)
    composeTestRule
        .onNodeWithText(continueLabel)
        .assertIsDisplayed()
}
```

### State Restoration Testing

**Test configuration changes (rotation, process death)**

```kotlin
@Test
fun screen_restoresStateAfterRecreation() {
    val restorationTester = StateRestorationTester(composeTestRule)

    restorationTester.setContent {
        var counter by rememberSaveable { mutableStateOf(0) }

        Column {
            Text("Count: $counter")
            Button(onClick = { counter++ }) {
                Text("Increment")
            }
        }
    }

    // Modify state
    composeTestRule.onNodeWithText("Increment").performClick()
    composeTestRule.onNodeWithText("Count: 1").assertIsDisplayed()

    // Simulate configuration change
    restorationTester.emulateSavedInstanceStateRestore()

    // State should be restored
    composeTestRule.onNodeWithText("Count: 1").assertIsDisplayed()
}
```

### Testing Different Device Configurations

```kotlin
@Test
fun screen_adaptsToTabletSize() {
    composeTestRule.setContent {
        DeviceConfigurationOverride(
            DeviceConfigurationOverride.ForcedSize(DpSize(1280.dp, 800.dp))
        ) {
            ResponsiveScreen()
        }
    }

    // Assert tablet-specific UI
    composeTestRule
        .onNodeWithText("Two-pane layout")
        .assertIsDisplayed()
}

@Test
fun screen_adaptsToDarkMode() {
    composeTestRule.setContent {
        DeviceConfigurationOverride(
            DeviceConfigurationOverride.DarkMode(true)
        ) {
            MyAppTheme {
                MainScreen()
            }
        }
    }

    // Verify dark mode rendering
}
```

---

## ViewModel & StateFlow Testing

### Basic ViewModel Test Setup

```kotlin
// app/src/test/java/com/voxplanapp/viewmodel/MainViewModelTest.kt
@OptIn(ExperimentalCoroutinesApi::class)
class MainViewModelTest {
    // Use TestDispatcher for deterministic coroutine execution
    private val testDispatcher = UnconfinedTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state is correct`() = runTest {
        val repository = FakeTodoRepository()
        val viewModel = MainViewModel(repository)

        val state = viewModel.uiState.value
        assertThat(state.goals).isEmpty()
        assertThat(state.isLoading).isFalse()
    }
}
```

### Testing StateFlow with Turbine

**Why Turbine?** Simplifies testing Flow emissions with a fluent API

```kotlin
// Add to build.gradle
testImplementation("app.cash.turbine:turbine:1.1.0")
```

**Basic Turbine Test**

```kotlin
@Test
fun `loadGoals emits loading then success`() = runTest {
    val repository = FakeTodoRepository()
    val goals = listOf(
        TodoItem(id = 1, title = "Goal 1", order = 0),
        TodoItem(id = 2, title = "Goal 2", order = 1)
    )
    repository.insertItems(goals)

    val viewModel = MainViewModel(repository)

    viewModel.uiState.test {
        // First emission: initial state
        val initial = awaitItem()
        assertThat(initial.isLoading).isFalse()
        assertThat(initial.goals).isEmpty()

        // Trigger load
        viewModel.loadGoals()

        // Second emission: loading
        val loading = awaitItem()
        assertThat(loading.isLoading).isTrue()

        // Third emission: success
        val success = awaitItem()
        assertThat(success.isLoading).isFalse()
        assertThat(success.goals).hasSize(2)

        // No more emissions
        cancelAndIgnoreRemainingEvents()
    }
}
```

### Testing StateFlow Value Property (Recommended)

**For StateFlow, test the `value` property directly rather than collecting:**

```kotlin
@Test
fun `completeGoal updates state`() = runTest {
    val repository = FakeTodoRepository()
    val goal = TodoItem(id = 1, title = "Test", order = 0, isComplete = false)
    repository.insertItem(goal)

    val viewModel = MainViewModel(repository)
    viewModel.loadGoals()

    // Act
    viewModel.completeGoal(1)

    // Assert on value property
    val state = viewModel.uiState.value
    assertThat(state.goals.first().isComplete).isTrue()
}
```

### Testing Multiple StateFlows

```kotlin
@Test
fun `multiple state flows update correctly`() = runTest {
    val viewModel = FocusViewModel(repository)

    turbineScope {
        val timerFlow = viewModel.timerState.testIn(this)
        val uiFlow = viewModel.uiState.testIn(this)

        // Start timer
        viewModel.startTimer()

        // Check timer state
        assertThat(timerFlow.awaitItem().isRunning).isTrue()

        // Check UI state
        assertThat(uiFlow.awaitItem().showTimer).isTrue()

        timerFlow.cancelAndIgnoreRemainingEvents()
        uiFlow.cancelAndIgnoreRemainingEvents()
    }
}
```

### Testing ViewModel with Fake Repository

**Create a fake repository for testing:**

```kotlin
// app/src/test/java/com/voxplanapp/fakes/FakeTodoRepository.kt
class FakeTodoRepository : TodoRepository {
    private val items = mutableListOf<TodoItem>()
    private val itemsFlow = MutableStateFlow<List<TodoItem>>(emptyList())

    override fun getAllItems(): Flow<List<TodoItem>> = itemsFlow

    override fun getItemById(id: Int): Flow<TodoItem?> {
        return itemsFlow.map { list -> list.find { it.id == id } }
    }

    override suspend fun insertItem(item: TodoItem) {
        items.add(item)
        itemsFlow.value = items.toList()
    }

    override suspend fun updateItem(item: TodoItem) {
        val index = items.indexOfFirst { it.id == item.id }
        if (index != -1) {
            items[index] = item
            itemsFlow.value = items.toList()
        }
    }

    override suspend fun deleteItem(item: TodoItem) {
        items.removeIf { it.id == item.id }
        itemsFlow.value = items.toList()
    }

    // Test helper
    suspend fun insertItems(itemsList: List<TodoItem>) {
        items.addAll(itemsList)
        itemsFlow.value = items.toList()
    }
}
```

### Testing Error Handling

```kotlin
@Test
fun `loadGoals handles repository error`() = runTest {
    val repository = object : FakeTodoRepository() {
        override fun getAllItems(): Flow<List<TodoItem>> {
            return flow { throw IOException("Network error") }
        }
    }

    val viewModel = MainViewModel(repository)

    viewModel.uiState.test {
        viewModel.loadGoals()

        val errorState = awaitItem()
        assertThat(errorState.error).isNotNull()
        assertThat(errorState.error).contains("Network error")

        cancelAndIgnoreRemainingEvents()
    }
}
```

### Testing ViewModels with Dependencies

**Using MockK for complex dependencies:**

```kotlin
@Test
fun `saveGoal calls repository and shows success`() = runTest {
    val repository = mockk<TodoRepository>()
    val goal = TodoItem(id = 1, title = "Test", order = 0)

    coEvery { repository.insertItem(goal) } just Runs

    val viewModel = GoalEditViewModel(repository)
    viewModel.saveGoal(goal)

    // Verify interaction
    coVerify { repository.insertItem(goal) }

    // Verify state
    assertThat(viewModel.uiState.value.isSaved).isTrue()
}
```

### Testing Coroutine Cancellation

```kotlin
@Test
fun `cancelling ViewModel cancels ongoing operations`() = runTest {
    val repository = FakeTodoRepository()
    val viewModel = MainViewModel(repository)

    // Start long operation
    viewModel.loadGoals()

    // Cancel ViewModel scope
    viewModel.onCleared()

    // Verify no state updates after cancellation
    // (Implementation depends on ViewModel design)
}
```

---

## Room Database Testing

### In-Memory Database Setup

**Always use in-memory database for tests - data doesn't persist between tests**

```kotlin
// app/src/androidTest/java/com/voxplanapp/database/TodoDaoTest.kt
@RunWith(AndroidJUnit4::class)
class TodoDaoTest {
    private lateinit var database: AppDatabase
    private lateinit var todoDao: TodoDao

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(
            context,
            AppDatabase::class.java
        )
            .allowMainThreadQueries() // Only for tests
            .build()

        todoDao = database.todoDao()
    }

    @After
    fun closeDb() {
        database.close()
    }

    @Test
    fun insertAndRetrieveItem() = runTest {
        val item = TodoItem(id = 1, title = "Test Goal", order = 0)

        todoDao.insert(item)

        val retrieved = todoDao.getItemById(1).first()
        assertThat(retrieved).isEqualTo(item)
    }
}
```

### Testing Flow Queries

```kotlin
@Test
fun getAllItems_returnsFlowOfItems() = runTest {
    val items = listOf(
        TodoItem(id = 1, title = "Goal 1", order = 0),
        TodoItem(id = 2, title = "Goal 2", order = 1),
        TodoItem(id = 3, title = "Goal 3", order = 2)
    )

    items.forEach { todoDao.insert(it) }

    val flow = todoDao.getAllItems()

    flow.test {
        val emittedItems = awaitItem()
        assertThat(emittedItems).hasSize(3)
        assertThat(emittedItems).containsExactlyElementsIn(items)

        cancelAndIgnoreRemainingEvents()
    }
}
```

### Testing Flow Updates

**Verify Flow re-emits when data changes:**

```kotlin
@Test
fun getAllItems_emitsUpdates_whenDataChanges() = runTest {
    val job = launch {
        todoDao.getAllItems().collect { items ->
            // Collect in background
        }
    }

    todoDao.getAllItems().test {
        // Initial emission: empty
        assertThat(awaitItem()).isEmpty()

        // Insert item
        todoDao.insert(TodoItem(id = 1, title = "New Goal", order = 0))

        // Flow re-emits with new item
        val updated = awaitItem()
        assertThat(updated).hasSize(1)

        cancelAndIgnoreRemainingEvents()
    }

    job.cancel()
}
```

### Testing Transactions

```kotlin
@Test
fun transaction_rollsBackOnError() = runTest {
    val items = listOf(
        TodoItem(id = 1, title = "Goal 1", order = 0),
        TodoItem(id = 2, title = "Goal 2", order = 1)
    )

    try {
        database.withTransaction {
            items.forEach { todoDao.insert(it) }
            // Simulate error
            throw Exception("Test error")
        }
    } catch (e: Exception) {
        // Expected
    }

    // Verify rollback - no items inserted
    val allItems = todoDao.getAllItems().first()
    assertThat(allItems).isEmpty()
}
```

### Testing Foreign Key Constraints

```kotlin
@Test
fun deletingGoal_cascadesDeleteToQuota() = runTest {
    val goal = TodoItem(id = 1, title = "Goal", order = 0)
    val quota = Quota(
        id = 1,
        goalId = 1,
        dailyMinutes = 60,
        activeDays = "1111111"
    )

    todoDao.insert(goal)
    quotaDao.insert(quota)

    // Delete goal
    todoDao.delete(goal)

    // Quota should be cascade deleted
    val remainingQuotas = quotaDao.getAllQuotas().first()
    assertThat(remainingQuotas).isEmpty()
}
```

### Testing Database Migrations

```kotlin
@RunWith(AndroidJUnit4::class)
class MigrationTest {
    @get:Rule
    val helper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        AppDatabase::class.java
    )

    @Test
    fun migrate12To13_preservesData() {
        // Create database with version 12
        helper.createDatabase(TEST_DB_NAME, 12).apply {
            execSQL("INSERT INTO todo_items (id, title, `order`) VALUES (1, 'Test', 0)")
            close()
        }

        // Run migration
        helper.runMigrationsAndValidate(TEST_DB_NAME, 13, true, MIGRATION_12_13)

        // Verify data preserved
        val db = helper.runMigrationsAndValidate(TEST_DB_NAME, 13, true)
        val cursor = db.query("SELECT * FROM todo_items WHERE id = 1")
        assertThat(cursor.moveToFirst()).isTrue()
        assertThat(cursor.getString(cursor.getColumnIndex("title"))).isEqualTo("Test")
        cursor.close()
    }

    companion object {
        private const val TEST_DB_NAME = "migration-test"
    }
}
```

### Testing Repository with Room

```kotlin
@RunWith(AndroidJUnit4::class)
class TodoRepositoryIntegrationTest {
    private lateinit var database: AppDatabase
    private lateinit var repository: TodoRepository

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()

        repository = TodoRepositoryImpl(database.todoDao())
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun insertAndRetrieve_worksCorrectly() = runTest {
        val item = TodoItem(id = 1, title = "Test", order = 0)

        repository.insertItem(item)

        repository.getItemById(1).test {
            val retrieved = awaitItem()
            assertThat(retrieved).isEqualTo(item)
            cancelAndIgnoreRemainingEvents()
        }
    }
}
```

### Testing with Test Dispatchers

**For deterministic async testing:**

```kotlin
@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(AndroidJUnit4::class)
class TodoDaoAsyncTest {
    private lateinit var database: AppDatabase
    private lateinit var todoDao: TodoDao
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)

        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .setTransactionExecutor(testDispatcher.asExecutor())
            .setQueryExecutor(testDispatcher.asExecutor())
            .build()

        todoDao = database.todoDao()
    }

    @After
    fun tearDown() {
        database.close()
        Dispatchers.resetMain()
    }

    @Test
    fun testWithControlledExecution() = runTest(testDispatcher) {
        val item = TodoItem(id = 1, title = "Test", order = 0)

        todoDao.insert(item)
        testDispatcher.scheduler.advanceUntilIdle()

        val retrieved = todoDao.getItemById(1).first()
        assertThat(retrieved).isEqualTo(item)
    }
}
```

---

## Navigation Testing

### Basic Navigation Test with Compose

```kotlin
@RunWith(AndroidJUnit4::class)
class NavigationTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    private lateinit var navController: TestNavHostController

    @Before
    fun setupNavHost() {
        composeTestRule.setContent {
            navController = TestNavHostController(LocalContext.current).apply {
                navigatorProvider.addNavigator(ComposeNavigator())
            }

            MyAppNavHost(navController = navController)
        }
    }

    @Test
    fun navHost_verifyStartDestination() {
        composeTestRule
            .onNodeWithText("Main Screen")
            .assertIsDisplayed()

        val route = navController.currentBackStackEntry?.destination?.route
        assertThat(route).isEqualTo("main")
    }
}
```

### Testing Navigation Actions

```kotlin
@Test
fun clickGoalItem_navigatesToEditScreen() {
    // Verify we're on main screen
    assertThat(navController.currentBackStackEntry?.destination?.route)
        .isEqualTo("main")

    // Click goal item
    composeTestRule
        .onNodeWithText("Goal 1")
        .performClick()

    // Verify navigation to edit screen
    val route = navController.currentBackStackEntry?.destination?.route
    assertThat(route).isEqualTo("edit/{goalId}")

    // Verify argument passed
    val goalId = navController.currentBackStackEntry?.arguments?.getInt("goalId")
    assertThat(goalId).isEqualTo(1)
}
```

### Testing Back Navigation

```kotlin
@Test
fun clickBack_navigatesToPreviousScreen() {
    // Navigate to edit screen
    composeTestRule.runOnUiThread {
        navController.navigate("edit/1")
    }

    // Verify we're on edit screen
    composeTestRule
        .onNodeWithText("Edit Goal")
        .assertIsDisplayed()

    // Press back
    composeTestRule
        .onNodeWithContentDescription("Navigate up")
        .performClick()

    // Verify we're back on main screen
    assertThat(navController.currentBackStackEntry?.destination?.route)
        .isEqualTo("main")
}
```

### Testing Deep Links

```kotlin
@Test
fun deepLink_navigatesToCorrectDestination() {
    val uri = Uri.parse("voxplan://goal/5")

    composeTestRule.runOnUiThread {
        navController.navigate(uri)
    }

    val route = navController.currentBackStackEntry?.destination?.route
    assertThat(route).isEqualTo("edit/{goalId}")

    val goalId = navController.currentBackStackEntry?.arguments?.getInt("goalId")
    assertThat(goalId).isEqualTo(5)
}
```

### Testing with Type-Safe Navigation (Navigation 2.8+)

```kotlin
@Serializable
data class EditGoal(val goalId: Int)

@Test
fun navigateWithTypeSafeRoute() {
    composeTestRule.runOnUiThread {
        navController.navigate(EditGoal(goalId = 10))
    }

    val hasRoute = navController.currentBackStackEntry?.destination?.hasRoute<EditGoal>()
    assertThat(hasRoute).isTrue()
}
```

---

## Testing Best Practices

### 1. Test Organization

**Follow the AAA pattern:**
- **Arrange** - Set up test data and dependencies
- **Act** - Execute the code under test
- **Assert** - Verify the outcome

```kotlin
@Test
fun example_followsAAAPattern() {
    // Arrange
    val repository = FakeTodoRepository()
    val viewModel = MainViewModel(repository)

    // Act
    viewModel.loadGoals()

    // Assert
    assertThat(viewModel.uiState.value.goals).isNotEmpty()
}
```

### 2. Test Naming

**Use descriptive names that explain what is being tested:**

```kotlin
// ✓ Good
@Test
fun `clicking complete button marks goal as complete`()

@Test
fun `when repository throws error, UI shows error message`()

@Test
fun deleteGoal_removesGoalFromList()

// ✗ Bad
@Test
fun test1()

@Test
fun testGoals()
```

### 3. Test Independence

**Each test should be independent and not rely on other tests:**

```kotlin
// ✓ Good - each test sets up its own data
class GoalListTest {
    private lateinit var repository: FakeTodoRepository

    @Before
    fun setup() {
        repository = FakeTodoRepository()
    }

    @Test
    fun test1() {
        val goal = TodoItem(id = 1, title = "Goal 1", order = 0)
        repository.insertItem(goal)
        // Test with this goal
    }

    @Test
    fun test2() {
        val goal = TodoItem(id = 2, title = "Goal 2", order = 0)
        repository.insertItem(goal)
        // Test with this goal (independent of test1)
    }
}
```

### 4. Use Fakes Over Mocks When Possible

**Fakes are simpler and more reliable:**

```kotlin
// ✓ Good - Fake repository
class FakeTodoRepository : TodoRepository {
    private val items = mutableListOf<TodoItem>()

    override suspend fun insertItem(item: TodoItem) {
        items.add(item)
    }

    override fun getAllItems() = flowOf(items.toList())
}

// ✗ Acceptable but more complex - Mock
val mockRepository = mockk<TodoRepository>()
coEvery { mockRepository.getAllItems() } returns flowOf(emptyList())
```

### 5. Test Edge Cases

```kotlin
@Test
fun `empty list shows empty state`()

@Test
fun `single item displays correctly`()

@Test
fun `maximum items (1000) renders without crash`()

@Test
fun `null values handled gracefully`()

@Test
fun `network error shows error message`()
```

### 6. Avoid Testing Implementation Details

```kotlin
// ✗ Bad - testing internal state
@Test
fun viewModel_hasCorrectPrivateVariable() {
    // Don't test private variables
}

// ✓ Good - testing observable behavior
@Test
fun viewModel_exposesCorrectUiState() {
    val state = viewModel.uiState.value
    assertThat(state.goals).hasSize(5)
}
```

### 7. Keep Tests Fast

```kotlin
// Use fake/in-memory implementations
// Avoid real network calls
// Avoid Thread.sleep() - use test dispatchers instead

// ✗ Bad
@Test
fun slowTest() {
    Thread.sleep(5000) // Don't do this
}

// ✓ Good
@Test
fun fastTest() = runTest {
    testDispatcher.scheduler.advanceTimeBy(5000)
}
```

### 8. Test One Thing at a Time

```kotlin
// ✗ Bad - testing multiple behaviors
@Test
fun complexTest() {
    viewModel.loadGoals()
    viewModel.completeGoal(1)
    viewModel.deleteGoal(2)
    // Too many things in one test
}

// ✓ Good - focused tests
@Test
fun loadGoals_populatesUiState()

@Test
fun completeGoal_updatesGoalStatus()

@Test
fun deleteGoal_removesFromList()
```

### 9. Use Test Fixtures

```kotlin
object TestFixtures {
    fun createGoal(
        id: Int = 1,
        title: String = "Test Goal",
        order: Int = 0,
        isComplete: Boolean = false
    ) = TodoItem(id, title, order, isComplete)

    fun createGoalList(count: Int) = (1..count).map { createGoal(id = it) }
}

// Use in tests
@Test
fun test() {
    val goal = TestFixtures.createGoal(title = "Custom Title")
}
```

### 10. Clean Up Resources

```kotlin
@After
fun cleanup() {
    database.close()
    // Cancel coroutines
    // Release resources
}
```

---

## Complete Code Examples

### Example 1: Complete Feature with TDD

**Feature: Todo Item Completion Tracking**

**Step 1: Write Entity Test**

```kotlin
// Entity is simple data class, minimal testing needed
@Test
fun todoItem_toggleComplete_updatesStatus() {
    val item = TodoItem(id = 1, title = "Test", order = 0, isComplete = false)
    val completed = item.copy(isComplete = true)

    assertThat(completed.isComplete).isTrue()
}
```

**Step 2: Write DAO Test (RED)**

```kotlin
@RunWith(AndroidJUnit4::class)
class TodoDaoTest {
    private lateinit var database: AppDatabase
    private lateinit var todoDao: TodoDao

    @Before
    fun createDb() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AppDatabase::class.java
        ).build()
        todoDao = database.todoDao()
    }

    @After
    fun closeDb() {
        database.close()
    }

    @Test
    fun updateItem_changesCompleteStatus() = runTest {
        // Arrange
        val item = TodoItem(id = 1, title = "Test", order = 0, isComplete = false)
        todoDao.insert(item)

        // Act
        val updated = item.copy(isComplete = true)
        todoDao.update(updated)

        // Assert
        val retrieved = todoDao.getItemById(1).first()
        assertThat(retrieved?.isComplete).isTrue()
    }
}
```

**Step 3: Implement DAO (GREEN)**

```kotlin
@Dao
interface TodoDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: TodoItem)

    @Update
    suspend fun update(item: TodoItem)

    @Query("SELECT * FROM todo_items WHERE id = :id")
    fun getItemById(id: Int): Flow<TodoItem?>
}
```

**Step 4: Write Repository Test (RED)**

```kotlin
class TodoRepositoryTest {
    private lateinit var database: AppDatabase
    private lateinit var repository: TodoRepository

    @Before
    fun setup() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AppDatabase::class.java
        ).build()
        repository = TodoRepositoryImpl(database.todoDao())
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun completeItem_updatesDatabase() = runTest {
        // Arrange
        val item = TodoItem(id = 1, title = "Test", order = 0, isComplete = false)
        repository.insertItem(item)

        // Act
        repository.completeItem(1)

        // Assert
        repository.getItemById(1).test {
            val updated = awaitItem()
            assertThat(updated?.isComplete).isTrue()
            cancelAndIgnoreRemainingEvents()
        }
    }
}
```

**Step 5: Implement Repository (GREEN)**

```kotlin
class TodoRepositoryImpl(private val dao: TodoDao) : TodoRepository {
    override suspend fun insertItem(item: TodoItem) = dao.insert(item)

    override fun getItemById(id: Int) = dao.getItemById(id)

    override suspend fun completeItem(id: Int) {
        val item = dao.getItemById(id).first()
        item?.let {
            dao.update(it.copy(isComplete = true))
        }
    }
}
```

**Step 6: Write ViewModel Test (RED)**

```kotlin
class MainViewModelTest {
    private val testDispatcher = UnconfinedTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `completeGoal updates UI state`() = runTest {
        // Arrange
        val repository = FakeTodoRepository()
        val goal = TodoItem(id = 1, title = "Test", order = 0, isComplete = false)
        repository.insertItem(goal)

        val viewModel = MainViewModel(repository)
        viewModel.loadGoals()

        // Act
        viewModel.completeGoal(1)

        // Assert
        val state = viewModel.uiState.value
        assertThat(state.goals.first().isComplete).isTrue()
    }
}
```

**Step 7: Implement ViewModel (GREEN)**

```kotlin
class MainViewModel(
    private val repository: TodoRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(MainUiState())
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

    fun loadGoals() {
        viewModelScope.launch {
            repository.getAllItems().collect { goals ->
                _uiState.value = _uiState.value.copy(goals = goals)
            }
        }
    }

    fun completeGoal(id: Int) {
        viewModelScope.launch {
            repository.completeItem(id)
        }
    }
}

data class MainUiState(
    val goals: List<TodoItem> = emptyList(),
    val isLoading: Boolean = false
)
```

**Step 8: Write UI Test (RED)**

```kotlin
@RunWith(AndroidJUnit4::class)
class MainScreenTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun clickCompleteButton_marksGoalComplete() {
        // Arrange
        val repository = FakeTodoRepository()
        val goal = TodoItem(id = 1, title = "Test Goal", order = 0, isComplete = false)
        runBlocking { repository.insertItem(goal) }

        val viewModel = MainViewModel(repository)

        composeTestRule.setContent {
            MainScreen(viewModel = viewModel)
        }

        // Act
        composeTestRule
            .onNode(hasText("Test Goal") and hasContentDescription("Mark complete"))
            .performClick()

        // Assert
        composeTestRule.waitUntil(timeoutMillis = 2000) {
            viewModel.uiState.value.goals.first().isComplete
        }
    }
}
```

**Step 9: Implement UI (GREEN)**

```kotlin
@Composable
fun MainScreen(viewModel: MainViewModel = viewModel()) {
    val uiState by viewModel.uiState.collectAsState()

    LazyColumn {
        items(uiState.goals) { goal ->
            GoalItem(
                goal = goal,
                onComplete = { viewModel.completeGoal(goal.id) }
            )
        }
    }
}

@Composable
fun GoalItem(goal: TodoItem, onComplete: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = goal.title,
            style = if (goal.isComplete) {
                MaterialTheme.typography.bodyLarge.copy(
                    textDecoration = TextDecoration.LineThrough
                )
            } else {
                MaterialTheme.typography.bodyLarge
            }
        )

        IconButton(
            onClick = onComplete,
            modifier = Modifier.semantics {
                contentDescription = "Mark complete"
            }
        ) {
            Icon(
                imageVector = if (goal.isComplete) {
                    Icons.Default.CheckCircle
                } else {
                    Icons.Default.RadioButtonUnchecked
                },
                contentDescription = null
            )
        }
    }
}
```

**Step 10: Refactor with Tests Green**

---

### Example 2: Testing Complex State Flow

```kotlin
class FocusViewModelTest {
    private val testDispatcher = UnconfinedTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `timer starts and increments elapsed time`() = runTest {
        val repository = FakeTodoRepository()
        val viewModel = FocusViewModel(repository)

        viewModel.timerState.test {
            // Initial state
            val initial = awaitItem()
            assertThat(initial.elapsedSeconds).isEqualTo(0)
            assertThat(initial.isRunning).isFalse()

            // Start timer
            viewModel.startTimer()

            val running = awaitItem()
            assertThat(running.isRunning).isTrue()

            // Advance time
            testDispatcher.scheduler.advanceTimeBy(1000)

            val afterOneSecond = awaitItem()
            assertThat(afterOneSecond.elapsedSeconds).isEqualTo(1)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `pause timer stops incrementing`() = runTest {
        val viewModel = FocusViewModel(FakeTodoRepository())

        viewModel.startTimer()
        testDispatcher.scheduler.advanceTimeBy(5000)

        viewModel.pauseTimer()

        val pausedTime = viewModel.timerState.value.elapsedSeconds

        testDispatcher.scheduler.advanceTimeBy(5000)

        // Time should not advance while paused
        assertThat(viewModel.timerState.value.elapsedSeconds).isEqualTo(pausedTime)
    }
}
```

---

## Summary Checklist

### Before Writing Code

- [ ] Write test that fails (RED)
- [ ] Test describes desired behavior
- [ ] Test is focused and tests one thing

### After Writing Code

- [ ] Test passes (GREEN)
- [ ] Code is minimal to pass test
- [ ] No premature optimization

### Refactoring

- [ ] All tests still pass
- [ ] Code is clean and maintainable
- [ ] No duplication

### Test Quality

- [ ] Tests are independent
- [ ] Tests use descriptive names
- [ ] Tests use AAA pattern
- [ ] Tests are fast
- [ ] Edge cases covered

### Coverage

- [ ] Unit tests for ViewModels
- [ ] Instrumented tests for UI
- [ ] Integration tests for Database
- [ ] Navigation tests for flows

---

## Resources & References

### Official Documentation
- [Testing in Jetpack Compose](https://developer.android.com/codelabs/jetpack-compose-testing)
- [Common Testing Patterns](https://developer.android.com/develop/ui/compose/testing/common-patterns)
- [Testing Cheatsheet](https://developer.android.com/develop/ui/compose/testing/testing-cheatsheet)
- [Testing Kotlin Flows](https://developer.android.com/kotlin/flow/test)
- [Test Room Databases](https://developer.android.com/training/data-storage/room/testing-db)
- [Navigation Testing](https://developer.android.com/guide/navigation/testing)

### Libraries
- [Turbine - Flow Testing](https://github.com/cashapp/turbine)
- [MockK - Mocking Library](https://mockk.io/)
- [Truth - Fluent Assertions](https://truth.dev/)

### Best Practices Articles
- [Building High-Quality Android UI with TDD](https://www.droidcon.com/2023/07/26/building-high-quality-android-ui-embracing-test-driven-development-with-jetpack-compose/)
- [Testing Android Flows with Turbine](https://proandroiddev.com/testing-android-flows-in-viewmodel-with-turbine-ea9bae7e811a)
- [Testing Room with Kotlin Coroutines](https://medium.com/@eyalg/testing-androidx-room-kotlin-coroutines-2d1faa3e674f)

---

**Next Steps:**

1. Set up test dependencies in your project
2. Create test directory structure
3. Start with simple ViewModel tests
4. Add UI tests for critical user flows
5. Build up test coverage incrementally
6. Run tests on CI/CD pipeline

**Remember:** TDD is a discipline that takes practice. Start small, be consistent, and watch your code quality improve over time.
