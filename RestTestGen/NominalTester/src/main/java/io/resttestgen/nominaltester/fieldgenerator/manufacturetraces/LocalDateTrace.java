package io.resttestgen.nominaltester.fieldgenerator.manufacturetraces;

import org.threeten.bp.LocalDate;

public class LocalDateTrace extends ManufactureTrace {
    private final Object value;

    public <T> LocalDateTrace(Object value) {
        this.value = value;
    }

    @Override
    public String constructionString() {
        String objectName = getObjectName(value);
        LocalDate localDate = (LocalDate)value;

        if (value == null) {
            return String.format("LocalDate %s = null;", objectName);
        }

        int year, month, day;
        year = localDate.getYear();
        month = localDate.getMonthValue();
        day = localDate.getDayOfMonth();

        return String.format("LocalDate %s = new LocalDate(%d, %d, %d);", objectName, year, month, day);
    }
}
