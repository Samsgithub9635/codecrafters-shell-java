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
        List<String> builtins = Arrays.asList("echo", "exit", "type", "pwd", "cd");

        while (true) {
            System.out.print("$ ");
            String input = scanner.nextLine();
            if (DEBUG) System.err.println("[DEBUG] Command received: " + input);

            String[] parts = splitCommandLine(input); // (+ Updated method for enhanced backslash handling)
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
                    String typeCommand = parts[1];
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
                } else {
                    System.out.println("type: command not found");
                }
            } else if (command.equals("pwd")) {
                System.out.println(System.getProperty("user.dir"));
            } else if (command.equals("cd")) {
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
            } else if (command.equals("cat")) { // (+ Updated cat command logic)
                for (int i = 1; i < parts.length; i++) {
                    File file = new File(parts[i]);
                    if (file.exists()) {
                        try (Scanner fileScanner = new Scanner(file)) {
                            while (fileScanner.hasNextLine()) {
                                System.out.print(fileScanner.nextLine());
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    } else {
                        System.out.println("cat: " + parts[i] + ": No such file or directory");
                    }
                }
            } else {
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

        for (char c : chars) {
            if (escapeNext) {
                switch (c) { // (+ Added detailed backslash handling)
                    case 'n': token.append('\n'); break;
                    case 't': token.append('\t'); break;
                    case '\'': token.append('\''); break;
                    case '\"': token.append('\"'); break;
                    case '\\': token.append('\\'); break;
                    default: token.append(c); break;
                }
                escapeNext = false;
            } else if (c == '\\' && !insideSingleQuote) {
                escapeNext = true;
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
