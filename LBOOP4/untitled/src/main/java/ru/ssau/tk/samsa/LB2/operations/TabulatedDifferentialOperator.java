package ru.ssau.tk.samsa.LB2.operations;

import ru.ssau.tk.samsa.LB2.functions.Point;
import ru.ssau.tk.samsa.LB2.functions.TabulatedFunction;
import ru.ssau.tk.samsa.LB2.functions.factory.ArrayTabulatedFunctionFactory;
import ru.ssau.tk.samsa.LB2.functions.factory.TabulatedFunctionFactory;
import ru.ssau.tk.samsa.LB2.concurrent.SynchronizedTabulatedFunction;

public class TabulatedDifferentialOperator implements DifferentialOperator<TabulatedFunction> {
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

    @Override
    public TabulatedFunction derive(TabulatedFunction function) {
        return calculateDerivative(function);
    }

    @Override
    public double apply(double x) {
        throw new IllegalStateException("Нужно вызвать derive()");
    }

    private TabulatedFunction calculateDerivative(TabulatedFunction function) {
        Point[] points = TabulatedFunctionOperationService.asPoints(function);
        int count = points.length;
        double[] xValues = new double[count];
        double[] yValues = new double[count];

        for (int i = 0; i < count; i++) {
            xValues[i] = points[i].x;
        }

        for (int i = 0; i < count - 1; i++) {
            yValues[i] = (points[i + 1].y - points[i].y) / (points[i + 1].x - points[i].x);
        }
        yValues[count - 1] = yValues[count - 2];

        return factory.create(xValues, yValues);
    }

    public synchronized TabulatedFunction deriveSynchronously(TabulatedFunction function) {
        if (!(function instanceof SynchronizedTabulatedFunction))
            function = new SynchronizedTabulatedFunction(function);

        return ((SynchronizedTabulatedFunction)function).doSynchronously(f -> derive(f));
    }
}
