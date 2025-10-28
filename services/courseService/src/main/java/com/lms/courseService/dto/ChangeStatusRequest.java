package com.lms.courseService.dto;

import com.lms.courseService.model.CourseStatus;
import jakarta.validation.constraints.NotNull;

public class ChangeStatusRequest {
    @NotNull
    private CourseStatus status;

    public CourseStatus getStatus() { return status; }
    public void setStatus(CourseStatus status) { this.status = status; }
}