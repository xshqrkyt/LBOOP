package ru.ssau.tk.samsa.LB2.functions;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class IdentityFunctionTest {

    @Test
    void testApply() {
        IdentityFunction function = new IdentityFunction();

        assertEquals(0.0, function.apply(0.0), 1e-10);
        assertEquals(1.0, function.apply(1.0), 1e-10);
        assertEquals(-1.0, function.apply(-1.0), 1e-10);
        assertEquals(5.5, function.apply(5.5), 1e-10);
        assertEquals(-3.14, function.apply(-3.14), 1e-10);
    }

    @Test
    void testAndThen() {
        IdentityFunction id = new IdentityFunction();
        SqrFunction sqr = new SqrFunction();

        MathFunction composite = id.andThen(sqr);
        assertEquals(4.0, composite.apply(2.0), 1e-10);
        assertEquals(9.0, composite.apply(3.0), 1e-10);
        assertEquals(0.0, composite.apply(0.0), 1e-10);
    }
}