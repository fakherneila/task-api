package com.fakher.taskapi.dto;

import com.fakher.taskapi.model.Task;
import com.fakher.taskapi.model.TaskStatus;

public class TaskResponse {

    private Long id;
    private String title;
    private String description;
    private TaskStatus status;

    public TaskResponse() {}

    public TaskResponse(Long id, String title, String description, TaskStatus status) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.status = status;
    }

    public static TaskResponse from(Task task) {
        return new TaskResponse(task.getId(), task.getTitle(), task.getDescription(), task.getStatus());
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public TaskStatus getStatus() { return status; }
    public void setStatus(TaskStatus status) { this.status = status; }
}