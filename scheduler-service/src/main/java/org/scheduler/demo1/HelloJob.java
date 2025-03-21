package org.scheduler.demo1;

import org.quartz.Job;
import org.quartz.JobExecutionContext;

import java.util.Date;

public class HelloJob implements Job {
    @Override
    public void execute(JobExecutionContext jobExecutionContext) {
        System.out.println("Hello World " + new Date());
    }
}
