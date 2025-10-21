package ru.ssau.tk.samsa.lb6.operations;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import ru.ssau.tk.samsa.lb6.exceptions.InconsistentFunctionsException;
import ru.ssau.tk.samsa.lb6.functions.*;
import ru.ssau.tk.samsa.lb6.functions.factory.*;


class TabulatedFunctionOperationServiceTest {
    @Test
    public void asPointsTest() {
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
        
        assertThrows(InconsistentFunctionsException.class, () -> obj.add(tabulatedFunction2, tabulatedFunction1));
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
        
        assertThrows(InconsistentFunctionsException.class, () -> obj.subtract(tabulatedFunction1, tabulatedFunction2));
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
    @Test
    void testMultiplyOperations() {
        TabulatedFunctionOperationService service = new TabulatedFunctionOperationService();

        // Тест 1: Array * Array
        testMultiplyCombination(
                new ArrayTabulatedFunction(new double[]{1, 2, 3}, new double[]{2, 4, 6}),
                new ArrayTabulatedFunction(new double[]{1, 2, 3}, new double[]{3, 5, 7}),
                new double[]{6, 20, 42}, // 2*3=6, 4*5=20, 6*7=42
                "Array * Array",
                service
        );

        // Тест 2: LinkedList * LinkedList
        testMultiplyCombination(
                new LinkedListTabulatedFunction(new double[]{1, 2, 3}, new double[]{1, 2, 3}),
                new LinkedListTabulatedFunction(new double[]{1, 2, 3}, new double[]{2, 3, 4}),
                new double[]{2, 6, 12}, // 1*2=2, 2*3=6, 3*4=12
                "LinkedList * LinkedList",
                service
        );

        // Тест 3: Array * LinkedList
        testMultiplyCombination(
                new ArrayTabulatedFunction(new double[]{0, 1, 2}, new double[]{0, 1, 2}),
                new LinkedListTabulatedFunction(new double[]{0, 1, 2}, new double[]{1, 1, 1}),
                new double[]{0, 1, 2}, // 0*1=0, 1*1=1, 2*1=2
                "Array * LinkedList",
                service
        );

        // Тест 4: LinkedList * Array
        testMultiplyCombination(
                new LinkedListTabulatedFunction(new double[]{1, 2}, new double[]{5, 10}),
                new ArrayTabulatedFunction(new double[]{1, 2}, new double[]{2, 3}),
                new double[]{10, 30}, // 5*2=10, 10*3=30
                "LinkedList * Array",
                service
        );
    }

    @Test
    void testDivideOperations() {
        TabulatedFunctionOperationService service = new TabulatedFunctionOperationService();

        // Тест 1: Array / Array
        testDivideCombination(
                new ArrayTabulatedFunction(new double[]{1, 2, 4}, new double[]{6, 12, 24}),
                new ArrayTabulatedFunction(new double[]{1, 2, 4}, new double[]{2, 3, 4}),
                new double[]{3, 4, 6}, // 6/2=3, 12/3=4, 24/4=6
                "Array / Array",
                service
        );

        // Тест 2: LinkedList / LinkedList
        testDivideCombination(
                new LinkedListTabulatedFunction(new double[]{1, 2, 3}, new double[]{10, 20, 30}),
                new LinkedListTabulatedFunction(new double[]{1, 2, 3}, new double[]{2, 5, 10}),
                new double[]{5, 4, 3}, // 10/2=5, 20/5=4, 30/10=3
                "LinkedList / LinkedList",
                service
        );

        // Тест 3: Array / LinkedList
        testDivideCombination(
                new ArrayTabulatedFunction(new double[]{1, 2}, new double[]{8, 12}),
                new LinkedListTabulatedFunction(new double[]{1, 2}, new double[]{2, 3}),
                new double[]{4, 4}, // 8/2=4, 12/3=4
                "Array / LinkedList",
                service
        );
    }

    @Test
    void testDivideByZero() {
        TabulatedFunctionOperationService service = new TabulatedFunctionOperationService();

        TabulatedFunction numerator = new ArrayTabulatedFunction(
                new double[]{1, 2, 3}, new double[]{10, 20, 30});
        TabulatedFunction denominator = new ArrayTabulatedFunction(
                new double[]{1, 2, 3}, new double[]{2, 0, 5}); // деление на 0 во второй точке

        // Должно бросить ArithmeticException при делении на 0
        assertThrows(ArithmeticException.class, () -> {
            service.divide(numerator, denominator);
        }, "Should throw ArithmeticException when dividing by zero");
    }

    @Test
    void testMultiplyWithDifferentFactories() {
        // Тестируем с разными фабриками в сервисе
        testWithFactory(new ArrayTabulatedFunctionFactory(), "Array Factory");
        testWithFactory(new LinkedListTabulatedFunctionFactory(), "LinkedList Factory");
    }

    private void testWithFactory(TabulatedFunctionFactory factory, String factoryName) {
        TabulatedFunctionOperationService service = new TabulatedFunctionOperationService(factory);

        TabulatedFunction a = new ArrayTabulatedFunction(new double[]{1, 2}, new double[]{3, 4});
        TabulatedFunction b = new ArrayTabulatedFunction(new double[]{1, 2}, new double[]{2, 3});

        TabulatedFunction resultMultiply = service.multiply(a, b);
        TabulatedFunction resultDivide = service.divide(a, b);

        // Проверяем тип результата
        if (factory instanceof ArrayTabulatedFunctionFactory) {
            assertTrue(resultMultiply instanceof ArrayTabulatedFunction,
                    factoryName + ": multiply result should be ArrayTabulatedFunction");
            assertTrue(resultDivide instanceof ArrayTabulatedFunction,
                    factoryName + ": divide result should be ArrayTabulatedFunction");
        } else {
            assertTrue(resultMultiply instanceof LinkedListTabulatedFunction,
                    factoryName + ": multiply result should be LinkedListTabulatedFunction");
            assertTrue(resultDivide instanceof LinkedListTabulatedFunction,
                    factoryName + ": divide result should be LinkedListTabulatedFunction");
        }

        // Проверяем корректность вычислений
        assertEquals(6.0, resultMultiply.getY(0), 1e-10, factoryName + ": 3*2=6");
        assertEquals(12.0, resultMultiply.getY(1), 1e-10, factoryName + ": 4*3=12");

        assertEquals(1.5, resultDivide.getY(0), 1e-10, factoryName + ": 3/2=1.5");
        assertEquals(4.0/3.0, resultDivide.getY(1), 1e-10, factoryName + ": 4/3≈1.333");
    }

    private void testMultiplyCombination(TabulatedFunction a, TabulatedFunction b,
                                         double[] expectedY, String testName,
                                         TabulatedFunctionOperationService service) {
        TabulatedFunction result = service.multiply(a, b);

        assertEquals(a.getCount(), result.getCount(), testName + ": result count should match");

        for (int i = 0; i < result.getCount(); i++) {
            assertEquals(a.getX(i), result.getX(i), 1e-10,
                    testName + ": X values should be preserved at index " + i);
            assertEquals(expectedY[i], result.getY(i), 1e-10,
                    testName + ": multiplication result at index " + i);
        }

        System.out.println(testName + " - PASSED");
    }

    private void testDivideCombination(TabulatedFunction a, TabulatedFunction b,
                                       double[] expectedY, String testName,
                                       TabulatedFunctionOperationService service) {
        TabulatedFunction result = service.divide(a, b);

        assertEquals(a.getCount(), result.getCount(), testName + ": result count should match");

        for (int i = 0; i < result.getCount(); i++) {
            assertEquals(a.getX(i), result.getX(i), 1e-10,
                    testName + ": X values should be preserved at index " + i);
            assertEquals(expectedY[i], result.getY(i), 1e-10,
                    testName + ": division result at index " + i);
        }

        System.out.println(testName + " - PASSED");
    }

    @Test
    void testInconsistentFunctionsForMultiplyDivide() {
        TabulatedFunctionOperationService service = new TabulatedFunctionOperationService();

        TabulatedFunction a = new ArrayTabulatedFunction(new double[]{1, 2, 3}, new double[]{1, 2, 3});
        TabulatedFunction b = new ArrayTabulatedFunction(new double[]{1, 3, 4}, new double[]{1, 2, 3}); // разные X

        assertThrows(InconsistentFunctionsException.class, () -> {
            service.multiply(a, b);
        }, "Should throw InconsistentFunctionsException for different X values in multiply");

        assertThrows(InconsistentFunctionsException.class, () -> {
            service.divide(a, b);
        }, "Should throw InconsistentFunctionsException for different X values in divide");

        // Разное количество точек
        TabulatedFunction c = new ArrayTabulatedFunction(new double[]{1, 2}, new double[]{1, 2});
        assertThrows(InconsistentFunctionsException.class, () -> {
            service.multiply(a, c);
        }, "Should throw InconsistentFunctionsException for different count in multiply");
    }
}
