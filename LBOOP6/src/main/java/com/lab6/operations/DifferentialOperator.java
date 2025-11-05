package com.lab6.operations;

import com.lab6.functions.MathFunction;

public interface DifferentialOperator<T> extends MathFunction {
    T derive(T function);
}
