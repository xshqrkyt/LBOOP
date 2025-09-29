package ru.ssau.tk.samsa.LB2.functions;

public interface MathFunction {
    double apply(double x);

    default CompositeFunction andThen(MathFunction afterFunction) {
        CompositeFunction f = new CompositeFunction(this, afterFunction);
        return f;
    }
}