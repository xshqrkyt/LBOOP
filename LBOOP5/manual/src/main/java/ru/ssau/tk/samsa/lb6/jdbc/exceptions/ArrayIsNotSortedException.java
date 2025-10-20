package ru.ssau.tk.samsa.lb6.jdbc.exceptions;

public class ArrayIsNotSortedException extends RuntimeException {
    public ArrayIsNotSortedException() {}

    public ArrayIsNotSortedException(String message) {
        super(message);
    }
}
