package com.example.batchjobs.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/jobs")
@RequiredArgsConstructor
public class JobController {
    private final JobLauncher jobLauncher;
    private final Job databaseToSftpJob;
    private final Job fileToSftpJob;
    private final Job sftpToFileJob;

    @PostMapping("/database-to-sftp")
    public ResponseEntity<String> triggerDatabaseToSftpJob(@RequestParam(required = false) String param) {
        try {
            JobParameters jobParameters = new JobParametersBuilder()
                    .addLong("time", System.currentTimeMillis())
                    .addString("param", param)
                    .toJobParameters();
            jobLauncher.run(databaseToSftpJob, jobParameters);
            return ResponseEntity.ok("Database to SFTP job triggered successfully");
        } catch (Exception e) {
            log.error("Error triggering database to SFTP job: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body("Error triggering job: " + e.getMessage());
        }
    }

    @PostMapping("/file-to-sftp")
    public ResponseEntity<String> triggerFileToSftpJob(@RequestParam(required = false) String param) {
        try {
            JobParameters jobParameters = new JobParametersBuilder()
                    .addLong("time", System.currentTimeMillis())
                    .addString("param", param)
                    .toJobParameters();
            jobLauncher.run(fileToSftpJob, jobParameters);
            return ResponseEntity.ok("File to SFTP job triggered successfully");
        } catch (Exception e) {
            log.error("Error triggering file to SFTP job: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body("Error triggering job: " + e.getMessage());
        }
    }

    @PostMapping("/sftp-to-file")
    public ResponseEntity<String> triggerSftpToFileJob(@RequestParam(required = false) String param) {
        try {
            JobParameters jobParameters = new JobParametersBuilder()
                    .addLong("time", System.currentTimeMillis())
                    .addString("param", param)
                    .toJobParameters();
            jobLauncher.run(sftpToFileJob, jobParameters);
            return ResponseEntity.ok("SFTP to file job triggered successfully");
        } catch (Exception e) {
            log.error("Error triggering SFTP to file job: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body("Error triggering job: " + e.getMessage());
        }
    }
}
