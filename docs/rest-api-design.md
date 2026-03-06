# REST API Design

## Overview
Phase 3 introduces the web layer to the search engine, transitioning the project from a standalone library to a functional RESTful service. This layer handles HTTP communication, request validation, and centralized error management.

---

## Controllers
The system exposes two primary endpoints to interact with the in-memory index.

### 1. IndexController
* **Endpoint**: `POST /api/documents`
* **Request Format**: `IndexRequest` (Java Record) containing `int documentId` and `String content`.
* **Response**: `201 Created` on success.
* **Validation**: Throws `IllegalArgumentException` if content is null or blank.

### 2. SearchController
* **Endpoint**: `GET /api/search`
* **Parameters**:
    * `query` (Required): The search term.
    * `topK` (Optional): The number of results for ranked search.
* **Logic**: Dynamically switches between standard Boolean search (if `topK` is absent) and **Advanced Ranked Search** (if `topK` is present).
* **Scoring Strategy**: By default, the system utilizes the `Bm25Scorer` for ranking, providing industry-standard relevance through term saturation and length normalization.
* **Validation**: Ensures `query` is not empty or whitespace and that `topK` is greater than 0 if provided.

---

## Service Layer: DocumentService
While `SearchService` handles retrieval, `DocumentService` was introduced to manage the **Ingestion Pipeline**.
* **Purpose**: Acts as a bridge between the `IndexController` and the `InvertedIndex`.
* **Pipeline Role**: It encapsulates the "Tokenize -> Index" workflow. By separating this from the controller, the business logic remains reusable and the controller stays "thin".

---

## Global Exception Handler
To provide a consistent API experience, the `GlobalExceptionHandler` uses `@ControllerAdvice` to intercept exceptions globally.

| Exception                         | HTTP Status          | Reason                                                                          |
|:----------------------------------|:---------------------|:--------------------------------------------------------------------------------|
| `IllegalArgumentException`        | `400 Bad Request`    | Handled for validation failures like empty queries or invalid document content. |
| `HttpMessageNotReadableException` | `400 Bad Request`    | Triggered when a POST request contains malformed or missing JSON.               |
| `Exception` (Generic)             | `500 Internal Error` | A "catch-all" to prevent leaking internal stack traces to the client.           |

---

## Configuration: AppConfig
The project uses Java-based `@Configuration` instead of marking every class with `@Component`.

### Design Decisions & Trade-offs
* **Decoupling**: By using `@Bean` methods, core logic classes like `Tokenizer` and `InvertedIndex` remain pure Java classes without Spring-specific annotations.
* **Centralized Wiring**: All dependency injection logic is located in one file, making it easy to see how `SearchService` and `DocumentService` are constructed.
* **Testing**: This approach simplifies unit testing as the components are not tightly coupled to the Spring Container.

---