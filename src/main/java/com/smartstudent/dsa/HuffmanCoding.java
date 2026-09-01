package com.smartstudent.dsa;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.BitSet;
import java.util.HashMap;
import java.util.Map;
import java.util.PriorityQueue;

public final class HuffmanCoding {
    public record Payload(Map<Byte, String> codes, byte[] compressed, int bitLength) implements Serializable {
    }

    private static final class Node implements Comparable<Node>, Serializable {
        byte value;
        int frequency;
        Node left;
        Node right;

        Node(byte value, int frequency) {
            this.value = value;
            this.frequency = frequency;
        }

        Node(Node left, Node right) {
            this.frequency = left.frequency + right.frequency;
            this.left = left;
            this.right = right;
        }

        boolean leaf() {
            return left == null && right == null;
        }

        @Override
        public int compareTo(Node other) {
            return Integer.compare(frequency, other.frequency);
        }
    }

    private HuffmanCoding() {
    }

    public static Payload compress(byte[] data) {
        if (data.length == 0) return new Payload(Map.of(), new byte[0], 0);
        Map<Byte, Integer> frequencies = new HashMap<>();
        for (byte b : data) frequencies.merge(b, 1, Integer::sum);
        PriorityQueue<Node> queue = new PriorityQueue<>();
        frequencies.forEach((value, count) -> queue.add(new Node(value, count)));
        while (queue.size() > 1) queue.add(new Node(queue.poll(), queue.poll()));
        Map<Byte, String> codes = new HashMap<>();
        buildCodes(queue.peek(), "", codes);
        BitSet bits = new BitSet();
        int index = 0;
        for (byte b : data) {
            String code = codes.get(b);
            for (char c : code.toCharArray()) {
                if (c == '1') bits.set(index);
                index++;
            }
        }
        return new Payload(codes, bits.toByteArray(), index);
    }

    public static byte[] decompress(Payload payload) {
        if (payload.bitLength == 0) return new byte[0];
        Map<String, Byte> reverse = new HashMap<>();
        payload.codes.forEach((value, code) -> reverse.put(code, value));
        BitSet bits = BitSet.valueOf(payload.compressed);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        StringBuilder current = new StringBuilder();
        for (int i = 0; i < payload.bitLength; i++) {
            current.append(bits.get(i) ? '1' : '0');
            Byte value = reverse.get(current.toString());
            if (value != null) {
                out.write(value);
                current.setLength(0);
            }
        }
        return out.toByteArray();
    }

    public static void writeCompressed(Path path, byte[] data) throws IOException {
        Files.createDirectories(path.getParent());
        try (ObjectOutputStream output = new ObjectOutputStream(Files.newOutputStream(path))) {
            output.writeObject(compress(data));
        }
    }

    public static byte[] readCompressed(Path path) throws IOException, ClassNotFoundException {
        try (ObjectInputStream input = new ObjectInputStream(Files.newInputStream(path))) {
            return decompress((Payload) input.readObject());
        }
    }

    private static void buildCodes(Node node, String code, Map<Byte, String> codes) {
        if (node.leaf()) {
            codes.put(node.value, code.isEmpty() ? "0" : code);
            return;
        }
        buildCodes(node.left, code + "0", codes);
        buildCodes(node.right, code + "1", codes);
    }
}
