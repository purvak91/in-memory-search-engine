package com.purva.searchengine;

import com.purva.searchengine.index.InvertedIndex;
import com.purva.searchengine.index.Posting;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class InvertedIndexTest {
    @Test
    void shouldIndexAndRetrieveTokensCorrectly() {
        InvertedIndex invertedIndex = new InvertedIndex();

        invertedIndex.index(1, List.of("java", "programming", "language"));
        invertedIndex.index(2, List.of("python", "programming", "language"));
        invertedIndex.index(3, List.of("java", "python", "programming"));

        assertTrue(invertedIndex.contains("java"));
        assertTrue(invertedIndex.contains("python"));
        assertTrue(invertedIndex.contains("programming"));
        assertFalse(invertedIndex.contains("ruby"));
        assertTrue(invertedIndex.getPostings("unknown").isEmpty());

        Collection<Posting> javaPostings = invertedIndex.getPostings("java");
        assertEquals(2, javaPostings.size());

        Collection<Posting> pythonPostings = invertedIndex.getPostings("python");
        assertEquals(2, pythonPostings.size());

        Collection<Posting> programmingPostings = invertedIndex.getPostings("programming");
        assertEquals(3, programmingPostings.size());
    }

    @Test
    void shouldIgnoreInvalidInputs() {
        InvertedIndex invertedIndex = new InvertedIndex();

        // Test null tokens
        invertedIndex.index(1, null);
        assertFalse(invertedIndex.contains("java"));

        // Test empty tokens
        invertedIndex.index(2, List.of());
        assertFalse(invertedIndex.contains("python"));

        // Test invalid document ID
        invertedIndex.index(-1, List.of("invalid", "docId"));
        assertFalse(invertedIndex.contains("invalid"));
    }

    @Test
    void frequencyAccuracyTest() {
        InvertedIndex invertedIndex = new InvertedIndex();

        invertedIndex.index(1, List.of("test", "test", "test"));
        invertedIndex.index(2, List.of("test", "test"));
        invertedIndex.index(3, List.of("test"));

        Collection<Posting> testPostings = invertedIndex.getPostings("test");
        assertEquals(3, testPostings.size());

        for (Posting posting : testPostings) {
            if (posting.documentId() == 1) {
                assertEquals(3, posting.termFrequency());
            } else if (posting.documentId() == 2) {
                assertEquals(2, posting.termFrequency());
            } else if (posting.documentId() == 3) {
                assertEquals(1, posting.termFrequency());
            } else {
                fail("Unexpected document ID: " + posting.documentId());
            }
        }
    }

    @Test
    void testMultiDocumentIndexing() {
        InvertedIndex invertedIndex = new InvertedIndex();

        invertedIndex.index(1, List.of("multi", "document", "indexing"));
        invertedIndex.index(2, List.of("multi", "indexing"));
        invertedIndex.index(3, List.of("document", "indexing"));

        assertTrue(invertedIndex.contains("multi"));
        assertTrue(invertedIndex.contains("document"));
        assertTrue(invertedIndex.contains("indexing"));

        Collection<Posting> multiPostings = invertedIndex.getPostings("multi");
        assertEquals(2, multiPostings.size());

        Collection<Posting> documentPostings = invertedIndex.getPostings("document");
        assertEquals(2, documentPostings.size());

        Collection<Posting> indexingPostings = invertedIndex.getPostings("indexing");
        assertEquals(3, indexingPostings.size());
    }

    @Test
    void testUnmodifiablePostings() {
        InvertedIndex invertedIndex = new InvertedIndex();

        invertedIndex.index(1, List.of("immutable", "postings"));
        Collection<Posting> postings = invertedIndex.getPostings("immutable");

        assertThrows(UnsupportedOperationException.class, () -> postings.add(new Posting(2, 1)));
    }
}
