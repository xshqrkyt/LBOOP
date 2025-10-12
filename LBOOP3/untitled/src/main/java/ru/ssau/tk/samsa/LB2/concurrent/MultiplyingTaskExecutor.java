package ru.ssau.tk.samsa.LB2.concurrent;

import ru.ssau.tk.samsa.LB2.functions.LinkedListTabulatedFunction;
import ru.ssau.tk.samsa.LB2.functions.Point;
import ru.ssau.tk.samsa.LB2.functions.TabulatedFunction;
import ru.ssau.tk.samsa.LB2.functions.UnitFunction;

import java.util.ArrayList;

public class MultiplyingTaskExecutor {
    public static void main(String[] s) throws InterruptedException {
        TabulatedFunction function = new LinkedListTabulatedFunction(new UnitFunction(), 1, 1000, 1000);
        ArrayList<Thread> threads = new ArrayList<>();

        for (int i = 0; i < 10; ++i)
            threads.add(new Thread(new MultiplyingTask(function)));

        for (Thread thread : threads)
                thread.start();

        Thread.sleep(5000);

        for (Point point : function)
            System.out.println(point.y);
    }
}