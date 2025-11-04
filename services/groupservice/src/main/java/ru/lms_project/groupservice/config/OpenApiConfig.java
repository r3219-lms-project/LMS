package ru.lms_project.groupservice.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI groupServiceOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Group Service API")
                        .description("REST API для управления учебными группами в LMS")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("LMS Team")
                                .email("support@lms-project.ru")));
    }
}

