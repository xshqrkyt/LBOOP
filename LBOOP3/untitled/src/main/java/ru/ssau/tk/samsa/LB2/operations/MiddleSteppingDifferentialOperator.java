package ru.ssau.tk.samsa.LB2.operations;

import ru.ssau.tk.samsa.LB2.functions.MathFunction;

public class MiddleSteppingDifferentialOperator extends SteppingDifferentialOperator<MathFunction> {
    public MiddleSteppingDifferentialOperator(double step) {
        super(step);
    }

    @Override
    public MathFunction derive(MathFunction function) {
        return new MathFunction() {
            @Override
            public double apply(double x) {
                return (function.apply(x + step) - function.apply(x - step)) / (2 * step);
            }
        };
    }

    @Override
    public double apply(double x) {
        return 0;
    }
}
