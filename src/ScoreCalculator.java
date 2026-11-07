import java.util.List;

/**
 * Pure logic class (no console I/O) responsible for scoring a quiz
 * attempt. Kept separate from QuizEngine so it can be unit-tested
 * in isolation without needing user input (important for SQE / JUnit testing).
 */
public class ScoreCalculator {

    /**
     * Given the list of questions asked and the list of answers the
     * student gave (same order, same size, using -1 for "not answered"),
     * returns the number of correct answers.
     *
     * @throws IllegalArgumentException if the two lists differ in size
     */
    public int calculateScore(List<Question> questions, List<Integer> answers) {
        if (questions.size() != answers.size()) {
            throw new IllegalArgumentException("Questions and answers lists must be the same size");
        }
        int score = 0;
        for (int i = 0; i < questions.size(); i++) {
            int given = answers.get(i);
            if (questions.get(i).isCorrect(given)) {
                score++;
            }
        }
        return score;
    }

    public double calculatePercentage(int score, int total) {
        if (total <= 0) {
            return 0.0;
        }
        return (score * 100.0) / total;
    }

    /** Simple grading rule used for feedback messages. */
    public String getGrade(double percentage) {
        if (percentage >= 90) return "A+ (Excellent)";
        if (percentage >= 75) return "A (Very Good)";
        if (percentage >= 60) return "B (Good)";
        if (percentage >= 40) return "C (Pass)";
        return "F (Fail)";
    }
}
