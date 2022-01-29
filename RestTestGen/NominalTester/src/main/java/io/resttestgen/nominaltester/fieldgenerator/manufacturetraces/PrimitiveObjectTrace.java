package io.resttestgen.nominaltester.fieldgenerator.manufacturetraces;

import org.apache.commons.lang3.ClassUtils;

public class PrimitiveObjectTrace extends ManufactureTrace {
    private final Class<?> itemType;

    public PrimitiveObjectTrace(Class<?> itemType, Object value) {
        this.itemType = itemType;
        this.value = value;
    }

    public Class<?> getItemType() {
        return itemType;
    }

    public Object getValue() {
        return value;
    }

    private final Object value;

    @Override
    public String constructionString() {
        String objectName = getObjectName(value);

        boolean primitiveWrapper =
                ClassUtils.isPrimitiveWrapper(getItemType());

        String valueString = "null";
        if (value != null) {
            valueString = value.toString();
        }

        if (value instanceof String) {
            valueString = "\"" + valueString + "\"";
        }

        if (primitiveWrapper && value != null) {
            String baseString = "%s %s = %s.valueOf(\"%s\");";
            return String.format(baseString, getItemType().getSimpleName(), objectName, getItemType().getSimpleName(), valueString);
        } else if (getItemType().getSimpleName().equals("BigDecimal")) {
            String baseString = "%s %s = %s.valueOf(%s);";
            return String.format(baseString, getItemType().getSimpleName(), objectName, getItemType().getSimpleName(), valueString);
        } else {
            String baseString = "%s %s = %s;";
            return String.format(baseString, getItemType().getSimpleName(), objectName, valueString);
        }
    }


}
