package io.resttestgen.nominaltester.fieldgenerator.manufacturetraces;

public abstract class ManufactureTrace {

    public abstract String constructionString();

    protected String getObjectName(Object value) {
        String objectId = String.valueOf(System.identityHashCode(value));
        if (value == null) return "null0";
        String className = value.getClass().getSimpleName().toLowerCase();
        return className + objectId;
    }

    @Override
    public String toString() {
        return constructionString();
    }
}
