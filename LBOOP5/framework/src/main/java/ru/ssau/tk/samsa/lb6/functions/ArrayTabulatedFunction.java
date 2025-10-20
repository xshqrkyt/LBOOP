package ru.ssau.tk.samsa.LB2.lb6.jdbc.functions;

import ru.ssau.tk.samsa.LB2.lb6.jdbc.exceptions.*;

import java.util.Arrays;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.io.Serializable;

public class ArrayTabulatedFunction extends AbstractTabulatedFunction implements Insertable, Removable, Serializable {
    protected double[] xValues;
    protected double[] yValues;
    protected int count;

    private static final long serialVersionUID = 7505690091096630414L;
    
    public ArrayTabulatedFunction(double[] xValues, double[] yValues) {
        checkLengthIsTheSame(xValues, yValues);
        
        if (xValues.length < 2)
            throw new IllegalArgumentException("Length of array is less than 2");
        
        checkSorted(xValues);
        
        count = xValues.length;
        this.xValues = Arrays.copyOf(xValues, count);
        this.yValues = Arrays.copyOf(yValues, count);
    }

    public ArrayTabulatedFunction(MathFunction source, double xFrom, double xTo, int count) {
        if (count < 2)
            throw new IllegalArgumentException("Count of points is less than 2");
        
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
        if (x < xValues[0])
            throw new IllegalArgumentException("x = " + x + " is less than left bound " + xValues[0]);

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
        if (x < xValues[floorIndex] || xValues[floorIndex + 1] < x)
            throw new InterpolationException("The number is not inside the interpolation interval.");

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
        if (index < 0 || index >= count)
            throw new IllegalArgumentException("Index " + index + " is out of bounds [0, " + (count-1) + "]");
        
        return xValues[index];
    }

    @Override
    public double getY(int index) {
        if (index < 0 || index >= count)
            throw new IllegalArgumentException("Index " + index + " is out of bounds [0, " + (count-1) + "]");

        return yValues[index];
    }

    @Override
    public void setY(int index, double value) {
        if (index < 0 || index >= count)
            throw new IllegalArgumentException("Index " + index + " is out of bounds [0, " + (count-1) + "]");

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
        if (Double.isNaN(x) || Double.isNaN(y) || Double.isInfinite(x) || Double.isInfinite(y))
            throw new IllegalArgumentException("Incorrect value.");
        
        int existingIndex = indexOfX(x);
        if (existingIndex != -1) {
            setY(existingIndex, y);
            return;
        }

        // Находим позицию для вставки
        int insertIndex = 0;
        while (insertIndex < count && xValues[insertIndex] < x) {
            insertIndex++;
        }

        // Создаем новые массивы с увеличенным размером
        double[] newXValues = new double[count + 1];
        double[] newYValues = new double[count + 1];

        // Копируем элементы до позиции вставки
        System.arraycopy(xValues, 0, newXValues, 0, insertIndex);
        System.arraycopy(yValues, 0, newYValues, 0, insertIndex);

        // Вставляем новый элемент
        newXValues[insertIndex] = x;
        newYValues[insertIndex] = y;

        // Копируем оставшиеся элементы
        System.arraycopy(xValues, insertIndex, newXValues, insertIndex + 1, count - insertIndex);
        System.arraycopy(yValues, insertIndex, newYValues, insertIndex + 1, count - insertIndex);

        xValues = newXValues;
        yValues = newYValues;
        count++;
    }

    @Override
    public void remove(int index) {
        if (count == 2)
            throw new IllegalArgumentException("Length of array is 2");
        
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

    @Override
    public Iterator<Point> iterator() {
        // throw new UnsupportedOperationException("This method has not been implemented yet.");

        return new Iterator<Point>() {
            int i = 0;

            @Override
            public boolean hasNext() {
                if (i < count)
                    return true;

                return false;
            }

            @Override
            public Point next() {
                if(!hasNext())
                    throw new NoSuchElementException("The requested item does not exist.");

                else
                    return new Point(xValues[i], yValues[i++]);
            }
        };
    }
}
