package io.resttestgen.nominaltester.fieldgenerator.manufacturetraces;

import java.util.HashMap;

public class ConstructorTrace extends ManufactureTrace {

    public ConstructorTrace(Class<?> targetClass, Object[] constructorParameters, Object createdObject) {
        this.targetClass = targetClass;
        this.constructorParameters = constructorParameters;
        this.createdObject = createdObject;
    }

    public Class<?> getTargetClass() {
        return targetClass;
    }

    public Object[] getConstructorParameters() {
        return constructorParameters;
    }

    private final Class<?> targetClass;
    private final Object[] constructorParameters;

    public Object getCreatedObject() {
        return createdObject;
    }

    private final Object createdObject;

    @Override
    public String constructionString() {
        String className = targetClass.getSimpleName();
        String objectName = getObjectName(createdObject);
        if (targetClass.equals(HashMap.class)) {
            return String.format("%s<String, Object> %s = new %s<>();", className, objectName, className);
        } else {
            return String.format("%s %s = new %s();", className, objectName, className);
        }
    }
}
