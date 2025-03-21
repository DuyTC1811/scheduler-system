package org.scheduler.config;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;

import java.util.Properties;

import static org.scheduler.config.YamlConfigLoader.getProperty;

public class KafkaProducerConfig {
    public static Producer<String, String> createProducer() {
        Properties props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, getProperty("kafka.bootstrapServers"));
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        props.put(ProducerConfig.ACKS_CONFIG, "all"); // Đảm bảo dữ liệu an toàn
        props.put(ProducerConfig.RETRIES_CONFIG, 3); // Tự động retry khi gặp lỗi
        props.put(ProducerConfig.BATCH_SIZE_CONFIG, 16384); // Batch size tối ưu
        props.put(ProducerConfig.BUFFER_MEMORY_CONFIG, 33554432); // Bộ nhớ buffer

        return new KafkaProducer<>(props);
    }
}
