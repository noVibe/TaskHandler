package Tasks.Periodic;

import Tasks.Task;
import enums.Period;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;

public class YearlyTask extends Task {

    public YearlyTask(long id, boolean isPersonal, String header, String description, LocalDateTime date, Period period) {
        super(id, isPersonal, header, description, date, period);
    }

    @Override
    public boolean isActiveAt(LocalDate date) {
        return date.getMonth().equals(this.getDate().getMonth()) && date.getDayOfMonth() == this.getDate().getDayOfMonth();
    }

    protected void refreshDate() {
        while (getDate().isBefore(LocalDate.now())) setDateTime(getDateTime().plusYears(1));
    }

    @Override
    public String toString() {
        return getPeriod() + ": " + getDate().getDayOfMonth() + " of " + getDate().getMonth() +
                " at " + getDateTime().format(DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT)) +
                "\n" + super.toString();
    }
}

