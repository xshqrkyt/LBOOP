package com.lab5.functions;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

class SqrFunctionTest {
    @Test
    public void applyTest1() {
        SqrFunction f = new SqrFunction();
        assertEquals(49, f.apply(7));
    }

    @Test
    public void applyTest2() {
        SqrFunction f = new SqrFunction();
        assertEquals(2.25, f.apply(1.5));
    }

    @Test
    public void applyTest3() {
        SqrFunction f = new SqrFunction();
        assertEquals(6.25, f.apply(-2.5));
    }
}