package ru.ssau.tk.samsa.lb6.operations;

import ru.ssau.tk.samsa.lb6.functions.MathFunction;

public interface DifferentialOperator<T> extends MathFunction {
    T derive(T function);
}

