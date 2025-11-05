package com.lab6.functions;

import com.lab6.functions.MathFunction;

import static java.lang.Math.pow;

public class SqrFunction implements MathFunction {
    @Override
    public double apply(double x) {
        return pow(x, 2);
    }
}
