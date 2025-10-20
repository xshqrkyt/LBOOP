package ru.ssau.tk.samsa.LB2.concurrent;

import ru.ssau.tk.samsa.LB2.functions.TabulatedFunction;

public class MultiplyingTask implements Runnable {
    private TabulatedFunction function;
    private boolean end = false;

    public MultiplyingTask(TabulatedFunction function) {
        this.function = function;
    }

    public void run() {
        int n = function.getCount();
        for (int i = 0; i < n; ++i)
            synchronized (function) {
                function.setY(i, 2 * function.getY(i));
            }

        System.out.println("This thread has completed the task.");
        end = true;
    }

    public boolean isEnd() {
        return end;
    }
}