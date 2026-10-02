package com.fakher.taskapi.service;

import com.fakher.taskapi.dto.CreateTaskRequest;
import com.fakher.taskapi.dto.UpdateTaskRequest;
import com.fakher.taskapi.model.Task;
import com.fakher.taskapi.model.TaskStatus;
import com.fakher.taskapi.repository.TaskRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TaskService {

    private final TaskRepository repository;

    public TaskService(TaskRepository repository) {
        this.repository = repository;
    }

    public Task create(CreateTaskRequest req) {
        Task task = new Task(req.getTitle().trim(), req.getDescription());
        task.setStatus(TaskStatus.TODO);
        return repository.save(task);
    }

    public List<Task> findAll(TaskStatus status) {
        if (status == null) {
            return repository.findAll();
        }
        return repository.findByStatus(status);
    }

    public Task findById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new TaskNotFoundException(id));
    }

    public Task update(Long id, UpdateTaskRequest req) {
        Task task = findById(id);
        if (req.getTitle() != null && !req.getTitle().isBlank()) {
            task.setTitle(req.getTitle().trim());
        }
        if (req.getDescription() != null) {
            task.setDescription(req.getDescription());
        }
        if (req.getStatus() != null) {
            task.setStatus(req.getStatus());
        }
        return repository.save(task);
    }

    public void delete(Long id) {
        if (!repository.existsById(id)) {
            throw new TaskNotFoundException(id);
        }
        repository.deleteById(id);
    }
}