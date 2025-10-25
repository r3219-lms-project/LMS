package com.example.lms.groups.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

  @Bean
  public OpenAPI groupServiceOpenAPI() {
    return new OpenAPI()
        .info(new Info()
            .title("Group Service API")
            .description("API для управления группами в LMS")
            .version("1.0.0"));
  }
}
