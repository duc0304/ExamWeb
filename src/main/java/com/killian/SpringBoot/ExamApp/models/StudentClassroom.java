package com.killian.SpringBoot.ExamApp.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "tblStudentClassroom")
public class StudentClassroom {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "student", columnDefinition = "VARCHAR(255) COLLATE utf8mb4_bin")
    private String student;

    @Column(name = "className", columnDefinition = "VARCHAR(255) COLLATE utf8mb4_bin")
    private String className;

    @Column(name = "classCode", columnDefinition = "VARCHAR(255) COLLATE utf8mb4_bin")
    private String classCode;

    public StudentClassroom() {
    }

    public StudentClassroom(String student, String className, String classCode) {
        this.student = student;
        this.classCode = classCode;
        this.className = className;
    }

    public String getStudent() {
        return student;
    }

    public void setStudent(String student) {
        this.student = student;
    }

    public String getClassCode() {
        return classCode;
    }

    public void setClassCode(String classCode) {
        this.classCode = classCode;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    
}
