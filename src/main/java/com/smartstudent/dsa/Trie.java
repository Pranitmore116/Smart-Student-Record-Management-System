package com.smartstudent.dsa;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class Trie<T> {
    public static final class SnapshotNode {
        public final char label;
        public final boolean terminal;
        public final List<SnapshotNode> children;

        private SnapshotNode(char label, boolean terminal, List<SnapshotNode> children) {
            this.label = label;
            this.terminal = terminal;
            this.children = children;
        }
    }

    private static final class Node<T> {
        Map<Character, Node<T>> children = new TreeMap<>();
        LinkedHashSet<T> values = new LinkedHashSet<>();
        boolean terminal;
    }

    private final Node<T> root = new Node<>();

    public void clear() {
        root.children.clear();
        root.values.clear();
        root.terminal = false;
    }

    public void insert(String word, T value) {
        if (word == null || word.isBlank()) return;
        Node<T> current = root;
        for (char ch : word.toLowerCase().toCharArray()) {
            if (Character.isWhitespace(ch)) continue;
            current = current.children.computeIfAbsent(ch, ignored -> new Node<>());
            current.values.add(value);
        }
        current.terminal = true;
    }

    public List<T> prefixSearch(String prefix, int limit) {
        Node<T> node = root;
        String clean = prefix == null ? "" : prefix.toLowerCase().replaceAll("\\s+", "");
        for (char ch : clean.toCharArray()) {
            node = node.children.get(ch);
            if (node == null) return List.of();
        }
        return node.values.stream().limit(limit).toList();
    }

    public int nodeCount() {
        return count(root);
    }

    public SnapshotNode snapshot() {
        return snapshot('\0', root);
    }

    private SnapshotNode snapshot(char label, Node<T> node) {
        List<SnapshotNode> children = new ArrayList<>();
        node.children.forEach((ch, child) -> children.add(snapshot(ch, child)));
        return new SnapshotNode(label, node.terminal, children);
    }

    private int count(Node<T> node) {
        int total = 1;
        for (Node<T> child : node.children.values()) total += count(child);
        return total;
    }
}
