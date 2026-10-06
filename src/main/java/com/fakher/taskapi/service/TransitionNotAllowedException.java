package com.fakher.taskapi.service;

import com.fakher.taskapi.model.TaskStatus;

public class TransitionNotAllowedException extends RuntimeException {

    public TransitionNotAllowedException(TaskStatus from, TaskStatus to) {
        super("Transition not allowed: " + from + " -> " + to);
    }
}
