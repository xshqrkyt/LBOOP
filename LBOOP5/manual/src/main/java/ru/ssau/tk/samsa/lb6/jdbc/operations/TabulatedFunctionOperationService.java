package ru.ssau.tk.samsa.lb6.jdbc.operations;

import ru.ssau.tk.samsa.lb6.jdbc.functions.*;
import ru.ssau.tk.samsa.lb6.jdbc.functions.factory.*;
import ru.ssau.tk.samsa.lb6.jdbc.exceptions.InconsistentFunctionsException;

public class TabulatedFunctionOperationService {
    TabulatedFunctionFactory factory;

    public TabulatedFunctionOperationService(TabulatedFunctionFactory factory) {
        this.factory = factory;
    }

    public TabulatedFunctionOperationService() {
        factory = new ArrayTabulatedFunctionFactory();
    }

    public static Point[] asPoints(TabulatedFunction tabulatedFunction) {
        int count = tabulatedFunction.getCount();

        Point[] points = new Point[count];
        int i = 0;
        for (Point point : tabulatedFunction)
            points[i++] = point;

        return points;
    }

    private interface BiOperation {
        double apply(double u, double v);
    }

    private TabulatedFunction doOperation(TabulatedFunction a, TabulatedFunction b, BiOperation operation) {
        int n = a.getCount();
        if (n != b.getCount())
            throw new InconsistentFunctionsException("The functions are incompatible.");

        Point[] points1 = asPoints(a);
        Point[] points2 = asPoints(b);

        double[] xValues = new double[n];
        double[] yValues = new double[n];

        int i = 0;
        while (i < n) {
            if (points1[i].x != points2[i].x)
                throw new InconsistentFunctionsException("The functions are incompatible.");

            xValues[i] = points1[i].x;
            yValues[i] = operation.apply(points1[i].y, points2[i].y);

            ++i;
        }

        return factory.create(xValues, yValues);
    }

    public TabulatedFunction add(TabulatedFunction f1, TabulatedFunction f2) {
        return doOperation (f1, f2, (y1, y2) -> y1 + y2);
    }

    public TabulatedFunction subtract(TabulatedFunction f1, TabulatedFunction f2) {
        return doOperation (f1, f2, (y1, y2) -> y1 - y2);
    }
    public TabulatedFunction multiply(TabulatedFunction a, TabulatedFunction b) {
        return doOperation(a, b, (u, v) -> u * v);
    }

    // Деление
    public TabulatedFunction divide(TabulatedFunction a, TabulatedFunction b) {
        return doOperation(a, b, (u, v) -> {
            if (Math.abs(v) < 1e-10)
                throw new ArithmeticException("Division by zero at y = " + v);

            return u / v;
        });
    }

    public TabulatedFunctionFactory get() {
        return factory;
    }

    public void set(TabulatedFunctionFactory factory) {
        this.factory = factory;
    }
}
