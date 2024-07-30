package com.killian.SpringBoot.ExamApp.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.security.SecureRandom;

@Entity
@Table(name = "tblClassroom")
public class Classroom {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "assignmentId", columnDefinition = "VARCHAR(255) COLLATE utf8mb4_bin")
    private String name;

    private String subject;

    private String grade;

    @Column(name = "teacher", columnDefinition = "VARCHAR(255) COLLATE utf8mb4_bin")
    private String teacher;

    @Column(name = "classCode", columnDefinition = "VARCHAR(255) COLLATE utf8mb4_bin")
    private String classCode;

    private static final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    private static SecureRandom random = new SecureRandom();

    public Classroom(String name, String subject, String grade, String teacher) {
        this.name = name;
        this.subject = subject;
        this.grade = grade;
        this.teacher = teacher;
        this.classCode = classCodeGenerate();
    }

    public Classroom() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getGrade() {
        return grade;
    }

    public void setGrade(String grade) {
        this.grade = grade;
    }

    public String getTeacher() {
        return teacher;
    }

    public void setTeacher(String teacher) {
        this.teacher = teacher;
    }

    public String getClassCode() {
        return classCode;
    }

    public void setClassCode(String classCode) {
        this.classCode = classCode;
    }

    public void addAssignment(Assignment assignment) {
        // for (String student : students) {
        // assignment.addStudentScore(student, -1);
        // }
    }

    public static String classCodeGenerate() {
        StringBuilder code = new StringBuilder();
        for (int i = 0; i < 6; i++) {
            int randomIndex = random.nextInt(CHARACTERS.length());
            code.append(CHARACTERS.charAt(randomIndex));
        }
        return code.toString();
    }

}
