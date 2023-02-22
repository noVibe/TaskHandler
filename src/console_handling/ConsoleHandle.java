package console_handling;

import Tasks.Task;
import Tasks.TaskHandler;
import enums.Period;
import exceptions.PastCallException;

import java.io.IOException;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.time.temporal.ChronoField;
import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;

import static enums.Period.*;
import static java.time.temporal.ChronoField.*;

final public class ConsoleHandle {
    static Scanner scanner = new Scanner(System.in);
    static Supplier<LocalDateTime> now = LocalDateTime::now;
    static long id;
    static Map<Integer, Period> periods = new HashMap<>() {{
        put(1, ONCE);
        put(2, DAILY);
        put(3, WEEKLY);
        put(4, MONTHLY);
        put(5, YEARLY);
    }};


    public static void main(String[] args) throws InterruptedException {
        TaskHandler.loadData();

        while (true) {
            int general = validateRangeIntInput("""
                    ___________________
                    Task operations:  1
                    Watch info:       2
                    Save and Exit: 0""", 0, 2);
            switch (general) {
                case 0 -> {
                    TaskHandler.saveData();
                    System.exit(0);
                }
                case 1 -> {
                    int taskOperations = validateRangeIntInput("Add: 1. Modify: 2. Remove: 3. Back: 0", 0, 3);
                    switch (taskOperations) {
                        case 1 -> addTask();
                        case 2 -> {
                            id = validateLongInput("Type 'back' to return.\nPut id of task to modify: ", TaskHandler.getIdList());
                            if (id == -1) continue;
                            modifyTask(TaskHandler.findByID(id));
                        }
                        case 3 -> {
                            id = validateLongInput("Type 'back' to return.\nPut id of task to remove: ", TaskHandler.getIdList());
                            if (id == -1) continue;
                            System.out.printf("|. . .  .  .   Chosen task  .  .  . . .|\n%s\n", TaskHandler.removeByID(id));
                            System.out.println("( -x-x-x- Removed successfully! -x-x-x- )");
                        }
                    }
                }
                case 2 -> {
                    int info = validateRangeIntInput("""
                            Today's tasks         1
                            All active tasks      2
                            Selected date tasks   3
                            Expired tasks         4
                            Removed tasks         5
                            Back                  0""", 0, 5);
                    switch (info) {
                        case 1 -> {
                            System.out.println("Today is " + now.get().format(DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT)));
                            TaskHandler.printTodayTasks();
                        }
                        case 2 -> TaskHandler.printAllActiveTasks();
                        case 3 -> TaskHandler.printTasksOnSpecificDate(validateDateInput());
                        case 4 -> TaskHandler.printExpiredTasks();
                        case 5 -> TaskHandler.printRemovedTasks();
                    }
                }
            }
        }
    }

    private static long validateLongInput(String message, long... args) {
        System.out.print(message);
        while (true) {
            try {
                long n;
                String s = scanner.nextLine();
                if (s.matches("\\d+")) n = Long.parseLong(s);
                else if (s.matches("back")) return -1;
                else throw new IOException();
                for (long arg : args) {
                    if (arg == n) return arg;
                }
                throw new IOException();
            } catch (IOException | InputMismatchException e) {
                System.err.println("Incorrect input! \n" + message);
            }
        }
    }

    private static int validateRangeIntInput(String message, int min, int max) {
        System.out.print(message);
        while (true) {
            try {
                int n;
                String s = scanner.nextLine();
                if (s.matches("\\d+")) n = Integer.parseInt(s);
                else throw new IOException();
                if (n < min || n > max) throw new IOException();
                else return n;
            } catch (IOException | InputMismatchException e) {
                System.err.printf("Incorrect input! Allowed range: from %s to %s\n%s", min, max, message);
            }
        }
    }

    private static String validateStringInput(String message) {
        System.out.println(message);
        while (true) {
            try {
                String temp = scanner.nextLine();
                if (temp.isBlank()) throw new IOException();
                if (temp.equals("-")) return "";
                return temp;
            } catch (IOException e) {
                System.err.println("Empty input!\n" + message);
            }
        }
    }

    public static void addTask() throws InterruptedException {
        String header, description;
        int weekday, period, isPersonal;

        HashMap<ChronoField, Integer> chronos = new LinkedHashMap<>();

        Function<HashMap<ChronoField, Integer>, LocalDateTime> converter = m -> {
            LocalDateTime date = now.get();
            for (ChronoField unit : m.keySet()) date = date.with(unit, m.get(unit));
            return date;
        };

        isPersonal = validateRangeIntInput("Choose status. Personal: 1. Work: 2.", 1, 2);
        header = validateStringInput("Create the header:");
        description = validateStringInput("Write a description. Put '-' if you don't need it:");
        period = validateRangeIntInput("Set the period:\nOnce: 1. Daily: 2. Weekly: 3. Monthly: 4. Yearly: 5", 1, 5);
        while (true) try {
            chronos.put(HOUR_OF_DAY, validateRangeIntInput("Set hours: ", 0, 23));
            chronos.put(MINUTE_OF_HOUR, validateRangeIntInput("Set minutes: ", 0, 59));
            if (period == 3) {
                weekday = validateRangeIntInput("Monday: 1. Tuesday: 2. Wednesday: 3. Thursday: 4. " +
                        "Friday: 5. Saturday: 6, Sunday: 7.\nSet the day of week: ", 1, 7);
                LocalDate serviceDate = LocalDate.now();
                while (!serviceDate.getDayOfWeek().equals(DayOfWeek.of(weekday)))
                    serviceDate = serviceDate.plusDays(1);
                chronos.put(DAY_OF_MONTH, serviceDate.getDayOfMonth());
            } else if (period == 4) {
                chronos.put(DAY_OF_MONTH, validateRangeIntInput("""
                        Set the day (monthly task can't have a day which is not present in every month).
                        Enter the day number:\s""", 1, 28));
            } else if (period == 5) {
                chronos.put(MONTH_OF_YEAR, validateRangeIntInput("Set the month: ", 1, 12));
                chronos.put(DAY_OF_MONTH, validateRangeIntInput("Set the day of month: ", 1,
                        now.get().withMonth(chronos.get(MONTH_OF_YEAR)).getMonth().minLength()));
            }
            LocalDateTime date = converter.apply(chronos);
            if (period == 1 && (date = date.with(validateDateInput())).isBefore(now.get()))
                throw new PastCallException();
            TaskHandler.addNewTaskInstance(isPersonal == 1, header, description, date, periods.get(period));
            System.out.println("( +++++ Added successfully! +++++ )");
            chronos.clear();
            break;
        } catch (PastCallException | DateTimeException e) {
            System.err.println(e.getMessage());
            Thread.sleep(100);
        }
    }

    public static void modifyTask(Task task) {
        System.out.printf("|. . .  .  .   Chosen task   .  .  . . .|\n%s\n. . . . . . . . . . . . . . . . . . . . .\n", task);
        int modify = validateRangeIntInput("Modify Header: 1. Description: 2. Back: 0.", 0, 2);
        if (modify == 0) return;
        if (modify == 1) task.setHeader(validateStringInput("Write a new Header: "));
        if (modify == 2) task.setDescription(validateStringInput("Write a new Description or use '-' to remove it: "));
        System.out.println("( ~~~~~ Modified successfully! ~~~~~ )");
    }

    public static LocalDate validateDateInput() {
        LocalDate date = LocalDate.now();
        date = date.withYear(validateRangeIntInput("Choose the year: ", now.get().getYear(), LocalDate.MAX.getYear()));
        date = date.withMonth(validateRangeIntInput("Choose the month: ",
                LocalDate.now().getYear() == date.getYear() ? now.get().getMonthValue() : 1, 12));
        return date.withDayOfMonth(validateRangeIntInput("Choose the day: ",
                now.get().getYear() == date.getYear() && now.get().getMonthValue() == date.getMonthValue() ? now.get().getDayOfMonth() : 1,
                date.getMonth().length(date.isLeapYear())));
    }
}
