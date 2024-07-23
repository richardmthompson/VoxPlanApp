# VISION

To be a fully voice-interactive, intelligent goal setting and planning application 
with an innovative user interface, as a platform for building ever better human computer
collaboration. 

# FUNCTIONALITY

The primary function of Voxplan is to assist the user to create a roadmap or blueprint for the fulfilment of any number of complex goals, and then to implement time-management and psychological optimisation strategies to  facilitate the progressive achievement of those goals through management of an overall schedule as well as daily activities.


# PROJECT ROADMAP

## In Progress... (please maintain a logical sequence!)

1. Implement full Voxplan functions into GUI (main task)
					DONE?
	- Navigate between nodes.	^
	- Edit goal			^
	- Delete goal			(in prog)
	- Move to L / R			^
	- Move up / down
	- Load				(prog)
	- Save

2. Visual management of nodes to fill existing window:
	- 'Center' - re-centre the tree around a sub-node.
	- 'Uncenter' - pressing up at the new 'root' node brings us back to the main node tree.

3. Voice recognition of commands

4. Colour coding & other styling of nodes
	- choose colour of node / auto-colour on creation - use grades of colour for sub-nodes
	- increase elaborate-ness of nodes according to how much time has been spent on them (gamification)

5. Additional app parts:
	- Day-planner & focus mode (perhaps create on mobile, then create integration with desktop and mobile app as part of later course content)
	- e.g. morning planner (morning routine) / (evening routine)
	- Brainstorm list & select / convert to tree.

6. Movement of node tree with re-sizing of window
	- maintain position of root node in center / top of window with re-sizing.

7. Expansion / contraction of the tree viewing window, and how to navigate upwards/ downwards seamlessly for larger and larger trees.:
	- 'center' function, which brings the selected node to the middle of the screen.
	- also loading and saving sub-node trees to specific files, and managing file sturctures


## Next Up...

* Delete item
* Move item: left, up, down, right
* Save and load from different filenames
* Reset entire list
- Implement detailed goal functions (e.g. timeframe, reminders (?))

## Future... 

* Ability to nest sub-files within goal-nodes, so that opening a specific goal node will recurse into the specific saved file, and bridging upwards out of file will save progress within that file.  Then we have abilities to split sections of the tree off into individual files, and settings concerning how sub-projects are viewed and interacted with in the GUI.

* Ability to enter sequentialised series of actions, with timeframes, e.g. entire syllabus for AI MSc, which the app then keeps track of, prompts as to what should be being done on a specific day/time, gives rewards for accomplished tasks, keeps track of rewards attained, specific to the set.

* how do we manage keyboard events with attributewin?
		
	- remove keyboard control before sending focus to attributewin.
			
	- when attributewin receives focus:
			- set the keyboard listener up.
	- when closing attributewin
		- unset the keyboard listener so that navmode can grab it again
		- as the focus will be un-set upon closing,
			we must control focus on exiting attributewin.
		
	**** I'VE GIVEN UP ON THIS ^^^^ THINK IT NEEDS A NEW INTERFACE TO BE IMPLEMENTED.....
		


# COMPLETED

- Basic command-line user interface
- Build first version of hierarchical tree-structure 
- Navigation methodology
- Store goals in file and retrieve



# APPLICATION FEATURES

1. Node view: To aid in its ongoing visual representation, as a structure of interconnected and hierarchical nodes.  To facilitate accelerated navigation of this structure with a combination of voice and keyboard interface.  To present this interface in a beautiful and intuitive way that is enjoyable and natural to use.

2. Day & task focus: To easily and quickly plan a day-schedule based on the above, along with a "Focus-mode" which has a countdown timer and 'prevents access' to other phone apps during focused individual work slots.
	Ideas:
	- Timetable 'templates' - a template for the day with overall activities time-boxed, which can then be filled in with specific tasks from the node structure.
		- Focus mode: From this timetable, enter another 'focus screen' where a clock counts down, and afterwards, the time is 'banked' and visually represented somehow in the original node structure.  Nodes which have had more time banked into them are larger, and visually more impressive, detailed, etc. to illustrate the balance of work that is taking place at a glance. 
	- Use specific time-management techniques, e.g. pomodoro, 52/17
	- Notification and other apps - blocked for this time in the focus screen.
	- For time-slot (e.g. 3 hours, time tech is suggested / selected.  Then the time-slot is completed using the selected timetech.

3. Recognise achievements within a gamified reward structure of sorts,
	- Represent time spent and achivements garnered within specific sub-goal structures, visually as 'metamorphoses' of the graphical goal node representations...
		--> Opportunity for creative A.I graphical representations?

4. To aid in elaboration and visualisation of the overall plan and individual elements, using NLP & goal-setting psychology, to create an ever clearer map of the problem space and manage its components

5. To use AI to sanity check the overall plan, suggest improvements and/or modifications to the sequence or elements; to keep the goals fresh, removing those that are no longer relevant, etc.
	- Opportunity to include chat GPT as an advisor / AutoGPT- of sorts.
	- Auto clean up of Task-lists and maintaining focus within goal tree to keep the application current

6. To plan the achievement of goals over time, and check-in as to whether the plan is being followed:
	- Particularly with managing routines to facilitate goal outcomes, e.g.:
		- Are you following the planned schedule?
		- If not, how does it need to be adjusted?

7. Interactive journalling - using voice for journalling as prompted by ai
	- Includes coaching and overcoming psychological barriers by ai

8. More detailed project management ?
	- Achievement timeframes and habit management (self-maintenance) programs


=======
# voxplan
Goal-setting and management application with voice recognition and other ai support (eventually)
>>>>>>> d3277d6229d842967d46ba3943a05aa946f7a867
