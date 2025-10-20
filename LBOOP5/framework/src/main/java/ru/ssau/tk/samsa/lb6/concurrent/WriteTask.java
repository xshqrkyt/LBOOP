package ru.ssau.tk.samsa.LB2.lb6.jdbc.concurrent;

import ru.ssau.tk.samsa.LB2.lb6.jdbc.functions.TabulatedFunction;

public class WriteTask implements Runnable {
    private final TabulatedFunction function;
    private final double value;

    public WriteTask(TabulatedFunction function, double value) {
        this.function = function;
        this.value = value;
    }

    @Override
    public void run() {
        for (int i = 0; i < function.getCount(); i++) {
            synchronized (function) {
                function.setY(i, value);
                System.out.printf("Writing for index %d complete%n", i);
            }
            try {
                Thread.sleep(0, 1);
            } catch (InterruptedException ignored) {}
        }
    }
}
