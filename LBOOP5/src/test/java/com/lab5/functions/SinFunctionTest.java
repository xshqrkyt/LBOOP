package com.lab5.functions;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class SinFunctionTest {

    @Test
    void testApply() {
        SinFunction function = new SinFunction();

        assertEquals(0.0, function.apply(0.0), 1e-10);
        assertEquals(1.0, function.apply(Math.PI / 2), 1e-10);
        assertEquals(0.0, function.apply(Math.PI), 1e-10);
        assertEquals(-1.0, function.apply(3 * Math.PI / 2), 1e-10);
    }

    @Test
    void testAndThen() {
        SinFunction sin = new SinFunction();
        SqrFunction sqr = new SqrFunction();

        MathFunction composite = sin.andThen(sqr);
        assertEquals(1.0, composite.apply(Math.PI / 2), 1e-10);
        assertEquals(0.0, composite.apply(Math.PI), 1e-10);
    }
}