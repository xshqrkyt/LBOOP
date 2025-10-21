package ru.ssau.tk.samsa.lb6.concurrent;

import org.junit.jupiter.api.Test;
import ru.ssau.tk.samsa.lb6.functions.Point;
import ru.ssau.tk.samsa.lb6.functions.TabulatedFunction;
import ru.ssau.tk.samsa.lb6.functions.ArrayTabulatedFunction;

import java.util.Iterator;

import static org.junit.jupiter.api.Assertions.*;

public class SynchronizedTabulatedFunctionTest {
    @Test
    public void getCountTest() {
        SynchronizedTabulatedFunction f = new SynchronizedTabulatedFunction(new ArrayTabulatedFunction(new double[] {1, 2, 3}, new double[] {1, 4, 9}));
        assertEquals(3, f.getCount());
    }

    @Test
    public void testIteratorReturnsSnapshot() {
        TabulatedFunction base = new ArrayTabulatedFunction(new double[]{1,2,3}, new double[]{10,20,30});
        SynchronizedTabulatedFunction sync = new SynchronizedTabulatedFunction(base);

        Iterator<Point> it = sync.iterator();

        // Модифицируем исходную функцию после получения итератора
        base.setY(0, 999);

        // Проверяем, что итератор вернёт старое значение
        Point p = it.next();
        assertEquals(1.0, p.x, 1e-6);
        assertEquals(10.0, p.y, 1e-6);  // должно остаться старое значение
    }

    @Test
    public void getXTest() {
        SynchronizedTabulatedFunction f = new SynchronizedTabulatedFunction(new ArrayTabulatedFunction(new double[] {1, 2, 3}, new double[] {1, 4, 9}));
        assertEquals(2, f.getX(1));
    }

    @Test
    public void getYTest() {
        SynchronizedTabulatedFunction f = new SynchronizedTabulatedFunction(new ArrayTabulatedFunction(new double[] {1, 2, 3}, new double[] {1, 4, 9}));
        assertEquals(9, f.getY(2));
    }

    @Test
    public void setYTest() {
        SynchronizedTabulatedFunction f = new SynchronizedTabulatedFunction(new ArrayTabulatedFunction(new double[] {1, 2, 3}, new double[] {1, 4, 9}));
        f.setY(1, 7);
        assertEquals(7, f.getY(1));
    }

    @Test
    public void indexOfXTest() {
        SynchronizedTabulatedFunction f = new SynchronizedTabulatedFunction(new ArrayTabulatedFunction(new double[] {1, 2, 3}, new double[] {1, 4, 9}));

        assertEquals(-1, f.indexOfX(1.5));
    }

    @Test
    public void indexOfYTest() {
        SynchronizedTabulatedFunction f = new SynchronizedTabulatedFunction(new ArrayTabulatedFunction(new double[] {1, 2, 3}, new double[] {1, 4, 9}));

        assertEquals(1, f.indexOfY(4));
    }

    @Test
    public void leftBoundTest() {
        SynchronizedTabulatedFunction f = new SynchronizedTabulatedFunction(new ArrayTabulatedFunction(new double[] {1, 2, 3}, new double[] {1, 4, 9}));

        assertEquals(1, f.leftBound());
    }

    @Test
    public void rightBoundTest() {
        SynchronizedTabulatedFunction f = new SynchronizedTabulatedFunction(new ArrayTabulatedFunction(new double[] {1, 2, 3}, new double[] {1, 4, 9}));

        assertEquals(3, f.rightBound());
    }

     @Test
     public void applyTest() {
         SynchronizedTabulatedFunction f = new SynchronizedTabulatedFunction(new ArrayTabulatedFunction(new double[] {1, 2, 3}, new double[] {1, 4, 9}));

         assertEquals(4, f.apply(2));
     }

    @Test
    public void iteratorTest() {
        SynchronizedTabulatedFunction f = new SynchronizedTabulatedFunction(new ArrayTabulatedFunction(new double[] {1, 2, 3}, new double[] {1, 4, 9}));

        int i = 0;
        for (Point point : f) {
            assertEquals(i + 1, point.x);
            assertEquals(f.getY(i++), point.y);
        }
    }

    @Test
    public void doSynchronouslyTest1() {
        SynchronizedTabulatedFunction f = new SynchronizedTabulatedFunction(new ArrayTabulatedFunction(new double[] {1, 2, 3}, new double[] {1, 4, 9}));

        SynchronizedTabulatedFunction.Operation<Integer> oper = new SynchronizedTabulatedFunction.Operation<Integer>() {
            @Override
            public Integer apply(SynchronizedTabulatedFunction function) {
                return function.getCount();
            }
        };

        assertEquals(3, f.doSynchronously(oper));
    }

    @Test
    public void doSynchronouslyTest2() {
        SynchronizedTabulatedFunction f = new SynchronizedTabulatedFunction(new ArrayTabulatedFunction(new double[] {1, 2, 3}, new double[] {1, 4, 9}));

        SynchronizedTabulatedFunction.Operation<Void> oper = new SynchronizedTabulatedFunction.Operation<Void>() {
            @Override
            public Void apply(SynchronizedTabulatedFunction function) {
                function.setY(0, 0);
                return null;
            }
        };

        assertEquals(null, f.doSynchronously(oper));
        assertEquals(0, f.getY(0));
    }

    @Test
    public void doSynchronouslyTest3() {
        SynchronizedTabulatedFunction f = new SynchronizedTabulatedFunction(new ArrayTabulatedFunction(new double[] {1, 2, 3}, new double[] {1, 4, 9}));

        SynchronizedTabulatedFunction.Operation<Double> oper = new SynchronizedTabulatedFunction.Operation<Double>() {
            @Override
            public Double apply(SynchronizedTabulatedFunction function) {
                return function.apply(3);
            }
        };

        assertEquals(9, f.doSynchronously(oper));
    }

}
