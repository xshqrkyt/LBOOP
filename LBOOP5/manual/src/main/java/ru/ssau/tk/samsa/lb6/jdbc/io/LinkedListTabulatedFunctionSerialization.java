package ru.ssau.tk.samsa.lb6.jdbc.io;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ru.ssau.tk.samsa.lb6.jdbc.functions.*;
import ru.ssau.tk.samsa.lb6.jdbc.operations.TabulatedDifferentialOperator;
import ru.ssau.tk.samsa.lb6.jdbc.functions.factory.LinkedListTabulatedFunctionFactory;

import java.io.*;

public class LinkedListTabulatedFunctionSerialization {
    private static final Logger logger = LogManager.getLogger(TabulatedFunctionFileInputStream.class);

    public static void main(String[] args) {
        logger.info("Запущена программа сериализации и десериализации функции LinkedListTabulatedFunction.");

        String filename = "output/serialized linked list functions.bin";

        // Сериализация
        try (BufferedOutputStream outputStream = new BufferedOutputStream(new FileOutputStream(filename))) {
            logger.debug("Создаётся функция с производными первого и второго порядков для сериализации.");

            // Создаем исходную функцию и её производные
            TabulatedDifferentialOperator operator = new TabulatedDifferentialOperator(new LinkedListTabulatedFunctionFactory());

            TabulatedFunction original = new LinkedListTabulatedFunction(
                    new double[]{0.0, 1.0, 2.0, 3.0},
                    new double[]{0.0, 1.0, 4.0, 9.0}
            );

            TabulatedFunction firstDerivative = operator.derive(original);
            TabulatedFunction secondDerivative = operator.derive(firstDerivative);

            logger.debug("Осуществляется сериализация.");

            // Сериализуем все три функции
            FunctionsIO.serialize(outputStream, original);
            FunctionsIO.serialize(outputStream, firstDerivative);
            FunctionsIO.serialize(outputStream, secondDerivative);

            logger.info("Функции сериализованы.");

            System.out.println("Функции сериализованы в файл: " + filename);

        } catch (IOException e) {
            logger.error("Ошибка при сериализации.");
            e.printStackTrace();
        }

        // Десериализация
        try (BufferedInputStream inputStream = new BufferedInputStream(new FileInputStream(filename))) {
            logger.debug("Осуществляется десериализация функции с производными первого и второго порядков.");

            TabulatedFunction deserializedOriginal = FunctionsIO.deserialize(inputStream);
            TabulatedFunction deserializedFirstDerivative = FunctionsIO.deserialize(inputStream);
            TabulatedFunction deserializedSecondDerivative = FunctionsIO.deserialize(inputStream);

            logger.info("Функции десериализованы.");

            System.out.println("\nДесериализованные функции:");
            System.out.println("Исходная функция:\n" + deserializedOriginal);
            System.out.println("Первая производная:\n" + deserializedFirstDerivative);
            System.out.println("Вторая производная:\n" + deserializedSecondDerivative);
        } catch (IOException | ClassNotFoundException e) {
            logger.error("Ошибка при десериализации.");
            e.printStackTrace();
        }

        logger.info("Программа завершила свою работу.");
    }
}
