package com.lms.progressService.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI progressServiceOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Progress Service API")
                        .description("API для отслеживания прогресса пользователей в курсах LMS")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("LMS Team")
                                .email("lms@example.com")));
    }
}
