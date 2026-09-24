package org.labs;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

public class Table {
    private final int seatsCount;
    private final int waitersCount;
    private final int threshold;
    private final Programmer[] allProgrammers;
    private final AtomicInteger sides;
    private final BlockingQueue<Programmer> queue;
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
        this.queue = new LinkedBlockingQueue<>();
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
                programmer = new Programmer(ls, rs);
            } else {
                programmer = new Programmer(ls, rs, fixedEatDuration);
            }
            allProgrammers[p] = programmer;
            queue.add(programmer);
        }
        try (ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor()) {
            for (Programmer p : allProgrammers) {
                executor.submit(() -> {
                    while (sides.get() > 0) {
                        p.waitSide();
                        if (!tryReserveSide()) {
                            return;
                        }
                        p.eat();
                        queue.add(p);
                    }
                });
            }

            for (int i = 0; i < waitersCount; i++) {
                executor.submit(() -> {
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
            try {
                p = queue.take();
                int currentMinimal = Integer.MAX_VALUE;
                for (Programmer p1 : allProgrammers) {
                    currentMinimal = Math.min(currentMinimal, p1.getTotalSides());
                }
                if (sides.get() > 0 && p.getTotalSides() - currentMinimal >= this.threshold) {
                    queue.add(p);
                    continue;
                }
                break;
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        return p;
    }
}
