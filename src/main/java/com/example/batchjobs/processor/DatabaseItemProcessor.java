package com.example.batchjobs.processor;

import com.example.batchjobs.model.DataRecord;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Slf4j
@Component
public class DatabaseItemProcessor implements ItemProcessor<DataRecord, DataRecord> {

    @Override
    public DataRecord process(DataRecord record) throws Exception {
        log.info("Processing database record: {}", record.getId());
        
        // Validate data
        if (record.getField1() == null || record.getField1().isEmpty()) {
            log.warn("Skipping record {} due to empty field1", record.getId());
            return null; // Skip this record
        }

        // Transform data
        DataRecord processedRecord = new DataRecord();
        processedRecord.setId(record.getId());
        processedRecord.setField1(record.getField1().toUpperCase()); // Convert to uppercase
        processedRecord.setField2(sanitizeField(record.getField2())); // Clean field2
        processedRecord.setField3(enrichField(record.getField3())); // Enrich field3
        processedRecord.setCreatedAt(record.getCreatedAt());

        log.info("Processed database record: {}", processedRecord.getId());
        return processedRecord;
    }

    private String sanitizeField(String field) {
        if (field == null) return "";
        // Remove special characters and trim
        return field.replaceAll("[^a-zA-Z0-9\\s]", "").trim();
    }

    private String enrichField(String field) {
        if (field == null) return "";
        // Add timestamp to the field
        return field + "_" + LocalDateTime.now().toString();
    }
}
