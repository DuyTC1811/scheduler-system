package org.scheduler.producer;

import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.scheduler.config.KafkaProducerConfig;

public class KafkaProducerService {
    private static final Logger LOGGER = LogManager.getLogger(KafkaProducerService.class);

    private static final String TOPIC = "scheduler-topic";
    private final Producer<String, String> producer;

    public KafkaProducerService() {
        this.producer = KafkaProducerConfig.createProducer();
    }

    public void sendMessage(String message) {
        String key = "KEY-" + (int) (Math.random() * 10);
        ProducerRecord<String, String> record = new ProducerRecord<>(TOPIC, key, message);
        producer.send(record, (metadata, exception) -> {
            if (exception == null) {
                LOGGER.info(
                        "[ SENT MESSAGE ]::: TOPIC [ {} ] PARTITION [ {} ] OFFSET [ {} ]",
                        metadata.topic(), metadata.partition(), metadata.offset()
                );
            } else {
                exception.printStackTrace(System.err);
            }
        });
    }

    public void close() {
        producer.close();
    }
}
