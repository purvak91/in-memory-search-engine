# Tf-Idf Scorer Technical Documentation

## 1. Purpose in System
The `TfIdfScorer` is the primary ranking engine of the search system. It transforms the search engine from a binary "matches/no-match" model into a **statistical heuristic model**. By calculating the relative importance of terms, it allows the `SearchService` to order results so that the most relevant documents appear at the top of the list.

## 2. The Ranking Formula
The scorer implements a normalized variation of the **Term Frequency-Inverse Document Frequency** algorithm.

$$score(t, d) = \text{TF}(t, d) \times \text{IDF}(t)$$

### Formula Components:
* **TF (Term Frequency):** `Math.log(1 + termFrequency / documentLength)`
    * **Length Normalization:** Dividing by `documentLength` prevents long documents from gaining an unfair advantage simply because they contain more words.
    * **Logarithmic Scaling:** The `log(1 + count)` approach prevents a single term that appears many times from over-dominating the total score.
* **IDF (Inverse Document Frequency):** `Math.log((totalDocuments + 1) / (1 + documentFrequency)) + 1`
    * **Significance Weighting:** This penalizes common "noise" words and heavily weights rare, descriptive terms.
    * **Smoothing:** The `+1` additions ensure the IDF score remains positive and prevent division-by-zero errors.

---

## 3. Design Decisions & Trade-offs
* **Stateless Execution:** The scorer is a stateless utility depending on the `InvertedIndex`, making it thread-safe and lightweight.
* **Heuristic vs. Probabilistic:** Chose TF-IDF as a robust statistical baseline. While it lacks the formal probabilistic foundations of models like BM25, it is highly effective for general-purpose keyword ranking.
* **Known Limitation (Length Bias):** A key trade-off in this implementation is that the linear length normalization can occasionally favor very short documents. If a short document contains a keyword once, it may receive a higher TF score than a much longer, more focused document where the keyword appears multiple times.
* **Decoupled Architecture:** Separating scoring logic from the `SearchService` allows the system to transition to more advanced models (like BM25) without altering the core retrieval pipeline.

---

## 4. Complexity Analysis
| Operation | Complexity | Description |
| :--- | :--- | :--- |
| **Score Calculation** | $O(1)$ | Retrieval of pre-calculated frequencies and lengths from the index happens in constant time. |
| **Memory Footprint** | $O(1)$ | The class only stores a reference to the `InvertedIndex`, adding no significant memory overhead. |

---

## 5. Future Improvements
* **BM25 Integration:** Implementing the Okapi BM25 algorithm to better handle term frequency saturation and refine length normalization logic.
* **Field Weighting:** Allowing different weights for matches found in titles versus body text.