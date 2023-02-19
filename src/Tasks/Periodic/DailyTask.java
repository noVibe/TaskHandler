package Tasks.Periodic;

import Tasks.Task;
import enums.Period;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;

import static java.time.temporal.ChronoField.*;

public class DailyTask extends Task {
    public DailyTask(long id, boolean isPersonal, String header, String description, LocalDateTime date, Period period) {
        super(id, isPersonal, header, description, date, period);
    }

    @Override
    public boolean isActiveAt(LocalDate date) {
        return true;
    }

    @Override
    protected void refreshDate() {
        LocalTime time = getDateTime().toLocalTime();
        setDateTime(LocalDateTime.now().with(MINUTE_OF_HOUR, time.getMinute()).with(HOUR_OF_DAY, time.getHour()));
    }

    @Override
    public String toString() {
        return getPeriod() + ": at " + getDateTime().format(DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT)) + "\n" +
                super.toString();
    }
}
