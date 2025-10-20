package ru.ssau.tk.samsa.LB2.operations;

public abstract class SteppingDifferentialOperator<T> implements DifferentialOperator<T> {
    protected double step;

    public SteppingDifferentialOperator(double step) {
        if (step <= 0 || Double.isInfinite(step) || Double.isNaN(step))
            throw new IllegalArgumentException("Incorrect step.");

        this.step = step;
    }

    public double get() {
        return step;
    }

    public void set(double step) {
        this.step = step;
    }
}