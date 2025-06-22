package com.example.batchjobs.processor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

import java.util.regex.Pattern;

@Slf4j
@Component
public class FileItemProcessor implements ItemProcessor<String, String> {
    private static final Pattern CSV_PATTERN = Pattern.compile("^[^,]+(,[^,]+)*$");
    private static final int EXPECTED_FIELDS = 5; // Adjust based on your requirements

    @Override
    public String process(String line) throws Exception {
        log.info("Processing line: {}", line);

        // Skip empty lines
        if (line == null || line.trim().isEmpty()) {
            log.warn("Skipping empty line");
            return null;
        }

        // Validate CSV format
        if (!CSV_PATTERN.matcher(line).matches()) {
            log.warn("Invalid CSV format, skipping line: {}", line);
            return null;
        }

        // Validate number of fields
        String[] fields = line.split(",");
        if (fields.length != EXPECTED_FIELDS) {
            log.warn("Invalid number of fields (expected {}, got {}), skipping line: {}", 
                    EXPECTED_FIELDS, fields.length, line);
            return null;
        }

        // Process each field
        StringBuilder processedLine = new StringBuilder();
        for (int i = 0; i < fields.length; i++) {
            String processedField = processField(fields[i].trim());
            processedLine.append(processedField);
            if (i < fields.length - 1) {
                processedLine.append(",");
            }
        }

        String result = processedLine.toString();
        log.info("Processed line: {}", result);
        return result;
    }

    private String processField(String field) {
        // Remove any special characters
        field = field.replaceAll("[^a-zA-Z0-9\\s,.-]", "");
        
        // Convert to uppercase if it's a string field
        if (!field.matches(".*\\d+.*")) {
            field = field.toUpperCase();
        }
        
        // Trim and ensure no empty fields
        field = field.trim();
        return field.isEmpty() ? "N/A" : field;
    }
}
