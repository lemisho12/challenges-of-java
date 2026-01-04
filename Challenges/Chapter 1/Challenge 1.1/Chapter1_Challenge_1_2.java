public class Chapter1_Challenge_1_2 {

    public static void main(String[] args) {
        String[] winningNumbers = {
            "12-34-56-78-90",
            "33-44-11-66-22",
            "01-02-03-04-05"
        };

        String winningNumberWithHighestAvg = "";
        double highestAverage = -1.0;

        for (String number : winningNumbers) {
            try {
                System.out.println("Analyzing: " + number);

                // Remove dashes
                String continuousNumber = number.replace("-", "");

                // Convert to char array
                char[] digitsChars = continuousNumber.toCharArray();

                int[] digits = new int[digitsChars.length];
                int sum = 0;

                // Convert characters to integers
                for (int i = 0; i < digitsChars.length; i++) {
                    // Defensive: check if the character is a digit
                    if (Character.isDigit(digitsChars[i])) {
                        digits[i] = Character.getNumericValue(digitsChars[i]);
                        sum += digits[i];
                    } else {
                        throw new NumberFormatException("Invalid character in number: " + digitsChars[i]);
                    }
                }

                double average = (double) sum / digits.length;

                System.out.printf("Digit Sum: %d, Digit Average: %.1f%n", sum, average);

                // Check if this is the highest average so far
                if (average > highestAverage) {
                    highestAverage = average;
                    winningNumberWithHighestAvg = number;
                }

            } catch (NumberFormatException e) {
                System.out.println("Error processing number '" + number + "': " + e.getMessage());
            } catch (Exception e) {
                System.out.println("Unexpected error processing number '" + number + "': " + e.getMessage());
            }
            System.out.println(); // For readability
        }

        // Final output
        System.out.println("The winning number with the highest average is: " 
            + winningNumberWithHighestAvg + " with an average of " + highestAverage);
    }
}