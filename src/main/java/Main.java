import java.util.Arrays;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) throws Exception {
        // Uncomment this block to pass the first stage
        Scanner scanner = new Scanner(System.in);
        
        
        String typeSubstring;
        String[] commands = {"echo", "exit", "type"};
        while (true) {
            // Print the prompt
            System.out.print("$ ");
            // In this stage, you'll implement support for handling invalid commands in your shell.
            // $ invalid_command
            // invalid_command: command not found
            
            // Read user input
            String input = scanner.nextLine();
            // Check for the exit command
            if (input.equals("exit 0")) {
                break;
              }
            
            // Handle invalid commands
            // System.out.println(input + ": command not found");
            if (input.startsWith("echo")) {
                System.out.println(input.substring(5));
            } 
            else if (input.startsWith("type")) {
                typeSubstring = input.substring(5);
                if (Arrays.asList(commands).contains(typeSubstring)) {
                System.out.println(typeSubstring + " is a shell builtin");
                }   
                else {
                System.out.println(typeSubstring + " not found");
                }
            }
            else {
                System.out.println(input + ": command not found");
            }
        }
        
        
    }
}
