package com.lab6.functions;

import com.lab6.functions.MathFunction;

public class SinFunction implements MathFunction {
    @Override
    public double apply(double x) {
        return Math.sin(x);
    }
}
