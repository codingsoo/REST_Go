package io.resttestgen.nominaltester.models.exceptions;

/**
 * Exception thrown when Method's parameters differ from the matching Swagger codegen parameters
 * or because of an unsuccessful cast due to to a wrong parameter generation
 */
public class ParametersMismatchException extends Exception {
    public ParametersMismatchException(String msg){
        super(msg);
    }
}
