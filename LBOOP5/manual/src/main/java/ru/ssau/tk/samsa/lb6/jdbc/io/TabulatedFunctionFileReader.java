package ru.ssau.tk.samsa.lb6.jdbc.io;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ru.ssau.tk.samsa.lb6.jdbc.functions.*;
import ru.ssau.tk.samsa.lb6.jdbc.functions.factory.*;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class TabulatedFunctionFileReader {
    private static final Logger logger = LogManager.getLogger(TabulatedFunctionFileReader.class);

    public static void main(String[] str) {
        logger.info("Запущена программа чтения табулированных функций из файла.");

        try (BufferedReader fileReader1 = new BufferedReader(new FileReader("input/function.txt")); BufferedReader fileReader2 = new BufferedReader(new FileReader("input/function.txt"))) {
            logger.debug("Считывается первая функция.");
            ArrayTabulatedFunction f1 = (ArrayTabulatedFunction)FunctionsIO.readTabulatedFunction(fileReader1, new ArrayTabulatedFunctionFactory());

            logger.debug("Считывается вторая функция.");
            LinkedListTabulatedFunction f2 = (LinkedListTabulatedFunction)FunctionsIO.readTabulatedFunction(fileReader2, new LinkedListTabulatedFunctionFactory());

            System.out.println(f1.toString());
            System.out.println(f2.toString());
            logger.info("Функции выведены в консоль.");
        }

        catch (IOException error) {
            logger.error("Ошибка при чтении функций из файла.", error);
            error.printStackTrace();
        }

        logger.info("Программа завершила свою работу.");
    }
}
