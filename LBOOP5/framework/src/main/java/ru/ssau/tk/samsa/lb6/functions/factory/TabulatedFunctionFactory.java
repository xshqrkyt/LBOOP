package ru.ssau.tk.samsa.LB2.lb6.jdbc.functions.factory;

import ru.ssau.tk.samsa.LB2.lb6.jdbc.functions.TabulatedFunction;

public interface TabulatedFunctionFactory {
    TabulatedFunction create(double[] xValues, double[] yValues);
}
