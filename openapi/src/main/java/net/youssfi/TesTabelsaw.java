package net.youssfi;

import tech.tablesaw.api.DoubleColumn;

public class TesTabelsaw {
    public static void main(String[] args) {
        double[] numbers = {1, 2, 3, 4};
        DoubleColumn nc = DoubleColumn.create("nc", numbers);
        System.out.println(nc.print());
    }
}
