package io.resttestgen.nominaltester.fieldgenerator.manufacturetraces;

import io.resttestgen.nominaltester.helper.ReflectionHelper;

import java.lang.reflect.Method;

public class EnumTrace extends ManufactureTrace {
    private final Class enumClass;
    private final Object value;

    public <T> EnumTrace(Class<T> pojoClass, Object value) {
        this.enumClass = pojoClass;
        this.value = value;
    }


    @Override
    public String constructionString() {
        String objectName = getObjectName(value);
        String enumName = enumClass.getName().replace("$", ".");
        Method fromValue = ReflectionHelper.getMethodByName(enumClass, "fromValue");

        if (value == null) {
            // enum value is null
            if (fromValue != null) {
                return String.format("%s %s = null;", enumName, objectName);
            }
            return String.format("%s %s = null;", enumName, objectName);
        }

        // enum value is not null
        if (fromValue != null) {
            return String.format("%s %s = %s.fromValue(\"%s\");", enumName, objectName, enumName, value.toString());
        }
        return String.format("%s %s = %s.valueOf(\"%s\");", enumName, objectName, enumName, value.toString());
    }
}
