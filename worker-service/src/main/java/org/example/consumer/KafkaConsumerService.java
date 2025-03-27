package org.example.consumer;

import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.errors.WakeupException;
import org.example.model.Mobiles;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.example.config.DataSourceConfig.getDataSource;
import static org.example.config.KafkaConsumerConfig.createConsumer;
import static org.example.config.KafkaConsumerConfig.createProducer;

public class KafkaConsumerService {
    private static final String TOPIC = "scheduler-topic";
    private static final String DLQ_TOPIC = "scheduler-topic-dlq";
    private static final int BATCH_SIZE = 300;

    private final AtomicBoolean running = new AtomicBoolean(true);
    private final Consumer<String, String> consumer;
    private final Producer<String, String> dlqProducer;

    public KafkaConsumerService() {
        this.consumer = createConsumer();
        this.consumer.subscribe(Collections.singletonList(TOPIC));
        this.dlqProducer = createProducer();
    }

    public void consumeMessages() {

        List<ConsumerRecord<String, String>> batch = new ArrayList<>(BATCH_SIZE);

        try {
            while (running.get()) {
                ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(1000));

                for (ConsumerRecord<String, String> record : records) {
                    batch.add(record);

                    if (batch.size() >= BATCH_SIZE) {
                        processBatch(batch);
                        consumer.commitSync();
                        batch.clear();
                    }
                }
            }

            // Xử lý batch cuối khi consumer dừng
            if (!batch.isEmpty()) {
                processBatch(batch);
                consumer.commitSync();
                batch.clear();
            }

        } catch (WakeupException e) {
            System.out.println("Consumer is shutting down...");
        } finally {
            consumer.close();
            dlqProducer.close();
            System.out.println("Consumer closed gracefully.");
        }
    }

    public static Mobiles parseMobilesFromCsvLine(String csvLine) {
        Mobiles mobile = new Mobiles();
        String[] fields = csvLine.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)");
        mobile.setBrand(fields[0]);
        mobile.setModel(fields[1]);
        mobile.setWeight(fields[2]);
        mobile.setRam(fields[3]);
        mobile.setFrontCamera(fields[4]);
        mobile.setBackCamera(fields[5]);
        mobile.setChip(fields[6]);
        mobile.setBattery(cleanQuotes(fields[7]));
        mobile.setDisplaySize(fields[8]);
        mobile.setPricePKR(cleanQuotes(fields[9]));
        mobile.setPriceINR(cleanQuotes(fields[10]));
        mobile.setPriceCNY(cleanQuotes(fields[11]));
        mobile.setPriceUSD(cleanQuotes(fields[12]));
        mobile.setPriceAED(cleanQuotes(fields[13]));
        mobile.setReleaseYear(fields[14]);
        return mobile;
    }

    private static String cleanQuotes(String input) {
        return input != null ? input.replaceAll("^\"|\"$", "").trim() : null;
    }


    private void processBatch(List<ConsumerRecord<String, String>> records) {
        DataSource dataSource = getDataSource();
        String sql = "INSERT INTO mobiles (" +
                "brand, model, weight, ram, main_camera, front_camera, processor, " +
                "battery_capacity, screen_size, price_pkr, price_inr, price_cny, price_usd, price_aed, release_year" +
                ") VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            for (ConsumerRecord<String, String> record : records) {
                Mobiles mobiles = parseMobilesFromCsvLine(record.value());

                ps.setString(1, mobiles.getBrand());
                ps.setString(2, mobiles.getModel());
                ps.setString(3, mobiles.getWeight());
                ps.setString(4, mobiles.getRam());
                ps.setString(5, mobiles.getFrontCamera());
                ps.setString(6, mobiles.getBackCamera());
                ps.setString(7, mobiles.getChip());
                ps.setString(8, mobiles.getBattery());
                ps.setString(9, mobiles.getDisplaySize());
                ps.setString(10, mobiles.getPricePKR());
                ps.setString(11, mobiles.getPriceINR());
                ps.setString(12, mobiles.getPriceCNY());
                ps.setString(13, mobiles.getPriceUSD());
                ps.setString(14, mobiles.getPriceAED());
                ps.setString(15, mobiles.getReleaseYear());

                ps.addBatch();
            }

            ps.executeBatch();

            System.out.printf("[ BATCH SUCCESS ] Processed %d records successfully.%n", records.size());

        } catch (SQLException e) {
            e.printStackTrace(System.err);
            System.err.println("[ BATCH ERROR ] Sending batch to DLQ immediately.");

            // Nếu batch lỗi, gửi từng record sang DLQ ngay
            for (ConsumerRecord<String, String> record : records) {
                sendToDLQ(record);
            }
        }
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
