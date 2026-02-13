# Tokenizer Normalization Rules

This document describes the text normalization and tokenization rules used in the In-Memory Search Engine. These rules ensure consistent indexing and improve search relevance by transforming raw text into high-quality search tokens.

---

## 1. Tokenization Pipeline Overview
The tokenizer follows a deterministic, multi-stage pipeline to process input text:
1. **Normalization:** Case folding and character cleaning.
2. **Hyphen Handling:** Isolation of compound terms.
3. **Token Splitting:** Generation of raw tokens.
4. **Stopword Filtering:** Removal of high-frequency, low-meaning words.
5. **Single-Letter Filtering:** Noise reduction based on character type.
6. **Light Stemming:** Suffix stripping to improve search recall.

---

## 2. Normalization Rules

### Lowercasing
All text is converted to **lowercase** to ensure case-insensitive search (e.g., "Java" and "java" map to the same token).

### Special Character Handling
To support technical and programming-related queries, the following characters are preserved when they occur within tokens:
- `+` (e.g., `C++`)
- `.` (e.g., `node.js`)
- `#` (e.g., `C#`)
  All other non-alphanumeric characters are replaced with spaces.

### Hyphen Handling
Hyphens are replaced with spaces to split compound terms into separate, searchable tokens.
- **Example:** `"covid-19"` → `["covid", "19"]`
- **Reason:** This ensures that a search for "covid" or "19" independently will successfully find the document.

---

## 3. Filtering Rules

### Stopwords
Common English "noise" words (e.g., "the", "is", "at") are removed using a high-performance `HashSet`. These words are removed because they carry little search intent and would bloat the index.

### Single-Letter Tokens
- **Alphabetic:** Single-letter tokens (a-z) are removed to reduce noise.
- **Numeric:** Single-digit numbers (0-9) are **preserved**.
- **Reason:** Numeric tokens often represent versions (e.g., "Java 8") which are critical for search accuracy in technical contexts.

---

## 4. Light Stemming Rules
Stemming is applied only to **purely alphabetic** tokens of length > 3 to prevent over-stemming. The rules are applied in the following order:

| Suffix | Transformation | Example |
| :--- | :--- | :--- |
| `ies` | Replace with `y` | `stories` → `story` |
| `ing` | Remove suffix | `running` → `run` (handles double letters) |
| `es` | Remove suffix | `boxes` → `box` (applied if length > 4) |
| `sis` | **Exception: Preserve** | `analysis` → `analysis` |
| `s` | Remove suffix | `cats` → `cat` (applied if length > 3) |

---

## 5. Design Trade-offs

### Light Stemming vs. Porter Stemmer
I implemented a custom **Light Stemmer** instead of the industry-standard Porter Stemmer to keep the system lightweight and maintainable. While the Porter Stemmer is more linguistically accurate, the Light Stemmer covers 90% of common use cases with significantly less computational overhead.

### Preservation of Technical Tokens
By preserving `+`, `.`, and `#`, the engine is optimized for technical documentation where `C++` and `C` are entirely different search intents.

### Hyphen Splitting
While some engines preserve hyphens (e.g., "back-end"), splitting them (e.g., "back", "end") increases **Recall**, ensuring users find relevant content even if they don't use the exact hyphenation.

---

## 6. Future Improvements
- **Token Position Indexing:** Store the position of tokens to support "phrase searches."
- **Full Porter Stemmer:** Transition to a more robust algorithm for complex linguistic edge cases.
- **Multilingual Support:** Implement normalization rules for languages other than English.