import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class QuestionBankTest {
    private QuestionBank bank;
    private static final String TEST_FILE = "questions.txt";

    @BeforeEach
    void setUp() {
        new File(TEST_FILE).delete();
        bank = new QuestionBank();
    }

    @AfterEach
    void tearDown() {
        new File(TEST_FILE).delete();
    }

    @Test
    void testQuestionSerializationRoundTrip() {
        Question q = new Question("Java", "What is JVM?",
                new String[]{"A compiler", "A runtime engine", "An IDE", "A library"}, 1);
        Question restored = Question.fromFileString(q.toFileString());

        assertNotNull(restored);
        assertEquals(q.getCategory(), restored.getCategory());
        assertEquals(q.getQuestionText(), restored.getQuestionText());
        assertArrayEquals(q.getOptions(), restored.getOptions());
        assertEquals(q.getCorrectOption(), restored.getCorrectOption());
    }

    @Test
    void testMalformedQuestionIsRejected() {
        assertNull(Question.fromFileString("Java|Incomplete line|OnlyOneOption"));
        assertNull(Question.fromFileString("Java|Q?|A|B|C|D|9"));
        assertNull(Question.fromFileString("Java|Q?|A|B|C|D|xyz"));
    }

    @Test
    void testIsCorrect() {
        Question q = new Question("Cat", "Q?",
                new String[]{"A", "B", "C", "D"}, 2);
        assertTrue(q.isCorrect(2));
        assertFalse(q.isCorrect(0));
        assertFalse(q.isCorrect(-1));
    }

    @Test
    void testBankSeedsManyQuestions() {
        assertTrue(bank.size() >= 60, "The starter bank should contain many subject questions");
    }

    @Test
    void testSubjectFiltering() {
        List<Question> java = bank.getQuestionsForSubject("Java");
        assertFalse(java.isEmpty());
        assertTrue(java.stream().allMatch(q -> q.getCategory().equals("Java")));
    }

    @Test
    void testRandomQuestionsDoNotRepeatWithinQuiz() {
        List<Question> quiz = bank.getRandomQuestions("Java", 10);
        Set<String> unique = new HashSet<>();
        for (Question q : quiz) unique.add(q.getQuestionText());
        assertEquals(quiz.size(), unique.size());
    }

    @Test
    void testRandomQuestionsCapsAtAvailableSubjectQuestions() {
        int total = bank.sizeForSubject("Java");
        List<Question> quiz = bank.getRandomQuestions("Java", total + 100);
        assertEquals(total, quiz.size());
    }

    @Test
    void testAddAndDeleteQuestion() {
        int before = bank.size();
        bank.addQuestion(new Question("Test", "New Q?",
                new String[]{"1", "2", "3", "4"}, 0));
        assertEquals(before + 1, bank.size());

        assertTrue(bank.deleteQuestion(bank.size()));
        assertEquals(before, bank.size());
        assertFalse(bank.deleteQuestion(9999));
    }
}
