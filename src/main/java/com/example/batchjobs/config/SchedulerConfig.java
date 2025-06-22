package com.example.batchjobs.config;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.Scheduled;

@Configuration
@RequiredArgsConstructor
public class SchedulerConfig {
    private final JobLauncher jobLauncher;
    private final Job databaseToSftpJob;
    private final Job fileToSftpJob;
    private final Job sftpToFileJob;

    @Scheduled(cron = "${jobs.scheduler.database-to-sftp}")
    public void runDatabaseToSftpJob() throws Exception {
        JobParameters params = new JobParametersBuilder()
                .addLong("time", System.currentTimeMillis())
                .toJobParameters();
        jobLauncher.run(databaseToSftpJob, params);
    }

    @Scheduled(cron = "${jobs.scheduler.file-to-sftp}")
    public void runFileToSftpJob() throws Exception {
        JobParameters params = new JobParametersBuilder()
                .addLong("time", System.currentTimeMillis())
                .toJobParameters();
        jobLauncher.run(fileToSftpJob, params);
    }

    @Scheduled(cron = "${jobs.scheduler.sftp-to-file}")
    public void runSftpToFileJob() throws Exception {
        JobParameters params = new JobParametersBuilder()
                .addLong("time", System.currentTimeMillis())
                .toJobParameters();
        jobLauncher.run(sftpToFileJob, params);
    }
}
