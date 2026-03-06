package com.purva.searchengine.search;

import com.purva.searchengine.index.InvertedIndex;
import com.purva.searchengine.index.Posting;

public class Bm25Scorer implements Scorer {
    private final InvertedIndex invertedIndex;
    private static final double k1 = 1.5;
    private static final double b = 0.75;

    public Bm25Scorer(InvertedIndex invertedIndex) {
        this.invertedIndex = invertedIndex;
    }

    @Override
    public double score(int docId, String token) {
        Posting posting = invertedIndex.getPostingsMap(token).get(docId);
        if (posting == null) return 0.0;
        int termFrequency = posting.termFrequency();
        double documentLength = invertedIndex.getDocumentLength(docId);
        double averageDocumentLength = invertedIndex.getAverageDocumentLength();
        double documentFrequency = invertedIndex.getDocumentFrequency(token);
        double totalDocuments = invertedIndex.getTotalDocuments();
        double idf = Math.log((totalDocuments - documentFrequency + 0.5) / (documentFrequency + 0.5) + 1);

        return idf * ((k1 + 1) * termFrequency) / (k1 * (1 - b + b * (documentLength / averageDocumentLength)) + termFrequency);
    }
}
