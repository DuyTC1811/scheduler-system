package org.scheduler.read_file;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.scheduler.config.YamlConfigLoader;
import org.scheduler.producer.KafkaProducerService;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;


public class ReadFileCSV {
    private final String csvFilePath = "mobiles_dataset_2025.csv";
    private final ObjectMapper objectMapper = new ObjectMapper();

    private static final String[] HEADERS = {
            "company_name", "model_name", "weight", "ram", "camera", "processor",
            "battery_capacity", "screen_size", "launched_price_pakistan",
            "launched_price_india", "launched_price_china", "launched_price_usa",
            "launched_price_dubai", "launched_year"
    };

    public void readFileAndSM() {
        KafkaProducerService producerService = new KafkaProducerService();

        long startTime = System.nanoTime(); // Bắt đầu đo thời gian
        try (InputStream input = YamlConfigLoader.class.getClassLoader().getResourceAsStream(csvFilePath)) {
            if (input == null) {
                throw new RuntimeException("Data file not found: " + csvFilePath);
            }

            try (BufferedReader br = new BufferedReader(new InputStreamReader(input))) {
                br.readLine(); // bo dong nay
                String line;
                while ((line = br.readLine()) != null) {
                    producerService.sendMessage(objectMapper.writeValueAsString(line));
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
