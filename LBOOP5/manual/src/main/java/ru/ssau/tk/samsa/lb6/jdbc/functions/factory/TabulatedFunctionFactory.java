package ru.ssau.tk.samsa.LB2.functions.factory;

import ru.ssau.tk.samsa.LB2.functions.TabulatedFunction;

public interface TabulatedFunctionFactory {
    TabulatedFunction create(double[] xValues, double[] yValues);
}
