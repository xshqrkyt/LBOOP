package ru.ssau.tk.samsa.LB2.functions;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class ArrayTabulatedFunctionTest {
    @Test
    public void ArrayTabulatedFunctionTest1() {
        double[] xArray = {-1.5, -0.5, 0.5, 1.5};
        double[] yArray = {1.25, -0.75, -0.75, 1.25};

        ArrayTabulatedFunction obj = new ArrayTabulatedFunction(xArray, yArray);
        assertEquals(4, obj.count);
        assertEquals(0.5, obj.xValues[2]);
        assertEquals(-0.75, obj.yValues[1]);
   }

    @Test
    public void ArrayTabulatedFunctionTest2() {
        double xFrom = 0.8, xTo = -0.3;
        MathFunction f = new ConstantFunction(0.5);
        ArrayTabulatedFunction obj = new ArrayTabulatedFunction(f, xFrom, xTo, 5);

        assertEquals(0.8, obj.xValues[4]);
        assertEquals(0.5, obj.yValues[2]);
    }

    @Test
    public void floorIndexOfXTest() {
        double xFrom = 0.8, xTo = -0.3;
        MathFunction f = new ConstantFunction(0.5);
        ArrayTabulatedFunction obj = new ArrayTabulatedFunction(f, xFrom, xTo, 5);

        assertEquals(0, obj.floorIndexOfX(-10));
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

        obj.interpolate(-3.6, 4);
    }

    @Test
    public void interpolateTest4() {
        double[] xArray = {0, 0.5, 1, 1.5, 2, 2.5};
        double[] yArray = {0, 0.7071, 1, 1.2247, 1.4142, 1.5811};
        ArrayTabulatedFunction obj = new ArrayTabulatedFunction(xArray, yArray);

        obj.interpolate(7.5, 4);
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

    @Test
    public void iteratorTest1() {
        double[] xArray = {0, 1, 2};
        double[] yArray = {0, 1, 4};

        ArrayTabulatedFunction obj = new ArrayTabulatedFunction(xArray, yArray);
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
        double[] xArray = {0, 1, 2};
        double[] yArray = {0, 1, 4};

        ArrayTabulatedFunction tabulatedFunction = new ArrayTabulatedFunction(xArray, yArray);
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
