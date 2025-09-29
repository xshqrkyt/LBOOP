package $ru.ssau.tk._NAME_._PROJECT_.functions;

public interface MathFunction {
    double apply(double x);

    default CompositeFunction andThen(MathFunction afterFunction) {
        CompositeFunction f = new CompositeFunction(this, afterFunction);
        return f;
    }
}