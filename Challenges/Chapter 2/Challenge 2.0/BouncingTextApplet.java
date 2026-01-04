import java.applet.Applet;
import java.awt.*;

// Class must extend Applet and implement Runnable for the thread [cite: 5]
public class BouncingTextApplet extends Applet implements Runnable {
    // Variables for the animation state
    private Thread animationThread; // The thread for animation [cite: 7]
    private String name = "KANU TECHOME"; // Initialize String with your name [cite: 6]
    private int xCoord = 0; // Current x-coordinate for the text [cite: 10]
    private final int yCoord = 50; // Fixed y-coordinate (vertical position)
    private final int textSpeed = 5; // Speed of movement
    private int xSpeed = 3; // NEW: Horizontal speed
    private int ySpeed = 3; // NEW: Vertical speed
    private Font textFont; // NEW: Font object for consistent measurement
    
     //Called when the applet is first loaded.
     
    public void init() {
        // Set the applet's size (required for AppletViewer) [cite: 6]
        setSize(3000, 900);
        // Set a background color [cite: 6]
        setBackground(Color.BLACK);
        // Initialize a Font object for accurate height/width measurements
        textFont = new Font("Times New Roman", Font.BOLD, 40); 
    }

    
     //Called to create and start the animation thread.
     
    public void start() {
        // Create and start a new thread (this, because the class implements Runnable) [cite: 7]
        if (animationThread == null) {
            animationThread = new Thread(this);
            animationThread.start();
        }
    }

    
     //The thread's main execution loop.
     
    public void run() {
        // Use a while loop to keep the animation running [cite: 9]
        while (Thread.currentThread() == animationThread) {
            // 1. Update the x-coordinate of the text [cite: 10]
           xCoord += textSpeed;
            yCoord += ySpeed;

           // 2. Bouncing Logic
            // Get necessary metrics for boundary checks
            int appletWidth = getSize().width;
            int appletHeight = getSize().height;
            FontMetrics fm = getFontMetrics(textFont);
            int textWidth = fm.stringWidth(name);
            int textHeight = fm.getAscent(); // Text height above the baseline


           // Horizontal Bounce
            // Check right edge
            if (xCoord + textWidth > appletWidth) {
                xCoord = appletWidth - textWidth; // Position exactly at the edge
                xSpeed = -xSpeed; // Reverse direction
            } 
            // Check left edge
            else if (xCoord < 0) {
                xCoord = 0; // Position exactly at the edge
                xSpeed = -xSpeed; // Reverse direction
            }
            // Vertical Bounce
              // Check bottom edge (yCoord is the baseline, so subtract text height)
            if (yCoord + fm.getDescent() > appletHeight) {
                yCoord = appletHeight - fm.getDescent(); // Position exactly at the bottom
                ySpeed = -ySpeed; // Reverse direction
            }

             // Check top edge (yCoord is the baseline, must be greater than text height)
            else if (yCoord < textHeight) { 
                yCoord = textHeight; // Position exactly at the top
                ySpeed = -ySpeed; // Reverse direction
            }


        

            // 3. Request the applet to redraw the screen (calls paint()) [cite: 12]
            repaint();

            // 4. Pause the animation for 100 milliseconds [cite: 13]
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                // If the thread is interrupted, break the loop and stop the animation
                break;
            }
        }
    }

     // Called to stop the thread when the applet is inactive.
     
    public void stop() {
        // Stop the thread by setting it to null [cite: 8]
        if (animationThread != null) {
            animationThread = null;
        }
    }

    
     // Draws the text on the screen.
     
    public void paint(Graphics g) {
        // Set the color for the text
        g.setColor(Color.WHITE);
        // Use g.drawString() to draw the text at the current x-coordinate [cite: 14]
        g.drawString(name, xCoord, yCoord);
    }
}