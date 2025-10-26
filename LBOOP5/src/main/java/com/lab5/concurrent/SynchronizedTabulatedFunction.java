package com.lab5.concurrent;

import com.lab5.functions.Point;
import com.lab5.functions.TabulatedFunction;
import com.lab5.operations.TabulatedFunctionOperationService;

import java.util.Iterator;
import java.util.NoSuchElementException;

public class SynchronizedTabulatedFunction implements TabulatedFunction {
    private TabulatedFunction function;

    public SynchronizedTabulatedFunction(TabulatedFunction function) {
        this.function = function;
    }

    @Override
    public synchronized int getCount() {
        return function.getCount();
    }

    @Override
    public synchronized double getX(int index) {
        return function.getX(index);
    }

    @Override
    public synchronized double getY(int index) {
        return function.getY(index);
    }

    @Override
    public synchronized void setY(int index, double value) {
        function.setY(index, value);
    }

    @Override
    public synchronized int indexOfX(double x) {
        return function.indexOfX(x);
    }

    @Override
    public synchronized int indexOfY(double y) {
        return function.indexOfY(y);
    }

    @Override
    public synchronized double leftBound() {
        return function.leftBound();
    }

    @Override
    public synchronized double rightBound() {
        return function.rightBound();
    }

    @Override
    public synchronized double apply(double x) {
        return function.apply(x);
    }

    @Override
    public synchronized Iterator<Point> iterator() {
        // Получаем копию всех точек функции
        Point[] pointsCopy = TabulatedFunctionOperationService.asPoints(function);

        // Возвращаем итератор по копии, а не по "живым" данным
        return new Iterator<Point>() {
            private int index = 0;

            @Override
            public boolean hasNext() {
                return index < pointsCopy.length;
            }

            @Override
            public Point next() {
                if (!hasNext()) {
                    throw new NoSuchElementException();
                }
                return pointsCopy[index++];
            }
        };
    }

    public interface Operation<T> {
        T apply(SynchronizedTabulatedFunction function);
    }

    public <T> T doSynchronously(Operation<? extends T> operation) {
        synchronized (function) {
            return operation.apply(this);
        }
    }
}
