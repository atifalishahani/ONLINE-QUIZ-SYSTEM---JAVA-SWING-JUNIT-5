import java.util.*;

/**
 * Entry point for the Online Quiz System.
 * Presents a main menu: Take Quiz (student), Admin Panel (manage questions),
 * View Leaderboard, and Exit.
 *
 * Run with:  java QuizSystemApp   (after compiling with javac *.java)
 */
public class QuizSystemApp {

    private static final String ADMIN_PASSWORD = "admin123";

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        QuestionBank bank = new QuestionBank();
        ResultManager resultManager = new ResultManager();

        System.out.println("=================================================");
        System.out.println("        WELCOME TO THE ONLINE QUIZ SYSTEM       ");
        System.out.println("=================================================");

        boolean exit = false;
        while (!exit) {
            System.out.println("\n===== MAIN MENU =====");
            System.out.println("1. Take Quiz (Student)");
            System.out.println("2. Admin Panel (Manage Questions)");
            System.out.println("3. View Leaderboard");
            System.out.println("4. Exit");
            System.out.print("Choose an option: ");
            String choice = scanner.nextLine().trim();

            switch (choice) {
                case "1":
                    takeQuiz(scanner, bank, resultManager);
                    break;
                case "2":
                    if (authenticateAdmin(scanner)) {
                        new AdminPanel(scanner, bank).showMenu();
                    } else {
                        System.out.println("Incorrect password. Access denied.");
                    }
                    break;
                case "3":
                    showLeaderboard(resultManager);
                    break;
                case "4":
                    exit = true;
                    System.out.println("Thank you for using the Online Quiz System. Goodbye!");
                    break;
                default:
                    System.out.println("Invalid choice. Please enter 1-4.");
            }
        }
        scanner.close();
    }

    private static boolean authenticateAdmin(Scanner scanner) {
        System.out.print("Enter admin password: ");
        String pass = scanner.nextLine();
        return ADMIN_PASSWORD.equals(pass);
    }

    private static void takeQuiz(Scanner scanner, QuestionBank bank, ResultManager resultManager) {
        if (bank.size() == 0) {
            System.out.println("No questions available yet. Please ask admin to add some.");
            return;
        }

        System.out.print("Enter your name: ");
        String name = scanner.nextLine().trim();
        if (name.isEmpty()) name = "Anonymous";

        int available = bank.size();
        int numQuestions;
        while (true) {
            System.out.print("How many questions do you want to attempt (1-" + available + ")? ");
            try {
                numQuestions = Integer.parseInt(scanner.nextLine().trim());
                if (numQuestions >= 1 && numQuestions <= available) {
                    break;
                }
                System.out.println("Please enter a number between 1 and " + available + ".");
            } catch (NumberFormatException e) {
                System.out.println("Invalid number, try again.");
            }
        }

        List<Question> quizQuestions = bank.getRandomQuestions(numQuestions);
        QuizEngine engine = new QuizEngine(scanner);
        Result result = engine.conductQuiz(name, quizQuestions);

        ScoreCalculator sc = new ScoreCalculator();
        System.out.println("\n===== QUIZ COMPLETE =====");
        System.out.println("Name       : " + result.getStudentName());
        System.out.println("Score      : " + result.getScore() + " / " + result.getTotal());
        System.out.printf("Percentage : %.2f%%\n", result.getPercentage());
        System.out.println("Grade      : " + sc.getGrade(result.getPercentage()));

        resultManager.saveResult(result);
        System.out.println("(Result saved to results.txt)");
    }

    private static void showLeaderboard(ResultManager resultManager) {
        List<Result> board = resultManager.getLeaderboard();
        if (board.isEmpty()) {
            System.out.println("No quiz attempts recorded yet.");
            return;
        }
        System.out.println("\n===== LEADERBOARD =====");
        int rank = 1;
        for (Result r : board) {
            System.out.println(rank + ". " + r);
            rank++;
        }
    }
}
