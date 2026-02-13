package com.purva.searchengine.tokenizer;

import org.springframework.stereotype.Component;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

@Component
public class Tokenizer {

    private static final Pattern SPLIT_PATTERN = Pattern.compile("[^a-z0-9]+");

    public List<String> tokenize(String text) {
        if (text == null || text.isBlank()) {
            return List.of();
        }

        return Arrays.stream(SPLIT_PATTERN.split(text.toLowerCase()))
                .filter(word -> word.length() > 1)
                .filter(word -> !StopWords.ENGLISH_STOP_WORDS.contains(word))
                .toList();
    }
}