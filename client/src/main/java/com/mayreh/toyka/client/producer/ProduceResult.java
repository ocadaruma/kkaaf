package com.mayreh.toyka.client.producer;

import com.mayreh.toyka.TopicPartition;

import java.time.Instant;

public class ProduceResult {
    private final TopicPartition topicPartition;
    private final Instant timestamp;
}
