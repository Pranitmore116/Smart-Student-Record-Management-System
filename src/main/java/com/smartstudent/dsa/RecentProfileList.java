package com.smartstudent.dsa;

import java.util.ArrayList;
import java.util.List;

public class RecentProfileList<T> {
    private final int capacity;
    private Node<T> head;
    private Node<T> tail;
    private int size;

    private static final class Node<T> {
        T value;
        Node<T> prev;
        Node<T> next;

        Node(T value) {
            this.value = value;
        }
    }

    public RecentProfileList(int capacity) {
        this.capacity = capacity;
    }

    public void open(T value) {
        remove(value);
        Node<T> node = new Node<>(value);
        node.next = head;
        if (head != null) head.prev = node;
        head = node;
        if (tail == null) tail = node;
        size++;
        if (size > capacity) removeTail();
    }

    public List<T> values() {
        ArrayList<T> list = new ArrayList<>();
        Node<T> current = head;
        while (current != null) {
            list.add(current.value);
            current = current.next;
        }
        return list;
    }

    private void remove(T value) {
        Node<T> current = head;
        while (current != null) {
            if (current.value.equals(value)) {
                unlink(current);
                return;
            }
            current = current.next;
        }
    }

    private void removeTail() {
        if (tail != null) unlink(tail);
    }

    private void unlink(Node<T> node) {
        if (node.prev != null) node.prev.next = node.next;
        else head = node.next;
        if (node.next != null) node.next.prev = node.prev;
        else tail = node.prev;
        size--;
    }
}
