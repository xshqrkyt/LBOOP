package ru.ssau.tk.samsa.LB2.operations;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import ru.ssau.tk.samsa.LB2.functions.*;
import ru.ssau.tk.samsa.LB2.functions.factory.*;
import ru.ssau.tk.samsa.LB2.concurrent.SynchronizedTabulatedFunction;

class TabulatedDifferentialOperatorTest {
    @Test
    void testDeriveWithDifferentFactoryCombinations() {
        // Исходные данные
        double[] xValues = {1.0, 2.0, 3.0, 4.0};
        double[] yValues = {1.0, 4.0, 9.0, 16.0}; // f(x) = x^2

        // Комбинация 1: Array -> Array
        testDeriveCombination(
                new ArrayTabulatedFunction(xValues, yValues),
                new ArrayTabulatedFunctionFactory(),
                "Array -> Array"
        );

        // Комбинация 2: Array -> LinkedList
        testDeriveCombination(
                new ArrayTabulatedFunction(xValues, yValues),
                new LinkedListTabulatedFunctionFactory(),
                "Array -> LinkedList"
        );

        // Комбинация 3: LinkedList -> Array
        testDeriveCombination(
                new LinkedListTabulatedFunction(xValues, yValues),
                new ArrayTabulatedFunctionFactory(),
                "LinkedList -> Array"
        );

        // Комбинация 4: LinkedList -> LinkedList
        testDeriveCombination(
                new LinkedListTabulatedFunction(xValues, yValues),
                new LinkedListTabulatedFunctionFactory(),
                "LinkedList -> LinkedList"
        );
    }

    private void testDeriveCombination(TabulatedFunction original,
                                       TabulatedFunctionFactory factory,
                                       String combinationName) {
        System.out.println("Testing combination: " + combinationName);

        TabulatedDifferentialOperator operator = new TabulatedDifferentialOperator(factory);
        TabulatedFunction derivative = operator.derive(original);

        // Проверяем, что производная имеет правильный тип
        if (factory instanceof ArrayTabulatedFunctionFactory) {
            assertTrue(derivative instanceof ArrayTabulatedFunction,
                    combinationName + ": derivative should be ArrayTabulatedFunction");
        } else {
            assertTrue(derivative instanceof LinkedListTabulatedFunction,
                    combinationName + ": derivative should be LinkedListTabulatedFunction");
        }

        // Проверяем количество точек
        assertEquals(original.getCount(), derivative.getCount(),
                combinationName + ": derivative should have same number of points");

        // Проверяем x значения (должны совпадать)
        for (int i = 0; i < original.getCount(); i++) {
            assertEquals(original.getX(i), derivative.getX(i), 1e-10,
                    combinationName + ": X values should be identical at index " + i);
        }

        // Проверяем вычисление производной (для f(x) = x^2 производная ≈ 2x)
        // Допускаем погрешность из-за численного дифференцирования
        double tolerance = 0.5; // Более высокая погрешность для численных методов

        for (int i = 0; i < derivative.getCount() - 1; i++) {
            double expectedDerivative = 2 * derivative.getX(i); // 2x для x^2
            double actualDerivative = derivative.getY(i);
            assertEquals(expectedDerivative, actualDerivative, tolerance,
                    combinationName + ": derivative at x=" + derivative.getX(i) +
                            " should be ≈ " + expectedDerivative);
        }

        // Последняя точка использует левую разность (значение предпоследней точки)
        int lastIndex = derivative.getCount() - 1;
        double lastExpected = 2 * derivative.getX(lastIndex - 1);
        double lastActual = derivative.getY(lastIndex);
        assertEquals(lastExpected, lastActual, tolerance,
                combinationName + ": last point should use left difference");

        System.out.println(combinationName + " - PASSED");
    }

    @Test
    void testDeriveWithLinearFunction() {
        // Тест с линейной функцией f(x) = 2x + 1 (производная должна быть 2 везде)
        double[] xValues = {0.0, 1.0, 2.0, 3.0};
        double[] yValues = {1.0, 3.0, 5.0, 7.0}; // 2x + 1

        TabulatedDifferentialOperator operator = new TabulatedDifferentialOperator();
        TabulatedFunction original = new ArrayTabulatedFunction(xValues, yValues);
        TabulatedFunction derivative = operator.derive(original);

        // Производная линейной функции должна быть константой ≈ 2
        for (int i = 0; i < derivative.getCount() - 1; i++) {
            assertEquals(2.0, derivative.getY(i), 0.1,
                    "Derivative of linear function should be ≈ 2.0 at index " + i);
        }
    }

    @Test
    void testDeriveFactoryGetterSetter() {
        TabulatedDifferentialOperator operator = new TabulatedDifferentialOperator();

        // Проверяем фабрику по умолчанию
        assertTrue(operator.getFactory() instanceof ArrayTabulatedFunctionFactory,
                "Default factory should be ArrayTabulatedFunctionFactory");

        // Меняем фабрику
        TabulatedFunctionFactory newFactory = new LinkedListTabulatedFunctionFactory();
        operator.setFactory(newFactory);

        assertSame(newFactory, operator.getFactory(),
                "Factory should be changed after setFactory()");
    }

    @Test
    public void deriveSynchronouslyTest1() {
        TabulatedFunction f = new LinkedListTabulatedFunction(new double[] {0.5, 1.0, 1.5, 2.0}, new double[] {0.25, 1.0, 2.25, 4.0});
        TabulatedDifferentialOperator operator = new TabulatedDifferentialOperator(new LinkedListTabulatedFunctionFactory());

        TabulatedFunction derf = operator.derive(f);
        TabulatedFunction derSf = operator.deriveSynchronously(f);

        for (int i = 0; i < 4; ++i)
            assertEquals(derf.getY(i), derSf.getY(i));
    }

    @Test
    public void deriveSynchronouslyTest2() {
        SynchronizedTabulatedFunction sf = new SynchronizedTabulatedFunction(new ArrayTabulatedFunction(new double[] {0.5, 1.0, 1.5, 2.0}, new double[] {0.25, 1.0, 2.25, 4.0}));
        TabulatedDifferentialOperator operator = new TabulatedDifferentialOperator(new LinkedListTabulatedFunctionFactory());

        TabulatedFunction derf = operator.derive(sf);
        TabulatedFunction derSf = operator.deriveSynchronously(sf);

        for (int i = 0; i < 4; ++i)
            assertEquals(derf.getY(i), derSf.getY(i));
    }
}
