package com.purva.searchengine.service;

import com.purva.searchengine.index.InvertedIndex;
import com.purva.searchengine.tokenizer.Tokenizer;

import java.util.List;

public class DocumentService {
    private final Tokenizer tokenizer;
    private final InvertedIndex invertedIndex;

    public DocumentService(Tokenizer tokenizer, InvertedIndex invertedIndex) {
        this.invertedIndex = invertedIndex;
        this.tokenizer = tokenizer;
    }

    public void indexDocument(int documentId, String content) {
        if (content == null || content.isBlank()) {
            throw new IllegalArgumentException("Document content cannot be null or blank");
        }
        List<String> tokens = tokenizer.tokenize(content);
        invertedIndex.index(documentId, tokens);
    }
}