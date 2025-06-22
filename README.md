# Spring Batch File Processing System

A robust Spring Batch application that handles automated file processing and data transfer between database, local filesystem, and SFTP server.

## Features

### 1. Database to SFTP Job
- Reads records from database in chunks
- Converts data to CSV format with headers
- Data validation and transformation
- Uploads processed CSV to SFTP server
- Includes timestamp in filenames

### 2. File to SFTP Job
- Monitors local directory for input files
- Validates file format and content
- Processes files in configurable chunks
- Uploads to SFTP with error handling
- Cleanup of processed files

### 3. SFTP to File Job
- Lists and downloads files from SFTP server
- Validates file names and extensions
- Adds checksum for file integrity
- Saves to configured local directory
- Robust error handling

## Technical Stack

- Java 17
- Spring Boot 3.1.0
- Spring Batch
- Spring Data JPA
- H2 Database
- JSch (SFTP client)
- Apache Commons CSV
- Lombok
- SLF4J (Logging)

## Prerequisites

- JDK 11 or higher
- Maven 3.6 or higher
- SFTP server access
- Sufficient disk space for file processing

## Configuration

### Application Properties
Configure the following in `application.yml`:

```yaml
sftp:
  host: your-sftp-server
  port: 22
  username: your-username
  password: your-password
  remote:
    directory:
      input: /input
      output: /output

file:
  local:
    directory:
      input: C:/shared/input
      output: C:/shared/output
      temp: C:/shared/temp

jobs:
  scheduler:
    database-to-sftp: "0 0 1 * * ?"  # 1 AM daily
    file-to-sftp: "0 0 2 * * ?"      # 2 AM daily
    sftp-to-file: "0 0 3 * * ?"      # 3 AM daily
```

## Building the Application

```bash
mvn clean install
```

## Running the Application

### Using Maven
```bash
mvn spring-boot:run
```

### Using JAR
```bash
java -jar target/spring-batch-jobs-1.0.0.jar
```

## Job Execution

### Scheduled Execution
Jobs run automatically based on configured schedules:
- Database to SFTP: 1 AM daily
- File to SFTP: 2 AM daily
- SFTP to File: 3 AM daily

### Manual Trigger via REST API
```bash
# Database to SFTP Job
curl -X POST http://localhost:8080/api/jobs/database-to-sftp

# File to SFTP Job
curl -X POST http://localhost:8080/api/jobs/file-to-sftp

# SFTP to File Job
curl -X POST http://localhost:8080/api/jobs/sftp-to-file
```

## Data Processing Features

### Database Processing
- Validates data fields
- Converts text to uppercase
- Removes special characters
- Adds timestamps
- Handles null values

### File Processing
- CSV format validation
- Field count verification
- Data cleaning and standardization
- Empty field handling
- Special character removal

### SFTP Processing
- File extension validation
- Filename length checks
- Checksum generation
- File integrity verification

## Error Handling

- Retry mechanism (3 attempts)
- Transaction management
- Detailed error logging
- Global exception handling
- Skip logic for invalid records

## Monitoring

- Detailed logging at each step
- Job execution status tracking
- Error notifications
- Progress monitoring

## Best Practices

- Chunk-based processing
- Proper resource handling
- Secure credential management
- Code modularity
- Comprehensive logging
- Transaction management
- Error handling

## Directory Structure

```
src/main/java/com/example/batchjobs/
├── config/
│   ├── BatchConfig.java
│   ├── SchedulerConfig.java
│   └── SftpConfig.java
├── controller/
│   └── JobController.java
├── job/
│   ├── DatabaseToSftpJobConfig.java
│   ├── FileToSftpJobConfig.java
│   └── SftpToFileJobConfig.java
├── model/
│   └── DataRecord.java
├── processor/
│   ├── DatabaseItemProcessor.java
│   ├── FileItemProcessor.java
│   └── SftpItemProcessor.java
└── service/
    └── SftpService.java
```

## Contributing

1. Fork the repository
2. Create your feature branch
3. Commit your changes
4. Push to the branch
5. Create a new Pull Request

## License

This project is licensed under the MIT License - see the LICENSE file for details.

## Support

For support and questions, please create an issue in the repository.
