package com.lab5.functions;

public class SinFunction implements MathFunction {
    @Override
    public double apply(double x) {
        return Math.sin(x);
    }
}
