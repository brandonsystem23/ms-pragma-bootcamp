package com.pragma.bootcamp_service.infrastructure.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

    @Bean(name = "capabilityWebClient")
    public WebClient technologyWebClient(
            WebClient.Builder builder,
            @Value("${clients.capability.base-url}") String usersBaseUrl
    ) {
        return builder
                .baseUrl(usersBaseUrl)
                .build();
    }

    @Bean(name = "reportWebClient")
    public WebClient reportWebClient(
            WebClient.Builder builder,
            @Value("${clients.report.base-url}") String reportBaseUrl
    ) {
        return builder
                .baseUrl(reportBaseUrl)
                .build();
    }
}
