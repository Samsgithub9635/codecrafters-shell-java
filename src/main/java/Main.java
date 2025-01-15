import java.util.Scanner;

public class Main {
    public static void main(String[] args) throws Exception {
        // Uncomment this block to pass the first stage
        
        
        

        while (true) {
            // Print the prompt
            System.out.print("$ ");
            // In this stage, you'll implement support for handling invalid commands in your shell.
            // $ invalid_command
            // invalid_command: command not found
            Scanner scanner = new Scanner(System.in);
            // Read user input
            String input = scanner.nextLine();
            // Check for the exit command
            if (command.startsWith("exit")) {
                // Extract the status code
                String[] parts = command.split(" ");
                int statusCode = Integer.parseInt(parts[1]);
                
                // Terminate the program with the specified status code
                System.exit(statusCode);
            }
            
            // Handle invalid commands
            System.out.println(input + ": command not found");
        }
        
        
    }
}
