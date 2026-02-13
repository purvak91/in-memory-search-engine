package com.purva.searchengine.tokenizer;

import java.util.Set;

public class StopWords {
    private StopWords(){}

    public static final Set<String> ENGLISH_STOP_WORDS = Set.of(
            "a", "an", "and", "are", "as", "at", "be", "but", "by", "for",
            "if", "in", "into", "is", "it", "no", "not", "of", "on", "or",
            "such", "than", "that", "the", "their", "then", "there", "these",
            "they", "this", "to", "was", "will", "with", "i", "you", "we", "he", "she"
    );
}
