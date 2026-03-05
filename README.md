# In-Memory Search Engine

An in-memory search engine built with **Java** and **Spring Boot**. This project demonstrates high-performance **TF-IDF Ranked search**, **Boolean AND retrieval**, and **Autocomplete** using a Trie-based index with normalized tokenization. Developed as a core project for technical interview and internship preparation.

## Architecture

The system is designed with a clear separation of concerns, moving data from raw strings to a structured, ranked result set through four primary components:

1.  **Tokenizer**: Normalizes raw text input by converting it to lowercase and splitting it into individual terms (tokens) while removing non-alphanumeric characters.
2.  **Trie-Based Index**: Acts as the core storage structure, enabling $O(L)$ time complexity (where $L$ is word length) for term lookups and supporting efficient prefix-based autocomplete.
3.  **Inverted Index**: Maps unique tokens to a list of document identifiers (postings), allowing the engine to instantly retrieve all documents containing a specific search term.

4.  **Search Service**: Orchestrates the retrieval logic, performing Boolean AND operations for multi-term queries and calculating TF-IDF scores to rank results by relevance.

---

## Performance Results

The following metrics represent the steady-state performance of the engine after JVM warm-up, measured in microseconds ($\mu s$) on an in-memory dataset of 10,000+ documents. Detailed analysis can be found in `BENCHMARK_RESULTS.md`.

| Operation                  | Average Latency | Min Latency | P99 Latency | Max Latency |
|----------------------------|-----------------|-------------|-------------|-------------|
| **Document Indexing**      | 2.26 µs         | 1.30 µs     | 9.00 µs     | 2842.90 µs  |
| **Boolean Search (AND)**   | 2416.18 µs      | 355.90 µs   | 5151.40 µs  | 8705.00 µs  |
| **Ranked Search (TF-IDF)** | 5109.49 µs      | 414.60 µs   | 10180.10 µs | 69680.80 µs |

---

## How to Run

### Prerequisites
* **Java**: JDK 17 or higher
* **Build Tool**: Maven 3.8+

### Setup and Build
1. **Clone the repository**:
   ```bash
   git clone [https://github.com/purvak91/in-memory-search-engine.git](https://github.com/purvak91/in-memory-search-engine.git)
   cd in-memory-search-engine
    ```
2. **Build the project**:
    ```bash
    mvn clean install
    ```
3. **Run the benchmarks**:
To evaluate the system performance (Indexing, Boolean Search, Ranked Search), execute the following command:
    ```bash
    mvn exec:java -Dexec.mainClass="com.purva.searchengine.LatencyBenchmark"
    ```
4. **Run the tests**:
Execute the comprehensive test suite, including concurrency and integrity checks:
    ```bash
    mvn test
    ```

### Roadmap

#### Core Features 
- [x] **Trie-based Autocomplete**: Fast prefix matching for real-time search suggestions.
- [x] **TF-IDF Ranking**: Relevance-based scoring using term frequency and inverse document frequency.
- [x] **Top-K Retrieval**: Optimized Ranked Search using a Min-Heap to maintain only the most relevant results.
- [x] **Concurrency Support**: Thread-safe indexing and searching using ReentrantReadWriteLock.
- [x] **Performance Benchmarking**: Automated latency analysis suite with P99 and JIT warm-up logic.

#### Future Enhancements
- [ ] **Global Top-K Integration**: Extending the PriorityQueue optimization to standard Boolean searches for consistent performance.
- [ ] **Disk Persistence**: Implementing a storage layer to serialize the index for persistence across restarts.
- [ ] **Fuzzy Search**: Integrating Levenshtein distance for typo-tolerant query matching.
- [ ] **Distributed Indexing**: Exploring sharding strategies for datasets exceeding single-node memory limits.
