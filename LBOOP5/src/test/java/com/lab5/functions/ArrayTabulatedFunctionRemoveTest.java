package com.lab5.functions;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class ArrayTabulatedFunctionRemoveTest {
    @Test
    void testRemoveFirst() {
        double[] xValues = {0.0, 1.0, 2.0, 3.0};
        double[] yValues = {0.0, 1.0, 4.0, 9.0};
        ArrayTabulatedFunction function = new ArrayTabulatedFunction(xValues, yValues);

        function.remove(0);

        assertEquals(3, function.getCount());
        assertEquals(1.0, function.leftBound(), 1e-10);
        assertEquals(1.0, function.getX(0), 1e-10);
    }

    @Test
    void testRemoveLast() {
        double[] xValues = {0.0, 1.0, 2.0, 3.0};
        double[] yValues = {0.0, 1.0, 4.0, 9.0};
        ArrayTabulatedFunction function = new ArrayTabulatedFunction(xValues, yValues);

        function.remove(3);

        assertEquals(3, function.getCount());
        assertEquals(2.0, function.rightBound(), 1e-10);
        assertEquals(2.0, function.getX(2), 1e-10);
    }

    @Test
    void testRemoveMiddle() {
        double[] xValues = {0.0, 1.0, 2.0, 3.0};
        double[] yValues = {0.0, 1.0, 4.0, 9.0};
        ArrayTabulatedFunction function = new ArrayTabulatedFunction(xValues, yValues);

        function.remove(1);

        assertEquals(3, function.getCount());
        assertEquals(0.0, function.getX(0), 1e-10);
        assertEquals(2.0, function.getX(1), 1e-10);
        assertEquals(3.0, function.getX(2), 1e-10);
        assertEquals(4.0, function.getY(1), 1e-10);
    }
}
