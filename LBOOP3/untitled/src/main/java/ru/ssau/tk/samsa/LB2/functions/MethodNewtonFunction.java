package ru.ssau.tk.samsa.LB2.functions;

public class MethodNewtonFunction implements MathFunction {
   private MathFunction f;
   private static final double EPS = 1e-10; // Точность вычислений
   private static final double H = 1e-6; // Маленький шаг для аппроксимации производной

   public MethodNewtonFunction (MathFunction f) {
       this.f = f;
   }

   @Override
   public double apply(double x) {
       double x0, root = x, df, h = H;
       int i = 0;

       do {
           x0 = root;

           df = derivative(x0, h); // Производная
           while (Math.abs(df) < EPS && h >= EPS) {
               h /= 10;
               df = derivative(x0, h);
           }

           root = x0 - f.apply(x0) / df;

           h = H;
       } while (Math.abs(root - x0) > EPS && i++ < 1000);

       return root;
   }

   // Центральная аппроксимация производной
   private double derivative(double x, double h) {
       return (f.apply(x + h) - f.apply(x - h)) / (2 * h);
   }
}