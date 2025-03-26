package com.kleberrhuan.intuitivecare.util;

import java.util.Scanner;

/**
 * ScannerHelper provides utility methods for reading user input from the console.
 * It simplifies the process of prompting the user and validating their input.
 */
public final class ScannerHelper implements AutoCloseable {

    private Scanner scanner;

    /**
     * Initializes a new ScannerHelper instance with System.in.
     */
    public ScannerHelper() {
        scanner = new Scanner(System.in);
    }

    /**
     * Prompts the user with the specified message and returns the input as a String.
     *
     * @param prompt The prompt message to display.
     * @return The user's input as a String.
     */
    public String getString(String prompt) {
        System.out.print(prompt);
        return scanner.nextLine();
    }

    /**
     * Prompts the user with the specified message and returns the input as an integer.
     * If the user enters invalid input, it will repeatedly prompt until a valid integer is provided.
     *
     * @param prompt The prompt message to display.
     * @return The user's input as an integer.
     */
    public int getInt(String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine();
            try {
                return Integer.parseInt(input);
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter a valid integer.");
            }
        }
    }

    /**
     * Closes the underlying Scanner resource.
     */
    public void close() {
        scanner.close();
    }
}