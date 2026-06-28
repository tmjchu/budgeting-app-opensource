package com.localbudget.app;

import com.localbudget.app.config.BudgetAppProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(BudgetAppProperties.class)
public class BudgetAppApplication {

    public static void main(String[] args) {
        SpringApplication.run(BudgetAppApplication.class, args);
    }
}
