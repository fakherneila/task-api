package com.fakher.taskapi.dto;

import com.fakher.taskapi.model.TaskStatus;
import jakarta.validation.constraints.NotNull;

public class UpdateStatusRequest {

    @NotNull(message = "status is required")
    private TaskStatus status;

    public UpdateStatusRequest() {}

    public TaskStatus getStatus() { return status; }
    public void setStatus(TaskStatus status) { this.status = status; }
}
