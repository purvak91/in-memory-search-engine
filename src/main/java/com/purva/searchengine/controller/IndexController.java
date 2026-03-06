package com.purva.searchengine.controller;

import com.purva.searchengine.service.DocumentService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class IndexController {
    private final DocumentService documentService;

    public IndexController(DocumentService documentService) {
        this.documentService = documentService;
    }

    @PostMapping("/documents")
    public ResponseEntity<String> indexDocument(@RequestBody IndexRequest indexRequest) {
        int documentId = indexRequest.documentId();
        String content = indexRequest.content();

        if (content == null || content.isBlank()) {
            throw new IllegalArgumentException("Document content cannot be null or blank");
        }

        documentService.indexDocument(documentId, content);
        return ResponseEntity.status(HttpStatus.CREATED).body("Document indexed successfully");
    }
}
