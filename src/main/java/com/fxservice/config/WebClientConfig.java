package com.fxservice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * This class configres the WebClient, which is Spring's modern,
 * non-blocking way to make HTTP requests to external services.
 */
@Configuration
public class WebClientConfig {

    /**
     * Creates a WebClient "Bean" (a managed object) for Spring to use.
     * We are setting the base URL for the free Frankfurter currency API.
     * This means any request we make with this client will start with this URL.
     *
     * We are using "frankfurter.app" because it is free, fast, and requires no API key.
     */
    @Bean
    public WebClient frankfurterWebClient(){
        return WebClient.builder()
                .baseUrl("https://api.frankfurter.app")
                .build();
    }
}
