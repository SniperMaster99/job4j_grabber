package ru.job4j.grabber.utils;

import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;
import ru.job4j.grabber.HabrCareerParse;
import ru.job4j.quartz.Post;
import ru.job4j.quartz.PsqlStore;

import static org.quartz.JobBuilder.newJob;
import static org.quartz.TriggerBuilder.newTrigger;

import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.Properties;

public class Grabber implements Grab {
    private final Properties config = new Properties();
    private PsqlStore psqlStore;

    public PsqlStore store() throws SQLException {
        return psqlStore = new PsqlStore(config);
    }

    public Scheduler scheduler() throws SchedulerException {
        Scheduler scheduler = StdSchedulerFactory.getDefaultScheduler();
        scheduler.start();
        return  scheduler;
    }

    public void configure() throws IOException {
        try(InputStream inputStream = Grabber.class.getClassLoader()
                .getResourceAsStream("app.properties")) {
            config.load(inputStream);
        }
    }

    @Override
    public void init(Parse parse, Store store, Scheduler scheduler) throws SchedulerException {
        JobDataMap map = new JobDataMap();
        map.put("store", store);
        map.put("parse", parse);
        JobDetail jobDetail = newJob(GrabJob.class).usingJobData(map).build();
        SimpleScheduleBuilder builder = SimpleScheduleBuilder
                .simpleSchedule().
                withIntervalInSeconds(Integer.parseInt(config.getProperty("time")))
                .repeatForever();
        Trigger trigger = newTrigger().startNow().withSchedule(builder).build();
        scheduler.scheduleJob(trigger);
    }

    public static class GrabJob implements Job {
        @Override
        public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
            JobDataMap map = jobExecutionContext.getJobDetail().getJobDataMap();
            HabrCareerParse habr = new HabrCareerParse();
            Store store = (Store) map.get("store");
            Parse parse = (Parse) map.get("parse");

        }
    }
}
