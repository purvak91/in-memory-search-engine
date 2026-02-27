package com.purva.searchengine;

import com.purva.searchengine.index.InvertedIndex;
import com.purva.searchengine.index.Posting;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;
import java.util.concurrent.*;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.fail;

class InvertedIndexConcurrencyTest {
    /**
     * Verifies that multiple threads can index documents simultaneously without
     * losing data or corrupting global counters.
     * * <p>This test ensures that the {@code totalDocuments} counter and the
     * {@code documentLengths} map remain synchronized even when 10 threads
     * are attempting to update them at the exact same millisecond.</p>
     * * @throws InterruptedException if the execution is interrupted during the
     * wait for the completion latch.
     */
    @Test
    void testConcurrentWrites() throws InterruptedException {
        ExecutorService executor = Executors.newFixedThreadPool(10);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch completionLatch = new CountDownLatch(10);
        InvertedIndex invertedIndex = new InvertedIndex();

        try {
            for (int i = 0; i < 10; i++) {
                final int threadIndex = i;
                executor.submit(() -> {
                    try {
                        startLatch.await();
                        for (int j = 0; j < 10; j++) {
                            int docId = (threadIndex * 10) + j + 1;
                            invertedIndex.index(docId, List.of("concurrent", "test", "indexing"));
                        }
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    } finally {
                        completionLatch.countDown();
                    }
                });
            }

            startLatch.countDown();

            boolean finished = completionLatch.await(10, TimeUnit.SECONDS);
            if (!finished) {
                fail("Test timed out! Possible deadlock.");
            }

        } finally {
            executor.shutdown();
        }

        assertEquals(100, invertedIndex.getTotalDocuments());

        assertEquals(100, invertedIndex.getDocumentFrequency("concurrent"));
        assertEquals(100, invertedIndex.getDocumentFrequency("test"));
        assertEquals(100, invertedIndex.getDocumentFrequency("indexing"));

        int[] samples = {1, 50, 100};
        for (int id : samples) {
            assertEquals(1, invertedIndex.getPostingsMap("concurrent").get(id).termFrequency());
            assertEquals(3, invertedIndex.getDocumentLength(id));
        }

        Set<Integer> actualDocIds = invertedIndex.getPostings("concurrent").stream()
                .map(Posting::documentId)
                .collect(Collectors.toSet());

        assertEquals(100, actualDocIds.size(), "Should have exactly 100 unique document IDs");
        assertTrue(actualDocIds.contains(1), "First document missing");
        assertTrue(actualDocIds.contains(100), "Last document missing");
    }

    /**
     * Verifies that a reader never observes a "partially indexed" document
     * during active write operations.
     * * <p>The test checks for internal consistency by asserting that the
     * document frequency always aligns with the size of the postings list.
     * It uses a {@link CopyOnWriteArrayList} to capture
     * any race conditions detected by reader threads.</p>
     * * @throws InterruptedException if the execution is interrupted during the
     * wait for the completion latch.
     */
    @Test
    void testConcurrentReadWrite() throws InterruptedException {
        ExecutorService executor = Executors.newFixedThreadPool(10);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch completionLatch = new CountDownLatch(10);
        InvertedIndex invertedIndex = new InvertedIndex();
        List<String> inconsistencies = new CopyOnWriteArrayList<>();

        try {
            for (int i = 0; i < 5; i++) {
                final int threadIndex = i;
                executor.submit(() -> {
                    try {
                        startLatch.await();
                        for (int j = 0; j < 10; j++) {
                            invertedIndex.index((threadIndex * 10) + j + 1, List.of("concurrent"));
                        }
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    } finally {
                        completionLatch.countDown();
                    }
                });
            }

            for (int i = 0; i < 5; i++) {
                executor.submit(() -> {
                    try {
                        startLatch.await();
                        for (int j = 0; j < 100; j++) {
                            int freqBefore = invertedIndex.getDocumentFrequency("concurrent");
                            int postingsSize = invertedIndex.getPostings("concurrent").size();

                            if (postingsSize < freqBefore) {
                                inconsistencies.add("Reader saw " + postingsSize + " postings but freq was " + freqBefore);
                            }
                        }
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    } finally {
                        completionLatch.countDown();
                    }
                });
            }

            startLatch.countDown();

            boolean finished = completionLatch.await(10, TimeUnit.SECONDS);
            if (!finished) {
                fail("Test timed out! Likely a deadlock in the ReadWriteLock.");
            }

        } finally {
            executor.shutdown();
        }

        assertTrue(inconsistencies.isEmpty(), "Inconsistencies found: " + inconsistencies);
        assertEquals(50, invertedIndex.getTotalDocuments());
    }
}
