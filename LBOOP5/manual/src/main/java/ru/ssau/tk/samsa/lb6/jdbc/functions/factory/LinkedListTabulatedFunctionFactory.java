package ru.ssau.tk.samsa.LB2.lb6.jdbc.functions.factory;

import ru.ssau.tk.samsa.LB2.lb6.jdbc.functions.LinkedListTabulatedFunction;
import ru.ssau.tk.samsa.LB2.lb6.jdbc.functions.TabulatedFunction;

public class LinkedListTabulatedFunctionFactory implements TabulatedFunctionFactory {
    @Override
    public TabulatedFunction create(double[] xValues, double[] yValues) {
        return new LinkedListTabulatedFunction(xValues, yValues);
    }
}
