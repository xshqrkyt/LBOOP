package functions;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class LinkedListTabulatedFunctionInsertTest {

    @Test
    void testInsertAtBeginning() {
        double[] xValues = {1.0, 2.0, 3.0};
        double[] yValues = {1.0, 4.0, 9.0};
        LinkedListTabulatedFunction function = new LinkedListTabulatedFunction(xValues, yValues);

        function.insert(0.0, 0.0);

        assertEquals(4, function.getCount());
        assertEquals(0.0, function.leftBound(), 1e-10);
        assertEquals(0.0, function.getY(0), 1e-10);
        assertEquals(1.0, function.getY(1), 1e-10);
    }

    @Test
    void testInsertAtEnd() {
        double[] xValues = {0.0, 1.0, 2.0};
        double[] yValues = {0.0, 1.0, 4.0};
        LinkedListTabulatedFunction function = new LinkedListTabulatedFunction(xValues, yValues);

        function.insert(3.0, 9.0);

        assertEquals(4, function.getCount());
        assertEquals(3.0, function.rightBound(), 1e-10);
        assertEquals(9.0, function.getY(3), 1e-10);
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

    @Test
    void testInsertIntoEmpty() {
        LinkedListTabulatedFunction function = new LinkedListTabulatedFunction(new double[0], new double[0]);
        function.insert(1.0, 2.0);

        assertEquals(1, function.getCount());
        assertEquals(1.0, function.leftBound(), 1e-10);
        assertEquals(2.0, function.getY(0), 1e-10);
    }
}