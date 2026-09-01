package com.smartstudent.dsa;

import com.smartstudent.model.Student;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

public class StudentGraph {
    private final Map<String, Set<String>> adjacency = new LinkedHashMap<>();

    public void rebuild(List<Student> students) {
        adjacency.clear();
        students.forEach(student -> adjacency.put(student.getStudentId(), new LinkedHashSet<>()));
        for (int i = 0; i < students.size(); i++) {
            for (int j = i + 1; j < students.size(); j++) {
                Student a = students.get(i);
                Student b = students.get(j);
                if (related(a, b)) connect(a.getStudentId(), b.getStudentId());
            }
        }
    }

    public Map<String, Set<String>> adjacency() {
        return adjacency;
    }

    public int edgeCount() {
        return adjacency.values().stream().mapToInt(Set::size).sum() / 2;
    }

    public List<String> bfs(String start) {
        if (!adjacency.containsKey(start)) return List.of();
        List<String> order = new ArrayList<>();
        Set<String> visited = new HashSet<>();
        Queue<String> queue = new ArrayDeque<>();
        visited.add(start);
        queue.add(start);
        while (!queue.isEmpty()) {
            String node = queue.poll();
            order.add(node);
            for (String next : adjacency.getOrDefault(node, Set.of())) {
                if (visited.add(next)) queue.add(next);
            }
        }
        return order;
    }

    public List<String> dfs(String start) {
        List<String> order = new ArrayList<>();
        dfs(start, new HashSet<>(), order);
        return order;
    }

    public List<List<String>> components() {
        List<List<String>> components = new ArrayList<>();
        Set<String> visited = new HashSet<>();
        for (String node : adjacency.keySet()) {
            if (visited.contains(node)) continue;
            List<String> component = new ArrayList<>();
            collect(node, visited, component);
            components.add(component);
        }
        return components;
    }

    public List<String> shortestConnection(String from, String to) {
        if (!adjacency.containsKey(from) || !adjacency.containsKey(to)) return List.of();
        Queue<String> queue = new ArrayDeque<>();
        Map<String, String> parent = new HashMap<>();
        Set<String> visited = new HashSet<>();
        queue.add(from);
        visited.add(from);
        while (!queue.isEmpty()) {
            String current = queue.poll();
            if (current.equals(to)) break;
            for (String next : adjacency.getOrDefault(current, Set.of())) {
                if (visited.add(next)) {
                    parent.put(next, current);
                    queue.add(next);
                }
            }
        }
        if (!from.equals(to) && !parent.containsKey(to)) return List.of();
        ArrayList<String> path = new ArrayList<>();
        for (String at = to; at != null; at = parent.get(at)) {
            path.add(0, at);
            if (at.equals(from)) break;
        }
        return path;
    }

    private void dfs(String node, Set<String> visited, List<String> order) {
        if (!visited.add(node)) return;
        order.add(node);
        for (String next : adjacency.getOrDefault(node, Set.of())) dfs(next, visited, order);
    }

    private void collect(String node, Set<String> visited, List<String> component) {
        if (!visited.add(node)) return;
        component.add(node);
        for (String next : adjacency.getOrDefault(node, Set.of())) collect(next, visited, component);
    }

    private void connect(String a, String b) {
        adjacency.computeIfAbsent(a, ignored -> new LinkedHashSet<>()).add(b);
        adjacency.computeIfAbsent(b, ignored -> new LinkedHashSet<>()).add(a);
    }

    private boolean related(Student a, Student b) {
        if (!a.getDepartment().isBlank() && a.getDepartment().equalsIgnoreCase(b.getDepartment())) return true;
        if (sharesToken(a.getProjects(), b.getProjects())) return true;
        return sharesToken(a.getSkills(), b.getSkills());
    }

    private boolean sharesToken(String left, String right) {
        Set<String> tokens = tokenize(left);
        tokens.retainAll(tokenize(right));
        return !tokens.isEmpty();
    }

    private Set<String> tokenize(String text) {
        Set<String> set = new HashSet<>();
        if (text == null) return set;
        for (String token : text.toLowerCase().split(",")) {
            String clean = token.trim();
            if (!clean.isBlank()) set.add(clean);
        }
        return set;
    }
}
