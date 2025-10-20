package ru.ssau.tk.samsa.LB2.lb6.jdbc.operations;

import ru.ssau.tk.samsa.LB2.lb6.jdbc.functions.MathFunction;

public class RightSteppingDifferentialOperator extends SteppingDifferentialOperator<MathFunction> {
    public RightSteppingDifferentialOperator(double step) {
        super(step);
    }

    @Override
    public MathFunction derive(MathFunction function) {
        return new MathFunction() {
            @Override
            public double apply(double x) {
                return (function.apply(x + step) - function.apply(x)) / step;
            }
        };
    }

    @Override
    public double apply(double x) {
        throw new UnsupportedOperationException();
    }
}
