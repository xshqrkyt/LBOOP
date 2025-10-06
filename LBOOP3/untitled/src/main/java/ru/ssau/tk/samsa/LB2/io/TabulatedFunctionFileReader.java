package io;

import functions.*;
import functions.ArrayTabulatedFunction;
import functions.factory.*;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class TabulatedFunctionFileReader {
    public static void main(String[] str) {
        try (BufferedReader fileReader1 = new BufferedReader(new FileReader("input/function.txt")); BufferedReader fileReader2 = new BufferedReader(new FileReader("input/function.txt"))) {
            ArrayTabulatedFunction f1 = (ArrayTabulatedFunction)FunctionsIO.readTabulatedFunction(fileReader1, new ArrayTabulatedFunctionFactory());
            LinkedListTabulatedFunction f2 = (LinkedListTabulatedFunction)FunctionsIO.readTabulatedFunction(fileReader2, new LinkedListTabulatedFunctionFactory());

            System.out.println(f1.toString());
            System.out.println(f2.toString());
        }

        catch (IOException error) {
            error.printStackTrace();
        }




    }
}