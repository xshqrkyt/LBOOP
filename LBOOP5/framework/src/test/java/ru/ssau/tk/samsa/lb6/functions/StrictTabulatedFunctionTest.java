package ru.ssau.tk.samsa.lb6.functions;

import org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

import java.util.Iterator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class StrictTabulatedFunctionTest {
    @Test
    public void getCountTest() {
        StrictTabulatedFunction function = new StrictTabulatedFunction(new ArrayTabulatedFunction(new double[] {1, 2, 3}, new double[] {1, 4, 9}));
        assertEquals(3, function.getCount());
    }

    @Test
    public void getXTest() {
        StrictTabulatedFunction function = new StrictTabulatedFunction(new ArrayTabulatedFunction(new double[] {1, 2, 3}, new double[] {1, 4, 9}));
        assertEquals(2, function.getX(1));
    }

    @Test
    public void getYTest() {
        StrictTabulatedFunction function = new StrictTabulatedFunction(new ArrayTabulatedFunction(new double[] {1, 2, 3}, new double[] {1, 4, 9}));
        assertEquals(9, function.getY(2));
    }

    @Test
    public void setYTest() {
        StrictTabulatedFunction function = new StrictTabulatedFunction(new ArrayTabulatedFunction(new double[] {1, 2, 3}, new double[] {1, 4, 9}));
        function.setY(1, 7);
        assertEquals(7, function.getY(1));
    }

    @Test
    public void indexOfXTest() {
        StrictTabulatedFunction function = new StrictTabulatedFunction(new ArrayTabulatedFunction(new double[] {1, 2, 3}, new double[] {1, 4, 9}));

        assertEquals(-1, function.indexOfX(0));
        assertEquals(1, function.indexOfX(2));
        assertEquals(-1, function.indexOfX(4));
    }

    @Test
    public void indexOfYTest() {
        StrictTabulatedFunction function = new StrictTabulatedFunction(new ArrayTabulatedFunction(new double[] {1, 2, 3}, new double[] {1, 4, 9}));

        assertEquals(-1, function.indexOfY(0));
        assertEquals(1, function.indexOfY(4));
        assertEquals(-1, function.indexOfY(10));
    }

    @Test
    public void leftBoundTest() {
        StrictTabulatedFunction function = new StrictTabulatedFunction(new ArrayTabulatedFunction(new double[] {1, 2, 3}, new double[] {1, 4, 9}));

        assertEquals(1, function.leftBound());
    }

    @Test
    public void rightBoundTest() {
        StrictTabulatedFunction function = new StrictTabulatedFunction(new ArrayTabulatedFunction(new double[] {1, 2, 3}, new double[] {1, 4, 9}));

        assertEquals(3, function.rightBound());
    }

    @Test
    public void applyTest() {
        StrictTabulatedFunction function = new StrictTabulatedFunction(new ArrayTabulatedFunction(new double[] {1, 2, 3}, new double[] {1, 4, 9}));

        assertThrows(UnsupportedOperationException.class, () -> function.apply(0));
        assertEquals(9, function.apply(3));
    }

    @Test
    public void iteratorTest1() {
        StrictTabulatedFunction function = new StrictTabulatedFunction(new ArrayTabulatedFunction(new double[] {0, 1, 2}, new double[] {0, 1, 4}));
        Iterator<Point> iterator = function.iterator();

        int i = 0;
        while(iterator.hasNext()) {
            Point point = iterator.next();
            assertEquals(function.getX(i), point.x);
            assertEquals(function.getY(i), point.y);
            ++i;
        }
    }

    @Test
    public void iteratorTest2() {
        StrictTabulatedFunction tabulatedFunction = new StrictTabulatedFunction(new ArrayTabulatedFunction(new double[] {0, 1, 2}, new double[] {0, 1, 4}));
        Iterator<Point> iterator = tabulatedFunction.iterator();

        int i = 0;
        for (Point point : tabulatedFunction) {
            point = iterator.next();
            assertEquals(tabulatedFunction.getX(i), point.x);
            assertEquals(tabulatedFunction.getY(i), point.y);
            ++i;
        }
    }
}