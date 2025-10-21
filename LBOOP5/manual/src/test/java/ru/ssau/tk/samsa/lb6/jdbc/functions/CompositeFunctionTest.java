package ru.ssau.tk.samsa.lb6.jdbc.functions;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class CompositeFunctionTest {

    @Test
    void testApply() {
        IdentityFunction first = new IdentityFunction();
        SqrFunction second = new SqrFunction();
        CompositeFunction composite = new CompositeFunction(first, second);

        // f(g(x)) где f(x)=x², g(x)=x
        assertEquals(4.0, composite.apply(2.0), 1e-10);
        assertEquals(9.0, composite.apply(3.0), 1e-10);
        assertEquals(0.0, composite.apply(0.0), 1e-10);
    }

    @Test
    void testComplexComposition() {
        SinFunction sin = new SinFunction();
        SqrFunction sqr = new SqrFunction();
        CompositeFunction composite1 = new CompositeFunction(sin, sqr); // sin²(x)
        CompositeFunction composite2 = new CompositeFunction(sqr, sin); // sin(x²)

        assertEquals(Math.pow(Math.sin(2.0), 2), composite1.apply(2.0), 1e-10);
        assertEquals(Math.sin(4.0), composite2.apply(2.0), 1e-10);
    }

    @Test
    void testNestedComposition() {
        IdentityFunction id = new IdentityFunction();
        SqrFunction sqr = new SqrFunction();
        SinFunction sin = new SinFunction();

        // h(x) = sin(sqr(id(x)))
        CompositeFunction inner = new CompositeFunction(id, sqr);
        CompositeFunction outer = new CompositeFunction(inner, sin);

        assertEquals(Math.sin(4.0), outer.apply(2.0), 1e-10);
        assertEquals(Math.sin(9.0), outer.apply(3.0), 1e-10);
    }

    @Test
    void testWithConstantFunction() {
        ConstantFunction constant = new ConstantFunction(5.0);
        SqrFunction sqr = new SqrFunction();
        CompositeFunction composite = new CompositeFunction(constant, sqr);

        // Всегда возвращает квадрат константы
        assertEquals(25.0, composite.apply(0.0), 1e-10);
        assertEquals(25.0, composite.apply(10.0), 1e-10);
        assertEquals(25.0, composite.apply(-5.0), 1e-10);
    }
}