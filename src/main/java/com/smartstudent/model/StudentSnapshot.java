package com.smartstudent.model;

import java.util.ArrayList;
import java.util.List;

public class StudentSnapshot {
    private final List<Student> students;

    public StudentSnapshot(List<Student> source) {
        this.students = source.stream().map(Student::copy).toList();
    }

    public List<Student> restore() {
        return new ArrayList<>(students.stream().map(Student::copy).toList());
    }
}
