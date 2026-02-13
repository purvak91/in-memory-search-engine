package com.purva.searchengine;

import com.purva.searchengine.tokenizer.Tokenizer;
import org.junit.jupiter.api.Test;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TokenizerTest {

    private final Tokenizer tokenizer = new Tokenizer();

    @Test
    void shouldReturnCleanTokensForStandardSentence() {
        String input = "The quick brown fox jumps over the lazy dog";
        List<String> tokens = tokenizer.tokenize(input);

        assertEquals(List.of("quick", "brown", "fox", "jump", "over", "lazy", "dog"), tokens);
    }

    @Test
    void shouldReturnSingleDigitTokens() {
        String input = "Java 8 is better than Java 7 in 2024";
        List<String> tokens = tokenizer.tokenize(input);

        assertEquals(List.of("java", "8", "better", "java", "7", "2024"), tokens);
    }

    @Test
    void shouldHandleProgrammingLanguageTokens() {
        String input = "C#, C++ and Node.js are popular programming languages";
        List<String> tokens = tokenizer.tokenize(input);

        assertEquals(List.of("c#", "c++", "node.js", "popular", "program", "languag"), tokens);
    }

    @Test
    void shouldHandleHyphenatedWords() {
        String input = "State-of-the-art technology is evolving rapidly";
        List<String> tokens = tokenizer.tokenize(input);

        assertEquals(List.of("state", "art", "technology", "evolv", "rapidly"), tokens);
    }

    @Test
    void shouldReturnStemmedTokens() {
        String input = "Stories Running Boxes Cats and runs";
        List<String> tokens = tokenizer.tokenize(input);

        assertEquals(List.of("story", "run", "box", "cat", "run"), tokens);
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
        assertEquals(List.of(), tokens);
    }
}