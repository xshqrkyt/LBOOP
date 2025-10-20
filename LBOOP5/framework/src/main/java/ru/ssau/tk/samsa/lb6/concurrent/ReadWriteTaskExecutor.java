package ru.ssau.tk.samsa.lb6.concurrent;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ru.ssau.tk.samsa.lb6.functions.ConstantFunction;
import ru.ssau.tk.samsa.lb6.functions.LinkedListTabulatedFunction;
import ru.ssau.tk.samsa.lb6.functions.TabulatedFunction;
import ru.ssau.tk.samsa.lb6.io.TabulatedFunctionFileInputStream;

public class ReadWriteTaskExecutor {
    private static final Logger logger = LogManager.getLogger(TabulatedFunctionFileInputStream.class);

    public static void main(String[] args) {
        logger.info("Запущена программа умножения ArrayTabulatedFunction в 2 раза.");

        logger.debug("Создаются функция с потоками чтения и записи функций.");

        int count = 1000;
        TabulatedFunction function = new LinkedListTabulatedFunction(new ConstantFunction(-1), 1, 1000, count);

        Thread reader = new Thread(new ReadTask(function), "Reader");
        Thread writer = new Thread(new WriteTask(function, 0.5), "Writer");

        logger.debug("Запускаются потоки.");

        reader.start();
        writer.start();

        logger.debug("Приостанавливаем текущие потоки.");

        try {
            reader.join();
            writer.join();
        } catch (InterruptedException e) {
            logger.error("Ошибка приостановки потоков.");
            e.printStackTrace();
        }

        logger.info("Программа завершила свою работу.");
    }
}

