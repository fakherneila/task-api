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

	@Test
	void getTask_missing_returns404() throws Exception {
		mockMvc.perform(get("/tasks/9999"))
				.andExpect(status().isNotFound());
	}

	@Test
	void deleteTask_returns204_then404() throws Exception {
		Task saved = repository.save(new Task("X", null));

		mockMvc.perform(delete("/tasks/" + saved.getId()))
				.andExpect(status().isNoContent());

		assertThat(repository.findById(saved.getId())).isEmpty();
	}
}