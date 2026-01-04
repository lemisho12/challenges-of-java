import java.io.*;

public class Chapter1_Challenge_1_4 {

    // Custom exception for invalid config version
    static class InvalidConfigVersionException extends Exception {
        public InvalidConfigVersionException(String message) {
            super(message);
        }
    }

    public static void main(String[] args) {
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader("config.txt"));

            // Read the first line: version number
            String line1 = reader.readLine();
            if (line1 == null) {
                throw new IOException("Config file is empty or missing version info.");
            }

            int version;
            try {
                version = Integer.parseInt(line1.trim());
            } catch (NumberFormatException e) {
                System.out.println("Error: The first line must be a number representing the version.");
                return; // Exit after handling
            }

            // Check version
            if (version < 2) {
                // Throw custom exception
                throw new InvalidConfigVersionException("Config version too old!");
            }

            // Read second line: file path
            String filePath = reader.readLine();
            if (filePath == null) {
                System.out.println("Error: Missing file path in config.");
                return;
            }

            // Check if file exists
            File f = new File(filePath);
            if (!f.exists()) {
                throw new IOException("The file at path " + filePath + " does not exist.");
            }

            System.out.println("Configuration read successfully!");

        } catch (FileNotFoundException e) {
            System.out.println("Error: The configuration file 'config.txt' was not found.");
        } catch (InvalidConfigVersionException e) {
            System.out.println("Error: " + e.getMessage());
        } catch (IOException e) {
            System.out.println("IO Error: " + e.getMessage());
        } catch (Exception e) {
            // Catch any other unexpected exceptions
            System.out.println("Unexpected error: " + e.getMessage());
        } finally {
            System.out.println("Config read attempt finished.");
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    System.out.println("Error closing the reader.");
                }
            }
        }
    }
}