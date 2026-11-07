import java.util.*;

/**
 * Conducts an interactive quiz session: prints each question, reads and
 * validates the student's input, and computes the final Result using
 * ScoreCalculator.
 */
public class QuizEngine {

    private Scanner scanner;
    private ScoreCalculator scoreCalculator;

    public QuizEngine(Scanner scanner) {
        this.scanner = scanner;
        this.scoreCalculator = new ScoreCalculator();
    }

    /**
     * Runs the quiz for the given list of questions and returns a Result.
     * Robust to invalid input: keeps re-prompting until the student enters
     * a number between 1 and 4 (or 0 to skip the question).
     */
    public Result conductQuiz(String studentName, List<Question> questions) {
        List<Integer> answers = new ArrayList<>();
        int qNum = 1;

        for (Question q : questions) {
            System.out.println("\nQ" + qNum + ". [" + q.getCategory() + "] " + q.getQuestionText());
            String[] opts = q.getOptions();
            for (int i = 0; i < opts.length; i++) {
                System.out.println("  " + (i + 1) + ". " + opts[i]);
            }
            int answerIndex = readValidatedAnswer();
            answers.add(answerIndex);
            qNum++;
        }

        int score = scoreCalculator.calculateScore(questions, answers);
        return new Result(studentName, score, questions.size());
    }

    /**
     * Reads the student's choice (1-4), validating input so the program
     * never crashes on bad input (letters, out-of-range numbers, blanks).
     * Enter 0 to skip a question (counts as wrong).
     */
    private int readValidatedAnswer() {
        while (true) {
            System.out.print("Your answer (1-4, or 0 to skip): ");
            String input = scanner.nextLine().trim();
            try {
                int choice = Integer.parseInt(input);
                if (choice == 0) {
                    return -1; // skipped, treated as incorrect
                }
                if (choice >= 1 && choice <= 4) {
                    return choice - 1; // convert to 0-based index
                }
                System.out.println("Please enter a number between 0 and 4.");
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter a number.");
            }
        }
    }
}
