package com.ooad.home4paws;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.lang.NonNull;

@SpringBootApplication
public class Home4PawsApplication {

    public static void main(String[] args) {
        SpringApplication.run(Home4PawsApplication.class, args);
    }


}
