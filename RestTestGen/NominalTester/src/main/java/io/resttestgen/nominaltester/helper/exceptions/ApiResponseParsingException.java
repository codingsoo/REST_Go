package io.resttestgen.nominaltester.helper.exceptions;


public class ApiResponseParsingException extends Exception {
    private Exception innerException;
    private Object parsedElement;

    public ApiResponseParsingException(String typeString, Object parsedElement, Exception innerException) {
        super(typeString);
        this.parsedElement = parsedElement;
        this.innerException = innerException;
    }

    public Object getParsedElement() {
        return parsedElement;
    }

    public Exception getInnerException() {
        return innerException;
    }
}
