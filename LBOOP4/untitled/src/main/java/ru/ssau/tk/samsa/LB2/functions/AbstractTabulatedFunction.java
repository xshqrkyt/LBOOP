package ru.ssau.tk.samsa.LB2.functions;

import ru.ssau.tk.samsa.LB2.exceptions.*;

public abstract class AbstractTabulatedFunction implements MathFunction, TabulatedFunction {
    protected abstract int floorIndexOfX(double x);
    protected abstract double extrapolateLeft(double x);
    protected abstract double extrapolateRight(double x);
    protected abstract double interpolate(double x, int floorIndex);
    protected double interpolate(double x, double leftX, double rightX, double leftY, double rightY) {
        return leftY + (rightY - leftY) / (rightX - leftX) * (x - leftX);
    }
    
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append(getClass().getSimpleName())
                .append(" size = ")
                .append(getCount())
                .append("\n");

        for (Point point : this) {
            builder.append("[")
                    .append(point.x)
                    .append("; ")
                    .append(point.y)
                    .append("]\n");
        }

        return builder.toString();
    }
    
    @Override
    public double apply(double x) {
        if (x < leftBound())
            return extrapolateLeft(x);

        else if (x > rightBound())
            return extrapolateRight(x);

        else
            if (indexOfX(x) != -1)
                return getY(indexOfX(x));
            else
                return interpolate(x, floorIndexOfX(x));
    }

    static void checkLengthIsTheSame(double[] xValues, double[] yValues) {
        if (xValues.length != yValues.length)
            throw new DifferentLengthOfArraysException("The lengths of the arrays do not match.");
    }

    static void checkSorted(double[] xValues) {
        int i = 0, n = xValues.length - 1;
        while (i < n) {
            if (xValues[i] >= xValues[i + 1])
                throw new ArrayIsNotSortedException("The elements in the array are not ordered.");

            ++i;
        }
    }
}

