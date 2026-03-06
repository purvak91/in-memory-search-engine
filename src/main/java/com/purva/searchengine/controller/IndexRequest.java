package com.purva.searchengine.controller;

public record IndexRequest(
        int documentId,
        String content
){}