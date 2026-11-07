/**
 * Represents one four-option multiple-choice question.
 */
public class Question {
    private String category;
    private String questionText;
    private String[] options;
    private int correctOption;

    public Question(String category, String questionText, String[] options, int correctOption) {
        if (options == null || options.length != 4) {
            throw new IllegalArgumentException("A question must have exactly 4 options.");
        }
        if (correctOption < 0 || correctOption > 3) {
            throw new IllegalArgumentException("Correct option must be between 0 and 3.");
        }
        this.category = category == null ? "General" : category;
        this.questionText = questionText == null ? "" : questionText;
        this.options = options.clone();
        this.correctOption = correctOption;
    }

    public String getCategory() { return category; }
    public String getQuestionText() { return questionText; }
    public String[] getOptions() { return options.clone(); }
    public int getCorrectOption() { return correctOption; }

    public boolean isCorrect(int answerIndex) {
        return answerIndex == correctOption;
    }

    /** Return a copy with the answer choices shuffled. */
    public Question shuffledOptions() {
        java.util.List<String> shuffled = new java.util.ArrayList<>();
        for (String option : options) shuffled.add(option);
        String correctText = options[correctOption];
        java.util.Collections.shuffle(shuffled);
        int newCorrect = shuffled.indexOf(correctText);
        return new Question(category, questionText,
                shuffled.toArray(new String[0]), newCorrect);
    }

    public String toFileString() {
        return safe(category) + "|" + safe(questionText) + "|" +
                safe(options[0]) + "|" + safe(options[1]) + "|" +
                safe(options[2]) + "|" + safe(options[3]) + "|" + correctOption;
    }

    private static String safe(String value) {
        return value.replace("|", "/");
    }

    public static Question fromFileString(String line) {
        if (line == null || line.trim().isEmpty()) return null;
        String[] parts = line.split("\\|", -1);
        if (parts.length != 7) return null;
        try {
            String[] options = {parts[2], parts[3], parts[4], parts[5]};
            int correct = Integer.parseInt(parts[6].trim());
            if (correct < 0 || correct > 3) return null;
            if (parts[1].trim().isEmpty()) return null;
            return new Question(parts[0], parts[1], options, correct);
        } catch (RuntimeException e) {
            return null;
        }
    }

    @Override
    public String toString() {
        return "[" + category + "] " + questionText;
    }
}
