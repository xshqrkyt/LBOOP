package ru.ssau.tk.samsa.lb6.functions;

public class DeBoorFunction implements MathFunction {
    private final double[] knots;
    private final double[] controlPoints;
    private final int degree;

    public DeBoorFunction(double[] knots, double[] controlPoints, int degree) {
        if (knots.length != controlPoints.length + degree + 1) {
            throw new IllegalArgumentException(
                    "Invalid knots length. Expected: " + (controlPoints.length + degree + 1) +
                            ", got: " + knots.length
            );
        }
        if (!isNonDecreasing(knots)) {
            throw new IllegalArgumentException("Knots must be non-decreasing");
        }

        this.knots = knots.clone();
        this.controlPoints = controlPoints.clone();
        this.degree = degree;
    }

    private boolean isNonDecreasing(double[] array) {
        for (int i = 1; i < array.length; i++) {
            if (array[i] < array[i - 1]) {
                return false;
            }
        }
        return true;
    }

    @Override
    public double apply(double x) {
        return deBoorAlgorithm(x);
    }

    private double deBoorAlgorithm(double x) {
        // Проверка выхода за пределы области определения
        if (x < knots[0] || x > knots[knots.length - 1]) {
            return 0.0;
        }

        // Находим интервал [knots[span], knots[span+1]), содержащий x
        int span = findSpan(x);

        // Если span выходит за допустимые пределы, возвращаем 0
        if (span < degree || span >= knots.length - degree - 1) {
            return 0.0;
        }

        // Создаем временный массив для вычислений
        double[] d = new double[degree + 1];
        for (int i = 0; i <= degree; i++) {
            d[i] = controlPoints[span - degree + i];
        }

        // Рекурсивный алгоритм де Бура
        for (int r = 1; r <= degree; r++) {
            for (int j = degree; j >= r; j--) {
                int i = span - degree + j;
                double left = knots[i];
                double right = knots[i + degree + 1 - r];

                // Проверка деления на ноль
                if (right == left) {
                    // Если знаменатель нулевой, продолжаем с предыдущим значением
                    continue;
                }

                double alpha = (x - left) / (right - left);
                d[j] = (1.0 - alpha) * d[j - 1] + alpha * d[j];
            }
        }

        return d[degree];
    }

    /**
     * Находит индекс span такой, что knots[span] <= x < knots[span+1]
     */
    private int findSpan(double x) {
        int n = knots.length - degree - 2; // Количество контрольных точек - 1

        // Особые случаи на границах
        if (x >= knots[n + 1]) {
            return n;
        }
        if (x <= knots[degree]) {
            return degree;
        }

        // Бинарный поиск
        int low = degree;
        int high = n + 1;
        int mid = (low + high) / 2;

        while (x < knots[mid] || x >= knots[mid + 1]) {
            if (x < knots[mid]) {
                high = mid;
            } else {
                low = mid;
            }
            mid = (low + high) / 2;
        }

        return mid;
    }

    // Геттеры для тестирования
    public double[] getKnots() {
        return knots.clone();
    }

    public double[] getControlPoints() {
        return controlPoints.clone();
    }

    public int getDegree() {
        return degree;
    }

}


