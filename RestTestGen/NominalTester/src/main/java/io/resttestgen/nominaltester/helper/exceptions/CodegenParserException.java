package io.resttestgen.nominaltester.helper.exceptions;

public class CodegenParserException extends Exception {
    private Exception innerException;

    public CodegenParserException(String typeString, Exception innerException) {
        super(typeString);
        this.innerException = innerException;
    }

    public Exception getInnerException() {
        return innerException;
    }
}