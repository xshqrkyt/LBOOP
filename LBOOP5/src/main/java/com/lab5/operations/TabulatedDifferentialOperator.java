package com.lab5.operations;

import com.lab5.concurrent.SynchronizedTabulatedFunction;
import com.lab5.functions.TabulatedFunction;
import com.lab5.functions.factory.TabulatedFunctionFactory;
import com.lab5.functions.factory.ArrayTabulatedFunctionFactory;

/**
 * Класс для вычисления производной табулированной функции.
 */
public class TabulatedDifferentialOperator {
    private TabulatedFunctionFactory factory;

    public TabulatedDifferentialOperator() {
        this.factory = new ArrayTabulatedFunctionFactory();
    }

    public TabulatedDifferentialOperator(TabulatedFunctionFactory factory) {
        this.factory = factory;
    }

    public TabulatedFunctionFactory getFactory() {
        return factory;
    }

    public void setFactory(TabulatedFunctionFactory factory) {
        this.factory = factory;
    }

    /**
     * Вычисление производной табулированной функции.
     * Для последней точки используется левая разность.
     */
    public TabulatedFunction derive(TabulatedFunction function) {
        int n = function.getCount();
        double[] xValues = new double[n];
        double[] yValues = new double[n];

        for (int i = 0; i < n; i++) {
            xValues[i] = function.getX(i);
        }

        for (int i = 0; i < n - 1; i++) {
            double x0 = function.getX(i);
            double x1 = function.getX(i + 1);
            double y0 = function.getY(i);
            double y1 = function.getY(i + 1);
            yValues[i] = (y1 - y0) / (x1 - x0);
        }

        // Левая разность для последней точки
        yValues[n - 1] = yValues[n - 2];

        return factory.create(xValues, yValues);
    }

    /**
     * Потокобезопасное вычисление производной.
     * Если функция уже синхронизирована — не оборачиваем повторно.
     */
    public TabulatedFunction deriveSynchronously(TabulatedFunction function) {
        SynchronizedTabulatedFunction syncFunction;

        if (function instanceof SynchronizedTabulatedFunction) {
            syncFunction = (SynchronizedTabulatedFunction) function;
        } else {
            syncFunction = new SynchronizedTabulatedFunction(function);
        }

        return syncFunction.doSynchronously(f -> derive(f));
    }
}
