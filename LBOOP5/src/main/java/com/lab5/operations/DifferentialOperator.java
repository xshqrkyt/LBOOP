package com.lab5.operations;

import com.lab5.functions.MathFunction;

public interface DifferentialOperator<T> extends MathFunction {
    T derive(T function);
}
