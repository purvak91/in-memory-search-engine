package com.purva.searchengine;

import com.purva.searchengine.trie.Trie;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TrieTest {

    @Test
    void shouldInsertAndSearchWords() {
        Trie trie = new Trie();
        trie.insert("java");
        trie.insert("javascript");
        trie.insert("java");
        trie.insert("jacket");

        assertTrue(trie.startsWith("ja"));
        assertTrue(trie.startsWith("java"));
        assertFalse(trie.startsWith("javac"));
    }

    @Test
    void shouldReturnTopKSuggestions() {
        Trie trie = new Trie();
        trie.insert("java");
        trie.insert("javascript");
        trie.insert("java");
        trie.insert("jacket");

        var suggestions = trie.getTopKSuggestions("ja", 1);
        assertEquals(List.of("java"), suggestions);

        suggestions = trie.getTopKSuggestions("ja", 2);
        assertEquals(List.of("java", "jacket"), suggestions);

        suggestions = trie.getTopKSuggestions("ja", 3);
        assertEquals(List.of("java", "jacket", "javascript"), suggestions);
    }

    @Test
    void shouldHandleEdgeCasesForSuggestions() {
        Trie trie = new Trie();
        trie.insert("java");
        trie.insert("javascript");

        // Test with null prefix
        var suggestions = trie.getTopKSuggestions(null, 2);
        assertTrue(suggestions.isEmpty());

        // Test with short prefix
        suggestions = trie.getTopKSuggestions("j", 2);
        assertTrue(suggestions.isEmpty());

        // Test with zero k
        suggestions = trie.getTopKSuggestions("ja", 0);
        assertTrue(suggestions.isEmpty());

        // Test with negative k
        suggestions = trie.getTopKSuggestions("ja", -1);
        assertTrue(suggestions.isEmpty());
    }

    @Test
    void shouldReturnEmptyListForNonExistingPrefix() {
        Trie trie = new Trie();
        trie.insert("java");
        trie.insert("javascript");

        var suggestions = trie.getTopKSuggestions("xyz", 2);
        assertTrue(suggestions.isEmpty());
    }

    @Test
    void shouldReturnAvailableSuggestionsForKGreaterThanAvailableSuggestions() {
        Trie trie = new Trie();
        trie.insert("java");
        trie.insert("javascript");

        var suggestions = trie.getTopKSuggestions("ja", 5);
        assertEquals(List.of("java", "javascript"), suggestions);
    }

    @Test
    void shouldReturnLexicographicallySortedSuggestionsWhenFrequenciesAreEqual() {
        Trie trie = new Trie();
        trie.insert("java");
        trie.insert("javascript");
        trie.insert("java");
        trie.insert("javascript");

        var suggestions = trie.getTopKSuggestions("ja", 1);
        assertEquals(List.of("java"), suggestions);
    }
}
