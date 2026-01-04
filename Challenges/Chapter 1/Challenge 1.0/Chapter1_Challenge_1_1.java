import java.util.InputMismatchException;
import java.util.Scanner;

public class Chapter1_Challenge_1_1 {
    public static void main(String[] args) {
        Scanner input = new Scanner(System.in);

        try {
            System.out.print("Enter the cryptic integer: ");
            int number = input.nextInt();

            if (number <= 0) {
                throw new IllegalArgumentException("Number must be a positive integer!");
            }

            // Step 1: Extract the first digit
            int numDigits = (int) Math.log10(number) + 1;
            int firstDigit = number / (int) Math.pow(10, numDigits - 1);

            // Step 2: Extract the last digit
            int lastDigit = number % 10;

            // Step 3: Product of first and last
            int product = firstDigit * lastDigit;

            // Step 4: Extract second and second-last digits
            int secondDigit = (number / (int) Math.pow(10, numDigits - 2)) % 10;
            int secondLastDigit = (number / 10) % 10;

            // Step 5: Sum of second and second-last
            int sum = secondDigit + secondLastDigit;

            // Step 6: Concatenate product and sum
            String finalCode = String.valueOf(product) + String.valueOf(sum);

            // Step 7: Display the result
            System.out.println("The decrypted code is: " + finalCode);

        } catch (InputMismatchException e) {
            System.err.println("❌ Error: Please enter a valid integer number.");
        } catch (IllegalArgumentException e) {
            System.err.println("⚠️ " + e.getMessage());
        } catch (Exception e) {
            System.err.println("⚠️ Unexpected error: " + e.getMessage());
        } finally {
            input.close();
        }
    }
}
