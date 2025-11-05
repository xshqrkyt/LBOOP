package com.lab6.exceptions;

public class DifferentLengthOfArraysException extends RuntimeException {
    public DifferentLengthOfArraysException() {}

    public DifferentLengthOfArraysException(String message) {
        super(message);
    }
}
