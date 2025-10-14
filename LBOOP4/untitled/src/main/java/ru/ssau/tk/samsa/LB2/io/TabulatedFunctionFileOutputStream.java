package ru.ssau.tk.samsa.LB2.io;

import ru.ssau.tk.samsa.LB2.functions.*;

import java.io.*;

public class TabulatedFunctionFileOutputStream {
    public static void main(String[] args) {
        try (BufferedOutputStream arrayStream = new BufferedOutputStream(new FileOutputStream("output/array function.bin"));
             BufferedOutputStream linkedListStream = new BufferedOutputStream(new FileOutputStream("output/linked list function.bin"))) {

            // Создаем тестовые функции
            TabulatedFunction arrayFunction = new ArrayTabulatedFunction(
                    new double[]{0.0, 1.0, 2.0, 3.0},
                    new double[]{0.0, 1.0, 4.0, 9.0}
            );

            TabulatedFunction linkedListFunction = new LinkedListTabulatedFunction(
                    new double[]{0.0, 0.5, 1.0, 1.5},
                    new double[]{0.0, 0.25, 1.0, 2.25}
            );

            // Записываем в бинарные файлы
            FunctionsIO.writeTabulatedFunction(arrayStream, arrayFunction);
            FunctionsIO.writeTabulatedFunction(linkedListStream, linkedListFunction);

            System.out.println("Функции успешно записаны в бинарные файлы");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}