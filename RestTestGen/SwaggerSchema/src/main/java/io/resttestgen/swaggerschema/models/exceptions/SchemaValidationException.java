package io.resttestgen.swaggerschema.models.exceptions;


public class SchemaValidationException extends Exception {
    private Exception innerException;
    private Object targetElement;

    public SchemaValidationException(String typeString, Object targetElement, Exception innerException) {
        super(typeString);
        this.targetElement = targetElement;
        this.innerException = innerException;
    }

    public SchemaValidationException(String typeString, Object targetElement) {
        super(typeString);
        this.targetElement = targetElement;
    }

    public Object getTargetElement() {
        return targetElement;
    }
    public Exception getInnerException() {
        return innerException;
    }
}
