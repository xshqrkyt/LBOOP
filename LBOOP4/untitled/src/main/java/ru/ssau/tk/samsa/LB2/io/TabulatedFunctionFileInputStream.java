package ru.ssau.tk.samsa.LB2.io;

import ru.ssau.tk.samsa.LB2.functions.*;
import ru.ssau.tk.samsa.LB2.operations.TabulatedDifferentialOperator;
import ru.ssau.tk.samsa.LB2.functions.factory.*;

import java.io.*;

public class TabulatedFunctionFileInputStream {
    public static void main(String[] args) {
        // Чтение из бинарного файла
        try (BufferedInputStream fileInputStream = new BufferedInputStream(new FileInputStream("input/binary function.bin"))) {

            TabulatedFunctionFactory arrayFactory = new ArrayTabulatedFunctionFactory();
            TabulatedFunction functionFromFile = FunctionsIO.readTabulatedFunction(fileInputStream, arrayFactory);

            System.out.println("Функция, прочитанная из бинарного файла:");
            System.out.println(functionFromFile);

        } catch (IOException e) {
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
            // Обертываем System.in для чтения
            InputStreamReader inputStreamReader = new InputStreamReader(System.in);
            consoleReader = new BufferedReader(inputStreamReader);

            TabulatedFunctionFactory linkedListFactory = new LinkedListTabulatedFunctionFactory();
            TabulatedFunction functionFromConsole = FunctionsIO.readTabulatedFunction(consoleReader, linkedListFactory);

            System.out.println("\nФункция, введенная с консоли:");
            System.out.println(functionFromConsole);

            // Вычисляем производную
            TabulatedDifferentialOperator operator = new TabulatedDifferentialOperator(linkedListFactory);
            TabulatedFunction derivative = operator.derive(functionFromConsole);

            System.out.println("Производная функции:");
            System.out.println(derivative);

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            // Закрываем reader, но НЕ закрываем System.in
            if (consoleReader != null) {
                try {
                    consoleReader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
