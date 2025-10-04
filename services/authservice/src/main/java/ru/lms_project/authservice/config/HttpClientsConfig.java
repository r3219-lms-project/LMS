package ru.lms_project.authservice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

@Configuration
public class HttpClientsConfig {

    @Bean
    public RestTemplate restTemplate() {
        var factory = new HttpComponentsClientHttpRequestFactory();
        factory.setConnectTimeout(2000);
        factory.setReadTimeout(3000);

        return new RestTemplate(factory);
    }
}
