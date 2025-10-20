package ru.ssau.tk.samsa.lb6.jdbc.functions;

public class IdentityFunction implements MathFunction {
    @Override
    public double apply(double x) {
        return x;
    }
}
