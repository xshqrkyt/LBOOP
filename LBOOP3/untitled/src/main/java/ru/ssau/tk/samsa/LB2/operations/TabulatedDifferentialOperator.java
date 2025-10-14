package ru.ssau.tk.samsa.LB2.operations;

import ru.ssau.tk.samsa.LB2.concurrent.SynchronizedTabulatedFunction;
import ru.ssau.tk.samsa.LB2.functions.TabulatedFunction;
import ru.ssau.tk.samsa.LB2.functions.ArrayTabulatedFunction;
import ru.ssau.tk.samsa.LB2.functions.LinkedListTabulatedFunction;
import ru.ssau.tk.samsa.LB2.functions.factory.TabulatedFunctionFactory;
import ru.ssau.tk.samsa.LB2.functions.factory.ArrayTabulatedFunctionFactory;
import ru.ssau.tk.samsa.LB2.functions.factory.LinkedListTabulatedFunctionFactory;

import java.lang.reflect.Method;
import java.util.Arrays;

/**
 * Robust TabulatedDifferentialOperator implementation.
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

    public TabulatedFunction derive(TabulatedFunction function) {
        int n = function.getCount();
        if (n < 2) {
            // тривиальный случай: вернём копию через фабрику или новую реализацию
            double[] xs = new double[n];
            double[] ys = new double[n];
            for (int i = 0; i < n; i++) {
                xs[i] = function.getX(i);
                ys[i] = function.getY(i);
            }
            return createWithFactoryOrFallback(xs, ys);
        }

        double[] xValues = new double[n];
        double[] yValues = new double[n];

        // Копируем X как есть
        for (int i = 0; i < n; i++) {
            xValues[i] = function.getX(i);
        }

        // Вычисляем производную как впередя разность для [0..n-2]
        for (int i = 0; i < n - 1; i++) {
            double x0 = function.getX(i);
            double x1 = function.getX(i + 1);
            double y0 = function.getY(i);
            double y1 = function.getY(i + 1);
            // безопасное деление на double
            yValues[i] = (y1 - y0) / (x1 - x0);
        }

        // Последняя точка — левая разность (повтор предыдущей производной)
        yValues[n - 1] = yValues[n - 2];

        // Возвращаем созданную функци через фабрику (с fallback'ами)
        return createWithFactoryOrFallback(xValues, yValues);
    }

    private TabulatedFunction createWithFactoryOrFallback(double[] xValues, double[] yValues) {
        // Попытка 1: вызвать factory.create(double[], double[]) если такой метод есть
        try {
            Method m = factory.getClass().getMethod("create", double[].class, double[].class);
            if (m != null) {
                Object res = m.invoke(factory, xValues, yValues);
                if (res instanceof TabulatedFunction) {
                    return (TabulatedFunction) res;
                }
            }
        } catch (NoSuchMethodException ignored) {
            // метод не найден — это нормально, попробуем другие варианты
        } catch (Exception ex) {
            // если отражение упало — печатаем и продолжим fallback
            ex.printStackTrace();
        }

        // Попытка 2: вызвать factory.create(TabulatedFunction) если есть (передадим временную реализацию)
        try {
            Method m2 = null;
            for (Method mm : factory.getClass().getMethods()) {
                Class<?>[] params = mm.getParameterTypes();
                if (params.length == 1 && TabulatedFunction.class.isAssignableFrom(params[0]) && mm.getName().equals("create")) {
                    m2 = mm;
                    break;
                }
            }
            if (m2 != null) {
                // создаём временную ArrayTabulatedFunction и передаём
                TabulatedFunction tmp = new ArrayTabulatedFunction(xValues, yValues);
                Object res = m2.invoke(factory, tmp);
                if (res instanceof TabulatedFunction) {
                    return (TabulatedFunction) res;
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        // Fallback: если фабрика — ArrayTabulatedFunctionFactory, вернём ArrayTabulatedFunction
        if (factory instanceof ArrayTabulatedFunctionFactory) {
            return new ArrayTabulatedFunction(xValues, yValues);
        }

        // Fallback: если фабрика — LinkedListTabulatedFunctionFactory, вернём LinkedListTabulatedFunction
        if (factory instanceof LinkedListTabulatedFunctionFactory) {
            return new LinkedListTabulatedFunction(xValues, yValues);
        }

        // Последний резервный вариант: вернём ArrayTabulatedFunction
        return new ArrayTabulatedFunction(xValues, yValues);
    }

    public TabulatedFunction deriveSynchronously(TabulatedFunction function) {
        if (function instanceof SynchronizedTabulatedFunction) {
            SynchronizedTabulatedFunction sync = (SynchronizedTabulatedFunction) function;
            // предполагается, что doSynchronously принимает Operation и возвращает TabulatedFunction
            return sync.doSynchronously(f -> derive(sync.getDelegate()));
        } else {
            SynchronizedTabulatedFunction wrapper = new SynchronizedTabulatedFunction(function);
            return wrapper.doSynchronously(f -> derive(wrapper.getDelegate()));
        }
    }
}
