package com.fakher.taskapi;

import com.fakher.taskapi.model.Task;
import com.fakher.taskapi.model.TaskStatus;
import com.fakher.taskapi.repository.TaskRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class TaskApiApplicationTests {

    @Autowired MockMvc mockMvc;

    @Autowired TaskRepository repository;

    @BeforeEach
    void clean() {
        repository.deleteAll();
    }

    // ---------- POST /tasks ----------

    @Test
    void createTask_returns201_andDefaultStatusTodo() throws Exception {
        String body = """
                {"title":"Write report","description":"desc"}
                """;

        mockMvc.perform(post("/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.title").value("Write report"))
                .andExpect(jsonPath("$.description").value("desc"))
                .andExpect(jsonPath("$.status").value("TODO"));
    }

    @Test
    void createTask_blankTitle_returns400() throws Exception {
        String body = """
                {"title":"   "}
                """;

        mockMvc.perform(post("/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createTask_titleTooLong_returns400() throws Exception {
        String tooLong = "a".repeat(121);
        String body = "{\"title\":\"" + tooLong + "\"}";

        mockMvc.perform(post("/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createTask_titleIsTrimmed() throws Exception {
        String body = """
                {"title":"   Hello   "}
                """;

        mockMvc.perform(post("/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("Hello"));
    }

    // ---------- GET /tasks ----------

    @Test
    void listTasks_filteredByStatus() throws Exception {
        repository.save(new Task("A", null));
        Task done = new Task("B", null);
        done.setStatus(TaskStatus.DONE);
        repository.save(done);

        mockMvc.perform(get("/tasks?status=DONE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].title").value("B"));
    }

    @Test
    void listTasks_unknownStatus_returns400() throws Exception {
        mockMvc.perform(get("/tasks?status=NOPE"))
                .andExpect(status().isBadRequest());
    }

    // ---------- GET /tasks/{id} ----------

    @Test
    void getTask_missing_returns404() throws Exception {
        mockMvc.perform(get("/tasks/9999"))
                .andExpect(status().isNotFound());
    }

    // ---------- PATCH /tasks/{id}/status ----------

    @Test
    void patchStatus_todoToInProgress_returns200() throws Exception {
        Task saved = repository.save(new Task("X", null));

        String body = """
                {"status":"IN_PROGRESS"}
                """;

        mockMvc.perform(patch("/tasks/" + saved.getId() + "/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("IN_PROGRESS"));
    }

    @Test
    void patchStatus_inProgressToDone_returns200() throws Exception {
        Task saved = new Task("X", null);
        saved.setStatus(TaskStatus.IN_PROGRESS);
        saved = repository.save(saved);

        String body = """
                {"status":"DONE"}
                """;

        mockMvc.perform(patch("/tasks/" + saved.getId() + "/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("DONE"));
    }

    @Test
    void patchStatus_forbiddenTransition_returns409() throws Exception {
        Task saved = repository.save(new Task("X", null)); // status = TODO

        String body = """
                {"status":"DONE"}
                """;

        mockMvc.perform(patch("/tasks/" + saved.getId() + "/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.message").value("Transition not allowed: TODO -> DONE"));
    }

    @Test
    void patchStatus_doneIsTerminal_returns409() throws Exception {
        Task saved = new Task("X", null);
        saved.setStatus(TaskStatus.DONE);
        saved = repository.save(saved);

        String body = """
                {"status":"IN_PROGRESS"}
                """;

        mockMvc.perform(patch("/tasks/" + saved.getId() + "/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isConflict());
    }

    @Test
    void patchStatus_unknownStatus_returns400() throws Exception {
        Task saved = repository.save(new Task("X", null));

        String body = """
                {"status":"WHATEVER"}
                """;

        mockMvc.perform(patch("/tasks/" + saved.getId() + "/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    void patchStatus_missingTask_returns404() throws Exception {
        String body = """
                {"status":"IN_PROGRESS"}
                """;

        mockMvc.perform(patch("/tasks/9999/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isNotFound());
    }

    // ---------- DELETE /tasks/{id} ----------

    @Test
    void deleteTask_returns204_then404() throws Exception {
        Task saved = repository.save(new Task("X", null));

        mockMvc.perform(delete("/tasks/" + saved.getId()))
                .andExpect(status().isNoContent());

        assertThat(repository.findById(saved.getId())).isEmpty();
    }
}
