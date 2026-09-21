package org.labs;

import java.util.Arrays;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

public class Table {
    private final int seatsCount;
    private final int waitersCount;
    private final int threshold;
    private final Programmer[] allProgrammers;
    private final AtomicInteger sides;
    private final Queue<Programmer> queue;
    private final int fixedEatDuration;

    public Table(int seatsCount, int waitersCount, int threshold, int sides, int eatDuration) {
        if (seatsCount <= 2) throw new IllegalArgumentException("Число мест за столом должно быть больше 2-х");
        if (waitersCount <= 0) throw new IllegalArgumentException("Число официантов должно быть больше 0");
        if (threshold <= 0) throw new IllegalArgumentException("Порог должен быть положительным");
        this.seatsCount = seatsCount;
        this.waitersCount = waitersCount;
        this.threshold = threshold;
        this.sides = new AtomicInteger(sides);
        this.allProgrammers = new Programmer[seatsCount];
        this.queue = new ConcurrentLinkedQueue<>();
        this.fixedEatDuration = eatDuration;
    }

    public Table(int seatsCount, int waitersCount, int threshold, int sides) {
        this(seatsCount, waitersCount, threshold, sides, -1);
    }

    public Programmer[] start() {
        Spoon[] spoons = new Spoon[seatsCount];
        for (int s = 0; s < seatsCount; s++) {
            Spoon newSpoon = new Spoon(s);
            spoons[s] = newSpoon;
        }

        for (int p = 0; p < seatsCount; p++) {
            Spoon ls = spoons[p];
            Spoon rs = spoons[(p + 1) % seatsCount];
            Programmer programmer;
            if (fixedEatDuration == -1) {
                programmer = new Programmer(p, ls, rs);
            } else {
                programmer = new Programmer(p, ls, rs, fixedEatDuration);
            }
            allProgrammers[p] = programmer;
            queue.add(programmer);
        }
        try (
                ExecutorService programmersExecutor = Executors.newFixedThreadPool(seatsCount);
                ExecutorService waitersExecutor = Executors.newFixedThreadPool(waitersCount)
        ) {
            for (Programmer p : allProgrammers) {
                programmersExecutor.submit(() -> {
                    while (sides.get() > 0) {
                        if (!p.hasSide()) continue;
                        if (tryReserveSide()) {
                            p.eat();
                            queue.add(p);
                        }
                    }
                });
            }

            for (int i = 0; i < waitersCount; i++) {
                waitersExecutor.submit(() -> {
                    while (sides.get() > 0) {
                        Programmer p = getWaitProgrammer();
                        if (p == null) continue;
                        p.putSide();
                    }
                });
            }
        }

        return allProgrammers;
    }

    private boolean tryReserveSide() {
        int remaining;
        do {
            remaining = sides.get();
            if (remaining <= 0) {
                return false;
            }
        } while (!sides.compareAndSet(remaining, remaining - 1));

        return true;
    }

    private Programmer getWaitProgrammer() {
        Programmer p;
        while (true) {
            p = queue.poll();
            if (p == null) return null;
            int currentMinimal = Arrays.stream(allProgrammers)
                    .map(Programmer::getTotalSides)
                    .min(Integer::compareTo)
                    .orElse(0);
            if (sides.get() > 0 && p.getTotalSides() - currentMinimal >= this.threshold) {
                queue.add(p);
                continue;
            }
            break;
        }
        return p;
    }
}
