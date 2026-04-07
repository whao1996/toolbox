package com.example.toolbox;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;

@SpringBootApplication
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @Bean
    public ApplicationListener<ApplicationReadyEvent> applicationReadyEventListener(Environment environment) {
        return event -> {
            String port = environment.getProperty("server.port", "8080");
            String contextPath = environment.getProperty("server.servlet.context-path", "");
            String accessUrl = "http://localhost:" + port + contextPath + "/index.html";
            System.out.println("\n🎉========================================🎉");
            System.out.println("✅Toolbox is ready!");
            System.out.println("🚀 Access Toolbox: " + accessUrl);
            System.out.println("🎉========================================🎉\n");
        };
    }
}
