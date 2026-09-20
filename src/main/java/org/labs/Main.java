package org.labs;

import java.util.Arrays;
import java.util.Comparator;

public class Main {
    public static void main(String[] args) {
        final int seatsCount = 7;
        final int waiterCount = 2;
        Table table = new Table(seatsCount, waiterCount, 5, 5_000);
        Programmer[] programmers = table.start();
        Arrays.stream(programmers)
                .sorted(Comparator.comparingInt(Programmer::getTotalSides).reversed())
                .forEach(it -> System.out.printf("%d: %d\n", it.getTotalSides(), it.getEatDuration()));
    }
}
