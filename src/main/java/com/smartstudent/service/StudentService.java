package com.smartstudent.service;

import com.smartstudent.dsa.AVLTree;
import com.smartstudent.dsa.RecentProfileList;
import com.smartstudent.dsa.SortAlgorithms;
import com.smartstudent.dsa.StudentGraph;
import com.smartstudent.dsa.Trie;
import com.smartstudent.model.Student;
import com.smartstudent.model.StudentSnapshot;
import com.smartstudent.storage.StudentStorage;

import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Set;
import java.util.Stack;
import java.util.function.ToDoubleFunction;
import java.util.stream.Collectors;

public class StudentService {
    public record Metrics(double averageCgpa, double highestCgpa, double lowestCgpa, int departments,
                          long placementReady, long lowAttendance, long scholarshipEligible, long archived,
                          long storageBytes, int treeHeight, int heapSize, double hashLoad, long memoryBytes,
                          int trieNodes, int graphNodes, int graphEdges) {
    }

    public record SearchStats(long hashMapNanos, long avlNanos, long binaryNanos, long trieNanos) {
        public double hashMapMillis() { return hashMapNanos / 1_000_000.0; }
        public double avlMillis() { return avlNanos / 1_000_000.0; }
        public double binaryMillis() { return binaryNanos / 1_000_000.0; }
        public double trieMillis() { return trieNanos / 1_000_000.0; }
    }

    private final StudentStorage storage;
    private final ArrayList<Student> students = new ArrayList<>();
    private final Map<Integer, Student> rollMap = new HashMap<>();
    private final Map<String, Student> idMap = new HashMap<>();
    private final Map<String, Student> emailMap = new HashMap<>();
    private final Set<String> phones = new HashSet<>();
    private final AVLTree<Student> avlTree = new AVLTree<>(Student::getRollNumber);
    private final Trie<Student> nameTrie = new Trie<>();
    private final StudentGraph graph = new StudentGraph();
    private final RecentProfileList<Student> recentProfiles = new RecentProfileList<>(8);
    private final Stack<StudentSnapshot> undo = new Stack<>();
    private final Stack<StudentSnapshot> redo = new Stack<>();
    private final Queue<String> notifications = new ArrayDeque<>();
    private final Queue<String> searchHistory = new ArrayDeque<>();
    private SortAlgorithms.SortResult<Student> lastSort;
    private long lastSearchNanos;
    private SearchStats lastSearchStats = new SearchStats(0, 0, 0, 0);
    private boolean autosaveEnabled = true;
    private Path exportFolderOverride;

    public StudentService(StudentStorage storage) {
        this.storage = storage;
    }

    public void load() {
        students.clear();
        students.addAll(storage.load());
        refreshComputedFields();
        rebuildIndexes();
        notify("Loaded " + students.size() + " student records");
    }

    public List<Student> students() {
        return List.copyOf(students);
    }

    public List<Student> activeStudents() {
        return students.stream().filter(s -> !s.isArchived()).toList();
    }

    public StudentStorage storage() {
        return storage;
    }

    public void setAutosaveEnabled(boolean autosaveEnabled) {
        this.autosaveEnabled = autosaveEnabled;
        notify("Autosave " + (autosaveEnabled ? "enabled" : "paused"));
    }

    public void setExportFolder(Path exportFolder) {
        this.exportFolderOverride = exportFolder;
        notify("Export folder updated");
    }

    public AVLTree<Student> avlTree() {
        return avlTree;
    }

    public Trie<Student> nameTrie() {
        return nameTrie;
    }

    public StudentGraph graph() {
        return graph;
    }

    public SortAlgorithms.SortResult<Student> lastSort() {
        return lastSort;
    }

    public long lastSearchNanos() {
        return lastSearchNanos;
    }

    public SearchStats lastSearchStats() {
        return lastSearchStats;
    }

    public void addStudent(Student student) {
        validate(student, null);
        checkpoint();
        students.add(student);
        afterMutation("Added " + student.getFullName());
    }

    public void updateStudent(Student student) {
        updateStudent(student.getRollNumber(), student);
    }

    public void updateStudent(int originalRollNumber, Student student) {
        Student old = rollMap.get(originalRollNumber);
        if (old == null) throw new IllegalArgumentException("Roll number not found");
        validate(student, old);
        checkpoint();
        int index = students.indexOf(old);
        students.set(index, student);
        afterMutation("Updated " + student.getFullName());
    }

