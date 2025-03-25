package org.example.consumer;

import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.errors.WakeupException;

import javax.sql.DataSource;
import java.time.Duration;
import java.util.Collections;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.example.config.DataSourceConfig.getDataSource;
import static org.example.config.KafkaConsumerConfig.createConsumer;
import static org.example.config.KafkaConsumerConfig.createProducer;

public class KafkaConsumerService {
    private static final String TOPIC = "scheduler-topic";
    private static final String DLQ_TOPIC = "scheduler-topic-dlq";
    private static final int MAX_RETRY = 3;

    private final AtomicBoolean running = new AtomicBoolean(true);
    private final Consumer<String, String> consumer;
    private final Producer<String, String> dlqProducer;
    private final ExecutorService executorService;

    public KafkaConsumerService() {
        this.consumer = createConsumer();
        this.consumer.subscribe(Collections.singletonList(TOPIC));
        this.dlqProducer = createProducer();
        this.executorService = Executors.newVirtualThreadPerTaskExecutor();

        Runtime.getRuntime().addShutdownHook(new Thread(this::shutdown));
    }

    public void consumeMessages() {
        try {
            while (running.get()) {
                ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(1000));

                if (records.isEmpty()) continue;

                for (ConsumerRecord<String, String> record : records) {
                    executorService.submit(() -> processWithRetry(record));
                }
                consumer.commitSync();
            }
        } catch (WakeupException e) {
            // Đây là ngoại lệ bình thường khi gọi consumer.wakeup()
            System.out.println("Consumer is shutting down...");
        } finally {
            consumer.close();
            dlqProducer.close();
            executorService.shutdown();
            System.out.println("Consumer closed gracefully.");
        }

    }

    /**
     * Xử lý 1 record, retry tối đa 3 lần.
     * Nếu vẫn thất bại thì gửi sang DLQ (ví dụ là 1 topic khác).
     */
    private void processWithRetry(ConsumerRecord<String, String> record) {
        long startTime = System.nanoTime(); // Bắt đầu đo thời gian
        int attempt = 0;
        while (attempt < MAX_RETRY) {
            // Gọi hàm xử lý chính
            boolean processed = processMessage(record);
            if (processed) {
                long endTime = System.nanoTime(); // Kết thúc thời gian
                long durationMillis = (endTime - startTime) / 1_000_000;
                System.out.printf("[ SUCCESS ] Offset %d processed in %d ms (retry %d)%n",
                        record.offset(), durationMillis, attempt);
                return;
            } else {
                attempt++;
                System.err.printf("[ RETRY %d/%d ] Failed to process message at offset %d%n",
                        attempt, MAX_RETRY, record.offset());
                try {
                    Thread.sleep(1000); // Delay giữa các lần retry
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }

        // Nếu quá 3 lần vẫn fail => gửi vào DLQ
        sendToDLQ(record);
    }

    private boolean insertDataBase(ConsumerRecord<String, String> record) {
        DataSource dataSource = getDataSource();

        return false;
    }

    private boolean processMessage(ConsumerRecord<String, String> record) {
        String threadName = Thread.currentThread().getName();
        System.out.printf(
                "%s [ PROCESSING ] key=%s, value=%s, partition=%d, offset=%d%n",
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

    private void shutdown() {
        running.set(false); // Báo hiệu vòng lặp dừng lại
        consumer.wakeup(); // Đánh thức consumer để thoát khỏi poll() ngay lập tức
    }
}
