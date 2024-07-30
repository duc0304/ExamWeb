package com.killian.SpringBoot.ExamApp.models;

import java.security.SecureRandom;
import java.util.List;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;

// A POJO 
@Entity
@Table(name = "tblExam")
public class Exam {
    // this is "primary key"
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "owner", columnDefinition = "VARCHAR(255) COLLATE utf8mb4_bin")
    private String owner;

    private String subject;

    private int grade;

    @Column(name = "name", columnDefinition = "VARCHAR(255) COLLATE utf8mb4_bin")
    private String name;

    private int duration;

    private int examCode;

    @Column(name = "examId", columnDefinition = "VARCHAR(255) COLLATE utf8mb4_bin")
    private String examId;

    @ManyToMany
    @JoinTable(name = "exam_to_question", joinColumns = @JoinColumn(name = "exam_id"), inverseJoinColumns = @JoinColumn(name = "question_id"))
    private List<Question> questions;

    private static final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    private static SecureRandom random = new SecureRandom();

    public Exam() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Question> getQuestions() {
        return questions;
    }

    public void addQuestion(Question question) {
        this.questions.add(question);
    }

    public void setQuestions(List<Question> questions) {
        this.questions = questions;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public int getGrade() {
        return grade;
    }

    public void setGrade(int grade) {
        this.grade = grade;
    }

    public int getExamCode() {
        return examCode;
    }

    public void setExamCode(int examCode) {
        this.examCode = examCode;
    }

    public String getExamId() {
        return examId;
    }

    public void setExamId() {
        this.examId = examIdGenerate();
    }

    public void setExamId(String examId) {
        this.examId = examId;
    }

    @Override
    public String toString() {
        return name;
    }

    @Override
    public boolean equals(Object o) {
        return false;
    }

    public static String examIdGenerate() {
        StringBuilder code = new StringBuilder();
        for (int i = 0; i < 6; i++) {
            int randomIndex = random.nextInt(CHARACTERS.length());
            code.append(CHARACTERS.charAt(randomIndex));
        }
        return code.toString();
    }
}
