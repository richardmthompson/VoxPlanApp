### version tracker

16/06:	1.0 - represents a todo item
	1.1 - edit todo item
	1.2 - show multiple todo items in a list
	1.3 - top app bar
28/06:  1.4 - move up, down hierarchy & vertically
	1.5 - represent multi layer hierarchy of goals
17/07:	1.6 - represent multi layer goals with breadcrumbs 
28/07: 	2.1 - with schedule screen, bot nav bar, sample event.
	2.2 - event table in db, create today events from edit screen
07/08	2.3 - events creatable from goals on today's day scheduler
13/08	2.4 - event icons on event selection
28/08	2.5 - with focus screen and focus timer!
18/09	2.6 - power bar, time banking
	2.7 - completed tasks, Diamond awards for power bar achievements
<todo>
3/12	3.0 - daily quotas
</todo>



# functionality

__version_1__

* stores and represents recursive layers of goals and sub-goals

* goals can be moved up and down:
	- vertically within current list
	- hierarchically across levels

* breadcrumbs tracks current hierarchy

* save goals across quits

* floating action button for adding goals


__version_2__

* day scheduling screen

* navigation bar

* month and week view

* focus mode with pomodoro

* accruing time into time-bank

* showing accrued time in power bar

* completing tasks accrues 15m time bank, completed tasks disappear on following day.

* diamond display when daily time focus quota achieved!

<todo>

__version_3__

* set goal-specific time quotas for daily basis

* view progress on a weekly-screen for daily quotas

* initial reactive agent-rules:
	- schedule today's activities based on quotas
		* (what other rules are required for this?)
		* establish other fixed events, e.g. meal-times
	

__version_4__

* ai voice controlled assistant installation

* voice recognition for:
	& goal entry
	& scheduling


__version_5__

* load/retrieve goals to/from cloud storage

* connection to desktop version of voxplan


__version_6__

* intelligent ai-driven process

* voxplan can call on whatsapp and conduct a 'secretary convo', then subsequently update functioning of app accordingly.

</todo>



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
	
# Detailed Explanation of Key Features


<FOCUS MODE SINGLE>   - WHEN A GOAL WITH NO SUB-GOALS IS SCHEDULED.

  - FOCUS MODE -
  start		end
  7am		8am

0700 ___________________
     |  PROGRAMMING    |-
     |_________________|+
1000

Total:	3 hours 0 mins
Split:	Work: 25  mins		// Timer will say GO for 25 mins,
	Rest: 5   mins		// REST for 5 mins, e.g.
Blocks:	6 work blocks

				// Once we enter 'Go' mode, the icons
       30			// and + / - signs disappear, and 
   ____|____			// the clock starts counting.
  /    |    \	   X   QUIT
25     |     5			Blocks accrued: 
 |    GO!    |	   >>   FF	3  ***	(90 mins)
20          10
  \         /	   ||  PAUSE
   ----|----
       15

* tasks are either TIME-BASED tasks (i.e. the task is to spend a certain amount of time doing them) or ACTION-BASED tasks (i.e. the task is to complete an action).

* for an 'ongoing' task, that involves spending time on the goal without any defined sub-goal, this may be 'practice,' or 'playing', 'dancing,' whatever, and it has no end-point - the goal is to simply do the activity, wwhatever it is, and accrue time points.

-> in this instance, in focus mode, the time is accumulating, and when done, the time is accrued in the time-vault for this activity.


# summary 21 jun 2024

## Developer overview

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