    public void deleteStudent(int rollNumber) {
        Student student = rollMap.get(rollNumber);
        if (student == null) return;
        checkpoint();
        students.remove(student);
        afterMutation("Deleted roll " + rollNumber);
    }

    public void archiveStudent(int rollNumber) {
        Student student = rollMap.get(rollNumber);
        if (student == null) return;
        checkpoint();
        student.setArchived(true);
        afterMutation("Archived " + student.getFullName());
    }

    public void restoreStudent(int rollNumber) {
        Student student = rollMap.get(rollNumber);
        if (student == null) return;
        checkpoint();
        student.setArchived(false);
        afterMutation("Restored " + student.getFullName());
    }

    public Student cloneStudent(int rollNumber) {
        Student source = rollMap.get(rollNumber);
        if (source == null) throw new IllegalArgumentException("Roll number not found");
        Student clone = source.copy();
        int nextRoll = students.stream().mapToInt(Student::getRollNumber).max().orElse(100) + 1;
        clone.setRollNumber(nextRoll);
        clone.setStudentId(source.getStudentId() + "-CLONE");
        clone.setEmail("clone." + source.getEmail());
        clone.setPhone(source.getPhone() + "9");
        clone.setFullName(source.getFullName() + " Copy");
        addStudent(clone);
        return clone;
    }

    public int bulkImport(Path csv) {
        checkpoint();
        int added = 0;
        for (Student student : storage.importCsv(csv)) {
            if (!hasDuplicate(student, null)) {
                hydrateImportedStudent(student);
                students.add(student);
                added++;
            }
        }
        afterMutation("Imported " + added + " CSV records");
        return added;
    }

    public void undo() {
        if (undo.isEmpty()) return;
        redo.push(new StudentSnapshot(students));
        students.clear();
        students.addAll(undo.pop().restore());
        afterRestore("Undo complete");
    }

    public void redo() {
        if (redo.isEmpty()) return;
        undo.push(new StudentSnapshot(students));
        students.clear();
        students.addAll(redo.pop().restore());
        afterRestore("Redo complete");
    }

    public Optional<Student> byRoll(int rollNumber) {
        long start = System.nanoTime();
        Student student = Optional.ofNullable(rollMap.get(rollNumber)).orElse(avlTree.search(rollNumber));
        lastSearchNanos = System.nanoTime() - start;
        if (student != null) recentProfiles.open(student);
        return Optional.ofNullable(student);
    }

    public Optional<Student> byId(String id) {
        long start = System.nanoTime();
        Student student = idMap.get(normalize(id));
        lastSearchNanos = System.nanoTime() - start;
        if (student != null) recentProfiles.open(student);
        return Optional.ofNullable(student);
    }

    public List<Student> search(String query) {
        return search(query, false);
    }

    public List<Student> searchAndRemember(String query) {
        return search(query, true);
    }

    private List<Student> search(String query, boolean remember) {
        long start = System.nanoTime();
        String q = normalize(query);
        if (q.isBlank()) {
            lastSearchNanos = System.nanoTime() - start;
            return activeStudents();
        }
        if (remember) addSearchHistory(query);
        long hashStart = System.nanoTime();
        Optional<Student> direct = parseInt(q).map(rollMap::get);
        long hashNanos = System.nanoTime() - hashStart;
        long avlStart = System.nanoTime();
        parseInt(q).ifPresent(avlTree::search);
        long avlNanos = System.nanoTime() - avlStart;
        long binaryStart = System.nanoTime();
        parseInt(q).ifPresent(this::binarySearchRoll);
        long binaryNanos = System.nanoTime() - binaryStart;
        long trieStart = System.nanoTime();
        List<Student> suggestions = nameTrie.prefixSearch(q, 12);
        long trieNanos = System.nanoTime() - trieStart;
        List<Student> filtered = students.stream().filter(student ->
                String.valueOf(student.getRollNumber()).contains(q)
                        || normalize(student.getStudentId()).contains(q)
                        || normalize(student.getFullName()).contains(q)
                        || normalize(student.getDepartment()).contains(q)
                        || String.valueOf(student.getSemester()).equals(q)
                        || normalize(student.getPhone()).contains(q)
                        || normalize(student.getEmail()).contains(q)
                        || normalize(student.getSkills()).contains(q)
        ).toList();
        ArrayList<Student> merged = new ArrayList<>(suggestions);
        direct.ifPresent(merged::add);
        for (Student student : filtered) if (!merged.contains(student)) merged.add(student);
        lastSearchNanos = System.nanoTime() - start;
        lastSearchStats = new SearchStats(hashNanos, avlNanos, binaryNanos, trieNanos);
        if (remember) notify("Search: " + query);
        return merged;
    }

