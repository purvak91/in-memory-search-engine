package com.purva.searchengine;

import com.purva.searchengine.tokenizer.Tokenizer;
import org.junit.jupiter.api.Test;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TokenizerTest {

    private final Tokenizer tokenizer = new Tokenizer();

    @Test
    void shouldReturnCleanTokensForStandardSentence() {
        String input = "The 2 Quick brown foxes!";
        List<String> tokens = tokenizer.tokenize(input);

        assertEquals(3, tokens.size(), "Should have exactly 3 tokens");
        assertTrue(tokens.contains("quick"));
        assertTrue(tokens.contains("brown"));
        assertTrue(tokens.contains("foxes"));
    }

    @Test
    void shouldReturnEmptyListForNullOrEmptyInput() {
        assertTrue(tokenizer.tokenize(null).isEmpty());
        assertTrue(tokenizer.tokenize("   ").isEmpty());
    }

    @Test
    void shouldFilterOutSingleLettersAndStopWords() {
        String input = "a and the is i";
        List<String> tokens = tokenizer.tokenize(input);

        assertTrue(tokens.isEmpty(), "Should be empty as all words are noise");
    }
}