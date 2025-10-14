package ru.ssau.tk.samsa.LB2.concurrent;

import ru.ssau.tk.samsa.LB2.functions.ConstantFunction;
import ru.ssau.tk.samsa.LB2.functions.TabulatedFunction;
import ru.ssau.tk.samsa.LB2.functions.LinkedListTabulatedFunction;

public class ReadWriteTaskExecutor {
    public static void main(String[] args) {
        int count = 1000;
        TabulatedFunction function = new LinkedListTabulatedFunction(new ConstantFunction(-1), 1, 1000, count);

        Thread reader = new Thread(new ReadTask(function), "Reader");
        Thread writer = new Thread(new WriteTask(function, 0.5), "Writer");

        reader.start();
        writer.start();

        try {
            reader.join();
            writer.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
