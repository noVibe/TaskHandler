package Tasks;

import java.io.*;
import java.time.LocalDateTime;

import Tasks.Periodic.*;
import enums.Period;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.*;
import java.util.stream.Collectors;

import static enums.Period.*;

final public class TaskHandler {
    final private static Set<Task> tasks = new HashSet<>();
    final private static List<Task> expired = new LinkedList<>();
    final private static Set<Task> removed = new LinkedHashSet<>();
    private static long id = 0;

    public static void addNewTaskInstance(boolean isPersonal, String header, String description, LocalDateTime date, Period period) {
        if (period.equals(ONCE)) tasks.add(new OneTimeTask(id, isPersonal, header, description, date, period));
        else if (period.equals(DAILY)) tasks.add(new DailyTask(id, isPersonal, header, description, date, period));
        else if (period.equals(WEEKLY)) tasks.add(new WeeklyTask(id, isPersonal, header, description, date, period));
        else if (period.equals(MONTHLY)) tasks.add(new MonthlyTask(id, isPersonal, header, description, date, period));
        else if (period.equals(YEARLY)) tasks.add(new YearlyTask(id, isPersonal, header, description, date, period));
        id++;
    }

    private static void refresh() {
        var iterator = tasks.iterator();
        while (iterator.hasNext()) {
            var task = iterator.next();
            task.refreshDate();
            if (!task.isActual()) {
                expired.add(0, task);
                iterator.remove();
            }
        }
    }

    public static Task removeByID(long id) {
        Task t = findByID(id);
        t.setActual(false);
        removed.add(t);
        tasks.remove(t);
        return t;
    }

    public static Task findByID(long id) {
        return tasks.stream().filter(t -> t.getId() == id).findAny()
                .orElseThrow(() -> new NoSuchElementException("Task with id " + id + " is not present"));
    }

    public static void printAllActiveTasks() {
        refresh();
        if (tasks.isEmpty()) System.out.println("\n|---No-Active-Tasks-Found---|\n");
        else {
            Map<LocalDate, List<Task>> groupedTasks = tasks.stream().collect(Collectors.groupingBy(Task::getDate));
            groupedTasks.entrySet().stream().sorted(Map.Entry.comparingByKey()).forEach((entry) -> {
                System.out.printf("========= %s =========\n", entry.getKey().format(DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT)));
                entry.getValue().stream().sorted()
                        .forEach(t -> System.out.println(t + "\n------------------------------"));
            });
        }
    }


    public static void printTodayTasks() {
        refresh();
        printTasksOnSpecificDate(LocalDate.now());
    }


    public static void printRemovedTasks() {
        System.out.println("Removed tasks list:");
        if (removed.isEmpty()) System.out.println("\n|---No-Removed-Tasks-Found---|\n");
        else for (Task t : removed) System.out.println("============Removed=Task============\n" + t);
    }

    public static void printExpiredTasks() {
        refresh();
        System.out.println("Expired tasks list:");
        if (expired.isEmpty()) System.out.println("\n|---No-Expired-Tasks-Found---|\n");
        else for (Task t : expired) System.out.println("============Expired=Task============\n" + t);
    }

    public static void printTasksOnSpecificDate(LocalDate date) {
        long r = tasks.stream()
                .filter(t -> t.isActiveAt(date))
                .sorted()
                .peek(t -> System.out.println("=========Tasks=Of=This=Date========\n" + t))
                .count();
        if (r == 0) System.out.println("\n|---No-Tasks-For-This-Date---|\n");
    }

    public static long[] getIdList() {
        refresh();
        return tasks.stream().mapToLong(Task::getId).toArray();
    }

    public static void saveData() {
        List<Task> save = new LinkedList<>(){{addAll(tasks); addAll(expired); addAll(removed);}};
        try {
            FileOutputStream outFS = new FileOutputStream("saved_data");
            ObjectOutputStream outOS = new ObjectOutputStream(outFS);
            outOS.writeObject(save);
            outFS.flush();
            outOS.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void loadData() {
        List<Task> t;
        try {
            FileInputStream inFS = new FileInputStream("saved_data");
            ObjectInputStream inOS = new ObjectInputStream(inFS);
            t = (LinkedList<Task>) inOS.readObject();
            for (Task task : t) {
                id = task.getId() < id ? id : task.getId() + 1;
                if (task.isActual()) tasks.add(task);
                else if (task.getPeriod().equals(ONCE) && task.getDate().isBefore(LocalDate.now())) expired.add(task);
                else removed.add(task);
            }
        } catch (FileNotFoundException e) {
            saveData();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            System.out.println("Looks like save file has been damaged. New one will be created on exit.");
        }
    }
}


