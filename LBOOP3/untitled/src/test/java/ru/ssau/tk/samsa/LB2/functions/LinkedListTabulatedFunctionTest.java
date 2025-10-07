package ru.ssau.tk.samsa.LB2.functions;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class LinkedListTabulatedFunctionTest {

    @Test
    void testConstructorWithArrays() {
        double[] xValues = {0.0, 1.0, 2.0, 3.0};
        double[] yValues = {0.0, 1.0, 4.0, 9.0};

        LinkedListTabulatedFunction function = new LinkedListTabulatedFunction(xValues, yValues);

        assertEquals(4, function.getCount());
        assertEquals(0.0, function.leftBound(), 1e-10);
        assertEquals(3.0, function.rightBound(), 1e-10);
    }

    @Test
    void testConstructorWithFunction() {
        SqrFunction source = new SqrFunction();
        LinkedListTabulatedFunction function = new LinkedListTabulatedFunction(source, 0, 2, 5);

        assertEquals(5, function.getCount());
        assertEquals(0.0, function.leftBound(), 1e-10);
        assertEquals(2.0, function.rightBound(), 1e-10);
        assertEquals(0.0, function.getY(0), 1e-10);
        assertEquals(1.0, function.getY(2), 1e-10);
        assertEquals(4.0, function.getY(4), 1e-10);
    }

    @Test
    void testGetXGetY() {
        double[] xValues = {0.0, 1.0, 2.0};
        double[] yValues = {0.0, 1.0, 4.0};
        LinkedListTabulatedFunction function = new LinkedListTabulatedFunction(xValues, yValues);

        assertEquals(0.0, function.getX(0), 1e-10);
        assertEquals(1.0, function.getX(1), 1e-10);
        assertEquals(2.0, function.getX(2), 1e-10);

        assertEquals(0.0, function.getY(0), 1e-10);
        assertEquals(1.0, function.getY(1), 1e-10);
        assertEquals(4.0, function.getY(2), 1e-10);
    }

    @Test
    void testSetY() {
        double[] xValues = {0.0, 1.0, 2.0};
        double[] yValues = {0.0, 1.0, 4.0};
        LinkedListTabulatedFunction function = new LinkedListTabulatedFunction(xValues, yValues);

        function.setY(1, 5.0);
        assertEquals(5.0, function.getY(1), 1e-10);
    }

    @Test
    void testIndexOfX() {
        double[] xValues = {0.0, 1.0, 2.0};
        double[] yValues = {0.0, 1.0, 4.0};
        LinkedListTabulatedFunction function = new LinkedListTabulatedFunction(xValues, yValues);

        assertEquals(0, function.indexOfX(0.0));
        assertEquals(1, function.indexOfX(1.0));
        assertEquals(2, function.indexOfX(2.0));
        assertEquals(-1, function.indexOfX(3.0));
    }

    @Test
    void testIndexOfY() {
        double[] xValues = {0.0, 1.0, 2.0};
        double[] yValues = {0.0, 1.0, 4.0};
        LinkedListTabulatedFunction function = new LinkedListTabulatedFunction(xValues, yValues);

        assertEquals(0, function.indexOfY(0.0));
        assertEquals(1, function.indexOfY(1.0));
        assertEquals(2, function.indexOfY(4.0));
        assertEquals(-1, function.indexOfY(3.0));
    }

    @Test
    void testFloorIndexOfX() {
        double[] xValues = {0.0, 1.0, 2.0, 3.0};
        double[] yValues = {0.0, 1.0, 4.0, 9.0};
        LinkedListTabulatedFunction function = new LinkedListTabulatedFunction(xValues, yValues);

        assertEquals(0, function.floorIndexOfX(-0.5)); // Левее всех
        assertEquals(0, function.floorIndexOfX(0.0));  // Точно на первом элементе
        assertEquals(0, function.floorIndexOfX(0.5));  // Между 0 и 1
        assertEquals(1, function.floorIndexOfX(1.5));  // Между 1 и 2
        assertEquals(2, function.floorIndexOfX(2.5));  // Между 2 и 3
        assertEquals(4, function.floorIndexOfX(3.5));  // Правее всех
    }

    @Test
    void testApply() {
        double[] xValues = {0.0, 1.0, 2.0};
        double[] yValues = {0.0, 1.0, 4.0};
        LinkedListTabulatedFunction function = new LinkedListTabulatedFunction(xValues, yValues);

        // Точное значение
        assertEquals(1.0, function.apply(1.0), 1e-10);

        // Интерполяция
        assertEquals(0.5, function.apply(0.5), 1e-10);
        assertEquals(2.5, function.apply(1.5), 1e-10);

        // Экстраполяция слева
        assertEquals(-1.0, function.apply(-1.0), 1e-10);

        // Экстраполяция справа
        assertEquals(7.0, function.apply(3.0), 1e-10);
    }

    @Test
    void testSinglePointFunction() {
        double[] xValues = {2.0};
        double[] yValues = {4.0};
        LinkedListTabulatedFunction function = new LinkedListTabulatedFunction(xValues, yValues);

        assertEquals(4.0, function.apply(0.0), 1e-10);
        assertEquals(4.0, function.apply(2.0), 1e-10);
        assertEquals(4.0, function.apply(5.0), 1e-10);
    }

    @Test
    void testCircularStructure() {
        double[] xValues = {0.0, 1.0, 2.0};
        double[] yValues = {0.0, 1.0, 4.0};
        LinkedListTabulatedFunction function = new LinkedListTabulatedFunction(xValues, yValues);

        // Проверяем, что список действительно циклический
        assertEquals(0.0, function.getX(0), 1e-10);
        assertEquals(2.0, function.getX(2), 1e-10);
        // После последнего должен идти первый
    }

    @Test
    public void checkLengthIsTheSameTest() {
        double[] xArray = {0, 1, 2};
        double[] yArray = {0, 4};

        ArrayTabulatedFunction obj = new ArrayTabulatedFunction(xArray, yArray);
    }

    @Test
    public void checkSortedTest() {
        double[] xArray = {0, 3, 2};
        double[] yArray = {0, 9, 4};

        ArrayTabulatedFunction obj = new ArrayTabulatedFunction(xArray, yArray);
    }
}
