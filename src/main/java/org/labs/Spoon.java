package org.labs;

import java.util.concurrent.locks.ReentrantLock;

public class Spoon {
    private final int id;
    private final ReentrantLock mx;

    public Spoon(int id) {
        this.id = id;
        this.mx = new ReentrantLock(true);
    }

    public ReentrantLock getMx() {
        return mx;
    }

    public void take() {
        this.mx.lock();
    }

    public void put() {
        this.mx.unlock();
    }

    public int getId() {
        return id;
    }
}
