package com.killian.SpringBoot.ExamApp.models;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
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

@Entity
@Table(name = "tblSubmission")
public class Submission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "student", columnDefinition = "VARCHAR(255) COLLATE utf8mb4_bin")
    private String student;

    private String startedTime;

    private String endTime;

    private String submittedTime;

    private double score;

    private int examCode;

    @ManyToMany
    @JoinTable(name = "submission_to_choices", joinColumns = @JoinColumn(name = "submission_id"), inverseJoinColumns = @JoinColumn(name = "choice_id"))
    private List<Choice> choices;

    private String assignmentId;

    private String examId;

    private String submissionId;

    private static final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    private static SecureRandom random = new SecureRandom();

    public Submission() {
    }

    public Submission(String student, int examCode, String examId, int questionCount, int duration) {
        this.student = student;
        this.examId = examId;
        this.examCode = examCode;
        this.startedTime = getCurrentDateTime();
        this.endTime = calculateEndTime(this.startedTime, duration);
        this.score = -1.0;
        this.submissionId = submissionIdGenerate();
    }

    public Submission(String student, String assignmentId, int examCode, int questionCount, int duration) {
        this.student = student;
        this.assignmentId = assignmentId;
        this.examCode = examCode;
        this.startedTime = getCurrentDateTime();
        this.endTime = calculateEndTime(this.startedTime, duration);
        this.score = -1.0;
        this.submissionId = submissionIdGenerate();
    }

    public String getStudent() {
        return student;
    }

    public void setStudent(String student) {
        this.student = student;
    }

    public String getStartedTime() {
        return startedTime;
    }

    public void setStartedTime(String startedTime) {
        this.startedTime = startedTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public int getExamCode() {
        return examCode;
    }

    public void setExamCode(int examCode) {
        this.examCode = examCode;
    }

    public void setScore(double score) {
        this.score = score;
    }

    public double getScore() {
        return score;
    }

    public String getExamId() {
        return examId;
    }

    public void setExamId(String examId) {
        this.examId = examId;
    }

    public String getSubmittedTime() {
        return submittedTime;
    }

    public void setSubmittedTime(String submittedTime) {
        this.submittedTime = submittedTime;
    }

    public List<Choice> getChoices() {
        return choices;
    }

    public void setChoices(List<Choice> choices) {
        this.choices = choices;
    }

    public String getAssignmentId() {
        return assignmentId;
    }

    public void setAssignmentId(String assignmentId) {
        this.assignmentId = assignmentId;
    }

    public String getSubmissionId() {
        return submissionId;
    }

    public void setSubmissionId(String submissionId) {
        this.submissionId = submissionId;
    }

    public static String submissionIdGenerate() {
        StringBuilder code = new StringBuilder();
        for (int i = 0; i < 6; i++) {
            int randomIndex = random.nextInt(CHARACTERS.length());
            code.append(CHARACTERS.charAt(randomIndex));
        }
        return code.toString();
    }

    private static String getCurrentDateTime() {
        ZoneId zoneId = ZoneId.of("Asia/Bangkok");
        LocalDateTime currentDateTime = LocalDateTime.now(zoneId);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss MM/dd/yyyy");
        String formattedDateTime = currentDateTime.format(formatter);
        return formattedDateTime;
    }

    private String calculateEndTime(String startTimeInStr, int durationInMinutes) {

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss MM/dd/yyyy");
        LocalDateTime startedTime = LocalDateTime.parse(startTimeInStr, formatter);
        LocalDateTime endTime = startedTime.plusMinutes(durationInMinutes);
        String formattedEndtime = endTime.format(formatter);
        return formattedEndtime;
    }
}
