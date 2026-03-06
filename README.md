# In-Memory Search Engine

An in-memory search engine built with **Java** and **Spring Boot**. This project demonstrates high-performance **Ranked search (BM25 & TF-IDF)**, **Thread-safe Boolean retrieval**, and **Autocomplete** using a Trie-based index with normalized tokenization. Developed as a core project for technical interview and internship preparation.

## Architecture

The system follows a clear separation of concerns, transitioning raw HTTP requests into structured, ranked results through a multi-stage pipeline:

1.  **REST API (Web Layer)**: Provides validated endpoints for document ingestion (`POST /api/documents`) and multi-parameter searching (`GET /api/search`).
2.  **Document Service (Ingestion)**: Encapsulates the workflow of processing raw input, coordinating with the Tokenizer, and updating the index.
3.  **Tokenizer (Normalization)**: Handles character cleaning, preserves technical symbols (C++, #), and applies light stemming to improve recall.
4.  **Inverted Index (Storage)**: A thread-safe data structure using `ReentrantReadWriteLock` to map tokens to document IDs for $O(1)$ lookups.
5.  **Trie Index (Suggestions)**: Powers the autocomplete engine by storing term frequencies and providing $O(L)$ prefix matching.
6.  **Search Service (Orchestration)**: The "brain" of the system that utilizes the **Strategy Pattern** to apply BM25 or TF-IDF ranking.

---

## Performance Results

The following metrics represent the steady-state performance of the engine after JVM warm-up, measured on a dataset of 10,000+ documents. Full details and bottleneck analysis can be found in `BENCHMARK_RESULTS.md`.

| Operation                  | Average Latency | Min Latency | P99 Latency | Max Latency |
|----------------------------|-----------------|-------------|-------------|-------------|
| **Document Indexing**      | 3.96 µs         | 1.30 µs     | 18.70 µs    | 5100.80 µs  |
| **Boolean Search (AND)**   | 3454.72 µs      | 373.90 µs   | 8896.30 µs  | 30604.70 µs |
| **Ranked Search (TF-IDF)** | 5410.87 µs      | 420.80 µs   | 10537.10 µs | 31895.80 µs |
| **Ranked Search (BM25)**   | 6460.48 µs      | 498.40 µs   | 12997.80 µs | 24720.80 µs |

---

## How to Run

### Prerequisites
* **Java**: JDK 17 or higher
* **Build Tool**: Maven 3.8+

### Setup and Build
1. **Clone the repository**:
   ```bash
   git clone https://github.com/purvak91/in-memory-search-engine.git
    ```
2. **Build the project**:
    ```bash
    mvn clean install
    ```
3. **Run the benchmarks**:
To evaluate the system performance (Indexing, Boolean Search, BM25 vs. TF-IDF), execute the following command:
    ```bash
    mvn exec:java -Dexec.mainClass="com.purva.searchengine.benchmark.LatencyBenchmark"
    ```
4. **Run the tests**:
Execute the comprehensive test suite, including concurrency and integrity checks:
    ```bash
    mvn clean test
    ```

### Roadmap

#### Core Features 
- [x] **Trie-based Autocomplete**: Fast prefix matching for real-time search suggestions.
- [x] **Pluggable Ranking (Strategy Pattern)**: Support for both **TF-IDF** and industry-standard **Okapi BM25**.
- [x] **Top-K Retrieval**: Optimized Ranked Search using a Min-Heap ($O(D \log K)$) to maintain only the most relevant results.
- [x] **Concurrency Support**: Thread-safe indexing and searching using `ReentrantReadWriteLock`.
- [x] **Performance Benchmarking**: Automated latency analysis suite with P99 and JIT warm-up logic.
- [x] **REST API**: Spring Boot integration for document ingestion and searching.

#### Future Enhancements
- [ ] **Global Top-K Integration**: Extending the PriorityQueue optimization to standard Boolean searches for consistent performance.
- [ ] **Disk Persistence**: Implementing a storage layer to serialize the index for persistence across restarts.
- [ ] **Fuzzy Search**: Integrating Levenshtein distance for typo-tolerant query matching.
- [ ] **Distributed Indexing**: Exploring sharding strategies for datasets exceeding single-node memory limits.
- [ ] **Field-Specific Scoring**: Allowing different weights for matches found in titles versus body text.
- [ ] **WAND Algorithm**: Implementing a "Max-Score" heuristic to skip scoring documents that cannot mathematically enter the Top-K results, reducing the total number of floating-point operations.
- [ ] **Primitive Collections**: Use specialized collections (like `fastutil`) to store scores as primitive `double` values rather than `Double` objects, reducing heap allocation and GC frequency.
- [ ] **Pre-computed Statistics**: Caching `avgdl` and `IDF` values to further reduce BM25 query latency.
- [ ] **Threshold Matching**: Implementing "Match $X\%$" logic (e.g., return documents that match 3 out of 4 words) to provide more flexible search options.
