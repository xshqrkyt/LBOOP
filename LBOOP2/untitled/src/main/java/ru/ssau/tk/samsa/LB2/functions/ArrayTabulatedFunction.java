package ru.ssau.tk.samsa.LB2.functions;

import java.util.Arrays;

public class ArrayTabulatedFunction extends AbstractTabulatedFunction implements Insertable, Removable {
    protected double[] xValues;
    protected double[] yValues;
    protected int count;

    ArrayTabulatedFunction(double[] xValues, double[] yValues) {
        count = xValues.length;
        this.xValues = Arrays.copyOf(xValues, count);
        this.yValues = Arrays.copyOf(yValues, count);
    }

    ArrayTabulatedFunction(MathFunction source, double xFrom, double xTo, int count) {
        this.count = count;
        xValues = new double[count];
        yValues = new double[count];

        if (xFrom > xTo) {
            double temp = xFrom;
            xFrom = xTo;
            xTo = temp;
        }

        double step = (xTo - xFrom) / (count - 1), x = xFrom;
        yValues[0] = source.apply(xValues[0] = xFrom);

        int i = 0;
        while (++i < count - 1)
            yValues[i] = source.apply(xValues[i] = x += step);

        yValues[i] = source.apply(xValues[i] = xTo);
    }

    @Override
    protected int floorIndexOfX(double x) {
        if (xValues[0] > x)
            return 0;

        else if (xValues[count - 1] < x)
            return count;

        else {
            int max = 0;
            while (max < count && xValues[max] <= x)
                ++max;

            return max - 1;
        }
    }

    @Override
    protected double extrapolateLeft(double x) {
        return interpolate(x, xValues[0], xValues[1], yValues[0], yValues[1]);
    }

    @Override
    protected double extrapolateRight(double x) {
        return interpolate(x, xValues[count - 2], xValues[count - 1], yValues[count - 2], yValues[count - 1]);
    }

    @Override
    protected double interpolate(double x, int floorIndex) {
        if (count == 1)
            return yValues[0];

        return interpolate(x, xValues[floorIndex], xValues[floorIndex + 1], yValues[floorIndex], yValues[floorIndex + 1]);
    }

    @Override
    protected double interpolate(double x, double leftX, double rightX, double leftY, double rightY) {
        return leftY + (rightY - leftY) / (rightX - leftX) * (x - leftX);
    }

    @Override
    public int getCount() {
        return count;
    }

    @Override
    public double getX(int index) {
        return xValues[index];
    }

    @Override
    public double getY(int index) {
        return yValues[index];
    }

    @Override
    public void setY(int index, double value) {
        yValues[index] = value;
    }

    @Override
    public int indexOfX(double x) {
        int i = 0;
        while (i < count)
            if (xValues[i++] == x)
                return i - 1;

        return -1;
    }

    @Override
    public int indexOfY(double y) {
        int i = 0;
        while (i < count)
            if (yValues[i++] == y)
                return i - 1;

        return -1;
    }

    @Override
    public double leftBound() {
        return xValues[0];
    }

    @Override
    public double rightBound() {
        return xValues[count - 1];
    }

    @Override
    public void insert(double x, double y) {
        if (indexOfX(x) != -1) {
            int index = floorIndexOfX(x);
            if (index == count)
                --index;
            setY(index, y);
        }

        else {
            double[] newxValues = new double[count+1];
            double[] newyValues = new double[count+1];

            if (x < xValues[0]) {
                newxValues[0] = x;
                newyValues[0] = y;

                for (int i = 0; i < count; ++i) {
                    newxValues[i + 1] = xValues[i];
                    newyValues[i + 1] = yValues[i];
                }
            }

            else if (x > xValues[count - 1]) {
                for (int i = 0; i < count; ++i) {
                    newxValues[i] = xValues[i];
                    newyValues[i] = yValues[i];
                }

                newxValues[count] = x;
                newyValues[count] = y;
            }

            else {
                int i = 0, n = count - 1;
                while (i < n && xValues[i] < x) {
                    if (xValues[i + 1] < x) {
                        newxValues[i] = xValues[i];
                        newyValues[i] = yValues[i];
                    }

                    else {
                        newxValues[i] = xValues[i];;
                        newyValues[i] = yValues[i];
                        newxValues[i + 1] = x;
                        newyValues[i + 1] = y;
                    }

                    ++i;
                }

                while (i < count) {
                    newxValues[i + 1] = xValues[i];
                    newyValues[i + 1] = yValues[i];
                    ++i;
                }

            }

            xValues = newxValues;
            yValues = newyValues;
            ++count;
        }
    }

    @Override
    public void remove(int index) {
        double[] newxValues = new double[count - 1];
        double[] newyValues = new double[count - 1];

        int i = 0;
        while (i != index) {
            newxValues[i] = xValues[i];
            newyValues[i] = yValues[i];

            ++i;
        }
        ++i;

        while (i < count) {
            newxValues[i - 1] = xValues[i];
            newyValues[i - 1] = yValues[i];

            ++i;
        }

        xValues = newxValues;
        yValues = newyValues;
        --count;
        return;
    }
}