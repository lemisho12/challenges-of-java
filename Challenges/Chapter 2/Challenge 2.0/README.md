# Chapter2_Challenge1
Bouncing Text Applet
This project is a classic Java Applet demonstrating a simple animation: a string of text moving across the screen.

Project Files
BouncingTextApplet.java: The main Java source file containing the Applet logic.

BouncingTextApplet.html: The HTML file used to embed and run the Applet in a web browser (or an Applet Viewer).

##How It Works
The animation is achieved using a separate thread and the repaint() mechanism, which are fundamental concepts for animation in Java Applets.

BouncingTextApplet.java Key Components:
Inheritance and Interface: The class BouncingTextApplet extends Applet and implements the Runnable interface. This allows it to be executed by a separate thread.

##start() Method: This method is called when the Applet is loaded. It creates a new Thread object, passing this (the Runnable instance) to it, and then calls start() on the thread to begin execution.

##run() Method: This is the core of the animation loop.

It updates the xCoord variable to move the text horizontally.

It checks if the text has moved past the right edge of the applet window (getSize().width) and, if so, resets the text's position far to the left (xCoord = -100), creating a continuous-scrolling effect.

It calls repaint(), which tells the AWT to schedule a call to the paint() method.

It uses Thread.sleep(100) to pause the thread for 100 milliseconds, controlling the speed of the animation.

paint(Graphics g) Method: This method is responsible for drawing the text. It uses the current xCoord to draw the text string ("KANU TECHOME") at its updated position in a white color on a black background.

BouncingTextApplet.html
This simple HTML file embeds the compiled Java Applet using the <applet> tag, specifying the compiled class file (BouncingTextApplet.class), and the dimensions (width="400" height="100").

Running the Project
To run this project, you would typically follow these steps:

Compile the Java file:

Download ZIP file to your local machine.

javac BouncingTextApplet.java
Run the Applet:

Using a Web Browser: Open the BouncingTextApplet.html file in a browser that supports Java Applets (note: browser support for Applets is now rare).

#Using the AppletViewer tool:


appletviewer BouncingTextApplet.html
The compiled .class file and the .html file must be in the same directory.