    public List<Student> advancedSearch(String query, String department, Integer semester, double minAttendance,
                                        double minCgpa, String placement, String scholarship, String skill) {
        return advancedSearch(query, department, semester, minAttendance, minCgpa, placement, scholarship, skill, false);
    }

    public List<Student> advancedSearch(String query, String department, Integer semester, double minAttendance,
                                        double minCgpa, String placement, String scholarship, String skill,
                                        boolean remember) {
        return search(query, remember).stream()
                .filter(s -> department == null || department.equals("All Departments") || s.getDepartment().equalsIgnoreCase(department))
                .filter(s -> semester == null || semester == 0 || s.getSemester() == semester)
                .filter(s -> s.getAttendancePercentage() >= minAttendance)
                .filter(s -> s.getCgpa() >= minCgpa)
                .filter(s -> placement == null || placement.equals("Any Placement") || s.getPlacementStatus().equalsIgnoreCase(placement))
                .filter(s -> scholarship == null || scholarship.equals("Any Scholarship") || s.getScholarshipStatus().equalsIgnoreCase(scholarship))
                .filter(s -> skill == null || skill.isBlank() || normalize(s.getSkills()).contains(normalize(skill)))
                .toList();
    }

    public List<Student> suggestions(String prefix) {
        return nameTrie.prefixSearch(prefix, 8);
    }

    public SortAlgorithms.SortResult<Student> sortBy(String field, String algorithm, boolean ascending) {
        Comparator<Student> comparator = comparator(field);
        lastSort = SortAlgorithms.sort(activeStudents(), comparator, algorithm, ascending);
        notify("%s by %s in %.2f ms".formatted(algorithm, field, lastSort.millis()));
        return lastSort;
    }

    public int binarySearchRoll(int rollNumber) {
        List<Student> sorted = avlTree.inorder();
        Student probe = new Student();
        probe.setRollNumber(rollNumber);
        return SortAlgorithms.binarySearch(sorted, probe, Comparator.comparingInt(Student::getRollNumber));
    }

    public List<Student> top(String type, int limit) {
        ToDoubleFunction<Student> score = switch (type) {
            case "Attendance" -> Student::getAttendancePercentage;
            case "Placement" -> Student::placementScore;
            case "Scholarship" -> Student::scholarshipScore;
            case "Overall" -> Student::overallScore;
            default -> Student::getCgpa;
        };
        PriorityQueue<Student> heap = new PriorityQueue<>(Comparator.comparingDouble(score).reversed());
        heap.addAll(activeStudents());
        ArrayList<Student> result = new ArrayList<>();
        while (!heap.isEmpty() && result.size() < limit) result.add(heap.poll());
        return result;
    }

    public List<Student> bottom(String type, int limit) {
        ArrayList<Student> ranked = new ArrayList<>(top(type, students.size()));
        java.util.Collections.reverse(ranked);
        return ranked.stream().limit(limit).toList();
    }

    public List<Student> scholarshipEligible() {
        return activeStudents().stream()
                .filter(s -> s.getCgpa() >= 7.5 && s.getAttendancePercentage() >= 75 && s.getBacklogs() == 0)
                .sorted(Comparator.comparingDouble(Student::scholarshipScore).reversed())
                .toList();
    }

    public Map<String, Long> departmentDistribution() {
        return activeStudents().stream().collect(Collectors.groupingBy(Student::getDepartment, LinkedHashMap::new, Collectors.counting()));
    }

    public List<String> departments() {
        return activeStudents().stream().map(Student::getDepartment).distinct().sorted().toList();
    }

    public Map<Integer, Long> semesterDistribution() {
        return activeStudents().stream().collect(Collectors.groupingBy(Student::getSemester, LinkedHashMap::new, Collectors.counting()));
    }

