# Smart Student Record Management System

A **JavaFX-based desktop application** for managing student records while demonstrating the practical implementation of **Data Structures and Algorithms (DSA)** such as AVL Trees, Tries, HashMaps, Heaps, Graphs, Stacks, Queues, searching, sorting, and graph traversal.

The project was developed as part of the **Fundamentals of Data Structures: Learn, Apply and Build Projects** summer training course.

---

## 📌 Overview

The **Smart Student Record Management System** provides a centralized offline platform for managing student information, academic performance, attendance, rankings, scholarships, and placement-related analysis.

Instead of relying only on conventional CRUD operations, the system integrates custom Data Structures and Algorithms into actual application features.

### Key capabilities

* 👨‍🎓 Student record management
* 🔎 Advanced student search
* ⚡ Prefix-based autocomplete
* 📊 Academic analytics
* 🏆 Student ranking
* 🎓 Scholarship eligibility analysis
* 📈 Performance prediction
* 📅 Attendance analysis
* ↕️ Multiple sorting algorithms
* 🌐 Student relationship graph
* 🔄 Undo/Redo
* 💾 JSON/CSV persistence
* 🗄️ Backup and restore
* 📦 Huffman-based compression
* 🌳 Interactive DSA visualization
* ⚙️ Configurable application settings

---

## 🖥️ Application Preview

### Dashboard

The dashboard provides an overview of the student dataset, including total students, average CGPA, highest CGPA, attendance alerts, placement readiness, scholarship eligibility, AVL height, and graph relationships.

### Student Management

Manage student records using:

* Add
* Edit
* Clone
* Archive
* Restore
* Delete
* Import CSV
* Export CSV
* Undo
* Redo

### Search

Supports multiple search techniques including:

* Linear Search
* Binary Search
* HashMap Search
* AVL Tree Search
* Trie Prefix Search

### Sorting

Compare and execute:

* Merge Sort
* Quick Sort
* Heap Sort

The application displays sorting metrics such as comparisons, swaps, execution information, and algorithm characteristics.

### Ranking

Uses a **Max Heap / Priority Queue** to prioritize high-performing students and generate rankings.

### DSA Visualization

Provides a visual representation of:

* AVL Tree
* Trie
* Heap
* Graph
* HashMap

It also displays DSA metrics such as tree height, Trie nodes, graph nodes/edges, and HashMap load information.

---

# 🧠 Data Structures & Algorithms

| DSA                    | Purpose in Project                              |
| ---------------------- | ----------------------------------------------- |
| **ArrayList**          | Primary in-memory collection of student records |
| **Doubly Linked List** | Recent profile/history functionality            |
| **HashMap**            | Fast key-based lookup and grouping              |
| **HashSet**            | Uniqueness and duplicate detection              |
| **AVL Tree**           | Balanced ordered student index and lookup       |
| **Trie**               | Name/roll-number prefix search and autocomplete |
| **Max Heap**           | Extracting high-priority/top students           |
| **Priority Queue**     | Student ranking                                 |
| **Graph**              | Modeling relationships between students         |
| **Stack**              | Undo/Redo history                               |
| **Queue**              | Notification/task processing                    |
| **Huffman Coding**     | Compression of exported/archived data           |

---

# 🔍 Searching Algorithms

### Linear Search

Sequentially checks student records until the required record is found.

**Time Complexity:** `O(n)`

### Binary Search

Searches sorted data by repeatedly dividing the search range in half.

**Time Complexity:** `O(log n)`

### Trie Search

Traverses characters of a name or roll number and supports prefix-based searching.

**Time Complexity:** `O(k)`

Where `k` is the length of the searched string.

### AVL Tree Search

Uses the balanced structure of the AVL Tree for ordered lookup.

**Time Complexity:** `O(log n)`

### HashMap Search

Uses a key to locate a record.

**Average:** `O(1)`
**Worst Case:** `O(n)`

---

# ↕️ Sorting Algorithms

| Algorithm      |       Best |    Average |      Worst | Stable |
| -------------- | ---------: | ---------: | ---------: | ------ |
| **Merge Sort** | O(n log n) | O(n log n) | O(n log n) | Yes    |
| **Quick Sort** | O(n log n) | O(n log n) |      O(n²) | No     |
| **Heap Sort**  | O(n log n) | O(n log n) | O(n log n) | No     |

The algorithms can be used to organize students by fields such as:

* Roll Number
* Name
* CGPA
* Attendance
* Academic performance

---

# 🌐 Graph Algorithms

The project models relationships between students using a graph.

* **Vertices:** Students
* **Edges:** Relationships between students

The graph can represent relationships based on attributes such as department, section, skills, or other defined connections.

### BFS — Breadth-First Search

Explores connected students **level by level**.

