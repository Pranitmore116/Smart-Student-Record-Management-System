# Smart Student Record Management System

Offline JavaFX desktop application for managing student records while demonstrating core Data Structures and Algorithms.

## Run

```powershell
mvn javafx:run
```

The project is configured for the Java version available on this machine, Java 17. To target Java 21 later, change `<maven.compiler.release>` in `pom.xml` from `17` to `21`.

## Included

- Dark premium JavaFX desktop UI with sidebar navigation, dashboard cards, charts, tables, dialogs, animations, and CSS theme.
- Student CRUD, clone, archive, restore, duplicate detection, autosave, CSV/JSON export, Huffman compressed export, backup and restore.
- DSA implementations: ArrayList storage, doubly linked recent profiles, HashMap lookup, HashSet duplicate checks, AVL tree, Trie autocomplete, PriorityQueue rankings, Graph BFS/DFS/components/shortest path, Stack undo/redo, Queue notifications, binary search, merge sort, quick sort, heap sort, Huffman coding.
- Modules for Dashboard, Students, Search, Sorting, Analytics, Attendance, Ranking, Scholarship, Prediction, Reports, Backup & Restore, DSA Visualization, Settings, and About.

## Data

On first run the app creates sample offline data under `data/students.json`. Exports and backups are written under `data/exports` and `data/backups`.
