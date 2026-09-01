package com.smartstudent.dsa;

import java.util.ArrayList;
import java.util.List;
import java.util.function.ToIntFunction;

public class AVLTree<T> {
    public static final class SnapshotNode<T> {
        public final int key;
        public final T value;
        public final int height;
        public final SnapshotNode<T> left;
        public final SnapshotNode<T> right;

        private SnapshotNode(int key, T value, int height, SnapshotNode<T> left, SnapshotNode<T> right) {
            this.key = key;
            this.value = value;
            this.height = height;
            this.left = left;
            this.right = right;
        }
    }

    private static final class Node<T> {
        int key;
        T value;
        int height = 1;
        Node<T> left;
        Node<T> right;

        Node(int key, T value) {
            this.key = key;
            this.value = value;
        }
    }

    private final ToIntFunction<T> keyExtractor;
    private final List<String> rotations = new ArrayList<>();
    private Node<T> root;

    public AVLTree(ToIntFunction<T> keyExtractor) {
        this.keyExtractor = keyExtractor;
    }

    public void clear() {
        root = null;
        rotations.clear();
    }

    public void insert(T value) {
        root = insert(root, keyExtractor.applyAsInt(value), value);
    }

    public void delete(int key) {
        root = delete(root, key);
    }

    public T search(int key) {
        Node<T> current = root;
        while (current != null) {
            if (key == current.key) return current.value;
            current = key < current.key ? current.left : current.right;
        }
        return null;
    }

    public List<T> inorder() {
        List<T> values = new ArrayList<>();
        inorder(root, values);
        return values;
    }

    public int height() {
        return height(root);
    }

    public List<String> rotations() {
        return List.copyOf(rotations);
    }

    public SnapshotNode<T> snapshot() {
        return snapshot(root);
    }

    private Node<T> insert(Node<T> node, int key, T value) {
        if (node == null) return new Node<>(key, value);
        if (key < node.key) node.left = insert(node.left, key, value);
        else if (key > node.key) node.right = insert(node.right, key, value);
        else {
            node.value = value;
            return node;
        }
        update(node);
        return balance(node);
    }

    private Node<T> delete(Node<T> node, int key) {
        if (node == null) return null;
        if (key < node.key) node.left = delete(node.left, key);
        else if (key > node.key) node.right = delete(node.right, key);
        else {
            if (node.left == null || node.right == null) {
                node = node.left == null ? node.right : node.left;
            } else {
                Node<T> min = min(node.right);
                node.key = min.key;
                node.value = min.value;
                node.right = delete(node.right, min.key);
            }
        }
        if (node == null) return null;
        update(node);
        return balance(node);
    }

    private Node<T> balance(Node<T> node) {
        int balance = balanceFactor(node);
        if (balance > 1) {
            if (balanceFactor(node.left) < 0) {
                node.left = rotateLeft(node.left);
                rotations.add("Left rotation on left child of " + node.key);
            }
            rotations.add("Right rotation at " + node.key);
            return rotateRight(node);
        }
        if (balance < -1) {
            if (balanceFactor(node.right) > 0) {
                node.right = rotateRight(node.right);
                rotations.add("Right rotation on right child of " + node.key);
            }
            rotations.add("Left rotation at " + node.key);
            return rotateLeft(node);
        }
        return node;
    }

    private Node<T> rotateRight(Node<T> y) {
        Node<T> x = y.left;
        Node<T> t2 = x.right;
        x.right = y;
        y.left = t2;
        update(y);
        update(x);
        return x;
    }

    private Node<T> rotateLeft(Node<T> x) {
        Node<T> y = x.right;
        Node<T> t2 = y.left;
        y.left = x;
        x.right = t2;
        update(x);
        update(y);
        return y;
    }

    private Node<T> min(Node<T> node) {
        while (node.left != null) node = node.left;
        return node;
    }

    private void inorder(Node<T> node, List<T> values) {
        if (node == null) return;
        inorder(node.left, values);
        values.add(node.value);
        inorder(node.right, values);
    }

    private SnapshotNode<T> snapshot(Node<T> node) {
        if (node == null) return null;
        return new SnapshotNode<>(node.key, node.value, node.height, snapshot(node.left), snapshot(node.right));
    }

    private void update(Node<T> node) {
        node.height = 1 + Math.max(height(node.left), height(node.right));
    }

    private int height(Node<T> node) {
        return node == null ? 0 : node.height;
    }

    private int balanceFactor(Node<T> node) {
        return node == null ? 0 : height(node.left) - height(node.right);
    }
}
