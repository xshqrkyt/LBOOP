package ru.ssau.tk.samsa.LB2.operations;

import ru.ssau.tk.samsa.LB2.functions.*;
import ru.ssau.tk.samsa.LB2.functions.factory.*;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class TabulatedFunctionOperationServiceTest {
    @Test
    public void asPointsTest1() {
        TabulatedFunction tabulatedFunction = new ArrayTabulatedFunction(new double[] {1, 3, 5}, new double[] {1, 9, 25});
        TabulatedFunctionOperationService obj = new TabulatedFunctionOperationService();
        Point[] points = obj.asPoints(tabulatedFunction);

        int i = 0;
        for (Point point : points) {
            assertEquals(tabulatedFunction.getX(i), point.x);
            assertEquals(tabulatedFunction.getY(i), point.y);
            ++i;
        }
    }

    @Test
    public void asPointsTest2() {
        TabulatedFunction tabulatedFunction = new ArrayTabulatedFunction(new double[] {}, new double[] {});
        TabulatedFunctionOperationService obj = new TabulatedFunctionOperationService();
        Point[] points = obj.asPoints(tabulatedFunction);

        int i = 0;
        for (Point point : points) {
            assertEquals(tabulatedFunction.getX(i), point.x);
            assertEquals(tabulatedFunction.getY(i), point.y);
            ++i;
        }
    }

    @Test
    public void asPointsTest3() {
        TabulatedFunction tabulatedFunction = new ArrayTabulatedFunction(new double[] {1}, new double[] {7});
        TabulatedFunctionOperationService obj = new TabulatedFunctionOperationService();
        Point[] points = obj.asPoints(tabulatedFunction);

        int i = 0;
        for (Point point : points) {
            assertEquals(tabulatedFunction.getX(i), point.x);
            assertEquals(tabulatedFunction.getY(i), point.y);
            ++i;
        }
    }

    @Test
    public void getTest() {
        TabulatedFunctionFactory factory = new LinkedListTabulatedFunctionFactory();
        TabulatedFunctionOperationService obj = new TabulatedFunctionOperationService(factory);

        assertEquals(obj.get(), factory);
    }

    @Test
    public void setTest() {
        TabulatedFunctionFactory factory = new ArrayTabulatedFunctionFactory();
        TabulatedFunction tabulatedFunction = factory.create(new double[] {1, 2, 3}, new double[] {1, 4, 9});

        TabulatedFunctionOperationService obj = new TabulatedFunctionOperationService();
        obj.set(factory);

        assertEquals(2, tabulatedFunction.getX(1));
        assertEquals(1, tabulatedFunction.getY(0));
    }

    @Test
    public void addTest1() {
        TabulatedFunctionOperationService obj = new TabulatedFunctionOperationService();

        TabulatedFunction tabulatedFunction1 = new ArrayTabulatedFunction(new double[] {1, 2, 3}, new double[] {1, 4, 9});
        TabulatedFunction tabulatedFunction2 = new ArrayTabulatedFunction(new double[] {1, 2, 3}, new double[] {-1, 0, 1});
        TabulatedFunction tabulatedFunction = obj.add(tabulatedFunction1, tabulatedFunction2);

        assertEquals(1, tabulatedFunction.getX(0));
        assertEquals(2, tabulatedFunction.getX(1));
        assertEquals(3, tabulatedFunction.getX(2));

        assertEquals(0, tabulatedFunction.getY(0));
        assertEquals(4, tabulatedFunction.getY(1));
        assertEquals(10, tabulatedFunction.getY(2));
    }

    @Test
    public void addTest2() {
        TabulatedFunctionOperationService obj = new TabulatedFunctionOperationService();

        TabulatedFunction tabulatedFunction1 = new ArrayTabulatedFunction(new double[] {1, 2, 3, 4}, new double[] {1, 4, 9, 16});
        TabulatedFunction tabulatedFunction2 = new ArrayTabulatedFunction(new double[] {1, 2, 3}, new double[] {-1, 0, 1});
        TabulatedFunction tabulatedFunction = obj.add(tabulatedFunction2, tabulatedFunction1);
    }

    @Test
    public void addTest3() {
        TabulatedFunctionOperationService obj = new TabulatedFunctionOperationService();

        TabulatedFunction tabulatedFunction1 = new LinkedListTabulatedFunction(new double[] {1, 2, 3, 4}, new double[] {1, 4, 9, 16});
        TabulatedFunction tabulatedFunction2 = new ArrayTabulatedFunction(new double[] {1, 2, 3, 4}, new double[] {-1, 0, 1, 2});
        TabulatedFunction tabulatedFunction = obj.add(tabulatedFunction1, tabulatedFunction2);

        int i = 1;
        for (Point point : tabulatedFunction)
            assertEquals(i++, point.x);

        assertEquals(0, tabulatedFunction.getY(0));
        assertEquals(4, tabulatedFunction.getY(1));
        assertEquals(10, tabulatedFunction.getY(2));
        assertEquals(18, tabulatedFunction.getY(3));
    }

    @Test
    public void addTest4() {
        TabulatedFunctionOperationService obj = new TabulatedFunctionOperationService();

        TabulatedFunction tabulatedFunction1 = new LinkedListTabulatedFunction(new double[] {1, 2, 3, 4}, new double[] {1, 4, 9, 16});
        TabulatedFunction tabulatedFunction2 = new LinkedListTabulatedFunction(new double[] {1, 2, 3, 4}, new double[] {-1, 0, 1, 2});
        TabulatedFunction tabulatedFunction = obj.add(tabulatedFunction1, tabulatedFunction2);

        assertEquals(0, tabulatedFunction.getY(0));
        assertEquals(4, tabulatedFunction.getY(1));
        assertEquals(10, tabulatedFunction.getY(2));
        assertEquals(18, tabulatedFunction.getY(3));
    }

    @Test
    public void subtractTest1() {
        TabulatedFunctionOperationService obj = new TabulatedFunctionOperationService();

        TabulatedFunction tabulatedFunction1 = new ArrayTabulatedFunction(new double[] {1, 2, 3}, new double[] {1, 4, 9});
        TabulatedFunction tabulatedFunction2 = new ArrayTabulatedFunction(new double[] {1, 2, 3}, new double[] {-1, 0, 1});
        TabulatedFunction tabulatedFunction = obj.subtract(tabulatedFunction2, tabulatedFunction1);

        assertEquals(1, tabulatedFunction.getX(0));
        assertEquals(2, tabulatedFunction.getX(1));
        assertEquals(3, tabulatedFunction.getX(2));

        assertEquals(-2, tabulatedFunction.getY(0));
        assertEquals(-4, tabulatedFunction.getY(1));
        assertEquals(-8, tabulatedFunction.getY(2));
    }

    @Test
    public void subtractTest2() {
        TabulatedFunctionOperationService obj = new TabulatedFunctionOperationService();

        TabulatedFunction tabulatedFunction1 = new ArrayTabulatedFunction(new double[] {1, 2, 3}, new double[] {1, 4, 9});
        TabulatedFunction tabulatedFunction2 = new ArrayTabulatedFunction(new double[] {1, 3, 4}, new double[] {-1, 1, 2});
        TabulatedFunction tabulatedFunction = obj.subtract(tabulatedFunction1, tabulatedFunction2);
    }

    @Test
    public void subtractTest3() {
        TabulatedFunctionOperationService obj = new TabulatedFunctionOperationService();

        TabulatedFunction tabulatedFunction1 = new ArrayTabulatedFunction(new double[] {1, 2, 3, 4}, new double[] {1, 4, 9, 16});
        TabulatedFunction tabulatedFunction2 = new LinkedListTabulatedFunction(new double[] {1, 2, 3, 4}, new double[] {-1, 0, 1, 2});
        TabulatedFunction tabulatedFunction = obj.subtract(tabulatedFunction1, tabulatedFunction2);

        int i = 1;
        for (Point point : tabulatedFunction)
            assertEquals(i++, point.x);

        assertEquals(2, tabulatedFunction.getY(0));
        assertEquals(4, tabulatedFunction.getY(1));
        assertEquals(8, tabulatedFunction.getY(2));
        assertEquals(14, tabulatedFunction.getY(3));
    }

    @Test
    public void subtractTest4() {
        TabulatedFunctionOperationService obj = new TabulatedFunctionOperationService();

        TabulatedFunction tabulatedFunction1 = new LinkedListTabulatedFunction(new double[] {1, 2, 3, 4}, new double[] {1, 4, 9, 16});
        TabulatedFunction tabulatedFunction2 = new LinkedListTabulatedFunction(new double[] {1, 2, 3, 4}, new double[] {-1, 0, 1, 2});
        TabulatedFunction tabulatedFunction = obj.subtract(tabulatedFunction2, tabulatedFunction1);
        
        assertEquals(-2, tabulatedFunction.getY(0));
        assertEquals(-4, tabulatedFunction.getY(1));
        assertEquals(-8, tabulatedFunction.getY(2));
        assertEquals(-14, tabulatedFunction.getY(3));
    }
}
