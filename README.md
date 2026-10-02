# Task API — PFE Java/Spring Boot Test

Small REST API for managing tasks, built with **Java 21**, **Spring Boot 4.0.8**, **Spring Data JPA**, and **H2** (in-memory database).

## Tech stack

| Layer | Technology |
|---|---|
| Language | Java 21 |
| Framework | Spring Boot 4.0.8 |
| Web | Spring Web MVC |
| Persistence | Spring Data JPA / Hibernate |
| Database | H2 (in-memory) |
| Validation | Jakarta Bean Validation |
| Build | Maven |
| Tests | JUnit 5, MockMvc, AssertJ |

## How to run

```
./mvnw spring-boot:run
```

The API starts on http://localhost:8080.

H2 console (for debugging): http://localhost:8080/h2-console
- JDBC URL: jdbc:h2:mem:taskdb
- User: sa
- Password: (empty)

## How to run the tests

```
./mvnw test
```

Expected output:

Tests run: 8, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS

## Domain model

A Task has:

| Field | Type | Rules |
|---|---|---|
| id | Long | Auto-generated |
| title | String | Required, 1-120 chars after trim |
| description | String | Optional, max 1000 chars |
| status | Enum | TODO, IN_PROGRESS, DONE - default TODO |

## Endpoints

| Method | Path | Description | Success | Errors |
|---|---|---|---|---|
| POST | /tasks | Create a task | 201 | 400 invalid title |
| GET | /tasks | List tasks (optional ?status=) | 200 | 400 unknown status |
| GET | /tasks/{id} | Get a task | 200 | 404 not found |
| PUT | /tasks/{id} | Update a task | 200 | 400 / 404 |
| DELETE | /tasks/{id} | Delete a task | 204 | 404 |

### Example requests

Create a task:
```
curl -X POST http://localhost:8080/tasks -H "Content-Type: application/json" -d '{"title":"Write report","description":"For the internship"}'
```

Response (201):

{
  "id": 1,
  "title": "Write report",
  "description": "For the internship",
  "status": "TODO"
}

List tasks filtered by status:
```
curl "http://localhost:8080/tasks?status=TODO"
```

Update a task:
```
curl -X PUT http://localhost:8080/tasks/1 -H "Content-Type: application/json" -d '{"status":"DONE"}'
```
Delete a task:
```
curl -X DELETE http://localhost:8080/tasks/1
```
Error response shape (400 / 404):

{
  "timestamp": "2026-10-02T21:52:04.653263066Z",
  "status": 400,
  "error": "Bad Request",
  "message": "title: title is required"
}

## Project structure

src/main/java/com/fakher/taskapi/
- TaskApiApplication.java        (entry point)
- model/
  - Task.java                    (JPA entity)
  - TaskStatus.java              (enum)
- repository/
  - TaskRepository.java          (Spring Data JPA)
- service/
  - TaskService.java             (business logic)
  - TaskNotFoundException.java
- controller/
  - TaskController.java          (REST endpoints)
- dto/
  - CreateTaskRequest.java
  - UpdateTaskRequest.java
  - TaskResponse.java
- exception/
  - GlobalExceptionHandler.java

## Design notes

- Layered architecture: controller -> service -> repository, so each layer has a single responsibility.
- DTOs separate from entities: request validation and response shaping are independent from persistence concerns.
- Global exception handling via @RestControllerAdvice, returning a consistent error body.
- Derived query findByStatus: Spring Data generates the SQL from the method name.
- Tests cover the happy path, validation failures, unknown status filter, and 404 handling.
- No authentication, no frontend, no deployment - as per the test instructions.
