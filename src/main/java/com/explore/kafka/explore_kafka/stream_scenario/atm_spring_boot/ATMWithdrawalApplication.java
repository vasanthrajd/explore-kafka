package com.explore.kafka.explore_kafka.stream_scenario.atm_spring_boot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.kafka.annotation.EnableKafkaStreams;

import java.util.Collections;

@SpringBootApplication
@EnableKafkaStreams
public class ATMWithdrawalApplication {

    public static void main(String[] args) {
        //SpringApplication.run(ATMWithdrawalApplication.class, args);
        SpringApplication app = new SpringApplication(ATMWithdrawalApplication.class);
        // activate profile
        app.setAdditionalProfiles("atm-stream");
        // add external config file
        app.setDefaultProperties(Collections.singletonMap("spring.config.additional-location", "file:D:\\vasanth-git\\explore-kafka\\src\\main\\resources\\stream\\application-atm-stream.yaml"));
        app.run(args);
        //mvn spring-boot:run -Dspring-boot.run.arguments="--spring.profiles.active=atm-stream --spring.config.additional-location=file:C:/path/to/application-atm-stream.yaml"
        //java -Dspring.profiles.active=atm-stream -Dspring.config.additional-location=file:C:/path/to/application-atm-stream.yaml -jar atm-app.jar
    }
}
