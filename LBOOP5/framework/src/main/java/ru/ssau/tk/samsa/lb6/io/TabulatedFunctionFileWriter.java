package ru.ssau.tk.samsa.lb6.io;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ru.ssau.tk.samsa.lb6.functions.*;

import java.io.*;
import java.lang.String;

public class TabulatedFunctionFileWriter {
    private static final Logger logger = LogManager.getLogger(TabulatedFunctionFileWriter.class);

    public static void main(String[] str) {
        logger.info("Запущена программа записи табулированных функций из файла.");

        try (BufferedWriter fileWriter1 = new BufferedWriter(new FileWriter("output/array function.txt")); BufferedWriter fileWriter2 = new BufferedWriter(new FileWriter("output/linked list function.txt"))) {
            logger.debug("Создаются две функции.");
            TabulatedFunction f1 = new ArrayTabulatedFunction(new double[] {1, 2, 3}, new double[] {1, 4, 9});
            TabulatedFunction f2 = new LinkedListTabulatedFunction(new double[] {1, 2, 3}, new double[] {-1, 0, 1});

            logger.debug("Функции записываются в консоль.");
            FunctionsIO.writeTabulatedFunction(fileWriter1, f1);
            FunctionsIO.writeTabulatedFunction(fileWriter2, f2);

            logger.info("Функции записаны в консоль.");
        }

        catch (IOException error) {
            logger.error("Ошибка при записи функций в файл.", error);
            error.printStackTrace();
        }

        logger.info("Программа завершила свою работу.");
    }
}

