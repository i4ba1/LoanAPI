package com.loan.loanapi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class LoanApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(LoanApiApplication.class, args);
    }

}
