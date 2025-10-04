package com.lms.taskservice.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum TaskType {
    BASIC("Basic homework assignment"),
    TEST("Test assignment");

    private final String description;
}
