package org.example.consumer;

import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;

import java.time.Duration;
import java.util.Collections;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.example.config.KafkaConsumerConfig.createConsumer;
import static org.example.config.KafkaConsumerConfig.createProducer;

public class KafkaConsumerService {
    private static final String TOPIC = "scheduler-topic";
    private static final String DLQ_TOPIC = "scheduler-topic-dlq";
    private static final int MAX_RETRY = 3;

    private final Consumer<String, String> consumer;
    private final Producer<String, String> dlqProducer;
    private final ExecutorService executorService;

    public KafkaConsumerService() {
        this.consumer = createConsumer();
        this.consumer.subscribe(Collections.singletonList(TOPIC));
        this.dlqProducer = createProducer();
        this.executorService = Executors.newFixedThreadPool(10); // Tạo 10 thread để xử lý message
    }

    public void consumeMessages() {
        while (true) {
            ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(1000));

            if (records.isEmpty()) continue;


            for (ConsumerRecord<String, String> record : records) {
                executorService.submit(() -> processWithRetry(record));
            }
            consumer.commitSync();
        }
    }

    private boolean processWithRetry(ConsumerRecord<String, String> record) {
        int attempt = 0;
        while (attempt < MAX_RETRY) {

            boolean isProcess = processMessage(record);
            if (!isProcess) {
                attempt++;
                System.err.printf("[ RETRY %d/%d ] Failed to process message at offset %d: %n",
                        attempt, MAX_RETRY, record.offset());
            }
        }
        // Retry thất bại, gửi message vào DLQ
        sendToDLQ(record);
        return true; // Trả về true để vẫn commit offset (tránh lặp vô hạn)
    }

    private boolean processMessage(ConsumerRecord<String, String> record) {
        String threadName = Thread.currentThread().getName();
        System.out.printf(
                "[ THREAD: %s ] [ PROCESSING ] key=%s, value=%s, partition=%d, offset=%d%n",
                threadName, record.key(), record.value(), record.partition(), record.offset()
        );

        // Giả lập lỗi để test retry
        return !record.value().contains("fail");
    }

    private void sendToDLQ(ConsumerRecord<String, String> record) {
        ProducerRecord<String, String> dlqRecord = new ProducerRecord<>(
                DLQ_TOPIC,
                record.key(),
                record.value()
        );

        dlqProducer.send(dlqRecord, (metadata, exception) -> {
            if (exception == null) {
                System.err.printf("[ DLQ ] Sent message to DLQ: key=%s, value=%s, partition=%d, offset=%d%n",
                        record.key(), record.value(), metadata.partition(), metadata.offset());
            } else {
                System.err.printf("[ DLQ ERROR ] Failed to send message to DLQ: %s%n", exception.getMessage());
            }
        });
    }

}
