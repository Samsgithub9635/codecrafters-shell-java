import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

public class Main {
    private static final boolean DEBUG = false; // Set to true for debug logs

    public static void main(String[] args) throws Exception {
        Scanner scanner = new Scanner(System.in);
        List<String> builtins = Arrays.asList("echo", "exit", "type", "pwd", "cd", "cat");

        while (true) {
            System.out.print("$ ");
            String input = scanner.nextLine();
            if (DEBUG) System.err.println("[DEBUG] Command received: " + input);

            String[] parts = splitCommandLine(input); // Enhanced split for backslash handling
            if (parts.length == 0) continue;
            String command = parts[0];

            if (command.equals("exit") && parts.length > 1 && parts[1].equals("0")) {
                break;
            } else if (command.equals("echo")) {
                if (parts.length > 1) {
                    System.out.println(String.join(" ", Arrays.copyOfRange(parts, 1, parts.length)));
                }
            } else if (command.equals("type")) {
                if (parts.length > 1) {
                    handleTypeCommand(parts[1], builtins);
                } else {
                    System.out.println("type: command not found");
                }
            } else if (command.equals("pwd")) {
                System.out.println(System.getProperty("user.dir"));
            } else if (command.equals("cd")) {
                handleCdCommand(parts);
            } else if (command.equals("cat")) {
                handleCatCommand(parts);
            } else {
                handleExternalCommand(parts);
            }
        }
    }

    // Method to handle the 'type' command
    private static void handleTypeCommand(String typeCommand, List<String> builtins) {
        if (builtins.contains(typeCommand)) {
            System.out.println(typeCommand + " is a shell builtin");
        } else {
            String path = System.getenv("PATH");
            String[] directories = path.split(":");
            boolean found = false;
            for (String dir : directories) {
                File file = new File(dir, typeCommand);
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
    }

    // Method to handle the 'cd' command
    private static void handleCdCommand(String[] parts) {
        if (parts.length > 1) {
            String targetDir = parts[1];
            if (targetDir.equals("~")) {
                targetDir = System.getenv("HOME");
            }
            File dir = new File(targetDir);
            if (!dir.isAbsolute()) {
                dir = new File(System.getProperty("user.dir"), targetDir);
            }
            try {
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

    // Method to handle the 'cat' command
    private static void handleCatCommand(String[] parts) {
        for (int i = 1; i < parts.length; i++) {
            String filePath = parts[i];
            File file = new File(filePath);
            if (file.exists()) {
                try (Scanner fileScanner = new Scanner(file)) {
                    while (fileScanner.hasNextLine()) {
                        System.out.print(fileScanner.nextLine());
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                System.out.println("cat: " + filePath + ": No such file or directory");
            }
        }
    }

    // Method to handle external commands
    private static void handleExternalCommand(String[] parts) {
        String command = parts[0];
        String path = System.getenv("PATH");
        String[] directories = path.split(":");
        boolean found = false;
        for (String dir : directories) {
            File file = new File(dir, command);
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

    // Enhanced method to split command line arguments considering quotes and backslashes
    public static String[] splitCommandLine(String commandLine) {
        List<String> tokens = new ArrayList<>();
        char[] chars = commandLine.toCharArray();
        StringBuilder token = new StringBuilder();
        boolean insideSingleQuote = false;
        boolean insideDoubleQuote = false;
        boolean escapeNext = false;

        for (char c : chars) {
            if (escapeNext) {
                switch (c) {
                    case 'n': token.append('\n'); break;
                    case 't': token.append('\t'); break;
                    case '\'': token.append('\''); break;
                    case '\"': token.append('\"'); break;
                    case '\\': token.append('\\'); break;
                    default: token.append(c); break;
                }
                escapeNext = false;
            } else if (c == '\\') {
                if (insideSingleQuote) {
                    token.append(c);
                } else {
                    escapeNext = true;
                }
            } else if (c == '\'' && !insideDoubleQuote) {
                insideSingleQuote = !insideSingleQuote;
            } else if (c == '\"' && !insideSingleQuote) {
                insideDoubleQuote = !insideDoubleQuote;
            } else if (c == ' ' && !insideSingleQuote && !insideDoubleQuote) {
                if (token.length() > 0) {
                    tokens.add(token.toString());
                    token.setLength(0);
                }
            } else {
                token.append(c);
            }
        }

        if (token.length() > 0) {
            tokens.add(token.toString());
        }

        return tokens.toArray(new String[0]);
    }
}
