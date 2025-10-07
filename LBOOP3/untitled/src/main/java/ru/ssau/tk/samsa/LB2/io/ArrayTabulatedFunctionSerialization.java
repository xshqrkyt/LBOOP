package ru.ssau.tk.samsa.LB2.io;

import java.io.*;
import ru.ssau.tk.samsa.LB2.functions.*;
import ru.ssau.tk.samsa.LB2.functions.factory.*;
import ru.ssau.tk.samsa.LB2.operations.TabulatedDifferentialOperator;

public class ArrayTabulatedFunctionSerialization {
    public static void main (String[] str) {
        try (BufferedOutputStream stream = new BufferedOutputStream(new FileOutputStream("output/serialized array functions.bin"))) {
            ArrayTabulatedFunction f = new ArrayTabulatedFunction(new double[ ]{1.4, 3.5, 4.1, 5.8, 8.9}, new double[] {2.744, 42.875, 68.921, 195.112, 704.969});
            ArrayTabulatedFunction der1 = new TabulatedDifferentialOperator(new ArrayTabulatedFunctionFactory()).derive(f);
            ArrayTabulatedFunction der2 = new TabulatedDifferentialOperator(new ArrayTabulatedFunctionFactory()).derive(der1);

            FunctionsIO.serialize(stream, f);
            FunctionsIO.serialize(stream, der1);
            FunctionsIO.serialize(stream, der2);
        }

        catch (IOException error) {
            error.printStackTrace();
        }

        try (BufferedOutputStream stream = new BufferedOutputStream(new FileOutputStream("output/serialized array functions.bin"))) {
            System.out.println(stream.toString(FunctionsIO.deserialize(stream)));
            System.out.println(stream.toString(FunctionsIO.deserialize(stream)));
            System.out.println(stream.toString(FunctionsIO.deserialize(stream)));
        }

        catch (IOException | ClassNotFoundException error) {
            error.printStackTrace();
        }
    }
}