**Time Complexity:** `O(V + E)`

### DFS — Depth-First Search

Explores one relationship path deeply before backtracking.

**Time Complexity:** `O(V + E)`

Where:

* `V` = number of vertices
* `E` = number of edges

---

# 🏗️ Architecture

The application follows a modular architecture consisting of three major layers:

```text
                    ┌─────────────────────────┐
                    │       JavaFX UI         │
                    │ Dashboard / Students    │
                    │ Search / Sorting / etc. │
                    └────────────┬────────────┘
                                 │
                                 ▼
                    ┌─────────────────────────┐
                    │    Service / Business   │
                    │       Logic Layer       │
                    └────────────┬────────────┘
                                 │
                ┌────────────────┴────────────────┐
                ▼                                 ▼
     ┌─────────────────────┐          ┌─────────────────────┐
     │ DSA / Core Logic    │          │ Storage Layer       │
     │                     │          │                     │
     │ AVL Tree            │          │ JSON                │
     │ Trie                │          │ CSV                 │
     │ Heap                │          │ Backup / Restore    │
     │ Graph               │          │ Validation          │
     │ Sorting / Searching │          │                     │
     └─────────────────────┘          └─────────────────────┘
```

---

# 📁 Project Structure

```text
Smart-Student-Record-Management-System/
│
├── src/
│   └── main/
│       ├── java/
│       │   ├── com/
│       │   │   └── smartstudent/
│       │   │       ├── dsa/
│       │   │       │   ├── AVLTree.java
│       │   │       │   ├── Trie.java
│       │   │       │   ├── RecentProfileList.java
│       │   │       │   ├── HuffmanCoding.java
│       │   │       │   ├── SortAlgorithms.java
│       │   │       │   └── StudentGraph.java
│       │   │       │
│       │   │       ├── model/
│       │   │       │   ├── Student.java
│       │   │       │   └── StudentSnapshot.java
│       │   │       │
│       │   │       ├── service/
│       │   │       │   └── StudentService.java
│       │   │       │
│       │   │       ├── storage/
│       │   │       │   └── StudentStorage.java
│       │   │       │
│       │   │       ├── ui/
│       │   │       │   └── MainController.java
│       │   │       │
│       │   │       ├── MainApp.java
│       │   │       └── module-info.java
│       │   │
│       │   └── resources/
│       │       └── com/
│       │           └── smartstudent/
│       │               └── styles/
│       │                   └── app.css
│       │
├── pom.xml
└── README.md
```

---

# ⚙️ Technologies Used

### Programming Language

**Java**

### GUI Framework

**JavaFX**

Used to build the desktop interface.

### Styling

**JavaFX CSS**

Used for the application's visual theme, spacing, typography, and controls.

### Build Tool

**Apache Maven**

Used for dependency management and project building.

### Data Persistence

**JSON**

Used for persistent student records and local backups.

**CSV**

Used for importing/exporting student data and generating reports.

### Architecture

**Java Platform Module System**

Used through `module-info.java` for application modularization.

---

# 💾 Data Storage

This is an **offline desktop application**.

It does **not require MySQL or another external relational database**.

Student information is persisted locally using:

```text
JSON → Main persistent student records
CSV  → Import / Export / Reports
```

The active dataset is maintained in memory while the application is running, with the ArrayList serving as the primary collection.

Additional DSA indexes/structures are used to perform specialized operations efficiently.

---

# 🔄 Student Data Flow

```text
User
  │
  ▼
JavaFX Form
  │
  ▼
Validation
  │
  ▼
StudentService
  │
  ├──────────────► ArrayList
  │
  ├──────────────► AVL Tree
  │
  ├──────────────► Trie
  │
  ├──────────────► HashMap / HashSet
  │
  ├──────────────► Heap / Priority Queue
  │
  └──────────────► Graph
  │
  ▼
StudentStorage
  │
  ├── JSON
  └── CSV
```

---

# 📊 Major Application Modules

## Dashboard

Provides a real-time overview of:

* Total students
* Average CGPA
* Highest CGPA
* Attendance alerts
* Placement readiness
* Scholarship eligibility
* Department count
* Storage usage
* AVL Tree height
* Graph relationships

## Students

Complete student CRUD and record-management operations.

## Search

Provides advanced filtering and multiple search techniques.

## Sorting

Provides algorithm comparison and sorted-result visualization.

## Analytics

Displays academic and attendance statistics.

## Attendance

Provides attendance monitoring and low-attendance analysis.

## Ranking

Ranks students based on academic performance.

## Scholarship

Determines scholarship eligibility using defined rules.

## Prediction

Provides rule-based placement, scholarship, academic-risk, and attendance confidence.

## Reports

Supports:

* Topper CSV
* Department CSV
* JSON export
* Huffman-compressed export
* Student transcript

