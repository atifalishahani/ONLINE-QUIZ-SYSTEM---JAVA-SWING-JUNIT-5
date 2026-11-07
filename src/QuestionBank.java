import java.io.*;
import java.util.*;

/**
 * Persistent question bank with subject filtering and non-repeating random selection.
 */
public class QuestionBank {
    private static final String FILE_PATH = "questions.txt";
    private final List<Question> questions = new ArrayList<>();

    public QuestionBank() {
        loadQuestions();
        if (questions.isEmpty()) {
            seedDefaultQuestions();
            saveQuestions();
        }
    }

    public void loadQuestions() {
        questions.clear();
        File file = new File(FILE_PATH);
        if (!file.exists()) return;
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                Question q = Question.fromFileString(line);
                if (q != null) questions.add(q);
            }
        } catch (IOException e) {
            System.out.println("Warning: could not read questions.txt (" + e.getMessage() + ")");
        }
    }

    public void saveQuestions() {
        try (PrintWriter writer = new PrintWriter(new FileWriter(FILE_PATH))) {
            for (Question q : questions) writer.println(q.toFileString());
        } catch (IOException e) {
            System.out.println("Error saving questions: " + e.getMessage());
        }
    }

    public void addQuestion(Question q) {
        if (q == null) return;
        questions.add(q);
        saveQuestions();
    }

    public boolean deleteQuestion(int oneBasedIndex) {
        int i = oneBasedIndex - 1;
        if (i < 0 || i >= questions.size()) return false;
        questions.remove(i);
        saveQuestions();
        return true;
    }

    public List<Question> getAllQuestions() {
        return new ArrayList<>(questions);
    }

    public List<String> getSubjects() {
        LinkedHashSet<String> subjects = new LinkedHashSet<>();
        for (Question q : questions) subjects.add(q.getCategory());
        return new ArrayList<>(subjects);
    }

    public List<Question> getQuestionsForSubject(String subject) {
        if (subject == null || subject.equalsIgnoreCase("All Subjects")) {
            return getAllQuestions();
        }
        List<Question> result = new ArrayList<>();
        for (Question q : questions) {
            if (q.getCategory().equalsIgnoreCase(subject)) result.add(q);
        }
        return result;
    }

    public int size() { return questions.size(); }

    public int sizeForSubject(String subject) {
        return getQuestionsForSubject(subject).size();
    }

    /** Returns up to n unique random questions from one subject. */
    public List<Question> getRandomQuestions(String subject, int n) {
        List<Question> copy = getQuestionsForSubject(subject);
        Collections.shuffle(copy);
        int limit = Math.min(Math.max(n, 0), copy.size());
        List<Question> result = new ArrayList<>(copy.subList(0, limit));
        List<Question> shuffledAnswers = new ArrayList<>();
        for (Question q : result) shuffledAnswers.add(q.shuffledOptions());
        return shuffledAnswers;
    }

    /** Backwards-compatible random selection across the whole bank. */
    public List<Question> getRandomQuestions(int n) {
        return getRandomQuestions("All Subjects", n);
    }

    private void seedDefaultQuestions() {
        // Java
        addSeed("Java", "Which keyword is used to inherit a class in Java?", "extends", "implements", "inherits", "super", 0);
        addSeed("Java", "Which of these is not a primitive data type?", "int", "boolean", "String", "char", 2);
        addSeed("Java", "Which method is the entry point of a standard Java application?", "start()", "main()", "run()", "init()", 1);
        addSeed("Java", "Which collection does not allow duplicate elements?", "List", "Queue", "Set", "ArrayList", 2);
        addSeed("Java", "What does JVM stand for?", "Java Variable Machine", "Java Virtual Machine", "Java Verified Mode", "Java Visual Model", 1);
        addSeed("Java", "Which symbol ends a normal Java statement?", ":", ";", ".", ",", 1);
        addSeed("Java", "Which access modifier makes a member visible only inside its class?", "public", "protected", "private", "default", 2);
        addSeed("Java", "Which keyword creates an object in Java?", "class", "new", "this", "object", 1);
        addSeed("Java", "Which loop is guaranteed to execute its body at least once?", "for", "while", "do-while", "foreach", 2);
        addSeed("Java", "Which concept allows one interface to have different implementations?", "Inheritance", "Polymorphism", "Compilation", "Serialization", 1);
        addSeed("Java", "Which keyword is used to prevent a class from being inherited?", "static", "final", "const", "sealed", 1);
        addSeed("Java", "Which exception occurs when dividing an integer by zero?", "IOException", "ArithmeticException", "NullPointerException", "ClassNotFoundException", 1);

        // Operating Systems
        addSeed("Operating Systems", "What is the main role of an operating system?", "Design websites", "Manage computer resources", "Create databases", "Compile only Java", 1);
        addSeed("Operating Systems", "Which part of an operating system runs with high privileges?", "Kernel", "Browser", "Text editor", "Shell script", 0);
        addSeed("Operating Systems", "What is a process?", "A program in execution", "A file only", "A hardware cable", "A database table", 0);
        addSeed("Operating Systems", "Which scheduling algorithm gives each process a time slice?", "FCFS", "Round Robin", "SJF", "Priority only", 1);
        addSeed("Operating Systems", "What does DMA allow?", "Direct data transfer with minimal CPU involvement", "Faster typing", "More RAM", "User authentication", 0);
        addSeed("Operating Systems", "What is virtual memory?", "Using storage to extend apparent memory", "A virtual keyboard", "A backup password", "A network cable", 0);
        addSeed("Operating Systems", "What is a system call?", "An interface for a program to request OS services", "A hardware failure", "A compiler warning", "A database query", 0);
        addSeed("Operating Systems", "Which condition can cause deadlock?", "Mutual exclusion", "Infinite RAM", "No processes", "No resources", 0);
        addSeed("Operating Systems", "What does time sharing aim to provide?", "Interactive sharing of CPU time", "Permanent CPU ownership", "Only batch processing", "Disk formatting", 0);
        addSeed("Operating Systems", "What is an interrupt?", "A signal requesting CPU attention", "A type of file", "A memory chip", "A programming language", 0);
        addSeed("Operating Systems", "Which memory is usually closest to the CPU?", "Cache", "Hard disk", "USB drive", "Optical disk", 0);
        addSeed("Operating Systems", "Multiprogramming means:", "Keeping multiple programs in memory to improve CPU use", "Using many keyboards", "Installing many OSs only", "Deleting programs", 0);

        // Software Quality Engineering
        addSeed("SQE", "Which testing technique focuses on internal code structure?", "Black-box testing", "White-box testing", "Beta testing", "Acceptance testing", 1);
        addSeed("SQE", "What does SDLC stand for?", "Software Design Life Cycle", "System Data Life Cycle", "Software Development Life Cycle", "Structured Development Logic", 2);
        addSeed("SQE", "Which test checks whether a small unit of code works correctly?", "Unit test", "Load test", "System test", "Smoke test", 0);
        addSeed("SQE", "JUnit is mainly used for:", "Java testing", "Database hosting", "Image editing", "Network routing", 0);
        addSeed("SQE", "What does QA mainly focus on?", "Quality processes and prevention", "Only fixing one bug", "Buying hardware", "Writing advertisements", 0);
        addSeed("SQE", "What is regression testing?", "Testing after changes to ensure existing behavior still works", "Testing only new users", "Deleting old tests", "Testing without requirements", 0);
        addSeed("SQE", "What is a test case?", "A documented set of inputs, conditions and expected results", "A compiler", "A server", "A programming language", 0);
        addSeed("SQE", "Which testing is performed without executing the program?", "Static testing", "Dynamic testing", "Load testing", "Performance testing", 0);
        addSeed("SQE", "What is a defect?", "A flaw that can cause software to behave incorrectly", "A user account", "A source folder", "A database backup", 0);
        addSeed("SQE", "What does black-box testing primarily use?", "External behavior and requirements", "Internal source code", "CPU registers", "Compiler internals", 0);
        addSeed("SQE", "Which test checks how a system behaves under expected load?", "Performance testing", "Unit testing", "Syntax testing", "Compilation testing", 0);
        addSeed("SQE", "What is the expected result in a test case?", "The behavior the system should produce", "The tester's name", "The source-code size", "The server IP only", 0);

        // Data Science
        addSeed("Data Science", "Which Python library is widely used for tabular data analysis?", "Pandas", "Swing", "JUnit", "Servlet", 0);
        addSeed("Data Science", "Which chart is commonly used to show the distribution of numerical data?", "Histogram", "Pie chart only", "Flowchart", "Class diagram", 0);
        addSeed("Data Science", "What is a sample?", "A subset selected from a population", "The entire universe", "A database server", "A programming language", 0);
        addSeed("Data Science", "What is bias in sampling?", "Systematic distortion caused by the sampling process", "Random success", "A chart title", "A file format", 0);
        addSeed("Data Science", "Which library is commonly used for numerical arrays in Python?", "NumPy", "JUnit", "Maven", "Swing", 0);
        addSeed("Data Science", "What does EDA stand for?", "Exploratory Data Analysis", "External Data Application", "Easy Data Algorithm", "Encoded Dataset Access", 0);
        addSeed("Data Science", "Which measure represents the middle value of ordered data?", "Mean", "Median", "Variance", "Range", 1);
        addSeed("Data Science", "What does a scatter plot help show?", "Relationship between two numerical variables", "Only text length", "Folder structure", "Class inheritance", 0);
        addSeed("Data Science", "Which value is strongly affected by extreme outliers?", "Mean", "Median", "Mode always", "Category name", 0);
        addSeed("Data Science", "What is data cleaning?", "Finding and correcting data quality problems", "Drawing a logo", "Compiling Java", "Installing an OS", 0);
        addSeed("Data Science", "What is a target population?", "The full group the study aims to describe", "Only the sample", "Only the missing data", "A graph type", 0);
        addSeed("Data Science", "Which step helps understand the real-world problem before modeling?", "Problem/domain understanding", "Deleting all data", "Changing the CPU", "Formatting a drive", 0);

        // Web Engineering
        addSeed("Web Engineering", "Which language structures the content of a web page?", "HTML", "CSS", "SQL", "JUnit", 0);
        addSeed("Web Engineering", "Which technology is mainly used to style HTML pages?", "Java", "CSS", "JVM", "JUnit", 1);
        addSeed("Web Engineering", "Which language commonly adds browser-side interactivity?", "JavaScript", "HTML only", "SQL", "XML only", 0);
        addSeed("Web Engineering", "What does HTTP stand for?", "HyperText Transfer Protocol", "High Transfer Text Process", "Hyperlink Test Transfer Program", "Host Transfer Type Protocol", 0);
        addSeed("Web Engineering", "Which HTTP method is commonly used to retrieve data?", "GET", "POST", "DELETE", "PATCH", 0);
        addSeed("Web Engineering", "Which HTML element creates a hyperlink?", "<a>", "<p>", "<img>", "<table>", 0);
        addSeed("Web Engineering", "What is CSS used for?", "Presentation and styling", "Database storage", "Java compilation", "CPU scheduling", 0);
        addSeed("Web Engineering", "What does API stand for?", "Application Programming Interface", "Applied Program Internet", "Application Page Index", "Advanced Protocol Input", 0);
        addSeed("Web Engineering", "What is JSON commonly used for?", "Exchanging structured data", "CPU scheduling", "Disk partitioning", "Image compression only", 0);
        addSeed("Web Engineering", "Which component normally runs JavaScript in a web page?", "Browser", "Printer", "Router only", "BIOS", 0);

        // Software Development & Construction
        addSeed("Software Development", "What is a design pattern?", "A reusable solution structure for a recurring design problem", "A compiler", "A database row", "A network cable", 0);
        addSeed("Software Development", "Which pattern provides a single shared instance of a class?", "Factory", "Singleton", "Adapter", "Observer", 1);
        addSeed("Software Development", "What is refactoring?", "Improving internal code structure without changing intended behavior", "Adding random features", "Deleting all tests", "Changing hardware", 0);
        addSeed("Software Development", "What is cohesion?", "How closely related responsibilities in a module are", "Network speed", "Disk capacity", "User count", 0);
        addSeed("Software Development", "What is coupling?", "The degree of dependency between modules", "Screen brightness", "CPU clock speed", "File size only", 0);
        addSeed("Software Development", "What does DRY mean in software development?", "Don't Repeat Yourself", "Design Runtime Yourself", "Data Runs Yearly", "Do Requirements Yourself", 0);
        addSeed("Software Development", "Which principle says a class should have one reason to change?", "Single Responsibility Principle", "Open/Closed Principle", "Liskov Principle", "Dependency Inversion", 0);
        addSeed("Software Development", "What is version control used for?", "Tracking changes to source code", "Cleaning RAM", "Testing a keyboard", "Formatting a disk", 0);
        addSeed("Software Development", "Git is primarily a:", "Version control system", "Database engine", "Web browser", "Testing framework", 0);
        addSeed("Software Development", "What is an IDE?", "Integrated Development Environment", "Internet Data Engine", "Internal Design Editor", "Interactive Disk Extension", 0);

        // General / computer science
        addSeed("Computer Science", "Which data structure follows FIFO order?", "Stack", "Queue", "Tree", "Graph", 1);
        addSeed("Computer Science", "Which data structure follows LIFO order?", "Queue", "Stack", "Heap only", "Array only", 1);
        addSeed("Computer Science", "What does CPU stand for?", "Central Processing Unit", "Computer Personal Unit", "Central Program Utility", "Control Processing User", 0);
        addSeed("Computer Science", "What is RAM?", "Volatile main memory", "Permanent optical storage", "A programming language", "A network protocol", 0);
        addSeed("Computer Science", "Which number system uses only 0 and 1?", "Decimal", "Binary", "Hexadecimal", "Octal", 1);
        addSeed("Computer Science", "Which device forwards packets between networks?", "Router", "Monitor", "Keyboard", "Scanner", 0);
        addSeed("Computer Science", "What does URL stand for?", "Uniform Resource Locator", "Universal Routing Link", "User Resource Line", "Unified Record Locator", 0);
        addSeed("Computer Science", "Which component stores data permanently on a computer?", "RAM", "CPU cache", "SSD", "Register", 2);
        addSeed("Computer Science", "Which language is primarily used to query relational databases?", "SQL", "HTML", "CSS", "XML", 0);
        addSeed("Computer Science", "Which protocol is commonly used to securely browse websites?", "FTP", "HTTP", "HTTPS", "SMTP", 2);
        addSeed("Computer Science", "What is an algorithm?", "A finite procedure for solving a problem", "A physical cable", "A monitor setting", "A file extension", 0);
        addSeed("Computer Science", "Which data structure is hierarchical?", "Array", "Stack", "Tree", "Queue", 2);
        addSeed("General Knowledge", "Which planet is known as the Red Planet?", "Earth", "Mars", "Jupiter", "Venus", 1);
        addSeed("General Knowledge", "How many days are in a leap year?", "365", "366", "364", "367", 1);
        addSeed("General Knowledge", "What is the capital of France?", "Madrid", "Paris", "Rome", "Berlin", 1);
        addSeed("General Knowledge", "Which ocean is the largest?", "Atlantic", "Indian", "Pacific", "Arctic", 2);
        addSeed("General Knowledge", "How many continents are commonly recognized?", "5", "6", "7", "8", 2);
        addSeed("General Knowledge", "Which gas is most abundant in Earth's atmosphere?", "Oxygen", "Nitrogen", "Carbon dioxide", "Hydrogen", 1);
        addSeed("General Knowledge", "Which instrument measures temperature?", "Barometer", "Thermometer", "Hygrometer", "Altimeter", 1);
        addSeed("General Knowledge", "Which country is famous for the pyramids of Giza?", "Egypt", "Greece", "Mexico", "India", 0);
        addSeed("General Knowledge", "What is the largest mammal?", "Elephant", "Blue whale", "Giraffe", "Hippopotamus", 1);
        addSeed("General Knowledge", "Which language has the most native speakers worldwide?", "English", "Spanish", "Mandarin Chinese", "French", 2);
        addSeed("Science & Nature", "What is the chemical formula for water?", "CO2", "H2O", "O2", "NaCl", 1);
        addSeed("Science & Nature", "Which organ pumps blood through the human body?", "Lung", "Brain", "Heart", "Kidney", 2);
        addSeed("Science & Nature", "What force pulls objects toward Earth?", "Magnetism", "Gravity", "Friction", "Electricity", 1);
        addSeed("Science & Nature", "Which gas do plants use during photosynthesis?", "Oxygen", "Nitrogen", "Carbon dioxide", "Helium", 2);
        addSeed("Science & Nature", "What is the boiling point of water at sea level?", "50°C", "75°C", "100°C", "150°C", 2);
        addSeed("Science & Nature", "Which planet is closest to the Sun?", "Venus", "Earth", "Mercury", "Mars", 2);
        addSeed("Science & Nature", "What is the basic unit of life?", "Atom", "Cell", "Organ", "Tissue", 1);
        addSeed("Science & Nature", "Which part of a plant usually absorbs water from soil?", "Flower", "Root", "Fruit", "Leaf", 1);
        addSeed("Science & Nature", "What is H2O commonly called?", "Hydrogen", "Water", "Oxygen", "Salt", 1);
        addSeed("Science & Nature", "Which vitamin is commonly produced in skin after sunlight exposure?", "Vitamin A", "Vitamin B12", "Vitamin C", "Vitamin D", 3);
    }

    private void addSeed(String category, String q, String a, String b, String c, String d, int correct) {
        questions.add(new Question(category, q, new String[]{a,b,c,d}, correct));
    }
}
