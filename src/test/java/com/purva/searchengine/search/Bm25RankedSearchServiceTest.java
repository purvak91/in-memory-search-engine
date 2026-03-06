package com.purva.searchengine.search;

import com.purva.searchengine.index.InvertedIndex;
import com.purva.searchengine.service.SearchService;
import com.purva.searchengine.tokenizer.Tokenizer;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class Bm25RankedSearchServiceTest {
    private void tokenizeAndIndex(String content, int docId, Tokenizer tokenizer, InvertedIndex invertedIndex) {
        var tokens = tokenizer.tokenize(content);
        invertedIndex.index(docId, tokens);
    }

    @Test
    void shouldRankByTermFrequency() {
        var tokenizer = new Tokenizer();
        var invertedIndex = new InvertedIndex();
        var scorer = new Bm25Scorer(invertedIndex);
        SearchService searchService = new SearchService(tokenizer, invertedIndex, scorer);

        tokenizeAndIndex("java java java - practise more java", 1, tokenizer, invertedIndex);
        tokenizeAndIndex("java programming language", 2, tokenizer, invertedIndex);

        var results = searchService.rankedSearch("java", 2);

        assertEquals(2, results.size(), "Should find both documents");
        assertEquals(1, results.get(0).documentId(), "Doc 1 should be first because it has higher TF");
        assertEquals(2, results.get(1).documentId(), "Doc 2 should be second");
    }

    @Test
    void shouldRankRareTermsHigherThanCommonTerms() {
        var tokenizer = new Tokenizer();
        var invertedIndex = new InvertedIndex();
        var scorer = new Bm25Scorer(invertedIndex);
        SearchService searchService = new SearchService(tokenizer, invertedIndex, scorer);

        tokenizeAndIndex("programming programming programming programming programming microservices", 1, tokenizer, invertedIndex);
        tokenizeAndIndex("programming microservices microservices", 2, tokenizer, invertedIndex);
        tokenizeAndIndex("programming microservices", 3, tokenizer, invertedIndex);

        var results = searchService.rankedSearch("programming microservices", 2);

        assertEquals(2, results.get(0).documentId(), "Doc 2 should rank first because it has more instances of the rarer term 'microservices'");
    }

    @Test
    void shouldRankShorterDocumentHigherForSameTermFrequency() {
        var tokenizer = new Tokenizer();
        var invertedIndex = new InvertedIndex();
        var scorer = new Bm25Scorer(invertedIndex);
        SearchService searchService = new SearchService(tokenizer, invertedIndex, scorer);

        tokenizeAndIndex("java programming", 1, tokenizer, invertedIndex);
        tokenizeAndIndex("java programming is a very extensive subject that requires a lot of practice and dedication to master properly", 2, tokenizer, invertedIndex);

        var results = searchService.rankedSearch("java programming", 2);

        assertEquals(1, results.get(0).documentId(), "Doc 1 should rank first because it is more concise and dense");
    }

    @Test
    void shouldDemonstrateTermFrequencySaturation() {
        var tokenizer = new Tokenizer();
        var index = new InvertedIndex();
        var scorer = new Bm25Scorer(index);
        SearchService searchService = new SearchService(tokenizer, index, scorer);

        tokenizeAndIndex("java java", 1, tokenizer, index);
        tokenizeAndIndex("java java java java java java java java java java", 2, tokenizer, index);

        var results = searchService.rankedSearch("java", 2);

        assertEquals(2, results.get(0).documentId(), "The higher frequency doc should still win");

        double score1 = results.get(1).score();
        double score2 = results.get(0).score();

        assertTrue(score2 < (score1 * 3), "Score should saturate and not grow linearly with frequency");
    }

}
