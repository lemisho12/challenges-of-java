import java.util.InputMismatchException;
import java.util.Random;
import java.util.Scanner;

public class Chapter1_Challenge_1_3 {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        Random rand = new Random();

        int health = 100;
        boolean defeated = false;

        for (int room = 1; room <= 5; room++) {
            System.out.println("Entering room " + room + "...");

            int event = rand.nextInt(3) + 1; // 1 to 3

            switch (event) {
                case 1: // Trap
                    health -= 20;
                    System.out.println("A trap sprung! Health is now " + health + ".");
                    if (health <= 0) {
                        System.out.println("You have been defeated in room " + room + ".");
                        defeated = true;
                        break;
                    }
                    break;

                case 2: // Healing potion
                    int oldHealth = health;
                    health += 15;
                    if (health > 100) {
                        health = 100;
                    }
                    System.out.println("You found a healing potion! Health is now " + health + " (capped from " + oldHealth + ").");
                    break;

                case 3: // Monster
                    int monsterNumber = rand.nextInt(5) + 1; // 1-5
                    int guess = 0;

                    System.out.print("A monster appears! Guess a number (1-5) to defeat it: ");

                    // Handle user input with exception handling
                    while (true) {
                        try {
                            guess = scanner.nextInt();
                            if (guess < 1 || guess > 5) {
                                System.out.print("Please enter a number between 1 and 5: ");
                                continue;
                            }
                            break; // valid input
                        } catch (InputMismatchException e) {
                            System.out.print("Invalid input! Please enter an integer between 1 and 5: ");
                            scanner.next(); // clear invalid input
                        }
                    }

                    // do-while loop for guessing
                    while (guess != monsterNumber) {
                        System.out.print("Wrong! Try again: ");
                        try {
                            guess = scanner.nextInt();
                            if (guess < 1 || guess > 5) {
                                System.out.print("Please enter a number between 1 and 5: ");
                                continue;
                            }
                        } catch (InputMismatchException e) {
                            System.out.print("Invalid input! Please enter an integer between 1 and 5: ");
                            scanner.next();
                        }
                    }

                    System.out.println("You defeated the monster!");
                    break;
            }

            if (health <= 0) {
                // Player defeated, exit loop early
                break;
            }
        }

        if (health > 0) {
            System.out.println("You cleared the dungeon! Victorious with " + health + " health!");
        } else {
            System.out.println("Game over! You have been defeated.");
        }

        scanner.close();
    }
}