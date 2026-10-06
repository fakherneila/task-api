# Task API — PFE Java/Spring Boot Test

Small REST API for managing tasks, built with **Java 21**, **Spring Boot 4.0.8**, **Spring Data JPA**, and **H2** (in-memory database).

No frontend, no authentication, no deployment — as per the test brief.

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

    ./mvnw spring-boot:run

The API starts on http://localhost:8080.

H2 console (for debugging): http://localhost:8080/h2-console
- JDBC URL: jdbc:h2:mem:taskdb
- User: sa
- Password: (empty)

## How to run the tests

    ./mvnw test

Expected output:

    Tests run: 14, Failures: 0, Errors: 0, Skipped: 0
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

### Required by the brief

| Method | Path | Description | Success | Errors |
|---|---|---|---|---|
| POST | /tasks | Create a task | 201 | 400 invalid title |
| GET | /tasks | List tasks (optional ?status=) | 200 | 400 unknown status |
| PATCH | /tasks/{id}/status | Change status (strict transitions) | 200 | 400 unknown status, 404 missing id, 409 forbidden transition |

### Bonus (not required, but implemented)

| Method | Path | Description | Success | Errors |
|---|---|---|---|---|
| GET | /tasks/{id} | Get a task by id | 200 | 404 not found |
| PUT | /tasks/{id} | Update title/description (status excluded) | 200 | 400 / 404 |
| DELETE | /tasks/{id} | Delete a task | 204 | 404 |

Note: PUT /tasks/{id} intentionally does not accept a status field. Status changes go through PATCH /tasks/{id}/status so the transition rule cannot be bypassed.

## Status transition rules

Only the following transitions are allowed:

| From | To | Result |
|---|---|---|
| TODO | IN_PROGRESS | 200 OK |
| IN_PROGRESS | DONE | 200 OK |
| TODO | DONE | 409 Conflict (skipping a step) |
| DONE | any | 409 Conflict (DONE is terminal) |
| IN_PROGRESS | TODO | 409 Conflict (no going back) |
| any | same status | 409 Conflict |
| any | unknown status | 400 Bad Request |

When a transition is refused, the response body carries the reason:

    {"status":409,"error":"Conflict","message":"Transition not allowed: TODO -> DONE", ...}

## Example requests

Create a task:

    curl -X POST http://localhost:8080/tasks -H "Content-Type: application/json" -d '{"title":"Write report","description":"For the internship"}'

Response (201):

    {
      "id": 1,
      "title": "Write report",
      "description": "For the internship",
      "status": "TODO"
    }

List tasks filtered by status:

    curl "http://localhost:8080/tasks?status=TODO"

Move a task to IN_PROGRESS:

    curl -X PATCH http://localhost:8080/tasks/1/status -H "Content-Type: application/json" -d '{"status":"IN_PROGRESS"}'

Try an invalid transition (TODO -> DONE):

    curl -X PATCH http://localhost:8080/tasks/1/status -H "Content-Type: application/json" -d '{"status":"DONE"}'

Response (409):

    {
      "timestamp": "2026-10-06T18:30:00.000000000Z",
      "status": 409,
      "error": "Conflict",
      "message": "Transition not allowed: TODO -> DONE"
    }

Update title/description:

    curl -X PUT http://localhost:8080/tasks/1 -H "Content-Type: application/json" -d '{"title":"Updated title"}'

Delete a task:

    curl -X DELETE http://localhost:8080/tasks/1

Error response shape (400 / 404 / 409):

    {
      "timestamp": "2026-10-06T18:30:00.000000000Z",
      "status": 400,
      "error": "Bad Request",
      "message": "title: title is required"
    }

## Tests

14 automated tests, all green:

- POST /tasks: valid creation, blank title (400), title too long (400), title trimmed
- GET /tasks: filter by status, unknown status (400)
- GET /tasks/{id}: missing id (404)
- PATCH /tasks/{id}/status: TODO to IN_PROGRESS (200), IN_PROGRESS to DONE (200), TODO to DONE (409), DONE to any (409), unknown status (400), missing id (404)
- DELETE /tasks/{id}: 204 then 404

The brief required at least 3 tests: valid creation, invalid title, forbidden transition - all covered, plus 11 more.

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
      - TransitionNotAllowedException.java
    - controller/
      - TaskController.java          (REST endpoints)
    - dto/
      - CreateTaskRequest.java
      - UpdateTaskRequest.java
      - UpdateStatusRequest.java
      - TaskResponse.java
    - exception/
      - GlobalExceptionHandler.java

## Design notes

- Layered architecture: controller to service to repository, so each layer has a single responsibility.
- DTOs separate from entities: request validation and response shaping are independent from persistence concerns.
- Global exception handling via @RestControllerAdvice, returning a consistent error body for 400, 404 and 409.
- Derived query findByStatus: Spring Data generates the SQL from the method name.
- Status transitions enforced in the service layer via an exhaustive switch, so adding a new status later forces a compile-time update.

## Time spent

Total: about 4 hours, spread over a few sessions.

- Setup (project skeleton, pom.xml, dependencies): ~30 min
- Model + repository + DTOs: ~30 min
- Service + controller: ~45 min
- Exception handling + validation: ~30 min
- PATCH endpoint + transition rules + 409 handling: ~30 min
- Tests (14 total): ~45 min
- README and cleanup: ~30 min

## Limitations and what is left to do

- No pagination on GET /tasks. For a small dataset it is fine; a production version would add page/size parameters.
- No authentication or authorization, as per the brief.
- No createdAt / updatedAt timestamps on Task. Easy to add later with @CreationTimestamp and @UpdateTimestamp.
- The transition rule is a strict linear chain. If a future requirement allows reopening a DONE task, only the switch inside TaskService.isAllowedTransition needs to change.
- Tests use the real H2 database (not mocks). Slower, but it verifies the full stack end-to-end.
- No Docker or CI pipeline. Not required, and out of scope for this exercise.

## AI usage

The brief allows AI assistance as long as its use is declared and the candidate can explain the code. This section declares it.

- An AI assistant (LLM) was used to help structure the project, review the code, and generate the initial test scaffolding.
- Every file has been read, executed, and verified locally. ./mvnw clean test passes with 14 tests green.
- All endpoints were manually exercised with curl and the Postman.
- The status transition rules and the 409 handling were written from the brief and verified against it - not generated blindly.
- No secrets, credentials, real user data, or confidential code were shared with the AI.
- All test data is fictional.
- I am able to explain and modify any part of this code during the follow-up interview.
