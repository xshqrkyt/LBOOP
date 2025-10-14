package ru.ssau.tk.samsa.LB2.concurrent;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ru.ssau.tk.samsa.LB2.functions.LinkedListTabulatedFunction;
import ru.ssau.tk.samsa.LB2.functions.TabulatedFunction;
import ru.ssau.tk.samsa.LB2.functions.UnitFunction;
import ru.ssau.tk.samsa.LB2.io.TabulatedFunctionFileInputStream;

import java.util.ArrayList;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class MultiplyingTaskExecutor {
    private static final Logger logger = LogManager.getLogger(TabulatedFunctionFileInputStream.class);

    public static void main(String[] s) throws InterruptedException {
        logger.info("Запущена программа умножения ArrayTabulatedFunction в 2 раза.");

        logger.debug("Создаются функция, список потоков и набор задач.");

        TabulatedFunction function = new LinkedListTabulatedFunction(new UnitFunction(), 1, 1000, 1000);
        ArrayList<Thread> threads = new ArrayList<>();

        Set<MultiplyingTask> tasks = ConcurrentHashMap.newKeySet(10);

        logger.debug("Заполняются список потоков и набор задач.");

        for (int i = 0; i < 10; ++i) {
            MultiplyingTask task = new MultiplyingTask(function);
            threads.add(new Thread(task));
            tasks.add(task);
        }

        logger.debug("Запускаются потоки.");

        for (Thread thread : threads)
            thread.start();

        // Thread.sleep(5000);

        logger.debug("Проверяются задачи на выполнение.");

        while (!tasks.isEmpty())
            for (MultiplyingTask task : tasks)
                if (task.isEnd())
                    tasks.remove(task);

        logger.debug("Выводится результат.");

        System.out.println(function);

        logger.info("Программа завершила свою работу.");
    }
}