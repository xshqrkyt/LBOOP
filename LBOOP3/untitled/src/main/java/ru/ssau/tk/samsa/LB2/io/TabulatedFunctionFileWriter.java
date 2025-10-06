package ru.ssau.tk.samsa.LB2.io;

import java.io.*;
import ru.ssau.tk.samsa.LB2.functions.*;
import java.lang.String;

public class TabulatedFunctionFileWriter {
    public static void main(String[] str) {
        try (BufferedWriter fileWriter1 = new BufferedWriter(new FileWriter("output/array function.txt")); BufferedWriter fileWriter2 = new BufferedWriter(new FileWriter("output/linked list function.txt"))) {
            TabulatedFunction f1 = new ArrayTabulatedFunction(new double[] {1, 2, 3}, new double[] {1, 4, 9});
            TabulatedFunction f2 = new LinkedListTabulatedFunction(new double[] {1, 2, 3}, new double[] {-1, 0, 1});

            FunctionsIO.writeTabulatedFunction(fileWriter1, f1);
            FunctionsIO.writeTabulatedFunction(fileWriter2, f2);
        }

        catch (IOException error) {
            error.printStackTrace();
        }
    }
}
