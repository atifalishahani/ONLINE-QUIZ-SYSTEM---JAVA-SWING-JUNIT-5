import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;

/**
 * Main Swing GUI for the Online Quiz System.
 *
 * Features:
 * - Responsive/wrapped question and answer text
 * - Subject selection
 * - User-selected question count
 * - Random question order and randomized answer order
 * - Online Open Trivia DB questions where a matching public category exists
 * - Local subject-specific question bank as a reliable fallback
 */
public class QuizGUIApp {
    private static final Color PRIMARY = new Color(99, 91, 255);
    private static final Color PRIMARY_DARK = new Color(70, 61, 214);
    private static final Color SUCCESS = new Color(39, 174, 96);
    private static final Color DANGER = new Color(231, 76, 60);
    private static final Color WARNING = new Color(243, 156, 18);
    private static final Color BG = new Color(244, 245, 250);
    private static final Color CARD_BG = Color.WHITE;
    private static final Color TEXT_DARK = new Color(45, 45, 70);
    private static final Color TEXT_MUTED = new Color(120, 120, 145);

    private static final Font FONT_TITLE = new Font("SansSerif", Font.BOLD, 30);
    private static final Font FONT_SUBTITLE = new Font("SansSerif", Font.PLAIN, 15);
    private static final Font FONT_HEADING = new Font("SansSerif", Font.BOLD, 20);
    private static final Font FONT_BODY = new Font("SansSerif", Font.PLAIN, 15);
    private static final Font FONT_BUTTON = new Font("SansSerif", Font.BOLD, 15);
    private static final String ADMIN_PASSWORD = "admin123";

    private JFrame frame;
    private JPanel contentPanel;
    private QuestionBank bank;
    private ResultManager resultManager;
    private ScoreCalculator scoreCalculator;
    private OpenTriviaApiClient apiClient;

    private String studentName;
    private String selectedSubject;
    private List<Question> quizQuestions;
    private List<Integer> quizAnswers;
    private int currentIndex;

