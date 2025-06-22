package com.example.batchjobs.service;

import com.jcraft.jsch.ChannelSftp;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Slf4j
@Service
@RequiredArgsConstructor
public class SftpService {
    private final ChannelSftp channelSftp;

    @Value("${sftp.remote.directory.input}")
    private String remoteInputDir;

    @Value("${sftp.remote.directory.output}")
    private String remoteOutputDir;

    public void uploadFile(String localFilePath, String remoteFileName) {
        try {
            channelSftp.cd(remoteOutputDir);
            try (InputStream inputStream = Files.newInputStream(Paths.get(localFilePath))) {
                channelSftp.put(inputStream, remoteFileName);
            }
            log.info("File {} uploaded successfully to SFTP", remoteFileName);
        } catch (Exception e) {
            log.error("Error uploading file to SFTP: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to upload file to SFTP", e);
        }
    }

    public void downloadFile(String remoteFileName, String localFilePath) {
        try {
            channelSftp.cd(remoteInputDir);
            Path localPath = Paths.get(localFilePath);
            Files.createDirectories(localPath.getParent());
            
            try (OutputStream outputStream = Files.newOutputStream(localPath)) {
                channelSftp.get(remoteFileName, outputStream);
            }
            log.info("File {} downloaded successfully from SFTP", remoteFileName);
        } catch (Exception e) {
            log.error("Error downloading file from SFTP: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to download file from SFTP", e);
        }
    }

    public boolean fileExists(String remoteDirectory, String fileName) {
        try {
            channelSftp.cd(remoteDirectory);
            return channelSftp.ls(fileName).size() > 0;
        } catch (Exception e) {
            return false;
        }
    }
}
