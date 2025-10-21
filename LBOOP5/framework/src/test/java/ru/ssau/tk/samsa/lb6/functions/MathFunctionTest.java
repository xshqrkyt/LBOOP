package ru.ssau.tk.samsa.lb6.functions;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MathFunctionTest {
    @Test
    public void applyTest1() {
        MathFunction f = new IdentityFunction();
        MathFunction g = new SqrFunction();
        MathFunction h = new ConstantFunction(9);

        assertEquals(9, f.andThen(g).andThen(h).apply(2));
    }

    @Test
    public void applyTest2() {
        MathFunction f = new ZeroFunction();
        MathFunction g = new SqrFunction();
        MathFunction h = new CompositeFunction(f, g);

        assertEquals(0, g.andThen(h).andThen(f).apply(7));
    }

    @Test
    public void applyTest3() {
        MathFunction f = x -> Math.pow(x, 3);
        MathFunction g = new MethodNewtonFunction(f);
        MathFunction h = new IdentityFunction();

        assertEquals(0, h.andThen(f).andThen(g).apply(4), 1e-9);
    }
}