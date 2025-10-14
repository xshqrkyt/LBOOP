package ru.ssau.tk.samsa.LB2.concurrent;

import ru.ssau.tk.samsa.LB2.functions.ConstantFunction;
import ru.ssau.tk.samsa.LB2.functions.TabulatedFunction;
<<<<<<< HEAD
import ru.ssau.tk.samsa.LB2.factory.LinkedListTabulatedFunctionFactory;
=======
import ru.ssau.tk.samsa.LB2.functions.factory.LinkedListTabulatedFunctionFactory;
>>>>>>> 8045191b29f044a19c8e4500187c71e6713d3c36

public class ReadWriteTaskExecutor {
    public static void main(String[] args) {
        int count = 1000;
        var factory = new LinkedListTabulatedFunctionFactory();

        TabulatedFunction function = factory.create(
                new ConstantFunction(-1),
                1, 1000, count
        );

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
