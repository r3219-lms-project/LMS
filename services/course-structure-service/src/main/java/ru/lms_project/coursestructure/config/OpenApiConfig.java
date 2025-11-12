package ru.lms_project.coursestructure.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.tags.Tag;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI courseStructureOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Course Structure Service API")
                        .description("REST API для управления структурой курсов (модули и уроки) в LMS")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("LMS Team")
                                .email("support@lms-project.ru")))
                .addSecurityItem(new SecurityRequirement().addList("Bearer Authentication"))
                .components(new Components()
                        .addSecuritySchemes("Bearer Authentication",
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .description("JWT token для аутентификации")))
                .tags(Arrays.asList(
                        new Tag().name("Modules").description("Управление модулями курсов"),
                        new Tag().name("Lessons").description("Управление уроками модулей"),
                        new Tag().name("Statistics").description("Статистика по структуре курсов")
                ));
    }
}
