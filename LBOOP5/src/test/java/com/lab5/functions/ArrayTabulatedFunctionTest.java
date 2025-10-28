package com.lab5.functions;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import com.lab5.exceptions.*;

import java.util.Iterator;

public class ArrayTabulatedFunctionTest {
    @Test
    public void floorIndexOfXTest() {
        double xFrom = 0.8, xTo = -0.3;
        MathFunction f = new ConstantFunction(0.5);
        ArrayTabulatedFunction obj = new ArrayTabulatedFunction(f, xFrom, xTo, 5);

        assertThrows(IllegalArgumentException.class, () -> obj.floorIndexOfX(-10));
        assertEquals(5, obj.floorIndexOfX(7.5));
        assertEquals(3, obj.floorIndexOfX(0.75));
        assertEquals(4, obj.floorIndexOfX(0.8));
        assertEquals(1, obj.floorIndexOfX(0.1));
    }

    @Test
    public void extrapolateLeftTest() {
        double[] xArray = {0, 0.5, 1, 1.5, 2, 2.5};
        double[] yArray = {0, 0.7071, 1, 1.2247, 1.4142, 1.5811};
        ArrayTabulatedFunction obj = new ArrayTabulatedFunction(xArray, yArray);

        assertEquals(1.4142, obj.extrapolateLeft(1));
    }

    @Test
    public void extrapolateRightTest() {
        double[] xArray = {0, 0.5, 1, 1.5, 2, 2.5};
        double[] yArray = {0, 0.7071, 1, 1.2247, 1.4142, 1.5811};
        ArrayTabulatedFunction obj = new ArrayTabulatedFunction(xArray, yArray);

        assertEquals(1.748, obj.extrapolateRight(3));
    }

    @Test
    public void interpolateTest1() {
        double[] xArray = {0, 0.5, 1, 1.5, 2, 2.5};
        double[] yArray = {0, 0.7071, 1, 1.2247, 1.4142, 1.5811};
        ArrayTabulatedFunction obj = new ArrayTabulatedFunction(xArray, yArray);

        assertEquals(1.3384, obj.interpolate(1.8, 3));
    }

    @Test
    public void interpolateTest2() {
        double[] xArray = {0, 0.5, 1, 1.5, 2, 2.5};
        double[] yArray = {0, 0.7071, 1, 1.2247, 1.4142, 1.5811};
        ArrayTabulatedFunction obj = new ArrayTabulatedFunction(xArray, yArray);

        assertEquals(1.13482, obj.interpolate(1.3, 1, 1.5, 1, 1.2247));
    }

    @Test
    public void interpolateTest3() {
        double[] xArray = {0, 0.5, 1, 1.5, 2, 2.5};
        double[] yArray = {0, 0.7071, 1, 1.2247, 1.4142, 1.5811};
        ArrayTabulatedFunction obj = new ArrayTabulatedFunction(xArray, yArray);

        assertThrows(InterpolationException.class, () -> obj.interpolate(-3.6, 4));
    }

    @Test
    public void interpolateTest4() {
        double[] xArray = {0, 0.5, 1, 1.5, 2, 2.5};
        double[] yArray = {0, 0.7071, 1, 1.2247, 1.4142, 1.5811};
        ArrayTabulatedFunction obj = new ArrayTabulatedFunction(xArray, yArray);

        assertThrows(InterpolationException.class, () -> obj.interpolate(7.5, 4));
    }

    @Test
    public void getCountTest() {
        double[] xArray = {-1.5, -0.5, 0.5, 1.5};
        double[] yArray = {1.25, -0.75, -0.75, 1.25};
        ArrayTabulatedFunction obj = new ArrayTabulatedFunction(xArray, yArray);

        assertEquals(4, obj.getCount());
    }

    @Test
    public void getXTest() {
        double[] xArray = {-1.5, -0.5, 0.5, 1.5};
        double[] yArray = {1.25, -0.75, -0.75, 1.25};
        ArrayTabulatedFunction obj = new ArrayTabulatedFunction(xArray, yArray);

        assertEquals(-1.5, obj.getX(0));
    }

    @Test
    public void getYTest() {
        double[] xArray = {-1.5, -0.5, 0.5, 1.5};
        double[] yArray = {1.25, -0.75, -0.75, 1.25};
        ArrayTabulatedFunction obj = new ArrayTabulatedFunction(xArray, yArray);

        assertEquals(-0.75, obj.getY(2));
    }

