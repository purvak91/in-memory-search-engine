package com.purva.searchengine.trie;

import java.util.*;

public class Trie {
    private final TrieNode root = new TrieNode();

    public void insert(String word) {
        TrieNode current = root;
        for (char c : word.toCharArray()) {
            if (!current.children.containsKey(c)) {
                current.children.put(c, new TrieNode());
            }
            current = current.children.get(c);
        }
        current.isEndOfWord = true;
        current.frequency++;
    }

    public boolean startsWith(String prefix) {
        return findNode(prefix) != null;
    }

    private record Suggestion(String word, int frequency) {}

    private void dfs(TrieNode node, StringBuilder prefix, PriorityQueue<Suggestion> pq, int k) {
        if (node.isEndOfWord) {
            pq.offer(new Suggestion(prefix.toString(), node.frequency));
            if (pq.size() > k) {
                pq.poll();
            }
        }

        for (var entry : node.children.entrySet()) {
            prefix.append(entry.getKey());
            dfs(entry.getValue(), prefix, pq, k);
            prefix.deleteCharAt(prefix.length() - 1);
        }
    }

    public List<String> getTopKSuggestions(String prefix, int k) {
        if (k <= 0) return List.of();
        if (prefix == null || prefix.length() < 2) return List.of();

        TrieNode node = findNode(prefix);
        if (node == null) {
            return List.of();
        }

        PriorityQueue<Suggestion> minHeap = new PriorityQueue<>(
                Comparator.comparingInt((Suggestion s) -> s.frequency)
                        .thenComparing(s -> s.word, Comparator.reverseOrder())
        );

        dfs(node, new StringBuilder(prefix), minHeap, k);

        List<String> suggestions = new java.util.ArrayList<>();
        for (int i = 0; i < k && !minHeap.isEmpty(); i++) {
            suggestions.add(minHeap.poll().word);
        }

        Collections.reverse(suggestions);

        return suggestions;
    }

    private TrieNode findNode(String prefix) {
        TrieNode current = root;
        if (prefix == null || prefix.isEmpty()) return root;

        for (char c : prefix.toCharArray()) {
            TrieNode next = current.children.get(c);
            if (next == null) return null;
            current = next;
        }
        return current;
    }
}
