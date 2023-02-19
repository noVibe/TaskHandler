package Tasks;

import enums.Period;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;


public abstract class Task implements Comparable<Task>, Serializable {
    final private long id;
    final private boolean isPersonal;
    private String header;
    private String description;
    private LocalDateTime date;
    final private Period period;
    private boolean isActual;


    public Task(long id, boolean isPersonal, String header, String description, LocalDateTime date, Period period) {
        this.id = id;
        this.isPersonal = isPersonal;
        this.header = header;
        this.description = description;
        this.date = date;
        this.period = period;
        isActual = true;
    }

    public abstract boolean isActiveAt(LocalDate date);

    protected abstract void refreshDate();

    @Override
    public int compareTo(Task o) {
        if (date.toLocalTime().isAfter((o.date.toLocalTime()))) return 1;
        else return -1;
    }

    public LocalDate getDate() {
        return date.toLocalDate();
    }

    public LocalDateTime getDateTime() {
        return date;
    }

    public boolean isActual() {
        return isActual;
    }

    public void setActual(boolean isActual) {
        this.isActual = isActual;
    }

    public void setDateTime(LocalDateTime date) {
        this.date = date;
    }

    @Override
    public String toString() {
        return "Header: " + header +
                (description.isEmpty() ? "" : "\nDescription: " + description) +
                "\nTask Status: " + (isPersonal ? "personal" : "work")+
                "\nid: " + id;
    }

    public long getId() {
        return id;
    }

    public Period getPeriod() {
        return period;
    }

    public void setHeader(String header) {
        this.header = header;
    }

    public void setDescription(String description) {
        this.description = description;
    }

}
