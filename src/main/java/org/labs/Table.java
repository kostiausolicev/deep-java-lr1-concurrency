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

    public Table(int seatsCount, int waitersCount, int threshold, int sides) {
        this.seatsCount = seatsCount;
        this.waitersCount = waitersCount;
        this.threshold = threshold;
        this.sides = new AtomicInteger(sides);
        this.allProgrammers = new Programmer[seatsCount];
        this.queue = new ConcurrentLinkedQueue<>();
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
            Programmer programmer = new Programmer(p, ls, rs);
            allProgrammers[p] = programmer;
            queue.add(programmer);
        }

        try (ExecutorService executor = Executors.newFixedThreadPool(seatsCount + waitersCount)) {
            for (int i = 0; i < seatsCount; i++) {
                final Programmer p = allProgrammers[i];
                executor.submit(() -> {
                    while (sides.get() > 0 || p.hasSide()) {
                        int eatCount = p.eat();
                        if (eatCount < 0) continue;
                        updateProgrammerSides(p);
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
                        sides.decrementAndGet();
                    }
                });
            }
        }

        return allProgrammers;
    }

    private void updateProgrammerSides(Programmer p) {

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
            // если он съел на threshold больше текущего минимального - возвращаем его в конец очереди
            if (p.getTotalSides() - currentMinimal > this.threshold) {
                queue.add(p);
                continue;
            }
            break;
        }
        return p;
    }
}
