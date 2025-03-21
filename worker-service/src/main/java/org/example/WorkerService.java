package org.example;

import org.example.consumer.KafkaConsumerService;

public class WorkerService {
    public static void main(String[] args) {
        KafkaConsumerService consumer = new KafkaConsumerService();
        consumer.consumeMessages();
    }
}