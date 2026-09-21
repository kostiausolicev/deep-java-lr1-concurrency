package org.labs;

public class Programmer {
    private final Spoon leftSpoon;
    private final Spoon rigthSpoon;

    private final int eatDuration;

    // общее число порций, полученных программистом
    private volatile int totalSides;

    private volatile boolean hasSide;

    Programmer(Spoon leftSpoon, Spoon rigthSpoon, int eatDuration) {
        this.leftSpoon = leftSpoon;
        this.rigthSpoon = rigthSpoon;
        this.eatDuration = eatDuration;
        this.totalSides = 0;
        this.hasSide = false;
    }

    Programmer(Spoon leftSpoon, Spoon rigthSpoon) {
        this(leftSpoon, rigthSpoon, (int) (Math.random() * 100));
    }

    public void putSide() {
        this.hasSide = true;
    }

    public boolean hasSide() {
        return this.hasSide;
    }

    public int eat() {
        if (!hasSide) return -1;
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
            return ++this.totalSides;
        } catch (InterruptedException ex) {
            throw new RuntimeException(ex);
        } finally {
            // мы поели и больше у нас нет еды
            this.hasSide = false;
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
        return totalSides;
    }
}