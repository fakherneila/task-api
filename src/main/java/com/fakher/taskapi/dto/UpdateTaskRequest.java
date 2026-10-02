package com.fakher.taskapi.dto;

import com.fakher.taskapi.model.TaskStatus;
import jakarta.validation.constraints.Size;

public class UpdateTaskRequest {

    @Size(min = 1, max = 120, message = "title must be between 1 and 120 characters")
    private String title;

    @Size(max = 1000, message = "description must be at most 1000 characters")
    private String description;

    private TaskStatus status;

    public UpdateTaskRequest() {}

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public TaskStatus getStatus() { return status; }
    public void setStatus(TaskStatus status) { this.status = status; }
}