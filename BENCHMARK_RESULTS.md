# Benchmark Results

### Performance Metrics (In-Memory)

| Operation                  | Average Latency | Min Latency | P99 Latency | Max Latency |
|----------------------------|-----------------|-------------|-------------|-------------|
| **Document Indexing**      | 3.96 µs         | 1.30 µs     | 18.70 µs    | 5100.80 µs  |
| **Boolean Search (AND)**   | 3454.72 µs      | 373.90 µs   | 8896.30 µs  | 30604.70 µs |
| **Ranked Search (TF-IDF)** | 5410.87 µs      | 420.80 µs   | 10537.10 µs | 31895.80 µs |
| **Ranked Search (BM25)**   | 6460.48 µs      | 498.40 µs   | 12997.80 µs | 24720.80 µs |

---

### Algorithm Comparison: TF-IDF vs. BM25

The benchmark results indicate that **BM25** introduces an average latency overhead of approximately **19% (1.05 ms)** compared to TF-IDF.

#### Why the 19% Overhead?
BM25 is mathematically more intensive than TF-IDF. For every document match, the engine must perform three additional operations:
1. **Document Length Lookup:** Retrieving the specific word count for the document being scored.
2. **Global Average Fetch:** Accessing the `avgdl` (Average Document Length) from the index.
3. **Normalization Math:** Applying the $b$ parameter (0.75) and $k_1$ parameter (1.5) to calculate the saturation curve and length penalty.


#### Why BM25 is the Superior Choice
Despite the minor micro-latency increase, BM25 provides industry-standard relevance:
* **Saturation:** It prevents "keyword stuffing" where a document repeating a word 100 times unfairly outranks a more concise result.
* **Length Normalization:** It ensures that a 1,000-word document doesn't rank higher than a 10-word title just because it has more "room" for keywords.
* **Stability:** Interestingly, the **Max Latency** for BM25 was lower in this run ($24.7$ ms vs $31.8$ ms), suggesting that the algorithm is robust even as computational complexity increases.

---

### Bottleneck Analysis

The primary bottleneck remains the **scoring and object allocation phase**.
* **Scoring Overhead:** Transitioning from simple Boolean intersection to a ranked search nearly doubles average latency.
* **Boolean Search Latency:** Standard Boolean search is surprisingly high (~3.4ms). This suggests that the current `search()` implementation for full intersections could benefit from the same Top-K/Heap pruning used in the ranked path.
* **Memory Spikes:** Max latencies ($>24$ ms) indicate that creating thousands of `SearchResult` objects and `Double` wrappers is triggering JVM Garbage Collection pauses.

---

### Proposed Optimizations

1. **Top-K for Boolean Search:** Extend the Min-Heap logic to the standard `search()` method. Currently, Boolean search returns all matching IDs; limiting this to a Top-K view would reduce memory pressure and DTO creation.
2. **Pre-computed avgdl:** Cache the average document length in the `InvertedIndex` during indexing so it does not need to be recalculated or fetched repeatedly during the query path.
3. **WAND (Weak AND) Algorithm:** Implement a "Max-Score" heuristic to skip scoring documents that cannot mathematically enter the Top-K results, reducing the total number of floating-point operations.
4. **Primitive Collections:** Use specialized collections (like `fastutil`) to store scores as primitive `double` values rather than `Double` objects, reducing heap allocation and GC frequency.