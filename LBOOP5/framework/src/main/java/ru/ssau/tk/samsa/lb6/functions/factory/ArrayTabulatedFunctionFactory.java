package ru.ssau.tk.samsa.LB2.lb6.jdbc.functions.factory;

import ru.ssau.tk.samsa.LB2.lb6.jdbc.functions.ArrayTabulatedFunction;
import ru.ssau.tk.samsa.LB2.lb6.jdbc.functions.TabulatedFunction;

public class ArrayTabulatedFunctionFactory implements TabulatedFunctionFactory {
    @Override
    public TabulatedFunction create(double[] xValues, double[] yValues) {
        return new ArrayTabulatedFunction(xValues, yValues);
    }
}
