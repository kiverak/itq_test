package com.example.itq.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "processing")
@Getter
@Setter
public class ProcessingProperties {
    private int batchSize;
    private long submitIntervalMs;
    private long approveIntervalMs;
}
