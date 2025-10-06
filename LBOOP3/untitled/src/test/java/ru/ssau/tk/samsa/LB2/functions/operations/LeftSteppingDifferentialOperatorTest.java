package ru.ssau.tk.samsa.LB2.operations;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

import ru.ssau.tk.samsa.LB2.functions.*;

class LeftSteppingDifferentialOperatorTest {
    private static final double EPS = 1e-1;
    private static final double STEP = 1e-3;

    private final MathFunction sqrFunction = new SqrFunction(); // f(x) = x^2

    @Test
    void deriveTest1() {
        LeftSteppingDifferentialOperator obj = new LeftSteppingDifferentialOperator(STEP);
        MathFunction derivative = obj.derive(sqrFunction);

        double x = 2.0;
        assertEquals(2 * x, derivative.apply(x), EPS);
    }

    @Test
    void deriveTest2() {
        LeftSteppingDifferentialOperator obj = new LeftSteppingDifferentialOperator(-0.01);
        MathFunction derivative = obj.derive(sqrFunction);

        double x = 2.0;
        assertEquals(2 * x, derivative.apply(x), EPS);
    }
}
