package com.jeremyyang.custom;

public class BaseBoundedBuffer<V> {

    private final V[] buf;
    private int tail;
    private int head;
    private int count;

    @SuppressWarnings("unchecked")
    protected BaseBoundedBuffer(int capacity) {
        this.buf = (V[]) new Object[capacity];
    }

    /* Non-Thread-Safe */
    protected final void doPut(V v) {
        buf[tail] = v;
        if (++tail == buf.length)
            tail = 0;
        ++count;
    }

    /* Non-Thread-Safe */
    protected final V doTake() {
        V v = buf[head];
        buf[head] = null;   // release reference
        if (++head == buf.length)
            head = 0;
        --count;
        return v;
    }

    /* Non-Thread-Safe */
    protected final boolean checkFull() {
        return count == buf.length;
    }

    /* Non-Thread-Safe */
    protected final boolean checkEmpty() {
        return count == 0;
    }
}
