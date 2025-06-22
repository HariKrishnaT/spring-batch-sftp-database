package com.example.batchjobs.job;

import com.example.batchjobs.model.DataRecord;
import com.example.batchjobs.processor.DatabaseItemProcessor;
import com.example.batchjobs.repository.DataRecordRepository;
import com.example.batchjobs.service.SftpService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.data.RepositoryItemReader;
import org.springframework.batch.item.data.builder.RepositoryItemReaderBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.PlatformTransactionManager;

import java.io.FileWriter;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class DatabaseToSftpJobConfig {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final SftpService sftpService;
    private final DataRecordRepository dataRecordRepository;
    
    @Value("${file.local.directory.temp}")
    private String tempDir;

    @Bean
    public Job databaseToSftpJob() {
        return new JobBuilder("databaseToSftpJob", jobRepository)
                .start(databaseToSftpStep())
                .build();
    }

    @Bean
    public Step databaseToSftpStep() {
        return new StepBuilder("databaseToSftpStep", jobRepository)
                .<DataRecord, DataRecord>chunk(10, transactionManager)
                .reader(databaseReader())
                .processor(databaseItemProcessor)
                .writer(csvWriter())
                .faultTolerant()
                .retry(Exception.class)
                .retryLimit(3)
                .build();
    }

    @Bean
    @StepScope
    public RepositoryItemReader<DataRecord> databaseReader() {
        return new RepositoryItemReaderBuilder<DataRecord>()
                .name("databaseReader")
                .repository(dataRecordRepository)
                .methodName("findAll")
                .pageSize(100)
                .sorts(new HashMap<String, Sort.Direction>() {{
                    put("id", Sort.Direction.ASC);
                }})
                .build();
    }

    private final DatabaseItemProcessor databaseItemProcessor;

    @Bean
    @StepScope
    public ItemProcessor<DataRecord, DataRecord> databaseProcessor() {
        return databaseItemProcessor;
    }

    @Bean
    @StepScope
    public ItemWriter<DataRecord> csvWriter() {
        return items -> {
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            String fileName = "data_export_" + timestamp + ".csv";
            Path filePath = Paths.get(tempDir, fileName);

            try (FileWriter fileWriter = new FileWriter(filePath.toFile());
                 CSVPrinter csvPrinter = new CSVPrinter(fileWriter, CSVFormat.DEFAULT.builder()
                         .setHeader("ID", "Field1", "Field2", "Field3", "Created At")
                         .build())) {
                
                for (DataRecord record : items) {
                    csvPrinter.printRecord(
                            record.getId(),
                            record.getField1(),
                            record.getField2(),
                            record.getField3(),
                            record.getCreatedAt()
                    );
                }
                csvPrinter.flush();
                
                // Upload to SFTP
                sftpService.uploadFile(filePath.toString(), fileName);
            } catch (Exception e) {
                log.error("Error writing CSV file: {}", e.getMessage(), e);
                throw new RuntimeException("Failed to write CSV file", e);
            }
        };
    }
}
