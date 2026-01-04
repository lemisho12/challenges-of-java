Dungeon Game Challenge
Overview
A simple text-based dungeon adventure game where the player navigates through 5 rooms, facing random events like traps, healing potions, and monsters. The game demonstrates the use of control structures (for, switch, do-while), exception handling, random number generation, and user input handling in Java.

Features
Player starts with 100 health points.
Progresses through 5 rooms using a for loop.
Randomly generates events:
Trap: reduces health by 20.
Healing potion: increases health by 15 (capped at 100).
Monster: player guesses a number (1-5) to defeat it, with repeated prompts until correct.
Uses switch statement to handle events.
Implements try-catch for input validation to prevent crashes from invalid entries.
Exits early if player's health drops to zero or below.
Announces victory if all rooms are cleared.
How to Run
Save the Java code in a file named Chapter1_Challenge_1_3.java.
Compile the program:

javac Chapter1_Challenge_1_3.java
Run the program:

java Chapter1_Challenge_1_3
Follow on-screen prompts to play.
Example Output

Entering room 1...
A healing potion! Health is now 100 (capped from 85).
Entering room 2...
A monster appears! Guess a number (1-5) to defeat it: 3
Wrong! Try again: 2
Wrong! Try again: 4
You defeated the monster!
...
You cleared the dungeon! Victorious with 80 health!
