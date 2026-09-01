package com.smartstudent.storage;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.smartstudent.dsa.HuffmanCoding;
import com.smartstudent.model.Student;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class StudentStorage {
    private final Path dataDir;
    private final Path jsonFile;
    private final Path backupDir;
    private final ObjectMapper mapper;

    public StudentStorage(Path dataDir) {
        this.dataDir = dataDir;
        this.jsonFile = dataDir.resolve("students.json");
        this.backupDir = dataDir.resolve("backups");
        this.mapper = new ObjectMapper().registerModule(new JavaTimeModule());
    }

    public List<Student> load() {
        try {
            Files.createDirectories(dataDir);
            if (!Files.exists(jsonFile)) {
                List<Student> sample = sampleStudents();
                save(sample);
                return sample;
            }
            List<Student> loaded = mapper.readValue(jsonFile.toFile(), new TypeReference<>() {});
            if (loaded.size() < 300) {
                loaded = sampleStudents();
                save(loaded);
            }
            return loaded;
        } catch (IOException e) {
            return sampleStudents();
        }
    }

    public void save(List<Student> students) {
        try {
            Files.createDirectories(dataDir);
            mapper.writerWithDefaultPrettyPrinter().writeValue(jsonFile.toFile(), students);
        } catch (IOException e) {
            throw new IllegalStateException("Unable to save student data", e);
        }
    }

    public Path backup(List<Student> students) {
        try {
            Files.createDirectories(backupDir);
            String stamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss"));
            Path file = backupDir.resolve("students-" + stamp + ".json");
            mapper.writerWithDefaultPrettyPrinter().writeValue(file.toFile(), students);
            return file;
        } catch (IOException e) {
            throw new IllegalStateException("Unable to create backup", e);
        }
    }

    public List<Path> backups() {
        try {
            Files.createDirectories(backupDir);
            try (var stream = Files.list(backupDir)) {
                return stream.filter(path -> path.toString().endsWith(".json")).sorted().toList();
            }
        } catch (IOException e) {
            return List.of();
        }
    }

    public List<Student> restore(Path backup) {
        try {
            return mapper.readValue(backup.toFile(), new TypeReference<>() {});
        } catch (IOException e) {
            throw new IllegalStateException("Unable to restore backup", e);
        }
    }

    public Path exportJson(List<Student> students, Path output) {
        try {
            Files.createDirectories(output.getParent());
            mapper.writerWithDefaultPrettyPrinter().writeValue(output.toFile(), students);
            return output;
        } catch (IOException e) {
            throw new IllegalStateException("Unable to export JSON", e);
        }
    }

    public Path exportCsv(List<Student> students, Path output) {
        try {
            Files.createDirectories(output.getParent());
            List<String> lines = new ArrayList<>();
            lines.add("rollNumber,studentId,fullName,gender,dob,age,department,semester,division,phone,email,address,guardianName,guardianContact,attendancePercentage,cgpa,credits,skills,projects,achievements,placementScore,placementStatus,scholarshipScore,scholarshipStatus,backlogs,incomeCategory,remarks,photo");
            for (Student s : students) {
                lines.add(String.join(",",
                        csv(s.getRollNumber()),
                        csv(s.getStudentId()),
                        csv(s.getFullName()),
                        csv(s.getGender()),
                        csv(s.getDateOfBirth()),
                        csv(s.age()),
                        csv(s.getDepartment()),
                        csv(s.getSemester()),
                        csv(s.getDivision()),
                        csv(s.getPhone()),
                        csv(s.getEmail()),
                        csv(s.getAddress()),
                        csv(s.getGuardianName()),
                        csv(s.getGuardianContact()),
                        csv(s.getAttendancePercentage()),
                        csv(s.getCgpa()),
                        csv(s.getCredits()),
                        csv(s.getSkills()),
                        csv(s.getProjects()),
                        csv(s.getAchievements()),
                        csv(s.placementScore()),
                        csv(s.getPlacementStatus()),
                        csv(s.scholarshipScore()),
                        csv(s.getScholarshipStatus()),
                        csv(s.getBacklogs()),
                        csv(s.getIncomeCategory()),
                        csv(s.getRemarks()),
                        csv(s.getPhoto())));
            }
            Files.write(output, lines, StandardCharsets.UTF_8);
            return output;
        } catch (IOException e) {
            throw new IllegalStateException("Unable to export CSV", e);
        }
    }

    public List<Student> importCsv(Path input) {
        try {
            List<String> lines = Files.readAllLines(input, StandardCharsets.UTF_8);
            List<Student> imported = new ArrayList<>();
            for (int i = 1; i < lines.size(); i++) {
                String[] parts = lines.get(i).split(",", -1);
                if (parts.length < 10) continue;
                Student s = new Student();
                s.setRollNumber(parseInt(parts[0], 0));
                s.setStudentId(parts[1]);
                s.setFullName(parts[2]);
                int offset = parts.length > 20 ? 3 : 0;
                if (offset > 0) {
                    s.setGender(parts[3]);
                    s.setDateOfBirth(java.time.LocalDate.parse(parts[4]));
                }
                s.setDepartment(parts[3 + offset]);
                s.setSemester(parseInt(parts[4 + offset], 1));
                s.setDivision(parts.length > 5 + offset ? parts[5 + offset] : "");
                s.setPhone(parts.length > 6 + offset ? parts[6 + offset] : "");
                s.setEmail(parts.length > 7 + offset ? parts[7 + offset] : "");
                if (offset > 0) {
                    s.setAddress(parts.length > 8 + offset ? parts[8 + offset] : "");
                    s.setGuardianName(parts.length > 9 + offset ? parts[9 + offset] : "");
                    s.setGuardianContact(parts.length > 10 + offset ? parts[10 + offset] : "");
                    offset += 3;
                }
                s.setAttendancePercentage(parseDouble(parts.length > 8 + offset ? parts[8 + offset] : "0", 0));
                s.setCgpa(parseDouble(parts.length > 9 + offset ? parts[9 + offset] : "0", 0));
                s.setCredits(parseInt(parts.length > 10 + offset ? parts[10 + offset] : "0", 0));
                s.setSkills(parts.length > 11 + offset ? parts[11 + offset] : "");
                s.setProjects(parts.length > 12 + offset ? parts[12 + offset] : "");
                s.setAchievements(parts.length > 13 + offset ? parts[13 + offset] : "");
                imported.add(s);
            }
            return imported;
        } catch (IOException e) {
            throw new IllegalStateException("Unable to import CSV", e);
        }
    }

    public Path exportCompressed(List<Student> students, Path output) {
        try {
            byte[] json = mapper.writeValueAsString(students).getBytes(StandardCharsets.UTF_8);
            HuffmanCoding.writeCompressed(output, json);
            return output;
        } catch (IOException e) {
            throw new IllegalStateException("Unable to export compressed binary", e);
        }
    }

    public Path exportTranscript(Student student, Path output) {
        try {
            Files.createDirectories(output.getParent());
            String report = """
                    SMART STUDENT RECORDS - TRANSCRIPT

                    Roll Number: %d
                    Student ID: %s
                    Name: %s
                    Department: %s
                    Semester: %d
                    Attendance: %.2f%%
                    CGPA: %.2f
                    Credits: %d
                    Skills: %s
                    Projects: %s
                    Achievements: %s
                    Placement Score: %.2f
                    Placement: %s
                    Scholarship Score: %.2f
                    Scholarship: %s
                    Risk Level: %s
                    Guardian: %s (%s)
                    Remarks: %s
                    """.formatted(student.getRollNumber(), student.getStudentId(), student.getFullName(),
                    student.getDepartment(), student.getSemester(), student.getAttendancePercentage(), student.getCgpa(),
                    student.getCredits(), student.getSkills(), student.getProjects(), student.getAchievements(),
                    student.placementScore(), student.getPlacementStatus(), student.scholarshipScore(),
                    student.getScholarshipStatus(), student.riskLevel(), student.getGuardianName(),
                    student.getGuardianContact(), student.getRemarks());
            Files.writeString(output, report, StandardCharsets.UTF_8);
            return output;
        } catch (IOException e) {
            throw new IllegalStateException("Unable to export transcript", e);
        }
    }

    public Path exportFolder() {
        Path folder = dataDir.resolve("exports");
        try {
            Files.createDirectories(folder);
        } catch (IOException e) {
            throw new IllegalStateException("Unable to create export folder", e);
        }
        return folder;
    }

    public long storageUsageBytes() {
        try {
            if (!Files.exists(dataDir)) return 0;
            try (var stream = Files.walk(dataDir)) {
                return stream.filter(Files::isRegularFile).mapToLong(path -> {
                    try {
                        return Files.size(path);
                    } catch (IOException e) {
                        return 0L;
                    }
                }).sum();
            }
        } catch (IOException e) {
            return 0;
        }
    }

    public long fileSize(Path path) {
        try {
            return Files.exists(path) ? Files.size(path) : 0;
        } catch (IOException e) {
            return 0;
        }
    }

    private String csv(Object value) {
        String text = String.valueOf(value == null ? "" : value);
        return text.replace(",", " ");
    }

    private int parseInt(String text, int fallback) {
        try {
            return Integer.parseInt(text.trim());
        } catch (Exception e) {
            return fallback;
        }
    }

    private double parseDouble(String text, double fallback) {
        try {
            return Double.parseDouble(text.trim());
        } catch (Exception e) {
            return fallback;
        }
    }

    private List<Student> sampleStudents() {
        List<Student> students = new ArrayList<>();
        String[] first = {"Aarav", "Aarohi", "Vivaan", "Anaya", "Aditya", "Diya", "Ishaan", "Meera", "Kabir", "Nisha", "Pranit", "Prakash", "Prasad", "Pranav", "Riya", "Saanvi", "Arjun", "Ira", "Dev", "Tara", "Neel", "Aisha", "Kunal", "Maya", "Rohan", "Sara", "Yash", "Kiara", "Om", "Avni"};
        String[] last = {"Sharma", "Iyer", "Kulkarni", "Desai", "Mehta", "Patil", "Khan", "Rao", "Joshi", "Nair", "Pillai", "Kapoor", "Gupta", "Bose", "Menon", "Shetty", "Chopra", "Naik", "Jain", "Saxena"};
        String[] departments = {"Computer Science", "Information Technology", "Electronics", "Mechanical", "Civil", "Data Science", "AI and ML", "Electrical"};
        String[] skillGroups = {"Java, JavaFX, DSA, SQL", "Python, ML, Data Analysis, Git", "React, Java, Testing, UI Design", "IoT, C, PCB, Sensors", "CAD, Robotics, Simulation, MATLAB", "Cloud, Linux, Docker, APIs", "Cybersecurity, Networks, Python, Bash", "Analytics, Excel, SQL, Tableau"};
        String[] projectGroups = {"Smart Records, Campus Connect", "Attendance AI, Risk Predictor", "Placement Portal, Resume Ranker", "Smart Meter, Health Band", "Library Optimizer, Event Planner", "Traffic Analyzer, Hostel Desk", "Scholarship Engine, Grade Tracker", "Lab Inventory, Notice Hub"};
        String[] achievements = {"Hackathon Finalist, Dean List", "Best Project, Coding Club", "Research Poster, Sports Medal", "Open Source Contributor, Mentor", "Innovation Award, Volunteer Lead", "Technical Quiz Winner, Seminar Speaker"};
        Random random = new Random(42);
        for (int i = 0; i < 300; i++) {
            int roll = 1001 + i;
            String name = first[i % first.length] + " " + last[(i * 7) % last.length];
            String department = departments[i % departments.length];
            int semester = 1 + (i % 8);
            String division = String.valueOf((char) ('A' + (i % 4)));
            double base = 5.2 + (random.nextDouble() * 4.7);
            double cgpa = Math.round(Math.min(9.95, base + (i % 9) * 0.04) * 100.0) / 100.0;
            double attendance = Math.round(Math.min(99, 56 + random.nextDouble() * 42 + (cgpa - 7) * 3) * 100.0) / 100.0;
            String income = i % 5 == 0 ? "Low" : i % 3 == 0 ? "High" : "Middle";
            int backlogs = cgpa > 8 ? 0 : random.nextInt(4);
            Student s = sample(roll, "S-2026-" + roll, name, department, semester, division, attendance, cgpa,
                    skillGroups[i % skillGroups.length], projectGroups[(i + semester) % projectGroups.length], income, backlogs);
            s.setGender(i % 3 == 0 ? "Female" : i % 3 == 1 ? "Male" : "Other");
            s.setDateOfBirth(java.time.LocalDate.of(2000 + (i % 7), 1 + (i % 12), 1 + (i % 27)));
            s.setAchievements(achievements[i % achievements.length]);
            s.setPhoto("avatar-" + (i % 12 + 1));
            s.setRemarks(s.riskLevel() + " academic profile with " + department + " specialization.");
            students.add(s);
        }
        return students;
    }

    private Student sample(int roll, String id, String name, String department, int semester, String division,
                           double attendance, double cgpa, String skills, String projects, String income, int backlogs) {
        Student s = new Student(roll, id, name, department, semester, attendance, cgpa);
        s.setDivision(division);
        s.setEmail(name.toLowerCase().replace(" ", ".") + "@college.edu");
        s.setPhone("90000" + roll);
        s.setGender(roll % 2 == 0 ? "Female" : "Male");
        s.setAddress("Campus Hostel " + division);
        s.setGuardianName("Guardian " + name.split(" ")[0]);
        s.setGuardianContact("80000" + roll);
        s.setCredits(semester * 22);
        s.setSkills(skills);
        s.setProjects(projects);
        s.setIncomeCategory(income);
        s.setBacklogs(backlogs);
        s.setMonthlyAttendance(List.of(Math.max(0, attendance - 5), Math.max(0, attendance - 3), attendance, Math.min(100, attendance + 1), Math.min(100, attendance + 3), attendance));
        s.setCgpaTrend(List.of(Math.max(0, cgpa - 0.7), Math.max(0, cgpa - 0.45), Math.max(0, cgpa - 0.25), cgpa));
        s.setTimeline(List.of("Profile created", "Semester " + semester + " review completed", "Placement score recalculated", "Scholarship eligibility checked"));
        return s;
    }
}
