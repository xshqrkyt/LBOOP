package ru.ssau.tk.samsa.LB2.lb6.jdbc.functions;

public class SinFunction implements MathFunction {
    @Override
    public double apply(double x) {
        return Math.sin(x);
    }
}