    public Map<String, Long> gradeDistribution() {
        Map<String, Long> distribution = new LinkedHashMap<>();
        distribution.put("9+", activeStudents().stream().filter(s -> s.getCgpa() >= 9).count());
        distribution.put("8-9", activeStudents().stream().filter(s -> s.getCgpa() >= 8 && s.getCgpa() < 9).count());
        distribution.put("7-8", activeStudents().stream().filter(s -> s.getCgpa() >= 7 && s.getCgpa() < 8).count());
        distribution.put("<7", activeStudents().stream().filter(s -> s.getCgpa() < 7).count());
        return distribution;
    }

    public Metrics metrics() {
        List<Student> active = activeStudents();
        double avg = active.stream().mapToDouble(Student::getCgpa).average().orElse(0);
        double high = active.stream().mapToDouble(Student::getCgpa).max().orElse(0);
        double low = active.stream().mapToDouble(Student::getCgpa).min().orElse(0);
        long placement = active.stream().filter(s -> s.placementScore() >= 75).count();
        long lowAttendance = active.stream().filter(s -> s.getAttendancePercentage() < 75).count();
        long scholarship = scholarshipEligible().size();
        long archived = students.stream().filter(Student::isArchived).count();
        long usedMemory = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
        return new Metrics(avg, high, low, departmentDistribution().size(), placement, lowAttendance, scholarship,
                archived, storage.storageUsageBytes(), avlTree.height(), active.size(), hashLoad(), usedMemory,
                nameTrie.nodeCount(), graph.adjacency().size(), graph.edgeCount());
    }

    public List<Student> recentProfiles() {
        return recentProfiles.values();
    }

    public List<String> notifications() {
        return new ArrayList<>(notifications);
    }

    public List<String> searchHistory() {
        ArrayList<String> history = new ArrayList<>(searchHistory);
        java.util.Collections.reverse(history);
        return history;
    }

    public void clearSearchHistory() {
        searchHistory.clear();
        notify("Search history cleared");
    }

    public Path exportCsv() {
        return storage.exportCsv(activeStudents(), exportFolder().resolve("students.csv"));
    }

    public Path exportJson() {
        return storage.exportJson(activeStudents(), exportFolder().resolve("students.json"));
    }

    public Path exportCompressed() {
        return storage.exportCompressed(activeStudents(), exportFolder().resolve("students.huff"));
    }

    public Path exportReport(String name, List<Student> rows) {
        Path output = exportFolder().resolve(name.toLowerCase().replace(" ", "-") + ".csv");
        return storage.exportCsv(rows, output);
    }

    public Path exportTranscript(Student student) {
        return storage.exportTranscript(student, exportFolder().resolve(student.getStudentId() + "-transcript.txt"));
    }

    public Path backup() {
        Path path = storage.backup(students);
        notify("Backup created: " + path.getFileName());
        return path;
    }

    public void restoreBackup(Path path) {
        checkpoint();
        students.clear();
        students.addAll(storage.restore(path));
        afterMutation("Restored backup " + path.getFileName());
    }

    private void validate(Student student, Student existing) {
        if (student.getRollNumber() <= 0) throw new IllegalArgumentException("Roll number is required");
        if (student.getStudentId().isBlank()) throw new IllegalArgumentException("Student ID is required");
        if (student.getFullName().isBlank()) throw new IllegalArgumentException("Full name is required");
        if (student.getEmail().isBlank()) throw new IllegalArgumentException("Email is required");
        if (hasDuplicate(student, existing)) throw new IllegalArgumentException("Duplicate roll, ID, email, or phone detected");
        if (student.getCgpa() < 0 || student.getCgpa() > 10) throw new IllegalArgumentException("CGPA must be between 0 and 10");
        if (student.getAttendancePercentage() < 0 || student.getAttendancePercentage() > 100) throw new IllegalArgumentException("Attendance must be between 0 and 100");
    }

    private boolean hasDuplicate(Student student, Student existing) {
        return duplicate(rollMap.get(student.getRollNumber()), existing)
                || duplicate(idMap.get(normalize(student.getStudentId())), existing)
                || duplicate(emailMap.get(normalize(student.getEmail())), existing)
                || (!student.getPhone().isBlank() && phones.contains(normalize(student.getPhone())) && (existing == null || !normalize(existing.getPhone()).equals(normalize(student.getPhone()))));
    }

