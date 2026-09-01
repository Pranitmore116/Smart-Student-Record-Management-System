package com.smartstudent.ui;

import com.smartstudent.dsa.AVLTree;
import com.smartstudent.dsa.SortAlgorithms;
import com.smartstudent.model.Student;
import com.smartstudent.service.StudentService;
import javafx.animation.FadeTransition;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Side;
import javafx.scene.Node;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Slider;
import javafx.scene.control.Spinner;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.util.Duration;

import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

public class MainController {
    private final StudentService service;
    private final BorderPane root = new BorderPane();
    private final VBox sidebar = new VBox(8);
    private final StackPane content = new StackPane();
    private final Label title = new Label("Dashboard");
    private final Label subtitle = new Label("Offline desktop record intelligence");
    private TableView<Student> table;
    private boolean animationsEnabled = true;
    private double animationMillis = 180;
    private double currentFontSize = 13;
    private String currentTheme = "Dark Premium";
    private String currentAccent = "Teal";
    private boolean autosaveEnabled = true;
    private Path customExportFolder;

    public MainController(StudentService service) {
        this.service = service;
        buildShell();
        show("Dashboard");
    }

    public BorderPane getRoot() {
        return root;
    }

    private void buildShell() {
        root.getStyleClass().add("app-root");
        sidebar.getStyleClass().add("sidebar");
        sidebar.setPadding(new Insets(22, 14, 18, 14));
        sidebar.setPrefWidth(230);

        Label brand = new Label("Smart Records");
        brand.getStyleClass().add("brand");
        Label edition = new Label("DSA Desktop Suite");
        edition.getStyleClass().add("muted");
        sidebar.getChildren().addAll(brand, edition, spacer(8));

        for (String item : List.of("Dashboard", "Students", "Search", "Sorting", "Analytics", "Attendance", "Ranking",
                "Scholarship", "Prediction", "Reports", "Backup & Restore", "DSA Visualization", "Settings", "About")) {
            Button button = navButton(item);
            sidebar.getChildren().add(button);
        }
        Region grow = new Region();
        VBox.setVgrow(grow, Priority.ALWAYS);
        sidebar.getChildren().addAll(grow, statusPill("Offline", "Local JSON + CSV + Huffman"));

        VBox main = new VBox(16);
        main.getStyleClass().add("main");
        main.setPadding(new Insets(20, 24, 24, 24));
        HBox header = new HBox(14);
        header.setAlignment(Pos.CENTER_LEFT);
        VBox titles = new VBox(4, title, subtitle);
        title.getStyleClass().add("page-title");
        subtitle.getStyleClass().add("muted");
        Region headerGrow = new Region();
        HBox.setHgrow(headerGrow, Priority.ALWAYS);
        TextField quick = new TextField();
        quick.setPromptText("Quick search");
        quick.getStyleClass().add("quick-search");
        quick.textProperty().addListener((obs, old, value) -> {
            if (value.length() > 1) showSearch(value);
        });
        Button add = iconButton("+", "Add student");
        add.setOnAction(e -> openEditor(null));
        header.getChildren().addAll(titles, headerGrow, quick, add);
        VBox.setVgrow(content, Priority.ALWAYS);
        main.getChildren().addAll(header, content);
        root.setLeft(sidebar);
        root.setCenter(main);
    }

    private Button navButton(String item) {
        Button button = new Button(item);
        button.getStyleClass().add("nav-button");
        button.setMaxWidth(Double.MAX_VALUE);
        button.setAlignment(Pos.CENTER_LEFT);
        button.setOnAction(e -> show(item));
        return button;
    }

    private void show(String page) {
        Node view;
        try {
            view = pageView(page);
        } catch (Exception ex) {
            view = pageErrorView(page, ex);
        }
        title.setText(page);
        subtitle.setText(switch (page) {
            case "Dashboard" -> "Live institutional overview and system status";
            case "DSA Visualization" -> "AVL, Trie, Heap, Graph, Stack, Queue, HashMap, sorting and search";
            case "Students" -> "Manage complete student profiles";
            case "Search" -> "Trie suggestions, indexed lookup, filters, and search performance";
            case "Sorting" -> "Compare stable and fast sorting algorithms with live metrics";
            case "Analytics" -> "Department, semester, grade, placement, and scholarship intelligence";
            case "Attendance" -> "Attendance trends, risk students, and department comparisons";
            case "Ranking" -> "Heap-powered top performers, podiums, and score badges";
            case "Scholarship" -> "Eligibility rules, priority queue ranking, and progress meters";
            case "Prediction" -> "Rule-based placement, risk, and scholarship confidence";
            case "Reports" -> "Professional exports, transcripts, and compressed archives";
            case "Backup & Restore" -> "Verified offline snapshots with restore history";
            case "Settings" -> "Personalize theme, export, autosave, and animation behavior";
            default -> "Smart offline tools backed by data structures";
        });
        setContent(view);
    }

    private Node pageView(String page) {
        return switch (page) {
            case "Dashboard" -> dashboard();
            case "Students" -> studentsView(service.activeStudents());
            case "Search" -> searchView("");
            case "Sorting" -> sortingView();
            case "Analytics" -> analyticsView();
            case "Attendance" -> attendanceView();
            case "Ranking" -> rankingView();
            case "Scholarship" -> scholarshipView();
            case "Prediction" -> predictionView();
            case "Reports" -> reportsView();
            case "Backup & Restore" -> backupView();
            case "DSA Visualization" -> visualizerView();
            case "Settings" -> settingsView();
            case "About" -> aboutView();
            default -> dashboard();
        };
    }

    private Node pageErrorView(String page, Exception ex) {
        return scroller(new VBox(14,
                card(page + " could not be opened", "View Error", "The previous page was not reused."),
                textPanel("Technical Details", List.of(
                        ex.getClass().getSimpleName(),
                        ex.getMessage() == null ? "No exception message was provided." : ex.getMessage(),
                        "Please rerun the app after the latest build if this appears in an already-open window."))));
    }

    private void setContent(Node node) {
        content.getChildren().setAll(node);
        if (!animationsEnabled) {
            node.setOpacity(1);
            return;
        }
        FadeTransition fade = new FadeTransition(Duration.millis(animationMillis), node);
        fade.setFromValue(0.2);
        fade.setToValue(1);
        fade.play();
    }

    private ScrollPane dashboard() {
        StudentService.Metrics m = service.metrics();
        GridPane grid = responsiveGrid();
        grid.add(card("Total Students", String.valueOf(service.activeStudents().size()), "ArrayList primary store"), 0, 0);
        grid.add(card("Average CGPA", fmt(m.averageCgpa()), "Median " + fmt(m.averageCgpa())), 1, 0);
        grid.add(card("Highest CGPA", fmt(m.highestCgpa()), "AVL ordered lookup"), 2, 0);
        grid.add(card("Low Attendance", String.valueOf(m.lowAttendance()), "Attendance alerts"), 3, 0);
        grid.add(card("Placement Ready", String.valueOf(m.placementReady()), "Rule-based scoring"), 0, 1);
        grid.add(card("Scholarship Eligible", String.valueOf(m.scholarshipEligible()), "Priority queue ranking"), 1, 1);
        grid.add(card("Departments", String.valueOf(m.departments()), "Hash grouped analysis"), 2, 1);
        grid.add(card("Storage", bytes(m.storageBytes()), "Autosaved offline"), 3, 1);

        HBox charts = new HBox(16, departmentPie(), cgpaBars(), trendChart());
        charts.getChildren().forEach(n -> HBox.setHgrow(n, Priority.ALWAYS));

        HBox quick = new HBox(12,
                card("Today", LocalDate.now().format(DateTimeFormatter.ofPattern("dd MMM yyyy")), LocalDateTime.now().format(DateTimeFormatter.ofPattern("hh:mm a"))),
                card("Dataset Size", String.valueOf(service.students().size()), "Active + archived records"),
                card("AVL Height", String.valueOf(m.treeHeight()), "Balanced roll-number index"),
                card("Graph Links", compactNumber(m.graphEdges()), "Relationship network edges"));

        HBox lower = new HBox(16, listPanel("Top Performers", service.top("CGPA", 5)),
                textPanel("Recent Activities", service.notifications()),
                systemPanel());
        lower.getChildren().forEach(n -> HBox.setHgrow(n, Priority.ALWAYS));

        Button addStudent = action("Add Student", () -> openEditor(null));
        addStudent.setTooltip(new Tooltip("Create a new student profile"));
        Button backupNow = action("Backup Now", () -> {
            Path path = service.backup();
            alertInfo("Backup created:\n" + path.toAbsolutePath());
            show("Dashboard");
        });
        backupNow.setTooltip(new Tooltip("Create a timestamped JSON backup in data/backups"));
        Button exportJson = action("Export JSON", () -> {
            Path path = service.exportJson();
            alertInfo("JSON exported:\n" + path.toAbsolutePath());
            show("Dashboard");
        });
        exportJson.setTooltip(new Tooltip("Export active student records to data/exports/students.json"));
        Button openSearch = action("Open Search", () -> show("Search"));
        openSearch.setTooltip(new Tooltip("Open indexed search and filters"));
        VBox box = new VBox(16,
                toolbar(addStudent, backupNow, exportJson, openSearch),
                grid, quick, charts, lower,
                textPanel("Recent Backups", service.storage().backups().stream().limit(5).map(path -> path.getFileName().toString()).toList()));
        return scroller(box);
    }

