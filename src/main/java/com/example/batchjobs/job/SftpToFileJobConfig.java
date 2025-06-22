package com.example.batchjobs.job;

import com.example.batchjobs.processor.SftpItemProcessor;
import com.example.batchjobs.service.SftpService;
import com.jcraft.jsch.ChannelSftp;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class SftpToFileJobConfig {
    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final SftpService sftpService;
    private final ChannelSftp channelSftp;

    @Value("${sftp.remote.directory.input}")
    private String remoteInputDir;

    @Value("${file.local.directory.output}")
    private String outputDir;

    @Bean
    public Job sftpToFileJob() {
        return new JobBuilder("sftpToFileJob", jobRepository)
                .start(sftpToFileStep())
                .build();
    }

    @Bean
    public Step sftpToFileStep() {
        return new StepBuilder("sftpToFileStep", jobRepository)
                .<String, String>chunk(10, transactionManager)
                .reader(sftpReader())
                .processor(sftpProcessor())
                .writer(fileWriter())
                .faultTolerant()
                .retry(Exception.class)
                .retryLimit(3)
                .build();
    }

    @Bean
    @StepScope
    public ItemReader<String> sftpReader() {
        return new ItemReader<String>() {
            private List<String> files;
            private int currentIndex = 0;

            @Override
            public String read() {
                try {
                    if (files == null) {
                        files = new ArrayList<>();
                        @SuppressWarnings("unchecked")
                        Vector<ChannelSftp.LsEntry> entries = (Vector<ChannelSftp.LsEntry>) channelSftp.ls(remoteInputDir);
                        for (ChannelSftp.LsEntry entry : entries) {
                            if (!entry.getAttrs().isDir()) {
                                files.add(entry.getFilename());
                            }
                        }
                    }

                    if (currentIndex < files.size()) {
                        return files.get(currentIndex++);
                    }
                } catch (Exception e) {
                    log.error("Error reading from SFTP: {}", e.getMessage(), e);
                    throw new RuntimeException("Failed to read from SFTP", e);
                }
                return null;
            }
        };
    }

    private final SftpItemProcessor sftpItemProcessor;

    @Bean
    @StepScope
    public ItemProcessor<String, String> sftpProcessor() {
        return sftpItemProcessor;
    }

    @Bean
    @StepScope
    public ItemWriter<String> fileWriter() {
        return items -> {
            for (String fileName : items) {
                Path localPath = Paths.get(outputDir, fileName);
                sftpService.downloadFile(fileName, localPath.toString());
                log.info("Downloaded file {} to {}", fileName, localPath);
            }
        };
    }
}
