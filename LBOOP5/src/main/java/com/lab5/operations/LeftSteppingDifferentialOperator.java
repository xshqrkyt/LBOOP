package com.lab5.operations;

import com.lab5.functions.MathFunction;

public class LeftSteppingDifferentialOperator extends SteppingDifferentialOperator<MathFunction> {
    public LeftSteppingDifferentialOperator(double step) {
        super(step);
    }

    @Override
    public MathFunction derive(MathFunction function) {
        return new MathFunction() {
            @Override
            public double apply(double x) {
                return (function.apply(x) - function.apply(x - step)) / step;
            }
        };
    }

    @Override
    public double apply(double x) {
        throw new UnsupportedOperationException();
    }
}
