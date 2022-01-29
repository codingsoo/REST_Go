package io.resttestgen.nominaltester.fieldgenerator.exceptions;

public class FieldGenerationException extends Exception {
    private Exception innerException;

    public FieldGenerationException(String typeString, Exception innerException) {
        super(typeString);
        this.innerException = innerException;
    }

    public FieldGenerationException(String msg) {
        super(msg);
    }

    public Exception getInnerException() {
        return innerException;
    }
}

