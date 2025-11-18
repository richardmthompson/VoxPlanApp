# Voxplan to do
--
# 6.30 - 10.30 every day maximum focus
--

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
	2.7 - completed tasks & Diamond awards for power bar achievements
10/1	3.0 - quotas, quota screen etc.
20/1	3.1 - dailies (beta) & release/debug bifurcation
29/01	3.2 - dailies with scheduled boxes


-------------
# Vox Apps #
-------------

## decision maker
* two sides of decision
* pros and cons
* voice

## manifestor
* 5 current manifestations
* well formed outcome features
* voice

## questions & answers:
- define a list of questions, with date we want the answer by
- once the date arrives, the question pops up, and asks if it's been answered
- better with voice (more simple implementation than voxplan etc.)

## annotator
- with different voice note items in a list


## vox OS
- ?

--------


## how can voxplan... ?

* support me to meditate 3 x per day

* schedule and facilitate an immediate activity, and remind me to do a bit more of the same activity later in the day

* help me complete my quota'd activities every day?


###############################
############ #NOW #################


# -- START

## PRODUCTION GOALS

* user records, read up on benefits of creating user profiles.

* working functionality:
	__minimal_functionality__
	:: goals to dailies to schedule
	:: goals / dailies / schedule to focus mode
	:: focus mode to schedule retrospective
	:: time-banking, quota functions working

	__nice_to_have__
	:: weekly summary of what's been accomplished (?)




# still a Q;
* what to do with short (1/2hr and less) tasks - difficult to see text when small bar is in schedule.
	- POSSIBLY: 'tooltip': name of task, start - end, duration

* allow drop down to appear in sub-items.

* only allow one drop-down at once on screen.

* completed in goal edit doesn't correspond to completed from dropdown

* review scheduling process

* multi-planned activities.
- can select multiple activities and run them in sequence.
- focus mode shows sequential activities, can:
	* FF to next
	* Activate open-ended timer (accrues time auto on FF) ->into medals area.
	* selected current activity in event boxes at top, to show sequence.
	* saves spent time into time bank 
		-> question: what hierarchy would these events exist inside of, and what would be saved in time bank as the target goal?  These daily tasks may not form part of the goal hierarchy.  But maybe we have to select a 'parent goal' that receives the time accruement.  Makes sense because every task we have should have its basis in a bigger goal.

### CURRENT CURRENT CURRENT ###

# !! currently in play --->

* bug: when reducing minutes in goal edit scren, should reduce to nearest rounded amount, then in increemnts, and not beyond 0.

# goal into daily

## simple daily structure.  one task one daily one scheduled event.

* need state variable for quota boxes so they update dynamically according to scheduled and completed.


## more complex daily structure to examine.
let's say i have a goal, coding, that i want to accomplish today.

