package ru.ssau.tk.samsa.LB2.concurrent;

import ru.ssau.tk.samsa.LB2.functions.LinkedListTabulatedFunction;
import ru.ssau.tk.samsa.LB2.functions.TabulatedFunction;
import ru.ssau.tk.samsa.LB2.functions.UnitFunction;

import java.util.ArrayList;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class MultiplyingTaskExecutor {
    public static void main(String[] s) throws InterruptedException {
        TabulatedFunction function = new LinkedListTabulatedFunction(new UnitFunction(), 1, 1000, 1000);
        ArrayList<Thread> threads = new ArrayList<>();

        Set<MultiplyingTask> tasks = ConcurrentHashMap.newKeySet(10);
        
        for (int i = 0; i < 10; ++i) {
            MultiplyingTask task = new MultiplyingTask(function);
            threads.add(new Thread(task));
            tasks.add(task);
        }

        for (Thread thread : threads)
            thread.start();

        // Thread.sleep(5000);

        while (!tasks.isEmpty())
            for (MultiplyingTask task : tasks)
                if (task.isEnd())
                    tasks.remove(task);

        System.out.println(function);
    }
}
