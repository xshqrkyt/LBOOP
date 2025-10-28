package com.lab5.exceptions;

public class DifferentLengthOfArraysException extends RuntimeException {
    public DifferentLengthOfArraysException() {}

    public DifferentLengthOfArraysException(String message) {
        super(message);
    }
}
