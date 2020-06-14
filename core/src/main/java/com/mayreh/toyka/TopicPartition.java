package com.mayreh.toyka;

import java.io.Serializable;
import java.util.Objects;

public class TopicPartition implements Serializable {
    private static final long serialVersionUID = -2481683359447011546L;

    private final String topic;
    private final int partition;

    public TopicPartition(String topic, int partition) {
        this.topic = topic;
        this.partition = partition;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        TopicPartition other = (TopicPartition) o;
        return partition == other.partition &&
                topic.equals(other.topic);
    }

    @Override
    public int hashCode() {
        return Objects.hash(topic, partition);
    }

    @Override
    public String toString() {
        return topic + '-' + partition;
    }
}