    @Test
    public void setY() {
        double[] xArray = {-1.5, -0.5, 0.5, 1.5};
        double[] yArray = {1.25, -0.75, -0.75, 1.25};
        ArrayTabulatedFunction obj = new ArrayTabulatedFunction(xArray, yArray);

        obj.setY(3, 1);
        assertEquals(1, obj.getY(3));
    }

    @Test
    public void indexOfXTest() {
        double[] xArray = {-1.5, -0.5, 0.5, 1.5};
        double[] yArray = {1.25, -0.75, -0.75, 1.25};
        ArrayTabulatedFunction obj = new ArrayTabulatedFunction(xArray, yArray);

        assertEquals(-1, obj.indexOfX(0));
        assertEquals(2, obj.indexOfX(0.5));
        assertEquals(-1, obj.indexOfX(3));
    }

    @Test
    public void indexOfYTest() {
        double[] xArray = {-1.5, -0.5, 0.5, 1.5};
        double[] yArray = {1.25, -0.75, -0.75, 1.25};
        ArrayTabulatedFunction obj = new ArrayTabulatedFunction(xArray, yArray);

        assertEquals(-1, obj.indexOfY(-1.1));
        assertEquals(0, obj.indexOfY(1.25));
        assertEquals(-1, obj.indexOfY(-4));
    }

    @Test
    public void leftBoundTest() {
        double[] xArray = {-1.5, -0.5, 0.5, 1.5};
        double[] yArray = {1.25, -0.75, -0.75, 1.25};
        ArrayTabulatedFunction obj = new ArrayTabulatedFunction(xArray, yArray);

        assertEquals(-1.5, obj.leftBound());
    }

    @Test
    public void rightBoundTest() {
        double[] xArray = {-1.5, -0.5, 0.5, 1.5};
        double[] yArray = {1.25, -0.75, -0.75, 1.25};
        ArrayTabulatedFunction obj = new ArrayTabulatedFunction(xArray, yArray);

        assertEquals(1.5, obj.rightBound());
    }

    double[] xArray = {-1, 0, 2, 5};
    double[] yArray = {1, 0, 4, 25};

    @Test
    public void removeTest1() {
        double[] newxArray = {0, 2, 5};
        double[] newyArray = {0, 4, 25};

        ArrayTabulatedFunction obj = new ArrayTabulatedFunction(xArray, yArray);
        obj.remove(0);

        assertEquals(newxArray[0], obj.getX(0));
        assertEquals(newxArray[1], obj.getX(1));

        assertEquals(newyArray[0], obj.getY(0));
        assertEquals(newyArray[1], obj.getY(1));
    }

    @Test
    public void removeTest2() {
        double[] newxArray = {-1, 0, 5};
        double[] newyArray = {1, 0, 25};

        ArrayTabulatedFunction obj = new ArrayTabulatedFunction(xArray, yArray);
        obj.remove(2);

        assertEquals(newxArray[1], obj.getX(1));
        assertEquals(newxArray[2], obj.getX(2));

        assertEquals(newyArray[0], obj.getY(0));
        assertEquals(newyArray[1], obj.getY(1));
    }

    @Test
    public void removeTest3() {
        double[] newxArray = {-1, 0, 2};
        double[] newyArray = {1, 0, 4};

        ArrayTabulatedFunction obj = new ArrayTabulatedFunction(xArray, yArray);
        obj.remove(3);

        assertEquals(newxArray[1], obj.getX(1));
        assertEquals(newxArray[2], obj.getX(2));

        assertEquals(newyArray[0], obj.getY(0));
        assertEquals(newyArray[1], obj.getY(1));
    }

    @Test
    public void checkLengthIsTheSameTest() {
        assertThrows(DifferentLengthOfArraysException.class, () -> new ArrayTabulatedFunction(new double[] {0, 1, 2}, new double[] {0, 4}));
    }

    @Test
    public void checkSortedTest() {
        assertThrows(ArrayIsNotSortedException.class, () -> new ArrayTabulatedFunction(new double[] {0, 3, 2}, new double[] {0, 9, 4}));
    }

    @Test
    public void iteratorTest1() {
        ArrayTabulatedFunction obj = new ArrayTabulatedFunction(new double[] {0, 1, 2}, new double[] {0, 1, 4});
        Iterator<Point> iterator = obj.iterator();

        int i = 0;
        while(iterator.hasNext()) {
            Point point = iterator.next();
            assertEquals(obj.getX(i), point.x);
            assertEquals(obj.getY(i), point.y);
            ++i;
        }
    }

