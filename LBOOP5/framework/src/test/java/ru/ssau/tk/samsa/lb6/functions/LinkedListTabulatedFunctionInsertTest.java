package ru.ssau.tk.samsa.lb6.functions;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class LinkedListTabulatedFunctionInsertTest {
    @Test
    void testInsertAtBeginning() {
        // Создаем список с 3 элементами: [1,2,3]
        LinkedListTabulatedFunction list = new LinkedListTabulatedFunction(
                new double[]{1.0, 2.0, 3.0},
                new double[]{10.0, 20.0, 30.0}
        );

        // Вставляем в начало: [0,1,2,3]
        list.insert(0.0, 0.0);

        // Проверяем, что стало 4 элемента
        assertEquals(4, list.getCount());
        assertEquals(0.0, list.getX(0), 1e-10);
        assertEquals(1.0, list.getX(1), 1e-10);
    }

    @Test
    void testInsertAtEnd() {
        // Создаем список с 3 элементами: [1,2,3]
        LinkedListTabulatedFunction list = new LinkedListTabulatedFunction(
                new double[]{1.0, 2.0, 3.0},
                new double[]{10.0, 20.0, 30.0}
        );

        // Вставляем в конец: [1,2,3,4]
        list.insert(4.0, 40.0);

        // Проверяем, что стало 4 элемента
        assertEquals(4, list.getCount());
        assertEquals(3.0, list.getX(2), 1e-10);
        assertEquals(4.0, list.getX(3), 1e-10);
    }

    @Test
    void testInsertInMiddle() {
        double[] xValues = {0.0, 1.0, 3.0};
        double[] yValues = {0.0, 1.0, 9.0};
        LinkedListTabulatedFunction function = new LinkedListTabulatedFunction(xValues, yValues);

        function.insert(2.0, 4.0);

        assertEquals(4, function.getCount());
        assertEquals(2.0, function.getX(2), 1e-10);
        assertEquals(4.0, function.getY(2), 1e-10);
        assertEquals(3.0, function.getX(3), 1e-10);
    }

    @Test
    void testUpdateExisting() {
        double[] xValues = {0.0, 1.0, 2.0};
        double[] yValues = {0.0, 1.0, 4.0};
        LinkedListTabulatedFunction function = new LinkedListTabulatedFunction(xValues, yValues);

        function.insert(1.0, 5.0);

        assertEquals(3, function.getCount()); // Количество не должно измениться
        assertEquals(5.0, function.getY(1), 1e-10);
    }
}
