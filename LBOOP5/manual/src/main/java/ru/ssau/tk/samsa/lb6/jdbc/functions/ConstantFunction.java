package ru.ssau.tk.samsa.lb6.jdbc.functions;

public class ConstantFunction implements MathFunction {
    private final double CONST_NUM;

    public ConstantFunction(double fNum) {
        CONST_NUM = fNum;
    }

    @Override
    public double apply(double x) {
        return CONST_NUM;
    }

    public double getConst() {
        return CONST_NUM;
    }
}
