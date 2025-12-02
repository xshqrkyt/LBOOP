package com.lab7.operations;

import com.lab7.functions.MathFunction;

public interface DifferentialOperator<T> extends MathFunction {
    T derive(T function);
}
