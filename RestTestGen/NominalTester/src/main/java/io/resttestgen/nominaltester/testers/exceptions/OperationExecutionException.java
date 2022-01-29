package io.resttestgen.nominaltester.testers.exceptions;

/**
 * Execution thrown if there is any error during the operation executions
 */
public class OperationExecutionException extends Exception {
    private Exception innerException;

    public OperationExecutionException(String typeString, Exception innerException) {
        super(typeString);
        this.innerException = innerException;
    }

    public Exception getInnerException() {
        return innerException;
    }
}

