package com.killian.SpringBoot.ExamApp.models;

import java.security.SecureRandom;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "tblAssignment")
public class Assignment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", columnDefinition = "VARCHAR(255) COLLATE utf8mb4_bin")
    private String name;

    private String deadline;

    @Column(name = "examId", columnDefinition = "VARCHAR(255) COLLATE utf8mb4_bin")
    private String examId;

    @Column(name = "className", columnDefinition = "VARCHAR(255) COLLATE utf8mb4_bin")
    private String className;

    @Column(name = "classCode", columnDefinition = "VARCHAR(255) COLLATE utf8mb4_bin")
    private String classCode;

    @Column(name = "assignmentId", columnDefinition = "VARCHAR(255) COLLATE utf8mb4_bin")
    private String assignmentId;

    private static final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    private static SecureRandom random = new SecureRandom();

    public Assignment() {
    }

    public Assignment(String name, String deadline, String examId, String className, String classCode) {
        this.name = name;
        this.deadline = deadline;
        this.examId = examId;
        this.className = className;
        this.classCode = classCode;
        this.assignmentId = assignmentIdGenerate();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDeadline() {
        return deadline;
    }

    public void setDeadline(String deadline) {
        this.deadline = deadline;
    }

    public String getExamId() {
        return examId;
    }

    public void setExamId(String examId) {
        this.examId = examId;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String getClassCode() {
        return classCode;
    }

    public void setClassCode(String classCode) {
        this.classCode = classCode;
    }

    public String getAssignmentId() {
        return assignmentId;
    }

    public void setAssignmentId(String assignmentId) {
        this.assignmentId = assignmentId;
    }

    public static String assignmentIdGenerate() {
        StringBuilder code = new StringBuilder();
        for (int i = 0; i < 6; i++) {
            int randomIndex = random.nextInt(CHARACTERS.length());
            code.append(CHARACTERS.charAt(randomIndex));
        }
        return code.toString();
    }
}
