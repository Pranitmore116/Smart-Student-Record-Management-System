package com.smartstudent.model;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.Period;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Student implements Serializable, Cloneable {
    private int rollNumber;
    private String studentId = "";
    private String fullName = "";
    private String gender = "";
    private LocalDate dateOfBirth = LocalDate.now().minusYears(18);
    private String department = "";
    private int semester = 1;
    private String division = "";
    private String phone = "";
    private String email = "";
    private String address = "";
    private String guardianName = "";
    private String guardianContact = "";
    private double attendancePercentage;
    private double cgpa;
    private int credits;
    private String skills = "";
    private String projects = "";
    private String achievements = "";
    private String placementStatus = "Not Ready";
    private String scholarshipStatus = "Not Eligible";
    private String photo = "";
    private String remarks = "";
    private String incomeCategory = "Middle";
    private int backlogs;
    private boolean archived;
    private boolean favorite;
    private boolean pinned;
    private List<Double> monthlyAttendance = new ArrayList<>();
    private List<Double> cgpaTrend = new ArrayList<>();
    private List<String> timeline = new ArrayList<>();

    public Student() {
    }

    public Student(int rollNumber, String studentId, String fullName, String department, int semester, double attendancePercentage, double cgpa) {
        this.rollNumber = rollNumber;
        this.studentId = studentId;
        this.fullName = fullName;
        this.department = department;
        this.semester = semester;
        this.attendancePercentage = attendancePercentage;
        this.cgpa = cgpa;
    }

    public double placementScore() {
        double skillBoost = skills == null || skills.isBlank() ? 0 : Math.min(1.0, skills.split(",").length / 6.0);
        double projectBoost = projects == null || projects.isBlank() ? 0 : Math.min(1.0, projects.split(",").length / 4.0);
        double achievementBoost = achievements == null || achievements.isBlank() ? 0 : Math.min(1.0, achievements.split(",").length / 4.0) * 8.0;
        return clamp(round((cgpa * 7.0) + (attendancePercentage * 0.2) + (skillBoost * 8.0) + (projectBoost * 10.0) + achievementBoost - (backlogs * 8.0)), 0, 100);
    }

    public double scholarshipScore() {
        double incomeBoost = switch (incomeCategory == null ? "" : incomeCategory.toLowerCase()) {
            case "low" -> 20;
            case "middle" -> 10;
            default -> 0;
        };
        return clamp(round((cgpa * 8.0) + (attendancePercentage * 0.15) + incomeBoost - (backlogs * 12.0)), 0, 100);
    }

    public double overallScore() {
        return round((cgpa * 10.0) + attendancePercentage + placementScore() + scholarshipScore());
    }

    public String riskLevel() {
        if (cgpa >= 8.5 && attendancePercentage >= 85 && backlogs == 0) return "Excellent";
        if (cgpa >= 7.5 && attendancePercentage >= 75 && backlogs <= 1) return "Good";
        if (cgpa >= 6.2 && attendancePercentage >= 65) return "Average";
        if (cgpa >= 5.0 || attendancePercentage >= 55) return "Weak";
        return "Critical";
    }

    public int age() {
        return Period.between(dateOfBirth, LocalDate.now()).getYears();
    }

    public Student copy() {
        try {
            Student clone = (Student) clone();
            clone.monthlyAttendance = new ArrayList<>(monthlyAttendance);
            clone.cgpaTrend = new ArrayList<>(cgpaTrend);
            clone.timeline = new ArrayList<>(timeline);
            return clone;
        } catch (CloneNotSupportedException e) {
            throw new IllegalStateException(e);
        }
    }

    private static double round(double value) {
        return Math.round(value * 100.0) / 100.0;
    }

    private static double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }

    public int getRollNumber() { return rollNumber; }
    public void setRollNumber(int rollNumber) { this.rollNumber = rollNumber; }
    public String getStudentId() { return studentId; }
    public void setStudentId(String studentId) { this.studentId = value(studentId); }
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = value(fullName); }
    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = value(gender); }
    public LocalDate getDateOfBirth() { return dateOfBirth; }
    public void setDateOfBirth(LocalDate dateOfBirth) { this.dateOfBirth = dateOfBirth == null ? LocalDate.now().minusYears(18) : dateOfBirth; }
    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = value(department); }
    public int getSemester() { return semester; }
    public void setSemester(int semester) { this.semester = semester; }
    public String getDivision() { return division; }
    public void setDivision(String division) { this.division = value(division); }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = value(phone); }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = value(email); }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = value(address); }
    public String getGuardianName() { return guardianName; }
    public void setGuardianName(String guardianName) { this.guardianName = value(guardianName); }
    public String getGuardianContact() { return guardianContact; }
    public void setGuardianContact(String guardianContact) { this.guardianContact = value(guardianContact); }
    public double getAttendancePercentage() { return attendancePercentage; }
    public void setAttendancePercentage(double attendancePercentage) { this.attendancePercentage = attendancePercentage; }
    public double getCgpa() { return cgpa; }
    public void setCgpa(double cgpa) { this.cgpa = cgpa; }
    public int getCredits() { return credits; }
    public void setCredits(int credits) { this.credits = credits; }
    public String getSkills() { return skills; }
    public void setSkills(String skills) { this.skills = value(skills); }
    public String getProjects() { return projects; }
    public void setProjects(String projects) { this.projects = value(projects); }
    public String getAchievements() { return achievements; }
    public void setAchievements(String achievements) { this.achievements = value(achievements); }
    public String getPlacementStatus() { return placementStatus; }
    public void setPlacementStatus(String placementStatus) { this.placementStatus = value(placementStatus); }
    public String getScholarshipStatus() { return scholarshipStatus; }
    public void setScholarshipStatus(String scholarshipStatus) { this.scholarshipStatus = value(scholarshipStatus); }
    public String getPhoto() { return photo; }
    public void setPhoto(String photo) { this.photo = value(photo); }
    public String getRemarks() { return remarks; }
    public void setRemarks(String remarks) { this.remarks = value(remarks); }
    public String getIncomeCategory() { return incomeCategory; }
    public void setIncomeCategory(String incomeCategory) { this.incomeCategory = value(incomeCategory); }
    public int getBacklogs() { return backlogs; }
    public void setBacklogs(int backlogs) { this.backlogs = backlogs; }
    public boolean isArchived() { return archived; }
    public void setArchived(boolean archived) { this.archived = archived; }
    public boolean isFavorite() { return favorite; }
    public void setFavorite(boolean favorite) { this.favorite = favorite; }
    public boolean isPinned() { return pinned; }
    public void setPinned(boolean pinned) { this.pinned = pinned; }
    public List<Double> getMonthlyAttendance() { return monthlyAttendance; }
    public void setMonthlyAttendance(List<Double> monthlyAttendance) { this.monthlyAttendance = monthlyAttendance == null ? new ArrayList<>() : monthlyAttendance; }
    public List<Double> getCgpaTrend() { return cgpaTrend; }
    public void setCgpaTrend(List<Double> cgpaTrend) { this.cgpaTrend = cgpaTrend == null ? new ArrayList<>() : cgpaTrend; }
    public List<String> getTimeline() { return timeline; }
    public void setTimeline(List<String> timeline) { this.timeline = timeline == null ? new ArrayList<>() : timeline; }

    private static String value(String text) {
        return text == null ? "" : text.trim();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Student student)) return false;
        return rollNumber == student.rollNumber;
    }

    @Override
    public int hashCode() {
        return Objects.hash(rollNumber);
    }
}
