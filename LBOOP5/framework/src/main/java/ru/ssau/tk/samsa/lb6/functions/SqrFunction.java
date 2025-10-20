package ru.ssau.tk.samsa.LB2.lb6.jdbc.functions;

import static java.lang.Math.pow;

public class SqrFunction implements MathFunction {
    @Override
    public double apply(double x) {
        return pow(x, 2);
    }
}
