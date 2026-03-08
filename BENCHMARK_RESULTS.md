# Benchmark Results

### Performance Metrics (In-Memory)

| Operation                     | Average Latency | Min Latency | P99 Latency | Max Latency |
|-------------------------------|-----------------|-------------|-------------|-------------|
| **Document Indexing**         | 4.75 µs         | 0.70 µs     | 14.50 µs    | 27418.90 µs |
| **Threshold Search (T=1.0)**  | 2507.15 µs      | 225.50 µs   | 6610.30 µs  | 24010.30 µs |
| **Threshold Search (T=0.75)** | 2774.32 µs      | 223.80 µs   | 7981.20 µs  | 25401.20 µs |
| **Ranked Search (TF-IDF)**    | 3893.97 µs      | 278.40 µs   | 9953.60 µs  | 20860.20 µs |
| **Ranked Search (BM25)**      | 4696.17 µs      | 306.70 µs   | 11191.40 µs | 20180.10 µs |

---

### Algorithm Comparison: TF-IDF vs. BM25 (Phase 4)

The benchmark indicates that **BM25** introduces an average latency overhead of approximately **20% (0.80 ms)** compared to TF-IDF.

#### Why the 20% Overhead?
BM25 is mathematically more intensive, requiring the engine to perform non-linear calculations for every document candidate:
1. **Document Length Normalization:** Computing the penalty or bonus based on document length relative to the global average (`avgdl`).
2. **Term Frequency Saturation:** Applying the $k_1$ parameter (1.2) to ensure that the score does not increase linearly with term frequency, preventing "keyword stuffing" bias.

---

### Bottleneck Analysis: The Cost of Flexibility

The side-by-side comparison of threshold values provides a deep look into the engine's retrieval costs:

#### 1. Discovery Phase Overhead
Transitioning from **T=1.0** to **T=0.75** resulted in a **10.6% increase** (approx. 267 µs) in average latency.
* **Reasoning:** Lowering the threshold forces the engine to aggregate and track a significantly larger set of document candidates in the local `HashMap`.
* **Evidence:** The P99 latency jumped from **6.6 ms** to **7.9 ms**, showing that the "tail end" of queries (likely long-tail or multi-token queries) suffers more as the candidate pool expands.

#### 2. Discovery vs. Scoring Allocation
By comparing `rankedSearch` against the baseline `search` method (both at T=0.75), we can isolate the cost of the ranking engine:
* **Ranking Overhead:** The transition from unranked discovery to BM25-ranked results adds **1.92 ms** of latency.
* **Conclusion:** In a full ranked query, approximately **59%** of the total execution time is dedicated to the "Candidate Discovery" pipeline (Tokenization + Map Aggregation), while **41%** is spent on "Scoring & Prioritization" (BM25 calculations + Min-Heap maintenance).

---

### Proposed Optimizations (Phase 4 & Beyond)

1. **Short-Circuit Logic for T=1.0:** Re-introduce an optimized "Strict Intersect" path that bypasses the frequency map when 100% matching is required to reclaim the performance of previous iterations.
2. **WAND (Weak AND) Algorithm:** Implement a heuristic to skip scoring documents that cannot mathematically enter the Top-K based on their maximum possible contribution.
3. **Primitive Map Implementations:** Replace the standard `HashMap<Integer, Integer>` with a primitive-specialized collection (like `fastutil`) to avoid `Integer` object wrapping and reduce GC pressure during high-frequency discovery.
4. **Top-K for Boolean Search:** Apply Min-Heap pruning to the standard `search()` method to reduce DTO creation and memory footprint for large result sets.