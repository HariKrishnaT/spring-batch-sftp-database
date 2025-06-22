package com.example.batchjobs.processor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

import java.util.zip.CRC32;

@Slf4j
@Component
public class SftpItemProcessor implements ItemProcessor<String, String> {
    private static final String ALLOWED_FILE_EXTENSIONS = ".(csv|txt|dat)$";
    private static final long MAX_FILE_NAME_LENGTH = 100;

    @Override
    public String process(String fileName) throws Exception {
        log.info("Processing SFTP file: {}", fileName);

        // Skip if filename is null or empty
        if (fileName == null || fileName.trim().isEmpty()) {
            log.warn("Skipping empty filename");
            return null;
        }

        // Validate file extension
        if (!fileName.toLowerCase().matches(ALLOWED_FILE_EXTENSIONS)) {
            log.warn("Invalid file extension, skipping file: {}", fileName);
            return null;
        }

        // Validate filename length
        if (fileName.length() > MAX_FILE_NAME_LENGTH) {
            log.warn("Filename too long, skipping file: {}", fileName);
            return null;
        }

        // Process filename
        String processedFileName = processFileName(fileName);
        log.info("Processed filename: {}", processedFileName);
        return processedFileName;
    }

    private String processFileName(String fileName) {
        // Add checksum to filename
        String nameWithoutExt = fileName.substring(0, fileName.lastIndexOf('.'));
        String extension = fileName.substring(fileName.lastIndexOf('.'));
        
        // Calculate checksum of original filename
        CRC32 crc32 = new CRC32();
        crc32.update(fileName.getBytes());
        String checksum = String.format("%08X", crc32.getValue());
        
        // Create new filename with checksum
        return String.format("%s_%s%s", nameWithoutExt, checksum, extension);
    }
}
