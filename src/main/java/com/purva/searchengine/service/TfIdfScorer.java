package com.purva.searchengine.service;

import com.purva.searchengine.index.InvertedIndex;
import com.purva.searchengine.index.Posting;

public class TfIdfScorer {
    private final InvertedIndex invertedIndex;

    public TfIdfScorer(InvertedIndex invertedIndex) {
        this.invertedIndex = invertedIndex;
    }

    public double score(int docId, String token) {
        Posting posting = invertedIndex.getPostingsMap(token).get(docId);
        if (posting == null) return 0.0;
        int termFrequency = posting.termFrequency();

        double documentLength = invertedIndex.getDocumentLength(docId);
        double  documentFrequency = invertedIndex.getDocumentFrequency(token);
        double totalDocuments = invertedIndex.getTotalDocuments();

        double tf = Math.log(1 + termFrequency / documentLength);
        double idf = Math.log((totalDocuments + 1) / (1 + documentFrequency)) + 1;

        return tf * idf;
    }
}
