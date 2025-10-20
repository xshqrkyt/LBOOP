package ru.ssau.tk.samsa.LB2.lb6.jdbc.operations;

import ru.ssau.tk.samsa.LB2.lb6.jdbc.functions.MathFunction;

public interface DifferentialOperator<T> extends MathFunction {
    T derive(T function);
}
