package ru.ssau.tk.samsa.LB2.operations;

import ru.ssau.tk.samsa.LB2.functions.MathFunction;

public interface DifferentialOperator<T> extends MathFunction {
    T derive(T function);
}