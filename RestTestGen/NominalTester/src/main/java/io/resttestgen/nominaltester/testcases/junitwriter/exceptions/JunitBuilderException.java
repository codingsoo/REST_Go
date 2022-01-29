package io.resttestgen.nominaltester.testcases.junitwriter.exceptions;

public class JunitBuilderException extends Exception {
    private Exception innerException;
    private Object targetElement;

    public JunitBuilderException(String typeString, Object targetElement, Exception innerException) {
        super(typeString);
        this.targetElement = targetElement;
        this.innerException = innerException;
    }

    public JunitBuilderException(String typeString, Object targetElement) {
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