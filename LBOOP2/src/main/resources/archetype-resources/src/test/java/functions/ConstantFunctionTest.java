package functions;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

class ConstantFunctionTest {
    @Test
    public void applyTest1() {
        ConstantFunction f = new ConstantFunction(0.75);
        assertEquals(0.75, f.apply(1.6));
    }

    @Test
    public void applyTest2() {
        ConstantFunction f = new ConstantFunction(-1);
        assertEquals(-1, f.apply(0));
    }
}