package ru.ssau.tk.samsa.LB2.functions.factory;

import ru.ssau.tk.samsa.LB2.functions.ArrayTabulatedFunction;
import ru.ssau.tk.samsa.LB2.functions.TabulatedFunction;

public class ArrayTabulatedFunctionFactory implements TabulatedFunctionFactory {
    @Override
    public TabulatedFunction create(double[] xValues, double[] yValues) {
        return new ArrayTabulatedFunction(xValues, yValues);
    }
}
