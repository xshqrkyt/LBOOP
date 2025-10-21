package ru.ssau.tk.samsa.lb6.jdbc.operations;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import ru.ssau.tk.samsa.lb6.jdbc.functions.*;

class RightSteppingDifferentialOperatorTest {
    private static final double EPS = 1e-1;
    private static final double STEP = 1e-3;
    private final MathFunction sqrFunction = new SqrFunction(); // f(x) = x^2

    @Test
    void deriveTest1() {
        RightSteppingDifferentialOperator obj = new RightSteppingDifferentialOperator(STEP);
        MathFunction derivative = obj.derive(sqrFunction);

        double x = 2.0;
        assertEquals(2 * x, derivative.apply(x), EPS);
    }

    @Test
    void deriveTest2() {
        assertThrows(IllegalArgumentException.class, () -> new RightSteppingDifferentialOperator(-0.01));
    }

    @Test
    void applyTest() {
        RightSteppingDifferentialOperator obj = new RightSteppingDifferentialOperator(STEP);
        assertThrows(UnsupportedOperationException.class, () -> obj.apply(5));
    }

    @Test
    void getTest() {
        SteppingDifferentialOperator obj = new RightSteppingDifferentialOperator(STEP);
        assertEquals(STEP, obj.get());
    }

    @Test
    void setTest() {
        SteppingDifferentialOperator obj = new RightSteppingDifferentialOperator(STEP);
        obj.set(EPS);
        assertEquals(EPS, obj.get());
    }
}