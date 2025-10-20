package ru.ssau.tk.samsa.lb6.functions.factory;

import ru.ssau.tk.samsa.lb6.functions.TabulatedFunction;

public interface TabulatedFunctionFactory {
    TabulatedFunction create(double[] xValues, double[] yValues);
}

