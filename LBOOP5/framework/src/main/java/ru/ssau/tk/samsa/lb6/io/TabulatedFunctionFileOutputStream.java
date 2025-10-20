package ru.ssau.tk.samsa.LB2.io;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ru.ssau.tk.samsa.LB2.functions.*;

import java.io.*;

public class TabulatedFunctionFileOutputStream {
    private static final Logger logger = LogManager.getLogger(TabulatedFunctionFileOutputStream.class);

    public static void main(String[] args) {
        logger.info("Запущена программа записи табулированных функций из файла.");

        try (BufferedOutputStream arrayStream = new BufferedOutputStream(new FileOutputStream("output/array function.bin"));
             BufferedOutputStream linkedListStream = new BufferedOutputStream(new FileOutputStream("output/linked list function.bin"))) {

            logger.debug("Создаются тестовые функции.");

            // Создаем тестовые функции
            TabulatedFunction arrayFunction = new ArrayTabulatedFunction(new double[] {0.0, 1.0, 2.0, 3.0}, new double[] {0.0, 1.0, 4.0, 9.0});
            TabulatedFunction linkedListFunction = new LinkedListTabulatedFunction(new double[] {0.0, 0.5, 1.0, 1.5}, new double[] {0.0, 0.25, 1.0, 2.25});

            logger.debug("Функции записываются в бинарные файлы.");

            // Записываем в бинарные файлы
            FunctionsIO.writeTabulatedFunction(arrayStream, arrayFunction);
            FunctionsIO.writeTabulatedFunction(linkedListStream, linkedListFunction);

            System.out.println("Функции успешно записаны в бинарные файлы");
        }

        catch (IOException e) {
            logger.error("Ошибка при записи в файлы.", e);
            e.printStackTrace();
        }

        logger.info("Программа завершила свою работу.");
    }
}