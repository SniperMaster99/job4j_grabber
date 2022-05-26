package ru.job4j.quartz;

import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import static org.quartz.SimpleScheduleBuilder.simpleSchedule;
import static org.quartz.TriggerBuilder.newTrigger;

public class AlertRabbit {
    public static void main(String[] args) {
        Properties properties = new Properties();
        try (InputStream stream = AlertRabbit.class.getClassLoader().getResourceAsStream("app.properties")) {
            try (Connection connection = AlertRabbit.connections(properties)) {
                properties.load(stream);
                List<Long> store = new ArrayList<>();
                Scheduler scheduler = StdSchedulerFactory.getDefaultScheduler();
                scheduler.start();
                JobDataMap data = new JobDataMap();
                data.put("store", store);
                data.put("connection", connection);
                JobDetail job = JobBuilder.newJob(Rabbit.class).usingJobData(data).build();
                SimpleScheduleBuilder times = simpleSchedule()
                        .withIntervalInSeconds(Integer.parseInt(reading()))
                        .repeatForever();
                Trigger trigger = newTrigger()
                        .startNow()
                        .withSchedule(times)
                        .build();
                scheduler.scheduleJob(job, trigger);
                Thread.sleep(10000);
                scheduler.shutdown();
                System.out.println(store);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        } catch (IOException | SchedulerException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static class Rabbit implements Job {

        public Rabbit() {
            System.out.println(hashCode());
        }

        @Override
        public void execute(JobExecutionContext jobExecutionContext) {
            System.out.println("Rabbit tuns here ...");
            List<Long> store = (List<Long>) jobExecutionContext.getJobDetail().getJobDataMap().get("store");
            Connection connection = (Connection) jobExecutionContext.getJobDetail().getJobDataMap().get("connection");
            store.add(System.currentTimeMillis());
        }
    }

    public static String reading() throws IOException {
        Properties properties = new Properties();
        try (InputStream input = AlertRabbit.class
                .getClassLoader()
                .getResourceAsStream("rabbit.properties")) {
            properties.load(input);
            return properties.getProperty("rabbit.interval");
        }
    }

    public static Connection connections(Properties properties) throws SQLException {
        return  DriverManager.getConnection(properties.getProperty("url"),
                properties.getProperty("login"),
                properties.getProperty("password"));
    }
}

