package ru.ssau.tk.samsa.lb6.functions;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ZeroFunctionTest {
    @Test
    public void applyTest1() {
        ConstantFunction f = new ZeroFunction();
        assertEquals(0, f.apply(1.6));
    }

    @Test
    public void applyTest2() {
        ConstantFunction f = new ZeroFunction();
        assertEquals(0, f.apply(1));
    }
}