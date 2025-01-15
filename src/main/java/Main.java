import java.nio.file.Files;
import java.nio.file.Path;
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
                        // Check for the type command
                        else if (input.startsWith("type")) {
                            String[] parts = input.split(" ");
                            if (parts.length > 1) {
                                String command = parts[1];
                                if (builtins.contains(command)) {
                                    System.out.println(command + " is a shell builtin");
                                } else {
                                    String path = System.getenv("PATH");
                                    String[] directories = path.split(":");
                                    boolean found = false;
                                    for (String dir : directories) {
                                        File file = new File(dir, command);
                                        if (file.exists() && file.canExecute()) {
                                            System.out.println(command + " is " + file.getAbsolutePath());
                                            found = true;
                                            break;
                                        }
                                    }
                                    if (!found) {
                                        System.out.println(command + ": not found");
                                    }
                                }
                            } else {
                                System.out.println("type: command not found");
                            }
                        }
                        
                        // Handle invalid commands
                        else {
                            System.out.println(input + ": command not found");
                        }
                    }
                }
            }