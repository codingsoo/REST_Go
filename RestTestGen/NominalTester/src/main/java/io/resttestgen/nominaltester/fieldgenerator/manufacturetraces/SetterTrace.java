package io.resttestgen.nominaltester.fieldgenerator.manufacturetraces;

import java.lang.reflect.Method;

public class SetterTrace extends ManufactureTrace {

    public SetterTrace(Object callingObject, Method setterMethod, Object[] setterParmaters) {
        this.callingObject = callingObject;
        this.setterMethod = setterMethod;
        this.setterParmaters = setterParmaters;
    }

    public Object getCallingObject() {
        return callingObject;
    }

    public Method getSetterMethod() {
        return setterMethod;
    }

    public Object[] getSetterParmaters() {
        return setterParmaters;
    }

    private final Object callingObject;
    private final Method setterMethod;
    private final Object[] setterParmaters;

    @Override
    public String constructionString() {
        String callingObjectName = getObjectName(callingObject);
        String setterObjectName = getObjectName(setterParmaters[0]);
        return String.format("%s.%s(%s);", callingObjectName, setterMethod.getName(), setterObjectName);
    }


}
