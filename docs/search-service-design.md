# Search Service Technical Documentation

## 1. Purpose in System
The `SearchService` is the orchestration layer of the search engine, acting as the bridge between raw user input and indexed data.

### Core Functions:
- **Component Coordination:** Integrates the `Tokenizer` for input cleaning and the `InvertedIndex` for data retrieval.
- **Query Processing:** Transforms raw user strings into searchable tokens.
- **Retrieval Strategy Selection:** Determines which documents qualify as candidates based on dynamic threshold logic.

---

## 2. Query Processing Workflow
When a search is initiated via the **REST API (Phase 3)**, the service follows a linear execution pipeline:

1. **Tokenize Query:** Raw input is normalized and filtered by the `Tokenizer`.
2. **Candidate Discovery:** The service identifies document IDs that meet the **Threshold** requirement (Phase 4).
3. **Scoring (Ranked Only):** The active `Scorer` (BM25 or TF-IDF) calculates relevance.
4. **Top-K Selection:** A Min-Heap prioritizes the best results to ensure $O(D \log K)$ efficiency.

---

## 3. Retrieval Strategy Evolution

### Phase 2: Strict Boolean Intersection (AND)
The engine initially used a binary "AND" strategy where `Set.retainAll()` required 100% of query tokens to be present. While high-precision, this was too restrictive for complex queries.

### Phase 4: Parameterized Threshold Matching (Current)
Introduced in Phase 4 to improve recall, the engine now utilizes a **Minimum Should Match** model.
- **Recall Optimization:** Documents are considered candidates if they contain $\ge \text{threshold}$ of the unique query tokens.
- **Logic:** For a query with $N$ tokens and a threshold $T$, the required match count is $\lceil N \times T \rceil$.
- **User Control:** The threshold parameter $(0, 1]$ allows users to toggle between strict and fuzzy matching at runtime.

---

## 4. Algorithm Design: Frequency Aggregation
To support the Phase 4 dynamic threshold, the service transitioned from Set-based intersection to a **Frequency Map** approach:

- **Map Aggregation:** A local `HashMap<Integer, Integer>` is initialized per request to track the occurrence count for each `documentId` across posting lists.
- **Counting:** Utilizes `Map.merge()` for efficient count increments.
- **Thread Context:** Because the map is local to the `search` method and not shared across threads, standard `HashMap` performance is optimized without requiring concurrent synchronization overhead.
- **Filtering:** Documents failing the threshold check are pruned before the scoring phase begins.

---

## 5. Strategy Pattern & Complexity Analysis
The `SearchService` acts as an orchestrator for pluggable ranking algorithms.

* **Strategy Pattern:** The service depends on the `Scorer` interface, allowing runtime switching between **BM25 (Phase 4)** and **TF-IDF (Phase 2)**.
* **Top-K Optimization:** Utilizes a `PriorityQueue` (Min-Heap) to maintain the highest-scoring $K$ results.

### Performance Profile:
| Operation               | Complexity      | Description                                                           |
|:------------------------|:----------------|:----------------------------------------------------------------------|
| **Tokenization**        | $O(L)$          | $L$ is the length of the raw query string.                            |
| **Candidate Discovery** | $O(Q \times D)$ | $Q$ is query tokens; $D$ is the average number of postings per token. |
| **Top-K Ranking**       | $O(D \log K)$   | $D$ is the number of candidates; $K$ is the requested result size.    |

---

## 6. System Extensions by Phase

### Phase 3: REST API & Web Layer
* **Spring Boot Integration:** Wrapped the search logic in a web service, introducing `SearchController` for HTTP communication.
* **Parameter Mapping:** Added support for mapping query strings and numeric parameters from URL requests to service methods.

### Phase 4: Advanced Ranking & Retrieval
* **BM25 Scorer:** Implemented industry-standard probabilistic ranking with term saturation and document length normalization.
* **Tie-Breaking Logic:** Descending score as primary sort; ascending `documentId` as secondary sort for deterministic API results.

---

## 7. Future Improvements
- **OR Search:** Implementing full union logic for broader result sets.
- **WAND (Weak AND) Algorithm:** Optimization to skip scoring documents that cannot mathematically enter the Top-K results.
- **Fuzzy Search:** Integrating the `Trie` to match similar terms via Levenshtein distance.