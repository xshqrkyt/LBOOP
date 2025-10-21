package ru.ssau.tk.samsa.lb6.io;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;

public class TabulatedFunctionFileInputStreamTest {
    String s = "3\n0.0 0.0\n1.0 1.0\n2.0 4.0\n";

    @Test
    public void mainTest() {
        System.setIn(new ByteArrayInputStream(s.getBytes()));

        TabulatedFunctionFileInputStream.main(new String[]{});

        System.setIn(System.in);
    }
}