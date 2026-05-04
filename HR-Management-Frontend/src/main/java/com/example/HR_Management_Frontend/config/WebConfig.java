package com.example.HR_Management_Frontend.config;

import java.math.BigDecimal;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.format.FormatterRegistry;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    // ✅ RestTemplate bean (used in your service)
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    // ✅ Converter for String → BigDecimal (important for IDs from request params)
    @Override
    public void addFormatters(FormatterRegistry registry) {
        registry.addConverter(String.class, BigDecimal.class, source -> {
            if (source == null || source.isBlank()) return null;
            return new BigDecimal(source.trim());
        });
    }
}