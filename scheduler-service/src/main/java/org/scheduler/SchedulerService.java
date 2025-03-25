package org.scheduler;

import org.scheduler.read_file.ReadFileCSV;

public class SchedulerService {
    public static void main(String[] args) {
        ReadFileCSV readFileCSV = new ReadFileCSV();
        readFileCSV.readFileAndSM();
    }
}
