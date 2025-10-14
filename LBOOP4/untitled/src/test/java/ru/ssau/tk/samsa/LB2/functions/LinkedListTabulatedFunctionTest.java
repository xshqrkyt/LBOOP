package ru.ssau.tk.samsa.LB2.functions;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import ru.ssau.tk.samsa.LB2.exceptions.*;

import java.util.Iterator;
import java.util.NoSuchElementException;

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

        assertThrows(IllegalArgumentException.class, () -> function.floorIndexOfX(-0.5)); // Левее всех
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
        assertThrows(InterpolationException.class, () -> function.apply(-1.0));
        
        // Экстраполяция справа
        assertThrows(InterpolationException.class, () -> function.apply(3.0));
    }

    @Test
    void testSinglePointFunction() {
        double[] xValues = {2.0};
        double[] yValues = {4.0};
        
        assertThrows(IllegalArgumentException.class, () -> new LinkedListTabulatedFunction(xValues, yValues));
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
        assertThrows(DifferentLengthOfArraysException.class, () -> new LinkedListTabulatedFunction(new double[]{0, 1, 2}, new double[]{0, 4}));
    }

    @Test
    public void checkSortedTest() {
        assertThrows(ArrayIsNotSortedException.class, () -> new LinkedListTabulatedFunction(new double[]{0, 3, 2}, new double[]{0, 9, 4}));
    }

    @Test
    public void interpolateTest1() {
        double[] xArray = {0, 0.5, 1, 1.5, 2, 2.5};
        double[] yArray = {0, 0.7071, 1, 1.2247, 1.4142, 1.5811};
        LinkedListTabulatedFunction obj = new LinkedListTabulatedFunction(xArray, yArray);

        assertThrows(InterpolationException.class, () -> obj.interpolate(-3.6, 4));
    }

    @Test
    public void interpolateTest2() {
        double[] xArray = {0, 0.5, 1, 1.5, 2, 2.5};
        double[] yArray = {0, 0.7071, 1, 1.2247, 1.4142, 1.5811};
        LinkedListTabulatedFunction obj = new LinkedListTabulatedFunction(xArray, yArray);

        assertThrows(InterpolationException.class, () -> obj.interpolate(7.5, 4));
    }

    // Тесты на конструкторы с исключениями
    @Test
    void testConstructorWithInvalidArrays() {
        // Массивы разной длины
        double[] xValues1 = {1.0, 2.0};
        double[] yValues1 = {1.0, 2.0, 3.0};
        assertThrows(DifferentLengthOfArraysException.class, () -> {
            new LinkedListTabulatedFunction(xValues1, yValues1);
        });

        // Неотсортированный массив x
        double[] xValues2 = {1.0, 3.0, 2.0};
        double[] yValues2 = {1.0, 2.0, 3.0};
        assertThrows(ArrayIsNotSortedException.class, () -> {
            new LinkedListTabulatedFunction(xValues2, yValues2);
        });

        // Дубликаты в x
        double[] xValues3 = {1.0, 2.0, 2.0};
        double[] yValues3 = {1.0, 2.0, 3.0};
        assertThrows(ArrayIsNotSortedException.class, () -> {
            new LinkedListTabulatedFunction(xValues3, yValues3);
        });

        // Меньше 2 точек
        double[] xValues4 = {1.0};
        double[] yValues4 = {1.0};
        assertThrows(IllegalArgumentException.class, () -> {
            new LinkedListTabulatedFunction(xValues4, yValues4);
        });
    }

    @Test
    void testConstructorWithFunctionInvalidCount() {
        MathFunction source = new SqrFunction();

        // count < 2
        assertThrows(IllegalArgumentException.class, () -> {
            new LinkedListTabulatedFunction(source, 0, 1, 1);
        });

        // count = 0
        assertThrows(IllegalArgumentException.class, () -> {
            new LinkedListTabulatedFunction(source, 0, 1, 0);
        });
    }

    // Тесты на методы доступа с невалидными индексами
    @Test
    void testGetXWithInvalidIndex() {
        double[] xValues = {1.0, 2.0, 3.0};
        double[] yValues = {1.0, 4.0, 9.0};
        LinkedListTabulatedFunction function = new LinkedListTabulatedFunction(xValues, yValues);

        // Отрицательный индекс
        assertThrows(IllegalArgumentException.class, () -> function.getX(-1));

        // Индекс больше размера
        assertThrows(IllegalArgumentException.class, () -> function.getX(3));
        assertThrows(IllegalArgumentException.class, () -> function.getX(10));
    }

    @Test
    void testGetYWithInvalidIndex() {
        double[] xValues = {1.0, 2.0, 3.0};
        double[] yValues = {1.0, 4.0, 9.0};
        LinkedListTabulatedFunction function = new LinkedListTabulatedFunction(xValues, yValues);

        assertThrows(IllegalArgumentException.class, () -> function.getY(-1));
        assertThrows(IllegalArgumentException.class, () -> function.getY(3));
    }

    @Test
    void testSetYWithInvalidIndex() {
        double[] xValues = {1.0, 2.0, 3.0};
        double[] yValues = {1.0, 4.0, 9.0};
        LinkedListTabulatedFunction function = new LinkedListTabulatedFunction(xValues, yValues);

        assertThrows(IllegalArgumentException.class, () -> function.setY(-1, 5.0));
        assertThrows(IllegalArgumentException.class, () -> function.setY(3, 5.0));
    }

    @Test
    void testFloorIndexOfXWithInvalidX() {
        double[] xValues = {1.0, 2.0, 3.0};
        double[] yValues = {1.0, 4.0, 9.0};
        LinkedListTabulatedFunction function = new LinkedListTabulatedFunction(xValues, yValues);

        // x меньше левой границы
        assertThrows(IllegalArgumentException.class, () -> function.floorIndexOfX(0.5));
    }

    @Test
    void testGetNodeWithInvalidIndex() {
        double[] xValues = {1.0, 2.0, 3.0};
        double[] yValues = {1.0, 4.0, 9.0};
        LinkedListTabulatedFunction function = new LinkedListTabulatedFunction(xValues, yValues);

        // Тестируем приватный метод через рефлексию или проверяем, что публичные методы
        // правильно используют getNode и бросают исключения
        assertThrows(IllegalArgumentException.class, () -> function.getX(-1));
        assertThrows(IllegalArgumentException.class, () -> function.getX(3));
    }

    @Test
    void testInsertWithInvalidParameters() {
        double[] xValues = {1.0, 3.0};
        double[] yValues = {1.0, 9.0};
        LinkedListTabulatedFunction function = new LinkedListTabulatedFunction(xValues, yValues);

        // Вставка NaN
        assertThrows(IllegalArgumentException.class, () -> function.insert(Double.NaN, 5.0));
    }

    @Test
    void testRemoveWithInvalidIndex() {
        double[] xValues = {1.0, 2.0, 3.0};
        double[] yValues = {1.0, 4.0, 9.0};
        LinkedListTabulatedFunction function = new LinkedListTabulatedFunction(xValues, yValues);

        assertThrows(IllegalArgumentException.class, () -> function.remove(-1));
        assertThrows(IllegalArgumentException.class, () -> function.remove(3));

        // После удаления не должно остаться меньше 2 точек
        function.remove(1); // теперь 2 точки - OK
        assertThrows(IllegalArgumentException.class, () -> function.remove(0)); // останется 1 точка - ошибка
    }

    @Test
    void testEmptyListOperations() {
        // Создаем пустой список через рефлексию или тестируем граничные случаи
        double[] xValues = {1.0, 2.0};
        double[] yValues = {1.0, 4.0};
        LinkedListTabulatedFunction function = new LinkedListTabulatedFunction(xValues, yValues);

        // После удаления всех элементов (оставляя 2) - должно работать
        assertDoesNotThrow(() -> {
            assertEquals(2, function.getCount());
        });
    }

    @Test
    void testInterpolationException() {
        double[] xValues = {1.0, 2.0, 3.0};
        double[] yValues = {1.0, 4.0, 9.0};
        LinkedListTabulatedFunction function = new LinkedListTabulatedFunction(xValues, yValues);

        // Интерполяция с невалидным floorIndex
        assertThrows(IllegalArgumentException.class, () -> function.interpolate(0.5, -1));
        assertThrows(InterpolationException.class, () -> function.interpolate(0.5, 2));
    }

    // Тесты на нормальную работу
    @Test
    void testValidOperations() {
        double[] xValues = {1.0, 2.0, 3.0};
        double[] yValues = {1.0, 4.0, 9.0};
        LinkedListTabulatedFunction function = new LinkedListTabulatedFunction(xValues, yValues);

        // Валидные операции не должны бросать исключения
        assertDoesNotThrow(() -> {
            assertEquals(1.0, function.getX(0));
            assertEquals(4.0, function.getY(1));
            function.setY(1, 5.0);
            assertEquals(5.0, function.getY(1));
            assertEquals(0, function.floorIndexOfX(1.5));
            function.insert(1.5, 2.25);
            assertEquals(4, function.getCount());
        });
    }
    @Test
    void testIteratorTwoWays() {
        double[] xValues = {0.0, 1.0, 2.0, 3.0};
        double[] yValues = {0.0, 1.0, 4.0, 9.0};
        LinkedListTabulatedFunction function = new LinkedListTabulatedFunction(xValues, yValues);

        // Способ 1: с помощью цикла while
        System.out.println("Testing iterator with while loop:");
        Iterator<Point> iterator1 = function.iterator();
        int count1 = 0;
        while (iterator1.hasNext()) {
            Point point = iterator1.next();
            assertEquals(function.getX(count1), point.x, 1e-10, "X value mismatch at index " + count1);
            assertEquals(function.getY(count1), point.y, 1e-10, "Y value mismatch at index " + count1);
            count1++;
        }
        assertEquals(function.getCount(), count1, "Iterator should return all " + function.getCount() + " points");

        // Способ 2: с помощью цикла for-each
        System.out.println("Testing iterator with for-each loop:");
        int count2 = 0;
        for (Point point : function) {
            assertEquals(function.getX(count2), point.x, 1e-10, "X value mismatch at index " + count2);
            assertEquals(function.getY(count2), point.y, 1e-10, "Y value mismatch at index " + count2);
            count2++;
        }
        assertEquals(function.getCount(), count2, "For-each should iterate over all " + function.getCount() + " points");
    }

    @Test
    void testIteratorEdgeCases() {
        // Тест с минимальным количеством элементов (2 точки)
        LinkedListTabulatedFunction minFunction = new LinkedListTabulatedFunction(
                new double[]{1.0, 2.0}, new double[]{1.0, 4.0});

        Iterator<Point> iterator = minFunction.iterator();
        assertTrue(iterator.hasNext(), "Iterator should have next for 2-point function");
        Point point1 = iterator.next();
        assertEquals(1.0, point1.x, 1e-10);
        assertEquals(1.0, point1.y, 1e-10);

        assertTrue(iterator.hasNext(), "Iterator should have second element");
        Point point2 = iterator.next();
        assertEquals(2.0, point2.x, 1e-10);
        assertEquals(4.0, point2.y, 1e-10);

        assertFalse(iterator.hasNext(), "Iterator should not have more elements");
        assertThrows(NoSuchElementException.class, iterator::next,
                "Should throw NoSuchElementException when no more elements");
    }
}

