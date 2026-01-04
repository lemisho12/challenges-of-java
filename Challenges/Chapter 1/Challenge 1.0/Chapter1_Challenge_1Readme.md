🧩 Chapter1_Challenge_1_1 — Cryptic Integer Decoder
📘 Overview

Chapter1_Challenge_1_1 is a simple Java console program that decodes a “cryptic integer” by performing a series of digit-based operations.
It demonstrates exception handling, mathematical computation, and input validation in Java.

⚙️ Features

Prompts the user to enter a positive integer.

Extracts:

First digit

Last digit

Second digit

Second-last digit

Calculates:

Product of first and last digits

Sum of second and second-last digits

Concatenates these two results to form a “decrypted code”.

Handles invalid input gracefully with proper error messages.

🧮 Example
Input
Enter the cryptic integer: 5834

Steps

First digit = 5

Last digit = 4

Product = 5 × 4 = 20

Second digit = 8

Second-last digit = 3

Sum = 8 + 3 = 11

Concatenated result = “2011”

Output
The decrypted code is: 2011

🧠 Concepts Demonstrated

Exception Handling: Using try-catch-finally with InputMismatchException and IllegalArgumentException.

Mathematical Operations: Using Math.log10() and Math.pow() for digit extraction.

Input Validation: Ensuring the input is a positive integer.

String Concatenation and Type Conversion.

🖥️ How to Run
✅ Option 1: Run in Visual Studio Code
Prerequisites

Install JDK (version 17 or later)
Verify installation:

java -version
javac -version


Both should show the same version.

Install Java Extension Pack in VS Code:

Go to Extensions (Ctrl+Shift+X)

Search “Extension Pack for Java” and install it.

Steps

Save the code as Chapter1_Challenge_1_1.java in a folder.

Open the folder in VS Code.

Open an integrated terminal (Ctrl+`).

Compile and run:

javac Chapter1_Challenge_1_1.java
java Chapter1_Challenge_1_1


When prompted, enter a positive integer to test the program.

✅ Option 2: Run in NetBeans IDE
Prerequisites

Install the latest NetBeans IDE (with Java support).

Ensure a JDK (Java Development Kit) is installed and configured.

Steps

Open NetBeans.

Create a new Java Project → Java Application.

Name it Chapter1_Challenge_1_1.

Replace the default Main.java content with the provided code.

Right-click the project → Run or press Shift + F6.

Input an integer when prompted.