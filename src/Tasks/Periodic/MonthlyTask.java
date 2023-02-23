package Tasks.Periodic;

import Tasks.Task;
import enums.Period;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;

public class MonthlyTask extends Task {
    public MonthlyTask(long id, boolean isPersonal, String header, String description, LocalDateTime date, Period period) {
        super(id, isPersonal, header, description, date, period);
    }

    @Override
    public boolean isActiveAt(LocalDate date) {
        return date.getDayOfMonth() == getDate().getDayOfMonth();
    }

    @Override
    protected void refreshDate() {
        setDateTime(getDateTime().withYear(LocalDate.now().getYear()).withMonth(LocalDate.now().getMonthValue()));
        if (getDate().isBefore(LocalDate.now())) setDateTime(getDateTime().plusMonths(1));
    }

    @Override
    public String toString() {
        return getPeriod() + ": Day " + getDate().getDayOfMonth() + " at " +
                getDateTime().format(DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT)) + "\n" + super.toString();
    }
}
