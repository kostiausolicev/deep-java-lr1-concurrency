package org.labs;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class Spoon {
    private final int id;
    private final ReentrantLock mx;
    private final Condition taken;

    public Spoon(int id) {
        this.id = id;
        this.mx = new ReentrantLock(true);
        this.taken = this.mx.newCondition();
    }

    public ReentrantLock getMx() {
        return mx;
    }

    public void take() {
//        try {
//            this.taken.await();
//        } catch (InterruptedException e) {
//            throw new RuntimeException(e);
//        }
        this.mx.lock();
    }

    public void put() {
        this.mx.unlock();
//        this.taken.signal();
    }

    public int getId() {
        return id;
    }
}