    private Node studentsView(List<Student> rows) {
        table = studentTable(rows);
        HBox actions = toolbar(
                action("Add", () -> openEditor(null)),
                action("Edit", () -> selected().ifPresent(this::openEditor)),
                action("Clone", () -> selected().ifPresent(s -> {
                    service.cloneStudent(s.getRollNumber());
                    show("Students");
                })),
                action("Archive", () -> selected().ifPresent(s -> {
                    service.archiveStudent(s.getRollNumber());
                    show("Students");
                })),
                action("Restore", () -> selected().ifPresent(s -> {
                    service.restoreStudent(s.getRollNumber());
                    show("Students");
                })),
                action("Delete", () -> selected().ifPresent(s -> {
                    if (confirm("Delete " + s.getFullName() + "?")) {
                        service.deleteStudent(s.getRollNumber());
                        show("Students");
                    }
                })),
                action("Import CSV", this::importCsv),
                action("Export CSV", service::exportCsv),
                action("Undo", () -> {
                    service.undo();
                    show("Students");
                }),
                action("Redo", () -> {
                    service.redo();
                    show("Students");
                })
        );
        VBox box = new VBox(12, actions, table);
        VBox.setVgrow(table, Priority.ALWAYS);
        return box;
    }

    private Node searchView(String initialQuery) {
        TextField search = new TextField();
        search.setText(initialQuery);
        search.setPromptText("Search roll, name, department, semester, phone, email, or skills");
        ComboBox<String> department = new ComboBox<>(FXCollections.observableArrayList(service.departments()));
        department.getItems().add(0, "All Departments");
        department.setValue("All Departments");
        ComboBox<Integer> semester = new ComboBox<>(FXCollections.observableArrayList(0, 1, 2, 3, 4, 5, 6, 7, 8));
        semester.setValue(0);
        ComboBox<String> placement = combo("Any Placement", "Ready", "Developing", "Not Ready");
        ComboBox<String> scholarship = combo("Any Scholarship", "Eligible", "Not Eligible");
        TextField skill = new TextField();
        skill.setPromptText("Skill filter");
        Slider minAttendance = new Slider(0, 100, 0);
        Slider minCgpa = new Slider(0, 10, 0);
        minAttendance.setPrefWidth(230);
        minCgpa.setPrefWidth(230);
        ListView<String> suggestions = new ListView<>();
        suggestions.setFixedCellSize(42);
        suggestions.setMaxHeight(190);
        suggestions.setMinHeight(48);
        suggestions.getStyleClass().add("suggestion-list");
        suggestions.setPlaceholder(new Label("No name suggestions"));
        ListView<String> history = new ListView<>();
        history.setMaxHeight(104);
        history.getStyleClass().add("compact-list");
        history.setPlaceholder(new Label("No saved searches yet. Type a query and click Save Search."));
        VBox savedPanel = savedSearchesPanel(history);
        Button saveSearch = action("Save Search", () -> { });
        Button clearSaved = action("Clear Saved", () -> { });
        TableView<Student> results = studentTable(service.activeStudents());
        Label time = new Label("Search time: 0 ms | HashMap 0 ms | AVL 0 ms | Binary 0 ms | Trie 0 ms");
        time.getStyleClass().add("muted");
        final Runnable[] refreshHistoryRef = new Runnable[1];
        Runnable refreshHistory = () -> {
            List<String> saved = service.searchHistory();
            history.setItems(FXCollections.observableArrayList(saved));
            savedPanel.setVisible(!saved.isEmpty());
            savedPanel.setManaged(!saved.isEmpty());
            clearSaved.setDisable(saved.isEmpty());
        };
        refreshHistoryRef[0] = refreshHistory;
        Runnable runSearch = () -> {
            List<Student> found = service.advancedSearch(search.getText(), department.getValue(), semester.getValue(),
                    minAttendance.getValue(), minCgpa.getValue(), placement.getValue(), scholarship.getValue(), skill.getText());
            results.setItems(FXCollections.observableArrayList(found));
            List<String> names = search.getText().isBlank()
                    ? List.of()
                    : service.suggestions(search.getText()).stream().map(Student::getFullName).toList();
            suggestions.setItems(FXCollections.observableArrayList(names));
            suggestions.setManaged(!names.isEmpty());
            suggestions.setVisible(!names.isEmpty());
            suggestions.setPrefHeight(names.isEmpty() ? 0 : Math.min(190, Math.max(54, names.size() * 42 + 12)));
            var stats = service.lastSearchStats();
            time.setText("Search %.3f ms | HashMap %.3f ms | AVL %.3f ms | Binary %.3f ms | Trie %.3f ms"
                    .formatted(service.lastSearchNanos() / 1_000_000.0, stats.hashMapMillis(), stats.avlMillis(), stats.binaryMillis(), stats.trieMillis()));
        };
        Runnable submitSearch = () -> {
            if (search.getText().isBlank()) return;
            service.advancedSearch(search.getText(), department.getValue(), semester.getValue(),
                    minAttendance.getValue(), minCgpa.getValue(), placement.getValue(), scholarship.getValue(), skill.getText(), true);
            refreshHistory.run();
            runSearch.run();
        };
        saveSearch.setOnAction(e -> submitSearch.run());
        clearSaved.setOnAction(e -> {
            service.clearSearchHistory();
            refreshHistoryRef[0].run();
        });
        saveSearch.setDisable(search.getText().isBlank());
        history.setOnMouseClicked(e -> {
            String saved = history.getSelectionModel().getSelectedItem();
            if (saved != null && !saved.isBlank()) {
                search.setText(saved);
                runSearch.run();
            }
        });
        search.setOnAction(e -> submitSearch.run());
        search.textProperty().addListener((obs, old, value) -> {
            saveSearch.setDisable(value.isBlank());
            runSearch.run();
        });
        department.setOnAction(e -> runSearch.run());
        semester.setOnAction(e -> runSearch.run());
        placement.setOnAction(e -> runSearch.run());
        scholarship.setOnAction(e -> runSearch.run());
        skill.textProperty().addListener((obs, old, value) -> runSearch.run());
        minAttendance.valueProperty().addListener((obs, old, value) -> runSearch.run());
        minCgpa.valueProperty().addListener((obs, old, value) -> runSearch.run());
        HBox filterRow = new HBox(10, department, semester, placement, scholarship, skill);
        HBox rangeRow = new HBox(18, sliderGroup("Min Attendance", minAttendance), sliderGroup("Min CGPA", minCgpa));
        VBox filters = new VBox(12, filterRow, rangeRow);
        HBox searchActions = toolbar(saveSearch, clearSaved);
        VBox box = new VBox(12, glass(search), glass(filters), suggestions, time,
                searchActions,
                savedPanel,
                results);
        VBox.setVgrow(results, Priority.ALWAYS);
        refreshHistory.run();
        runSearch.run();
        return box;
    }