i add it as a daily, using the quota button (because there's a quota of 4 hours set for the goal).  it appears on the daily screen and hasn't yet been scheduled.



# scheduling is brokeN!


* set quota duration when adding a new task to dailies.
	: we need an estimate of the amount of time when adding.

	: dailies 
	-> set duration of task.  (apply back to goal as default task length)
	-> schedule in time-chunks.


1. create daily list of things needing to be done today
	- from quotas
	- from goals... ?
	- add new daily only as task!!
	

2. schedule from dailies into schedule

3. focus mode from schedule

4. view achieved quotas

5. check achievement of today's dailies

6. make dailies for tomorrow and beyond


* smalls
	: focus mode 3min -> 90 sec
	
	: goaledit: store default task length?
	: progress: minimise days by default,
		- open and save open state.




__RULES_FOR_VOXPLAN__

* dailies must be accounted for by end of day

* achievement of quotas should be viewable and rewarded accurately

* schedule should represent actual time spent

* banked time should be accrued and represented / gamified somehow



* (schedule today) button - decision process of app to take dailies and put them into the schedule.


* today's list:
	<schedule today's activities into the diary quickly and easily>
	- flag item
	- estimate time
	- flagged items screen (today's list)


* order importance of quota listing on progress screen:
	- e.g. meditation is top, coding is second.
	  (helps for scheduling)

* bottom bar selection of progress icon (small debugging)

* correctly displaying smaller quotas than 1 hour.

* BRINGING THE QUOTAS into focus for daily & hourly accomplishments
 - prioritising, scheduling and focusing on things that should be done daily

* set as a quota a yes/no accomplishment, not time based.

* being able to 'tick something off the list' for the day
	- create a today task screen and populate it with instances (events or some other structure?)

* desired frequency -> daily
	-> when completed, is completed for day only.
	-> generate list of tasks for the day
* per day accomplishments
* to-do's - 'schedule x tomorrow'   --> what happens to this , what is this function?


* daily / weekly / monthyly quotas
# weekly yes - daily & monthly no

* weeklies - amounts of things that should be done weekly


__scheduling__
	
* scheduling:
	- select time block
	- select items to be done
	- work back from schedule into focus mode.


* 'ive got 2 hours' -> schedule -> focus mode
	- create the sense of being 'locked in for 2. hours eg.'
	- from main screen -> goal edit screen -> schedule.
	- separate schedule screen?

* fill gap from now until next planned activity, then enter focus for that time, e.g. next 2.5 hours until foot massage, = 5 x pomos.

--> in this case, top representation of goal should be divided by x number, and colour-code count along...
	grey, to orange, to green...

* can we schedule without having a goal?  e.g. foot massage.

* retroactive from focus mode into scheduler..

(same for tomorrow -> planning ->
* schedule for tomorrow

* e.g. 4 pomo's in row
	
* 4 hour achievement - power bar transforms to crystal / mushroom
	- and how is this recognised later on??

* show this weeks accomplishments



# SMALL UI IMPROVEMENTS

* focus mode - change position of numbers on clock so we can see how long the period goes for.

! display coin in event to show accrued time?  (future UI)



__multi_focus__
* use for selecting and checking off sequences of e.g. morning routine exerises, within an allotted timeframe.

* schedule sequence
- schedule action into drop down box

* also : a 'model sequence' from scheduler
-> can 'record' a sequence and repeat it on demand.
	
* scheduling the day
* managing "today's tasks"

* show scheduling detail by zoom

* update schedule with resulting count.

* categories for tasks



__other_ideas__

* brain dump voice button
- for ideas on tasks to process later

* focus sound-tracks

* use for a 24 hour fast, e.g.





# QUESTION! #
* what about small todo's, e.g. buy curtains / check bank acc., etc.
* should be inserted somewhere into the day as 'general todos?'
* they don't really deserve a place in the project hierarchies, more just a list of things that need doing at some point, and need to be scheduled.
* once added a todo-item, VP asks when to do it, possibly showing day-schedule, and inserting a half-hour 'todo-item' block somewhere, unless there is already a half-hour todo-item block, in which case, it adds it to the existing event.
* to tick off todos, we enter into the event, click the item, at which point the clock starts, and when we click 'complete,' we accrue the points in 'general willpower' (shown on stats page)

* do not auto de-select vertical up and down movers

__auto_focus__
* events can be scheduled to 'auto-pop' the focus mode at a specified date and time.
* the app must be able to take control of the screen! / or send a notification, like an alarm, that demands attention.

__long_focus_periods__

* what about activities that last for 24 - 48 hours, that also require checking in, and a longer focus window that pops back up intermittently, and towards the end of the period.
* e.g. fasting, period of eating well, habit quitting, etc.

* time totals are stored within subgoals, and once time has been accrued, their parent cannot be altered (or only with a stern error message indicating the consequences: "time accrued under the existing parent goal, may now be allocated to a new goal, are you sure?"



# BugS
* two weird spaces at the bottom of the screen when entering the sub-goal screens.
* when creating new item in subgoal menu.

* drop down only viewable on mainscreen top level items (feature or bug?)

* icons showing up for both eventboxes when only one clicked.
- debug: can drag once with icons still displaying but not twice
- click off event to de-select
* how to display description for events that are half an hour.
* 2 events side to side (at same time)
# unnecessary - just prevent it from happening!
* change color of events.


__^^^^^^^^^^^^^^^^^___
__^^^^^^^^^^^^^^^^^___

* add calendar view.
	* daily:
		- store selected date
		- show date on re-entry
	- create monthly calendar view
	- create weekly calendar view
	- enter monthly view on first access.
	- navigate between day/month/week


# potential new features (to implement) list 16 jul __>

* after deleting - small notification at bottom of screen (undo? option)





# overall pathway

* hiearchical goal organiser.
- edit goals.
- add subsubgoals (subgoal screen) with all moving operations rectified
# done

* scheduler
- now add goals to schedule and start scoring points for time spent.
- focus mode in scheduler -> 'click when complete' -> 'exit without completing'


* High level VISION screen, which contains no more than 5 life goals.  These will form the topics / areas that Goals can be assigned.
* Todo's are then assigned to these categories.
* It's a way to make sure we are spending time working towards every major goal-area, every day or every few days, and to accumulate time towards them.
* these also determine the color-scheme of the goals we are working on, and the events that are displayed.


* block out time for 'high level goals' that can then be filled with whatever lower level goals need to be achieved. e.g. spend time visualising a goal before taking actions towards achieving it.

* voice to text in app
- voice list app

* simple timer app / questions (see above) - with voice?

* add voice to goal organiser & scheduler
- refine flow of point scoring, etc.

* separate test app?
- extra ai. features: generate subgoals from discussions, organise discussions visually - design an api for a language model to have an ongoing conversation and represent information that can be discretely modified, one piece at a time, then signed off on as a whole piece.

# that should take 3 months lol  -> til end SEP = 2 mths

# next
---

__voice app__

* VOX button
* list of voice items


* weekly review (what you've done this week: hours per task)
# as above.

------------
* working towards a sequence of goals into scheduler -> focus mode
* accrue points towards goal with achievement in focus mode





##################
# DONE	DONE 	DONE	
##################
------------------------

* two versions: debug and production version.  so we can work on potentially buggy updates and then streamline them into the release version for app store.
# yep done late Jan

## DAILIES SCREEN

* two versions: debug and production.
* dailies layout: make it look pretty
* to do / today's to do list
* incorporate voxplantopapp bar with date nav function
* incorporate quota adding into action bar.

# yes did it all 29/01

* allow multiple events @ same time in schedule - so that we can add new events at an estimated time, and then move them around in the schedule itself.
# done 9 jan

* only 1 event mod box displays at once.  (currently all events display)
# ye done 9 jan


* set default start time in schedule as NOW.
# yep done 9 jan


* focus mode into schedule.
# done 6 jan/25

* remove quotas
# yeh

__now__  (10 dec updated)

* focus mode = tracking mode
# yep done
* start storing hours - total & weekly / monthly - data structure?
# yes done
* represent stacked hours
# yes: in timebank, and viewable in quota screen.
* start tracking activities and storing data
# as above.

* sounds on mainscreen after power bar achieved
# yep did it november

__time_bank__

* subgoals all accrue time towards their parent goal.
# done dec 10

__quotas_for_goals__

* be able to set desired quotas, per day, per week, per month, and then accrue time towards them.
* set quota in GoalEditScreen.
* another screen to view all quota'd items, and which ones are scheduled in.
# done dec 10

* only show quota if task has not been completed!!
# yep done (& task cleanup)

__access__
* focus mode directly from main screen.
* after exit, event is created post-hoc to show what was done.
* time is accrued against GoalItem:
	time-amount
	date/time	// necessary for showing weekly/monthlies
* qu: calculate 
# yep did it dec 10


# est.d .5 day
* pomodoro
# took a day: 3 oct DONE (v1)

# est'd 1 day
* Log discrete tasks (rather than timed)
	- Slider button : Goal is timed / Goal is discrete
	- Changes layoutof clock:
		- No numbers on clock
		- No timer buttons
	- Discrete progressions = 
		- LOG COMPLETION
		- EASY / SMALL TASK: 30m
		- CHALLENGE / LARGE TASK: 1h
		- DISCIPLINE / WORTHY GOAL: 2h
		- EPIC WIN / MAJOR ACCOMPLISHMENT: 4h
	- Press & hold on clock face, ticks clock different colours and shows above progressions
	- Logs requisite amount in Time Vault, to be banked.
	- Time vault cleared if clock is pressed again.
## yep did that 1st Oct

- accrue points for completion?
# 30 sep

* task complete process
- hide yesterday's tasks?
# 30 sep

* start accruing time into goals
- how do we want to accrue?
- it would be good to have a 'power bar' for the day.
# yep started it 18/9/24

* load focus mode direct from goal
# yeh fakin yeh 28/08


# focus mode TODO

* 'START' begins pie chart counting per second & time updating.
# done 24 aug

* continue counting after pause
# yeh done yeh

* tidy layout
# a little

* numbers on clock face reflecting time
# done 26 aug

* buttons to change clock face
# 26 aug

* today button in dayschedule
# yeh 14/8

__navigate days__
# yep done 14 aug

- debug: save 'expanded' setting on main screen
# YEP DONE 13 aug

- allow deletion using icon
# yep done 13 aug

* Schedule Event into today Day View from GoalEditScreen.
### now need to map from todoItem into Event
### DONE 7 AUG WHEW wth

* debug moving event
# yep the thing moves fewweeeeyyy  7 AUG

* debug bottom nav bar!
# SEEMS Ok for now


* allow event selection:
- on event click,
- visually select event
# yep done 13 aug

* GoalEditScreen & ViewModel: refactor for GoalWithSubGoals.
### done 5 aug


__event_icons__
fun EventActions.

* display directly within basicschedule.
* can calculate position based on eventStartTime.
* use selectedEventId to toggle display & pass event info.
# done 13 aug YEEeeeHWAAaeewww

- + or - increases or decreases time allocation
# yeeep



### calendar flow
* what's the pathway into calendar view?
* bottom bar with calendar view -> straight into day-view
* can switch to weekly view with button (phone rotation-switch to weekly)

### done 25 jul

### gridlines and events
* first draw gridlines corresponding to hours and half hours.


* Edit goal 
	-> Time allocated field: 15m/30m/45m/60m
		* Text field, default 60m, +/- buttons 15m increments
	-> Schedule button.
* Straight into day view
* Day view forward/back across days.
* Current item


### code it:

* navigation: standardize screen name usage
	- enumerate screen reference names.
# yep done

# 16 jul work


* RECOVER functionality now with subgoal screens navigation.
# bug: make subgoal to super: brings subgoal to root list.
# yep done :D

* be able to move items with subgoals to sub
# yep easy

* check delete function also deletes subgoals recursively.
# yep recursive delete done BITCH 17 jul

## debugging

* fatal exception after moveToSuper:
# done 22 july

* status messages - e.g. 'cannot move while goal has subgoals,' etc.
- flash for 3-5 sec then disappear.




# immediate improvements 30 jun

-> create new
# yes done 10 jul

-> enter into subgoal menus
# top level fine
# sub level:
- breadcrumb loses parent.
# yes done 16 jul


-> delete
-> move all directions


* fix vertical movement
- crash on last subgoal list item down, first subgoal list item up,
- but not on toplevels.
# yep done 2 jul

* "notes" field in goal edit screen
	- add notes to database tables
	- add to repository / dao
	- add to goal edit screen
# yep done 2 jul

* hierarchy display
# yep done 2 jul

* small edit goal mod: goal id# padding
### ???? couldnt get it to work

* font size smaller on mainscreen
# yep done 2 jul

* remove completed box from main screen
* subGoal entry screen and scheduler icons

***


* enter subGoal menu clicking on subGoals
* "enter goal" icon, for goals with subgoals.
* subgoal screen -> breadcrumbs
# done 16 july (one small bug to fix)



# bugs

* not adding order to new items on topLevelList.
# yep done jul 1

* doesn't delete subgoals when topLevelGoal deleted!
-> still in database
# yep fixed jul 2

* last item in list doesnt sub
## fixed 30/6

* new todos are added after first todo
# fixed 1/7

***
## Mechanism for creating sub-goals
	-> How it should look - UI elements & process
	-> Underlying data structures.

1. Create goal

2. Goal editing

__TODO: scroll main screen__

- Create composable
	:existing item contents (TodoItem->title, isDone)
	:-> viewModel
	:-> feed item from main screen
# done 16/6

- Create viewmodel
# done 16/6

- Edit and save goal details in Edit Goal Screen
* allow update of fields (upon change) __x__
* store updated field into goalUiState __x__
* Implement SAVE button: save values upon button press __x__
* Show Goal ID on edit page __x__
# done 17/6

3. Set up parent & child relationship within todo's
* update todoRepository with getGoalsWithSubGoals function  __x__
* receive goalsWithSubGoals Flow from repository into ViewModel __x__
* expose GoalsWithSubGoals to UI Layer __x__
* consume goalsWithSubGoals into LazyColumn of goals & collapsible subgoals. __x__
# done 20/6

4. Begin creating hierarchical goal structures
* Style sub-goal items so they can be deleted and edited __x__


__errors__
* After editing subGoal and saving, it becomes topLevelGoal again.
-> potential sources of error: the save function!@
# done

* Visual Styling of buttons
# done

* Move goals Up, Down, Left and Right
*** corresponding buttons (in toolbar?) to move goals.
*** press button then press goal to do the move.
# done 28/6

* How to add existing goals as sub-goals
*** drag & drop?
# done

* Move goals up and down hierarchy.
# done


5. Sub-goal appears in list below parent.
# done


###################
### MANUAL

# how the quota system works

__overview__

* goal-setting is an important activity, along with establishing sub-goal sequences for the accomplishment of these.  however, human beings exist in sequential time.

* we only have so many hours in the day, days in the week, weeks in the month, etc., and to live a fulfilled life, we must find ways of doing all the things we want to do , in the time we have.

* this is where quotas come in. quotas help us define how much time we want to allocate for specific goals, whether it be within a day or a week.  defining these quotas is the first step along the path of defining the rules and guidelines around our time preferences, so that our voxplan assistant can ultimately construct a schedule that works for us, and to help keep us on track with it.

* let's start with daily quotas.  let's say we know we want to do four hours of focused work per day. we should be able to:
* tell voxplan our desired daily quota,
* have it schedule the work, 
* remind us when the focus block will begin,
* enter focus mode when it does start,
* record the amount of time actually accrued during that block, 
* and display a record for the week, of what we have accomplished each day, like this:

there are some things we need to do every day, for 10 mins, 30 mins, an hour, etc.  these are parts of our daily routine, but it can be difficult to keep track of them.
* what if you had an app that could keep you on task while doing these things, and log the time spent doing them, so you could see that time accruing?  VoxPlan is that app.


__screen_mockup__

--------------------
* [ DAILY QUOTAS] * 
   > this week < 

### MON
STRETCH: (*)
GYM: (*)
CODING: (*)(*)(*)( )

### TUE
STRETCH: ( )
CODING: * <^> *		<-- (diamond showing achieved)

### WED
STRETCH: * <^> *
GYM: (*)
CODING: (*)(*)( )( )	<-- blank spaces show quota not achieved

### THU
STRETCH: * <^> *
CODING: * <^> *

### FRI
STRETCH: * <^> *
GYM: * <^> *
CODING: * <^> *

### SAT
STRETCH: (*)

### SUN
STRETCH: (*)

--------------------

So in our quota-setting screen (part of the goal-details screen), we should be able to set a daily quota, as well as assign which days of the week this quota should be achieved.

Some goals will have an *every day* quota, others we can select and de-select certain days.

Another screen showing today's achievements would help us to stay on track

__gamification_&_rewards__

* so what happens when quotas are achieved?
* this is something to develop.
* when we achieve a daily quota, we have a diamond and a power up sound displayed.
* later on, we would like to include some beneficial consequences for these achievements.
* for now we can display the diamonds, and even display a full diamond / emerald if all quotas for a day are achieved, eg.:

### MON
* <^> <^> <^> *

__feedback_for_voxplan_ai__

* ultimately, achievement of goals would reinforce whatever processes had been done on those days, lack of achievement, would cause a learning process to occur, for adjustments in the process to be made.
* this of course is extremely advanced, and the subject of a much later and more intelligent system!



# ideas about how the scheduling could work

* an event block is allocated to an overall goal which has subgoals

* once 'inside' the event block, we can select a sequence of sub-goals we want to accomplish.
-> in this instance, each sub-goal can be allocated a length of time to spend, e.g. in the case of a morning routine, we want multiple timed sequences to follow, one after another.  we want to be able to change the order of these events, and potentially extend or contract the specified time (+ 1 min, -1 min / stop now).  Once one is completed, the next begins.  Each individual event can be stopped, or the overall block can be stopped, at any time.  The tasks are either Terminal - in so far as when they are done, they are removed as options and potentially archived - or they are Ongoing - in so far as there is no end-goal specified, the accomplishment is in spending the allocated-time per allocated-frequency.

* this could be the default event 'edit screen', i.e. the focus-mode screen... contains a list of goals that can be selected (they must be subgoals of the existing goal) to occur in sequence.  their time-allocation can be individually set within a time allocation box:

<FOCUS MODE MULTI>  - WHEN A GOAL WITH SUB-GOALS IS SCHEDULED.

700 ___________________		(should show actual time on LHS)
   |  drink water    |-		maybe the length of time could be
  D|_________________|+		adjusted with a - and + on either side
715|  run            |		that decreases or increases the
   |                 |-		time block.
  D|_________________|+		then, when ready, you click the
730|                 |		'GO' button, and the timer starts
   | meditate        |-
  D|_________________|+		The 'D' is a trash icon for removal
745|_______+_________|		the plus adds another item.





#Day 1: Setup and Basic UI

Project Setup:
Initialize your project with the necessary dependencies, including Jetpack Compose for the UI, Room for data persistence, and Kotlin Coroutines for asynchronous operations.

Data Model:
Define a GoalItem entity with fields id, title, isDone, and parentId to represent the hierarchical structure of goals.

Database and DAO:
Setup your Room database with the GoalItem entity and create a DAO with basic operations: insert, delete, and queries to fetch top-level goals and sub-goals.

#Day 2: Displaying Goals and Navigation

UI for Displaying Goals:
Implement a composable function to display a list of goals. Use a different background color for sub-goals for visual differentiation.

Navigation Mechanism:
Develop the functionality to navigate "one layer down" in the hierarchy. When a goal with sub-goals is selected, the UI should update to show these sub-goals as the top level.

Back Navigation:
Implement a back navigation feature, allowing users to go "back up" the hierarchy. This can be represented in a title bar or breadcrumb navigation.

# Day 3: Adding Goals at Different Levels
TodoInputBar Enhancements:

Update TodoInputBar to support adding both top-level goals and sub-goals. This may involve temporarily storing the selected parent goal's id when a goal is selected.
Goal Selection:

Allow users to select a goal for adding sub-goals. Highlight the selected goal to indicate it's been selected.
Adding Sub-Goals:

Implement the logic to add a sub-goal to the selected goal. Ensure the parentId of the new goal is set correctly.

# Day 4: Expanding and Collapsing Goals
Expand/Collapse UI:
Add an expand/collapse mechanism to each goal entry, allowing users to view or hide its immediate sub-goals.

State Management:
Implement state handling for the expand/collapse feature, ensuring the UI correctly reflects each goal's current state.

# Day 5: Advanced Navigation and State Handling

Deeper Navigation:
Enhance the navigation mechanism to allow viewing sub-sub-goals and beyond. Ensure the UI and state management scale to support deeper hierarchy levels.

Title Bar / Breadcrumbs:
Implement a title bar or breadcrumb component that reflects the current position in the hierarchy and supports navigation.

# Day 6: Integrate into python desktop app

Pass goals to and from Python desktop app.

# Day-7: Integrate LLM into goal-setting processes




# Testing and Refinement

Testing:
Thoroughly test your app, focusing on navigation, adding goals at various levels, and ensuring data integrity across the hierarchy.
UI/UX Refinement:

Refine the UI and user experience based on testing feedback. This could involve adjusting the visual hierarchy, improving touch targets for expand/collapse and selection, and ensuring a smooth navigation experience.
Documentation and Cleanup:

Document your code where necessary, especially the parts handling the hierarchical data structure and navigation logic. Clean up any unused code or resources.

