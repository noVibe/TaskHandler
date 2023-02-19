package Tasks.Periodic;

import Tasks.Task;
import enums.Period;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;

public class OneTimeTask extends Task {
    public OneTimeTask(long id, boolean isPersonal, String header, String description, LocalDateTime date, Period period) {
        super(id, isPersonal, header, description, date, period);
    }

    @Override
    public boolean isActiveAt(LocalDate date) {
        return getDate().equals(date);
    }

    @Override
    protected void refreshDate() {
        if (getDate().isBefore(LocalDate.now())) setActual(false);
    }

    @Override
    public String toString() {
        return getPeriod() + ": " + getDateTime().format(DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT)) + " at " +
        getDateTime().format(DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT)) + "\n" + super.toString();
    }
}

