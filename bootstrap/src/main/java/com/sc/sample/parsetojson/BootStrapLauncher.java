package com.sc.sample.parsetojson;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;


/**
 * This can be tested by Terminal /cmd:
 * curl -X POST "http://localhost:8081/api/somebizdomain/upload" \
 *   -F "file=@EntryFile.txt" \
 *   -o OutcomeFile.json
 */
@SpringBootApplication
public class BootStrapLauncher {

    public static void main(String[] args) {
        SpringApplication.run(BootStrapLauncher.class, args);
    }
}

