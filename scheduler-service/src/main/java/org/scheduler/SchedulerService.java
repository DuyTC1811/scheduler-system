package org.scheduler;

import org.scheduler.producer.KafkaProducerService;

import java.util.ArrayList;
import java.util.List;

public class SchedulerService {
    public static void main(String[] args) {
        List<String> messenger = List.of("ok");
        KafkaProducerService producer = new KafkaProducerService();
        for (String s : messenger) {
            producer.sendMessage(s);
        }
        producer.close();
    }
}
