package ru.ssau.tk.samsa.LB2.io;

import ru.ssau.tk.samsa.LB2.functions.*;
import ru.ssau.tk.samsa.LB2.operations.TabulatedDifferentialOperator;
import ru.ssau.tk.samsa.LB2.functions.factory.LinkedListTabulatedFunctionFactory;

import java.io.*;

public class LinkedListTabulatedFunctionSerialization {
    public static void main(String[] args) {
        String filename = "output/serialized linked list functions.bin";

        // Сериализация
        try (BufferedOutputStream outputStream = new BufferedOutputStream(new FileOutputStream(filename))) {

            // Создаем исходную функцию и её производные
            TabulatedDifferentialOperator operator = new TabulatedDifferentialOperator(new LinkedListTabulatedFunctionFactory());

            TabulatedFunction original = new LinkedListTabulatedFunction(
                    new double[]{0.0, 1.0, 2.0, 3.0},
                    new double[]{0.0, 1.0, 4.0, 9.0}
            );

            TabulatedFunction firstDerivative = operator.derive(original);
            TabulatedFunction secondDerivative = operator.derive(firstDerivative);

            // Сериализуем все три функции
            FunctionsIO.serialize(outputStream, original);
            FunctionsIO.serialize(outputStream, firstDerivative);
            FunctionsIO.serialize(outputStream, secondDerivative);

            System.out.println("Функции сериализованы в файл: " + filename);

        } catch (IOException e) {
            e.printStackTrace();
        }

        // Десериализация
        try (BufferedInputStream inputStream = new BufferedInputStream(new FileInputStream(filename))) {

            TabulatedFunction deserializedOriginal = FunctionsIO.deserialize(inputStream);
            TabulatedFunction deserializedFirstDerivative = FunctionsIO.deserialize(inputStream);
            TabulatedFunction deserializedSecondDerivative = FunctionsIO.deserialize(inputStream);

            System.out.println("\nДесериализованные функции:");
            System.out.println("Исходная функция:\n" + deserializedOriginal);
            System.out.println("Первая производная:\n" + deserializedFirstDerivative);
            System.out.println("Вторая производная:\n" + deserializedSecondDerivative);

        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
}
