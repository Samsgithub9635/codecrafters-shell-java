import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class Main {

    public static void main(String[] args) {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(System.in))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) {
                    continue;
                }

                List<String> tokens = parseCommand(line);
                if (tokens.isEmpty()) {
                    continue;
                }

                String command = tokens.get(0);
                List<String> arguments = tokens.subList(1, tokens.size());

                if (command.equals("exit")) {
                    break;
                }

                executeCommand(command, arguments);

                // Print the prompt after executing the command
                System.out.print("$ ");
            }
        } catch (Exception e) {
            System.err.println("An error occurred: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static List<String> parseCommand(String line) {
        List<String> tokens = new ArrayList<>();
        StringBuilder currentToken = new StringBuilder();
        boolean inSingleQuotes = false;
        boolean inDoubleQuotes = false;

        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);

            if (c == '\\' && i + 1 < line.length()) {
                char nextChar = line.charAt(i + 1);
                currentToken.append(nextChar);
                i++; // Skip the next character as it's escaped
            } else if (c == '\'') {
                inSingleQuotes = !inSingleQuotes;
            } else if (c == '"') {
                inDoubleQuotes = !inDoubleQuotes;
            } else if (Character.isWhitespace(c) && !inSingleQuotes && !inDoubleQuotes) {
                if (currentToken.length() > 0) {
                    tokens.add(currentToken.toString());
                    currentToken.setLength(0);
                }
            } else {
                currentToken.append(c);
            }
        }

        if (currentToken.length() > 0) {
            tokens.add(currentToken.toString());
        }

        return tokens;
    }

    private static void executeCommand(String command, List<String> arguments) {
        try {
            List<String> commandWithArgs = new ArrayList<>();
            commandWithArgs.add(command);
            commandWithArgs.addAll(arguments);

            ProcessBuilder processBuilder = new ProcessBuilder(commandWithArgs);
            processBuilder.directory(new File(System.getProperty("user.dir")));
            processBuilder.redirectErrorStream(true);

            Process process = processBuilder.start();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    System.out.println(line);
                }
            }

            int exitCode = process.waitFor();
            if (exitCode != 0) {
                System.err.println("Command exited with code " + exitCode);
            }
        } catch (Exception e) {
            System.err.println("Failed to execute command: " + e.getMessage());
        }
    }
}
