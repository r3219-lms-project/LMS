package com.lms.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HealthController {

    @GetMapping("/health")
    public String health() {
        return "LMS Application is running!";
    }

    @GetMapping("/")
    public String home() {
        return "Welcome to LMS - Learning Management System";
    }
}
