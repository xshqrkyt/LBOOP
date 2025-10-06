package ru.ssau.tk.samsa.LB2.functions;

import java.util.Arrays;

public class ArrayTabulatedFunction extends AbstractTabulatedFunction implements Insertable, Removable, Serializable {
    protected double[] xValues;
    protected double[] yValues;
    protected int count;
    private static final long serialVersionUID = 1L;

    public ArrayTabulatedFunction(double[] xValues, double[] yValues) {
        if (xValues.length < 2) {
            throw new IllegalArgumentException("Length of array is less than 2");
        }
        count = xValues.length;
        this.xValues = Arrays.copyOf(xValues, count);
        this.yValues = Arrays.copyOf(yValues, count);
    }

    public ArrayTabulatedFunction(MathFunction source, double xFrom, double xTo, int count) {
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
        if (x < xValues[0]) {
            throw new IllegalArgumentException("x = " + x + " is less than left bound " + xValues[0]);
        }

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
        if (index < 0 || index >= count) {
            throw new IllegalArgumentException("Index " + index + " is out of bounds [0, " + (count-1) + "]");
        }
        return xValues[index];
    }

    @Override
    public double getY(int index) {
        if (index < 0 || index >= count) {
            throw new IllegalArgumentException("Index " + index + " is out of bounds [0, " + (count-1) + "]");
        }
        return yValues[index];
    }

    @Override
    public void setY(int index, double value) {
        if (index < 0 || index >= count) {
            throw new IllegalArgumentException("Index " + index + " is out of bounds [0, " + (count-1) + "]");
        }
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
        int existingIndex = indexOfX(x);
        if (existingIndex != -1) {
            setY(existingIndex, y);
            return;
        }

        int insertIndex = findInsertIndex(x);

        // Увеличиваем массивы
        xValues = Arrays.copyOf(xValues, count + 1);
        yValues = Arrays.copyOf(yValues, count + 1);

        // Сдвигаем элементы для освобождения места
        if (insertIndex < count) {
            System.arraycopy(xValues, insertIndex, xValues, insertIndex + 1, count - insertIndex);
            System.arraycopy(yValues, insertIndex, yValues, insertIndex + 1, count - insertIndex);
        }

        // Вставляем новый элемент
        xValues[insertIndex] = x;
        yValues[insertIndex] = y;
        ++count;
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