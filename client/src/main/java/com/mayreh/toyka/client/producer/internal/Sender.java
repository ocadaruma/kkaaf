package com.mayreh.toyka.client.producer.internal;

import com.mayreh.toyka.client.SocketClient;

import java.util.concurrent.atomic.AtomicInteger;

public class Sender extends Thread implements AutoCloseable {
    private static final AtomicInteger sequence = new AtomicInteger(0);
    private final RecordAccumulator accumulator;

    private volatile boolean terminated;
    private SocketClient client;

    public Sender(RecordAccumulator accumulator) {
        this.accumulator = accumulator;

        setName("producer-sender-" + sequence.getAndIncrement());
    }

    @Override
    public void run() {
        client = new SocketClient();

        while (!terminated) {

        }
    }

    @Override
    public void close() {
        terminated = true;

        try {
            join();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(e);
        }
    }
}
