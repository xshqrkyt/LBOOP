package ru.ssau.tk.samsa.lb6.jdbc.functions;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class DeBoorFunctionTest {

    @Test
    void testLinearDeBoorFunction() {
        // Линейный B-сплайн (степень 1)
        double[] knots = {0, 0, 1, 2, 2};
        double[] controlPoints = {0, 1, 0};
        DeBoorFunction function = new DeBoorFunction(knots, controlPoints, 1);

        assertEquals(0.0, function.apply(0.0), 1e-10);
        assertEquals(1.0, function.apply(1.0), 1e-10);
        assertEquals(0.0, function.apply(2.0), 1e-10);
        assertEquals(0.5, function.apply(0.5), 1e-10);
        assertEquals(0.5, function.apply(1.5), 1e-10);
    }

    @Test
    void testOutOfDomain() {
        double[] knots = {0, 0, 1, 2, 2};
        double[] controlPoints = {0, 1, 0};
        DeBoorFunction function = new DeBoorFunction(knots, controlPoints, 1);

        // За пределами области определения должен возвращать 0
        assertEquals(0.0, function.apply(-1.0), 1e-10);
        assertEquals(0.0, function.apply(3.0), 1e-10);
        assertEquals(0.0, function.apply(-10.0), 1e-10);
        assertEquals(0.0, function.apply(10.0), 1e-10);
    }

    @Test
    void testQuadraticDeBoorFunction() {
        // Квадратичный B-сплайн (степень 2)
        double[] knots = {0, 0, 0, 1, 2, 2, 2};
        double[] controlPoints = {0, 1, 0.5, 0};
        DeBoorFunction function = new DeBoorFunction(knots, controlPoints, 2);

        // Проверка граничных условий
        assertEquals(0.0, function.apply(0.0), 1e-10);
        assertEquals(0.0, function.apply(2.0), 1e-10);

        // Проверка гладкости
        double result1 = function.apply(0.5);
        double result2 = function.apply(1.0);
        double result3 = function.apply(1.5);

        assertTrue(result1 > 0 && result1 < 1.0);
        assertTrue(result2 > 0 && result2 < 1.0);
        assertTrue(result3 > 0 && result3 < 0.5);
    }

    @Test
    void testCubicDeBoorFunction() {
        // Кубический B-сплайн (степень 3)
        double[] knots = {0, 0, 0, 0, 1, 2, 3, 3, 3, 3};
        double[] controlPoints = {0, 1, 1, 0, -1, 0};
        DeBoorFunction function = new DeBoorFunction(knots, controlPoints, 3);

        // Проверка граничных условий
        assertEquals(0.0, function.apply(0.0), 1e-10);
        assertEquals(0.0, function.apply(3.0), 1e-10);

        // Проверка, что функция возвращает значения (не обязательно точные)
        assertTrue(Math.abs(function.apply(0.5)) > 0);
        assertTrue(Math.abs(function.apply(1.5)) > 0);
        assertTrue(Math.abs(function.apply(2.5)) > 0);
    }

    @Test
    void testInvalidConstructor() {
        double[] knots = {0, 1, 2}; // Слишком мало узлов
        double[] controlPoints = {0, 1, 2};

        assertThrows(IllegalArgumentException.class, () -> {
            new DeBoorFunction(knots, controlPoints, 2);
        });
    }

    @Test
    void testNonDecreasingKnots() {
        double[] invalidKnots = {2, 1, 0}; // Убывающие узлы
        double[] controlPoints = {0, 1};

        assertThrows(IllegalArgumentException.class, () -> {
            new DeBoorFunction(invalidKnots, controlPoints, 1);
        });
    }

    @Test
    void testAsMathFunction() {
        double[] knots = {0, 0, 1, 2, 2};
        double[] controlPoints = {0, 2, 0};
        DeBoorFunction deBoor = new DeBoorFunction(knots, controlPoints, 1);

        // Проверка реализации интерфейса MathFunction
        MathFunction function = deBoor;
        assertEquals(1.0, function.apply(0.5), 1e-10);

        // Композиция с другими функциями
        MathFunction composite = deBoor.andThen(new SqrFunction());
        assertEquals(4.0, composite.apply(1.0), 1e-10); // 2^2 = 4
    }

    @Test
    void testEdgeCases() {
        // Тестирование особых случаев
        double[] knots = {0, 0, 0, 1, 1, 1}; // Все узлы на границах
        double[] controlPoints = {1, 2, 3};
        DeBoorFunction function = new DeBoorFunction(knots, controlPoints, 2);

        // В этом случае функция должна вести себя корректно
        assertEquals(1.0, function.apply(0.0), 1e-10);
        assertEquals(3.0, function.apply(1.0), 1e-10);
    }

    @Test
    public void getKnotsTest() {
        double[] knots = {0, 0, 1, 2, 2};
        double[] controlPoints = {0, 2, 0};
        DeBoorFunction deBoor = new DeBoorFunction(knots, controlPoints, 1);

        double[] getKnots = deBoor.getKnots();
        for (int i = 0; i < 5; ++i)
            assertEquals(knots[i], getKnots[i]);
    }

    @Test
    public void getControlPointsTest() {
        double[] knots = {0, 0, 1, 2, 2};
        double[] controlPoints = {0, 2, 0};
        DeBoorFunction deBoor = new DeBoorFunction(knots, controlPoints, 1);

        double[] getControlPoints = deBoor.getControlPoints();
        for (int i = 0; i < 3; ++i)
            assertEquals(controlPoints[i], getControlPoints[i]);
    }

    @Test
    public void getDegreeTest() {
        double[] knots = {0, 0, 1, 2, 2};
        double[] controlPoints = {0, 2, 0};
        DeBoorFunction deBoor = new DeBoorFunction(knots, controlPoints, 1);

        assertEquals(1, deBoor.getDegree());
    }
}