    private ButtonGroup optionGroup;
    private JRadioButton[] optionButtons;
    private JTextArea[] optionTextAreas;
    private JProgressBar quizProgress;
    private JLabel quizProgressLabel;
    private JTextArea questionArea;
    private JButton nextBtn;

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
        } catch (Exception ignored) { }
        SwingUtilities.invokeLater(QuizGUIApp::new);
    }

    public QuizGUIApp() {
        bank = new QuestionBank();
        resultManager = new ResultManager();
        scoreCalculator = new ScoreCalculator();
        apiClient = new OpenTriviaApiClient();

        frame = new JFrame("Online Quiz System");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(1000, 720);
        frame.setMinimumSize(new Dimension(820, 620));
        frame.setLocationRelativeTo(null);

        contentPanel = new JPanel(new BorderLayout());
        contentPanel.setBackground(BG);
        frame.add(contentPanel, BorderLayout.CENTER);

        showWelcomeScreen();
        frame.setVisible(true);
    }

    private void setScreen(JPanel panel) {
        contentPanel.removeAll();
        contentPanel.add(panel, BorderLayout.CENTER);
        contentPanel.revalidate();
        contentPanel.repaint();
    }

    private JPanel gradientHeader(String title, String subtitle) {
        JPanel header = new GradientPanel(PRIMARY, PRIMARY_DARK);
        header.setPreferredSize(new Dimension(10, 104));
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
        header.setBorder(new EmptyBorder(18, 30, 18, 30));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(FONT_TITLE);
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel subLabel = new JLabel(subtitle);
        subLabel.setFont(FONT_SUBTITLE);
        subLabel.setForeground(new Color(230, 230, 255));
        subLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        header.add(titleLabel);
        header.add(Box.createVerticalStrut(4));
        header.add(subLabel);
        return header;
    }

    private JButton makeButton(String text, Color bg, Color fg) {
        RoundedButton btn = new RoundedButton(text, bg, fg);
        btn.setAlignmentX(Component.CENTER_ALIGNMENT);
        btn.setFont(FONT_BUTTON);
        return btn;
    }

    private JPanel card() {
        ShadowCard p = new ShadowCard();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        return p;
    }

    private JLabel label(String text) {
        JLabel l = new JLabel(text);
        l.setFont(FONT_BODY);
        l.setForeground(TEXT_DARK);
        l.setAlignmentX(Component.LEFT_ALIGNMENT);
        return l;
    }

    private JPanel centeredPanel(JComponent component) {
        JPanel p = new JPanel(new GridBagLayout());
        p.setBackground(BG);
        p.add(component);
        return p;
    }

    // ---------------- Welcome ----------------

    private void showWelcomeScreen() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(BG);
        root.add(gradientHeader("Online Quiz System",
                "Choose a subject • choose your question count • take a randomized quiz"), BorderLayout.NORTH);

        JPanel card = card();
        card.setPreferredSize(new Dimension(480, 330));

        JLabel welcome = new JLabel("Welcome!");
        welcome.setFont(FONT_HEADING);
        welcome.setForeground(TEXT_DARK);
        welcome.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel info = new JLabel("<html><center>" + bank.size() +
                " local questions available.<br>Online categories can load fresh questions from the public API.</center></html>");
        info.setFont(FONT_BODY);
        info.setForeground(TEXT_MUTED);
        info.setAlignmentX(Component.CENTER_ALIGNMENT);

        JButton studentBtn = makeButton("Start Quiz (Student)", PRIMARY, Color.WHITE);
        studentBtn.setMaximumSize(new Dimension(330, 50));
        studentBtn.addActionListener(e -> showStudentInfoScreen());

        JButton leaderboardBtn = makeButton("View Leaderboard", new Color(236, 236, 250), TEXT_DARK);
        leaderboardBtn.setMaximumSize(new Dimension(330, 50));
        leaderboardBtn.addActionListener(e -> showLeaderboardScreen());

        JButton adminBtn = makeButton("Admin Login", new Color(236, 236, 250), TEXT_DARK);
        adminBtn.setMaximumSize(new Dimension(330, 50));
        adminBtn.addActionListener(e -> showAdminLoginScreen());

        card.add(welcome);
        card.add(Box.createVerticalStrut(8));
        card.add(info);
        card.add(Box.createVerticalStrut(22));
        card.add(studentBtn);
        card.add(Box.createVerticalStrut(12));
        card.add(leaderboardBtn);
        card.add(Box.createVerticalStrut(12));
        card.add(adminBtn);

        root.add(centeredPanel(card), BorderLayout.CENTER);
        setScreen(root);
    }

    // ---------------- Student Setup ----------------

    private void showStudentInfoScreen() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(BG);
        root.add(gradientHeader("Quiz Setup",
                "Select your subject and exactly how many questions you want"), BorderLayout.NORTH);

        JPanel card = card();
        card.setPreferredSize(new Dimension(560, 450));

        JLabel nameLbl = label("Your Name");
        JTextField nameField = new JTextField();
        nameField.setFont(FONT_BODY);
        nameField.setMaximumSize(new Dimension(480, 38));
        nameField.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel subjectLbl = label("Subject");
        String[] subjects = {
                "Java", "Operating Systems", "SQE", "Data Science",
                "Web Engineering", "Software Development",
                "Computer Science (Online)", "General Knowledge (Online)",
                "Science & Nature (Online)"
        };
        JComboBox<String> subjectBox = new JComboBox<>(subjects);
        subjectBox.setFont(FONT_BODY);
        subjectBox.setMaximumSize(new Dimension(480, 38));
        subjectBox.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel countLbl = label("Number of Questions");
        int defaultMax = Math.max(1, Math.min(20, bank.sizeForSubject("Java")));
        JSpinner countSpinner = new JSpinner(new SpinnerNumberModel(Math.max(1, Math.min(10, defaultMax)), 1, 50, 1));
        countSpinner.setFont(FONT_BODY);
        countSpinner.setMaximumSize(new Dimension(480, 38));
        countSpinner.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel availabilityLbl = label("Local questions: " + bank.sizeForSubject("Java") +
                " • Online categories can fetch fresh questions");
        availabilityLbl.setForeground(TEXT_MUTED);

        JCheckBox onlineBox = new JCheckBox("Use online questions when this subject has a public API category");
        onlineBox.setSelected(true);
        onlineBox.setBackground(CARD_BG);
        onlineBox.setForeground(TEXT_DARK);
        onlineBox.setFont(FONT_BODY);
        onlineBox.setAlignmentX(Component.LEFT_ALIGNMENT);

        subjectBox.addActionListener(e -> {
            String subject = (String) subjectBox.getSelectedItem();
            int localCount = bank.sizeForSubject(subject);
            availabilityLbl.setText("Local questions: " + localCount +
                    " • " + (isOnlineSubject(subject) ? "Online API available" : "Local subject bank"));
            int max = isOnlineSubject(subject) ? 50 : Math.max(1, localCount);
            int current = (Integer) countSpinner.getValue();
            countSpinner.setModel(new SpinnerNumberModel(Math.min(Math.max(1, current), max), 1, max, 1));
            onlineBox.setEnabled(isOnlineSubject(subject));
            if (!isOnlineSubject(subject)) onlineBox.setSelected(false);
        });

        JLabel errorLbl = new JLabel(" ");
        errorLbl.setForeground(DANGER);
        errorLbl.setFont(FONT_SUBTITLE);
        errorLbl.setAlignmentX(Component.LEFT_ALIGNMENT);

        JButton startBtn = makeButton("Start Quiz", PRIMARY, Color.WHITE);
        startBtn.setMaximumSize(new Dimension(480, 48));
        startBtn.setAlignmentX(Component.LEFT_ALIGNMENT);

        startBtn.addActionListener(e -> {
            String name = nameField.getText().trim();
            if (name.isEmpty()) {
                errorLbl.setText("Please enter your name.");
                return;
            }

            String subject = (String) subjectBox.getSelectedItem();
            int count = (Integer) countSpinner.getValue();
            boolean online = onlineBox.isSelected();

            studentName = name;
            selectedSubject = subject;
            startBtn.setEnabled(false);
            errorLbl.setForeground(TEXT_MUTED);
            errorLbl.setText("Preparing your quiz...");

            new SwingWorker<List<Question>, Void>() {
                @Override protected List<Question> doInBackground() {
                    return prepareQuizQuestions(subject, count, online);
                }

                @Override protected void done() {
                    try {
                        quizQuestions = get();
                        if (quizQuestions.size() < count) {
                            errorLbl.setForeground(DANGER);
                            errorLbl.setText("Only " + quizQuestions.size() +
                                    " unique questions are available for this subject.");
                            startBtn.setEnabled(true);
                            return;
                        }
                        quizAnswers = new ArrayList<>();
                        currentIndex = 0;
                        showQuizScreen();
                    } catch (Exception ex) {
                        errorLbl.setForeground(DANGER);
                        errorLbl.setText("Could not prepare quiz: " + ex.getMessage());
                        startBtn.setEnabled(true);
                    }
                }
            }.execute();
        });

        JButton backBtn = makeButton("Back", new Color(236, 236, 250), TEXT_DARK);
        backBtn.setMaximumSize(new Dimension(480, 44));
        backBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        backBtn.addActionListener(e -> showWelcomeScreen());

        card.add(nameLbl);
        card.add(Box.createVerticalStrut(6));
        card.add(nameField);
        card.add(Box.createVerticalStrut(14));
        card.add(subjectLbl);
        card.add(Box.createVerticalStrut(6));
        card.add(subjectBox);
        card.add(Box.createVerticalStrut(6));
        card.add(availabilityLbl);
        card.add(Box.createVerticalStrut(14));
        card.add(countLbl);
        card.add(Box.createVerticalStrut(6));
        card.add(countSpinner);
        card.add(Box.createVerticalStrut(10));
        card.add(onlineBox);
        card.add(Box.createVerticalStrut(8));
        card.add(errorLbl);
        card.add(Box.createVerticalStrut(8));
        card.add(startBtn);
        card.add(Box.createVerticalStrut(8));
        card.add(backBtn);

        root.add(centeredPanel(card), BorderLayout.CENTER);
        setScreen(root);
    }

    private boolean isOnlineSubject(String subject) {
        return subject != null && (subject.contains("(Online)") ||
                subject.equalsIgnoreCase("Computer Science") ||
                subject.equalsIgnoreCase("General Knowledge") ||
                subject.equalsIgnoreCase("Science & Nature"));
    }

    private String apiSubject(String subject) {
        if (subject == null) return subject;
        return subject.replace(" (Online)", "");
    }

    /**
     * Builds one quiz without duplicate question text.
     * Online API is tried first for supported subjects; local questions fill any gap.
     */
    private List<Question> prepareQuizQuestions(String subject, int count, boolean online) {
        String cleanSubject = apiSubject(subject);
        List<Question> result = new ArrayList<>();
        Set<String> seen = new HashSet<>();

        if (online && isOnlineSubject(subject)) {
            try {
                List<Question> remote = apiClient.fetchQuestions(cleanSubject, count);
                for (Question q : remote) {
                    if (seen.add(normalize(q.getQuestionText()))) result.add(q.shuffledOptions());
                }
            } catch (Exception ignored) {
                // Network failure is intentionally handled by local fallback.
            }
        }

        List<Question> local = bank.getRandomQuestions(cleanSubject, Math.max(count * 2, count));
        for (Question q : local) {
            if (result.size() >= count) break;
            if (seen.add(normalize(q.getQuestionText()))) result.add(q.shuffledOptions());
        }

        // If the subject is a local subject, getRandomQuestions already filtered it.
        // For an online subject, local questions of the same category are the fallback.
        Collections.shuffle(result);
        return result.size() > count ? new ArrayList<>(result.subList(0, count)) : result;
    }

    private String normalize(String s) {
        return s.toLowerCase(Locale.ROOT).replaceAll("\\s+", " ").trim();
    }

    // ---------------- Quiz Screen ----------------

    private void showQuizScreen() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(BG);

        JPanel top = new JPanel();
        top.setLayout(new BoxLayout(top, BoxLayout.Y_AXIS));
        top.setBackground(PRIMARY);
        top.setBorder(new EmptyBorder(16, 30, 16, 30));

        JLabel title = new JLabel("Quiz in Progress • " + selectedSubject);
        title.setFont(FONT_HEADING);
        title.setForeground(Color.WHITE);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);

        quizProgressLabel = new JLabel();
        quizProgressLabel.setFont(FONT_SUBTITLE);
        quizProgressLabel.setForeground(new Color(230, 230, 255));
        quizProgressLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        quizProgress = new JProgressBar(0, quizQuestions.size());
        quizProgress.setAlignmentX(Component.LEFT_ALIGNMENT);
        quizProgress.setMaximumSize(new Dimension(2000, 10));
        quizProgress.setForeground(SUCCESS);
        quizProgress.setBackground(new Color(230, 230, 255));
        quizProgress.setBorderPainted(false);

        top.add(title);
        top.add(Box.createVerticalStrut(4));
        top.add(quizProgressLabel);
        top.add(Box.createVerticalStrut(8));
        top.add(quizProgress);
        root.add(top, BorderLayout.NORTH);

        JPanel card = card();
        card.setMaximumSize(new Dimension(760, 900));
        card.setPreferredSize(new Dimension(740, 520));

        questionArea = new JTextArea();
        questionArea.setEditable(false);
        questionArea.setLineWrap(true);
        questionArea.setWrapStyleWord(true);
        questionArea.setOpaque(false);
        questionArea.setFont(FONT_HEADING);
        questionArea.setForeground(TEXT_DARK);
        questionArea.setBorder(null);
        questionArea.setAlignmentX(Component.LEFT_ALIGNMENT);
        questionArea.setRows(3);

        optionGroup = new ButtonGroup();
        optionButtons = new JRadioButton[4];
        optionTextAreas = new JTextArea[4];

        JPanel optionsPanel = new JPanel();
        optionsPanel.setLayout(new BoxLayout(optionsPanel, BoxLayout.Y_AXIS));
        optionsPanel.setBackground(CARD_BG);
        optionsPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        for (int i = 0; i < 4; i++) {
            final int index = i;
            JPanel row = new JPanel(new BorderLayout(10, 4));
            row.setBackground(CARD_BG);
            row.setBorder(new CompoundBorder(
                    new LineBorder(new Color(225, 225, 235), 1, true),
                    new EmptyBorder(8, 10, 8, 10)));
            row.setAlignmentX(Component.LEFT_ALIGNMENT);
            row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 95));

            JRadioButton rb = new JRadioButton();
            rb.setBackground(CARD_BG);
            rb.setForeground(TEXT_DARK);
            rb.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            optionGroup.add(rb);
            optionButtons[i] = rb;

            JTextArea text = new JTextArea();
            text.setEditable(false);
            text.setFocusable(false);
            text.setLineWrap(true);
            text.setWrapStyleWord(true);
            text.setBackground(CARD_BG);
            text.setForeground(TEXT_DARK);
            text.setFont(FONT_BODY);
            text.setBorder(null);
            optionTextAreas[i] = text;

            row.add(rb, BorderLayout.WEST);
            row.add(text, BorderLayout.CENTER);

            MouseAdapter selectRow = new MouseAdapter() {
                @Override public void mousePressed(MouseEvent e) {
                    rb.setSelected(true);
                }
            };
            row.addMouseListener(selectRow);
            text.addMouseListener(selectRow);

            optionsPanel.add(row);
            if (i < 3) optionsPanel.add(Box.createVerticalStrut(8));
        }

        JLabel hint = new JLabel("Select one answer, or leave blank to skip.");
        hint.setFont(new Font("SansSerif", Font.ITALIC, 12));
        hint.setForeground(TEXT_MUTED);
        hint.setAlignmentX(Component.LEFT_ALIGNMENT);

        nextBtn = makeButton("Next Question", PRIMARY, Color.WHITE);
        nextBtn.setMaximumSize(new Dimension(740, 48));
        nextBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        nextBtn.addActionListener(e -> handleNextQuestion());

        card.add(questionArea);
        card.add(Box.createVerticalStrut(14));
        card.add(optionsPanel);
        card.add(Box.createVerticalStrut(10));
        card.add(hint);
        card.add(Box.createVerticalStrut(10));
        card.add(nextBtn);

        JScrollPane scroll = new JScrollPane(card);
        scroll.setBorder(null);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        scroll.setBackground(BG);
        root.add(scroll, BorderLayout.CENTER);

        setScreen(root);
        loadQuestionIntoUI();
    }

    private void loadQuestionIntoUI() {
        Question q = quizQuestions.get(currentIndex);
        questionArea.setText("Q" + (currentIndex + 1) + "  [" + q.getCategory() + "]\n" + q.getQuestionText());
        questionArea.setCaretPosition(0);

        String[] opts = q.getOptions();
        for (int i = 0; i < 4; i++) {
            optionButtons[i].setSelected(false);
            optionTextAreas[i].setText(opts[i]);
        }
        optionGroup.clearSelection();

        quizProgress.setValue(currentIndex);
        quizProgressLabel.setText("Question " + (currentIndex + 1) + " of " + quizQuestions.size());
        nextBtn.setText(currentIndex == quizQuestions.size() - 1 ? "Finish Quiz" : "Next Question");
    }

    private void handleNextQuestion() {
        int selected = -1;
        for (int i = 0; i < 4; i++) {
            if (optionButtons[i].isSelected()) {
                selected = i;
                break;
            }
        }
        quizAnswers.add(selected);
        currentIndex++;

        if (currentIndex >= quizQuestions.size()) finishQuiz();
        else loadQuestionIntoUI();
    }

    private void finishQuiz() {
        int score = scoreCalculator.calculateScore(quizQuestions, quizAnswers);
        Result result = new Result(studentName, score, quizQuestions.size());
        resultManager.saveResult(result);
        showResultScreen(result);
    }

    // ---------------- Results ----------------

    private void showResultScreen(Result result) {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(BG);
        root.add(gradientHeader("Quiz Complete!", "Your result for " + selectedSubject), BorderLayout.NORTH);

        JPanel card = card();
        card.setPreferredSize(new Dimension(480, 470));

        JLabel scoreLabel = new JLabel(result.getScore() + " / " + result.getTotal());
        scoreLabel.setFont(new Font("SansSerif", Font.BOLD, 46));
        scoreLabel.setForeground(TEXT_DARK);
        scoreLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel pctLabel = new JLabel(String.format("%.1f%% correct", result.getPercentage()));
        pctLabel.setFont(FONT_BODY);
        pctLabel.setForeground(TEXT_MUTED);
        pctLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        double pct = result.getPercentage();
        Color gradeColor = pct >= 75 ? SUCCESS : (pct >= 40 ? WARNING : DANGER);

        JProgressBar resultBar = new JProgressBar(0, 100);
        resultBar.setValue((int) pct);
        resultBar.setForeground(gradeColor);
        resultBar.setBackground(new Color(235, 235, 245));
        resultBar.setBorderPainted(false);
        resultBar.setMaximumSize(new Dimension(360, 14));
        resultBar.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel gradeLabel = new JLabel(scoreCalculator.getGrade(pct));
        gradeLabel.setOpaque(true);
        gradeLabel.setBackground(gradeColor);
        gradeLabel.setForeground(Color.WHITE);
        gradeLabel.setFont(FONT_BUTTON);
        gradeLabel.setHorizontalAlignment(SwingConstants.CENTER);
        gradeLabel.setBorder(new EmptyBorder(8, 18, 8, 18));
        gradeLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JButton retryBtn = makeButton("Take Another Quiz", PRIMARY, Color.WHITE);
        retryBtn.setMaximumSize(new Dimension(320, 46));
        retryBtn.addActionListener(e -> showStudentInfoScreen());

        JButton leaderboardBtn = makeButton("View Leaderboard", new Color(236, 236, 250), TEXT_DARK);
        leaderboardBtn.setMaximumSize(new Dimension(320, 46));
        leaderboardBtn.addActionListener(e -> showLeaderboardScreen());

        JButton homeBtn = makeButton("Main Menu", new Color(236, 236, 250), TEXT_DARK);
        homeBtn.setMaximumSize(new Dimension(320, 46));
        homeBtn.addActionListener(e -> showWelcomeScreen());

        card.add(scoreLabel);
        card.add(Box.createVerticalStrut(6));
        card.add(pctLabel);
        card.add(Box.createVerticalStrut(14));
        card.add(resultBar);
        card.add(Box.createVerticalStrut(16));
        card.add(gradeLabel);
        card.add(Box.createVerticalStrut(22));
        card.add(retryBtn);
        card.add(Box.createVerticalStrut(10));
        card.add(leaderboardBtn);
        card.add(Box.createVerticalStrut(10));
        card.add(homeBtn);

        root.add(centeredPanel(card), BorderLayout.CENTER);
        setScreen(root);
    }

    // ---------------- Leaderboard ----------------

    private void showLeaderboardScreen() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(BG);
        root.add(gradientHeader("Leaderboard", "Top scores from all quiz attempts"), BorderLayout.NORTH);

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setBackground(BG);
        wrapper.setBorder(new EmptyBorder(24, 40, 24, 40));

        String[] columns = {"Rank", "Name", "Score", "Percentage", "Date"};
        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override public boolean isCellEditable(int row, int col) { return false; }
        };

        List<Result> board = resultManager.getLeaderboard();
        int rank = 1;
        for (Result r : board) {
            model.addRow(new Object[]{rank++, r.getStudentName(),
                    r.getScore() + "/" + r.getTotal(),
                    String.format("%.1f%%", r.getPercentage()), r.getTimestamp()});
        }

        JTable table = new JTable(model);
        table.setFont(FONT_BODY);
        table.setRowHeight(30);
        table.getTableHeader().setFont(FONT_BUTTON);
        table.getTableHeader().setBackground(PRIMARY);
        table.getTableHeader().setForeground(Color.WHITE);
        table.setGridColor(new Color(235, 235, 245));

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(new LineBorder(new Color(225, 225, 235), 1, true));
        wrapper.add(scrollPane, BorderLayout.CENTER);

        JButton backBtn = makeButton("Back to Menu", PRIMARY, Color.WHITE);
        backBtn.addActionListener(e -> showWelcomeScreen());
        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.CENTER));
        btnRow.setBackground(BG);
        btnRow.add(backBtn);
        wrapper.add(btnRow, BorderLayout.SOUTH);

        root.add(wrapper, BorderLayout.CENTER);
        setScreen(root);
    }

    // ---------------- Admin ----------------

    private void showAdminLoginScreen() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(BG);
        root.add(gradientHeader("Admin Login", "Manage the question bank"), BorderLayout.NORTH);

        JPanel card = card();
        card.setPreferredSize(new Dimension(420, 250));

        JLabel lbl = label("Enter Admin Password");
        JPasswordField passField = new JPasswordField();
        passField.setMaximumSize(new Dimension(360, 38));
        passField.setFont(FONT_BODY);
        passField.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel errorLbl = new JLabel(" ");
        errorLbl.setForeground(DANGER);
        errorLbl.setAlignmentX(Component.LEFT_ALIGNMENT);

        JButton loginBtn = makeButton("Login", PRIMARY, Color.WHITE);
        loginBtn.setMaximumSize(new Dimension(360, 46));
        loginBtn.setAlignmentX(Component.LEFT_ALIGNMENT);

        ActionListener tryLogin = e -> {
            if (ADMIN_PASSWORD.equals(new String(passField.getPassword()))) showAdminManageScreen();
            else errorLbl.setText("Incorrect password. Try again.");
        };
        loginBtn.addActionListener(tryLogin);
        passField.addActionListener(tryLogin);

        JButton backBtn = makeButton("Back", new Color(236, 236, 250), TEXT_DARK);
        backBtn.setMaximumSize(new Dimension(360, 44));
        backBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        backBtn.addActionListener(e -> showWelcomeScreen());

        card.add(lbl);
        card.add(Box.createVerticalStrut(6));
        card.add(passField);
        card.add(Box.createVerticalStrut(8));
        card.add(errorLbl);
        card.add(Box.createVerticalStrut(8));
        card.add(loginBtn);
        card.add(Box.createVerticalStrut(8));
        card.add(backBtn);

        root.add(centeredPanel(card), BorderLayout.CENTER);
        setScreen(root);
    }

    private void showAdminManageScreen() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(BG);
        root.add(gradientHeader("Manage Questions", bank.size() + " questions in the bank"), BorderLayout.NORTH);

        JPanel wrapper = new JPanel(new BorderLayout(0, 12));
        wrapper.setBackground(BG);
        wrapper.setBorder(new EmptyBorder(20, 40, 20, 40));

        DefaultListModel<String> listModel = new DefaultListModel<>();
        List<Question> all = bank.getAllQuestions();
        for (int i = 0; i < all.size(); i++) {
            Question q = all.get(i);
            listModel.addElement((i + 1) + ". [" + q.getCategory() + "] " + q.getQuestionText());
        }

        JList<String> questionList = new JList<>(listModel);
        questionList.setFont(FONT_BODY);
        questionList.setFixedCellHeight(32);
        wrapper.add(new JScrollPane(questionList), BorderLayout.CENTER);

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 14, 10));
        btnRow.setBackground(BG);

        JButton addBtn = makeButton("Add Question", SUCCESS, Color.WHITE);
        addBtn.addActionListener(e -> showAddQuestionDialog());

        JButton deleteBtn = makeButton("Delete Selected", DANGER, Color.WHITE);
        deleteBtn.addActionListener(e -> {
            int sel = questionList.getSelectedIndex();
            if (sel < 0) {
                JOptionPane.showMessageDialog(frame, "Select a question first.");
                return;
            }
            int confirm = JOptionPane.showConfirmDialog(frame, "Delete this question?",
                    "Confirm Delete", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                bank.deleteQuestion(sel + 1);
                showAdminManageScreen();
            }
        });

        JButton backBtn = makeButton("Back to Menu", new Color(236, 236, 250), TEXT_DARK);
        backBtn.addActionListener(e -> showWelcomeScreen());

        btnRow.add(addBtn);
        btnRow.add(deleteBtn);
        btnRow.add(backBtn);
        wrapper.add(btnRow, BorderLayout.SOUTH);

        root.add(wrapper, BorderLayout.CENTER);
        setScreen(root);
    }

    private void showAddQuestionDialog() {
        JDialog dialog = new JDialog(frame, "Add New Question", true);
        dialog.setSize(520, 620);
        dialog.setLocationRelativeTo(frame);
        dialog.setLayout(new BorderLayout());

        JPanel form = new JPanel();
        form.setBackground(CARD_BG);
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.setBorder(new EmptyBorder(20, 24, 20, 24));

        JTextField categoryField = new JTextField();
        categoryField.setMaximumSize(new Dimension(450, 34));

        JTextArea questionArea = new JTextArea(4, 30);
        questionArea.setLineWrap(true);
        questionArea.setWrapStyleWord(true);

        JTextField[] optionFields = new JTextField[4];
        ButtonGroup correctGroup = new ButtonGroup();
        JRadioButton[] correctRadios = new JRadioButton[4];

        form.add(sectionLabel("Subject / Category"));
        form.add(categoryField);
        form.add(Box.createVerticalStrut(10));
        form.add(sectionLabel("Question"));
        JScrollPane qScroll = new JScrollPane(questionArea);
        qScroll.setMaximumSize(new Dimension(450, 90));
        form.add(qScroll);
        form.add(Box.createVerticalStrut(10));
        form.add(sectionLabel("Options — select the radio button for the correct answer"));

        for (int i = 0; i < 4; i++) {
            JPanel row = new JPanel(new BorderLayout(8, 0));
            row.setBackground(CARD_BG);
            row.setMaximumSize(new Dimension(450, 38));

            JRadioButton rb = new JRadioButton();
            rb.setBackground(CARD_BG);
            correctGroup.add(rb);
            correctRadios[i] = rb;

            JTextField field = new JTextField();
            optionFields[i] = field;
            row.add(rb, BorderLayout.WEST);
            row.add(field, BorderLayout.CENTER);
            form.add(row);
            if (i < 3) form.add(Box.createVerticalStrut(6));
        }
        correctRadios[0].setSelected(true);

        JLabel errorLbl = new JLabel(" ");
        errorLbl.setForeground(DANGER);
        form.add(Box.createVerticalStrut(8));
        form.add(errorLbl);

        JButton saveBtn = makeButton("Save Question", SUCCESS, Color.WHITE);
        JButton cancelBtn = makeButton("Cancel", new Color(236, 236, 250), TEXT_DARK);

        saveBtn.addActionListener(e -> {
            String category = categoryField.getText().trim();
            String qText = questionArea.getText().trim();
            if (category.isEmpty()) category = "General Knowledge";
            if (qText.isEmpty()) {
                errorLbl.setText("Question text cannot be empty.");
                return;
            }

            String[] opts = new String[4];
            for (int i = 0; i < 4; i++) {
                opts[i] = optionFields[i].getText().trim();
                if (opts[i].isEmpty()) {
                    errorLbl.setText("All four options are required.");
                    return;
                }
            }

            int correct = 0;
            for (int i = 0; i < 4; i++) if (correctRadios[i].isSelected()) correct = i;

            bank.addQuestion(new Question(category, qText, opts, correct));
            dialog.dispose();
            showAdminManageScreen();
        });

        cancelBtn.addActionListener(e -> dialog.dispose());

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttons.setBackground(CARD_BG);
        buttons.add(cancelBtn);
        buttons.add(saveBtn);

        dialog.add(new JScrollPane(form), BorderLayout.CENTER);
        dialog.add(buttons, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }

    private JLabel sectionLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("SansSerif", Font.BOLD, 13));
        l.setForeground(TEXT_DARK);
        l.setAlignmentX(Component.LEFT_ALIGNMENT);
        return l;
    }
}