    private boolean duplicate(Student found, Student existing) {
        return found != null && found != existing;
    }

    private void checkpoint() {
        undo.push(new StudentSnapshot(students));
        redo.clear();
    }

    private void afterMutation(String message) {
        refreshComputedFields();
        rebuildIndexes();
        if (autosaveEnabled) storage.save(students);
        notify(message);
    }

    private void afterRestore(String message) {
        refreshComputedFields();
        rebuildIndexes();
        if (autosaveEnabled) storage.save(students);
        notify(message);
    }

    private Path exportFolder() {
        return exportFolderOverride == null ? storage.exportFolder() : exportFolderOverride;
    }

    private void refreshComputedFields() {
        for (Student student : students) {
            student.setPlacementStatus(student.placementScore() >= 75 ? "Ready" : student.placementScore() >= 60 ? "Developing" : "Not Ready");
            student.setScholarshipStatus(student.getCgpa() >= 7.5 && student.getAttendancePercentage() >= 75 && student.getBacklogs() == 0 ? "Eligible" : "Not Eligible");
        }
    }

    private void hydrateImportedStudent(Student student) {
        if (student.getPhoto().isBlank()) student.setPhoto("avatar-" + (Math.abs(student.getRollNumber()) % 12 + 1));
        if (student.getMonthlyAttendance().isEmpty()) {
            double a = student.getAttendancePercentage();
            student.setMonthlyAttendance(List.of(Math.max(0, a - 5), Math.max(0, a - 2), a, Math.min(100, a + 2)));
        }
        if (student.getCgpaTrend().isEmpty()) {
            double c = student.getCgpa();
            student.setCgpaTrend(List.of(Math.max(0, c - 0.6), Math.max(0, c - 0.3), c));
        }
        if (student.getTimeline().isEmpty()) student.setTimeline(List.of("Imported from CSV", "Indexes rebuilt"));
    }

    private void rebuildIndexes() {
        rollMap.clear();
        idMap.clear();
        emailMap.clear();
        phones.clear();
        avlTree.clear();
        nameTrie.clear();
        for (Student student : students) {
            rollMap.put(student.getRollNumber(), student);
            idMap.put(normalize(student.getStudentId()), student);
            emailMap.put(normalize(student.getEmail()), student);
            if (!student.getPhone().isBlank()) phones.add(normalize(student.getPhone()));
            avlTree.insert(student);
            nameTrie.insert(student.getFullName(), student);
        }
        graph.rebuild(activeStudents());
    }

    private Comparator<Student> comparator(String field) {
        return switch (field) {
            case "CGPA" -> Comparator.comparingDouble(Student::getCgpa);
            case "Attendance" -> Comparator.comparingDouble(Student::getAttendancePercentage);
            case "Department" -> Comparator.comparing(Student::getDepartment, String.CASE_INSENSITIVE_ORDER);
            case "Semester" -> Comparator.comparingInt(Student::getSemester);
            case "Name" -> Comparator.comparing(Student::getFullName, String.CASE_INSENSITIVE_ORDER);
            case "Placement Score" -> Comparator.comparingDouble(Student::placementScore);
            case "Scholarship Score" -> Comparator.comparingDouble(Student::scholarshipScore);
            default -> Comparator.comparingInt(Student::getRollNumber);
        };
    }

    private double hashLoad() {
        int maps = Math.max(1, rollMap.size() + idMap.size() + emailMap.size());
        int buckets = 16;
        while (buckets < maps) buckets *= 2;
        return maps / (double) buckets;
    }

    private void notify(String message) {
        notifications.add(LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm")) + "  " + message);
        while (notifications.size() > 12) notifications.poll();
    }

    private void addSearchHistory(String query) {
        String clean = query == null ? "" : query.trim();
        if (clean.isBlank()) return;
        searchHistory.remove(clean);
        searchHistory.add(clean);
        while (searchHistory.size() > 10) searchHistory.poll();
    }

    private Optional<Integer> parseInt(String value) {
        try {
            return Optional.of(Integer.parseInt(value.trim()));
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    private String normalize(String text) {
        return text == null ? "" : text.trim().toLowerCase();
    }
}
