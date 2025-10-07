package ru.ssau.tk.samsa.LB2.operations;

import ru.ssau.tk.samsa.LB2.functions.Point;
import ru.ssau.tk.samsa.LB2.functions.TabulatedFunction;
import ru.ssau.tk.samsa.LB2.factory.ArrayTabulatedFunctionFactory;
import ru.ssau.tk.samsa.LB2.factory.TabulatedFunctionFactory;

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
        this.currentFunction = function;
        this.currentDerivative = calculateDerivative(function);
        return currentDerivative;
    }

    @Override
    public double apply(double x) {
        if (currentDerivative == null) {
            throw new IllegalStateException("Сначала нужно вызвать derive()");
        }
        return currentDerivative.apply(x);
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
}
}