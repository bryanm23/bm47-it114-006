package MM.Server;

import java.util.Random;
import MM.*;
import MM.Server.RollandFlip;

public class RollandFlip {

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
    public String handleRollCommand(String[] parts) {
        if (parts.length == 2) {
            String argument = parts[1];
            if (argument.matches("\\d+-\\d+")) {
                // Format 1: /roll 0 - X or 1 - X
                String[] range = argument.split("-");
                int min = Integer.parseInt(range[0]);
                int max = Integer.parseInt(range[1]);
                int result = rollInRange(min, max);
                return "Rolled between " + min + " and " + max + ": <b>" + result + "</b>";
            } else if (argument.matches("\\d+d\\d+")) {
                // Format 2: /roll #d#
                String[] dice = argument.split("d");
                int numOfDice = Integer.parseInt(dice[0]);
                int sides = Integer.parseInt(dice[1]);
                int result = rollDice(numOfDice, sides);
                return "<html>Rolled " + numOfDice + "d" + sides + ": <font color='red'><b>" + result + "</b></font></html>";
            } else {
                // Invalid roll format
                return "<html><font color='red'><b>Invalid roll format. Please use /roll &lt;min-max&gt; or /roll &lt;#d#&gt;</b></font></html>";
            }
        } else {
            // Invalid roll format
            return "<html><font color='red'><b>Invalid roll format. Please use /roll &lt;min-max&gt; or /roll &lt;#d#&gt;</b></font></html>";
        }
    }
    

    // Method to handle the /flip command
    // Method to handle the /flip command
    public String handleFlipCommand() {
        String result = Math.round(Math.random()) == 0 ? "Tails" : "Heads";
        return "<html><font color='red'><b>Flipped a coin: " + result + "</b></font></html>";
    }


    // Method to simulate rolling dice within a given range
    private static int rollInRange(int min, int max) {
        return (int) ((Math.random() * (max - min)) + min);
    }

    // Method to simulate rolling dice
    private static int rollDice(int numOfDice, int sides) {
        int total = 0;
        Random random = new Random();
        for (int i = 0; i < numOfDice; i++) {
            total += random.nextInt(sides) + 1;
        }
        return total;
    }

    // Other server methods...
}
