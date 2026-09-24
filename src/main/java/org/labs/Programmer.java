package org.labs;

import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.LongAdder;

public class Programmer {
    private final Spoon leftSpoon;
    private final Spoon rigthSpoon;

    private final int eatDuration;

    // общее число порций, полученных программистом
    private final LongAdder totalSides;
    private final Semaphore hasSide;

    Programmer(Spoon leftSpoon, Spoon rigthSpoon, int eatDuration) {
        this.leftSpoon = leftSpoon;
        this.rigthSpoon = rigthSpoon;
        this.eatDuration = eatDuration;
        this.totalSides = new LongAdder();
        this.hasSide = new Semaphore(0);
    }

    Programmer(Spoon leftSpoon, Spoon rigthSpoon) {
        this(leftSpoon, rigthSpoon, (int) (Math.random() * 10));
    }

    public void putSide() {
        this.hasSide.release();
    }

    public void waitSide() {
        try {
            this.hasSide.acquire();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public void eat() {
        try {
            // пытаемся взять первую ложку и затем пытаемся взять вторую
            if (this.leftSpoon.getId() < this.rigthSpoon.getId()) {
                this.leftSpoon.take();
                this.rigthSpoon.take();
            } else {
                this.rigthSpoon.take();
                this.leftSpoon.take();
            }
            // типо едим
            Thread.sleep(this.eatDuration);
            this.totalSides.add(1);
        } catch (InterruptedException ex) {
            throw new RuntimeException(ex);
        } finally {
            // отпускаем ложки в обратном порядке блокировки
            if (this.leftSpoon.getId() < this.rigthSpoon.getId()) {
                this.rigthSpoon.put();
                this.leftSpoon.put();
            } else {
                this.leftSpoon.put();
                this.rigthSpoon.put();
            }
        }
    }

    public int getEatDuration() {
        return eatDuration;
    }

    public int getTotalSides() {
        return totalSides.intValue();
    }
}