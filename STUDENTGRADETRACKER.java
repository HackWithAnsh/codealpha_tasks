
import java.util.ArrayList;
import java.util.InputMismatchException;
import java.util.Scanner;

public class STUDENTGRADETRACKER {

    // Represents a single student and their scores
    static class Student {
        String name;
        ArrayList<Double> scores = new ArrayList<>();

        Student(String name) {
            this.name = name;
        }

        double getAverage() {
            if (scores.isEmpty()) return 0.0;
            double sum = 0;
            for (double s : scores) sum += s;
            return sum / scores.size();
        }

        double getHighest() {
            double max = scores.get(0);
            for (double s : scores) if (s > max) max = s;
            return max;
        }

        double getLowest() {
            double min = scores.get(0);
            for (double s : scores) if (s < min) min = s;
            return min;
        }

        String getGrade() {
            double avg = getAverage();
            if (avg >= 90) return "A";
            if (avg >= 80) return "B";
            if (avg >= 70) return "C";
            if (avg >= 60) return "D";
            return "F";
        }
    }

    static ArrayList<Student> students = new ArrayList<>();
    static Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {
        boolean running = true;
        while (running) {
            printMenu();
            int choice = readInt("Choose an option: ");
            switch (choice) {
                case 1 -> addStudent();
                case 2 -> viewStudent();
                case 3 -> editStudent();
                case 4 -> removeStudent();
                case 5 -> displaySummaryReport();
                case 6 -> {
                    System.out.println("Goodbye!");
                    running = false;
                }
                default -> System.out.println("Invalid option. Please try again.\n");
            }
        }
        scanner.close();
    }

    static void printMenu() {
        System.out.println("===== Student Grade Tracker =====");
        System.out.println("1. Add Student");
        System.out.println("2. View Student");
        System.out.println("3. Edit Student Scores");
        System.out.println("4. Remove Student");
        System.out.println("5. Display Summary Report");
        System.out.println("6. Exit");
    }

    static void addStudent() {
        System.out.print("Enter student name: ");
        String name = scanner.nextLine().trim();
        if (name.isEmpty()) {
            System.out.println("Name cannot be empty.\n");
            return;
        }
        Student student = new Student(name);

        int numScores = readInt("How many subject scores to enter? ");
        for (int i = 1; i <= numScores; i++) {
            double score = readDouble("  Enter score " + i + " (0-100): ");
            student.scores.add(score);
        }

        if (student.scores.isEmpty()) {
            System.out.println("No scores entered. Adding default score of 0.");
            student.scores.add(0.0);
        }

        students.add(student);
        System.out.println("Student \"" + name + "\" added successfully.\n");
    }

    static void viewStudent() {
        Student s = findStudentByName();
        if (s == null) return;
        printStudentDetail(s);
    }

    static void editStudent() {
        Student s = findStudentByName();
        if (s == null) return;

        System.out.println("Current scores: " + s.scores);
        System.out.println("1. Add a score");
        System.out.println("2. Replace all scores");
        int opt = readInt("Choose an option: ");
        if (opt == 1) {
            double score = readDouble("Enter new score: ");
            s.scores.add(score);
            System.out.println("Score added.\n");
        } else if (opt == 2) {
            s.scores.clear();
            int numScores = readInt("How many scores to enter? ");
            for (int i = 1; i <= numScores; i++) {
                double score = readDouble("  Enter score " + i + ": ");
                s.scores.add(score);
            }
            System.out.println("Scores updated.\n");
        } else {
            System.out.println("Invalid option.\n");
        }
    }

    static void removeStudent() {
        Student s = findStudentByName();
        if (s == null) return;
        students.remove(s);
        System.out.println("Student \"" + s.name + "\" removed.\n");
    }

    static Student findStudentByName() {
        if (students.isEmpty()) {
            System.out.println("No students in the system yet.\n");
            return null;
        }
        System.out.print("Enter student name: ");
        String name = scanner.nextLine().trim();
        for (Student s : students) {
            if (s.name.equalsIgnoreCase(name)) return s;
        }
        System.out.println("Student not found.\n");
        return null;
    }

    static void printStudentDetail(Student s) {
        System.out.println("---- " + s.name + " ----");
        System.out.println("Scores : " + s.scores);
        System.out.printf("Average: %.2f%n", s.getAverage());
        System.out.printf("Highest: %.2f%n", s.getHighest());
        System.out.printf("Lowest : %.2f%n", s.getLowest());
        System.out.println("Grade  : " + s.getGrade());
        System.out.println();
    }

    static void displaySummaryReport() {
        if (students.isEmpty()) {
            System.out.println("No students to display.\n");
            return;
        }

        System.out.println("\n================= SUMMARY REPORT =================");
        System.out.printf("%-15s %-10s %-10s %-10s %-6s%n", "Name", "Average", "Highest", "Lowest", "Grade");
        System.out.println("----------------------------------------------------");

        double classSum = 0;
        double classHighest = Double.MIN_VALUE;
        double classLowest = Double.MAX_VALUE;
        String topStudent = "";
        String lowStudent = "";

        for (Student s : students) {
            double avg = s.getAverage();
            System.out.printf("%-15s %-10.2f %-10.2f %-10.2f %-6s%n",
                    s.name, avg, s.getHighest(), s.getLowest(), s.getGrade());

            classSum += avg;
            if (avg > classHighest) {
                classHighest = avg;
                topStudent = s.name;
            }
            if (avg < classLowest) {
                classLowest = avg;
                lowStudent = s.name;
            }
        }

        double classAverage = classSum / students.size();

        System.out.println("----------------------------------------------------");
        System.out.printf("Class Average       : %.2f%n", classAverage);
        System.out.printf("Top Performer       : %s (%.2f)%n", topStudent, classHighest);
        System.out.printf("Needs Improvement   : %s (%.2f)%n", lowStudent, classLowest);
        System.out.println("=====================================================\n");
    }

    // ---------- Input helper methods with validation ----------

    static int readInt(String prompt) {
        while (true) {
            try {
                System.out.print(prompt);
                int value = Integer.parseInt(scanner.nextLine().trim());
                return value;
            } catch (NumberFormatException e) {
                System.out.println("Please enter a valid whole number.");
            }
        }
    }

    static double readDouble(String prompt) {
        while (true) {
            try {
                System.out.print(prompt);
                double value = Double.parseDouble(scanner.nextLine().trim());
                if (value < 0 || value > 100) {
                    System.out.println("Score must be between 0 and 100.");
                    continue;
                }
                return value;
            } catch (NumberFormatException e) {
                System.out.println("Please enter a valid number.");
            }
        }
    }
}