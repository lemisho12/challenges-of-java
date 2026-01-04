Chapter1_Challenge_1_4: The Robust File Config Reader (Exception Handling)
Overview
This Java project demonstrates robust file reading with comprehensive exception handling. It reads a configuration file (config.txt) that contains two lines:

A version number (integer)
A file path
The program:

Checks for various errors such as missing files, invalid data, or outdated versions.
Implements specific catch blocks for different exceptions.
Uses custom exceptions for specific validation.
Ensures that a message indicating the completion of the read attempt is always printed, regardless of success or failure.
How it Works
Attempts to open and read config.txt.
Reads the first line as the version number:
If it cannot be parsed as an integer, reports an error.
If the version is less than 2, throws a custom exception.
Reads the second line as a file path:
Checks if the file exists at that path.
If not, throws an IOException.
Catches and displays user-friendly messages for specific errors.
Always prints "Config read attempt finished." at the end.
Setup Instructions
Make sure you have Java installed.
Save the Java code in a file named Chapter1_Challenge_1_4.java.
Compile the program and run:

javac Chapter1_Challenge_1_4.java

java Chapter1_Challenge_1_4
