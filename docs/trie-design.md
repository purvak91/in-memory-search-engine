# Trie-Based Suggestion Engine Design

## 1️⃣ Purpose of Trie in System
The Trie (Prefix Tree) serves as the core component for our **Autocomplete and Search Suggestion** feature.
* **Autocomplete Support:** Enables real-time feedback as the user types by quickly identifying all words sharing a common prefix.
* **Separation from Inverted Index:** While the Inverted Index handles full-text document retrieval, the Trie is optimized specifically for string-based prefix matching. The Trie does not store document references to avoid duplication of index data and to keep memory usage minimal.
* **Speed:** Provides O(L) lookups (where L is word length), ensuring low-latency suggestions during active typing.

## 2️⃣ Data Structure Design
The system uses a standard `TrieNode` architecture with specific optimizations:
* **TrieNode Fields:** Each node contains a `Map<Character, TrieNode>` for children, a `boolean isEndOfWord`, and an `int frequency`.
* **Frequency Significance:** Frequency represents the number of times a token appears during indexing and is used as a proxy for suggestion relevance.
* **Why Map instead of Array:** Using a `HashMap` is more memory-efficient for sparse datasets compared to a fixed-size 26-character array, and it naturally supports Unicode/Special characters.
* **Frequency Storage:** Frequency is only updated at the `isEndOfWord` node to minimize metadata overhead on intermediate prefix nodes.



## 3️⃣ Core Operations
* **`insert(String word)`:** Traverses the tree character by character, creating nodes as needed and incrementing frequency at the leaf.
* **`findNode(String prefix)`:** Navigates to the specific node representing the end of the user's input prefix.
* **`getTopKSuggestions(String prefix, int k)`:** Locates the prefix node and initiates a discovery process for the most relevant completions.

## 4️⃣ Suggestion Ranking Algorithm
To return the most relevant results, we use a **Bounded Min-Heap** approach:
* **DFS Traversal:** Starting from the prefix node, we perform a Depth-First Search to find all valid words in that subtree.
* **Min-Heap Usage:** We maintain a `PriorityQueue` of size K. If the heap exceeds size K, we remove the element with the lowest frequency. This ensures that only the "Top K" highest-frequency words remain.
* **Tie-Breaking:** If frequencies are identical, we use lexicographical ordering as a secondary sort.

## 5️⃣ Design Trade-offs
* **Map vs. Array:** Chose `Map` for flexibility with character sets, acknowledging a slight overhead in pointer storage.
* **Dynamic Building vs. Full Storage:** Words are reconstructed dynamically during DFS using a `StringBuilder` to save memory, rather than storing the full string at every node.
* **Heap vs. Sorting:** Using a Min-Heap during DFS is O(N log K), which is significantly more efficient than gathering all N possible words and sorting them (O(N log N)), especially when N is large and K is small.

## 6️⃣ Complexity Analysis
| Operation | Time Complexity | Space Complexity |
| :--- | :--- | :--- |
| **Insert** | O(L) | O(L) |
| **Prefix Search** | O(P) | O(1) additional space |
| **Top-K Retrieval** | O(N log K) | O(H + K) |

*(Where L=word length, P=prefix length, N=total nodes in subtree, H=tree height, K=number of suggestions)*

## 7️⃣ Future Improvements
* **Fuzzy Search:** Implement Levenshtein distance or deletion-based matching to suggest words even with typos.
* **Caching Suggestions:** Store the Top-K results directly on each `TrieNode` to turn O(N log K) retrieval into O(1) at the cost of higher memory.
* **Thread Safety:** Use `ConcurrentHashMap` or Read-Write locks if the Trie needs to be updated by multiple ingestion threads simultaneously.