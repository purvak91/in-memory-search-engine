# Inverted Index Technical Documentation

## 1. Purpose in System
The `InvertedIndex` serves as the primary retrieval engine of the search system. While the **Trie** is responsible for character-level discovery and autocomplete, the `InvertedIndex` handles the mapping of processed tokens to their source documents.

### Role in Pipeline:
1. **Tokenizer** processes raw text into normalized stems.
2. **InvertedIndex** maps these stems to Document IDs and calculates local relevance via Term Frequency.
3. **Search Service** queries the Index to retrieve and rank documents for the user.

---

## 2. Data Structure Design
The index is implemented using a **Nested Map Structure**:
`Map<String, Map<Integer, Posting>>`

* **Primary Map (Key: Token):** Provides $O(1)$ average-case lookup to find any word in the index.
* **Secondary Map (Key: DocID):** Prevents duplicate entries for the same document and allows $O(1)$ updates to term frequencies during indexing.
* **Posting (Record):** An immutable data carrier that ensures search results cannot be modified by external services, maintaining the integrity of the index.

---

## 3. Indexing Algorithm
The indexing process follows a "Collect and Map" strategy:
1. **Frequency Counting:** A local `termFreqMap` is built for each document to count occurrences of every token.
    * *Definition:* **Term Frequency (TF)** represents the number of times a token appears within a single document.
2. **Batch Update:** The system iterates through the frequency map and updates the global index.
3. **Efficiency:** By calculating frequencies locally before touching the global index, we minimize the number of `computeIfAbsent` calls and map lookups.



---

## 4. Retrieval Logic
* **Constant Time Lookup:** Retrieving a posting list is an $O(1)$ operation regardless of the number of unique words in the system.
* **Encapsulation:** The `getPostings` method returns an `unmodifiableCollection`. This **defensive wrapping approach** provides a read-only view of the data, ensuring that external components can access results but cannot modify the internal index state.

---

## 5. Design Trade-offs
* **Nested Map vs. List:** I chose a nested `Map<Integer, Posting>` over a `List<Posting>`. While a list is more memory-efficient, a map allows for **idempotent indexing** (re-indexing the same document updates counts instead of creating duplicate entries).
* **No Position Storage:** Currently, the index does not store the exact character position of words. This saves memory but limits the engine to keyword matching rather than phrase searches.
* **Memory vs. Persistence:** This is an in-memory index offering extreme speed at the cost of durability (index must be rebuilt on restart).

---

## 6. Complexity Analysis
| Operation       | Complexity      | Description                                                               |
|:----------------|:----------------|:--------------------------------------------------------------------------|
| **Indexing**    | $O(N)$          | Where $N$ is the number of tokens in the document.                        |
| **Word Lookup** | $O(1)$          | Average case for `HashMap` lookup.                                        |
| **Memory**      | $O(T \times D)$ | $T$ = number of unique terms, $D$ = average number of documents per term. |

---

## Phase 2B Extensions: Scoring Metadata

To support the transition from boolean retrieval to statistical ranking, the `InvertedIndex` was expanded to track global and local document statistics.

### 1. New State Management
Two new internal structures were added to support the **TF-IDF** calculation:
* **`documentLengths` (Map<Integer, Integer>):** Stores the total token count for each document. This is critical for **Length Normalization**, ensuring that relevance scores are adjusted based on document size.
* **`totalDocuments` (AtomicInteger):** Tracks the global count of unique documents indexed. This provides the $N$ value for the **Inverse Document Frequency (IDF)** formula.

### 2. Analytical Methods
The following methods were implemented to expose index statistics to the `TfIdfScorer`:
* **`getDocumentFrequency(token)`:** Returns the number of documents containing the specific term ($DF$).
* **`getDocumentLength(docId)`:** Retrieves the total token count for a specific document.
* **`getTotalDocuments()`:** Returns the total count of documents currently in the index.

### 3. Re-indexing Behavior: `put` vs `computeIfAbsent`
In the `index` method, the logic for document length tracking uses `put` rather than `computeIfAbsent`:
```bash
documentLengths.put(docId, tokens.size());
```
**Rationale**: This design choice ensures idempotency. If a document with an existing `documentId` is re-indexed with updated content, the `put` operation will overwrite the old length with the new one. This ensures the index remains synchronized with the most recent version of the data rather than ignoring updates.

### 4. Atomic Consistency
The use of `AtomicInteger` for `totalDocuments` ensures that the document count remains accurate during concurrent indexing operations, preventing race conditions when multiple threads update the index.

---

## Phase 2C Extensions: Concurrency & Thread Safety

In this phase, the `InvertedIndex` was upgraded from a single-threaded data structure to a thread-safe component capable of handling high-traffic search environments.

### 1. Read-Write Optimization

We implemented a `ReentrantReadWriteLock` to optimize for search-heavy workloads. This strategy maximizes throughput by allowing multiple "readers" to work simultaneously while still protecting "writers."

* **Read Lock:** Acquired during `getPostings()` and `getDocumentFrequency()`. Multiple users can search and retrieve statistics at the same time without contention.
* **Write Lock:** Acquired during the `index()` method. This ensures that while a document is being added, no other thread can read or modify the index, preventing data corruption.
* **Lock-Free Reads:** The `getTotalDocuments()` method is intentionally **lock-free**. Because `totalDocuments` is stored as an `AtomicInteger`, it provides thread-safe access to the global count without the overhead of the ReadWriteLock.

### 2. Ensuring Atomic Updates

A single call to `index()` updates three distinct internal structures: the postings map, the document length map, and the global document counter. Without proper locking, a reader could see a "partial" update—for example, finding a document in the search results before its length has been recorded. The `writeLock` makes these multi-step updates **atomic**, ensuring the index is always in a consistent state.

### 3. Verification via Stress Testing

Thread safety was verified through a dedicated test suite (`InvertedIndexConcurrencyTest`) using the following patterns:

* **The Starting Pistol (Contention):** Using `CountDownLatch` to force 10+ threads to "rush" the write lock at the exact same millisecond.
* **Read/Write Consistency:** Simultaneous reader and writer threads verify that the document frequency always matches the actual number of postings, even during heavy update bursts.
* **Resource Management:** Ensuring that the `ExecutorService` is properly shut down in `finally` blocks to prevent thread leaks during testing.

---

## 7. Future Improvements
* **Positional Indexing:** Adding an `ArrayList<Integer>` to the `Posting` record to store word positions for phrase matching.
