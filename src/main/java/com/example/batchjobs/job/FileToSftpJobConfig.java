package com.example.batchjobs.job;

import com.example.batchjobs.processor.FileItemProcessor;
import com.example.batchjobs.service.SftpService;
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
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.mapping.PassThroughLineMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;

import java.io.File;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class FileToSftpJobConfig {
    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final SftpService sftpService;

    @Value("${file.local.directory.input}")
    private String inputDir;

    @Bean
    public Job fileToSftpJob() {
        return new JobBuilder("fileToSftpJob", jobRepository)
                .start(fileToSftpStep())
                .build();
    }

    @Bean
    public Step fileToSftpStep() {
        return new StepBuilder("fileToSftpStep", jobRepository)
                .<String, String>chunk(10, transactionManager)
                .reader(fileReader())
                .processor(fileProcessor())
                .writer(sftpWriter())
                .faultTolerant()
                .retry(Exception.class)
                .retryLimit(3)
                .build();
    }

    @Bean
    @StepScope
    public FlatFileItemReader<String> fileReader() {
        FlatFileItemReader<String> reader = new FlatFileItemReader<>();
        File inputDirectory = new File(inputDir);
        File[] files = inputDirectory.listFiles((dir, name) -> name.endsWith(".txt"));
        
        if (files != null && files.length > 0) {
            reader.setResource(new FileSystemResource(files[0]));
            reader.setLineMapper(new PassThroughLineMapper());
        }
        
        return reader;
    }

    private final FileItemProcessor fileItemProcessor;

    @Bean
    @StepScope
    public ItemProcessor<String, String> fileProcessor() {
        return fileItemProcessor;
    }

    @Bean
    @StepScope
    public ItemWriter<String> sftpWriter() {
        return items -> {
            File inputDirectory = new File(inputDir);
            File[] files = inputDirectory.listFiles((dir, name) -> name.endsWith(".txt"));
            
            if (files != null && files.length > 0) {
                String fileName = files[0].getName();
                sftpService.uploadFile(files[0].getAbsolutePath(), fileName);
                
                // Optionally move or delete the processed file
                if (!files[0].delete()) {
                    log.warn("Could not delete processed file: {}", fileName);
                }
            }
        };
    }
}
