import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

public class Main {
    private static final boolean DEBUG = true; // Set to false to disable debug logs

    public static void main(String[] args) throws Exception {
        Scanner scanner = new Scanner(System.in);
        List<String> builtins = Arrays.asList("echo", "exit", "type", "pwd");

        while (true) {
            // Print the prompt
            System.out.print("$ ");

            // Read user input
            String input = scanner.nextLine();
            if (DEBUG) System.out.println("[DEBUG] Command received: " + input);
            
            // Parse the input considering backslashes and quotes
            String[] parts = parseInput(input);
            if (parts == null || parts.length == 0) continue;

            String command = parts[0];

            // Check for the exit command
            if (command.equals("exit") && parts.length > 1 && parts[1].equals("0")) {
                break;
            }

            // Check for the echo command
            else if (command.equals("echo")) {
                System.out.println(input.substring(5));
            }

            // Check for the type command
            else if (command.equals("type")) {
                if (parts.length > 1) {
                    String typeCommand = parts[1];
                    if (builtins.contains(typeCommand)) {
                        System.out.println(typeCommand + " is a shell builtin");
                    } else {
                        String path = System.getenv("PATH");
                        if (DEBUG) System.out.println("[DEBUG] PATH: " + path);
                        String[] directories = path.split(":");
                        boolean found = false;
                        for (String dir : directories) {
                            File file = new File(dir, typeCommand);
                            if (DEBUG) System.out.println("[DEBUG] Checking: " + file.getAbsolutePath());
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
                if (DEBUG) System.out.println("[DEBUG] Current working directory: " + currentDir);
                System.out.println(currentDir);
            }

            // Handle external programs
            else {
                String path = System.getenv("PATH");
                if (DEBUG) System.out.println("[DEBUG] PATH: " + path);
                String[] directories = path.split(":");
                boolean found = false;
                for (String dir : directories) {
                    File file = new File(dir, command);
                    if (DEBUG) System.out.println("[DEBUG] Checking: " + file.getAbsolutePath());
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

    private static String[] parseInput(String input) {
        StringBuilder currentToken = new StringBuilder();
        boolean inSingleQuotes = false;
        boolean inDoubleQuotes = false;
        boolean escapeNext = false;
        List<String> tokens = new ArrayList<>();

        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);

            if (escapeNext) {
                // Handle escape character, add the next character literally
                currentToken.append(c);
                escapeNext = false;
            } else if (c == '\\') {
                // If the character is a backslash, escape the next character
                escapeNext = true;
            } else if (c == '\'' && !inDoubleQuotes) {
                // Toggle single quote state
                inSingleQuotes = !inSingleQuotes;
            } else if (c == '"' && !inSingleQuotes) {
                // Toggle double quote state
                inDoubleQuotes = !inDoubleQuotes;
            } else if (Character.isWhitespace(c) && !inSingleQuotes && !inDoubleQuotes) {
                // If space and we're not inside quotes, split into token
                if (currentToken.length() > 0) {
                    tokens.add(currentToken.toString());
                    currentToken.setLength(0);
                }
            } else {
                currentToken.append(c);
            }
        }

        // Add the last token if exists
        if (currentToken.length() > 0) {
            tokens.add(currentToken.toString());
        }

        return tokens.toArray(new String[0]);
    }
}
