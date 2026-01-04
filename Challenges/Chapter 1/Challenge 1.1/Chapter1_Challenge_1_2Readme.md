Lottery Number Analyzer
Overview
This Java program analyzes a list of weekly winning lottery numbers. It processes each number by removing dashes, converting the digits into integers, calculating the sum and average of the digits, and determining which winning number has the highest average digit value.

Features
Initializes an array with predefined winning lottery numbers.
Removes formatting characters (-) from each lottery number.
Converts each character to its numeric value with exception handling.
Calculates the sum and average of digits for each number.
Identifies and displays the winning number with the highest average digit value.
Uses both for and for-each loops for iteration.
Handles potential errors gracefully with exception handling.
How to Run
Prerequisites
Java Development Kit (JDK) 8 or higher installed on your system.
A code editor or IDE such as Visual Studio Code or NetBeans.
Running in Visual Studio Code
Open VS Code.
Create a new file named Chapter1_Challenge_1_2.java.
Copy and Paste the Java code into this file.
Open the integrated terminal (`Ctrl + ``).
Compile the program:

javac Chapter1_Challenge_1_2.java
Run the program:

java Chapter1_Challenge_1_2
View the output in the terminal.
Running in NetBeans
Open NetBeans IDE.
Create a new Java Application project.
Add a new Java Class named Chapter1_Challenge_1_2.
Paste the provided code into the class.
Run the class:
Right-click on the class file.
Select Run File.
View the output in the Output window.
Sample Output

Analyzing: 12-34-56-78-90
Digit Sum: 45, Digit Average: 4.5

Analyzing: 33-44-11-66-22
Digit Sum: 30, Digit Average: 3.0

Analyzing: 01-02-03-04-05
Digit Sum: 15, Digit Average: 1.5

The winning number with the highest average is: 12-34-56-78-90 with an average of 4.5