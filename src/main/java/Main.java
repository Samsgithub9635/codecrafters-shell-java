import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) throws Exception {
        Scanner scanner = new Scanner(System.in);
        List<String> builtins = Arrays.asList("echo", "exit", "type");
        
        while (true) {
            // Print the prompt
            System.out.print("$ ");
            
            // Read user input
            String input = scanner.nextLine();
            
            // Check for the exit command
            if (input.equals("exit 0")) {
                break;
            }
            
            // Check for the echo command
            else if (input.startsWith("echo")) {
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