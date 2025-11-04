package ru.lms_project.coursestructure;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = {
    "ru.lms_project.coursestructure",
    "ru.lms_project.common.security"
})
public class CourseStructureServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(CourseStructureServiceApplication.class, args);
    }
}
