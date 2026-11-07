import java.io.*;
import java.util.*;

/**
 * Handles persistence of quiz Results to results.txt and provides
 * a leaderboard view (sorted by percentage, descending).
 */
public class ResultManager {

    private static final String FILE_PATH = "results.txt";

    public void saveResult(Result result) {
        try (FileWriter fw = new FileWriter(FILE_PATH, true); // append mode
             PrintWriter writer = new PrintWriter(fw)) {
            writer.println(result.toFileString());
        } catch (IOException e) {
            System.out.println("Error saving result: " + e.getMessage());
        }
    }

    public List<Result> getAllResults() {
        List<Result> results = new ArrayList<>();
        File file = new File(FILE_PATH);
        if (!file.exists()) {
            return results;
        }
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                Result r = Result.fromFileString(line);
                if (r != null) {
                    results.add(r);
                }
            }
        } catch (IOException e) {
            System.out.println("Warning: could not read results.txt (" + e.getMessage() + ")");
        }
        return results;
    }

    /** Returns all results sorted by percentage, highest first. */
    public List<Result> getLeaderboard() {
        List<Result> results = getAllResults();
        results.sort((a, b) -> Double.compare(b.getPercentage(), a.getPercentage()));
        return results;
    }
}