## Backup & Restore

Creates local snapshots and allows previous student datasets to be restored.

## DSA Visualization

Demonstrates the internal working of major Data Structures and Algorithms visually.

## Settings

Allows configuration of:

* Theme
* Accent colour
* Animation settings
* Animation speed
* Font size
* Workspace zoom
* Autosave
* Backup frequency
* Export folder
* Language

---

# 🧪 Testing

The application was tested for major operations including:

* Student creation
* Student modification
* Student deletion
* Archive/restore
* Search
* Sorting
* Ranking
* Attendance analysis
* CSV import/export
* JSON persistence
* Backup/restore
* DSA operations
* Input validation

The system validates required fields and relevant data ranges before records are persisted.

---

# 👥 Team

The project was developed by **three team members** with separate responsibilities:

| Member       | Responsibility       |
| ------------ | -------------------- |
| **Member 1** | DSA / Core Logic     |
| **Member 2** | JavaFX UI / Frontend |
| **Member 3** | Backend / Storage    |

### DSA / Core Logic

Implementation of:

* AVL Tree
* Trie
* Searching
* Sorting
* Heap/Priority Queue
* Graph
* BFS/DFS
* Huffman Coding
* Supporting data structures

### UI / Frontend

Development of:

* JavaFX interface
* Dashboard
* Student management
* Search
* Sorting
* Analytics
* Attendance
* Ranking
* Scholarship
* Prediction
* Styling

### Backend / Storage

Development of:

* Student model
* CRUD operations
* Service layer
* JSON/CSV persistence
* Backup/restore
* Validation
* Testing
* Maven/module configuration

---

# 🚀 Getting Started

## Prerequisites

Install:

* **JDK 17 or compatible JDK**
* **Apache Maven**
* **JavaFX dependencies** through Maven

Verify Java:

```bash
java -version
```

Verify Maven:

```bash
mvn -version
```

## Clone the Repository

```bash
git clone https://github.com/YOUR-USERNAME/YOUR-REPOSITORY.git
```

```bash
cd YOUR-REPOSITORY
```

## Run the Application

Using Maven:

```bash
mvn clean javafx:run
```

---

# 📈 Complexity Summary

| Operation          | Structure / Algorithm |              Complexity |
| ------------------ | --------------------- | ----------------------: |
| Access by index    | ArrayList             |                    O(1) |
| Search             | Linear Search         |                    O(n) |
| Search             | Binary Search         |                O(log n) |
| Search             | AVL Tree              |                O(log n) |
| Search             | HashMap               |            O(1) average |
| Prefix Search      | Trie                  |                    O(k) |
| Insert/Search      | AVL Tree              |                O(log n) |
| Ranking extraction | Max Heap              | O(log n) per extraction |
| Sorting            | Merge Sort            |              O(n log n) |
| Sorting            | Quick Sort            |      O(n log n) average |
| Sorting            | Heap Sort             |              O(n log n) |
| Graph Traversal    | BFS                   |                O(V + E) |
| Graph Traversal    | DFS                   |                O(V + E) |

---

# 🔮 Future Scope

Possible future improvements include:

* Role-based authentication
* Admin, Faculty, and Student accounts
* Cloud database synchronization
* REST API
* Mobile companion application
* Machine-learning-based performance prediction
* Automated email/SMS notifications
* PDF report generation
* LMS/ERP integration
* Multi-campus support
* Encryption and stronger auditing

---

# 🎯 Learning Outcomes

This project provided practical experience in:

* Data Structures implementation
* Algorithm design
* Algorithm complexity analysis
* Java programming
* JavaFX desktop development
* Object-oriented programming
* File-based persistence
* Modular application design
* Searching and sorting
* Graph traversal
* Data visualization
* Team-based software development
* Git-based collaboration

---

# 📜 License

This project was developed for **academic and educational purposes** as part of the Summer Training course **“Fundamentals of Data Structures: Learn, Apply and Build Projects.”**

---

## ⭐ Project Highlights

```text
✓ JavaFX Desktop Application
✓ 300+ Student Dataset Support
✓ AVL Tree
✓ Trie
✓ HashMap
✓ HashSet
✓ Max Heap
✓ Priority Queue
✓ Graph
✓ BFS / DFS
✓ Stack / Queue
✓ Merge Sort
✓ Quick Sort
✓ Heap Sort
✓ Binary Search
✓ Linear Search
✓ Huffman Coding
✓ JSON / CSV Persistence
✓ Backup & Restore
✓ DSA Visualization
✓ Academic Analytics
✓ Student Ranking
✓ Scholarship Analysis
✓ Attendance Analysis
✓ Placement Prediction
```

**Built to demonstrate that Data Structures and Algorithms are not just theoretical concepts — they can directly power real software features.**
