package com.purva.searchengine.config;

import com.purva.searchengine.index.InvertedIndex;
import com.purva.searchengine.search.Bm25Scorer;
import com.purva.searchengine.search.Scorer;
import com.purva.searchengine.service.DocumentService;
import com.purva.searchengine.service.SearchService;
import com.purva.searchengine.tokenizer.Tokenizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AppConfig {
    @Bean
    public Tokenizer tokenizer() {
        return new Tokenizer();
    }

    @Bean
    public InvertedIndex invertedIndex() {
        return new InvertedIndex();
    }

    @Bean
    public Scorer scorer(InvertedIndex invertedIndex) {
        return new Bm25Scorer(invertedIndex);
    }

    @Bean
    public SearchService searchService(Tokenizer tokenizer, InvertedIndex invertedIndex, Scorer scorer) {
        return new SearchService(tokenizer, invertedIndex, scorer);
    }

    @Bean
    public DocumentService documentService(Tokenizer tokenizer, InvertedIndex invertedIndex) {
        return new DocumentService(tokenizer, invertedIndex);
    }
}
