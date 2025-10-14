package ru.ssau.tk.samsa.LB2.functions;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class ConstantFunctionTest {

    @Test
    void testApply() {
        ConstantFunction function = new ConstantFunction(5.0);

        assertEquals(5.0, function.apply(0.0), 1e-10);
        assertEquals(5.0, function.apply(1.0), 1e-10);
        assertEquals(5.0, function.apply(-1.0), 1e-10);
        assertEquals(5.0, function.apply(100.0), 1e-10);
    }

    @Test
    void testGetConstant() {
        ConstantFunction function = new ConstantFunction(3.14);
        assertEquals(3.14, function.getConst(), 1e-10);
    }
}