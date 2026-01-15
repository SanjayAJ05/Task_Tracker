import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class TaskCLI {

    private static final String FILE_NAME = "tasks.json";
    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public static void main(String[] args) {
        if (args.length == 0) {
            System.out.println("Please provide a command.");
            return;
        }

        String command = args[0];

        try {
            switch (command) {
                case "add":
                    addTask(args);
                    break;
                case "list":
                    listTasks(args);
                    break;
                case "delete":
                    deleteTask(args);
                    break;
                case "update":
                    updateTask(args);
                    break;
                case "mark-in-progress":
                    markStatus(args, "in-progress");
                    break;
                case "mark-done":
                    markStatus(args, "done");
                    break;
                default:
                    System.out.println("Unknown command.");
            }
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    // ---------- CORE FUNCTIONS ----------

    private static void addTask(String[] args) throws IOException {
        if (args.length < 2) {
            System.out.println("Task description required.");
            return;
        }

        List<String> tasks = loadTasks();
        int id = tasks.size() + 1;
        String now = LocalDateTime.now().format(FORMATTER);

        String task = String.format(
                "{\"id\":%d,\"description\":\"%s\",\"status\":\"todo\",\"createdAt\":\"%s\",\"updatedAt\":\"%s\"}",
                id, args[1], now, now
        );

        tasks.add(task);
        saveTasks(tasks);
        System.out.println("Task added successfully (ID: " + id + ")");
    }

    private static void listTasks(String[] args) throws IOException {
        List<String> tasks = loadTasks();

        if (tasks.isEmpty()) {
            System.out.println("No tasks found.");
            return;
        }

        String filter = args.length > 1 ? args[1] : "";

        for (String task : tasks) {
            if (filter.isEmpty() || task.contains("\"status\":\"" + filter + "\"")) {
                System.out.println(task);
            }
        }
    }

    private static void deleteTask(String[] args) throws IOException {
        int id = Integer.parseInt(args[1]);
        List<String> tasks = loadTasks();

        tasks.removeIf(t -> t.contains("\"id\":" + id));
        saveTasks(tasks);
        System.out.println("Task deleted.");
    }

    private static void updateTask(String[] args) throws IOException {
        int id = Integer.parseInt(args[1]);
        String newDesc = args[2];
        List<String> tasks = loadTasks();
        String now = LocalDateTime.now().format(FORMATTER);

        for (int i = 0; i < tasks.size(); i++) {
            if (tasks.get(i).contains("\"id\":" + id)) {
                tasks.set(i, tasks.get(i)
                        .replaceAll("\"description\":\".*?\"", "\"description\":\"" + newDesc + "\"")
                        .replaceAll("\"updatedAt\":\".*?\"", "\"updatedAt\":\"" + now + "\""));
            }
        }
        saveTasks(tasks);
        System.out.println("Task updated.");
    }

    private static void markStatus(String[] args, String status) throws IOException {
        int id = Integer.parseInt(args[1]);
        List<String> tasks = loadTasks();
        String now = LocalDateTime.now().format(FORMATTER);

        for (int i = 0; i < tasks.size(); i++) {
            if (tasks.get(i).contains("\"id\":" + id)) {
                tasks.set(i, tasks.get(i)
                        .replaceAll("\"status\":\".*?\"", "\"status\":\"" + status + "\"")
                        .replaceAll("\"updatedAt\":\".*?\"", "\"updatedAt\":\"" + now + "\""));
            }
        }
        saveTasks(tasks);
        System.out.println("Task marked as " + status + ".");
    }

    // ---------- FILE HANDLING ----------

    private static List<String> loadTasks() throws IOException {
        File file = new File(FILE_NAME);
        if (!file.exists()) {
            file.createNewFile();
            saveTasks(new ArrayList<>());
        }

        BufferedReader br = new BufferedReader(new FileReader(FILE_NAME));
        String content = br.readLine();
        br.close();

        if (content == null || content.equals("[]")) {
            return new ArrayList<>();
        }

        content = content.substring(1, content.length() - 1);
        return new ArrayList<>(Arrays.asList(content.split("},")));
    }

    private static void saveTasks(List<String> tasks) throws IOException {
        BufferedWriter bw = new BufferedWriter(new FileWriter(FILE_NAME));
        bw.write("[");
        for (int i = 0; i < tasks.size(); i++) {
            bw.write(tasks.get(i));
            if (i < tasks.size() - 1) bw.write(",");
        }
        bw.write("]");
        bw.close();
    }
}
