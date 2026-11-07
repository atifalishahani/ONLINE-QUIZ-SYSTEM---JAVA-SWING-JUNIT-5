import java.util.*;

/**
 * Console-based admin panel for managing the question bank:
 * add questions, view all questions, and delete a question.
 * Protected by a simple password check in QuizSystemApp.
 */
public class AdminPanel {

    private Scanner scanner;
    private QuestionBank bank;

    public AdminPanel(Scanner scanner, QuestionBank bank) {
        this.scanner = scanner;
        this.bank = bank;
    }

    public void showMenu() {
        boolean back = false;
        while (!back) {
            System.out.println("\n===== ADMIN PANEL =====");
            System.out.println("1. Add New Question");
            System.out.println("2. View All Questions");
            System.out.println("3. Delete a Question");
            System.out.println("4. Back to Main Menu");
            System.out.print("Choose an option: ");
            String choice = scanner.nextLine().trim();

            switch (choice) {
                case "1": addQuestion(); break;
                case "2": viewQuestions(); break;
                case "3": deleteQuestion(); break;
                case "4": back = true; break;
                default: System.out.println("Invalid choice. Try again.");
            }
        }
    }

    private void addQuestion() {
        System.out.print("Enter category (e.g. Java, SQE, General): ");
        String category = scanner.nextLine().trim();
        if (category.isEmpty()) category = "General";

        System.out.print("Enter question text: ");
        String questionText = scanner.nextLine().trim();
        if (questionText.isEmpty()) {
            System.out.println("Question text cannot be empty. Cancelled.");
            return;
        }

        String[] options = new String[4];
        for (int i = 0; i < 4; i++) {
            System.out.print("Enter option " + (i + 1) + ": ");
            options[i] = scanner.nextLine().trim();
        }

        int correct;
        while (true) {
            System.out.print("Which option number is correct (1-4)? ");
            try {
                correct = Integer.parseInt(scanner.nextLine().trim());
                if (correct >= 1 && correct <= 4) {
                    correct -= 1;
                    break;
                }
                System.out.println("Enter a number between 1 and 4.");
            } catch (NumberFormatException e) {
                System.out.println("Invalid number, try again.");
            }
        }

        bank.addQuestion(new Question(category, questionText, options, correct));
        System.out.println("Question added successfully!");
    }

    private void viewQuestions() {
        List<Question> all = bank.getAllQuestions();
        if (all.isEmpty()) {
            System.out.println("No questions in the bank yet.");
            return;
        }
        System.out.println("\n----- All Questions (" + all.size() + ") -----");
        for (int i = 0; i < all.size(); i++) {
            Question q = all.get(i);
            System.out.println((i + 1) + ". " + q);
            String[] opts = q.getOptions();
            for (int j = 0; j < opts.length; j++) {
                String marker = (j == q.getCorrectOption()) ? " (correct)" : "";
                System.out.println("     " + (j + 1) + ". " + opts[j] + marker);
            }
        }
    }

    private void deleteQuestion() {
        viewQuestions();
        if (bank.size() == 0) return;
        System.out.print("Enter question number to delete: ");
        try {
            int idx = Integer.parseInt(scanner.nextLine().trim());
            boolean ok = bank.deleteQuestion(idx);
            System.out.println(ok ? "Deleted." : "Invalid question number.");
        } catch (NumberFormatException e) {
            System.out.println("Invalid input.");
        }
    }
}