    private void showSearch(String query) {
        title.setText("Search");
        subtitle.setText("Trie suggestions, indexed lookup, filters, and search performance");
        setContent(searchView(query));
    }

    private Node sortingView() {
        ComboBox<String> field = combo("Roll Number", "CGPA", "Attendance", "Department", "Semester", "Name", "Placement Score", "Scholarship Score");
        ComboBox<String> algorithm = combo("Merge Sort", "Quick Sort", "Heap Sort");
        CheckBox asc = new CheckBox("Ascending");
        asc.setSelected(true);
        Label stats = new Label("Run a sort to measure execution time, comparisons, and swaps.");
        stats.getStyleClass().add("muted");
        ProgressBar progress = new ProgressBar(0);
        progress.setPrefWidth(420);
        Label step = new Label("Preview step: waiting for sorted output");
        step.getStyleClass().add("muted");
        VBox algorithmInfo = algorithmInfo(algorithm.getValue());
        TableView<Student> results = studentTable(service.activeStudents());
        BarChart<String, Number> preview = sortingPreviewChart(service.activeStudents(), field.getValue(), 18);
        AtomicReference<SortAlgorithms.SortResult<Student>> lastResult = new AtomicReference<>();
        final int[] previewCount = {0};
        Button run = action("Run Sort", () -> {
            var sorted = service.sortBy(field.getValue(), algorithm.getValue(), asc.isSelected());
            lastResult.set(sorted);
            previewCount[0] = Math.min(18, sorted.values().size());
            results.setItems(FXCollections.observableArrayList(sorted.values()));
            stats.setText("%s | %.3f ms | comparisons %d | swaps %d | %s"
                    .formatted(sorted.algorithm(), sorted.millis(), sorted.comparisons(), sorted.swaps(), stabilityText(sorted.algorithm())));
            progress.setProgress(1);
            step.setText("Completed " + sorted.algorithm() + " over " + sorted.values().size() + " records.");
            refreshSortingPreview(preview, sorted.values(), field.getValue(), previewCount[0]);
        });
        algorithm.setOnAction(e -> algorithmInfo.getChildren().setAll(algorithmInfo(algorithm.getValue()).getChildren()));
        field.setOnAction(e -> refreshSortingPreview(preview,
                lastResult.get() == null ? service.activeStudents() : lastResult.get().values(), field.getValue(), 18));
        VBox box = new VBox(12,
                toolbar(field, algorithm, asc, run,
                action("Step Preview", () -> {
                    if (lastResult.get() == null) run.fire();
                    SortAlgorithms.SortResult<Student> sorted = lastResult.get();
                    if (sorted == null) return;
                    previewCount[0] = Math.min(sorted.values().size(), previewCount[0] + 18);
                    refreshSortingPreview(preview, sorted.values(), field.getValue(), previewCount[0]);
                    progress.setProgress(sorted.values().isEmpty() ? 0 : previewCount[0] / (double) sorted.values().size());
                    step.setText("Previewing first " + previewCount[0] + " sorted records. Metrics above are from the completed algorithm run.");
                }),
                action("Reset", () -> {
                    lastResult.set(null);
                    previewCount[0] = 0;
                    results.setItems(FXCollections.observableArrayList(service.activeStudents()));
                    progress.setProgress(0);
                    stats.setText("Run a sort to measure execution time, comparisons, and swaps.");
                    step.setText("Preview step: reset to original dataset order.");
                    refreshSortingPreview(preview, service.activeStudents(), field.getValue(), 18);
                })),
                stats, progress, step,
                new HBox(14, algorithmInfo, preview),
                results);
        VBox.setVgrow(results, Priority.ALWAYS);
        return box;
    }

    private Node analyticsView() {
        HBox charts = new HBox(16, departmentPie(), cgpaBars(), semesterBars());
        VBox stats = new VBox(12,
                card("Pass Percentage", percent(service.activeStudents().stream().filter(s -> s.getCgpa() >= 5).count(), service.activeStudents().size()), "CGPA >= 5"),
                card("Placement Percentage", percent(service.metrics().placementReady(), service.activeStudents().size()), "Placement score >= 75"),
                card("Scholarship Percentage", percent(service.metrics().scholarshipEligible(), service.activeStudents().size()), "Eligible by rules"));
        return scroller(new VBox(16, charts, stats));
    }

    private Node attendanceView() {
        BarChart<String, Number> bar = barChart("Student", "Attendance %");
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        service.activeStudents().forEach(s -> series.getData().add(new XYChart.Data<>(s.getFullName().split(" ")[0], s.getAttendancePercentage())));
        bar.getData().add(series);
        return scroller(new VBox(16, toolbar(action("Export Attendance Report", () -> service.exportReport("Attendance Report", service.activeStudents()))),
                card("Low Attendance Alerts", String.valueOf(service.metrics().lowAttendance()), "Students below 75%"),
                bar,
                listPanel("Top Attendance", service.top("Attendance", 10))));
    }

    private Node rankingView() {
        ComboBox<String> type = combo("CGPA", "Attendance", "Placement", "Scholarship", "Overall");
        TableView<Student> results = studentTable(service.top(type.getValue(), 10));
        HBox podium = podium(type.getValue());
        type.setOnAction(e -> results.setItems(FXCollections.observableArrayList(service.top(type.getValue(), 10))));
        type.setOnAction(e -> {
            results.setItems(FXCollections.observableArrayList(service.top(type.getValue(), 10)));
            podium.getChildren().setAll(podium(type.getValue()).getChildren());
        });
        return new VBox(12, toolbar(type, action("Export Ranking", () -> service.exportReport(type.getValue() + " Ranking", service.top(type.getValue(), 10)))),
                podium,
                textPanel("Ranking Badges", List.of("Top 3 podium uses a max heap", "Department and semester toppers are derived from the same ranking pipeline", "Trend indicator compares CGPA, attendance, placement, and scholarship scores")),
                results);
    }

    private Node scholarshipView() {
        long eligible = service.metrics().scholarshipEligible();
        long total = service.activeStudents().size();
        return new VBox(12,
                toolbar(action("Export Scholarship Report", () -> service.exportReport("Scholarship Report", service.scholarshipEligible()))),
                new HBox(12,
                        card("Eligibility Rules", "CGPA 7.5+ | Attendance 75%+ | No backlogs", "Priority queue ranks eligible students"),
                        meter("Eligible Students", total == 0 ? 0 : eligible * 100.0 / total),
                        card("Priority Queue", String.valueOf(eligible), "Highest scholarship score first")),
                textPanel("Rule Evaluation", List.of("Academic rule: CGPA must be 7.5 or higher", "Attendance rule: attendance must be 75% or higher", "Backlog rule: zero active backlogs", "Income category boosts priority score")),
                studentTable(service.scholarshipEligible()));
    }

