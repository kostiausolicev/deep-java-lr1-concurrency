package org.labs;

import java.util.Arrays;
import java.util.Comparator;

public class Main {
    public static void main(String[] args) {
        final Configuration conf = Configuration.load();
        Table table = new Table(conf.seats(), conf.waiters(), conf.threshold(), conf.sides());
        Programmer[] programmers = table.start();
        Arrays.stream(programmers)
                .sorted(Comparator.comparingInt(Programmer::getTotalSides).reversed())
                .forEach(it -> System.out.printf("%d: %d\n", it.getTotalSides(), it.getEatDuration()));
        System.out.println((Integer) Arrays.stream(programmers).mapToInt(Programmer::getTotalSides).sum());
    }
}
