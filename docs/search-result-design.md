# Search Result Technical Documentation

## 1. Purpose
The `SearchResult` class is a lightweight data carrier used to represent a single document match after ranking has been applied. It bundles the identity of a document with its calculated relevance score, allowing the `SearchService` to return ordered results to the user.

## 2. Design Choice: Java Record
This component is implemented as a Java **record** rather than a traditional class for the following reasons:

* **Immutability:** Once a score is calculated and a result is created, it should not be modified. Records are inherently immutable.
* **Boilerplate Reduction:** Records automatically provide implementations for `equals()`, `hashCode()`, and `toString()`, keeping the codebase clean and focused on logic rather than ceremony.
* **Data-Centric Design:** As a pure "Data Transfer Object" (DTO), it exists only to move data from the `Scorer` and `SearchService` to the UI/API layer.

---

## 3. Field Definitions
The record consists of two primary components:

| Field        | Type     | Description                                                                                     |
|:-------------|:---------|:------------------------------------------------------------------------------------------------|
| `documentId` | `int`    | The unique identifier for the document in the `InvertedIndex`.                                  |
| `score`      | `double` | The calculated weight (e.g., TF-IDF) representing the document's relevance to the search query. |

---

## 4. Usage in Ranking
In the `SearchService`, `SearchResult` objects are managed within a **PriorityQueue** to efficiently track the "Top-K" most relevant documents. This allows the system to process large candidate sets while only maintaining a sorted list of the highest-scoring matches.