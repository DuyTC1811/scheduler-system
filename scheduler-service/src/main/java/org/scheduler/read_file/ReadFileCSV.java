package org.scheduler.read_file;

import org.scheduler.producer.KafkaProducerService;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;


public class ReadFileCSV {
    private static final String CSV_FILE_PATH = "mobiles_dataset_2025.csv";
    private static final String CSV_FILE_PATH1 = "scheduler-service/src/main/resources/2019.csv";

    public void readFileAndSM() {
        KafkaProducerService producerService = new KafkaProducerService();

        long startTime = System.nanoTime(); // Bắt đầu đo thời gian
        try (InputStream input = ReadFileCSV.class.getClassLoader().getResourceAsStream(CSV_FILE_PATH)) {
            if (input == null) {
                throw new RuntimeException("Data file not found: " + CSV_FILE_PATH);
            }

            try (BufferedReader br = new BufferedReader(new InputStreamReader(input))) {
                br.readLine(); // bo dong nay
                String line;
                while ((line = br.readLine()) != null) {
                    producerService.sendMessage(line);
                }
                producerService.close(); // Đảm bảo đóng producer sau khi gửi xong
            }
        } catch (IOException e) {
            throw new RuntimeException("Error reading CSV file", e);
        }
        long endTime = System.nanoTime(); // Kết thúc đo thời gian
        long durationMillis = (endTime - startTime) / 1_000_000;

        System.out.println("⏱️ Tổng thời gian gửi: " + durationMillis + " ms");
    }
}