    private Node predictionView() {
        TableView<Student> results = studentTable(service.activeStudents());
        TableColumn<Student, String> placement = new TableColumn<>("Placement Readiness");
        placement.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getPlacementStatus() + " (" + fmt(c.getValue().placementScore()) + ")"));
        TableColumn<Student, String> risk = new TableColumn<>("Academic Risk");
        risk.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().riskLevel()));
        TableColumn<Student, String> scholarship = new TableColumn<>("Scholarship Chance");
        scholarship.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getScholarshipStatus() + " (" + fmt(c.getValue().scholarshipScore()) + ")"));
        results.getColumns().addAll(placement, risk, scholarship);
        return new VBox(12,
                new HBox(12,
                        card("Prediction Engine", "Rule-based", "Excellent, Good, Average, Weak, Critical"),
                        meter("Placement Confidence", service.metrics().placementReady() * 100.0 / Math.max(1, service.activeStudents().size())),
                        meter("Scholarship Confidence", service.metrics().scholarshipEligible() * 100.0 / Math.max(1, service.activeStudents().size())),
                        meter("Attendance Stability", service.activeStudents().stream().mapToDouble(Student::getAttendancePercentage).average().orElse(0))),
                results);
    }

    private Node reportsView() {
        TableView<Student> reportsTable = studentTable(service.activeStudents());
        VBox box = new VBox(12,
                toolbar(
                        action("Preview Topper Report", () -> previewReport("Topper Report", service.top("CGPA", 10))),
                        action("Topper CSV", () -> showExportResult("Topper CSV", service.exportReport("Topper Report", service.top("CGPA", 10)))),
                        action("Department CSV", () -> showExportResult("Department CSV", service.exportCsv())),
                        action("JSON Export", () -> showExportResult("JSON Export", service.exportJson())),
                        action("Compressed Huffman", () -> showExportResult("Compressed Huffman", service.exportCompressed())),
                        action("Import CSV", this::importCsv),
                        action("Transcript", () -> {
                            var selectedStudent = selected(reportsTable);
                            if (selectedStudent.isEmpty()) {
                                alertInfo("Select a student row first, then click Transcript.");
                                return;
                            }
                            showExportResult("Transcript", service.exportTranscript(selectedStudent.get()));
                        })
                ),
                textPanel("Report Actions", List.of(
                        "Topper CSV exports the top 10 CGPA ranking.",
                        "Department CSV exports the active student dataset for department analysis.",
                        "JSON Export writes all active records as structured JSON.",
                        "Compressed Huffman writes a compressed binary export.",
                        "Transcript requires selecting one student row.")),
                reportsTable);
        return box;
    }

    private Node backupView() {
        ListView<String> backups = new ListView<>();
        refreshBackups(backups);
        Button backup = action("Manual Backup", () -> {
            service.backup();
            refreshBackups(backups);
        });
        Button restore = action("Restore Selected", () -> {
            int index = backups.getSelectionModel().getSelectedIndex();
            List<Path> paths = service.storage().backups();
            if (index >= 0 && index < paths.size() && confirm("Restore backup " + paths.get(index).getFileName() + "?")) {
                service.restoreBackup(paths.get(index));
                refreshBackups(backups);
            }
        });
        return new VBox(12, toolbar(backup, restore),
                new HBox(12,
                        card("Backup Status", "Verified local backups", "JSON snapshots with version history"),
                        card("Backup Count", String.valueOf(service.storage().backups().size()), "Version history"),
                        card("Storage Usage", bytes(service.storage().storageUsageBytes()), "Data, exports, backups")),
                textPanel("Restore Preview", service.storage().backups().stream()
                        .map(path -> path.getFileName() + " | " + bytes(service.storage().fileSize(path)) + " | Verified")
                        .toList()),
                backups);
    }

    private Node visualizerView() {
        Label heading = new Label("Data Structures Live Console");
        heading.getStyleClass().add("dsa-hero-title");
        Label description = new Label("This page is generated only from the current student dataset: AVL roll index, Trie names, heap rankings, graph relationships, stacks, queues, and search/sort metrics.");
        description.getStyleClass().add("muted");
        description.setWrapText(true);
        HBox operationCards = new HBox(14,
                dsaInfoCard("AVL Tree", "Balanced roll-number index", "Add, edit, delete, and restore rebuild the tree. The preview shows the first four readable levels."),
                dsaInfoCard("Trie", "Name prefix autocomplete", "Search suggestions come from student names inserted into the Trie."),
                dsaInfoCard("Heap", "Ranking extraction", "Ranking pages use a max-heap priority queue for top CGPA, attendance, scholarship, and placement."),
                dsaInfoCard("Graph", "Relationship traversal", "Edges connect students sharing department, skills, or projects. BFS and DFS samples are shown below."));
        operationCards.getChildren().forEach(node -> HBox.setHgrow(node, Priority.ALWAYS));
        VBox page = new VBox(16,
                new VBox(4, heading, description),
                dsaSummaryCards(),
                visualCanvas(),
                operationCards,
                visualText(),
                textPanel("Visualizer Controls", List.of("Use Students to mutate AVL, Trie, HashMap, HashSet, Heap, and Graph indexes.", "Use Sorting to update comparison and swap metrics.", "Use Search to compare HashMap, AVL, Binary Search, and Trie timings.", "Use Backup and Reports to exercise queue and Huffman export flows.")));
        page.getStyleClass().add("dsa-page");
        return scroller(page);
    }

    private Node settingsView() {
        ChoiceBox<String> theme = new ChoiceBox<>(FXCollections.observableArrayList("Dark Premium", "High Contrast", "Compact"));
        theme.setValue(currentTheme);
        ChoiceBox<String> accent = new ChoiceBox<>(FXCollections.observableArrayList("Teal", "Sky", "Violet", "Rose", "Amber"));
        accent.setValue(currentAccent);
        CheckBox animations = new CheckBox("Enable Animations");
        animations.setSelected(animationsEnabled);
        CheckBox autosave = new CheckBox("Autosave Student Records");
        autosave.setSelected(autosaveEnabled);
        Slider font = new Slider(12, 18, currentFontSize);
        font.setShowTickMarks(true);
        font.setShowTickLabels(true);
        Slider speed = new Slider(80, 420, animationMillis);
        speed.setShowTickMarks(true);
        Spinner<Integer> backup = new Spinner<>(1, 24, 6);
        Label exportFolder = value((customExportFolder == null ? service.storage().exportFolder() : customExportFolder).toAbsolutePath().toString());
        Label status = new Label("Settings are ready.");
        status.getStyleClass().add("settings-status");
        Button chooseFolder = action("Choose Folder", () -> {
            DirectoryChooser chooser = new DirectoryChooser();
            chooser.setTitle("Choose Export Folder");
            java.io.File selected = chooser.showDialog(root.getScene().getWindow());
            if (selected != null) {
                customExportFolder = selected.toPath();
                exportFolder.setText(customExportFolder.toAbsolutePath().toString());
                status.setText("Export folder updated. Click Apply to keep this session setting.");
            }
        });
        Button apply = action("Apply Settings", () -> {
            currentTheme = theme.getValue();
            currentAccent = accent.getValue();
            animationsEnabled = animations.isSelected();
            autosaveEnabled = autosave.isSelected();
            currentFontSize = font.getValue();
            animationMillis = speed.getValue();
            applySettings();
            status.setText("Applied " + currentTheme + " with " + currentAccent + " accent.");
        });
        Button reset = action("Reset Settings", () -> {
            currentTheme = "Dark Premium";
            currentAccent = "Teal";
            animationsEnabled = true;
            autosaveEnabled = true;
            currentFontSize = 13;
            animationMillis = 180;
            customExportFolder = null;
            applySettings();
            show("Settings");
        });
        GridPane form = new GridPane();
        form.setHgap(16);
        form.setVgap(14);
        form.addRow(0, new Label("Theme"), theme);
        form.addRow(1, new Label("Accent Color"), accent);
        form.addRow(2, new Label("Animations"), animations);
        form.addRow(3, new Label("Animation Speed"), speed);
        form.addRow(4, new Label("Font Size"), font);
        form.addRow(5, new Label("Autosave"), autosave);
        form.addRow(6, new Label("Backup Frequency Hours"), backup);
        form.addRow(7, new Label("Export Folder"), new HBox(10, exportFolder, chooseFolder));
        form.addRow(8, new Label("Language"), value("English"));
        return scroller(new VBox(14,
                glass(form),
                toolbar(apply, reset),
                status,
                textPanel("What Settings Change", List.of("Theme changes shell contrast and spacing.", "Accent color updates the primary controls and tree nodes.", "Animation speed controls page transitions.", "Font size changes the whole application scale.", "Autosave remains enabled by default for local JSON storage."))));
    }

    private Node aboutView() {
        return scroller(new VBox(14,
                card("Smart Student Record Management System", "Offline JavaFX Desktop", "MVC architecture with local JSON, CSV, binary Huffman export"),
                textPanel("Implemented DSA", List.of("ArrayList primary storage", "Doubly linked recent profiles", "HashMap + HashSet duplicate detection", "AVL tree roll ordering", "Trie name autocomplete", "Priority queue rankings", "Graph BFS DFS components shortest path", "Stack undo redo", "Queue notifications and backup tasks", "Merge Quick Heap sort", "Binary search", "Huffman compression")),
                textPanel("Professional Modules", List.of("Dashboard, Students, Search, Sorting, Analytics, Attendance, Ranking, Scholarship, Prediction, Reports, Backup, Settings, DSA Visualization"))));
    }

    private Node visualCanvas() {
        Pane pane = new Pane();
        pane.getStyleClass().add("visual-pane");
        pane.setMinHeight(420);
        pane.setPrefHeight(420);
        drawTree(pane, service.avlTree().snapshot(), 360, 78, 168, 0, 4);
        Label heading = new Label("AVL Tree by Roll Number");
        heading.getStyleClass().add("section-title");
        heading.relocate(18, 14);
        Label note = new Label("Readable preview: first 4 levels of " + service.activeStudents().size() + " indexed students");
        note.getStyleClass().add("muted");
        note.relocate(18, 38);
        Label height = badge("Height " + service.avlTree().height());
        height.relocate(610, 18);
        pane.getChildren().addAll(heading, note, height);
        return pane;
    }

    private Node visualText() {
        Student first = service.activeStudents().isEmpty() ? null : service.activeStudents().get(0);
        List<String> bfs = first == null ? List.of() : service.graph().bfs(first.getStudentId());
        List<String> dfs = first == null ? List.of() : service.graph().dfs(first.getStudentId());
        int probeRoll = first == null ? 0 : first.getRollNumber();
        List<String> rotations = service.avlTree().rotations().stream().limit(6).toList();
        List<String> lines = new java.util.ArrayList<>();
        lines.add("AVL height: " + service.avlTree().height());
        lines.add("Recent rotations: " + (rotations.isEmpty() ? "Balanced without recent rotations" : String.join("; ", rotations)));
        lines.add("Trie suggestions for 'Pra': " + String.join(", ", service.suggestions("Pra").stream().map(Student::getFullName).limit(5).toList()));
        lines.add("Heap top CGPA: " + String.join(", ", service.top("CGPA", 5).stream().map(Student::getFullName).toList()));
        lines.add("HashMap buckets/load: " + fmt(service.metrics().hashLoad()));
        lines.add("Trie nodes: " + service.metrics().trieNodes());
        lines.add("Graph nodes/edges: " + service.metrics().graphNodes() + "/" + compactNumber(service.metrics().graphEdges()));
        lines.add("Graph components: " + service.graph().components().size());
        lines.add("BFS sample: " + String.join(" -> ", bfs.stream().limit(8).toList()));
        lines.add("DFS sample: " + String.join(" -> ", dfs.stream().limit(8).toList()));
        lines.add("Stack undo size: available through Undo button");
        lines.add("Queue notifications: " + String.join(" | ", service.notifications().stream().limit(3).toList()));
        lines.add("Binary search roll " + probeRoll + " index: " + service.binarySearchRoll(probeRoll));
        lines.add("Sorting animation data: run Sorting page to update comparisons and swaps");
        return textPanel("DSA Operations", lines);
    }

    private HBox dsaSummaryCards() {
        StudentService.Metrics m = service.metrics();
        HBox cards = new HBox(12,
                card("AVL Tree", "Height " + m.treeHeight(), "O(log n) roll operations"),
                card("Trie", m.trieNodes() + " nodes", "Prefix autocomplete"),
                card("Graph", m.graphNodes() + " / " + compactNumber(m.graphEdges()), "Nodes / edges"),
                card("HashMap", fmt(m.hashLoad()), "Load factor estimate"));
        cards.getStyleClass().add("dsa-metrics-row");
        cards.getChildren().forEach(node -> {
            HBox.setHgrow(node, Priority.ALWAYS);
            if (node instanceof Region region) region.setMinWidth(190);
        });
        return cards;
    }

    private VBox dsaInfoCard(String title, String value, String detail) {
        VBox card = card(title, value, detail);
        card.getStyleClass().add("dsa-info-card");
        card.setMinWidth(210);
        return card;
    }

    private void drawTree(Pane pane, AVLTree.SnapshotNode<Student> node, double x, double y, double gap, int depth, int maxDepth) {
        if (node == null) return;
        if (depth >= maxDepth) {
            Label more = badge("...");
            more.relocate(x - 16, y - 12);
            pane.getChildren().add(more);
            return;
        }
        if (node.left != null) {
            Line line = new Line(x, y, x - gap, y + 74);
            line.getStyleClass().add("tree-line");
            pane.getChildren().add(line);
            drawTree(pane, node.left, x - gap, y + 74, gap * 0.56, depth + 1, maxDepth);
        }
        if (node.right != null) {
            Line line = new Line(x, y, x + gap, y + 74);
            line.getStyleClass().add("tree-line");
            pane.getChildren().add(line);
            drawTree(pane, node.right, x + gap, y + 74, gap * 0.56, depth + 1, maxDepth);
        }
        Circle circle = new Circle(x, y, 22, Color.web(accentColor()));
        circle.getStyleClass().add("tree-node");
        Label label = new Label(String.valueOf(node.key));
        label.getStyleClass().add("tree-label");
        label.relocate(x - 15, y - 10);
        pane.getChildren().addAll(circle, label);
    }

    private TableView<Student> studentTable(List<Student> rows) {
        TableView<Student> view = new TableView<>(FXCollections.observableArrayList(rows));
        view.getStyleClass().add("data-table");
        view.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        addColumn(view, "Roll", s -> String.valueOf(s.getRollNumber()));
        addColumn(view, "ID", Student::getStudentId);
        addColumn(view, "Name", Student::getFullName);
        addColumn(view, "Department", Student::getDepartment);
        addColumn(view, "Sem", s -> String.valueOf(s.getSemester()));
        addColumn(view, "CGPA", s -> fmt(s.getCgpa()));
        addColumn(view, "Attendance", s -> fmt(s.getAttendancePercentage()) + "%");
        addColumn(view, "Placement", Student::getPlacementStatus);
        addColumn(view, "Risk", Student::riskLevel);
        MenuItem profile = new MenuItem("Open Profile");
        profile.setOnAction(e -> selected(view).ifPresent(this::openProfile));
        MenuItem edit = new MenuItem("Edit");
        edit.setOnAction(e -> selected(view).ifPresent(this::openEditor));
        MenuItem transcript = new MenuItem("Export Transcript");
        transcript.setOnAction(e -> selected(view).ifPresent(service::exportTranscript));
        view.setContextMenu(new ContextMenu(profile, edit, transcript));
        view.setOnMouseClicked(e -> {
            if (e.getClickCount() == 2) selected(view).ifPresent(this::openProfile);
        });
        return view;
    }

    private void addColumn(TableView<Student> view, String name, java.util.function.Function<Student, String> mapper) {
        TableColumn<Student, String> column = new TableColumn<>(name);
        column.setCellValueFactory(c -> new SimpleStringProperty(mapper.apply(c.getValue())));
        view.getColumns().add(column);
    }

    private void openProfile(Student student) {
        service.byRoll(student.getRollNumber());
        javafx.stage.Stage dialog = new javafx.stage.Stage();
        dialog.setTitle(student.getFullName() + " - Student Profile");

        VBox identity = new VBox(10, avatar(student, 86),
                labelBlock(student.getFullName(), student.getStudentId() + " | Roll " + student.getRollNumber()),
                badge(student.getPlacementStatus()), badge(student.getScholarshipStatus()), badge(student.riskLevel()));
        identity.getStyleClass().add("profile-rail");
        identity.setAlignment(Pos.TOP_CENTER);

        GridPane info = new GridPane();
        info.setHgap(18);
        info.setVgap(12);
        info.addRow(0, new Label("Department"), value(student.getDepartment()), new Label("Semester"), value(String.valueOf(student.getSemester())));
        info.addRow(1, new Label("Division"), value(student.getDivision()), new Label("Age"), value(String.valueOf(student.age())));
        info.addRow(2, new Label("Email"), value(student.getEmail()), new Label("Phone"), value(student.getPhone()));
        info.addRow(3, new Label("Guardian"), value(student.getGuardianName()), new Label("Contact"), value(student.getGuardianContact()));
        info.addRow(4, new Label("Address"), value(student.getAddress()));

        HBox meters = new HBox(12,
                meter("Attendance", student.getAttendancePercentage()),
                meter("Placement", student.placementScore()),
                meter("Scholarship", student.scholarshipScore()),
                meter("CGPA", student.getCgpa() * 10));
        LineChart<String, Number> attendance = line("Monthly Attendance", student.getMonthlyAttendance());
        LineChart<String, Number> cgpa = line("CGPA Trend", student.getCgpaTrend());

        VBox details = new VBox(14,
                textPanel("Basic Information", List.of("Gender: " + student.getGender(), "DOB: " + student.getDateOfBirth(), "Credits: " + student.getCredits(), "Backlogs: " + student.getBacklogs())),
                glass(info),
                meters,
                new HBox(12, attendance, cgpa),
                textPanel("Projects", splitText(student.getProjects())),
                textPanel("Skills", splitText(student.getSkills())),
                textPanel("Achievements", splitText(student.getAchievements())),
                textPanel("Timeline", student.getTimeline()),
                textPanel("Prediction", List.of("Academic risk: " + student.riskLevel(), "Placement readiness: " + student.getPlacementStatus(), "Scholarship status: " + student.getScholarshipStatus(), "Remarks: " + student.getRemarks())));
        HBox body = new HBox(16, identity, scroller(details));
        HBox actions = toolbar(action("Edit Profile", () -> openEditor(student)), action("Export Transcript", () -> service.exportTranscript(student)));
        VBox wrap = new VBox(14, body, actions);
        wrap.getStyleClass().add("dialog");
        javafx.scene.Scene scene = new javafx.scene.Scene(wrap, 1040, 760);
        scene.getStylesheets().add(getClass().getResource("/com/smartstudent/styles/app.css").toExternalForm());
        dialog.setScene(scene);
        dialog.show();
    }

    private void openEditor(Student existing) {
        Student working = existing == null ? new Student() : existing.copy();
        int originalRoll = working.getRollNumber();
        javafx.stage.Stage dialog = new javafx.stage.Stage();
        dialog.setTitle(existing == null ? "Add Student" : "Edit Student");
        GridPane form = new GridPane();
        form.setHgap(12);
        form.setVgap(10);
        form.setPadding(new Insets(18));
        TextField roll = text(String.valueOf(working.getRollNumber() == 0 ? "" : working.getRollNumber()));
        TextField id = text(working.getStudentId());
        TextField name = text(working.getFullName());
        ComboBox<String> gender = combo("Male", "Female", "Other");
        gender.setValue(working.getGender().isBlank() ? "Male" : working.getGender());
        DatePicker dob = new DatePicker(working.getDateOfBirth() == null ? LocalDate.now().minusYears(18) : working.getDateOfBirth());
        TextField dept = text(working.getDepartment());
        TextField sem = text(String.valueOf(working.getSemester()));
        TextField div = text(working.getDivision());
        TextField phone = text(working.getPhone());
        TextField email = text(working.getEmail());
        TextArea address = area(working.getAddress());
        TextField guardian = text(working.getGuardianName());
        TextField guardianPhone = text(working.getGuardianContact());
        TextField attendance = text(String.valueOf(working.getAttendancePercentage()));
        TextField cgpa = text(String.valueOf(working.getCgpa()));
        TextField credits = text(String.valueOf(working.getCredits()));
        TextField skills = text(working.getSkills());
        TextField projects = text(working.getProjects());
        TextField achievements = text(working.getAchievements());
        ComboBox<String> income = combo("Low", "Middle", "High");
        income.setValue(working.getIncomeCategory().isBlank() ? "Middle" : working.getIncomeCategory());
        TextField backlogs = text(String.valueOf(working.getBacklogs()));
        TextArea remarks = area(working.getRemarks());

        int r = 0;
        form.addRow(r++, new Label("Roll Number"), roll, new Label("Student ID"), id);
        form.addRow(r++, new Label("Full Name"), name, new Label("Gender"), gender);
        form.addRow(r++, new Label("DOB"), dob, new Label("Department"), dept);
        form.addRow(r++, new Label("Semester"), sem, new Label("Division"), div);
        form.addRow(r++, new Label("Phone"), phone, new Label("Email"), email);
        form.addRow(r++, new Label("Guardian"), guardian, new Label("Guardian Contact"), guardianPhone);
        form.addRow(r++, new Label("Attendance %"), attendance, new Label("CGPA"), cgpa);
        form.addRow(r++, new Label("Credits"), credits, new Label("Income"), income);
        form.addRow(r++, new Label("Backlogs"), backlogs, new Label("Skills"), skills);
        form.addRow(r++, new Label("Projects"), projects);
        form.addRow(r++, new Label("Achievements"), achievements);
        form.addRow(r++, new Label("Address"), address);
        form.addRow(r, new Label("Remarks"), remarks);

        Button save = action("Save", () -> {
            try {
                working.setRollNumber(Integer.parseInt(roll.getText().trim()));
                working.setStudentId(id.getText());
                working.setFullName(name.getText());
                working.setGender(gender.getValue());
                working.setDateOfBirth(dob.getValue());
                working.setDepartment(dept.getText());
                working.setSemester(Integer.parseInt(sem.getText().trim()));
                working.setDivision(div.getText());
                working.setPhone(phone.getText());
                working.setEmail(email.getText());
                working.setAddress(address.getText());
                working.setGuardianName(guardian.getText());
                working.setGuardianContact(guardianPhone.getText());
                working.setAttendancePercentage(Double.parseDouble(attendance.getText().trim()));
                working.setCgpa(Double.parseDouble(cgpa.getText().trim()));
                working.setCredits(Integer.parseInt(credits.getText().trim()));
                working.setSkills(skills.getText());
                working.setProjects(projects.getText());
                working.setAchievements(achievements.getText());
                working.setIncomeCategory(income.getValue());
                working.setBacklogs(Integer.parseInt(backlogs.getText().trim()));
                working.setRemarks(remarks.getText());
                if (existing == null) service.addStudent(working);
                else service.updateStudent(originalRoll, working);
                dialog.close();
                show("Students");
            } catch (Exception ex) {
                alert(ex.getMessage());
            }
        });
        VBox wrap = new VBox(14, glass(form), toolbar(save));
        wrap.getStyleClass().add("dialog");
        javafx.scene.Scene scene = new javafx.scene.Scene(wrap, 860, 680);
        scene.getStylesheets().add(getClass().getResource("/com/smartstudent/styles/app.css").toExternalForm());
        dialog.setScene(scene);
        dialog.show();
    }

    private PieChart departmentPie() {
        PieChart pie = new PieChart();
        pie.setTitle("Department Distribution");
        service.departmentDistribution().forEach((k, v) -> pie.getData().add(new PieChart.Data(k, v)));
        pie.setLabelsVisible(false);
        pie.setLegendVisible(true);
        pie.setLegendSide(Side.RIGHT);
        pie.setClockwise(true);
        pie.setStartAngle(90);
        pie.setMinSize(420, 320);
        pie.setPrefSize(460, 340);
        pie.getStyleClass().add("chart-card");
        return pie;
    }

    private BarChart<String, Number> cgpaBars() {
        BarChart<String, Number> chart = barChart("Grade", "Students");
        chart.setTitle("CGPA Distribution");
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        service.gradeDistribution().forEach((k, v) -> series.getData().add(new XYChart.Data<>(k, v)));
        chart.getData().add(series);
        return chart;
    }

    private BarChart<String, Number> semesterBars() {
        BarChart<String, Number> chart = barChart("Semester", "Students");
        chart.setTitle("Semester Analysis");
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        service.semesterDistribution().forEach((k, v) -> series.getData().add(new XYChart.Data<>("S" + k, v)));
        chart.getData().add(series);
        return chart;
    }

    private LineChart<String, Number> trendChart() {
        LineChart<String, Number> chart = new LineChart<>(new CategoryAxis(), new NumberAxis());
        chart.setTitle("Attendance Trend");
        chart.setLegendVisible(false);
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        for (int i = 1; i <= 6; i++) {
            final int index = i - 1;
            double avg = service.activeStudents().stream()
                    .filter(s -> s.getMonthlyAttendance().size() > index)
                    .mapToDouble(s -> s.getMonthlyAttendance().get(index)).average().orElse(0);
            series.getData().add(new XYChart.Data<>("M" + i, avg));
        }
        chart.getData().add(series);
        chart.getStyleClass().add("chart-card");
        return chart;
    }

    private BarChart<String, Number> barChart(String x, String y) {
        BarChart<String, Number> chart = new BarChart<>(new CategoryAxis(), new NumberAxis());
        chart.setLegendVisible(false);
        chart.getStyleClass().add("chart-card");
        chart.setMinHeight(270);
        return chart;
    }

    private VBox algorithmInfo(String algorithm) {
        List<String> lines = switch (algorithm) {
            case "Quick Sort" -> List.of(
                    "Stability: Not stable",
                    "Best/Average: O(n log n)",
                    "Worst: O(n^2)",
                    "Method: pivot partitioning",
                    "Use case: fast general-purpose sorting");
            case "Heap Sort" -> List.of(
                    "Stability: Not stable",
                    "Best/Average/Worst: O(n log n)",
                    "Method: binary heap construction and extraction",
                    "Use case: ranking and top-score ordering");
            default -> List.of(
                    "Stability: Stable",
                    "Best/Average/Worst: O(n log n)",
                    "Method: divide, sort, and merge",
                    "Use case: stable sorting by name, department, and semester");
        };
        VBox panel = textPanel("Algorithm Details", lines);
        panel.setMinWidth(330);
        return panel;
    }

    private String stabilityText(String algorithm) {
        return "Merge Sort".equals(algorithm) ? "stable" : "not stable";
    }

    private BarChart<String, Number> sortingPreviewChart(List<Student> rows, String field, int limit) {
        BarChart<String, Number> chart = barChart("Student", field);
        chart.setTitle("Sorted Preview");
        chart.setMinHeight(260);
        chart.setPrefWidth(560);
        refreshSortingPreview(chart, rows, field, limit);
        return chart;
    }

    private void refreshSortingPreview(BarChart<String, Number> chart, List<Student> rows, String field, int limit) {
        chart.getData().clear();
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        int count = Math.min(limit, rows.size());
        for (int i = 0; i < count; i++) {
            Student student = rows.get(i);
            series.getData().add(new XYChart.Data<>(student.getRollNumber() + "", sortPreviewValue(student, field, i)));
        }
        chart.getData().add(series);
    }

    private double sortPreviewValue(Student student, String field, int index) {
        return switch (field) {
            case "CGPA" -> student.getCgpa();
            case "Attendance" -> student.getAttendancePercentage();
            case "Semester" -> student.getSemester();
            case "Placement Score" -> student.placementScore();
            case "Scholarship Score" -> student.scholarshipScore();
            case "Name", "Department" -> index + 1;
            default -> student.getRollNumber();
        };
    }

    private GridPane responsiveGrid() {
        GridPane grid = new GridPane();
        grid.setHgap(14);
        grid.setVgap(14);
        return grid;
    }

    private VBox card(String label, String value, String hint) {
        VBox card = new VBox(8);
        card.getStyleClass().add("card");
        Label l = new Label(label);
        l.getStyleClass().add("muted");
        Label v = new Label(value);
        v.getStyleClass().add("metric");
        Label h = new Label(hint);
        h.getStyleClass().add("hint");
        card.getChildren().addAll(l, v, h);
        return card;
    }

    private VBox listPanel(String title, List<Student> students) {
        List<String> lines = students.stream().map(s -> s.getFullName() + "  " + fmt(s.getCgpa()) + " CGPA").toList();
        return textPanel(title, lines);
    }

    private HBox podium(String type) {
        List<Student> top = service.top(type, 3);
        HBox box = new HBox(12);
        box.setAlignment(Pos.CENTER);
        for (int i = 0; i < top.size(); i++) {
            Student s = top.get(i);
            double score = switch (type) {
                case "Attendance" -> s.getAttendancePercentage();
                case "Placement" -> s.placementScore();
                case "Scholarship" -> s.scholarshipScore();
                case "Overall" -> s.overallScore();
                default -> s.getCgpa();
            };
            Label rank = new Label("#" + (i + 1));
            rank.getStyleClass().add("podium-rank");
            Label name = new Label(s.getFullName());
            name.getStyleClass().add("podium-name");
            name.setWrapText(true);
            Label scoreBadge = badge(fmt(score));
            scoreBadge.getStyleClass().add("score-badge");
            VBox item = new VBox(10, avatar(s, 62), rank, name, scoreBadge);
            item.getStyleClass().addAll("podium-card", i == 0 ? "podium-first" : "card");
            item.setAlignment(Pos.CENTER);
            box.getChildren().add(item);
        }
        return box;
    }

    private VBox meter(String label, double value) {
        ProgressBar bar = new ProgressBar(Math.max(0, Math.min(1, value / 100.0)));
        bar.setPrefWidth(180);
        VBox box = new VBox(8, new Label(label), new Label(fmt(value) + "%"), bar);
        box.getStyleClass().add("card");
        return box;
    }

    private LineChart<String, Number> line(String title, List<Double> values) {
        LineChart<String, Number> chart = new LineChart<>(new CategoryAxis(), new NumberAxis());
        chart.setTitle(title);
        chart.setLegendVisible(false);
        chart.setMinHeight(230);
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        for (int i = 0; i < values.size(); i++) series.getData().add(new XYChart.Data<>("T" + (i + 1), values.get(i)));
        chart.getData().add(series);
        chart.getStyleClass().add("chart-card");
        return chart;
    }

    private StackPane avatar(Student student, double size) {
        Circle circle = new Circle(size / 2, Color.web(colorFor(student)));
        Label initials = new Label(initials(student.getFullName()));
        initials.getStyleClass().add("avatar-text");
        StackPane avatar = new StackPane(circle, initials);
        avatar.setMinSize(size, size);
        avatar.setPrefSize(size, size);
        avatar.setMaxSize(size, size);
        return avatar;
    }

    private VBox labelBlock(String main, String sub) {
        Label a = new Label(main);
        a.getStyleClass().add("section-title");
        Label b = new Label(sub);
        b.getStyleClass().add("muted");
        return new VBox(4, a, b);
    }

    private Label badge(String text) {
        Label badge = new Label(text);
        badge.getStyleClass().add("badge");
        return badge;
    }

    private Label value(String text) {
        Label label = new Label(text == null || text.isBlank() ? "Not provided" : text);
        label.getStyleClass().add("value-label");
        label.setWrapText(true);
        return label;
    }

    private List<String> splitText(String value) {
        if (value == null || value.isBlank()) return List.of("No records yet");
        return java.util.Arrays.stream(value.split(",")).map(String::trim).filter(s -> !s.isBlank()).toList();
    }

    private VBox textPanel(String title, List<String> lines) {
        VBox panel = new VBox(8);
        panel.getStyleClass().add("card");
        Label label = new Label(title);
        label.getStyleClass().add("section-title");
        panel.getChildren().add(label);
        for (String line : lines) {
            Label row = new Label(line);
            row.getStyleClass().add("list-line");
            row.setWrapText(true);
            panel.getChildren().add(row);
        }
        return panel;
    }

    private VBox savedSearchesPanel(ListView<String> history) {
        VBox panel = new VBox(8);
        panel.getStyleClass().add("card");
        Label title = new Label("Saved Searches");
        title.getStyleClass().add("section-title");
        Label help = new Label("Use Save Search or press Enter to store the current query. Click any saved item to run it again.");
        help.getStyleClass().add("hint");
        help.setWrapText(true);
        panel.getChildren().addAll(title, help, history);
        return panel;
    }

    private VBox sliderGroup(String label, Slider slider) {
        Label title = new Label(label);
        title.getStyleClass().add("hint");
        Label value = new Label(fmt(slider.getValue()));
        value.getStyleClass().add("value-label");
        slider.valueProperty().addListener((obs, old, current) -> value.setText(fmt(current.doubleValue())));
        HBox header = new HBox(8, title, value);
        header.setAlignment(Pos.CENTER_LEFT);
        VBox group = new VBox(6, header, slider);
        group.getStyleClass().add("filter-group");
        return group;
    }

    private VBox systemPanel() {
        StudentService.Metrics m = service.metrics();
        return textPanel("Performance Panel", List.of(
                "Search: O(1) HashMap / O(log n) AVL / O(k) Trie",
                "Sort: Merge O(n log n), Quick average O(n log n), Heap O(n log n)",
                "Tree height: " + m.treeHeight(),
                "Trie nodes: " + m.trieNodes(),
                "Heap size: " + m.heapSize(),
                "Graph nodes/edges: " + m.graphNodes() + "/" + m.graphEdges(),
                "HashMap load estimate: " + fmt(m.hashLoad()),
                "Memory: " + bytes(m.memoryBytes())
        ));
    }

    private HBox toolbar(Node... nodes) {
        HBox box = new HBox(10, nodes);
        box.setAlignment(Pos.CENTER_LEFT);
        box.getStyleClass().add("toolbar");
        return box;
    }

    private Button action(String text, Runnable runnable) {
        Button button = new Button(text);
        button.getStyleClass().add("primary-button");
        button.setOnAction(e -> runnable.run());
        return button;
    }

    private Button iconButton(String text, String tip) {
        Button button = new Button(text);
        button.getStyleClass().add("fab");
        button.setTooltip(new Tooltip(tip));
        return button;
    }

    private Node glass(Node child) {
        StackPane pane = new StackPane(child);
        pane.getStyleClass().add("glass");
        return pane;
    }

    private void applySettings() {
        root.getStyleClass().removeAll("theme-high-contrast", "theme-compact");
        if ("High Contrast".equals(currentTheme)) root.getStyleClass().add("theme-high-contrast");
        if ("Compact".equals(currentTheme)) root.getStyleClass().add("theme-compact");
        root.setStyle("-fx-font-size: " + fmt(currentFontSize) + "px; -app-accent: " + accentColor() + ";");
        service.setAutosaveEnabled(autosaveEnabled);
        service.setExportFolder(customExportFolder);
    }

    private String accentColor() {
        return switch (currentAccent) {
            case "Sky" -> "#38bdf8";
            case "Violet" -> "#a78bfa";
            case "Rose" -> "#fb7185";
            case "Amber" -> "#f59e0b";
            default -> "#2dd4bf";
        };
    }

    private HBox statusPill(String main, String sub) {
        HBox box = new HBox(8, new Label("ON"), new VBox(new Label(main), new Label(sub)));
        box.getStyleClass().add("status-pill");
        return box;
    }

    private ScrollPane scroller(Node node) {
        ScrollPane scroll = new ScrollPane(node);
        scroll.setFitToWidth(true);
        scroll.getStyleClass().add("scroll");
        return scroll;
    }

    private Region spacer(double height) {
        Region region = new Region();
        region.setMinHeight(height);
        return region;
    }

    @SafeVarargs
    private <T> ComboBox<T> combo(T... values) {
        ComboBox<T> box = new ComboBox<>(FXCollections.observableArrayList(values));
        box.setValue(values[0]);
        return box;
    }

    private TextField text(String value) {
        TextField field = new TextField(value == null ? "" : value);
        field.setMinWidth(180);
        return field;
    }

    private TextArea area(String value) {
        TextArea area = new TextArea(value == null ? "" : value);
        area.setPrefRowCount(2);
        return area;
    }

    private java.util.Optional<Student> selected() {
        return selected(table);
    }

    private java.util.Optional<Student> selected(TableView<Student> view) {
        if (view == null) return java.util.Optional.empty();
        Student student = view.getSelectionModel().getSelectedItem();
        if (student != null) service.byRoll(student.getRollNumber());
        return java.util.Optional.ofNullable(student);
    }

    private void refreshBackups(ListView<String> list) {
        list.setItems(FXCollections.observableArrayList(service.storage().backups().stream()
                .map(path -> path.getFileName().toString()).toList()));
    }

    private void importCsv() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Import Student CSV");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV Files", "*.csv"));
        java.io.File file = chooser.showOpenDialog(root.getScene().getWindow());
        if (file == null) return;
        try {
            int count = service.bulkImport(file.toPath());
            alertInfo("Imported " + count + " new records.");
            show("Students");
        } catch (Exception ex) {
            alert(ex.getMessage());
        }
    }

    private void previewReport(String name, List<Student> rows) {
        javafx.stage.Stage dialog = new javafx.stage.Stage();
        dialog.setTitle(name + " Preview");
        List<String> lines = new java.util.ArrayList<>();
        lines.add("Smart Student Records");
        lines.add(name);
        lines.add("Generated: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd MMM yyyy, hh:mm a")));
        lines.add("Records: " + rows.size());
        lines.add("Average CGPA: " + fmt(rows.stream().mapToDouble(Student::getCgpa).average().orElse(0)));
        lines.add("Average Attendance: " + fmt(rows.stream().mapToDouble(Student::getAttendancePercentage).average().orElse(0)) + "%");
        rows.stream().limit(10).forEach(s -> lines.add(s.getRollNumber() + " | " + s.getFullName() + " | " + s.getDepartment() + " | CGPA " + fmt(s.getCgpa())));
        VBox wrap = new VBox(14, textPanel("Report Preview", lines), toolbar(action("Export CSV", () -> service.exportReport(name, rows))));
        wrap.getStyleClass().add("dialog");
        javafx.scene.Scene scene = new javafx.scene.Scene(wrap, 620, 560);
        scene.getStylesheets().add(getClass().getResource("/com/smartstudent/styles/app.css").toExternalForm());
        dialog.setScene(scene);
        dialog.show();
    }

    private void showExportResult(String label, Path path) {
        alertInfo(label + " created:\n" + path.toAbsolutePath());
    }

    private void alert(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR, message);
        alert.showAndWait();
    }

    private void alertInfo(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION, message);
        alert.showAndWait();
    }

    private boolean confirm(String message) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, message);
        return alert.showAndWait().filter(button -> button == javafx.scene.control.ButtonType.OK).isPresent();
    }

    private String initials(String name) {
        String[] parts = name == null ? new String[0] : name.trim().split("\\s+");
        if (parts.length == 0 || parts[0].isBlank()) return "ST";
        if (parts.length == 1) return parts[0].substring(0, Math.min(2, parts[0].length())).toUpperCase();
        return (parts[0].substring(0, 1) + parts[parts.length - 1].substring(0, 1)).toUpperCase();
    }

    private String colorFor(Student student) {
        String[] colors = {"#2dd4bf", "#38bdf8", "#a78bfa", "#f472b6", "#f59e0b", "#22c55e"};
        return colors[Math.abs(student.getRollNumber()) % colors.length];
    }

    private String fmt(double value) {
        return "%.2f".formatted(value);
    }

    private String percent(long part, long total) {
        return total == 0 ? "0%" : fmt(part * 100.0 / total) + "%";
    }

    private String bytes(long value) {
        if (value < 1024) return value + " B";
        if (value < 1024 * 1024) return fmt(value / 1024.0) + " KB";
        return fmt(value / (1024.0 * 1024.0)) + " MB";
    }

    private String compactNumber(long value) {
        if (value < 1_000) return String.valueOf(value);
        if (value < 1_000_000) return fmt(value / 1_000.0) + "K";
        return fmt(value / 1_000_000.0) + "M";
    }
}
