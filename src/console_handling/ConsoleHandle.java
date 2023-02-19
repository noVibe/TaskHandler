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
import java.util.function.Predicate;
import java.util.function.Supplier;

import static enums.Period.*;
import static java.time.temporal.ChronoField.*;

final public class ConsoleHandle {
    static Scanner scanner = new Scanner(System.in);
    static Supplier<LocalDateTime> now = LocalDateTime::now;
    static Function<HashMap<ChronoField, Integer>, LocalDateTime> converter = m -> {
        LocalDateTime date = now.get();
        for (ChronoField unit : m.keySet()) date = date.with(unit, m.get(unit));
        return date;
    };
    static Predicate<HashMap<ChronoField, Integer>> checkIfPast = m -> now.get().isAfter(converter.apply(m));

    static Task task;
    static String header, description;
    static int weekday, period, isPersonal, modify;
    static long id;
    static Map<Integer, Period> periods = new HashMap<>() {{
        put(1, ONCE);
        put(2, DAILY);
        put(3, WEEKLY);
        put(4, MONTHLY);
        put(5, YEARLY);
    }};

    static HashMap<ChronoField, Integer> chronos = new LinkedHashMap<>();

    public static void main(String[] args) throws InterruptedException {
        TaskHandler.loadData();
        /*
        1 - task operations:
            1 add,
            2 modify:  1 - header  2 - description
            3 remove
        2 - info print:
            1 daily tasks.tmp
            2 all tasks.tmp
            3 specific date tasks.tmp
            4 removed tasks.tmp
            5 expired.tmp tasks.tmp
        0 - stop
         */

        while (true) {
            int general = validateIntInput("""
                    ___________________
                    Task operations:  1
                    Watch info:       2
                    Save and Exit: 0""", 1, 2, 0);
            switch (general) {
                case 0 -> {
                    TaskHandler.saveData();
                    System.exit(0);
                }
                case 1 -> {
                    int taskOperations = validateIntInput("Add: 1. Modify: 2. Remove: 3. Back: 0", 1, 2, 3, 0);
                    switch (taskOperations) {
                        case 1 -> {
                            isPersonal = validateIntInput("Choose status. Personal: 1. Work: 2.", 1, 2);
                            header = validateStringInput("Create the header:");
                            description = validateStringInput("Write a description. Put '-' if you don't need it:");
                            period = validateIntInput("Set the period:\nOnce: 1. Daily: 2. Weekly: 3. Monthly: 4. Yearly: 5", 1, 2, 3, 4, 5);
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
                                } else {
                                    if (period == 1) {
                                        chronos.put(YEAR, validateRangeIntInput("Set the year: ", now.get().getYear(), LocalDate.MAX.getYear()));
                                    }
                                    if (period == 5 || period == 1) {
                                        chronos.put(MONTH_OF_YEAR, validateRangeIntInput("Set the month: ",
                                                period == 1 && LocalDate.now().getYear() == chronos.get(YEAR) ? now.get().getMonthValue() : 1, 12));
                                    }
                                    if (period == 4) {
                                        chronos.put(DAY_OF_MONTH, validateRangeIntInput("""
                                                        Set the day (last day is limited by the current month.
                                                        It will be adjusted automatically for other ones).
                                                        Enter the day number:\s""", 1,
                                                now.get().getMonth().length(LocalDate.now().isLeapYear())));
                                    }
                                    if (period == 5) {
                                        chronos.put(DAY_OF_MONTH, validateRangeIntInput("Set the day of month: ", 1,
                                                now.get().withMonth(chronos.get(MONTH_OF_YEAR))
                                                        .getMonth().length(LocalDate.now().isLeapYear())));

                                    }
                                    if (period == 1) {
                                        chronos.put(DAY_OF_MONTH, validateRangeIntInput("Set the day: ",
                                                now.get().getYear() == chronos.get(YEAR) &&
                                                        now.get().getMonthValue() == chronos.get(MONTH_OF_YEAR) ? now.get().getDayOfMonth() : 1,
                                                Month.of(chronos.get(MONTH_OF_YEAR)).length(Year.isLeap(chronos.get(YEAR)))));
                                    }
                                }
                                if (period == 1 && checkIfPast.test(chronos)) throw new PastCallException();
                                TaskHandler.addNewTaskInstance(isPersonal == 1, header, description, converter.apply(chronos), periods.get(period));
                                System.out.println("( +++++ Added successfully! +++++ )");
                                chronos.clear();
                                break;
                            } catch (PastCallException | DateTimeException e) {
                                System.err.println(e.getMessage());
                                Thread.sleep(100);
                            }
                        }
                        case 2 -> {
                            id = validateLongInput("Type 'back' to return.\nPut id of task to modify: ", TaskHandler.getIdList());
                            if (id == -1) continue;
                            task = TaskHandler.findByID(id);
                            System.out.printf("|. . .  .  .   Chosen task   .  .  . . .|\n%s\n. . . . . . . . . . . . . . . . . . . . .\n", task);
                            modify = validateIntInput("Modify Header: 1. Description: 2. Back: 0.", 1, 2, 0);
                            if (modify == 0) continue;
                            if (modify == 1) {
                                task.setHeader(validateStringInput("Write a new Header: "));
                            }
                            if (modify == 2) {
                                task.setDescription(validateStringInput("Write a new Description or use '-' to remove it: "));
                            }
                            System.out.println("( ~~~~~ Modified successfully! ~~~~~ )");
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
                    int info = validateIntInput("""
                            Today's tasks         1
                            All active tasks      2
                            Selected date tasks   3
                            Expired tasks         4
                            Removed tasks         5
                            Back                  0""", 1, 2, 3, 4, 5, 0);
                    switch (info) {
                        case 1 -> {
                            System.out.println("Today is " + now.get().format(DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT)));
                            TaskHandler.printTodayTasks();
                        }
                        case 2 -> TaskHandler.printAllActiveTasks();
                        case 3 -> {
                            int year = validateRangeIntInput("Choose the year: ", now.get().getYear(), LocalDate.MAX.getYear());
                            int month = validateRangeIntInput("Choose the month: ",
                                    LocalDate.now().getYear() == year ? now.get().getMonthValue() : 1, 12);
                            int day = validateRangeIntInput("Choose the day: ", period == 1 &&
                                    now.get().getYear() == year && now.get().getMonthValue() == month ? now.get().getDayOfMonth() : 1, 31);
                            TaskHandler.printTasksOnSpecificDate(year, month, day);
                        }
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

    private static int validateIntInput(String message, int... args) {
        System.out.println(message);
        while (true) {
            try {
                int n;
                String s = scanner.nextLine();
                if (s.matches("\\d+")) n = Integer.parseInt(s);
                else throw new IOException();
                for (int arg : args) {
                    if (arg == n) return arg;
                }
                throw new IOException();
            } catch (IOException | InputMismatchException e) {
                System.err.println("Incorrect input!\n" + message);
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
}
