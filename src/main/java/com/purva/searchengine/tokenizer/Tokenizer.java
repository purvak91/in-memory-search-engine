package com.purva.searchengine.tokenizer;

import java.util.ArrayList;
import java.util.List;

public class Tokenizer {

    public List<String> tokenize(String text) {
        if (text == null || text.isBlank()) return List.of();

        String cleanedText = normalizeAndHandleHyphens(text);
        String[] rawTokens = cleanedText.split("\\s+");
        List<String> finalTokens = new ArrayList<>();

        for (String token : rawTokens) {
            if (token.isEmpty() || isStopWord(token) || isSingleLetterAlphabetic(token)) continue;

            finalTokens.add(applyLightStemming(token));
        }
        return finalTokens;
    }

    private String normalizeAndHandleHyphens(String text) {
        String lower = text.toLowerCase();
        String normalized = lower.replaceAll("[^a-z0-9+.#\\s-]", " ");
        return normalized.replace("-", " ");
    }

    private boolean isStopWord(String token) {
        return StopWords.ENGLISH_STOP_WORDS.contains(token);
    }

    private boolean isSingleLetterAlphabetic(String token) {
        return token.length() == 1 && Character.isLetter(token.charAt(0));
    }

    private String applyLightStemming(String word) {
        int len = word.length();
        if (len <= 3 || !containsVowel(word)) return word;
        if (!isAlphabeticWord(word)) return word;

        if (word.endsWith("ies")) {
            return word.substring(0, len - 3) + "y";
        }

        if (word.endsWith("ing")) {
            String base = word.substring(0, len - 3);
            if (base.length() > 1 && base.charAt(base.length() - 1) == base.charAt(base.length() - 2)) {
                return base.substring(0, base.length() - 1);
            }
            return base;
        }

        if (word.endsWith("es") && len > 4) {
            return word.substring(0, len - 2);
        }

        if (word.endsWith("sis")) {
            return word;
        }

        if (word.endsWith("s") && !word.endsWith("ss")) {
            return word.substring(0, len - 1);
        }

        return word;
    }

    private boolean containsVowel(String word) {
        return word.matches(".*[aeiouy].*");
    }

    private boolean isAlphabeticWord(String token) {
        return token.matches("[a-z]+");
    }
}