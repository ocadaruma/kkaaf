package com.mayreh.toyka.client.producer;

import java.util.concurrent.CompletableFuture;

public class StandardProducer<K, V> implements Producer<K, V> {
    @Override
    public CompletableFuture<ProduceResult> send(ProduceRecord<K, V> record) {
        
        return null;
    }
}