    @Test
    public void iteratorTest2() { 
        ArrayTabulatedFunction tabulatedFunction = new ArrayTabulatedFunction(new double[] {0, 1, 2}, new double[] {0, 1, 4});
        Iterator<Point> iterator = tabulatedFunction.iterator();

        int i = 0;
        for (Point point : tabulatedFunction) {
            point = iterator.next();
            assertEquals(tabulatedFunction.getX(i), point.x);
            assertEquals(tabulatedFunction.getY(i), point.y);
            ++i;
        }
    }
    
    @Test
    public void testGetXWithInvalidIndex() {
        ArrayTabulatedFunction obj = new ArrayTabulatedFunction(new double[]{1, 2}, new double[]{1, 4});

        // Отрицательный индекс
        assertThrows(IllegalArgumentException.class, () -> obj.getX(-1));

        // Индекс >= count
        assertThrows(IllegalArgumentException.class, () -> obj.getX(2));
        assertThrows(IllegalArgumentException.class, () -> obj.getX(10));
    }

    @Test
    public void testGetYWithInvalidIndex() {
        ArrayTabulatedFunction obj = new ArrayTabulatedFunction(new double[]{1, 2}, new double[]{1, 4});
        assertThrows(IllegalArgumentException.class, () -> obj.getY(-1));
        assertThrows(IllegalArgumentException.class, () -> obj.getY(2));
    }

    @Test
    public void testSetYWithInvalidIndex() {
        ArrayTabulatedFunction obj = new ArrayTabulatedFunction(new double[]{1, 2}, new double[]{1, 4});
        assertThrows(IllegalArgumentException.class, () -> obj.setY(-1, 5.0));
        assertThrows(IllegalArgumentException.class, () -> obj.setY(2, 5.0));
    }
    
    @Test
    public void testConstructorWithLessThan2Points() {
        // Меньше 2 точек
        assertThrows(IllegalArgumentException.class, () -> {
            new ArrayTabulatedFunction(new double[]{1}, new double[]{1});
        });

        // Пустые массивы
        assertThrows(IllegalArgumentException.class, () -> {
            new ArrayTabulatedFunction(new double[]{}, new double[]{});
        });
    }

    @Test
    public void testConstructorWithFunctionInvalidCount() {
        MathFunction f = new SqrFunction();

        // count < 2
        assertThrows(IllegalArgumentException.class, () -> {
            new ArrayTabulatedFunction(f, 0, 1, 1);
        });

        // count = 0
        assertThrows(IllegalArgumentException.class, () -> {
            new ArrayTabulatedFunction(f, 0, 1, 0);
        });
    }
    
    @Test
    public void testFloorIndexOfXWithXLessThanLeftBound() {
        ArrayTabulatedFunction obj = new ArrayTabulatedFunction(new double[]{1, 2, 3}, new double[]{1, 4, 9});

        // x меньше левой границы
        assertThrows(IllegalArgumentException.class, () -> obj.floorIndexOfX(0.5));
    }
    
    @Test
    public void testInterpolateWithInvalidFloorIndex() {
        ArrayTabulatedFunction obj = new ArrayTabulatedFunction(new double[]{1, 2, 3}, new double[]{1, 4, 9});

        // Невалидный floorIndex
        assertThrows(ArrayIndexOutOfBoundsException.class, () -> obj.interpolate(1.5, -1));
        assertThrows(InterpolationException.class, () -> obj.interpolate(1.5, 2));
    }
    
    @Test
    public void testRemoveWithInvalidIndex() {
        ArrayTabulatedFunction obj = new ArrayTabulatedFunction(new double[]{1, 2, 3}, new double[]{1, 4, 9});

        assertThrows(ArrayIndexOutOfBoundsException.class, () -> obj.remove(-1));
        assertThrows(ArrayIndexOutOfBoundsException.class, () -> obj.remove(3));

        // Проверка, что после удаления нельзя оставить меньше 2 точек
        obj.remove(1); // теперь 2 точки
        assertThrows(IllegalArgumentException.class, () -> obj.remove(0)); // останется 1 точка
    }
    
    @Test
    public void testInsertWithInvalidParameters() {
        ArrayTabulatedFunction obj = new ArrayTabulatedFunction(new double[]{1, 3}, new double[]{1, 9});

        // NaN значения
        assertThrows(IllegalArgumentException.class, () -> obj.insert(Double.NaN, 5.0));
        assertThrows(IllegalArgumentException.class, () -> obj.insert(2.0, Double.NaN));

        // Бесконечности
        assertThrows(IllegalArgumentException.class, () -> obj.insert(Double.POSITIVE_INFINITY, 5.0));
    }
}
