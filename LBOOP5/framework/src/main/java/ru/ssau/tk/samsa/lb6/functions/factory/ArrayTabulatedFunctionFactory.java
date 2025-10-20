package ru.ssau.tk.samsa.lb6.functions.factory;

import ru.ssau.tk.samsa.lb6.functions.ArrayTabulatedFunction;
import ru.ssau.tk.samsa.lb6.functions.TabulatedFunction;

public class ArrayTabulatedFunctionFactory implements TabulatedFunctionFactory {
    @Override
    public TabulatedFunction create(double[] xValues, double[] yValues) {
        return new ArrayTabulatedFunction(xValues, yValues);
    }
}

