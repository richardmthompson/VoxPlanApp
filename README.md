# functionality

* stores and represents recursive layers of goals and sub-goals
* goals can be moved up and down:
	- vertically within current list
	- hierarchically across levels
* breadcrumbs tracks current hierarchy

# current plan

# next up

* what about representing existing data structures?? -> i.e. from python program, we do use infinitely recursive goal-nodes.


# stages of implementation

__mirror voxplan data structure__

* goal_id, summary, details, + hierarchical structure for gui representation...
# done


__destination screens__

* goal tree screen
	- view different hierarchical levels
# done
	
* goal add/edit details screen
# done

* daily planner screen

* task focus screen 
	-> prevent access to other screens

* gamified success accruement screen


__step 1__

* base screen with 3-5 goals
* sub-goals
* add/edit goal screen
# done

__step 2__

* represent sub-goals on main screen
* save goals across quits
* floating action button for adding goals

__step 3__

* day scheduling screen
* navigation bar
* month and week view

__step4__

* focus mode (?)

__step 5__

* load/retrieve goals to/from cloud storage
* connection to desktop python
* voice recognition for goal entry

__step 6__

* chat bot incorporated into adding goals


# application components

__time_planning__

1. Bed time / wake up time --> set up daily screen

2. morning routine (things to do when i wake up): when, how long, what, what sequence.
	-> time of main work block?  Other time-blocks?
	-> set up in diary
	* what does this evening look like?
	* are there other evenings in the week that are similar to this?
		-> copy over, then refine afterwards
	* loop through every day of the week, evening (dinner onwards)
		
	-> click to gamify / suggest or ask (are you doing this now?) - adjust schedule as needed
	
3. things to do before bed
	-> set up night time sequence

4. things I want to do later
	-> set up reminders
	-> onClick (notification): do you want to (a) schedule this now? (b) be reminded again later?
		:: I will take action now.  requires action -> stay active until re-open app


__goal_management__

1. create hierarchical goal screens
# done

2. per day / per week quotas

3. scheduling of activities

4. gamified rewards for time spent

5. week-end review of time-spent

6. review of strategies

7. coaching conversation <-> compare existing goal structures <-> decision tree <-> modify / add / remove / goals from / to hierarchy


__focus_mode__

1. time-slot determined by #time_planning, 
	& organised into pomodoro / suitable rest-work periods
	& represented as 'focus-mode' - (to prevent phone use during focus periods)
	

__time_tracking__

(advanced)

1. check-in: "are you doing this now?" -> adjust schedule accordingly

2. voice notify -> upcoming schedule (perform 'phone call')

3. ^^ #1 check-in but with voice!

4. adjustment of schedule according to actual behaviour


# ai assistant spec

* morning 'phone call' : sets the plan for the day, goes through the proposed schedule, makes alterations where necessary (conversational & with corresponding highlights of the app where relevant)


# app definition

* __Android gamified planning and focusing app__
	- Enables navigation and editing of goal structures
	- Allows selection of a pre-existing goal and sequencing into timeline of day
	- 'Focus mode' for time-periods of increased focus
	- Gamified accruement of 'time-points'

* task list app
* connect to desktop python <-> goal structures
* gamify
* focus mode

# feature break-down

__Main screen:__

* mvp: goal-nodes navigator
	- can be simple list of goals, with sub-goals listed (to start with)
	- eventually will have more intuitive interface

* hierarchical structure, as in voxplan.py
	- navigation 'down' and 'up' into and out-of sub-goals

* goal-edit mode

__sequencer__

* sequence sub-goal list:
	- define a time for a sequence to start,
	- enter focus mode,
	- click 'finished' - app logs time spent on subtask
	- then moves on to next task immediately in focus mode.
	- * can skip tasks*
	- good for known sequences, e.g. morning routine, etc.
	- can also provide time-limits to sub-elements (time-goals etc.) - focus mode then includes count-down for each element.
	- show progress in list in real time (could be an expandable region in the screento give context, est'd time of the whole structure etc.
	
	
* day-sequencing and timing-manager

* focus mode

* gamification:
	+ building up of points per sub-goal and goal (over the week)
	

# summary 21 jun 2024

Application Overview:
The application is a goal management system built using Kotlin and Jetpack Compose for Android. It allows users to create, manage, and organize hierarchical goals and subgoals.

Architecture:
- The app follows the MVVM (Model-View-ViewModel) architecture.
- It uses Room for local database storage.
- Kotlin Coroutines and Flow are used for asynchronous operations and reactive programming.

Key Components:

1. Data Layer:
   - TodoItem: The main data class representing a goal or subgoal.
   - TodoDao: Data Access Object for database operations.
   - TodoRepository: Mediates between the ViewModel and the database.

2. ViewModel:
   - MainViewModel: Manages the UI state and business logic for the main screen.

3. UI Layer (Composables):
   - MainScreen: The main container composable for the app's primary interface.
   - GoalListContainer: Displays the list of goals and subgoals.
   - GoalItem: Represents an individual goal in the list.
   - SubGoalItem: Represents a subgoal within a goal.
   - TodoInputBar: Allows users to input new goals.
   - ReorderButtons: UI elements for reordering goals.

Key Files:
- MainScreen.kt: Contains the main UI composables.
- MainViewModel.kt: Contains the ViewModel for the main screen.
- TodoRepository.kt: Implements the repository pattern for data operations.
- TodoDao.kt: Defines database operations.
- AppDatabase.kt: Sets up the Room database.

Workflow:
1. The app starts at the MainScreen.
2. Goals and subgoals are fetched from the repository and displayed in a list.
3. Users can add new goals using the TodoInputBar.
4. Goals can be expanded to show subgoals.
5. Users can edit, delete, or reorder goals.

Current Implementation Details:
- Goals are stored in a Room database.
- The UI observes changes in the data through StateFlows.
- Reordering of goals is currently being implemented, focusing on top-level goals first.

Challenges and Upcoming Features:
- Implementing a robust reordering system for both top-level goals and subgoals.
- Considering the addition of an 'order' field to the database schema to support reordering.
- Enhancing the UI to better represent the hierarchical nature of goals and subgoals.

Data Flow:
1. User interactions in the UI trigger functions in the ViewModel.
2. The ViewModel interacts with the Repository to perform data operations.
3. The Repository uses the DAO to interact with the Room database.
4. Changes in the database are observed via Flows, updating the UI reactively.

Key Composables in Detail:
- MainScreen: Orchestrates the overall layout, including the top app bar, goal list, and input bar.
- GoalListContainer: A scrollable container that renders GoalItems.
- GoalItem: Displays a goal's title, completion status, and handles expansion to show subgoals.
- SubGoalItem: Similar to GoalItem but represents a subgoal and doesn't allow further nesting.
- ReorderButtons: Provides UI controls for reordering goals.

The application aims to provide a flexible, hierarchical goal management system with an intuitive UI and smooth user experience. The current focus is on implementing and refining the goal reordering functionality, starting with top-level goals and potentially expanding to handle subgoal reordering in the future.

