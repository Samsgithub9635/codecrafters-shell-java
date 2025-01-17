import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

public class Main {
    private static final boolean DEBUG = false; // Set to false to disable debug logs

    public static void main(String[] args) throws Exception {
        Scanner scanner = new Scanner(System.in);
        // List of built-in commands
        List<String> builtins = Arrays.asList("echo", "exit", "type", "pwd", "cd");
        
        while (true) {
            // Print the prompt
            System.out.print("$ ");
            
            // Read user input
            String input = scanner.nextLine();
            if (DEBUG) System.err.println("[DEBUG] Command received: " + input);
            
            // Use new split method for handling quotes and backslashes
            String[] parts = splitCommandLine(input);
            if (parts.length == 0) continue; // Skip if no command is entered
            String command = parts[0];
            
            // Check for the exit command
            if (command.equals("exit") && parts.length > 1 && parts[1].equals("0")) {
                break;
            }
            
            // Check for the echo command
            else if (command.equals("echo")) {
                if (parts.length > 1) {
                    // Print everything after "echo"
                    System.out.println(String.join(" ", Arrays.copyOfRange(parts, 1, parts.length)));
                }
            }
            
            // Check for the type command
            else if (command.equals("type")) {
                if (parts.length > 1) {
                    String typeCommand = parts[1];
                    if (builtins.contains(typeCommand)) {
                        System.out.println(typeCommand + " is a shell builtin");
                    } else {
                        String path = System.getenv("PATH");
                        if (DEBUG) System.err.println("[DEBUG] PATH: " + path);
                        String[] directories = path.split(":");
                        boolean found = false;
                        for (String dir : directories) {
                            File file = new File(dir, typeCommand);
                            if (DEBUG) System.err.println("[DEBUG] Checking: " + file.getAbsolutePath());
                            if (file.exists() && file.canExecute()) {
                                System.out.println(typeCommand + " is " + file.getAbsolutePath());
                                found = true;
                                break;
                            }
                        }
                        if (!found) {
                            System.out.println(typeCommand + ": not found");
                        }
                    }
                } else {
                    System.out.println("type: command not found");
                }
            }
            
            // Check for the pwd command
            else if (command.equals("pwd")) {
                String currentDir = System.getProperty("user.dir");
                if (DEBUG) System.err.println("[DEBUG] Current working directory: " + currentDir);
                System.out.println(currentDir);
            }
            
            // Check for the cd command
            else if (command.equals("cd")) {
                if (parts.length > 1) {
                    String targetDir = parts[1];
                    
                    // Check for the '~' character and replace with home directory
                    if (targetDir.equals("~")) {
                        targetDir = System.getenv("HOME");
                    }
                    
                    File dir = new File(targetDir);
                    
                    // Handle relative paths
                    if (!dir.isAbsolute()) {
                        String currentDir = System.getProperty("user.dir");
                        dir = new File(currentDir, targetDir);
                    }
                    
                    try {
                        // Change the directory if it exists and is a directory
                        if (dir.exists() && dir.isDirectory()) {
                            System.setProperty("user.dir", dir.getCanonicalPath());
                        } else {
                            System.out.println("cd: " + targetDir + ": No such file or directory");
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else {
                    System.out.println("cd: missing operand");
                }
            }
            
            // Handle external programs
            else {
                String path = System.getenv("PATH");
                if (DEBUG) System.err.println("[DEBUG] PATH: " + path);
                String[] directories = path.split(":");
                boolean found = false;
                for (String dir : directories) {
                    File file = new File(dir, command);
                    if (DEBUG) System.err.println("[DEBUG] Checking: " + file.getAbsolutePath());
                    if (file.exists() && file.canExecute()) {
                        found = true;
                        try {
                            ProcessBuilder pb = new ProcessBuilder(parts);
                            pb.directory(new File(System.getProperty("user.dir")));
                            pb.redirectErrorStream(true);
                            Process process = pb.start();
                            Scanner processScanner = new Scanner(process.getInputStream());
                            while (processScanner.hasNextLine()) {
                                System.out.println(processScanner.nextLine());
                            }
                            process.waitFor();
                        } catch (IOException | InterruptedException e) {
                            e.printStackTrace();
                        }
                        break;
                    }
                }
                if (!found) {
                    System.out.println(command + ": command not found");
                }
            }
        }
    }

    // Modified method to split command line into arguments considering quotes and backslashes
    public static String[] splitCommandLine(String commandLine) {
        List<String> tokens = new ArrayList<>();
        char[] chars = commandLine.toCharArray();
        StringBuilder token = new StringBuilder();
        boolean insideSingleQuote = false;
        boolean insideDoubleQuote = false;
        boolean escapeNext = false;

        for (int i = 0; i < chars.length; i++) {
            char c = chars[i];

            if (escapeNext) {
                // Append the next character literally
                token.append(c);
                escapeNext = false;
            } else if (c == '\\' && !insideSingleQuote) {
                // Escape character is activated
                escapeNext = true;
            } else if (c == '\'' && !insideDoubleQuote) {
                // Toggle single quote
                insideSingleQuote = !insideSingleQuote;
            } else if (c == '\"' && !insideSingleQuote) {
                // Toggle double quote
                insideDoubleQuote = !insideDoubleQuote;
            } else if (c == ' ' && !insideSingleQuote && !insideDoubleQuote) {
                // End the token if outside quotes
                if (token.length() > 0) {
                    tokens.add(token.toString());
                    token.setLength(0);
                }
            } else {
                // Add character to token
                token.append(c);
            }
        }

        if (token.length() > 0) {
            tokens.add(token.toString());
        }
        return tokens.toArray(new String[0]);
    }
}
