import java.io.IOException;
import java.net.URI;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Small dependency-free client for the free Open Trivia DB API.
 * No API key is required. A session token is used when possible so
 * repeated API calls do not intentionally return the same questions.
 */
public class OpenTriviaApiClient {
    private static final String API_BASE = "https://opentdb.com/api.php";
    private final HttpClient client;
    private String sessionToken;

    public OpenTriviaApiClient() {
        client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(7))
                .build();
    }

    public boolean isOnline() {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://opentdb.com/api_count_global.php"))
                    .timeout(Duration.ofSeconds(5))
                    .GET().build();
            return client.send(request, HttpResponse.BodyHandlers.ofString()).statusCode() == 200;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Fetches multiple-choice questions from Open Trivia DB.
     * Supported category IDs include 9 (General Knowledge), 17 (Science & Nature)
     * and 18 (Science: Computers).
     */
    public List<Question> fetchQuestions(String subject, int amount) throws IOException, InterruptedException {
        int category = categoryFor(subject);
        if (category < 0) return Collections.emptyList();

        ensureSessionToken();
        int remaining = Math.max(1, amount);
        List<Question> result = new ArrayList<>();
        Set<String> seen = new HashSet<>();

        // API maximum is 50 per call.
        while (remaining > 0 && result.size() < amount) {
            int batch = Math.min(50, remaining);
            String url = API_BASE + "?amount=" + batch +
                    "&category=" + category +
                    "&type=multiple&encode=url3986";
            if (sessionToken != null && !sessionToken.isEmpty()) {
                url += "&token=" + URLEncoder.encode(sessionToken, StandardCharsets.UTF_8);
            }

            String json = get(url);
            int responseCode = intField(json, "response_code", -1);
            if (responseCode == 4 && sessionToken != null) {
                resetSessionToken();
                continue;
            }
            if (responseCode != 0) break;

            List<String> objects = resultObjects(json);
            if (objects.isEmpty()) break;

            for (String object : objects) {
                String questionText = decodeJsonValue(stringField(object, "question"));
                String correct = decodeJsonValue(stringField(object, "correct_answer"));
                List<String> incorrect = stringArrayField(object, "incorrect_answers");
                if (questionText.isEmpty() || correct.isEmpty() || incorrect.size() != 3) continue;

                List<String> options = new ArrayList<>(incorrect);
                options.add(correct);
                Collections.shuffle(options);
                int correctIndex = options.indexOf(correct);
                Question q = new Question(subject, questionText,
                        options.toArray(new String[0]), correctIndex);

                String key = normalize(questionText);
                if (seen.add(key)) result.add(q);
            }

            remaining = amount - result.size();
            if (objects.size() < batch) break;
        }
        return result;
    }

    private int categoryFor(String subject) {
        if (subject == null) return -1;
        String s = subject.toLowerCase(Locale.ROOT);
        if (s.contains("general")) return 9;
        if (s.contains("science") || s.contains("nature")) return 17;
        if (s.contains("computer")) return 18;
        return -1;
    }

    private void ensureSessionToken() throws IOException, InterruptedException {
        if (sessionToken != null && !sessionToken.isEmpty()) return;
        String json = get("https://opentdb.com/api_token.php?command=request");
        if (intField(json, "response_code", -1) == 0) {
            sessionToken = decodeJsonValue(stringField(json, "token"));
        }
    }

    private void resetSessionToken() throws IOException, InterruptedException {
        if (sessionToken == null) return;
        String url = "https://opentdb.com/api_token.php?command=reset&token=" +
                URLEncoder.encode(sessionToken, StandardCharsets.UTF_8);
        get(url);
    }

    private String get(String url) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofSeconds(10))
                .header("Accept", "application/json")
                .GET().build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != 200) {
            throw new IOException("API returned HTTP " + response.statusCode());
        }
        return response.body();
    }

    private static int intField(String json, String field, int fallback) {
        Matcher m = Pattern.compile("\"" + Pattern.quote(field) + "\"\\s*:\\s*(\\d+)").matcher(json);
        return m.find() ? Integer.parseInt(m.group(1)) : fallback;
    }

    private static String stringField(String json, String field) {
        Pattern p = Pattern.compile("\"" + Pattern.quote(field) +
                "\"\\s*:\\s*\"((?:\\\\.|[^\"\\\\])*)\"");
        Matcher m = p.matcher(json);
        return m.find() ? m.group(1) : "";
    }

    private static List<String> stringArrayField(String json, String field) {
        List<String> values = new ArrayList<>();
        Pattern p = Pattern.compile("\"" + Pattern.quote(field) +
                "\"\\s*:\\s*\\[((?:\\s*\"(?:\\\\.|[^\"\\\\])*\"\\s*,?)+)\\]");
        Matcher m = p.matcher(json);
        if (!m.find()) return values;

        Matcher item = Pattern.compile("\"((?:\\\\.|[^\"\\\\])*)\"").matcher(m.group(1));
        while (item.find()) values.add(decodeJsonValue(item.group(1)));
        return values;
    }

    private static List<String> resultObjects(String json) {
        List<String> objects = new ArrayList<>();
        int resultsPos = json.indexOf("\"results\"");
        if (resultsPos < 0) return objects;
        int start = json.indexOf('[', resultsPos);
        if (start < 0) return objects;

        boolean inString = false;
        boolean escaped = false;
        int depth = 0;
        int objectStart = -1;

        for (int i = start + 1; i < json.length(); i++) {
            char c = json.charAt(i);
            if (inString) {
                if (escaped) escaped = false;
                else if (c == '\\') escaped = true;
                else if (c == '"') inString = false;
                continue;
            }
            if (c == '"') { inString = true; continue; }
            if (c == '{') {
                if (depth == 0) objectStart = i;
                depth++;
            } else if (c == '}') {
                depth--;
                if (depth == 0 && objectStart >= 0) {
                    objects.add(json.substring(objectStart, i + 1));
                    objectStart = -1;
                }
            } else if (c == ']' && depth == 0) {
                break;
            }
        }
        return objects;
    }

    private static String decodeJsonValue(String value) {
        if (value == null || value.isEmpty()) return "";
        String jsonUnescaped = value
                .replace("\\\"", "\"")
                .replace("\\\\", "\\")
                .replace("\\/", "/")
                .replace("\\n", "\n")
                .replace("\\r", "\r")
                .replace("\\t", "\t");
        Matcher unicode = Pattern.compile("\\\\u([0-9a-fA-F]{4})").matcher(jsonUnescaped);
        StringBuffer sb = new StringBuffer();
        while (unicode.find()) {
            char ch = (char) Integer.parseInt(unicode.group(1), 16);
            unicode.appendReplacement(sb, Matcher.quoteReplacement(String.valueOf(ch)));
        }
        unicode.appendTail(sb);
        try {
            return URLDecoder.decode(sb.toString(), StandardCharsets.UTF_8);
        } catch (Exception e) {
            return sb.toString();
        }
    }

    private static String normalize(String text) {
        return text.toLowerCase(Locale.ROOT).replaceAll("\\s+", " ").trim();
    }
}
