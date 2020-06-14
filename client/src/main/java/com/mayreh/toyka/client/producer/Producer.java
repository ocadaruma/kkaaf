package com.mayreh.toyka.client.producer;

import java.util.concurrent.CompletableFuture;

public interface Producer<K, V> {

    CompletableFuture<ProduceResult> send(ProduceRecord<K, V> record);
}
