# In-Memory Search Engine

An in-memory search engine built with **Java 17** and **Spring Boot**. This project demonstrates high-performance **Ranked search (BM25 & TF-IDF)**, **Dynamic Threshold Retrieval**, and **Autocomplete** using a Trie-based index with thread-safe concurrency. Developed as a core project for technical interview and internship preparation.

## Architecture

The system follows a clear separation of concerns, transitioning raw HTTP requests into structured, ranked results through a multi-stage pipeline:

1.  **REST API (Web Layer)**: Provides validated endpoints for document ingestion (`POST /api/documents`) and multi-parameter searching (`GET /api/search`).
2.  **Document Service (Ingestion)**: Encapsulates the workflow of processing raw input, coordinating with the Tokenizer, and updating the index.
3.  **Tokenizer (Normalization)**: Handles character cleaning, preserves technical symbols (C++, #), and applies light stemming to improve recall.
4.  **Threshold Engine (Discovery)**: A Phase 4 addition that uses a local frequency aggregator to identify candidates based on a "Minimum Should Match" percentage.
5.  **Inverted Index (Storage)**: A thread-safe data structure using `ReentrantReadWriteLock` to map tokens to document IDs for $O(1)$ lookups.
6. **Trie Index (Suggestions)**: Powers the autocomplete engine by storing term frequencies and providing $O(L)$ prefix matching.
7. **Search Service (Orchestration)**: The "brain" of the system that utilizes the **Strategy Pattern** to apply BM25 or TF-IDF ranking.

---

## Performance Results

The following metrics represent the steady-state performance of the engine after JVM warm-up, measured on a dataset of 10,000+ documents. Full details and bottleneck analysis can be found in `BENCHMARK_RESULTS.md`.

| Operation                     | Average Latency | Min Latency | P99 Latency | Max Latency |
|-------------------------------|-----------------|-------------|-------------|-------------|
| **Document Indexing**         | 4.75 µs         | 0.70 µs     | 14.50 µs    | 27418.90 µs |
| **Threshold Search (T=1.0)**  | 2507.15 µs      | 225.50 µs   | 6610.30 µs  | 24010.30 µs |
| **Threshold Search (T=0.75)** | 2774.32 µs      | 223.80 µs   | 7981.20 µs  | 25401.20 µs |
| **Ranked Search (TF-IDF)**    | 3893.97 µs      | 278.40 µs   | 9953.60 µs  | 20860.20 µs |
| **Ranked Search (BM25)**      | 4696.17 µs      | 306.70 µs   | 11191.40 µs | 20180.10 µs |

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

## Roadmap

### Core Features (Phase 1 - 4)
- [x] **Pluggable Ranking (Strategy Pattern)**: Support for both **TF-IDF** and industry-standard **Okapi BM25**.
- [x] **Threshold Matching**: Dynamic "Minimum Should Match" logic to improve recall on natural language queries.
- [x] **Top-K Retrieval**: Optimized Ranked Search using a Min-Heap ($O(D \log K)$) to prioritize results.
- [x] **Concurrency Support**: Thread-safe indexing and searching using `ReentrantReadWriteLock`.
- [x] **Performance Benchmarking**: Side-by-side latency analysis of discovery vs. scoring overhead.
- [x] **REST API**: Spring Boot integration for document ingestion and searching.

### Future Enhancements (Phase 5+)
- [ ] **Disk Persistence**: Implementing a storage layer to serialize the index for persistence across restarts.
- [ ] **Fuzzy Search**: Integrating Levenshtein distance for typo-tolerant query matching.
- [ ] **WAND Algorithm**: Implementing a "Max-Score" heuristic to skip scoring documents that cannot enter the Top-K.
- [ ] **Primitive Collections**: Using specialized collections (like `fastutil`) to reduce heap allocation and GC frequency.
- [ ] **Pre-computed Statistics**: Caching `avgdl` and `IDF` values to further reduce BM25 query latency.