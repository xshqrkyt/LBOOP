package ru.ssau.tk.samsa.lb6.io;

import java.io.*;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.ssau.tk.samsa.lb6.functions.*;
import ru.ssau.tk.samsa.lb6.functions.factory.*;
import ru.ssau.tk.samsa.lb6.operations.TabulatedDifferentialOperator;

public class ArrayTabulatedFunctionSerialization {
    private static final Logger logger = LogManager.getLogger(TabulatedFunctionFileInputStream.class);

    public static void main (String[] str) {
        logger.info("Запущена программа сериализации и десериализации функции LinkedListTabulatedFunction.");

        try (BufferedOutputStream stream = new BufferedOutputStream(new FileOutputStream("output/serialized array functions.bin"))) {
            logger.debug("Создаётся функция с производными первого и второго порядков для сериализации.");

            ArrayTabulatedFunction f = new ArrayTabulatedFunction(new double[] {1.4, 3.5, 4.1, 5.8, 8.9}, new double[] {2.744, 42.875, 68.921, 195.112, 704.969});
            TabulatedFunction der1 = new TabulatedDifferentialOperator(new ArrayTabulatedFunctionFactory()).derive(f);
            TabulatedFunction der2 = new TabulatedDifferentialOperator(new ArrayTabulatedFunctionFactory()).derive(der1);

            logger.debug("Осуществляется сериализация.");

            FunctionsIO.serialize(stream, f);
            FunctionsIO.serialize(stream, der1);
            FunctionsIO.serialize(stream, der2);
        }

        catch (IOException error) {
            logger.error("Ошибка при сериализации.");
            error.printStackTrace();
        }

        try (BufferedInputStream stream = new BufferedInputStream(new FileInputStream("output/serialized array functions.bin"))) {
            logger.debug("Осуществляется десериализация функции с производными первого и второго порядков.");

            System.out.println(FunctionsIO.deserialize(stream));
            System.out.println(FunctionsIO.deserialize(stream));
            System.out.println(FunctionsIO.deserialize(stream));

            logger.info("Функции десериализованы.");
        }

        catch (IOException | ClassNotFoundException error) {
            logger.error("Ошибка при десериализации.");
            error.printStackTrace();
        }

        logger.info("Программа завершила свою работу.");
    }
}

