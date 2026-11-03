package com.zodiakk.timetrackservice;

import com.zodiakk.timetrackservice.outbox.config.OutboxProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableFeignClients
@EnableScheduling
@EnableConfigurationProperties(OutboxProperties.class)
public class TimetrackServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(TimetrackServiceApplication.class, args);
    }

}
