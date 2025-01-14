import java.util.Scanner;

public class Main {
    public static void main(String[] args) throws Exception {
        // Uncomment this block to pass the first stage
        System.out.print("$ ");

        Scanner scanner = new Scanner(System.in);
        // In this stage, you'll implement support for handling invalid commands in your shell.
        // $ invalid_command
        // invalid_command: command not found
        String input = scanner.nextLine();
        System.out.println(input + ": command not found");
    }
}
