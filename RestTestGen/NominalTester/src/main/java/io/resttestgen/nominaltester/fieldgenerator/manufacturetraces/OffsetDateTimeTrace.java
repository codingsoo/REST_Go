package io.resttestgen.nominaltester.fieldgenerator.manufacturetraces;

import org.threeten.bp.OffsetDateTime;

public class OffsetDateTimeTrace extends ManufactureTrace {
    private final Object value;

    public <T> OffsetDateTimeTrace(Object value, String fieldName) {
        this.value = value;
    }

    @Override
    public String constructionString() {
        String objectName = getObjectName(value);
        OffsetDateTime dateTime = (OffsetDateTime)value;

        if (value == null) {
            return String.format("OffsetDateTime %s = null;", objectName);
        }

        return String.format("org.threeten.bp.OffsetDateTime %s = org.threeten.bp.OffsetDateTime.parse(%s);", objectName, dateTime.toString());
    }
}
