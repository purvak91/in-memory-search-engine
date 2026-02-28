# Benchmark Results

### Performance Metrics (In-Memory)

| Operation                  | Average Latency | Min Latency | P99 Latency | Max Latency |
|----------------------------|-----------------|-------------|-------------|-------------|
| **Document Indexing**      | 2.26 µs         | 1.30 µs     | 9.00 µs     | 2842.90 µs  |
| **Boolean Search (AND)**   | 2416.18 µs      | 355.90 µs   | 5151.40 µs  | 8705.00 µs  |
| **Ranked Search (TF-IDF)** | 5109.49 µs      | 414.60 µs   | 10180.10 µs | 69680.80 µs |

### Bottleneck Analysis

The primary bottleneck is the **computational overhead of the ranking phase**. While indexing is highly efficient (sub-3µs), Ranked Search is approximately **2.1x slower** than standard Boolean search. This latency jump is caused by the transition from simple set intersection to a floating-point heavy scoring phase. Specifically, the engine must calculate TF-IDF scores for the entire postings list and then perform a global sort. The significant **Max Latency** in the ranked results ($69.68ms$) indicates that the high volume of temporary double/object allocations during scoring is likely triggering JVM Garbage Collection (GC) pauses.

### Proposed Optimizations

1. **Top-K Priority Queue:** Instead of sorting the entire result set using `Collections.sort()`, use a **Min-Heap (PriorityQueue)** of size $K$. This shifts the sorting complexity from $O(N \log N)$ to $O(N \log K)$, where $K$ is the number of requested results.
2. **Pre-computed IDF:** Move the Inverse Document Frequency (IDF) calculations to the indexing phase. By caching the `log` values and updating them only when the total document count changes significantly, we can remove redundant transcendental math from the query path.
3. **WAND (Weak AND) Algorithm:** Implement a "Max-Score" heuristic to skip scoring documents that cannot mathematically enter the Top-K results based on their term frequencies, significantly reducing the number of TF-IDF calculations performed.