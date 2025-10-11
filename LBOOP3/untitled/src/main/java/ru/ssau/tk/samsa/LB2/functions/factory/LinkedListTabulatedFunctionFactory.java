package ru.ssau.tk.samsa.LB2.factory;

import ru.ssau.tk.samsa.LB2.functions.LinkedListTabulatedFunction;
import ru.ssau.tk.samsa.LB2.functions.TabulatedFunction;

public class LinkedListTabulatedFunctionFactory implements TabulatedFunctionFactory {
    @Override
    public TabulatedFunction create(double[] xValues, double[] yValues) {
        return new LinkedListTabulatedFunction(xValues, yValues);
    }
}
