package ru.ssau.tk.samsa.LB2.functions;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class MethodNewtonFunctionTest {
    private static final double EPS = 1e-9;

    @Test
    public void applyTest1() {
        MathFunction f = new SqrFunction();
        MethodNewtonFunction g = new MethodNewtonFunction(f);

        assertEquals(0, g.apply(5), EPS);
    }

    @Test
    public void applyTest2() {
        MathFunction f = x -> (x - 2) * (x - 4.5) * (x - 1);
        MethodNewtonFunction g = new MethodNewtonFunction(f);

        assertEquals(4.5, g.apply(7), EPS);
    }

    @Test
    public void applyTest3() {
        MathFunction f = x -> Math.pow(2, x) - 5;
        MathFunction g = new MethodNewtonFunction(f);

        assertEquals(2.321928094887362, g.apply(10), EPS);
    }
}