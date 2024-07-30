package com.killian.SpringBoot.ExamApp.models;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;

@Entity
@Table(name = "tblQuestion")
public class Question {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "text", columnDefinition = "VARCHAR(2000) COLLATE utf8mb4_bin")
    private String text;

    @ElementCollection
    @CollectionTable(name = "choices", joinColumns = @JoinColumn(name = "question_id"))
    private List<String> choices;

    @ElementCollection
    @CollectionTable(name = "answers", joinColumns = @JoinColumn(name = "question_id"))
    private List<String> answer;

    private String subject;

    private String difficulty;

    private String chapter;

    private int grade;

    private String questionType;

    public Question() {
    }

    public Question(int grade, String subject) {
        this.grade = grade;
        this.subject = subject;
    }

    public Question(String text, List<String> choices, List<String> answer, String subject, String chapter, int grade,
            String questionType) {
        this.text = text;
        this.choices = choices;
        this.answer = answer;
        this.subject = subject;
        this.chapter = chapter;
        this.grade = grade;
        this.questionType = questionType;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public List<String> getChoices() {
        return choices;
    }

    public void addChoice(String choice) {
        this.choices.add(choice);
    }

    public void setChoices(List<String> choices) {
        this.choices = choices;
    }

    public void shuffleChoices() {
        List<String> newChoices = new ArrayList<>();
        newChoices.addAll(this.choices);
        Collections.shuffle(newChoices);
        this.choices = newChoices;
    }

    public void resetAnswers() {
        List<String> newAnswers = new ArrayList<>();
        newAnswers.addAll(this.answer);
        Collections.shuffle(newAnswers);
        this.answer = newAnswers;
    }

    public List<String> getAnswer() {
        return answer;
    }

    public void setAnswer(List<String> answer) {
        this.answer = answer;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(String difficulty) {
        this.difficulty = difficulty;
    }

    public String getChapter() {
        return chapter;
    }

    public void setChapter(String chapter) {
        this.chapter = chapter;
    }

    public int getGrade() {
        return grade;
    }

    public void setGrade(int grade) {
        this.grade = grade;
    }

    public String getQuestionType() {
        return questionType;
    }

    public void setQuestionType(String questionType) {
        this.questionType = questionType;
    }

    @Override
    public String toString() {
        return text;
    }

    @Override
    public boolean equals(Object o) {
        return false;

    }
}
