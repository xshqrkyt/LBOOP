package ru.ssau.tk.samsa.LB2.functions;

import java.util.Iterator;

public class StrictTabulatedFunction implements TabulatedFunction {
    private final TabulatedFunction function;

    public StrictTabulatedFunction(TabulatedFunction function) {
        this.function = function;
    }

    @Override
    public int getCount() {
        return function.getCount();
    }

    @Override
    public double getX(int index) {
        return function.getX(index);
    }

    @Override
    public double getY(int index) {
        return function.getY(index);
    }

    @Override
    public void setY(int index, double value) {
        function.setY(index, value);
    }

    @Override
    public int indexOfX(double x) {
        return function.indexOfX(x);
    }

    @Override
    public int indexOfY(double y) {
        return function.indexOfY(y);
    }

    @Override
    public double leftBound() {
        return function.leftBound();
    }

    @Override
    public double rightBound() {
        return function.rightBound();
    }

    @Override
    public double apply(double x) {
        int index = indexOfX(x);
        if (index == -1) {
            throw new UnsupportedOperationException(
                    "Interpolation is not allowed for strict function. x = " + x + " not found in table."
            );
        }
        return getY(index);
    }

    @Override
    public Iterator<Point> iterator() {
        return function.iterator();
    }
}