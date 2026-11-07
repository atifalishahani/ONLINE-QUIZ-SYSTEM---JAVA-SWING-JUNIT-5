import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * JUnit 5 unit tests for ScoreCalculator.
 * These are WHITE-BOX tests: they exercise the internal scoring logic
 * directly, independent of the console UI (QuizEngine), so they can
 * run automatically without any user input.
 *
 * To run: see "How to run the JUnit tests" in README.txt
 */
public class ScoreCalculatorTest {

    private ScoreCalculator calculator = new ScoreCalculator();

    private Question sampleQuestion(int correctIndex) {
        return new Question("Test", "Sample question?",
                new String[] { "A", "B", "C", "D" }, correctIndex);
    }

    @Test
    void testAllAnswersCorrect() {
        List<Question> questions = Arrays.asList(sampleQuestion(0), sampleQuestion(1), sampleQuestion(2));
        List<Integer> answers = Arrays.asList(0, 1, 2);
        assertEquals(3, calculator.calculateScore(questions, answers));
    }

    @Test
    void testAllAnswersWrong() {
        List<Question> questions = Arrays.asList(sampleQuestion(0), sampleQuestion(1));
        List<Integer> answers = Arrays.asList(3, 3);
        assertEquals(0, calculator.calculateScore(questions, answers));
    }

    @Test
    void testMixedAnswers() {
        List<Question> questions = Arrays.asList(sampleQuestion(0), sampleQuestion(1), sampleQuestion(2));
        List<Integer> answers = Arrays.asList(0, 3, 2); // 2 correct, 1 wrong
        assertEquals(2, calculator.calculateScore(questions, answers));
    }

    @Test
    void testSkippedQuestionCountsAsWrong() {
        List<Question> questions = Arrays.asList(sampleQuestion(0));
        List<Integer> answers = Arrays.asList(-1); // skipped
        assertEquals(0, calculator.calculateScore(questions, answers));
    }

    @Test
    void testMismatchedListSizesThrowsException() {
        List<Question> questions = Arrays.asList(sampleQuestion(0), sampleQuestion(1));
        List<Integer> answers = Arrays.asList(0); // only one answer for two questions
        assertThrows(IllegalArgumentException.class, () -> calculator.calculateScore(questions, answers));
    }

    @Test
    void testEmptyQuizReturnsZero() {
        List<Question> questions = new ArrayList<>();
        List<Integer> answers = new ArrayList<>();
        assertEquals(0, calculator.calculateScore(questions, answers));
    }

    @Test
    void testPercentageCalculation() {
        assertEquals(50.0, calculator.calculatePercentage(5, 10));
        assertEquals(100.0, calculator.calculatePercentage(4, 4));
        assertEquals(0.0, calculator.calculatePercentage(0, 5));
    }

    @Test
    void testPercentageWithZeroTotalDoesNotCrash() {
        assertEquals(0.0, calculator.calculatePercentage(0, 0));
    }

    @Test
    void testGradeBoundaries() {
        assertEquals("A+ (Excellent)", calculator.getGrade(95));
        assertEquals("A (Very Good)", calculator.getGrade(80));
        assertEquals("B (Good)", calculator.getGrade(65));
        assertEquals("C (Pass)", calculator.getGrade(45));
        assertEquals("F (Fail)", calculator.getGrade(20));
    }
}
