ONLINE QUIZ SYSTEM - JAVA / SWING / JUNIT 5
=============================================

WHAT IS NEW
-----------
1. Responsive quiz GUI:
   - Long questions wrap inside the card.
   - Long answer choices wrap instead of going outside the box.
   - The quiz is inside a scrollable area for smaller screens.
2. Student chooses:
   - Name
   - Subject
   - Number of questions (up to 50)
3. Every quiz randomizes question order.
4. Answer choices are also randomized.
5. A question cannot appear twice in the same quiz.
6. Online questions:
   - Open Trivia DB is integrated directly.
   - No API key is required.
   - Online categories: Computer Science, General Knowledge, Science & Nature.
   - If the internet/API is unavailable, the app automatically uses the matching
     local subject bank instead of crashing.
7. Large local starter bank for Java, Operating Systems, SQE, Data Science,
   Web Engineering, Software Development, Computer Science, General Knowledge,
   and Science & Nature.
8. Maven project configuration is included so JUnit 5 tests can be discovered
   and run correctly in IntelliJ IDEA or Eclipse.

PROJECT
-------
OnlineQuizSystem/
  src/
    Question.java
    QuestionBank.java
    ScoreCalculator.java
    Result.java
    ResultManager.java
    QuizEngine.java
    AdminPanel.java
    QuizSystemApp.java
    QuizGUIApp.java
    OpenTriviaApiClient.java
    RoundedButton.java
    ShadowCard.java
    GradientPanel.java
  test/
    ScoreCalculatorTest.java
    QuestionBankTest.java
  pom.xml
  README.txt

HOW TO RUN IN INTELLIJ IDEA
----------------------------
1. Open the OnlineQuizSystem folder.
2. Open/import pom.xml as a Maven project.
3. Make sure JDK 17 (or newer) is selected.
4. Wait for Maven to download JUnit 5.
5. Run:
      src/QuizGUIApp.java
6. For tests, open the test folder and click the green Run icon beside a
   JUnit test class or method.

If IntelliJ still shows the old project structure:
   - Right-click pom.xml -> Maven -> Reload Project.
   - Make sure src is marked as Sources Root and test as Test Sources Root.
   - Do NOT add a second copy of the same .java files to another source folder.

HOW TO RUN WITH MAVEN
---------------------
From the OnlineQuizSystem folder:

    mvn clean test
    mvn compile
    mvn exec:java

The last command requires the Maven Exec plugin if your IDE does not already
provide a run configuration. The easiest demo method is simply to run
QuizGUIApp.java from IntelliJ.

HOW TO RUN WITHOUT MAVEN
------------------------
From the src folder:

    javac *.java
    java QuizGUIApp

The GUI itself uses only standard Java/Swing classes. Maven is included mainly
to make the JUnit 5 tests easy to configure.

ONLINE API
----------
Open Trivia DB:
    https://opentdb.com/

The application calls the free API only for categories it supports:
- Computer Science -> API category 18
- General Knowledge -> API category 9
- Science & Nature -> API category 17

The API has a maximum of 50 questions per request. A session token is used
when possible so successive requests can avoid previously returned questions.

IMPORTANT:
Open Trivia DB is a general trivia database. It does NOT provide exact
university-course categories for Java, Operating Systems, SQE, Data Science,
or Web Engineering. Those subjects therefore use the large local academic
question bank. This is intentional so the quiz does not show unrelated
questions under a course name.

DATA FILES
----------
questions.txt is created automatically on first run.
results.txt is created when a student completes a quiz.

ADMIN
-----
Password: admin123

The admin can add and delete questions. When adding a question, use a subject
name matching one of the existing subjects if you want it to appear in that
subject's quiz.

TESTING
-------
The project uses JUnit 5. ScoreCalculatorTest checks scoring, percentages,
grades, skipped answers and invalid list sizes.

QuestionBankTest checks:
- question serialization
- malformed input rejection
- large starter bank
- subject filtering
- no duplicate questions inside a random quiz
- question-count limits
- add/delete behavior

VIVA / SQE POINTS
-----------------
Good design points to explain:
- GUI and business logic are separated.
- ScoreCalculator is pure logic and easy to unit test.
- QuestionBank handles persistence and subject filtering.
- OpenTriviaApiClient isolates external API communication.
- SwingWorker prevents a slow network request from freezing the GUI.
- Randomization and duplicate prevention happen before the quiz starts.
- Local fallback means an API/network failure does not break the application.
