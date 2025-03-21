package org.scheduler.demo1;

import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerFactory;
import org.quartz.Trigger;
import org.quartz.impl.StdSchedulerFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Date;

import static org.quartz.DateBuilder.evenMinuteDate;
import static org.quartz.JobBuilder.newJob;
import static org.quartz.TriggerBuilder.newTrigger;

public class SimpleExample {
    private static final Logger LOGGER = LogManager.getLogger(SimpleExample.class);

    public void run() throws Exception {
        System.out.println("------- Initializing ----------------------");

        System.out.println("Starting Scheduler...");
        // First we must get a reference to a scheduler
        SchedulerFactory sf = new StdSchedulerFactory();
        Scheduler sched = sf.getScheduler();

        System.out.println("------- Initialization Complete -----------");

        // computer a time that is on the next round minute
        Date runTime = evenMinuteDate(new Date());

        System.out.println("------- Scheduling Job  -------------------");

        // define the job and tie it to our HelloJob class
        JobDetail job = newJob(HelloJob.class).withIdentity("job1", "group1").build();

        // Trigger the job to run on the next round minute
        Trigger trigger = newTrigger().withIdentity("trigger1", "group1").startAt(runTime).build();

        // Tell quartz to schedule the job using our trigger
        sched.scheduleJob(job, trigger);
        System.out.println(job.getKey() + " will run at: " + runTime);

        // Start up the scheduler (nothing can actually run until the
        // scheduler has been started)
        sched.start();

        System.out.println("------- Started Scheduler -----------------");

        // wait long enough so that the scheduler as an opportunity to
        // run the job!
        System.out.println("------- Waiting 65 seconds... -------------");
        try {
            // wait 65 seconds to show job
            Thread.sleep(65L * 1000L);
            // executing...
        } catch (Exception e) {
            //
        }

        // shut down the scheduler
        System.out.println("------- Shutting Down ---------------------");
        sched.shutdown(true);
        System.out.println("------- Shutdown Complete -----------------");
    }

    public static void main(String[] args) throws Exception {
        SimpleExample example = new SimpleExample();
        example.run();
    }
}