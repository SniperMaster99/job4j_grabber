package ru.job4j.quartz;

import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Properties;

import static org.quartz.SimpleScheduleBuilder.simpleSchedule;
import static org.quartz.TriggerBuilder.newTrigger;

public class AlertRabbit {
    public static void main(String[] args) throws IOException, SchedulerException, InterruptedException, SQLException {
        Properties properties = reading();
        Connection connection = connections(properties);
        Scheduler scheduler = StdSchedulerFactory.getDefaultScheduler();
        scheduler.start();
        JobDataMap data = new JobDataMap();
        data.put("connection", connection);
        JobDetail job = JobBuilder.newJob(Rabbit.class).usingJobData(data).build();
        SimpleScheduleBuilder times = simpleSchedule()
                .withIntervalInSeconds(Integer.parseInt("10"))
                .repeatForever();
        Trigger trigger = newTrigger()
                .startNow()
                .withSchedule(times)
                .build();
        scheduler.scheduleJob(job, trigger);
        Thread.sleep(10000);
        scheduler.shutdown();
    }

    public static class Rabbit implements Job {

        public Rabbit() {
            System.out.println(hashCode());
        }

        @Override
        public void execute(JobExecutionContext jobExecutionContext) {
            Connection connection = (Connection) jobExecutionContext.getJobDetail().getJobDataMap().get("connection");
            try {
                PreparedStatement statement = connection.
                        prepareStatement("insert into rabbit (create_date) value ('17/12/2015', 'DD/MM/YYYY')");
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public static Properties reading() throws IOException {
        Properties properties = new Properties();
        try (InputStream input = AlertRabbit.class
                .getClassLoader()
                .getResourceAsStream("rabbit.properties")) {
            properties.load(input);
            return properties;
        }
    }

    public static Connection connections(Properties properties) throws SQLException {
        return  DriverManager.getConnection(properties.getProperty("url"),
                properties.getProperty("login"),
                properties.getProperty("password"));
    }
}

