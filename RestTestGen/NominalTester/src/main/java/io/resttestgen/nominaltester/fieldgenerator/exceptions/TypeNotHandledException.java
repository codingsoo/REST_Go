package io.resttestgen.nominaltester.fieldgenerator.exceptions;

public class TypeNotHandledException extends Exception {
    public TypeNotHandledException(String typeString) {
        super(typeString);
    }
}