# BM25 Scorer Technical Documentation

## 1. Purpose in System
The `Bm25Scorer` serves as an advanced probabilistic ranking engine that **augments** the existing `TfIdfScorer`. By leveraging the **Strategy Pattern**, the system maintains both scorers as swappable components. BM25 implements the Okapi BM25 algorithm—the industry standard for modern search engines like Elasticsearch and Lucene—to refine document ranking by addressing two critical limitations of TF-IDF: **Term Frequency Saturation** and **Document Length Normalization**.

## 2. The Ranking Formula
The scorer calculates a relevance score for a query term $q$ in document $d$ using a probabilistic model that balances term frequency against document length:

$$score(q, d) = \text{IDF}(q) \cdot \frac{f(q, d) \cdot (k_1 + 1)}{f(q, d) + k_1 \cdot (1 - b + b \cdot \frac{|d|}{avgdl})}$$

### Formula Components:
* **IDF (Inverse Document Frequency):** $\ln(1 + \frac{N - n(q) + 0.5}{n(q) + 0.5})$
    * $N$: Total documents in the index.
    * $n(q)$: Number of documents containing term $q$.
    * **Impact:** Provides a logarithmic penalty for common terms, ensuring rare terms carry significantly more weight.
* **Term Frequency Saturation ($k_1$):** Controls the non-linear growth of the score. As term frequency $f(q, d)$ increases, the score approaches an asymptote rather than growing indefinitely.
* **Length Normalization ($b$):** Adjusts the score based on the document's length $|d|$ relative to the average document length $avgdl$ across the entire collection.



---

## 3. Key Parameters ($k_1$ and $b$)
BM25's strength lies in its tunability via two core constants:

* **$k_1$ (Typical range: 1.2 to 2.0):** The **Saturation** parameter. It determines how quickly the benefit of seeing a term multiple times diminishes. A lower $k_1$ means the score "plateaus" faster, effectively neutralizing "keyword stuffing."
* **$b$ (Typical range: 0.75):** The **Length** parameter. It scales the penalty for long documents.
    * If $b=1$, the score is fully normalized by length.
    * If $b=0$, document length is ignored entirely.
    * At **0.75**, the system favors concise documents while still rewarding the depth of longer, relevant content.

---

## 4. Why BM25 Improves on TF-IDF
| Feature           | TF-IDF Limitation                                                   | BM25 Improvement                                                                              |
|:------------------|:--------------------------------------------------------------------|:----------------------------------------------------------------------------------------------|
| **Saturation**    | Score grows linearly; 20 hits is $20\times$ better than 1 hit.      | Diminishing returns; the 20th hit adds very little extra value compared to the 1st.           |
| **Normalization** | Linear length penalty can be too aggressive or too weak.            | Uses $avgdl$ to penalize only when a doc is significantly longer than the collection average. |
| **IDF Scaling**   | Can produce negative scores for very common terms in some variants. | Uses a smoothed probabilistic IDF that remains robust across varied datasets.                 |



---

## 5. Complexity Analysis
| Operation             | Complexity | Description                                                                                     |
|:----------------------|:-----------|:------------------------------------------------------------------------------------------------|
| **Score Calculation** | $O(1)$     | Constant time retrieval of $                                                                    |d|$, $avgdl$, and $f(q, d)$ from the index per document. |
| **State Management**  | $O(1)$     | The scorer is stateless, pulling all necessary metadata from the `InvertedIndex` at query time. |

---

## 6. Future Enhancements
* **Parameter Tuning:** Implementing an A/B testing framework to optimize $k_1$ and $b$ for specific use cases (e.g., source code vs. natural language).
* **BM25+:** Integrating the BM25+ refinement to prevent the lower bound of the TF component from dropping too low for extremely long documents.