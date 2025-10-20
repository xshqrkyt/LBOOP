package ru.ssau.tk.samsa.lb6.jdbc.io;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ru.ssau.tk.samsa.lb6.jdbc.functions.*;
import ru.ssau.tk.samsa.lb6.jdbc.operations.TabulatedDifferentialOperator;
import ru.ssau.tk.samsa.lb6.jdbc.functions.factory.*;

import java.io.*;

public class TabulatedFunctionFileInputStream {
    private static final Logger logger = LogManager.getLogger(ru.ssau.tk.samsa.LB2.io.TabulatedFunctionFileInputStream.class);

    public static void main(String[] args) {
        logger.info("Запущена программа чтения табулированных функций из файла.");

        // Чтение из бинарного файла
        try (BufferedInputStream fileInputStream = new BufferedInputStream(new FileInputStream("input/binary function.bin"))) {
            logger.debug("Создаются фабрика с функцией.");

            TabulatedFunctionFactory arrayFactory = new ArrayTabulatedFunctionFactory();
            TabulatedFunction functionFromFile = FunctionsIO.readTabulatedFunction(fileInputStream, arrayFactory);

            logger.debug("Выводится результат чтения из бинарного файла.");

            System.out.println("Функция, прочитанная из бинарного файла:");
            System.out.println(functionFromFile);
        } catch (IOException e) {
            logger.error("Ошибка при чтении функций из файла.", e);
            e.printStackTrace();
        }


        // Чтение из консоли
        System.out.println("\nВведите размер и значения функции");
        System.out.println("Формат: сначала количество точек, затем пары x y через пробел");
        System.out.println("Пример:");
        System.out.println("3");
        System.out.println("0.0 0.0");
        System.out.println("1.0 1.0");
        System.out.println("2.0 4.0");

        // Важно: НЕ используем try-with-resources для System.in!
        BufferedReader consoleReader = null;
        try {
            logger.debug("Создаются буферизированный поток consoleReader.");

            // Обертываем System.in для чтения
            InputStreamReader inputStreamReader = new InputStreamReader(System.in);
            consoleReader = new BufferedReader(inputStreamReader);

            logger.debug("Производится чтение функции с консоли.");

            TabulatedFunctionFactory linkedListFactory = new LinkedListTabulatedFunctionFactory();
            TabulatedFunction functionFromConsole = FunctionsIO.readTabulatedFunction(consoleReader, linkedListFactory);

            System.out.println("\nФункция, введенная с консоли:");
            System.out.println(functionFromConsole);

            logger.debug("Производится вычисление производной функции.");

            // Вычисляем производную
            TabulatedDifferentialOperator operator = new TabulatedDifferentialOperator(linkedListFactory);
            TabulatedFunction derivative = operator.derive(functionFromConsole);

            logger.debug("Выводится результат чтения из консоли.");

            System.out.println("Производная функции:");
            System.out.println(derivative);

        } catch (IOException e) {
            logger.error("Ошибка при чтении функции с консоли.");
            e.printStackTrace();
        } finally {
            // Закрываем reader, но НЕ закрываем System.in
            if (consoleReader != null) {
                try {
                    logger.debug("Производится закрытие потока consoleReader.");
                    consoleReader.close();
                } catch (IOException e) {
                    logger.error("Ошибка при закрытии потока consoleReader.");
                    e.printStackTrace();
                }
            }
        }

        logger.info("Программа завершила свою работу.");
    }
}
