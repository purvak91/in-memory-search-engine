# Search Service Technical Documentation

## 1. Purpose in System
The `SearchService` is the orchestration layer of the search engine. It acts as the "brain" that connects disparate components to fulfill a user request.

### Core Functions:
- **Component Coordination:** Integrates the `Tokenizer` for input cleaning and the `InvertedIndex` for data retrieval.
- **Query Processing:** Transforms raw user strings into searchable tokens.
- **Retrieval Logic:** Implements the logic required to find matching documents across multiple terms.

---

## 2. Query Processing Workflow
When a search is initiated, the service follows a linear execution pipeline:

1. **Tokenize Query:** The raw input is passed to the `Tokenizer`, which removes stop-words and normalizes terms.
2. **Retrieve Postings:** The service fetches the `Collection<Posting>` for each valid token from the `InvertedIndex`.
3. **Perform AND Intersection:** The service identifies the set of Document IDs that contain **all** requested tokens.
4. **Return Sorted Results:** The final list of IDs is sorted (currently by Document ID) to provide a consistent, deterministic output.

---

## 3. Retrieval Strategy Choice: The "AND" Search
For the initial implementation, a **Strict Intersection (AND)** strategy was chosen.

- **Precision:** AND search ensures that the results are highly relevant to all terms in the user's query.
- **Complexity Management:** It provides a simpler logical foundation to verify that the `Tokenizer` and `InvertedIndex` are communicating correctly.
- **Scalability:** This forms the base "filtering" layer. Ranking and "OR" logic will be layered on top of this verified foundation in the next phase.

---

## 4. Algorithm Design
The service utilizes a **Set-based Intersection** algorithm:

- **Initial State:** The Document IDs of the first token are loaded into a `Set`.
- **Iterative Refinement:** For every subsequent token, `Set.retainAll()` is called.
- **Early Exit Optimization:** If at any point the candidate set becomes empty, the service terminates the loop and returns an empty list immediately, saving unnecessary index lookups.

---

## 5. Complexity Analysis
- **Tokenization Cost:** $O(L)$ where $L$ is the length of the query string.
- **Intersection Cost:** $O(Q \times D)$ where $Q$ is the number of query tokens and $D$ is the number of documents in the smallest posting list.
- **Overall Complexity:** Dominated by the intersection logic, making it highly efficient for sparse indices.

---

## Phase 2B Extensions: Ranked Retrieval

The `SearchService` has been extended to support **Ranked Search**, transitioning from a strict boolean intersection to a scored and prioritized result set using statistical heuristics.

### 1. The `rankedSearch` Methodology
While the standard `search` method returns all matching documents, `rankedSearch` introduces relevance scoring and result limits.
* **Scoring Integration:** The service utilizes the `TfIdfScorer` to calculate a cumulative score for each document based on all query tokens.
* **Input Validation:** The method includes defensive checks to ensure `topK` parameters are positive, preventing runtime errors during heap initialization.

### 2. Top-K Optimization: The Min-Heap Approach
To efficiently find the most relevant documents without sorting the entire candidate set, the service implements a **Min-PriorityQueue**.
* **Efficiency:** By maintaining a heap of size `topK`, the service achieves $O(D \log K)$ complexity (where $D$ is the number of candidate documents), which is significantly faster than sorting the full result set when $D$ is large.
* **Memory Management:** If the heap exceeds the `topK` size, the lowest-scoring document is polled, ensuring only the most relevant "winners" remain in memory.

### 3. Ranking and Tie-Breaking Logic
Once the top candidates are identified, they are converted to a list and sorted for final delivery.
* **Primary Sort:** Documents are ordered by their **TF-IDF score** in descending order.
* **Secondary Sort (Tie-Breaking):** In cases where two documents have identical statistical scores, the service applies a deterministic tie-breaker using the `documentId` in ascending order. This ensures consistent results across multiple API calls.

### 4. Complexity Analysis (Ranked)
* **Time Complexity:** $O(Q \times D + D \log K)$, where $Q$ is query length, $D$ is the number of candidate documents, and $K$ is the requested result count.
* **Space Complexity:** $O(K)$ additional space for the priority queue.

---

## 6. Future Improvements
The current design is built to be extensible for the following features:
- **OR Search:** Implementing "Union" logic to broaden result sets.
- **Threshold Matching:** Implementing "Match $X\%$" logic (e.g., return documents that match 3 out of 4 words).
- **Fuzzy Search:** Integrating the `Trie` to suggest or match similar terms when a direct match isn't found.