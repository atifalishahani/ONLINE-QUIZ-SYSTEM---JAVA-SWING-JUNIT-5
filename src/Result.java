import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Represents the outcome of one quiz attempt by a student:
 * their name, score, total questions attempted, and timestamp.
 */
public class Result {

    private String studentName;
    private int score;
    private int total;
    private String timestamp;

    public Result(String studentName, int score, int total) {
        this.studentName = studentName;
        this.score = score;
        this.total = total;
        this.timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }

    // Constructor used when reloading from file (timestamp already known)
    public Result(String studentName, int score, int total, String timestamp) {
        this.studentName = studentName;
        this.score = score;
        this.total = total;
        this.timestamp = timestamp;
    }

    public String getStudentName() {
        return studentName;
    }

    public int getScore() {
        return score;
    }

    public int getTotal() {
        return total;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public double getPercentage() {
        if (total == 0) return 0.0;
        return (score * 100.0) / total;
    }

    public String toFileString() {
        return studentName + "|" + score + "|" + total + "|" + timestamp;
    }

    public static Result fromFileString(String line) {
        if (line == null || line.trim().isEmpty()) return null;
        String[] parts = line.split("\\|");
        if (parts.length != 4) return null;
        try {
            return new Result(parts[0], Integer.parseInt(parts[1]), Integer.parseInt(parts[2]), parts[3]);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    @Override
    public String toString() {
        return String.format("%-15s Score: %d/%d (%.1f%%)  [%s]", studentName, score, total, getPercentage(), timestamp);
    }
}
