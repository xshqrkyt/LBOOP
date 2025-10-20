package ru.ssau.tk.samsa.lb6.jdbc.functions.factory;

import ru.ssau.tk.samsa.lb6.jdbc.functions.TabulatedFunction;

public interface TabulatedFunctionFactory {
    TabulatedFunction create(double[] xValues, double[] yValues);
}
