package com.purva.searchengine.trie;

import java.util.HashMap;
import java.util.Map;

public class TrieNode {
    Map<Character, TrieNode> children;
    boolean isEndOfWord;
    int frequency;

    TrieNode() {
        children = new HashMap<>();
        isEndOfWord = false;
        frequency = 0;
    }
}
