package Milestone1;

import java.util.Random;

public class RollandFlip {

    Server server = new Server();

    // Method to process message commands
    public void processMessage(String message) {
        String[] parts = message.split(" ");

        System.out.println("processing command from other class...");

        if (parts.length >= 1) {
            String command = parts[0];
            switch (command) {
                case "/roll":
                    handleRollCommand(parts);
                    break;
                case "/flip":
                    handleFlipCommand();
                    break;
                default:
                    // Unknown command, do nothing or handle accordingly
                    break;
            }
        }
    }

    // Method to handle the /roll command
    public void handleRollCommand(String[] parts) {
        if (parts.length == 2) {
            String argument = parts[1];
            if (argument.matches("\\d+-\\d+")) {
                // Format 1: /roll 0 - X or 1 - X
                String[] range = argument.split("-");
                int min = Integer.parseInt(range[0]);
                int max = Integer.parseInt(range[1]);
                int result = rollInRange(min, max);
                server.broadcast("Rolled between " + min + " and " + max + ": " + result);
            } else if (argument.matches("\\d+d\\d+")) {
                // Format 2: /roll #d#
                String[] dice = argument.split("d");
                int numOfDice = Integer.parseInt(dice[0]);
                int sides = Integer.parseInt(dice[1]);
                int result = rollDice(numOfDice, sides);
                server.broadcast("Rolled " + numOfDice + "d" + sides + ": " + result);
            } else {
                // Invalid roll format
                server.broadcast("Invalid roll format. Please use /roll <min-max> or /roll <#d#>");
            }
        } else {
            // Invalid roll format
            server.broadcast("Invalid roll format. Please use /roll <min-max> or /roll <#d#>");
        }
    }

    // Method to handle the /flip command
    public void handleFlipCommand() {
        Random random = new Random();
        String result = random.nextBoolean() ? "Heads" : "Tails";
        server.broadcast("Flipped a coin: " + result);
    }

    // Method to simulate rolling dice within a given range
    private int rollInRange(int min, int max) {
        return (int) ((Math.random() * (max - min)) + min);
    }

    // Method to simulate rolling dice
    private int rollDice(int numOfDice, int sides) {
        int total = 0;
        Random random = new Random();
        for (int i = 0; i < numOfDice; i++) {
            total += random.nextInt(sides) + 1;
        }
        return total;
    }

    // Other server methods...
}