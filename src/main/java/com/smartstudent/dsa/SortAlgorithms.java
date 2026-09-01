package com.smartstudent.dsa;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public final class SortAlgorithms {
    public record SortResult<T>(List<T> values, String algorithm, long nanos, long comparisons, long swaps) {
        public double millis() {
            return nanos / 1_000_000.0;
        }
    }

    private static final class Counter {
        long comparisons;
        long swaps;
    }

    private SortAlgorithms() {
    }

    public static <T> SortResult<T> sort(List<T> input, Comparator<T> comparator, String algorithm, boolean ascending) {
        Comparator<T> effective = ascending ? comparator : comparator.reversed();
        ArrayList<T> values = new ArrayList<>(input);
        Counter counter = new Counter();
        long start = System.nanoTime();
        switch (algorithm) {
            case "Quick Sort" -> quickSort(values, 0, values.size() - 1, effective, counter);
            case "Heap Sort" -> heapSort(values, effective, counter);
            default -> values = mergeSort(values, effective, counter);
        }
        long elapsed = System.nanoTime() - start;
        return new SortResult<>(values, algorithm, elapsed, counter.comparisons, counter.swaps);
    }

    public static <T> int binarySearch(List<T> sorted, T target, Comparator<T> comparator) {
        int low = 0;
        int high = sorted.size() - 1;
        while (low <= high) {
            int mid = (low + high) >>> 1;
            int cmp = comparator.compare(sorted.get(mid), target);
            if (cmp == 0) return mid;
            if (cmp < 0) low = mid + 1;
            else high = mid - 1;
        }
        return -1;
    }

    private static <T> ArrayList<T> mergeSort(ArrayList<T> values, Comparator<T> comparator, Counter counter) {
        if (values.size() <= 1) return values;
        int mid = values.size() / 2;
        ArrayList<T> left = mergeSort(new ArrayList<>(values.subList(0, mid)), comparator, counter);
        ArrayList<T> right = mergeSort(new ArrayList<>(values.subList(mid, values.size())), comparator, counter);
        return merge(left, right, comparator, counter);
    }

    private static <T> ArrayList<T> merge(ArrayList<T> left, ArrayList<T> right, Comparator<T> comparator, Counter counter) {
        ArrayList<T> merged = new ArrayList<>(left.size() + right.size());
        int i = 0;
        int j = 0;
        while (i < left.size() && j < right.size()) {
            counter.comparisons++;
            if (comparator.compare(left.get(i), right.get(j)) <= 0) merged.add(left.get(i++));
            else {
                merged.add(right.get(j++));
                counter.swaps++;
            }
        }
        merged.addAll(left.subList(i, left.size()));
        merged.addAll(right.subList(j, right.size()));
        return merged;
    }

    private static <T> void quickSort(ArrayList<T> values, int low, int high, Comparator<T> comparator, Counter counter) {
        if (low >= high) return;
        int pivot = partition(values, low, high, comparator, counter);
        quickSort(values, low, pivot - 1, comparator, counter);
        quickSort(values, pivot + 1, high, comparator, counter);
    }

    private static <T> int partition(ArrayList<T> values, int low, int high, Comparator<T> comparator, Counter counter) {
        T pivot = values.get(high);
        int i = low - 1;
        for (int j = low; j < high; j++) {
            counter.comparisons++;
            if (comparator.compare(values.get(j), pivot) <= 0) {
                i++;
                swap(values, i, j, counter);
            }
        }
        swap(values, i + 1, high, counter);
        return i + 1;
    }

    private static <T> void heapSort(ArrayList<T> values, Comparator<T> comparator, Counter counter) {
        int n = values.size();
        for (int i = n / 2 - 1; i >= 0; i--) heapify(values, n, i, comparator, counter);
        for (int i = n - 1; i > 0; i--) {
            swap(values, 0, i, counter);
            heapify(values, i, 0, comparator, counter);
        }
    }

    private static <T> void heapify(ArrayList<T> values, int n, int i, Comparator<T> comparator, Counter counter) {
        int best = i;
        int left = 2 * i + 1;
        int right = 2 * i + 2;
        if (left < n) {
            counter.comparisons++;
            if (comparator.compare(values.get(left), values.get(best)) > 0) best = left;
        }
        if (right < n) {
            counter.comparisons++;
            if (comparator.compare(values.get(right), values.get(best)) > 0) best = right;
        }
        if (best != i) {
            swap(values, i, best, counter);
            heapify(values, n, best, comparator, counter);
        }
    }

    private static <T> void swap(ArrayList<T> values, int a, int b, Counter counter) {
        if (a == b) return;
        T temp = values.get(a);
        values.set(a, values.get(b));
        values.set(b, temp);
        counter.swaps++;
    }
}
